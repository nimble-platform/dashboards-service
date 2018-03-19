package status;

import checks.BasicHealthChecker;
import checks.CheckResult;
import checks.DBHealthCheck;
import checks.EurekaHealthCheck;
import checks.HealthChecker;
import checks.MessageHubHealthCheck;
import checks.ObjectStoreHealthChecker;
import configs.DatabaseConfig;
import configs.EurekaConfig;
import configs.MessageHubConfig;
import configs.ObjectStoreConfig;
import configs.SimpleServiceConfig;
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
    private final Map<String, HealthStatus> infrastructureToStatus = new HashMap<>();

    private final Map<String, HealthChecker> servicesCheckers = new HashMap<>();
    private final Map<String, HealthChecker> infrastructuresCheckers = new HashMap<>();

    private final Object infrastructureSync = new Object();
    private final Object servicesSync = new Object();

    private String statusRowTemplate = "<tr><td class=\"statusData\">%s %s</tr>";


    public StatusHandler(int frequencyInSec, List<SimpleServiceConfig> serviceToCheck, List<DatabaseConfig> dbsToCheck, MessageHubConfig messageHubConfig, ObjectStoreConfig objectStore, EurekaConfig eurekaConfig) {

        addNewService(eurekaConfig.getName(), new EurekaHealthCheck(eurekaConfig), false);
        addNewService(objectStore.getName(), new ObjectStoreHealthChecker(objectStore), true);
        addNewService(messageHubConfig.getName(), new MessageHubHealthCheck(messageHubConfig), true);

        for (SimpleServiceConfig sc : serviceToCheck) {
            addNewService(sc.getName(), new BasicHealthChecker(sc.getName(), sc.getUrl()), false);
        }
        for (DatabaseConfig dbc : dbsToCheck) {
            addNewService(dbc.getName(), new DBHealthCheck(dbc), true);
        }

        startChecksThread(frequencyInSec * 1000, servicesSync, servicesCheckers, serviceToStatus);
        startChecksThread(frequencyInSec * 1000, infrastructureSync, infrastructuresCheckers, infrastructureToStatus);
        logger.info("The check thread has been started");
    }

    private void addNewService(String serviceName, HealthChecker healthChecker, boolean isInfrastructure) {
        try {
            healthChecker.init();

            if (isInfrastructure) {
                infrastructureToStatus.put(serviceName, new HealthStatus());
                infrastructuresCheckers.put(serviceName, healthChecker);
            } else {
                serviceToStatus.put(serviceName, new HealthStatus());
                servicesCheckers.put(serviceName, healthChecker);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Failed to initialize health check for service - " + serviceName);
        }
    }

    private void startChecksThread(int sleepDuration, Object sync, Map<String, HealthChecker> checks, Map<String, HealthStatus> statuses) {
        new Thread(() -> {
            while (true) {
                try {
                    Map<String, CheckResult> serviceToCheckResult = new HashMap<>();
                    logger.info("Running health checks checks");

                    checks.forEach((service, check) -> {
                        CheckResult result = check.runCheck();
                        serviceToCheckResult.put(service, result);
                    });

                    logger.info("Running health checks is completed");
                    synchronized (sync) {
                        serviceToCheckResult.forEach((k, v) -> statuses.get(k).updateLastCheck(v));
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

    public List<String> getServicesStatusesHtmls() {
        synchronized (servicesSync) {
            return serviceToStatus.entrySet().stream().map(e -> String.format(statusRowTemplate, e.getKey(), e.getValue().generateHtml())).collect(Collectors.toList());
        }
    }

    public List<String> getInfrastructureStatusesHtmls() {
        synchronized (infrastructureSync) {
            return infrastructureToStatus.entrySet().stream().map(e -> String.format(statusRowTemplate, e.getKey(), e.getValue().generateHtml())).collect(Collectors.toList());
        }
    }
}

