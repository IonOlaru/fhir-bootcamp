package com.luminatehealth.fhir.client;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

@ApplicationScoped
public class FhirClientPatient extends LiteFhirClient<Patient> {

    public FhirClientPatient(@ConfigProperty(name = "fhir.server.base") String serverBase,
                             @ConfigProperty(name = "fhir.client.timeout") Integer timeout) {
        super(serverBase, timeout);
    }

    public Bundle searchPatients(String name, String phone) {
        return iGenericClient
                .search()
                .forResource(Patient.class)
                .where(Patient.NAME.matches().value(name))
                .and(Patient.TELECOM.exactly().identifier(phone))
                .returnBundle(Bundle.class)
                .execute();
    }

}
