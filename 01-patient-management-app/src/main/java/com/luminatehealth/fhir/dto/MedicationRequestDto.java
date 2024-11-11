package com.luminatehealth.fhir.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicationRequestDto {
    String reference;
    String instructions;
    String text;
}
