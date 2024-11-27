package com.luminatehealth.fhir.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luminatehealth.fhir.client.TokenClient;
import com.luminatehealth.fhir.dto.CernerOAuthTokenResponse;
import com.luminatehealth.fhir.dto.EpicOAuthTokenResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

@ApplicationScoped
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    // EPIC
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

    // CERNER
    @ConfigProperty(name = "cerner_oauth_grant_type")
    String CERNER_OAUTH_GRANT_TYPE;

    @ConfigProperty(name = "cerner_redirect_uri")
    String CERNER_REDIRECT_URI;

    @ConfigProperty(name = "cerner_oauth_client_id")
    String CERNER_OAUTH_CLIENT_ID;

    @RestClient
    private TokenClient tokenServiceClient;

    @Inject
    FhirServerConfigService fhirServerConfigService;

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

    // epic
    public EpicOAuthTokenResponse exchangeCodeForToken(String code) throws JsonProcessingException {
        String token = tokenServiceClient.requestEpicToken(EPIC_OAUTH_GRANT_TYPE, code, EPIC_REDIRECT_URI, EPIC_OAUTH_CLIENT_ID, "");
        return new ObjectMapper().readValue(token, EpicOAuthTokenResponse.class);
    }

    // cerner
    public CernerOAuthTokenResponse exchangeCernerCodeForToken(String tokenEndpoint, String code) throws IOException {
        String token = fhirServerConfigService.exchangeCodeForToken(tokenEndpoint, CERNER_OAUTH_GRANT_TYPE, code, CERNER_REDIRECT_URI, CERNER_OAUTH_CLIENT_ID, "");
        return new ObjectMapper().readValue(token, CernerOAuthTokenResponse.class);
    }

}
