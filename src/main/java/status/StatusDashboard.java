package status;

import com.google.gson.Gson;
import common.Common;
import common.Incident;
import common.StatusDbHandler;
import configs.IncidentsDbConfig;
import configs.StatusConfigurations;
import connector.ManagerConfig;
import org.apache.log4j.Logger;
import org.jboss.resteasy.util.HttpResponseCodes;

import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.DatabaseMetaData;
import java.text.SimpleDateFormat;
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

    static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss (z)");

    private final Map<String, String> envToFile = new HashMap<String, String>() {
        {
            put("dev", "/dev_configurations.json");
            put("prod", "/configurations.json");

        }
    };
    private StatusHandler handler;
    private String htmlTemplate = Common.inputStreamToString(getClass().getResourceAsStream("/status.html"));

    static StatusDbHandler dbManager;

    public StatusDashboard() {
        String env = System.getenv("ENVIRONMENT");
        if (env == null || env.isEmpty()) {
            throw new RuntimeException("Missing the ENVIRONMENT environment variable");
        }
        String configFile = envToFile.get(env);
        if (configFile == null) {
            throw new IllegalArgumentException("Not supported environment variable - " + env);
        }
        logger.info("Loading the config file - " + configFile);
        String jsonConfig = Common.inputStreamToString(getClass().getResourceAsStream(configFile));
        if (jsonConfig == null) {
            logger.error("Failed to load configurations");
            throw new NullPointerException("Failed to load configurations");
        }

        StatusConfigurations c = (new Gson()).fromJson(jsonConfig, StatusConfigurations.class);

        setUpDbConnectorAndTable(c.getIncidentsDb());

        handler = new StatusHandler(c.getFrequency(), c.getServices(), c.getDatabases(), c.getMessageHub(), c.getObjectStore(), c.getEureka());
    }

    private void setUpDbConnectorAndTable(IncidentsDbConfig dbConfig) {
        try {
            String dbUrl = System.getenv(dbConfig.getEnvUrl());
            String username = System.getenv(dbConfig.getEnvUsername());
            String password = System.getenv(dbConfig.getEnvPassword());
            ManagerConfig config = new ManagerConfig(dbConfig.getDriverName(), username, password, dbUrl);

            String tableName = dbConfig.getTableName();
            logger.info("Creating db manager for - " + dbConfig.getName() + " using table - " + tableName);

            dbManager = new StatusDbHandler(config, tableName);

            logger.info("Getting metadata from the db");
            DatabaseMetaData metaData = dbManager.getMetaData();

            String createQuery = String.format(
                    "CREATE TABLE %s " +
                            "( time    BIGINT  NOT NULL ," +
                            "  service TEXT    NOT NULL ," +
                            "  message TEXT    NOT NULL ," +
                            "  PRIMARY KEY(time) );", tableName);
            dbManager.createTableIfMissing(metaData, tableName, createQuery);
        } catch (Exception e) {
            logger.error("Error during creation of db manager", e);
            throw new IllegalStateException(e);
        }
    }

    @GET
    public Response getHello() {
        return Response.status(200).entity("Hello from Dashboards-Service").build();
    }

    private static String COMMAND_TEMPLATE = "kubectl -n prod delete pod -l app=%s";

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/restart_pod/{serviceName}")
    public Response restartService(@PathParam("serviceName") String serviceName) {
        logger.info("Preparing to restart service name - " + serviceName);
        String command = String.format(COMMAND_TEMPLATE, serviceName);

        executeCommand(command);

        return Response.status(200).entity("").build();
    }

    private void executeCommand(String command) {
        logger.info("Executing command - " + command);
        try {
            Process p = Runtime.getRuntime().exec(command);

            System.out.println("INPUT STREAM:\n" + Common.inputStreamToString(p.getInputStream()));
            System.out.println("ERROR STREAM:\n" + Common.inputStreamToString(p.getErrorStream()));

            logger.info("Command completed successfully");
        } catch (Throwable t) {
            t.printStackTrace();
            logger.error("Failed to execute command ", t);
        }
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
        sb.setLength(0);

        List<Incident> incidents = dbManager.getAllIncidents();
        for (Incident i : incidents) {
            sb.append("<tr>");
            sb.append("<td class=\"statusData\" style=\"width:20%\">").append(dateFormatter.format(i.getTime())).append("</td>");
            sb.append("<td class=\"statusData\" style=\"width:15%\">").append(i.getService()).append("</td>");
            sb.append("<td class=\"statusData\" style=\"width:65%\">").append(i.getMessage()).append("</td>");
            sb.append("</tr>");
        }
        String completedHtml = String.format(htmlTemplate, servicesHtml, infrastructuresHtmls, sb.toString());

        return Response.status(HttpResponseCodes.SC_OK).entity(completedHtml).build();
    }
}
