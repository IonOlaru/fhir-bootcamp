package com.medblocks.providers;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.medblocks.utils.ConnectionManager;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ObservationProvider implements IResourceProvider {
    private final Connection connection = ConnectionManager.getInstance().getDbConnection();
    private final ObservationProviderHelper observationProviderHelper = new ObservationProviderHelper();

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Observation.class;
    }

    public ObservationProvider() throws SQLException, ClassNotFoundException {
    }

    @Search
    public List<Observation> getAllObservations() throws SQLException {
        return observationProviderHelper.getAllObservationsFromDb(null);
    }

    @Read
    public Observation getObservation(@IdParam IdType id) throws SQLException {
        List<Observation> allObservations = observationProviderHelper.getAllObservationsFromDb(id.getIdPart());

        if (allObservations.isEmpty()) {
            throw new ResourceNotFoundException(id);
        }

        return allObservations.get(0);
    }
}

