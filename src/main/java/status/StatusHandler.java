package status;

import common.Common;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by evgeniyh on 1/15/18.
 */

public class StatusHandler {
    private final static Logger logger = Logger.getLogger(StatusHandler.class);

    private final Map<String, HealthStatus> serviceToStatus = new HashMap<>();
    private final Object statusSync = new Object();

    private String statusRowTemplate = "<tr><td class=\"statusData\">%s %s</tr>";


    public StatusHandler(int frequencyInSec, List<String> servicesNames) {
        servicesNames.forEach(s -> serviceToStatus.put(s, new HealthStatus()));

        startChecksThread(frequencyInSec * 60 * 1000, servicesNames);
        logger.info("The check thread has been started");
    }

    private void startChecksThread(int sleepDuration, List<String> servicesNames) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, CheckResult> serviceToCheckResult = new HashMap<>();
                    while (true) {
                        logger.info("Running checks");

                        servicesNames.forEach(s -> {
                            CheckResult result = runCheck(s);
                            serviceToCheckResult.put(s, result);
                        });
                        synchronized (statusSync) {
                            serviceToCheckResult.forEach((k, v) -> serviceToStatus.get(k).updateLastCheck(v));
                        }
                        logger.info("Sleeping for - " + sleepDuration);
                        Thread.sleep(sleepDuration);
                    }
                } catch (Exception ex) {
                    logger.error("Failure during execution of the health checks thread", ex);
                    run();
                }
            }
        }).start();
    }

    public List<String> getStatusesHtmls() {
        synchronized (statusSync) {
            return serviceToStatus.entrySet().stream().map(e -> String.format(statusRowTemplate, e.getKey(), e.getValue().generateHtml())).collect(Collectors.toList());
        }
    }


    private CheckResult runCheck(String serviceName) {
        String healthUrl = serviceName + "/health-check";
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet getHealth = new HttpGet(healthUrl);

            HttpResponse response = httpclient.execute(getHealth);
            String res = Common.inputStreamToString(response.getEntity().getContent());

            String errorMessage;
            if (res == null) {
                errorMessage = "Response was null";
            } else if (!res.equals("OK")) {
                errorMessage = "Response wasn't OK, it was - " + res;
            } else if (response.getStatusLine().getStatusCode() != 200) {
                errorMessage = "Status wasn't 200, it was - " + String.valueOf(response.getStatusLine().getStatusCode());
            } else {
                logger.info("The health check was successful for service - " + serviceName);
                return new CheckResult(CheckResult.Result.GOOD, null);
            }

            logger.error("Failed on the health check of service - " + serviceName + " due to - " + errorMessage );
            return new CheckResult(CheckResult.Result.BAD, errorMessage);
        } catch (Throwable e) {
            logger.error("Exception during health check of service " + serviceName, e);
            return new CheckResult(CheckResult.Result.BAD, "Exception - " +e.getCause().getMessage());
        }
    }
}

