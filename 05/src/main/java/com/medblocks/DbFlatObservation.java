package com.medblocks;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DbFlatObservation {
    String observationId;
    String patientId;
    String observationType;
    String observationDate;
    String observationAttrName;
    String observationAttrValue;
}
