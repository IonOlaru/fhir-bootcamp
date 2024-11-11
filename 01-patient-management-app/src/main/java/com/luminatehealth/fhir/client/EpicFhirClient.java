package com.luminatehealth.fhir.client;

import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import org.hl7.fhir.r4.model.DomainResource;

public class EpicFhirClient<T extends DomainResource> extends LiteFhirClient<T> {

    public EpicFhirClient() {
    }

    public EpicFhirClient(String baseUrl, Integer timeout) {
        super(baseUrl, timeout);
    }

    /**
     * THIS CODE DOES NOT SUPPORT BEING USED BY MULTIPLE BROWSER SESSIONS AT THE SAME TIME
     * CHANGES ARE NEEDED TO ALLOW DIFFERENT SESSIONS TO USE THEIR OWN `bearerToken`
     */
    public void registerAuthInterceptor(String bearerToken) {
        iGenericClient.getInterceptorService().getAllRegisteredInterceptors().stream()
                .filter(interceptor -> interceptor instanceof BearerTokenAuthInterceptor)
                .findFirst()
                .ifPresent(iGenericClient.getInterceptorService()::unregisterInterceptor);

        iGenericClient.registerInterceptor(new BearerTokenAuthInterceptor(bearerToken));
    }
}
