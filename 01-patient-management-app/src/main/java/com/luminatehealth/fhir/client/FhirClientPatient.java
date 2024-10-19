package com.luminatehealth.fhir.client;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.Patient;

@ApplicationScoped
public class FhirClientPatient extends LiteFhirClient<Patient> {

    public FhirClientPatient(@ConfigProperty(name = "fhir.server.base") String serverBase,
                             @ConfigProperty(name = "fhir.client.timeout") Integer timeout) {
        super(serverBase, timeout);
    }
}
