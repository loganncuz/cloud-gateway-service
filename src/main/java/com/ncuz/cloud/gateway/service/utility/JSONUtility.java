package com.ncuz.cloud.gateway.service.utility;


import com.ncuz.log4j2.helper.service.Log4J2Service;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
public class JSONUtility {
    @Autowired
    Log4J2Service log4J2Service;
    @Autowired
    private Environment env;
    private static Logger logger;
    @PostConstruct
    private void post() throws IOException {
        logger=log4J2Service.getLogger(JSONUtility.class,false);
    }
    public JSONObject convertBodyToJSONObject(String data){
        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            json = (JSONObject) parser.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return json;
    }

    public JSONArray convertBodyToJSONArray(String data){
        JSONParser parser = new JSONParser();
        JSONArray json = null;
        try {
            json = (JSONArray) parser.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return json;
    }
}
