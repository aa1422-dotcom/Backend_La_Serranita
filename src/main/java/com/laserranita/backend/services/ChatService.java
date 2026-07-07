package com.laserranita.backend.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laserranita.backend.models.MovimientoInventario;
import com.laserranita.backend.models.Producto;
import com.laserranita.backend.models.Venta;
import com.laserranita.backend.repositories.MovimientoInventarioRepository;
import com.laserranita.backend.repositories.ProductoRepository;
import com.laserranita.backend.repositories.VentaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Value("${gemini.api.key:}") // Se puede inyectar desde variables de entorno o properties
    private String geminiApiKey;

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(ProductoRepository productoRepository,
                       VentaRepository ventaRepository,
                       MovimientoInventarioRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.movimientoRepository = movimientoRepository;
    }

    public String procesarConsultaChat(String consultaUsuario) {
        // 1. Obtener datos en tiempo real de la base de datos para alimentar el contexto (RAG)
        List<Producto> productosBajoStock = productoRepository.findLowStock();
        List<Producto> productosProximosVencer = productoRepository.findExpiringSoon(LocalDate.now().plusMonths(2));
        List<Venta> todasLasVentas = ventaRepository.findAll();
        List<MovimientoInventario> todosMovimientos = movimientoRepository.findAll();

        // 2. Calcular estadísticas clave para el prompt
        int totalProductos = (int) productoRepository.count();
        BigDecimal totalVentasDinero = todasLasVentas.stream()
                .map(v -> v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalVentasCantidad = todasLasVentas.size();

        // Obtener nombres de productos con bajo stock
        String listaBajoStock = productosBajoStock.stream()
                .map(p -> p.getNombre() + " (Stock actual: " + p.getStockActual() + ", Mínimo: " + p.getStockMinimo() + ")")
                .collect(Collectors.joining(", "));
        if (listaBajoStock.isEmpty()) {
            listaBajoStock = "Ninguno. El stock está óptimo.";
        }

        // Obtener nombres de productos próximos a vencer
        String listaProximosVencer = productosProximosVencer.stream()
                .map(p -> p.getNombre() + " (Vence: " + p.getFechaVencimiento() + ")")
                .collect(Collectors.joining(", "));
        if (listaProximosVencer.isEmpty()) {
            listaProximosVencer = "Ninguno en los próximos 2 meses.";
        }

        // Obtener los últimos 3 movimientos de inventario
        String listaMovimientosRecientes = todosMovimientos.stream()
                .sorted((a, b) -> {
                    if (a.getFechaMovimiento() == null || b.getFechaMovimiento() == null) return 0;
                    return b.getFechaMovimiento().compareTo(a.getFechaMovimiento());
                })
                .limit(3)
                .map(m -> "- " + m.getTipoMovimiento() + " de " + m.getCantidad() + " unidades de '" 
                        + (m.getProducto() != null ? m.getProducto().getNombre() : "N/A") + "' (Obs: " + m.getObservacion() + ")")
                .collect(Collectors.joining("\n"));
        if (listaMovimientosRecientes.isEmpty()) {
            listaMovimientosRecientes = "No hay movimientos registrados.";
        }

        // 3. Construir el Prompt del Sistema con RAG (Datos reales)
        String systemPrompt = String.format("""
                Eres el asistente analítico inteligente y consultor de negocios de "La Serranita" (un minimarket/tienda de abarrotes).
                Tu objetivo es ayudar al administrador a comprender el estado de su negocio respondiendo preguntas de forma profesional, clara y concisa en español.
                
                TIENES ACCESO A LOS SIGUIENTES DATOS EN TIEMPO REAL EXTRAÍDOS DE LA BASE DE DATOS:
                - Total de productos en catálogo: %d
                - Total de ventas acumuladas: S/ %.2f (%d transacciones realizadas)
                - Productos con STOCK CRÍTICO (bajo el mínimo o agotado): %d productos. Lista: %s
                - Productos PRÓXIMOS A VENCER (en menos de 2 meses): %d productos. Lista: %s
                - Últimos movimientos de inventario registrados:
                %s
                
                INSTRUCCIONES CLAVE:
                - Usa siempre tonos profesionales, cordiales y analíticos.
                - Responde de forma resumida y al grano. No uses explicaciones innecesariamente largas.
                - Si el usuario te pregunta por estadísticas, montos, bajo stock o vencimientos, utiliza ÚNICAMENTE los datos reales provistos arriba.
                - Si el usuario te hace una pregunta general de gestión o administración de tiendas, respóndela basándote en buenas prácticas de negocios, orientada al contexto de un minimarket.
                - Si te preguntan por información que no posees en los datos reales provistos, indícalo de manera educada.
                """, 
                totalProductos,
                totalVentasDinero,
                totalVentasCantidad,
                productosBajoStock.size(),
                listaBajoStock,
                productosProximosVencer.size(),
                listaProximosVencer,
                listaMovimientosRecientes
        );

        // 4. Llamar a la API de Gemini si hay API Key disponible
        String apiKeyClean = geminiApiKey != null ? geminiApiKey.trim() : "";
        if (apiKeyClean.startsWith("\"") && apiKeyClean.endsWith("\"")) {
            apiKeyClean = apiKeyClean.substring(1, apiKeyClean.length() - 1);
        }
        if (apiKeyClean.startsWith("'") && apiKeyClean.endsWith("'")) {
            apiKeyClean = apiKeyClean.substring(1, apiKeyClean.length() - 1);
        }
        apiKeyClean = apiKeyClean.trim();

        System.out.println("DEBUG CHAT: ¿API Key de Gemini detectada? -> " + (!apiKeyClean.isEmpty()));
        if (!apiKeyClean.isEmpty()) {
            try {
                String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKeyClean;

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // Construimos la estructura del JSON para Gemini API
                Map<String, Object> requestBody = new HashMap<>();
                List<Map<String, Object>> contents = new ArrayList<>();
                Map<String, Object> contentNode = new HashMap<>();
                contentNode.put("role", "user");
                
                List<Map<String, String>> parts = new ArrayList<>();
                Map<String, String> partNode = new HashMap<>();
                // Enviamos el contexto del sistema + la pregunta del usuario en el mismo mensaje
                partNode.put("text", systemPrompt + "\nPregunta del Usuario: " + consultaUsuario);
                parts.add(partNode);
                
                contentNode.put("parts", parts);
                contents.add(contentNode);
                requestBody.put("contents", contents);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

                System.out.println("DEBUG CHAT: Respuesta HTTP de Gemini: " + response.getStatusCode());

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    String textResponse = root.path("candidates")
                            .path(0)
                            .path("content")
                            .path("parts")
                            .path(0)
                            .path("text")
                            .asText();
                    if (!textResponse.isBlank()) {
                        System.out.println("DEBUG CHAT: Respuesta generada exitosamente por Gemini.");
                        return textResponse;
                    }
                }
            } catch (Exception e) {
                System.err.println("DEBUG CHAT ERROR: Error crítico al llamar a la API de Gemini.");
                e.printStackTrace();
            }
        } else {
            System.out.println("DEBUG CHAT: No se suministró una API Key válida. Saltando al motor de fallback.");
        }

        // 5. Motor de Reglas Offline (Fallback en caso de que no haya API Key o falle la conexión)
        return obtenerRespuestaFallback(consultaUsuario, productosBajoStock.size(), productosProximosVencer.size(), totalProductos, totalVentasDinero);
    }

    private String obtenerRespuestaFallback(String query, int lowStock, int expiring, int totalProds, BigDecimal sales) {
        String queryLower = query.toLowerCase();
        
        if (queryLower.contains("stock") || queryLower.contains("bajo") || queryLower.contains("reposicion") || queryLower.contains("falta")) {
            return String.format("Actualmente tenemos %d productos con stock crítico en la base de datos. Por favor, revise la sección de 'Reportes' en el menú principal para ver el listado detallado e iniciar las compras de reposición.", lowStock);
        }
        
        if (queryLower.contains("vence") || queryLower.contains("vencimiento") || queryLower.contains("caduc") || queryLower.contains("fecha")) {
            return String.format("El sistema registra %d productos próximos a vencer (menos de 2 meses de vigencia). Le sugiero revisar el módulo de 'Reportes' o el panel de notificaciones para aplicar descuentos de salida rápida y evitar pérdidas.", expiring);
        }

        if (queryLower.contains("venta") || queryLower.contains("ganancia") || queryLower.contains("dinero") || queryLower.contains("semana")) {
            return String.format("En total se registran ventas acumuladas por S/ %.2f en el sistema. Para ver la tendencia diaria detallada, le recomiendo revisar las gráficas interactivas del 'Dashboard'.", sales);
        }

        if (queryLower.contains("resumen") || queryLower.contains("inventario") || queryLower.contains("total") || queryLower.contains("productos")) {
            return String.format("El minimarket cuenta con %d productos en catálogo. El %d%% de los productos requiere atención por niveles bajos de stock.", totalProds, (int)(((double)lowStock/totalProds)*100));
        }

        if (queryLower.contains("hola") || queryLower.contains("buenos")) {
            return "¡Hola! Gusto en saludarte. Soy el asistente analítico del sistema. (Nota: Actualmente estoy funcionando en modo local sin llave API). Puedo brindarte resúmenes rápidos del inventario, ventas acumuladas y stock crítico si me preguntas sobre ellos.";
        }

        return "Lo siento, como asistente local offline tengo limitaciones para responder a esta consulta. Por favor, ingresa una pregunta referente al 'stock bajo', 'ventas acumuladas', 'productos próximos a vencer' o 'resumen del inventario'.";
    }
}
