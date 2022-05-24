package com.ncuz.cloud.gateway.service.controller;

import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

//@Controller
public class AppController {
//    @RequestMapping(value = { "/", "/api/**", "/fallback/**"})
    @RequestMapping(value = { "/", "/{x:[\\w\\-]+}", "/{x:^(?!api$).*$}/**/{y:[\\w\\-]+}" })
    public String getIndex(HttpServletRequest request) {
        System.out.println("xxxx :"+request.getRequestURI());
        return "/index.html";
    }
}
