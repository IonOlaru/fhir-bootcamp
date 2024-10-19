package com.luminatehealth.fhir.client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;

import java.util.HashMap;
import java.util.Map;

public abstract class LiteFhirClient<T extends DomainResource> {

    public static final String DEFAULT_FHIR_SERVER = "https://hapi.fhir.org/baseR4";
    public static final int FHIR_CLIENT_TIMEOUT = 5000;

    private final IGenericClient iGenericClient;

    public static final Map<Class, String> RESOURCE_TYPE_MAP = new HashMap<>();

    static {
        RESOURCE_TYPE_MAP.put(Patient.class, "Patient");
    }

    public LiteFhirClient() {
        this(DEFAULT_FHIR_SERVER, FHIR_CLIENT_TIMEOUT);
    }

    LiteFhirClient(String serverBase, Integer timeout) {
        FhirContext ctx = FhirContext.forR4();
        ctx.getRestfulClientFactory().setSocketTimeout(timeout);
        iGenericClient = ctx.newRestfulGenericClient(serverBase);
    }

    public T read(Class<T> klass, String id) {
        return iGenericClient.read().resource(klass).withId(id).execute();
    }

    public IIdType create(T t) {
        MethodOutcome outcome = iGenericClient.create()
                .resource(t)
                .prettyPrint()
                .encodedJson()
                .execute();
        return outcome.getId();
    }

    public T update(String id, T t) {
        IIdType idType = new IdType(RESOURCE_TYPE_MAP.get(t.getClass()), id);
        t.setId(idType.toString());
        iGenericClient.update().resource(t).execute();
        return t;
    }

    public Bundle getAll(int queryLimit, int offset) {
        SortSpec sortSpec = new SortSpec("_lastUpdated", SortOrderEnum.DESC);
        return iGenericClient.search()
                .forResource(Patient.class)
                .count(queryLimit)
                .offset(offset)
                .sort(sortSpec)
                .returnBundle(Bundle.class)
                .execute();
    }

}
