package com.medblocks.web;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import com.medblocks.utils.Configurable;
import com.medblocks.utils.MigrationService;
import com.medblocks.providers.ObservationProvider;
import com.medblocks.providers.PatientProvider;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FhirServlet extends RestfulServer implements Configurable {

    @Override
    protected void initialize() throws ServletException {
        setFhirContext(FhirContext.forR4());

        try {
            // Generate tables and data
            if (configService.getConfigDataGenerateData()) {
                MigrationService.generateTables();
                MigrationService.generateData();
            }

            // providers
            registerProvider(new PatientProvider());
            registerProvider(new ObservationProvider());

            // interceptors
            registerInterceptor(new ResponseHighlighterInterceptor());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {
        System.out.println("FHIR SERVLET DESTROYED");
    }
}
