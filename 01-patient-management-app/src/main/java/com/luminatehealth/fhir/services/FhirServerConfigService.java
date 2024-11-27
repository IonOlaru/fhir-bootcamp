package com.luminatehealth.fhir.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.StringJoiner;

@ApplicationScoped
public class FhirServerConfigService {

    CloseableHttpClient closeableHttpClient;

    @Inject
    ObjectMapper objectMapper;

    private static final String FHIR_SERVER_WELL_KNOWN_URL = "/.well-known/smart-configuration";

    @PostConstruct
    public void init() {
        closeableHttpClient = HttpClients.createDefault();
    }

    public JsonNode fetchFhirServerConfig(String fhirServer) throws IOException {
        HttpGet request = new HttpGet(fhirServer + FHIR_SERVER_WELL_KNOWN_URL);
        try (CloseableHttpResponse response = closeableHttpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                return objectMapper.readTree(responseBody);
            } else {
                throw new RuntimeException("Failed to fetch FHIR server config: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }
        }
    }

    public String exchangeCodeForToken(String tokenEndpoint, String grantType, String code, String redirectUri, String clientId, String clientSecret) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost postRequest = new HttpPost(tokenEndpoint);
            String formParams = buildFormParams(Map.of(
                    "grant_type", grantType,
                    "code", code,
                    "redirect_uri", redirectUri,
                    "client_id", clientId,
                    "client_secret", clientSecret
            ));
            postRequest.setEntity(new StringEntity(formParams, ContentType.APPLICATION_FORM_URLENCODED));

            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                return new String(response.getEntity().getContent().readAllBytes());
            }
        }
    }

    private String buildFormParams(Map<String, String> params) {
        StringJoiner joiner = new StringJoiner("&");
        params.forEach((key, value) -> joiner.add(key + "=" + value));
        return joiner.toString();
    }

    public String generateRedirectUrl(String fhirServerUrl, String authUrl, String clientId, String launchCode, String redirectUri, String scope) throws URISyntaxException {
        return new URIBuilder(authUrl)
                .addParameter("client_id", clientId)
                .addParameter("scope", scope)
                .addParameter("redirect_uri", redirectUri)
                .addParameter("response_type", "code")
                .addParameter("aud", fhirServerUrl)
                .addParameter("launch", launchCode)
                .build()
                .toString();
    }

}