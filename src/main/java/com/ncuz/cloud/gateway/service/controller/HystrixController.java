package com.ncuz.cloud.gateway.service.controller;

import com.ncuz.log4j2.helper.service.Log4J2Service;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@RestController
@RequestMapping("/hystrix")
public class HystrixController {
    @Autowired
    Log4J2Service log4J2Service;
    private static Logger logger;
    @PostConstruct
    private void post() throws IOException {
        logger = log4J2Service.getLogger(HystrixController.class,false);
    }

    @PreDestroy
    private  void destroy(){

    }
    private static final String DEFAULT_MESSAGE = " Service is not available, Please contact an administrator for more support";
    @GetMapping("/{service}")
    public ResponseEntity<String> getFallback(
            @PathVariable(name = "service", required = true) String service){
//        logger.debug("serviceFallback :"+request.getContextPath()+" | "+request.getServerName());
        return ResponseEntity
                .status(HttpStatus.valueOf(500))
                .contentType(MediaType.APPLICATION_JSON)
                .body("Fallback Response : 500 Internal Server Error ["+service.toUpperCase()+"]" +DEFAULT_MESSAGE);
    }

    @PostMapping("/{service}")
    public ResponseEntity<String> postFallback(
            @PathVariable(name = "service", required = true) String service
    ){
        return ResponseEntity
                .status(HttpStatus.valueOf(500))
                .contentType(MediaType.APPLICATION_JSON)
                .body("Fallback Response : 500 Internal Server Error ["+service.toUpperCase()+"]" +DEFAULT_MESSAGE);
    }

    @PutMapping("/{service}")
    public ResponseEntity<String> putFallback(
            @PathVariable(name = "service", required = true) String service){
//        logger.debug("serviceFallback :"+request.getContextPath()+" | "+request.getServerName());
        return ResponseEntity
                .status(HttpStatus.valueOf(500))
                .contentType(MediaType.APPLICATION_JSON)
                .body("Fallback Response : 500 Internal Server Error ["+service.toUpperCase()+"]" +DEFAULT_MESSAGE);
    }
    @DeleteMapping("/{service}")
    public ResponseEntity<String> deleteFallback(
            @PathVariable(name = "service", required = true) String service){
//        logger.debug("serviceFallback :"+request.getContextPath()+" | "+request.getServerName());
        return ResponseEntity
                .status(HttpStatus.valueOf(500))
                .contentType(MediaType.APPLICATION_JSON)
                .body("Fallback Response : 500 Internal Server Error ["+service.toUpperCase()+"]" +DEFAULT_MESSAGE);
    }
    @PatchMapping("/{service}")
    public ResponseEntity<String> patchFallback(
            @PathVariable(name = "service", required = true) String service){
//        logger.debug("serviceFallback :"+request.getContextPath()+" | "+request.getServerName());
        return ResponseEntity
                .status(HttpStatus.valueOf(500))
                .contentType(MediaType.APPLICATION_JSON)
                .body("Fallback Response : 500 Internal Server Error ["+service.toUpperCase()+"]" +DEFAULT_MESSAGE);
    }
}
