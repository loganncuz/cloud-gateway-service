package com.ncuz.cloud.gateway.service.component;

import com.ncuz.log4j2.helper.service.Log4J2Service;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

@Component
public class CustomWebSocketHandler implements WebSocketHandler {
    @Autowired
    Log4J2Service log4J2Service;
    private static Logger logger;
    @PostConstruct
    private void post() throws IOException {
        logger = log4J2Service.getLogger(CustomWebSocketHandler.class,false);
    }

    @PreDestroy
    private  void destroy(){

    }
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session
                .receive()
                .doOnNext(msg -> logger.warn("[{}]: {}", session.getId(), msg))
                .log()
                .doFinally(sig -> logger.warn("Web socket session finished: " +session.getId() +" | "+sig.name()))
                .then();
    }

}
