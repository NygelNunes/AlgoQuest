package algoquest.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class ConfigWeb implements WebMvcConfigurer {

    private final ResolvedorUsuarioAutenticado resolvedor;
    private final String[] origens;

    public ConfigWeb(ResolvedorUsuarioAutenticado resolvedor,
                     @Value("${algoquest.cors.origens:http://localhost:5500}") String[] origens) {
        this.resolvedor = resolvedor;
        this.origens = origens;
    }

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        registro.addMapping("/api/**")
                .allowedOrigins(origens)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type")
                .maxAge(3600);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvedores) {
        resolvedores.add(resolvedor);
    }
}
