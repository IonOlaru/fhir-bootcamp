package com.luminatehealth.fhir.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.luminatehealth.fhir.client.LiteFhirClientWithAuthToken;
import com.luminatehealth.fhir.convertors.FHIRPatientToDTOConverter;
import com.luminatehealth.fhir.dto.CernerOAuthTokenResponse;
import com.luminatehealth.fhir.dto.PatientDto;
import com.luminatehealth.fhir.services.FhirServerConfigService;
import com.luminatehealth.fhir.services.SessionService;
import com.luminatehealth.fhir.services.TokenService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Path("/cerner")
public class CernerController {

    private static final Logger log = LoggerFactory.getLogger(CernerController.class);

    @ConfigProperty(name = "cerner_scope")
    String CERNER_SCOPE;

    @ConfigProperty(name = "cerner_redirect_uri")
    String CERNER_REDIRECT_URI;

    @ConfigProperty(name = "cerner_oauth_client_id")
    String CERNER_OAUTH_CLIENT_ID;

    @Inject
    @Location("cerner-patient.html")
    Template cernerTemplate;

    @Inject
    FhirServerConfigService fhirServerConfigService;

    @Inject
    TokenService tokenService;

    @Inject
    SessionService sessionService;

    @Inject
    @Location("cerner-patient-info.html")
    Template cernerPatientInfo;

    private static final FHIRPatientToDTOConverter patientConverter = new FHIRPatientToDTOConverter();

    @GET
    @Path("/patient-info")
    @Produces(MediaType.TEXT_HTML)
    public Response cerner(@QueryParam("patientId") String patientId) {
        TemplateInstance templateInstance = cernerPatientInfo.instance();

        String fhirServerUrl = sessionService.getCernerFhirServerUrl();
        String accessToken = sessionService.getCernerAccessToken();

        // ToDo: no need to create a new object all the time, just get it from a cache by URL
        LiteFhirClientWithAuthToken<Patient> liteFhirClientWithAuthToken = new LiteFhirClientWithAuthToken(fhirServerUrl);
        Patient patient = liteFhirClientWithAuthToken.read(patientId, Patient.class, accessToken);

        PatientDto patientDto = patientConverter.convert(patient);
        return Response.ok(templateInstance.data("patient", patientDto)).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response cerner(@QueryParam("iss") String iss, @QueryParam("launch") String launch, @QueryParam("code") String code) throws IOException, URISyntaxException {

        if (StringUtils.isNoneBlank(iss, launch)) {
            JsonNode serverInfo = fhirServerConfigService.fetchFhirServerConfig(iss);
            String authEndpoint = serverInfo.get("authorization_endpoint").textValue();
            String tokenEndpoint = serverInfo.get("token_endpoint").textValue();

            sessionService.setCernerTokenEndpoint(tokenEndpoint);
            sessionService.setCernerFhirServerUrl(iss);

            String redirectUrl = fhirServerConfigService.generateRedirectUrl(iss, authEndpoint, CERNER_OAUTH_CLIENT_ID, launch, CERNER_REDIRECT_URI, CERNER_SCOPE);
            log.info("redirectUrl generated: {}", redirectUrl);

            return Response.seeOther(URI.create(redirectUrl)).build();
        }

        if (StringUtils.isNotBlank(code)) {
            log.info("Received Cerner OAuth code: {}", code);
            CernerOAuthTokenResponse cernerOAuthTokenResponse = null;

            try {
                cernerOAuthTokenResponse = tokenService.exchangeCernerCodeForToken(sessionService.getCernerTokenEndpoint(), code);
                sessionService.setCernerAccessToken(cernerOAuthTokenResponse.accessToken());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            return Response.seeOther(URI.create("cerner/patient-info?patientId=" + cernerOAuthTokenResponse.patient())).build();
        }

        TemplateInstance templateInstance = cernerTemplate.instance();
        return Response.ok(templateInstance).build();
    }

}
