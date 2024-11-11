package com.luminatehealth.fhir.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.luminatehealth.fhir.client.EpicFhirPatientClient;
import com.luminatehealth.fhir.convertors.FHIRMedicationRequestToDTOConverter;
import com.luminatehealth.fhir.convertors.FHIRObservationToDTOConverter;
import com.luminatehealth.fhir.convertors.FHIRPatientToDTOConverter;
import com.luminatehealth.fhir.dto.EpicOAuthTokenResponse;
import com.luminatehealth.fhir.dto.MedicationRequestDto;
import com.luminatehealth.fhir.dto.ObservationDto;
import com.luminatehealth.fhir.dto.PatientDto;
import com.luminatehealth.fhir.services.TokenService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

@Path("/epic")
public class EpicLoginController {

    private static final Logger log = LoggerFactory.getLogger(EpicLoginController.class);

    @Inject
    @Location("epic-patient-info.html")
    Template epicPatientInfo;

    @Inject
    TokenService tokenService;

    @Inject
    EpicFhirPatientClient epicFhirClient;

    private static final FHIRPatientToDTOConverter patientConverter = new FHIRPatientToDTOConverter();
    private static final FHIRMedicationRequestToDTOConverter medicationRequestConverter = new FHIRMedicationRequestToDTOConverter();
    private static final FHIRObservationToDTOConverter observationConverter = new FHIRObservationToDTOConverter();

    @Inject
    EpicFhirPatientClient epicFhirPatientClient;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response consumeCode(@QueryParam("code") String code) throws ParseException, JsonProcessingException {
        log.info("Received EPIC OAuth code: {}", code);

        // exchanging the code for a token
        EpicOAuthTokenResponse epicOAuthTokenResponse = tokenService.exchangeCodeForToken(code);
        epicFhirClient.registerAuthInterceptor(epicOAuthTokenResponse.accessToken());

        System.out.println(epicOAuthTokenResponse.accessToken());

        // redirect the user to patient-info endpoint
        return Response.seeOther(URI.create("epic/patient-info?patientId=" + epicOAuthTokenResponse.patient())).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/patient")
    public Response patientInfo(@Context UriInfo uriInfo) throws URISyntaxException {
        String redirectUrl = tokenService.generateRedirectUrl();
        log.info("redirectUrl generated: {}", redirectUrl);
        return Response.seeOther(URI.create(redirectUrl)).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/patient-info")
    public Response patientInfo(@QueryParam("patientId") String patientId) {
        Patient patient;
        List<MedicationRequestDto> patientMedications = null;
        List<ObservationDto> laboratoryObservations = null;
        List<ObservationDto> vitalSignsObservations = null;

        String vitalSignsDate = "2022-11-01";

        try {
            patient = epicFhirClient.read(patientId, Patient.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.seeOther(URI.create("epic/patient")).build();
        }

        try {
            patientMedications = epicFhirPatientClient.getMedicationRequestsByPatientId(patientId).getEntry()
                    .stream()
                    .filter(x -> x.getResource().getResourceType().name().equals("MedicationRequest"))
                    .map(x -> medicationRequestConverter.convert((MedicationRequest) x.getResource()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // lab results - laboratory
        try {
            laboratoryObservations = epicFhirPatientClient.getLabResultsByPatientId(patientId, "laboratory", null).getEntry()
                    .stream()
                    .filter(x -> x.getResource().getResourceType().name().equals("Observation"))
                    .map(x -> observationConverter.convert((Observation) x.getResource()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // lab results - vital signs
        try {
            // vitalSignsObservations = epicFhirPatientClient.getLabResultsByPatientId(patientId, "vital-signs", new DateParam(vitalSignsDate)).getEntry()
            vitalSignsObservations = epicFhirPatientClient.getLabResultsByPatientId(patientId, "vital-signs", null).getEntry()
                    .stream()
                    .filter(x -> x.getResource().getResourceType().name().equals("Observation"))
                    .map(x -> observationConverter.convert((Observation) x.getResource()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        PatientDto patientDto = patientConverter.convert(patient);
        TemplateInstance templateInstance = epicPatientInfo
                .instance()
                .data("patientMedications", patientMedications)
                .data("laboratoryObservations", laboratoryObservations)
                .data("vitalSignsObservations", vitalSignsObservations)
                .data("vitaSignsDate", vitalSignsDate)
                .data("patient", patientDto);

        return Response.ok(templateInstance).build();
    }
}
