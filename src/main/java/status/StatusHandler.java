package status;

import checks.BasicHealthChecker;
import checks.CheckResult;
import checks.DBHealthCheck;
import checks.HealthChecker;
import checks.MessageHubHealthCheck;
import configs.DatabaseConfig;
import configs.MessageHubConfig;
import configs.ServiceConfig;
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
    private final Map<String, HealthChecker> healthChecks = new HashMap<>();

    private final Object statusSync = new Object();

    private String statusRowTemplate = "<tr><td class=\"statusData\">%s %s</tr>";


    public StatusHandler(int frequencyInSec, List<ServiceConfig> serviceToCheck, List<DatabaseConfig> dbsToCheck, MessageHubConfig messageHubConfig) {

        //TODO: add interface to configs to get the service name

        addNewService(messageHubConfig.getMessageHubName(), new MessageHubHealthCheck(messageHubConfig.getMessageHubName(), messageHubConfig));

        for (ServiceConfig sc : serviceToCheck) {
            addNewService(sc.getName(), new BasicHealthChecker(sc.getName(), sc.getUrl()));
        }
        for (DatabaseConfig dbc : dbsToCheck) {
            addNewService(dbc.getName(), new DBHealthCheck(dbc.getName(), dbc));
        }

        startChecksThread(frequencyInSec * 60 * 1000, healthChecks);
        logger.info("The check thread has been started");
    }

    private void addNewService(String serviceName, HealthChecker healthChecker) {
        try {
            healthChecker.init();
            healthChecks.put(serviceName, healthChecker);
            serviceToStatus.put(serviceName, new HealthStatus());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Failed to initialize health check for service - " + serviceName);
        }
    }

    private void startChecksThread(int sleepDuration, Map<String, HealthChecker> serviceToCheck) {
        new Thread(() -> {
            while (true) {
                try {
                    Map<String, CheckResult> serviceToCheckResult = new HashMap<>();
                    logger.info("Running checks");

                    serviceToCheck.forEach((service, check) -> {
                        CheckResult result = check.runCheck();
                        serviceToCheckResult.put(service, result);
                    });

                    logger.info("Running check is completed");
                    synchronized (statusSync) {
                        serviceToCheckResult.forEach((k, v) -> serviceToStatus.get(k).updateLastCheck(v));
                    }
                    logger.info("Sleeping for - " + sleepDuration);
                    Thread.sleep(sleepDuration);
                } catch (Throwable t) {
                    t.printStackTrace();
                    logger.error("Failure during execution of the health checks thread", t);
                }
            }
        }).start();
    }

    public List<String> getStatusesHtmls() {
        synchronized (statusSync) {
            return serviceToStatus.entrySet().stream().map(e -> String.format(statusRowTemplate, e.getKey(), e.getValue().generateHtml())).collect(Collectors.toList());
        }
    }
}

