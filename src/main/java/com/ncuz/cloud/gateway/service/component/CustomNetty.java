package com.ncuz.cloud.gateway.service.component;

import com.ncuz.cloud.gateway.service.utility.JSONUtility;
import com.ncuz.encryption.service.PropertiesService;
import com.ncuz.log4j2.helper.service.Log4J2Service;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomNetty implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {
    @Autowired
    private WebSocketHandler webSocketHandler;
    @Autowired
    JSONUtility jsonUtility;
    @Autowired
    Log4J2Service log4J2Service;
    private static Logger logger;
    @PostConstruct
    private void post() throws IOException {
        logger = log4J2Service.getLogger(CustomNetty.class,false);
    }

    @PreDestroy
    private  void destroy(){

    }
    private int maxHeaderSize=65536;
    private JSONArray routeMap=new JSONArray();

    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        JSONObject springCloudConfig= (JSONObject) ((JSONObject) PropertiesService.getBootstrapConfig().get("spring")).get("cloud");
        JSONArray routeList= (JSONArray) springCloudConfig.get("routes");

        for(int i=0;i<routeList.size();i++){
            JSONObject route= (JSONObject) routeList.get(i);
            JSONArray predicates= (route.get("predicates")!=null)?jsonUtility.convertBodyToJSONArray(route.get("predicates").toString()):null ;
            JSONArray filters= (route.get("filters")!=null)?jsonUtility.convertBodyToJSONArray(route.get("filters").toString()):null ;

            String uri=(route.get("uri")!=null)?route.get("uri").toString():"";
            route.put("id",route.get("id"));
            route.put("uri",route.get("uri"));
            route.put("predicates",predicates);
            route.put("filters",filters);
            routeMap.add(route);
        }
        factory.addServerCustomizers(httpServer -> httpServer.httpRequestDecoder(
                        httpRequestDecoderSpec -> httpRequestDecoderSpec
                                .maxHeaderSize(maxHeaderSize)
                                .maxInitialLineLength(maxHeaderSize)
                )
        );

    }
    @Bean
    public HandlerMapping webSocketMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
//        map.put("/custom.job.manager.ws/sock.web.socket", webSocketHandler);
//        map.put("/custom.job.manager.ws/web.socket", webSocketHandler);
//        map.put("/custom.job.manager/**", webSocketHandler);
        getApiRoutes().map(i->{
            if(i.get("id").toString().contains("#web-socket")){
                JSONArray predicates=(i.get("predicates")!=null)? jsonUtility.convertBodyToJSONArray(i.get("predicates").toString()):null;
                if(predicates!=null)
                    for(int k=0;k<predicates.size();k++){
                        JSONObject predicate= (JSONObject) predicates.get(k);
//                        logger.warn("predicate :"+predicate);
                        if(predicate.get("Path")!=null){
                            String[] paths=predicate.get("Path").toString().split(",");
                            for(int l=0;l<paths.length;l++){
//                                logger.warn("Path :"+paths[l]);
//                                map.put(paths[l], webSocketHandler);
                            }
                        }
                    }
            }
//            logger.warn("ROUTE ID: {}", i.get("id"));
//            logger.warn("ROUTE URI: {}", i.get("uri"));
//            logger.warn("ROUTE PREDICATES: {}", i.get("predicates"));
//            logger.warn("ROUTE FILTERS: {}", i.get("filters"));
//            logger.trace("########################################################");
            return i;
        }).subscribe();


        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(1);
        handlerMapping.setUrlMap(map);
        return handlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    public Flux<Route> buildCustomRouteLocator(RouteLocatorBuilder routeLocatorBuilder){
        RouteLocatorBuilder.Builder routesBuilder = routeLocatorBuilder.routes();
//        logger.warn("buildCustomRouteLocator :"+routesBuilder);
        return routesBuilder.build().getRoutes();
    }



    public JSONArray getRouteMap() {
        return routeMap;
    }

    public Flux<JSONObject> getApiRoutes()   {

        return Mono.just(routeMap)
                .flatMapMany(msg -> {
                    return Flux.fromIterable(msg);
                } );
    }
}
