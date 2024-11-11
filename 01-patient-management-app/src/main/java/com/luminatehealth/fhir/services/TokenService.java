package com.luminatehealth.fhir.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luminatehealth.fhir.client.TokenClient;
import com.luminatehealth.fhir.dto.EpicOAuthTokenResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.net.URISyntaxException;
import java.text.ParseException;

@ApplicationScoped
public class TokenService {

    @ConfigProperty(name = "epic_oauth_client_id")
    String EPIC_OAUTH_CLIENT_ID;

    @ConfigProperty(name = "epic_smart_auth_url")
    String EPIC_SMART_AUTH_URL;

    @ConfigProperty(name = "epic_fhir_base_url")
    String EPIC_FHIR_BASE_URL;

    @ConfigProperty(name = "epic_redirect_uri")
    String EPIC_REDIRECT_URI;

    @ConfigProperty(name = "epic_oauth_grant_type")
    String EPIC_OAUTH_GRANT_TYPE;


    @RestClient
    private TokenClient tokenServiceClient;

    public String getToken(String code) {
        return tokenServiceClient.requestToken(EPIC_OAUTH_GRANT_TYPE, code, EPIC_REDIRECT_URI, EPIC_OAUTH_CLIENT_ID);
    }

    public String generateRedirectUrl() throws URISyntaxException {
        return new URIBuilder(EPIC_SMART_AUTH_URL)
                .addParameter("client_id", EPIC_OAUTH_CLIENT_ID)
                .addParameter("scope", "openid fhirUser offline")
                .addParameter("redirect_uri", EPIC_REDIRECT_URI)
                .addParameter("response_type", "code")
                .addParameter("aud", EPIC_FHIR_BASE_URL)
                // .addParameter("code_challenge", codeChallenge)
                // .addParameter("code_challenge_method", "S256")
                .build()
                .toString();
    }

    public EpicOAuthTokenResponse exchangeCodeForToken(String oAuthCode) throws ParseException, JsonProcessingException {
        return getEpicOAuthResponse(getToken(oAuthCode));
    }

    public EpicOAuthTokenResponse getEpicOAuthResponse(String jsonString) throws ParseException, JsonProcessingException {
        return new ObjectMapper().readValue(jsonString, EpicOAuthTokenResponse.class);
    }

}
