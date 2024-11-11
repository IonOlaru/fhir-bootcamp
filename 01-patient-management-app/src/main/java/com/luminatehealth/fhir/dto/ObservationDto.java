package com.luminatehealth.fhir.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ObservationDto {
    String text;
    String effectiveDateTime;
    String value;
    String unit;
}
