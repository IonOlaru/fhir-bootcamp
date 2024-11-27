package com.luminatehealth.fhir.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

@ApplicationScoped
public class SessionService {

    public static final String SESSION_CERNER_FHIR_SERVER_URL = "SESSION_CERNER_FHIR_SERVER_URL";
    public static final String SESSION_CERNER_TOKEN_ENDPOINT = "SESSION_CERNER_TOKEN_ENDPOINT";
    public static final String SESSION_CERNER_ACCESS_TOKEN = "SESSION_CERNER_ACCESS_TOKEN";

    @Inject
    HttpServletRequest request;

    // CernerFhirServerUrl
    public String getCernerFhirServerUrl() {
        return request.getSession().getAttribute(SESSION_CERNER_FHIR_SERVER_URL).toString();
    }

    public void setCernerFhirServerUrl(String cernerFhirServerUrl) {
        request.getSession().setAttribute(SESSION_CERNER_FHIR_SERVER_URL, cernerFhirServerUrl);
    }

    // Cerner Access Token
    public void setCernerAccessToken(String cernerAccessToken) {
        request.getSession().setAttribute(SESSION_CERNER_ACCESS_TOKEN, cernerAccessToken);
    }

    public String getCernerAccessToken() {
        return request.getSession().getAttribute(SESSION_CERNER_ACCESS_TOKEN).toString();
    }

    // Cerner Token Endpoint
    public void setCernerTokenEndpoint(String cernerTokenEndpoint) {
        request.getSession().setAttribute(SESSION_CERNER_TOKEN_ENDPOINT, cernerTokenEndpoint);
    }

    public String getCernerTokenEndpoint() {
        return request.getSession().getAttribute(SESSION_CERNER_TOKEN_ENDPOINT).toString();
    }

}
