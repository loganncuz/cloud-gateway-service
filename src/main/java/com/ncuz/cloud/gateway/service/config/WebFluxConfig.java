package com.ncuz.cloud.gateway.service.config;

//import org.springframework.context.annotation.Bean;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.web.server.SecurityWebFilterChain;

import com.ncuz.log4j2.helper.service.Log4J2Service;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import java.net.URI;

import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
//@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer
        ,WebFilter
{
    @Autowired
    Log4J2Service log4J2Service;
    private static Logger logger;
    @PostConstruct
    private void post() throws IOException {
        logger = log4J2Service.getLogger(WebFluxConfig.class,false);
    }

    @PreDestroy
    private  void destroy(){

    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        logger.debug("WebFluxConfig :"+exchange.getRequest().getURI()+" | "+
                chain.toString());
        if (exchange.getRequest().getURI().getPath().equals("/")) {
            logger.debug("1 :"+exchange.getRequest().getURI().getPath());
            return chain.filter(exchange.mutate().request(exchange.getRequest().mutate().path("/index.html").build()).build());
        }else{
            if (exchange.getRequest().getURI().getPath().contains("/eureka/")) {
//                URI myUri = null;
//                try {
//                    myUri = new URI("https://localhost:9999"+exchange.getRequest().getURI().getPath());
//                    URI finalMyUri = myUri;
//                    logger.debug("2 :"+myUri);
//                    return chain.filter(exchange.mutate().request(c -> c.uri(finalMyUri).build()).build());
//                } catch (URISyntaxException e) {
//                    e.printStackTrace();
//                }
            }
            logger.debug("3 :"+exchange);
        }
//        logger.debug("*************************************");
        return chain.filter(exchange);
    }

    @Bean
    RouterFunction<ServerResponse> staticResourceRouter(){
        logger.debug("**************** staticResourceRouter *********************");
        return RouterFunctions.resources("/**", new ClassPathResource("public/"));
    }
    @Bean
    RouterFunction<ServerResponse> routerFunction(GatewayHandler gatewayHandler) {
        logger.debug("**************** routerFunction *********************");
        return route(GET("/fallback/**"), gatewayHandler::getFallback)
//                .and(route(GET("/discovery/**"), gatewayHandler::getDiscovery))
//                .and(route(GET("/custom.job/**"), gatewayHandler::getWebSocket))
                ;
    }



    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addExposedHeader(HttpHeaders.SET_COOKIE);
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(corsConfigurationSource);
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("*");
    }

    @Component
    class GatewayHandler {
        public Mono<ServerResponse> getWebSocket(ServerRequest serverRequest) {

            String url=serverRequest.uri().getPath().replace("/custom.job/","");
            logger.debug("*************** getWebSocket **********************"+serverRequest.uri().getPath()+" | "+
                    serverRequest.uri());
            return ServerResponse.temporaryRedirect(URI.create("/#/fallback/?id="+url)).build();
        }
        public Mono<ServerResponse> getFallback(ServerRequest serverRequest) {

            String url=serverRequest.uri().getPath().replace("/fallback/","");
            logger.debug("*************** getFallback **********************"+serverRequest.uri().getPath());
            return ServerResponse.temporaryRedirect(URI.create("/#/fallback/?id="+url)).build();
        }
        public Mono<ServerResponse> getDiscovery(ServerRequest serverRequest) {
            String url=serverRequest.uri().toString();
            url=url.replace(String.valueOf(serverRequest.uri().getPort()),"9999");
            url=url.replace(serverRequest.uri().getPath(),"/");
            int index=url.indexOf("?id=");
            url=url.substring(0,index);
//            String url=serverRequest.uri().getPath().replace("/discovery/","/");
            logger.debug("*************** getDiscovery ********************** "+serverRequest.uri().getPath()+" | "+url
                    +" | "+index);
            return ServerResponse.temporaryRedirect(URI.create(url)).build();
        }
    }

}
