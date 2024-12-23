package com.medblocks.providers;

import com.medblocks.utils.ConnectionManager;
import com.medblocks.model.DbFlatObservation;
import com.medblocks.model.DbObservation;
import com.medblocks.utils.MigrationService;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Observation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ObservationProviderHelper {
    private final Connection connection = ConnectionManager.getInstance().getDbConnection();

    public ObservationProviderHelper() throws SQLException, ClassNotFoundException {
    }

    private Observation convertBloodPressureToObservation(DbObservation dbObservation) {
        Observation observation = new Observation();
        observation.setId(dbObservation.getObservationId());
        observation.setStatus(Observation.ObservationStatus.FINAL);
        observation.getCode().setText("Blood pressure panel with all children optional");
        observation.getCode().addCoding().setCode("85354-9").setDisplay("Blood pressure panel with all children optional");

        Observation.ObservationComponentComponent systolicComponent = new Observation.ObservationComponentComponent()
                .setCode(new org.hl7.fhir.r4.model.CodeableConcept()
                        .addCoding(new org.hl7.fhir.r4.model.Coding()
                                .setCode("8480-6")
                                .setDisplay("Systolic blood pressure")
                                .setSystem("http://loinc.org")))
                .setValue(new org.hl7.fhir.r4.model.Quantity()
                        .setValue(Integer.parseInt(dbObservation.getAttributes().get(MigrationService.OBSERVATION_BLOOD_PRESSURE_ATTR_SYSTOLIC)))
                        .setUnit("mmHg"));

        Observation.ObservationComponentComponent diastolicComponent = new Observation.ObservationComponentComponent()
                .setCode(new org.hl7.fhir.r4.model.CodeableConcept()
                        .addCoding(new org.hl7.fhir.r4.model.Coding()
                                .setCode("8462-4")
                                .setDisplay("Diastolic blood pressure")
                                .setSystem("http://loinc.org")))
                .setValue(new org.hl7.fhir.r4.model.Quantity()
                        .setValue(Integer.parseInt(dbObservation.getAttributes().get(MigrationService.OBSERVATION_BLOOD_PRESSURE_ATTR_DIASTOLIC)))
                        .setUnit("mmHg"));

        observation.addComponent(systolicComponent);
        observation.addComponent(diastolicComponent);

        observation.getSubject().setReference("Patient/" + dbObservation.getPatientId());

        return observation;
    }

    private Observation convertHeartRateToObservation(DbObservation dbObservation) {
        Observation observation = new Observation();
        observation.setId(dbObservation.getObservationId());
        observation.setStatus(Observation.ObservationStatus.FINAL);

        observation.getCode()
                .setText("Heart Rate")
                .addCoding()
                .setCode("8867-4")
                .setDisplay("Heart rate")
                .setSystem("http://loinc.org");

        observation.setValue(
                new org.hl7.fhir.r4.model.Quantity()
                        .setValue(Integer.parseInt(dbObservation.getAttributes().get(MigrationService.OBSERVATION_HEART_RATE_ATTR_RATE)))
                        .setUnit("/min"));
        observation.getSubject().setReference("Patient/" + dbObservation.getPatientId());
        return observation;
    }

    private Observation convertToFHIRObservation(DbObservation dbObservation) {
        if (dbObservation.getObservationType().equals(MigrationService.OBSERVATION_TYPE_HEART_RATE)) {
            return convertHeartRateToObservation(dbObservation);
        }
        if (dbObservation.getObservationType().equals(MigrationService.OBSERVATION_TYPE_BLOOD_PRESSURE)) {
            return convertBloodPressureToObservation(dbObservation);
        }
        return null;
    }

    List<Observation> getAllObservationsFromDb(String observationId) throws SQLException {
        String selectObservationsSql = "SELECT " +
                "   o.id AS observation_id, " +
                "   o.patient_id, " +
                "   o.observation_type, " +
                "   o.observation_date, " +
                "   oa.attr_name, " +
                "   oa.attr_value " +
                "FROM observations o " +
                "JOIN observation_attributes oa ON o.id = oa.observation_id " +
                (StringUtils.isNotBlank(observationId) ? "WHERE o.id = CAST(? AS UUID) " : "") +
                "ORDER BY observation_id, observation_date DESC";

        PreparedStatement statement = connection.prepareStatement(selectObservationsSql);
        if (StringUtils.isNotBlank(observationId)) {
            statement.setString(1, observationId);
        }

        ResultSet allObservationsAndTHeirAttributes = statement.executeQuery();

        List<Observation> observations = new ArrayList<Observation>();
        List<DbFlatObservation> dbObservations = new ArrayList<DbFlatObservation>();

        while (allObservationsAndTHeirAttributes.next()) {
            DbFlatObservation dbObservation = DbFlatObservation
                    .builder()
                    .observationId(allObservationsAndTHeirAttributes.getString("observation_id"))
                    .patientId(allObservationsAndTHeirAttributes.getString("patient_id"))
                    .observationType(allObservationsAndTHeirAttributes.getString("observation_type"))
                    .observationDate(allObservationsAndTHeirAttributes.getString("observation_date"))
                    .observationAttrName(allObservationsAndTHeirAttributes.getString("attr_name"))
                    .observationAttrValue(allObservationsAndTHeirAttributes.getString("attr_value"))
                    .build();
            dbObservations.add(dbObservation);
        }

        List<DbObservation> groupedObservations = dbObservations.stream()
                .collect(Collectors.groupingBy(
                        DbFlatObservation::getObservationId,
                        Collectors.collectingAndThen(Collectors.toList(), dbFlatOservationsList -> {
                                    // get the 1st DbFlatObservation in the list to read the common attributes
                                    DbFlatObservation firstDbFlatObservation = dbFlatOservationsList.get(0);
                                    // get the attribues from the whole list
                                    Map<String, String> attributes = dbFlatOservationsList.stream()
                                            .collect(Collectors.toMap(DbFlatObservation::getObservationAttrName, DbFlatObservation::getObservationAttrValue));
                                    // construct the final DbObservation
                                    return new DbObservation(
                                            firstDbFlatObservation.getObservationId(),
                                            firstDbFlatObservation.getPatientId(),
                                            firstDbFlatObservation.getObservationType(),
                                            firstDbFlatObservation.getObservationDate(),
                                            attributes
                                    );
                                }
                        )
                ))
                .values()
                .stream()
                .collect(Collectors.toList());

        return groupedObservations.stream().map(x -> convertToFHIRObservation(x)).collect(Collectors.toList());
    }

}

