package status;

import common.Common;
import org.apache.log4j.Logger;

import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

/**
 * Created by evgeniyh on 1/14/18.
 */

@ApplicationPath("/")
@Path("/status")
@Singleton
public class StatusDashboard extends Application {
    private final static Logger logger = Logger.getLogger(StatusDashboard.class);

    private StatusHandler handler = new StatusHandler(5);
    private String htmlTemplate = Common.inputStreamToString(getClass().getResourceAsStream("status.html"));

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getStatisticsDashboard() {
        StringBuilder sb = new StringBuilder();
        List<String> services = handler.getStatusesHtmls();

        services.forEach(sb::append);

        String completedHtml = String.format(htmlTemplate, sb.toString());
        logger.info("The created html dashboard is " + completedHtml);

        return Response.status(200).entity(completedHtml).build();
    }
}
