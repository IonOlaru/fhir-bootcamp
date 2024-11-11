package com.luminatehealth.fhir.client;

import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import org.hl7.fhir.r4.model.DomainResource;

public class EpicFhirClient<T extends DomainResource> extends LiteFhirClient<T> {

    public EpicFhirClient() {}

    public EpicFhirClient(String baseUrl, Integer timeout) {
        super(baseUrl, timeout);
    }

    public void registerAuthInterceptor(String bearerToken) {
        iGenericClient.registerInterceptor(new BearerTokenAuthInterceptor(bearerToken));
    }
}
