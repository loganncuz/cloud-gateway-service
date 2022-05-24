package com.ncuz.cloud.gateway.service.base;

import com.ncuz.cloud.gateway.service.component.CustomNetty;
import com.ncuz.cloud.gateway.service.utility.DateTimeUtilities;
import com.ncuz.encryption.service.PropertiesService;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RequestPredicates;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class CustomRouteLocator implements RouteLocator {

    private final DateTimeUtilities dateTimeUtilities;
    private final RouteLocatorBuilder builder;
    @Autowired
    CustomNetty customNetty;
    private final Logger logger;
    public CustomRouteLocator(RouteLocatorBuilder builder, CustomNetty customNetty, Logger logger,DateTimeUtilities dateTimeUtilities){
        this.builder=builder;
        this.customNetty=customNetty;
        this.logger=logger;
        this.dateTimeUtilities=dateTimeUtilities;
    }
    @Override
    public Flux<Route> getRoutes() {
        try {
            PropertiesService.loadBootstrapsProperties(Paths.get("./bootstrap.yml"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        logger.trace("------------ GENERATE ROUTES FROM CONFIGURATION FILES -----------------------------------");
        RouteLocatorBuilder.Builder routesBuilder = builder.routes();
            return (Flux<Route>) customNetty.getApiRoutes()
                    .map(apiRoute -> routesBuilder.route(String.valueOf(String.valueOf(apiRoute.get("id"))),
                            predicateSpec -> setPredicateSpec(apiRoute, predicateSpec)
                            )
                    )

                    .collectList()
                    .flatMapMany(builders -> {
                                return routesBuilder.build()
                                        .getRoutes();
                            }
                    );
    }

    int itemIndex=-1;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Route.AsyncBuilder setPredicateSpec(JSONObject apiRoute, PredicateSpec predicateSpec) {
//        logger.warn("ROUTE ID: {}", apiRoute.get("id"));
//        logger.warn("ROUTE URI: {}", apiRoute.get("uri"));
//        logger.warn("ROUTE PREDICATES: {}", apiRoute.get("predicates"));
//        logger.warn("ROUTE FILTERS: {}", apiRoute.get("filters"));
        BooleanSpec booleanSpec=null;
        GatewayFilterSpec gatewayFilterSpec = null;
//        logger.trace("**************************************************************************************");
        if(apiRoute.get("predicates")!=null) {
//            logger.trace("------------------- PREDICATES CONFIGURATION -----------------------------------------");
            booleanSpec = setPath((JSONArray) apiRoute.get("predicates"), predicateSpec, itemIndex);
            booleanSpec = setMethod((JSONArray) apiRoute.get("predicates"), booleanSpec, itemIndex);
            booleanSpec = setHost((JSONArray) apiRoute.get("predicates"), booleanSpec, itemIndex);
            booleanSpec = setHeader((JSONArray) apiRoute.get("predicates"), booleanSpec, itemIndex);
            booleanSpec = setQuery((JSONArray) apiRoute.get("predicates"), booleanSpec, itemIndex);
            booleanSpec = setCookie((JSONArray) apiRoute.get("predicates"), booleanSpec, itemIndex);
            booleanSpec = setRemoteAddress((JSONArray) apiRoute.get("predicates"), booleanSpec, itemIndex);
            booleanSpec = setAfter((JSONArray) apiRoute.get("predicates"), booleanSpec, itemIndex);
            booleanSpec = setBefore((JSONArray) apiRoute.get("predicates"), booleanSpec, itemIndex);
            booleanSpec = setBetween((JSONArray) apiRoute.get("predicates"), booleanSpec, itemIndex);
            booleanSpec = setWeight((JSONArray) apiRoute.get("predicates"), booleanSpec, itemIndex);
//            logger.trace("**************************************************************************************");
        }
        if(apiRoute.get("filters")!=null) {
//            logger.trace("--------------------- FILTERS CONFIGURATION ------------------------------------------");
            booleanSpec.filters(f -> f=setFiltersSpec(apiRoute,f));
//            logger.trace("**************************************************************************************");
        }
//        logger.trace("######################################################################################\n");

        String uri=(apiRoute.get("uri")!=null)?apiRoute.get("uri").toString():"";
        return booleanSpec.uri(uri).metadata(setMetaData(apiRoute));
    }

    private Map<String, Object> setMetaData(JSONObject apiRoute) {
        Map<String, Object> result= new HashMap<String,Object>();
        JSONObject metadata = (JSONObject) apiRoute.get("metadata");
        if(metadata != null){
            logger.debug("setMetaData :"+apiRoute.get("id")+" | "+metadata);
            for (Object key : metadata.keySet()) {
                String name= (String) key;
                Object value= metadata.get(key);
                result.put(name,value);
                logger.debug("Meta :"+name+" | "+result.get(name));
            }
        }

        return result;
    }

    private GatewayFilterSpec setFiltersSpec(JSONObject apiRoute, GatewayFilterSpec gatewayFilterSpec) {
//        logger.warn("setFiltersSpec "+gatewayFilterSpec );
        gatewayFilterSpec = setAddRequestHeader((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setAddResponseHeader((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setAddRequestParameter((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setDedupeResponseHeader((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setHystrixCircuitBreaker((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setCircuitBreaker((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setTrippingCircuitBreaker((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setFallbackHeaders((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setMapRequestHeader((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setPrefixPath((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setRedirectTo((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setPreserveHostHeader((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setRemoveRequestHeader((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setRemoveResponseHeader((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setRemoveRequestParameter((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setRewritePath((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setRewriteLocationResponseHeader((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setRewriteResponseHeader((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setSetPath((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setSetRequestHeader((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setSetResponseHeader((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setSetStatus((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setStripPrefix((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setSaveSession((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setRetry((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setRequestSize((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setSetRequestHostHeader((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setModifyRequestBody((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
        gatewayFilterSpec = setModifyResponseBody((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
//        gatewayFilterSpec = setRequestRateLimiter((JSONArray) apiRoute.get("filters"), gatewayFilterSpec, itemIndex);
    return gatewayFilterSpec;
    }
    private GatewayFilterSpec setAddRequestHeader(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"AddRequestHeader",index)) {
//            logger.warn("setAddRequestHeader "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("AddRequestHeader").toString().split(",");
//            logger.debug(" setAddRequestHeader "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            String regex="";
            if(items.length>=1){
                name=items[0];
                try {
                    regex=items[1];
                }catch(Exception e){
                    logger.error(" setAddRequestHeader "+e);
                }

            }
            gatewayFilterSpec.addRequestHeader(name,regex);
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setAddResponseHeader(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"AddResponseHeader",index)) {
//            logger.warn("setAddResponseHeader "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("AddResponseHeader").toString().split(",");
//            logger.debug(" setAddResponseHeader "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            String regex="";
            if(items.length>=1){
                name=items[0];
                try {
                    regex=items[1];
                }catch(Exception e){
                    logger.error(" setAddResponseHeader "+e);
                }

            }
            gatewayFilterSpec.addResponseHeader(name,regex);
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setAddRequestParameter(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"AddRequestParameter",index)) {
//            logger.warn("setAddRequestParameter "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("AddRequestParameter").toString().split(",");
//            logger.debug(" setAddRequestParameter "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            String regex="";
            if(items.length>=1){
                name=items[0];
                try {
                    regex=items[1];
                }catch(Exception e){
                    logger.error(" setAddRequestParameter "+e);
                }

            }
            gatewayFilterSpec.addRequestParameter(name,regex);
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setDedupeResponseHeader(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"DedupeResponseHeader",index)) {
//            logger.warn("setDedupeResponseHeader "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("DedupeResponseHeader").toString().split(",");
//            logger.debug(" setAddRequestParameter "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            String strategy="RETAIN_FIRST";
            if(items.length>=1){
                name=items[0];
                try {
                    strategy=items[1];
                }catch(Exception e){
                    logger.error(" setAddRequestParameter "+e);
                }

            }
            gatewayFilterSpec.dedupeResponseHeader(name,strategy);
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setCircuitBreaker(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"CircuitBreaker",index)) {
//            logger.warn("CircuitBreaker "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            gatewayFilterSpec.circuitBreaker(c -> c.setName(data.get("CircuitBreaker").toString()));
        }
        return gatewayFilterSpec;
    }

    private GatewayFilterSpec setTrippingCircuitBreaker(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterByValueExist(filter,"CircuitBreaker",index)) {
//            logger.warn("TrippingCircuitBreaker "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            try {
                JSONObject args = (JSONObject) data.get("args");
//                String[] items = args.get("statusCodes").toString().split(",");
//                logger.warn("TrippingCircuitBreaker data " + data + " | " + args.get("fallbackUri").toString());
                gatewayFilterSpec.circuitBreaker(c ->  c.setName(args.get("name").toString())
                        .setFallbackUri(args.get("fallbackUri").toString())
                        );
            }catch(Exception e){
                logger.error(" TrippingCircuitBreaker "+e);
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setHystrixCircuitBreaker(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterByValueExist(filter,"Hystrix",index)) {
//            logger.warn("HystrixCircuitBreaker "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            try {
                JSONObject args = (JSONObject) data.get("args");
//                String[] items = args.get("statusCodes").toString().split(",");
//                logger.warn("HystrixCircuitBreaker data " + data + " | " + args.get("fallbackUri").toString());
                gatewayFilterSpec.hystrix(c ->  c.setName(args.get("name").toString())
                        .setFallbackUri(args.get("fallbackUri").toString())
                );
            }catch(Exception e){
                logger.error(" TrippingCircuitBreaker "+e);
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setFallbackHeaders(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterByValueExist(filter,"FallbackHeaders",index)) {
//            logger.warn("setFallbackHeaders "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            try {
                JSONObject args = (JSONObject) data.get("args");
//                logger.warn("setFallbackHeaders data " + data + " | " + args.get("executionExceptionTypeHeaderName").toString());
                gatewayFilterSpec.fallbackHeaders(c->c.setExecutionExceptionTypeHeaderName(args.get("executionExceptionTypeHeaderName").toString()));
            }catch(Exception e){
                logger.error(" setFallbackHeaders "+e);
            }
//
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setMapRequestHeader(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"MapRequestHeader",index)) {
//            logger.warn("setMapRequestHeader "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("MapRequestHeader").toString().split(",");
//            logger.debug(" setMapRequestHeader "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            String to="";
            if(items.length>=1){
                name=items[0];
                try {
                    to=items[1];
                }catch(Exception e){
                    logger.error(" setMapRequestHeader "+e);
                }

            }
            gatewayFilterSpec.mapRequestHeader(name,to);
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setPrefixPath(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"PrefixPath",index)) {
//            logger.warn("setPrefixPath "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("PrefixPath").toString().split(",");
//            logger.debug(" setPrefixPath "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            if(items.length>=1){
                name=items[0];
                gatewayFilterSpec.prefixPath(name);
            }

        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setPreserveHostHeader(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterWithoutValueExist(filter,"PreserveHostHeader",index)) {
//            logger.warn("setPreserveHostHeader "+itemIndex +" | "+filter.get(itemIndex) );
            gatewayFilterSpec.preserveHostHeader();
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setRequestRateLimiter(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){

        if(isFilterByValueExist(filter,"RequestRateLimiter",index)) {
//            logger.warn("setRequestRateLimiter "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            try {
                JSONObject args = (JSONObject) data.get("args");
//                logger.warn("setRequestRateLimiter data " + data + " | " + args.get("redis-rate-limiter.replenishRate").toString());
//                gatewayFilterSpec.requestRateLimiter(c-> c.); STILL EXPLORE
            }catch(Exception e){
                logger.error(" setRequestRateLimiter "+e);
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setRedirectTo(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"RedirectTo",index)) {
//            logger.warn("setRedirectTo "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("RedirectTo").toString().split(",");
//            logger.debug(" setRedirectTo "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            String to="";
            if(items.length>=1){
                name=items[0];
                try {
                    to=items[1];
                    gatewayFilterSpec.redirect(name,to);
                }catch(Exception e){
                    logger.error(" setRedirectTo "+e);
                }
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setRemoveRequestHeader(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"RemoveRequestHeader",index)) {
//            logger.warn("setRemoveRequestHeader "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("RemoveRequestHeader").toString().split(",");
//            logger.debug(" setRemoveRequestHeader "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            if(items.length>=1){
                name=items[0];
                gatewayFilterSpec.removeRequestHeader(name);
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setRemoveResponseHeader(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"RemoveResponseHeader",index)) {
//            logger.warn("setRemoveResponseHeader "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("RemoveResponseHeader").toString().split(",");
//            logger.debug(" setRemoveResponseHeader "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            if(items.length>=1){
                name=items[0];
                gatewayFilterSpec.removeResponseHeader(name);
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setRemoveRequestParameter(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"RemoveRequestParameter",index)) {
//            logger.warn("setRemoveRequestParameter "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("RemoveRequestParameter").toString().split(",");
//            logger.debug(" setRemoveRequestParameter "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            if(items.length>=1){
                name=items[0];
                gatewayFilterSpec.removeRequestParameter(name);
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setRewritePath(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"RewritePath",index)) {
//            logger.warn("setRewritePath "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("RewritePath").toString().split(",");
//            logger.debug(" setRewritePath "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            String to="";
            if(items.length>=1){
                name=items[0];
                try {
                    to=items[1];
//                    logger.debug(" setRewritePath name :"+name +" | to: "+ to);
                    gatewayFilterSpec.rewritePath(name,to);
                }catch(Exception e){
                    logger.error(" setRewritePath "+e);
                }
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setRewriteLocationResponseHeader(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"RewriteLocationResponseHeader",index)) {
//            logger.warn("setRewriteLocationResponseHeader "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("RewriteLocationResponseHeader").toString().split(",");
//            logger.debug(" setRewriteLocationResponseHeader "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            String mode="";
            String host="";
            String regex="";
            if(items.length>=4){
                mode=items[0];
                try {
                    name=items[1];
                    host=items[2];
                    regex=items[3];
                    gatewayFilterSpec.rewriteLocationResponseHeader(mode,name,host,regex);
                }catch(Exception e){
                    logger.error(" setRewriteLocationResponseHeader "+e);
                }
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setRewriteResponseHeader(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"RewriteResponseHeader",index)) {
//            logger.warn("setRewriteResponseHeader "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("RewriteResponseHeader").toString().split(",");
//            logger.debug(" setRewriteResponseHeader "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            String mode="";
            String host="";
            if(items.length>=4){
                mode=items[0];
                try {
                    name=items[1];
                    host=items[2];
                    gatewayFilterSpec.rewriteResponseHeader(mode,name,host);
                }catch(Exception e){
                    logger.error(" setRewriteResponseHeader "+e);
                }
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setSaveSession(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterWithoutValueExist(filter,"SaveSession",index)) {
//            logger.warn("setSaveSession "+itemIndex +" | "+filter.get(itemIndex) );
            gatewayFilterSpec.saveSession();
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setSetPath(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"SetPath",index)) {
//            logger.warn("setSetPath "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("SetPath").toString().split(",");
//            logger.debug(" setSetPath "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            if(items.length>=1){
                name=items[0];
                gatewayFilterSpec.setPath(name);
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setSetRequestHeader(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"SetRequestHeader",index)) {
//            logger.warn("setSetRequestHeader "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("SetRequestHeader").toString().split(",");
//            logger.debug(" setSetRequestHeader "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            String to="";
            if(items.length>=1){
                name=items[0];
                try {
                    to=items[1];
                    gatewayFilterSpec.setRequestHeader(name,to);
                }catch(Exception e){
                    logger.error(" setSetRequestHeader "+e);
                }
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setSetResponseHeader(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"SetResponseHeader",index)) {
//            logger.warn("setSetResponseHeader "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("SetResponseHeader").toString().split(",");
//            logger.debug(" setSetResponseHeader "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            String to="";
            if(items.length>=1){
                name=items[0];
                try {
                    to=items[1];
                    gatewayFilterSpec.setResponseHeader(name,to);
                }catch(Exception e){
                    logger.error(" setSetResponseHeader "+e);
                }
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setSetStatus(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"SetStatus",index)) {
//            logger.warn("setSetStatus "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("SetStatus").toString().split(",");
//            logger.debug(" setSetStatus "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            if(items.length>=1){
                name=items[0];
                gatewayFilterSpec.setStatus(name);
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setStripPrefix(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"StripPrefix",index)) {
//            logger.warn("setStripPrefix "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            String[] items = data.get("StripPrefix").toString().split(",");
//            logger.debug(" setStripPrefix "+itemIndex +" | "+ Arrays.asList(items));
            String prefix="";
            if(items.length>=1){
                prefix=items[0];
                try{
                    gatewayFilterSpec.stripPrefix(Integer.parseInt(prefix));
                }catch(Exception e){
                    logger.error(" setStripPrefix "+e);
                }

            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setRetry(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterByValueExist(filter,"Retry",index)) {
//            logger.warn("setRetry "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            try {
                JSONObject args = (JSONObject) data.get("args");
                String retries=args.get("retries").toString();
                JSONObject backoff = null;
                String item = null;
                Duration firstBackoff = null;
                Duration maxBackoff = null;
                int factor = 0;
                boolean basedOnPreviousValue = false;
                boolean disabled=true;
                String[] items;
                try {
                    backoff= (JSONObject) args.get("backoff");
                    disabled=false;
                }catch(Exception e){

                    item = (String) args.get("backoff");
                    if(item.equals("disabled"))
                        disabled=true;
                }
//                logger.warn("setRetry data "+disabled);
                items= args.get("statuses").toString().split(",");
                HttpStatus[] statuses= new HttpStatus[items.length];
                for(int i=0;i<items.length;i++){
                    statuses[i]=HttpStatus.valueOf(items[i]);
                };
                items = args.get("methods").toString().split(",");
                HttpMethod[] methods = new HttpMethod[items.length];
                for(int i=0;i<items.length;i++){
                    methods[i]=HttpMethod.valueOf(items[i]);
                };
                Class<? extends Throwable>[] exception = new Class[items.length];
                if(args.get("exceptions")!=null) {
                    items = args.get("exceptions").toString().split(",");
                    exception = new Class[items.length];
                    for(int i=0;i<items.length;i++){
                        ConnectException d;
                        exception[i]= (Class<? extends Throwable>) Class.forName(items[i]);
                    };
                }
                Class<? extends Throwable>[] finalException = exception;
//                logger.warn("setRetry data " + Arrays.asList(methods)+" | "+exception);
                String temp;
                 if(!disabled){
                     item= (String) backoff.get("firstBackoff");
//                     logger.warn("disabled false " +" | "+item);
                     if(item.contains("ms")){
                          temp=item.replace("ms","");
                         firstBackoff=Duration.ZERO.plusMillis(Long.parseLong(temp));
//                         logger.warn("disabled false " +" | "+firstBackoff.toMillis());
                     }
                     item= (String) backoff.get("maxBackoff");
                     if(item.contains("ms")){
                         maxBackoff=Duration.ZERO.plusMillis(Long.parseLong(item.replace("ms","")));
//                         logger.warn("disabled false " +" | "+maxBackoff.toMillis());
                     }
                     long litem  = (Long) backoff.get("factor");
                     factor= (int) litem;
                     basedOnPreviousValue= (boolean) backoff.get("basedOnPreviousValue");
                     Duration finalFirstBackoff = firstBackoff;
                     Duration finalMaxBackoff = maxBackoff;
                     int finalFactor = factor;
                     boolean finalBasedOnPreviousValue = basedOnPreviousValue;
                     gatewayFilterSpec.retry(c-> c.setRetries(Integer.parseInt(retries))
                             .setStatuses(statuses).setBackoff(finalFirstBackoff, finalMaxBackoff, finalFactor, finalBasedOnPreviousValue).setMethods(methods)
                             .setExceptions(finalException));
                 }else{
//                     logger.warn("setRetry data " +finalException);
                     gatewayFilterSpec.retry(c-> c.setRetries(Integer.parseInt(retries))
                             .setStatuses(statuses).setMethods(methods)
                             .setExceptions(finalException));
                 }



            }catch(Exception e){
//                logger.error(" setRetry "+e);
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setRequestSize(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterByValueExist(filter,"RequestSize",index)) {
//            logger.warn("setRequestSize "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            try {
                JSONObject args = (JSONObject) data.get("args");
//                logger.warn("setRequestSize data " + data + " | " + args.get("maxSize").toString());
                gatewayFilterSpec.setRequestSize(Long.parseLong(args.get("maxSize").toString()));
            }catch(Exception e){
                logger.error(" setRequestSize "+e);
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setSetRequestHostHeader(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterByValueExist(filter,"SetRequestHostHeader",index)) {
//            logger.warn("setSetRequestHostHeader "+itemIndex +" | "+filter.get(itemIndex) );
            JSONObject data= (JSONObject) filter.get(itemIndex);
            try {
                JSONObject args = (JSONObject) data.get("args");
//                logger.warn("setSetRequestHostHeader data " + data + " | " + args.get("host").toString());
//                gatewayFilterSpec.setSetRequestHostHeader(args.get("host").toString()); DEPRECATED
            }catch(Exception e){
                logger.error(" setSetRequestHostHeader "+e);
            }
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setModifyRequestBody(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"ModifyRequestBody",index)) {
//            logger.warn("setModifyRequestBody "+itemIndex +" | "+filter.get(itemIndex) );
//          STILL EXPLORE
        }
        return gatewayFilterSpec;
    }
    private GatewayFilterSpec setModifyResponseBody(JSONArray filter,GatewayFilterSpec gatewayFilterSpec,int index){
        if(isFilterExist(filter,"ModifyResponseBody",index)) {
//            logger.warn("setModifyResponseBody "+itemIndex +" | "+filter.get(itemIndex) );
//          STILL EXPLORE
        }
        return gatewayFilterSpec;
    }


    private BooleanSpec setPath(JSONArray predicates,PredicateSpec predicateSpec,int index){
        BooleanSpec  booleanSpec = null;
        if(isPredicateExist(predicates,"Path",index)){
//            logger.warn("setPath "+itemIndex +" | "+predicates.get(itemIndex) );
            JSONObject data= (JSONObject) predicates.get(itemIndex);
            String[] items = data.get("Path").toString().split(",");
//            logger.debug(" setPath "+itemIndex +" | "+ Arrays.asList(items));
            if(items.length>=1){
                booleanSpec = predicateSpec.path(items);
            }
        }
        return booleanSpec;
    }
    private BooleanSpec setMethod(JSONArray predicates,BooleanSpec booleanSpec,int index){
        String[] path = new String[0];
        if(isPredicateExist(predicates,"Method",index)){
//            logger.warn("setMethod "+itemIndex +" | "+predicates.get(itemIndex));
            JSONObject data= (JSONObject) predicates.get(itemIndex);
            String[] items = data.get("Method").toString().split(",");
//            logger.debug(" setMethod "+itemIndex +" | "+ Arrays.asList(items));
            if(items.length>=1){
                booleanSpec.and().method(items);
            }
        }
        return booleanSpec;
    }
    private BooleanSpec setHost(JSONArray predicates,BooleanSpec booleanSpec,int index){
        if(isPredicateExist(predicates,"Host",index)){
//            logger.warn("setHost "+itemIndex +" | "+predicates.get(itemIndex));
            JSONObject data= (JSONObject) predicates.get(itemIndex);
            String[] items = data.get("Host").toString().split(",");
//            logger.debug(" setHost "+itemIndex +" | "+ Arrays.asList(items));
            if(items.length>=1){
                booleanSpec.and().host(items);
            }
        }
        return booleanSpec;
    }
    private BooleanSpec setHeader(JSONArray predicates,BooleanSpec booleanSpec,int index){
        if(isPredicateExist(predicates,"Header",index)){
//            logger.warn("setHeader "+itemIndex +" | "+predicates.get(itemIndex));
            JSONObject data= (JSONObject) predicates.get(itemIndex);
            String[] items = data.get("Header").toString().split(",");
//            logger.debug(" setHeader "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            String regex="";
            if(items.length>=1){
                name=items[0];
                try {
                    regex=items[1];
                }catch(Exception e){
//                    logger.error(" setHeader "+e);
                }

            }
            booleanSpec.and().header(name,regex);
        }
        return booleanSpec;
    }
    private BooleanSpec setQuery(JSONArray predicates,BooleanSpec booleanSpec,int index){
        String[] path = new String[0];
        if(isPredicateExist(predicates,"Query",index)){
//            logger.warn("setQuery "+itemIndex +" | "+predicates.get(itemIndex));
            JSONObject data= (JSONObject) predicates.get(itemIndex);
            String[] items = data.get("Query").toString().split(",");
//            logger.debug(" setQuery "+itemIndex +" | "+ Arrays.asList(items));
            String name="";
            String regex="";
            if(items.length>=1){
                name=items[0];
                try {
                    regex=items[1];
                }catch(Exception e){
                    logger.error(" setQuery "+e);
                }

            }
            booleanSpec.and().cookie(name,regex);
        }
        return booleanSpec;
    }
    private BooleanSpec setCookie(JSONArray predicates,BooleanSpec booleanSpec,int index){
        if(isPredicateExist(predicates,"Cookie",index)){
//            logger.warn("setCookie "+itemIndex +" | "+predicates.get(itemIndex));
            JSONObject data= (JSONObject) predicates.get(itemIndex);
            String[] items = data.get("Cookie").toString().split(",");
            String name="";
            String regex="";
            if(items.length>=1){
//                    logger.debug(" setCookie "+ Arrays.asList(items));
                    name=items[0];
                try {
                    regex=items[1];
                }catch(Exception e){
                    logger.error(" setCookie "+e);
                }

            }
            booleanSpec.and().cookie(name,regex);
        }
        return booleanSpec;
    }
    private BooleanSpec setRemoteAddress(JSONArray predicates,BooleanSpec booleanSpec,int index){
        if(isPredicateExist(predicates,"RemoteAddr",index)){
//            logger.warn("setRemoteAddress "+itemIndex +" | "+predicates.get(itemIndex));
            JSONObject data= (JSONObject) predicates.get(itemIndex);
            String[] items = data.get("RemoteAddr").toString().split(",");
//            logger.debug(" setRemoteAddress "+itemIndex +" | "+ Arrays.asList(items));
            if(items.length>=1){
                try {
                    booleanSpec.and().remoteAddr(items);
                }catch(Exception e){
                    logger.error(" setRemoteAddress "+e);
                }
//
                }
            }
        return booleanSpec;
    }
    private BooleanSpec setAfter(JSONArray predicates,BooleanSpec booleanSpec,int index){
        if(isPredicateExist(predicates,"After",index)){
            JSONObject data= (JSONObject) predicates.get(itemIndex);
//            logger.warn("setAfter "+itemIndex +" | "+predicates.get(itemIndex));
            String[] items = data.get("After").toString().split(",");
//            logger.debug(" setAfter "+itemIndex +" | "+ Arrays.asList(items));
            LocalDateTime start = null;
            ZonedDateTime zonedDate = null;
            Boolean isValid=false;
            if(items.length>=1){
                try {
                    start =  LocalDateTime.parse(items[0], formatter);
                    zonedDate = start.atZone(ZoneId.of("UTC"));
                    zonedDate = zonedDate.withZoneSameInstant(ZoneId.systemDefault());
                    isValid=true;
                }catch(Exception e){
                    logger.error(" setAfter "+e);
                }
                if(isValid){
//                    logger.debug(" setAfter "+ zonedDate);
                    booleanSpec.and().after(zonedDate);
                }
            }
        }
        return booleanSpec;
    }
    private BooleanSpec setBefore(JSONArray predicates,BooleanSpec booleanSpec,int index){
        if(isPredicateExist(predicates,"Before",index)){
            JSONObject data= (JSONObject) predicates.get(itemIndex);
//            logger.warn("setBefore "+itemIndex +" | "+predicates.get(itemIndex));
            String[] items = data.get("Before").toString().split(",");
//            logger.warn(" setBefore "+itemIndex +" | "+ Arrays.asList(items));
            LocalDateTime start = null;
            ZonedDateTime zonedDate = null;
            Boolean isValid=false;
            if(items.length>=1){
                try {
                    start =  LocalDateTime.parse(items[0], formatter);
                    zonedDate = start.atZone(ZoneId.of("UTC"));
                    zonedDate = zonedDate.withZoneSameInstant(ZoneId.systemDefault());
                    isValid=true;
                }catch(Exception e){
                    logger.error(" setBefore "+e);
                }
                if(isValid){
//                    logger.debug(" setBefore "+ zonedDate);
                    booleanSpec.and().before(zonedDate);
                }
            }
        }
        return booleanSpec;
    }
    private BooleanSpec setBetween(JSONArray predicates,BooleanSpec booleanSpec,int index){
        if(isPredicateExist(predicates,"Between",index)){
            JSONObject data= (JSONObject) predicates.get(itemIndex);
//            logger.warn("setBetween "+itemIndex +" | "+predicates.get(itemIndex));
            String[] items = data.get("Between").toString().split(",");
//            logger.warn(" setBetween "+itemIndex +" | "+ Arrays.asList(items));
            LocalDateTime start = null;
            LocalDateTime end = null;
            ZonedDateTime zonedStart = null;
            ZonedDateTime zonedEnd = null;
            Boolean isValid=false;
            if(items.length>=2){
                try {
                    start =  LocalDateTime.parse(items[0], formatter);
                    end =  LocalDateTime.parse(items[1], formatter);
                    zonedStart = start.atZone(ZoneId.of("UTC"));
                    zonedStart = zonedStart.withZoneSameInstant(ZoneId.systemDefault());
                    zonedEnd = end.atZone(ZoneId.of("UTC"));
                    zonedEnd = zonedEnd.withZoneSameInstant(ZoneId.systemDefault());
                    isValid=true;
                }catch(Exception e){
                    logger.error(" setBetween "+e);
                }
                if(isValid){
//                    logger.debug(" setBetween "+ zonedStart+""+zonedEnd);
                    booleanSpec.and().between(zonedStart,zonedEnd);
                }
            }
        }
        return booleanSpec;
    }
    private BooleanSpec setWeight(JSONArray predicates,BooleanSpec booleanSpec,int index){
        if(isPredicateExist(predicates,"Weight",index)){
            JSONObject data= (JSONObject) predicates.get(itemIndex);
//            logger.warn("setWeight "+itemIndex +" | "+predicates.get(itemIndex));
            String[] items = data.get("Weight").toString().split(",");
//            logger.warn(" setWeight "+itemIndex +" | "+ Arrays.asList(items));
            int value = 0;
            Boolean isNumber=false;
            if(items.length>=2){
                try{
                    value = Integer.parseInt(items[1]);
                    isNumber=true;
                }catch(Exception e){
                }
                if(isNumber){
//                    logger.debug(" setWeight "+ items[0]+" | "+value);
                    booleanSpec.and().weight(items[0],value);
                }
            }
        }
        return booleanSpec;
    }
    private Boolean isFilterWithoutValueExist(JSONArray filters,String filterName,int index){
        Boolean isFound=false;
        if(filters!=null)
            for(int k=0;k<filters.size();k++){
                JSONObject filter= (JSONObject) filters.get(k);
                if(!StringUtils.isEmpty(String.valueOf(filter.get(filterName)))){
                    for (Object key : filter.keySet()) {
                        if(key.equals(filterName)) {
//                            logger.warn(filterName.toUpperCase()+" FOUND ");
                            itemIndex=k;
                            isFound=true;
                        }
                    }
                    if(isFound) return true;
                }
            }
        itemIndex=-1;
        return false;
    }

    private Boolean isFilterExist(JSONArray filters,String filterName,int index){
        if(filters!=null)
            for(int k=0;k<filters.size();k++){
                JSONObject filter= (JSONObject) filters.get(k);
                if(filter.get(filterName)!=null && !StringUtils.isEmpty(String.valueOf(filter.get(filterName)))){
//                    logger.warn(filterName.toUpperCase()+" FOUND ");
                    itemIndex=k;
                    return true;
                }
            }
        itemIndex=-1;
        return false;
    }
    private Boolean isFilterByValueExist(JSONArray filters,String filterName,int index){
        if(filters!=null)
            for(int k=0;k<filters.size();k++){
                JSONObject filter= (JSONObject) filters.get(k);
                if(filter.get("name")!=null && !StringUtils.isEmpty(String.valueOf(filter.get("name")))){
                    if(filter.get("name").toString().equals(filterName)) {
//                        logger.warn(filterName.toUpperCase() + " FOUND ");
                        itemIndex = k;
                        return true;
                    }
                }
            }
        itemIndex=-1;
        return false;
    }
    private Boolean isPredicateExist(JSONArray predicates,String predicateName,int index){
        if(predicates!=null)
            for(int k=0;k<predicates.size();k++){
                JSONObject predicate= (JSONObject) predicates.get(k);
                if(predicate.get(predicateName)!=null && !StringUtils.isEmpty(String.valueOf(predicate.get(predicateName)))){
//                    logger.warn(predicateName.toUpperCase()+" FOUND ");
                    itemIndex=k;
                    return true;
                }
            }
        itemIndex=-1;
        return false;
    }
}
