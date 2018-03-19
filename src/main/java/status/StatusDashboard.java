package status;

import com.google.gson.Gson;
import common.Common;
import configs.StatusConfigurations;
import org.apache.log4j.Logger;

import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by evgeniyh on 1/14/18.
 */

@ApplicationPath("/")
@Path("/")
@Singleton
public class StatusDashboard extends Application {
    private final static Logger logger = Logger.getLogger(StatusDashboard.class);

    private final Map<String, String> envToFile = new HashMap<String, String>() {
        {
            put("dev", "/dev_configurations.json");
            put("prod", "/configurations.json");

        }
    };
    private StatusHandler handler;
    private String htmlTemplate = Common.inputStreamToString(getClass().getResourceAsStream("/status.html"));

    public StatusDashboard() {
        String env = System.getenv("ENVIRONMENT");
        if (env == null || env.isEmpty()) {
            throw new RuntimeException("Missing the ENVIRONMENT environment variable");
        }
        String configFile = envToFile.get(env);
        logger.info("config file - " + configFile);
        String jsonConfig = Common.inputStreamToString(getClass().getResourceAsStream(configFile));
        if (jsonConfig == null) {
            logger.error("Failed to load configurations");
            throw new NullPointerException("Failed to load configurations");
        }

        StatusConfigurations conf = (new Gson()).fromJson(jsonConfig, StatusConfigurations.class);

        handler = new StatusHandler(conf.getFrequency(), conf.getServices(), conf.getDatabases(), conf.getMessageHub(), conf.getObjectStore(), conf.getEureka());
    }

    @GET
    public Response getHello() {
        return Response.status(200).entity("Hello from Dashboards-Service").build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/status")
    public Response getStatisticsDashboard() {
        logger.info("Creating status dashboard");

        StringBuilder sb = new StringBuilder();
        List<String> servicesHtmls = handler.getServicesStatusesHtmls();
        servicesHtmls.forEach(sb::append);
        String servicesHtml = sb.toString();

        sb.setLength(0);

        List<String> infrastructures = handler.getInfrastructureStatusesHtmls();
        infrastructures.forEach(sb::append);
        String infrastructuresHtmls = sb.toString();

        String completedHtml = String.format(htmlTemplate, servicesHtml, infrastructuresHtmls);

        return Response.status(200).entity(completedHtml).build();
    }
}
