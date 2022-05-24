package com.ncuz.cloud.gateway.service.base;

import org.json.simple.JSONArray;
import org.reactivestreams.Publisher;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ApiRoute   {
    private String id;
    private String uri;
    private JSONArray predicates;
    private JSONArray filters;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public JSONArray getPredicates() {
        return predicates;
    }

    public void setPredicates(JSONArray predicates) {
        this.predicates = predicates;
    }

    public JSONArray getFilters() {
        return filters;
    }

    public void setFilters(JSONArray filters) {
        this.filters = filters;
    }



}
