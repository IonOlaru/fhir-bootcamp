package com.medblocks;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientResourceProvider implements IResourceProvider {
    private final Connection connection = ConnectionManager.getInstance().getDbConnection();

    public PatientResourceProvider() throws SQLException, ClassNotFoundException {
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }

    private Patient convertResultSetToPatient(ResultSet result) throws SQLException {
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

    @Read()
    public Patient read(@IdParam IdType theId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM patients WHERE id = CAST(? AS UUID)");
        statement.setString(1, theId.getIdPart());
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            Patient patient = convertResultSetToPatient(result);
            return patient;
        } else {
            throw new ResourceNotFoundException(theId);
        }
    }

    @Search
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM patients;");
        while (result.next()) {
            Patient patient = convertResultSetToPatient(result);
            patients.add(patient);
        }
        return patients;
    }

}