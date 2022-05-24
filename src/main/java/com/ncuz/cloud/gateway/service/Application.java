package com.ncuz.cloud.gateway.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ncuz.cloud.gateway.service.config.EurekaSSLConfig;
import com.ncuz.encryption.service.PropertiesService;
import org.json.simple.parser.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.annotation.PostConstruct;

@EnableDiscoveryClient
@EnableAutoConfiguration
@SpringBootApplication
@EnableHystrix
//@SpringBootApplication(exclude = GatewayRibbonConfig.class)

//@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = GatewayRibbonConfig.class))
//@RibbonClients(defaultConfiguration = GatewayRibbonConfig.class)
public class Application {

    public static void main(String[] args) throws ParseException {
        EurekaSSLConfig.disableSSLVerification();
        PropertiesService.initApplicationProperties(Application.class);
        SpringApplication.run(Application.class, args);

    }
    @PostConstruct
    private void post() {
//        PropertiesService.initApplicationProperties();
//        System.out.println("POST GATEWAY_SERVICE");
    }
    @Bean
    public HttpTraceRepository httpTraceRepository()
    {
        return new InMemoryHttpTraceRepository();
    }

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);
        // This line below is the magic one for me. Obviously, if need to switch properties to true, use featuresToEnable
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return builder;
    }


}
