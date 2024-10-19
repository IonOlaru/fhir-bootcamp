package com.luminatehealth.fhir.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luminatehealth.fhir.client.FhirClientPatient;
import com.luminatehealth.fhir.convertors.FHIRPatientToDTOConverter;
import com.luminatehealth.fhir.dto.PatientDto;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Path("/")
public class PatientController {

    private static final Logger log = LoggerFactory.getLogger(PatientController.class);
    private static final FHIRPatientToDTOConverter converter = new FHIRPatientToDTOConverter();

    @Inject
    @Location("patient-form.html")
    Template patientEdit;

    @Inject
    @Location("patient-list.html")
    Template patientListTemplate;

    @Inject
    FhirClientPatient fhirClientPatient;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance patientsList() {
        Bundle bundle = fhirClientPatient.getAll(20, 0);

        List<PatientDto> patients = bundle.getEntry()
                .stream()
                .map(x -> converter.convert((Patient) x.getResource()))
                .collect(Collectors.toList());

        return patientListTemplate.instance().data("patients", patients);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/edit/{id}")
    public TemplateInstance patientEditForm(@PathParam("id") String id) {
        log.info("Loading patient info by id: {}", id);

        Patient patient = fhirClientPatient.read(Patient.class, id);
        PatientDto onePatientDto = converter.convert(patient);

        log.info("{}", onePatientDto);

        return patientEdit.instance().data("onePatient", onePatientDto).data("isEdit", true);
    }

    @GET
    @Path("/create")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance patientCreateForm() {
        log.info("Create patient form...");
        PatientDto onePatientDto = PatientDto.builder().build();
        return patientEdit.instance().data("onePatient", onePatientDto).data("isEdit", false);
    }

    @POST
    @Path("/create")
    public Response patientCreate(@FormParam("inputPatientFirstName") String firstName,
                                  @FormParam("inputPatientLastName") String lastName,
                                  @FormParam("inputPatientDob") String dob,
                                  @FormParam("inputPatientPhone") String phone,
                                  @FormParam("inputPatientGender") String gender,
                                  @Context UriInfo uriInfo) {
        PatientDto onePatientDto = PatientDto
                .builder()
                .firstName(firstName)
                .lastName(lastName)
                .dob(dob)
                .phone(phone)
                .gender(gender)
                .build();

        log.info("Creating patient {}", onePatientDto);

        Patient patient = converter.revert(onePatientDto);
        fhirClientPatient.create(patient);

        return Response.seeOther(uriInfo.getBaseUriBuilder().path("/").build()).build();

    }

    @POST
    @Path("/update")
    public Response patientUpdate(@FormParam("inputPatientId") String id,
                                  @FormParam("inputPatientFirstName") String firstName,
                                  @FormParam("inputPatientLastName") String lastName,
                                  @FormParam("inputPatientDob") String dob,
                                  @FormParam("inputPatientPhone") String phone,
                                  @FormParam("inputPatientGender") String gender,
                                  @Context UriInfo uriInfo) {
        PatientDto onePatientDto = PatientDto
                .builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .dob(dob)
                .phone(phone)
                .gender(gender)
                .build();

        log.info("Saving patient {}", onePatientDto);

        Patient patient = converter.revert(onePatientDto);
        Patient updatedPatient = fhirClientPatient.update(id, patient);

        return Response.seeOther(uriInfo.getBaseUriBuilder().path("/").build()).build();

    }

}
