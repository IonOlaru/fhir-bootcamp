package com.luminatehealth.fhir.client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.DomainResource;

public class LiteFhirClientWithAuthToken<T extends DomainResource> {

    protected final IGenericClient iGenericClient;

    public LiteFhirClientWithAuthToken(String serverBase) {
        FhirContext ctx = FhirContext.forR4();
        iGenericClient = ctx.newRestfulGenericClient(serverBase);
    }

    public T read(String id, Class<T> klass, String authToken) {
        return iGenericClient.read().resource(klass).withId(id).withAdditionalHeader("Authorization", "Bearer " + authToken).execute();
    }
}
