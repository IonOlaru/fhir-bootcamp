package com.luminatehealth.fhir.convertors;

import com.luminatehealth.fhir.dto.PatientDto;
import com.luminatehealth.fhir.utils.TimeUtils;
import org.hl7.fhir.r4.model.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;

public class FHIRPatientToDTOConverter implements EntityConverter<Patient, PatientDto> {

    @Override
    public PatientDto convert(Patient patient) {
        return PatientDto
                .builder()
                .id(patient.getIdPart())
                .lastName(getLastName(patient))
                .firstName(getFirstName(patient))
                .phone(getPhone(patient))
                .dob(patient.getBirthDate() != null ? TimeUtils.toString(patient.getBirthDate()) : null)
                .gender(patient.getGender() != null ? patient.getGender().toString().substring(0, 1) : null)
                .build();
    }

    @Override
    public Patient revert(PatientDto patientDTO) {
        // dob
        LocalDate dobAsLocal = LocalDate.parse(patientDTO.getDob(), DateTimeFormatter.ISO_LOCAL_DATE);
        Date dob = Date.from(dobAsLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // contact
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
        contactPoint.setValue(patientDTO.getPhone());

        // gender
        Enumerations.AdministrativeGender gender = ("M".equalsIgnoreCase(patientDTO.getGender())) ?
                Enumerations.AdministrativeGender.MALE :
                Enumerations.AdministrativeGender.FEMALE;

        return new Patient()
                .setName(Collections.singletonList(new HumanName()
                        .setGiven(Collections.singletonList(new StringType(patientDTO.getFirstName())))
                        .setFamily(patientDTO.getLastName())
                ))
                .setBirthDate(dob)
                .addTelecom(contactPoint)
                .setGender(gender);
    }

    private String getPhone(Patient patient) {
        if (patient.getTelecom() != null && !patient.getTelecom().isEmpty()) {
            return patient.getTelecom().get(0).getValue();
        }
        return null;
    }


    private String getLastName(Patient patient) {
        if (patient.getNameFirstRep() != null) {
            return patient.getNameFirstRep().getFamily();
        }
        return null;
    }

    private String getFirstName(Patient patient) {
        if (patient.getNameFirstRep() != null && patient.getNameFirstRep().getGiven() != null && !patient.getNameFirstRep().getGiven().isEmpty()) {
            return patient.getNameFirstRep().getGiven().get(0).getValue();
        }
        return null;
    }
}
