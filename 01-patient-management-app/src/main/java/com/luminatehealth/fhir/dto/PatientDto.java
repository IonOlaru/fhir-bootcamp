package com.luminatehealth.fhir.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientDto {
    String id;
    String gender;
    String firstName;
    String lastName;
    String phone;
    String dob;
}
