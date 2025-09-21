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
 * РљРѕРЅС„РёРіСѓСЂР°С†РёСЏ CORS РґР»СЏ PingTower backend.
 * РћР±РµСЃРїРµС‡РёРІР°РµС‚ РєРѕСЂСЂРµРєС‚РЅСѓСЋ СЂР°Р±РѕС‚Сѓ СЃ С„СЂРѕРЅС‚РµРЅРґРѕРј РІ СЂР°Р·Р»РёС‡РЅС‹С… СЃСЂРµРґР°С… СЂР°Р·РІРµСЂС‚С‹РІР°РЅРёСЏ.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * РљРѕРЅС„РёРіСѓСЂР°С†РёСЏ CORS РґР»СЏ Spring Security
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Р Р°Р·СЂРµС€РµРЅРЅС‹Рµ РёСЃС‚РѕС‡РЅРёРєРё РґР»СЏ СЂР°Р·Р»РёС‡РЅС‹С… СЃСЂРµРґ
        List<String> allowedOrigins = Arrays.asList(
            // Р›РѕРєР°Р»СЊРЅР°СЏ СЂР°Р·СЂР°Р±РѕС‚РєР°
            "http://localhost:3000",           // React dev server
            "http://localhost:3001",           // РђР»СЊС‚РµСЂРЅР°С‚РёРІРЅС‹Р№ РїРѕСЂС‚
            "http://127.0.0.1:3000",          // РђР»СЊС‚РµСЂРЅР°С‚РёРІРЅС‹Р№ localhost
            "http://127.0.0.1:3001",          // РђР»СЊС‚РµСЂРЅР°С‚РёРІРЅС‹Р№ localhost
            
            // HTTPS Р»РѕРєР°Р»СЊРЅР°СЏ СЂР°Р·СЂР°Р±РѕС‚РєР°
            "https://localhost:3000",
            "https://localhost:3001",
            "https://127.0.0.1:3000",
            "https://127.0.0.1:3001",
            
            // Docker РѕРєСЂСѓР¶РµРЅРёРµ
            "http://pingtower-frontend",       // Docker РєРѕРЅС‚РµР№РЅРµСЂ С„СЂРѕРЅС‚РµРЅРґР°
            "http://nginx",                   // Nginx РїСЂРѕРєСЃРё
            "http://localhost",               // Nginx РЅР° localhost
            "https://localhost",              // HTTPS Nginx
            
            // РџСЂРѕРґР°РєС€РµРЅ РґРѕРјРµРЅС‹ (Р·Р°РјРµРЅРёС‚Рµ РЅР° РІР°С€Рё)
            "https://yourdomain.com",
            "https://www.yourdomain.com"
        );
        
        configuration.setAllowedOrigins(allowedOrigins);
        
        // Р Р°Р·СЂРµС€РµРЅРЅС‹Рµ РїР°С‚С‚РµСЂРЅС‹ РґР»СЏ Р±РѕР»РµРµ РіРёР±РєРѕР№ РЅР°СЃС‚СЂРѕР№РєРё
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",             // Р›СЋР±РѕР№ РїРѕСЂС‚ localhost
            "http://127.0.0.1:*",            // Р›СЋР±РѕР№ РїРѕСЂС‚ 127.0.0.1
            "https://localhost:*",            // HTTPS Р»СЋР±РѕР№ РїРѕСЂС‚
            "https://127.0.0.1:*",           // HTTPS Р»СЋР±РѕР№ РїРѕСЂС‚
            "http://*.localhost:*",          // РџРѕРґРґРѕРјРµРЅС‹ localhost
            "https://*.localhost:*",         // HTTPS РїРѕРґРґРѕРјРµРЅС‹
            "http://pingtower-frontend:*",    // Docker РєРѕРЅС‚РµР№РЅРµСЂ
            "http://nginx:*"                 // Nginx РїСЂРѕРєСЃРё
        ));
        
        // Р Р°Р·СЂРµС€РµРЅРЅС‹Рµ HTTP РјРµС‚РѕРґС‹
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));
        
        // Р Р°Р·СЂРµС€РµРЅРЅС‹Рµ Р·Р°РіРѕР»РѕРІРєРё
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",                  // JWT С‚РѕРєРµРЅС‹
            "Content-Type",                  // РўРёРї РєРѕРЅС‚РµРЅС‚Р°
            "Accept",                        // РџСЂРёРЅРёРјР°РµРјС‹Рµ С‚РёРїС‹
            "Origin",                        // РСЃС‚РѕС‡РЅРёРє Р·Р°РїСЂРѕСЃР°
            "Access-Control-Request-Method", // CORS preflight
            "Access-Control-Request-Headers",// CORS preflight
            "X-Requested-With",              // AJAX Р·Р°РїСЂРѕСЃС‹
            "Cache-Control",                 // РљСЌС€РёСЂРѕРІР°РЅРёРµ
            "Pragma",                        // РљСЌС€РёСЂРѕРІР°РЅРёРµ
            "X-CSRF-TOKEN",                  // CSRF Р·Р°С‰РёС‚Р°
            "X-Forwarded-For",               // РџСЂРѕРєСЃРё Р·Р°РіРѕР»РѕРІРєРё
            "X-Forwarded-Proto",             // РџСЂРѕРєСЃРё Р·Р°РіРѕР»РѕРІРєРё
            "X-Real-IP"                      // РџСЂРѕРєСЃРё Р·Р°РіРѕР»РѕРІРєРё
        ));
        
        // Р—Р°РіРѕР»РѕРІРєРё, РґРѕСЃС‚СѓРїРЅС‹Рµ РєР»РёРµРЅС‚Сѓ
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",                 // JWT С‚РѕРєРµРЅС‹ РІ РѕС‚РІРµС‚Рµ
            "Content-Type",                  // РўРёРї РєРѕРЅС‚РµРЅС‚Р° РѕС‚РІРµС‚Р°
            "Access-Control-Allow-Origin",    // CORS Р·Р°РіРѕР»РѕРІРєРё
            "Access-Control-Allow-Credentials",
            "Access-Control-Allow-Methods",
            "Access-Control-Allow-Headers",
            "X-Total-Count",                 // РџР°РіРёРЅР°С†РёСЏ
            "X-Page-Count"                   // РџР°РіРёРЅР°С†РёСЏ
        ));
        
        // Р Р°Р·СЂРµС€РёС‚СЊ РѕС‚РїСЂР°РІРєСѓ cookies Рё Р°РІС‚РѕСЂРёР·Р°С†РёРѕРЅРЅС‹С… Р·Р°РіРѕР»РѕРІРєРѕРІ
        configuration.setAllowCredentials(true);
        
        // РњР°РєСЃРёРјР°Р»СЊРЅРѕРµ РІСЂРµРјСЏ РєСЌС€РёСЂРѕРІР°РЅРёСЏ preflight Р·Р°РїСЂРѕСЃРѕРІ (1 С‡Р°СЃ)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Р”РѕРїРѕР»РЅРёС‚РµР»СЊРЅР°СЏ РєРѕРЅС„РёРіСѓСЂР°С†РёСЏ CORS РґР»СЏ Spring MVC
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
