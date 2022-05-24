package com.ncuz.cloud.gateway.service.config;


import com.ncuz.encryption.service.PropertiesService;
import com.netflix.discovery.DiscoveryClient;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.simple.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

@Configuration
public class EurekaSSLConfig {

    public EurekaSSLConfig() throws FileNotFoundException {
    }

    @Bean
    public DiscoveryClient.DiscoveryClientOptionalArgs getTrustStoredEurekaClient(SSLContext sslContext) {
//        System.out.println("getTrustStoredEurekaClient {} "+ Arrays.asList(sslContext.getDefaultSSLParameters().getProtocols()));
        DiscoveryClient.DiscoveryClientOptionalArgs args = new DiscoveryClient.DiscoveryClientOptionalArgs();
        args.setSSLContext(sslContext);
        return args;
    }
    private File trustStore;
    private String trustStorePassword="password";
    @Bean
    public SSLContext sslContext() throws Exception {
        JSONObject server= (JSONObject) PropertiesService.getBootstrapConfig().get("server");
        JSONObject ssl= (JSONObject) server.get("ssl");
        String keyStore= (String) ssl.get("key-store");
        trustStore=new File(keyStore);
//        trustStore=ResourceUtils.getFile(
//                keyStore);
//        System.out.println("initialize ssl context bean with keystore {} "+trustStore);
        return new SSLContextBuilder()
                .loadTrustMaterial(
                        trustStore,
                        trustStorePassword.toCharArray()
                ).build();
    }
    public static void disableSSLVerification() {
//        System.out.println("disableSSLVerification");
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
//                System.out.println("checkClientTrusted :"+authType);
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
//                System.out.println("checkServerTrusted :"+authType);
            }

        } };

        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

        } catch (KeyManagementException e) {
//            System.out.println("KeyManagementException");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
//            System.out.println("NoSuchAlgorithmException");
            e.printStackTrace();
        }
//        System.out.println("getSocketFactory :"+sc.getSocketFactory()+" | "+sc.getProtocol());

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
//                System.out.println("EurekaSSLConfig allHostsValid :"+hostname+" | "+session);
                return true;
            }
        };
//        System.out.println("getSocketFactory 2 :"+sc.getSocketFactory()+" | "+allHostsValid);
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

}
