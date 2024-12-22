package com.medblocks;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class DbObservation {
    String observationId;
    String patientId;
    String observationType;
    String observationDate;
    Map<String, String> attributes;
}
