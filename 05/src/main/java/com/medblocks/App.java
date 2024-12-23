package com.medblocks;

import com.medblocks.utils.Configurable;
import com.medblocks.web.FhirServlet;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

@Slf4j
public class App implements Configurable {
    public static void main(String[] args) throws Exception {
        log.info("Starting server");
        Server server = new Server(Integer.parseInt(configService.getAppPort()));

        FhirServlet fhirServlet = new FhirServlet();
        ServletContextHandler handler = new ServletContextHandler();

        handler.addServlet(new ServletHolder(fhirServlet), "/*");
        server.setHandler(handler);

        server.start();
        server.join();
    }
}
