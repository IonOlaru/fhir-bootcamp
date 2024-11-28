package com.luminatehealth.fhir.client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.DomainResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class LiteFhirClientWithAuthToken<T extends DomainResource> {

    private static final Logger log = LoggerFactory.getLogger(LiteFhirClientWithAuthToken.class);

    protected final IGenericClient iGenericClient;

    public static Map<String, LiteFhirClientWithAuthToken> fhirServers = new HashMap<>();

    public static <T extends DomainResource> LiteFhirClientWithAuthToken<T> getFhirServer(String serverUrl) {
        fhirServers.computeIfAbsent(serverUrl, LiteFhirClientWithAuthToken::new);
        return fhirServers.get(serverUrl);
    }

    private LiteFhirClientWithAuthToken(String serverBase) {
        log.info("Constructing LiteFhirClientWithAuthToken...");
        FhirContext ctx = FhirContext.forR4();
        iGenericClient = ctx.newRestfulGenericClient(serverBase);
    }

    public T read(String id, Class<T> klass, String authToken) {
        return iGenericClient
                .read()
                .resource(klass)
                .withId(id)
                .withAdditionalHeader("Authorization", "Bearer " + authToken)
                .execute();
    }
}
