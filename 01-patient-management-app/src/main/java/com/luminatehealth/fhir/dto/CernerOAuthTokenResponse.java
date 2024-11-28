package com.luminatehealth.fhir.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CernerOAuthTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") int expiresIn,
        @JsonProperty("scope") String scope,
        @JsonProperty("id_token") String idToken,
        @JsonProperty("patient") String patient,
        @JsonProperty("need_patient_banner") Boolean needPatientBanner,
        @JsonProperty("smart_style_url") String smartStyleUrl
) {}