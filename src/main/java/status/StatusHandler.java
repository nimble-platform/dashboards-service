package status;

import checks.BasicHealthChecker;
import checks.CheckResult;
import checks.DBVariables;
import checks.DBHealthCheck;
import checks.HealthChecker;
import configs.DatabaseConfig;
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
    private final Object statusSync = new Object();

    private String statusRowTemplate = "<tr><td class=\"statusData\">%s %s</tr>";


    public StatusHandler(int frequencyInSec, List<ServiceConfig> serviceToCheck, List<DatabaseConfig> dbsToCheck) {
        Map<String, HealthChecker> healthChecks = new HashMap<>();

        for (ServiceConfig sc : serviceToCheck) {
            healthChecks.put(sc.getName(), new BasicHealthChecker(sc.getName(), sc.getUrl()));
            serviceToStatus.put(sc.getName(), new HealthStatus());
        }

        for (DatabaseConfig dbc : dbsToCheck) {
            DBVariables variables = new DBVariables(dbc.getDriverName(), dbc.getEnvUsername(), dbc.getEnvUrl(), dbc.getEnvPassword());
            healthChecks.put(dbc.getName(), new DBHealthCheck(dbc.getDriverName(), variables));
            serviceToStatus.put(dbc.getName(), new HealthStatus());
        }

        startChecksThread(frequencyInSec * 60 * 1000, healthChecks);
        logger.info("The check thread has been started");
    }

    private void startChecksThread(int sleepDuration, Map<String, HealthChecker> serviceToCheck) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, CheckResult> serviceToCheckResult = new HashMap<>();
                    while (true) {
                        logger.info("Running checks");

                        serviceToCheck.forEach((service, check) -> {
                            CheckResult result = check.runCheck();
                            serviceToCheckResult.put(service, result);
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
}

