package com.luminatehealth.fhir.convertors;

import com.luminatehealth.fhir.dto.MedicationRequestDto;
import org.hl7.fhir.r4.model.MedicationRequest;

public class FHIRMedicationRequestToDTOConverter implements EntityConverter<MedicationRequest, MedicationRequestDto> {

    @Override
    public MedicationRequestDto convert(MedicationRequest mr) {
        return MedicationRequestDto
                .builder()
                .reference(mr.getMedicationReference() != null ? mr.getMedicationReference().getDisplay() : "")
                .instructions(mr.getDosageInstruction().isEmpty() ? "" : mr.getDosageInstructionFirstRep().getPatientInstruction())
                .text(mr.getReasonCode().isEmpty() ? "" : mr.getReasonCode().get(0).getText())
                .build();
    }

    @Override
    public MedicationRequest revert(MedicationRequestDto medicationRequestDto) {
        throw new RuntimeException("Not implemented yet");
    }
}
