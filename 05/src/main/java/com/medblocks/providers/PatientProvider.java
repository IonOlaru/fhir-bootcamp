package com.medblocks.providers;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.medblocks.utils.ConnectionManager;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientProvider implements IResourceProvider {
    private final Connection connection = ConnectionManager.getInstance().getDbConnection();
    private final PatientProviderHelper patientProviderHelper = new PatientProviderHelper();

    public PatientProvider() throws SQLException, ClassNotFoundException {
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }

    @Read()
    public Patient read(@IdParam IdType theId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM patients WHERE id = CAST(? AS UUID)");
        statement.setString(1, theId.getIdPart());
        ResultSet result = statement.executeQuery();
        if (result.next()) {
            Patient patient = patientProviderHelper.convertResultSetToPatient(result);
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
            Patient patient = patientProviderHelper.convertResultSetToPatient(result);
            patients.add(patient);
        }
        return patients;
    }

}