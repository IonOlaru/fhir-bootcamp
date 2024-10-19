package com.luminatehealth.fhir.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PatientDto {
    String id;
    String gender;
    String firstName;
    List<String> otherFirstNames;
    String lastName;
    String phone;
    String dob;

    public String otherFirstNamesAsString() {
        return String.join(", ", otherFirstNames);
    }
}
