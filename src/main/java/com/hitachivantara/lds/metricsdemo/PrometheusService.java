package com.hitachivantara.lds.metricsdemo;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class PrometheusService {

    private final RestTemplate restTemplate;
    private final HttpHeaders detaultHeaders;
    private String url;

    public PrometheusService(String url) {
        this.url = url;
        restTemplate = new RestTemplate();
        detaultHeaders = new HttpHeaders();
        detaultHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    }

    public String executeQuery(String query){
        HttpEntity<String> entity = new HttpEntity<>("query=".concat(query), detaultHeaders);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }
}
