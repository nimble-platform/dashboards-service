package common;

import checks.DBHealthCheck;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Created by evgeniyh on 1/16/18.
 */

public class Common {
    private final static Logger logger = Logger.getLogger(Common.class);

    public static String inputStreamToString(InputStream stream) {
        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer, "UTF-8");
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    TODO: fix issue with closing the client when exiting (response is closed)
    public static CloseableHttpResponse executeHttpGet(String url) throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet getHealth = new HttpGet(url);
            return httpclient.execute(getHealth);

        } catch (Throwable t) {
            logger.error("Error during execution of GET on - " + url, t);
            return null;
        }
    }

    public static boolean isNullOrEmpty(String s) {
        return (s == null || s.isEmpty());
    }
}
