package com.luminatehealth.fhir.client;

import ca.uhn.fhir.rest.gclient.DateClientParam;
import ca.uhn.fhir.rest.param.DateParam;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;

@ApplicationScoped
public class EpicFhirPatientClient extends EpicFhirClient<Patient> {

    public EpicFhirPatientClient(
            @ConfigProperty(name = "epic_fhir_base_url") String epicFhirBaseUrl,
            @ConfigProperty(name = "fhir.client.timeout") Integer timeout) {
        super(epicFhirBaseUrl, timeout);
    }

    public Bundle getMedicationRequestsByPatientId(String patientId) {
        return iGenericClient
                .search()
                .forResource(MedicationRequest.class)
                .where(MedicationRequest.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();
    }

    public Bundle getLabResultsByPatientId(String patientId, String category, DateParam date) {
        var search = iGenericClient
                .search()
                .forResource(Observation.class)
                .where(Observation.PATIENT.hasId(patientId))
                .where(Observation.CATEGORY.exactly().code(category));

        if (date != null) {
            search.where(new DateClientParam("date").afterOrEquals().day(date.getValue()));
        }

        return search.returnBundle(Bundle.class).execute();
    }

}
