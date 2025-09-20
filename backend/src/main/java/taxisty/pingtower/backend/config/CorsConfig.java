package taxisty.pingtower.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация CORS для PingTower backend.
 * Обеспечивает корректную работу с фронтендом в различных средах развертывания.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Конфигурация CORS для Spring Security
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Разрешенные источники для различных сред
        List<String> allowedOrigins = Arrays.asList(
            // Локальная разработка
            "http://localhost:3000",           // React dev server
            "http://localhost:3001",           // Альтернативный порт
            "http://127.0.0.1:3000",          // Альтернативный localhost
            "http://127.0.0.1:3001",          // Альтернативный localhost
            
            // HTTPS локальная разработка
            "https://localhost:3000",
            "https://localhost:3001",
            "https://127.0.0.1:3000",
            "https://127.0.0.1:3001",
            
            // Docker окружение
            "http://pingtower-frontend",       // Docker контейнер фронтенда
            "http://nginx",                   // Nginx прокси
            "http://localhost",               // Nginx на localhost
            "https://localhost",              // HTTPS Nginx
            
            // Продакшен домены (замените на ваши)
            "https://yourdomain.com",
            "https://www.yourdomain.com"
        );
        
        configuration.setAllowedOrigins(allowedOrigins);
        
        // Разрешенные паттерны для более гибкой настройки
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",             // Любой порт localhost
            "http://127.0.0.1:*",            // Любой порт 127.0.0.1
            "https://localhost:*",            // HTTPS любой порт
            "https://127.0.0.1:*",           // HTTPS любой порт
            "http://*.localhost:*",          // Поддомены localhost
            "https://*.localhost:*",         // HTTPS поддомены
            "http://pingtower-frontend:*",    // Docker контейнер
            "http://nginx:*"                 // Nginx прокси
        ));
        
        // Разрешенные HTTP методы
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));
        
        // Разрешенные заголовки
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",                  // JWT токены
            "Content-Type",                  // Тип контента
            "Accept",                        // Принимаемые типы
            "Origin",                        // Источник запроса
            "Access-Control-Request-Method", // CORS preflight
            "Access-Control-Request-Headers",// CORS preflight
            "X-Requested-With",              // AJAX запросы
            "Cache-Control",                 // Кэширование
            "Pragma",                        // Кэширование
            "X-CSRF-TOKEN",                  // CSRF защита
            "X-Forwarded-For",               // Прокси заголовки
            "X-Forwarded-Proto",             // Прокси заголовки
            "X-Real-IP"                      // Прокси заголовки
        ));
        
        // Заголовки, доступные клиенту
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",                 // JWT токены в ответе
            "Content-Type",                  // Тип контента ответа
            "Access-Control-Allow-Origin",    // CORS заголовки
            "Access-Control-Allow-Credentials",
            "Access-Control-Allow-Methods",
            "Access-Control-Allow-Headers",
            "X-Total-Count",                 // Пагинация
            "X-Page-Count"                   // Пагинация
        ));
        
        // Разрешить отправку cookies и авторизационных заголовков
        configuration.setAllowCredentials(true);
        
        // Максимальное время кэширования preflight запросов (1 час)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Дополнительная конфигурация CORS для Spring MVC
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                    "http://localhost:*",
                    "http://127.0.0.1:*", 
                    "https://localhost:*",
                    "https://127.0.0.1:*",
                    "http://pingtower-frontend:*",
                    "http://nginx:*",
                    "http://*.localhost:*",
                    "https://*.localhost:*"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders(
                    "Authorization",
                    "Content-Type", 
                    "Access-Control-Allow-Origin",
                    "Access-Control-Allow-Credentials",
                    "Access-Control-Allow-Methods",
                    "Access-Control-Allow-Headers",
                    "X-Total-Count",
                    "X-Page-Count"
                )
                .allowCredentials(true)
                .maxAge(3600);
    }
}
