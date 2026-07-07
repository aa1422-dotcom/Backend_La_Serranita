package com.laserranita.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapea la URL /api/v1/uploads/** a la carpeta física definida en upload.path
        String absolutePath = Paths.get(uploadPath).toAbsolutePath().toUri().toString();
        
        registry.addResourceHandler("/api/v1/uploads/**")
                .addResourceLocations(absolutePath);
    }
}
