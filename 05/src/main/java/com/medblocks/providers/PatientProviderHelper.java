package com.medblocks.providers;

import com.medblocks.utils.ConnectionManager;
import org.hl7.fhir.r4.model.Patient;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PatientProviderHelper {
    private final Connection connection = ConnectionManager.getInstance().getDbConnection();

    public PatientProviderHelper() throws SQLException, ClassNotFoundException {
    }

    Patient convertResultSetToPatient(ResultSet result) throws SQLException {
        String id = result.getString("id");
        String first_name = result.getString("first_name");
        String last_name = result.getString("last_name");
        Date dob = result.getDate("date_of_birth");

        Patient patient = new Patient();
        patient.setId(id);
        patient.addName().setFamily(last_name).addGiven(first_name).setText(first_name + " " + last_name);
        patient.setBirthDate(dob);

        return patient;
    }

}