package checks;

import static common.Common.isNullOrEmpty;

import common.Common;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

/**
 * Created by evgeniyh on 2/8/18.
 */

public class BasicHealthChecker implements HealthChecker {
    private final static Logger logger = Logger.getLogger(BasicHealthChecker.class);

    private final String healthUrl;
    private final String serviceName;

    public BasicHealthChecker(String serviceName, String healthUrl) {
        if (isNullOrEmpty(serviceName) || isNullOrEmpty(healthUrl)) {
            throw new NullPointerException("Service name and health url cannot be empty");
        }

        this.serviceName = serviceName;
        this.healthUrl = healthUrl;
    }

    @Override
    public CheckResult runCheck() {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet getHealth = new HttpGet(healthUrl);
            CloseableHttpResponse response = httpclient.execute(getHealth);

            if (response == null) {
                throw new RuntimeException("http response was null for - " + healthUrl);
            }
            String res = Common.inputStreamToString(response.getEntity().getContent());
            System.out.println("For - " + healthUrl + " response is " + res);

            CheckResult result = CheckResult.createResult(response.getStatusLine().getStatusCode(), res, "OK");
            CheckResult.logResult(serviceName, result);

            return result;
        } catch (Throwable e) {
            logger.error("Exception during health check of service " + serviceName, e);
            Throwable t = e.getCause();
            String exceptionMessage = (t == null) ? e.getMessage() : t.getMessage();
            return new CheckResult(CheckResult.Result.BAD, "Exception - " + exceptionMessage);
        }
    }
}
