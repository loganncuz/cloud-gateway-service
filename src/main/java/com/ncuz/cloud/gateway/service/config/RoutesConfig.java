package com.ncuz.cloud.gateway.service.config;

import com.ncuz.cloud.gateway.service.base.CustomRouteLocator;
import com.ncuz.cloud.gateway.service.component.CustomNetty;
import com.ncuz.cloud.gateway.service.utility.DateTimeUtilities;
import com.ncuz.log4j2.helper.service.Log4J2Service;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
public class RoutesConfig {
    @Autowired
    DateTimeUtilities dateTimeUtilities;
    @Autowired
    Log4J2Service log4J2Service;
    private static Logger logger;
    private static Logger loggerCustomRouteLocator;
    @PostConstruct
    private void post() throws IOException {
        logger = log4J2Service.getLogger(RoutesConfig.class,false);
        loggerCustomRouteLocator = log4J2Service.getLogger(CustomRouteLocator.class,false);
    }

    @PreDestroy
    private  void destroy(){

    }
    @Bean
    public RouteLocator customGatewayRoutes(RouteLocatorBuilder builder, CustomNetty customNetty) {
        return new CustomRouteLocator(builder,customNetty,loggerCustomRouteLocator,dateTimeUtilities);
    }
//    @Bean
    public RouteLocator basicGatewayRoutes(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> {
                            return r.path("/uim.task/**")
                            .uri("lb://UIM-TASK-SCHEDULER")
                            .id("uim-task-scheduler");
                        }
                )
//                .route(r -> {
//                            return r.path("/custom.job.manager/static/**","/custom.job.manager/manager/**","/custom.job.manager/manifest.json")
//                                    .filters(f -> f.setResponseHeader("X-Frame-Options","SAMEORIGIN"))
//                                    .uri("lb://custom-job-manager")
//                                    .id("custom-job-manager");
//                        }
//                )
                .route(r -> {
//                    System.out.println("r :"+r.toString());
                            return r.path("/ws/custom.job-manager/sock.web.socket",
                                            "/ws/custom.job-manager/web.socket")
//                                    .filters(f -> f.setRequestSize(50000000L))
                                    .filters(f -> f.stripPrefix(1))
//                            .filters(f -> f.rewritePath("/custom.job/(?<remains>.*)", "/custom-job-manager/${remains}"))
                                    .uri("lb://CUSTOM-JOB-MANAGER")
                                    .id("custom.job.manager#web-socket");
                        }
                )
                .build();
    }
}
