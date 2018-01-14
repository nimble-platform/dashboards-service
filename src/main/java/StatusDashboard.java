import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * Created by evgeniyh on 1/14/18.
 */

@ApplicationPath("/")
@Path("/status")
public class StatusDashboard extends Application {
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getStatisticsDashboard() {
        InputStream is = getClass().getResourceAsStream("status.html");
        return Response.status(200).entity(is).build();
    }
}
