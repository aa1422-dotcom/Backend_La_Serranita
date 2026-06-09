package com.laserranita.backend.services;

import com.laserranita.backend.models.Producto;
import com.laserranita.backend.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final ProductoRepository productoRepository;
    private final RestTemplate restTemplate;

    public ChatService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
        this.restTemplate = new RestTemplate();
    }

    public String getChatResponse(String userMessage) {
        if (apiKey == null || apiKey.equals("YOUR_OPENAI_API_KEY_HERE") || apiKey.isEmpty()) {
            return "Error: No se ha configurado la API Key de OpenAI en el servidor.";
        }

        List<Producto> productos = productoRepository.findAll();
        String context = buildContext(productos);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", 
            "Eres el 'Asistente de Inventario de La Serranita'. Tu objetivo es ayudar al usuario a entender el estado de sus productos. " +
            "Puedes responder saludos y despedidas de forma amable. " +
            "Si te saludan (hola, buenos días, etc.), responde cordialmente y ofrece una lista de lo que puedes hacer: " +
            "1. Analizar niveles de stock. 2. Reportar productos próximos a vencer. 3. Consultar precios. 4. Resumir estados críticos. " +
            "Mantén siempre el enfoque en los datos del inventario. Si preguntan algo totalmente ajeno (política, deportes, etc.), " +
            "di amablemente que solo estás capacitado para temas de inventario. Datos actuales:\n" + context));
        messages.add(Map.of("role", "user", "content", userMessage));

        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                List choices = (List) response.getBody().get("choices");
                Map firstChoice = (Map) choices.get(0);
                Map message = (Map) firstChoice.get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            // Fallback: Analizamos localmente si falla la conexión
            return getFallbackResponse(userMessage, productos);
        }

        return "Lo siento, no pude procesar tu solicitud en este momento.";
    }

    private String getFallbackResponse(String userMessage, List<Producto> productos) {
        String lower = userMessage.toLowerCase();
        
        // Saludos
        if (lower.contains("hola") || lower.contains("buenos") || lower.contains("buenas")) {
            return "¡Hola! Bienvenido al asistente de La Serranita. Puedo ayudarte con lo siguiente:\n" +
                   "- Consultar stock bajo o agotado.\n" +
                   "- Revisar vencimientos de productos.\n" +
                   "- Ver precios de venta.\n" +
                   "¿Qué dato necesitas revisar hoy?";
        }

        // Despedidas
        if (lower.contains("gracias") || lower.contains("adios") || lower.contains("chau") || lower.contains("luego")) {
            return "¡De nada! Quedo a tu disposición para cualquier otro análisis del inventario. ¡Que tengas un buen día!";
        }

        // Verificamos si la pregunta es sobre inventario
        boolean isInventoryQuery = lower.contains("stock") || lower.contains("bajo") || lower.contains("venc") || 
                                   lower.contains("caduc") || lower.contains("precio") || lower.contains("producto") ||
                                   lower.contains("cuánto") || lower.contains("qué hay") || lower.contains("alerta") ||
                                   lower.contains("lista");

        if (!isInventoryQuery) {
            return "Lo siento, mi capacitación está limitada al análisis de datos de inventario de La Serranita. ¿Deseas que revise el stock o los vencimientos?";
        }

        if (lower.contains("stock") || lower.contains("bajo") || lower.contains("agotado")) {
            List<Producto> lowStock = productos.stream()
                .filter(p -> p.getStockActual() <= (p.getStockMinimo() != null ? p.getStockMinimo() : 5))
                .collect(Collectors.toList());
            if (lowStock.isEmpty()) return "Análisis local: No detecto faltantes. Todos los productos tienen stock suficiente.";
            return "Análisis de Stock Crítico: He encontrado " + lowStock.size() + " productos en alerta. Los más urgentes son: " + 
                   lowStock.stream().limit(5).map(Producto::getNombre).collect(Collectors.joining(", ")) + ".";
        }
        
        if (lower.contains("venc") || lower.contains("vence") || lower.contains("caduc")) {
            LocalDate limit = LocalDate.now().plusMonths(2);
            List<Producto> expiring = productos.stream()
                .filter(p -> p.getFechaVencimiento() != null && p.getFechaVencimiento().isBefore(limit))
                .collect(Collectors.toList());
            if (expiring.isEmpty()) return "Análisis local: El sistema no registra vencimientos en los próximos 60 días.";
            return "Análisis de Vencimientos: Tienes " + expiring.size() + " productos próximos a caducar. Te recomiendo priorizar su salida.";
        }

        return "Entiendo tu consulta sobre inventario. Para darte un dato exacto, ¿prefieres que analice el stock, los vencimientos o los precios?";
    }

    private String buildContext(List<Producto> productos) {
        return productos.stream()
            .map(p -> String.format("- %s: Stock %d (min %d), Precio S/ %.2f, Vence: %s", 
                p.getNombre(), p.getStockActual(), p.getStockMinimo() != null ? p.getStockMinimo() : 5, 
                p.getPrecioVentaActual(), p.getFechaVencimiento() != null ? p.getFechaVencimiento() : "N/A"))
            .collect(Collectors.joining("\n"));
    }
}
