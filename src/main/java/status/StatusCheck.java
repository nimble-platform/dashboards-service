package status;

import common.Common;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

/**
 * Created by evgeniyh on 1/15/18.
 */

public class StatusCheck {
    private final static Logger logger = Logger.getLogger(StatusCheck.class);

    private final String healthUrl;
    private final int expectedCode;
    private final String expectedMessage;

    public StatusCheck(String healthUrl, int expectedCode, String expectedMessage) {
        this.healthUrl = healthUrl;
        this.expectedCode = expectedCode;
        this.expectedMessage = expectedMessage;
    }

    Status runCheck() {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet getHealth = new HttpGet(healthUrl);

            HttpResponse response = httpclient.execute(getHealth);
            String res = Common.inputStreamToString(response.getEntity().getContent());

            if (res == null || !res.equals(expectedMessage) || response.getStatusLine().getStatusCode() != expectedCode) {
                 logger.error("Failed on the health check");
                 return Status.BAD;
            }

            logger.info("The health check was successful");

            return Status.GOOD;
        } catch (Throwable e) {
            logger.error("Exception during health check", e);
            return Status.BAD;
        }
    }
}
