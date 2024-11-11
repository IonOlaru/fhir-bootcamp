package com.luminatehealth.fhir.convertors;

import com.luminatehealth.fhir.dto.ObservationDto;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;

public class FHIRObservationToDTOConverter implements EntityConverter<Observation, ObservationDto> {

    @Override
    public ObservationDto convert(Observation o) {
        return ObservationDto
                .builder()
                .text(o.getCode().getText())
                .effectiveDateTime(o.getEffective().primitiveValue())
                .value(getObservationValue(o))
                .unit(o.getValueQuantity().getUnit())
                .build();
    }

    @Override
    public Observation revert(ObservationDto observationDto) {
        throw new RuntimeException("Not implemented yet");
    }

    static Observation.ObservationComponentComponent findComponentByCode(Observation observation, String code) {
        return observation.getComponent().stream()
                .filter(component -> component.getCode().getCoding().stream().anyMatch(coding -> code.equals(coding.getCode())))
                .findFirst()
                .orElse(null);
    }

    static String getObservationValue(Observation o) {
        if (o.getValue() != null && o.getValue().getClass().equals(Quantity.class)) {
            return o.getValueQuantity().getValue().toString();
        }

        // Blood pressure
        if (o.getCode().getCoding().stream().anyMatch(x -> x.getCode().equals("55284-4"))) {
            Observation.ObservationComponentComponent systolic = findComponentByCode(o, "8480-6");
            Observation.ObservationComponentComponent diastolic = findComponentByCode(o, "8462-4");
            return systolic.getValueQuantity().getValue().toString()  + "/" + diastolic.getValueQuantity().getValue().toString();
        }

        return "";
    }
}
