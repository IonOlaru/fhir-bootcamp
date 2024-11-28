package com.luminatehealth.fhir.client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
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

    public Bundle getLabResultsByPatientId(String patientId, String category, String authToken) {
        var search = iGenericClient
                .search()
                .forResource(Observation.class)
                .withAdditionalHeader("Authorization", "Bearer " + authToken)
                .where(Observation.PATIENT.hasId(patientId))
                .where(Observation.CATEGORY.exactly().code(category))
                .count(20)
                .sort().descending(Observation.DATE);

        return search.returnBundle(Bundle.class).execute();
    }

    public void addBodyTemperatureObservationInCelsius(String patientId, double temperatureValue, String authToken) {
        // construct the Observation
        Observation observation = new Observation()
                .setSubject(new Reference("Patient/" + patientId))
                .setStatus(Observation.ObservationStatus.FINAL)
                .setCode(new CodeableConcept().addCoding(
                        new Coding()
                                .setSystem("http://loinc.org")
                                .setCode("8331-1")
                                .setDisplay("Temperature Oral")
                ))
                .setValue(new Quantity()
                        .setValue(temperatureValue)
                        .setUnit("degC")
                        .setSystem("http://unitsofmeasure.org")
                        .setCode("Cel"))
                .setEffective(new DateTimeType(new Date()));

        // add category
        observation.getCategory().add(new CodeableConcept().addCoding(
                new Coding()
                        .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
                        .setCode("vital-signs")
                        .setDisplay("Vital Signs")
        ));

        // send
        iGenericClient
                .create()
                .resource(observation)
                .withAdditionalHeader("Authorization", "Bearer " + authToken)
                .execute();
    }

}
