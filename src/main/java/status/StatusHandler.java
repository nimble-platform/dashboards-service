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

    private final Map<String, AbstractHealthStatus> serviceToStatus = new HashMap<>();
    private final Map<String, AbstractHealthStatus> infrastructureToStatus = new HashMap<>();

    private final Map<String, HealthChecker> servicesCheckers = new HashMap<>();
    private final Map<String, HealthChecker> infrastructuresCheckers = new HashMap<>();

    private final Object infrastructureSync = new Object();
    private final Object servicesSync = new Object();

    private final EurekaHealthCheck eurekaHealthCheck;

    private String statusRowTemplate = "<tr><td class=\"statusData\">%s %s</tr>";


    public StatusHandler(int frequencyInSec, List<SimpleServiceConfig> serviceToCheck, List<DatabaseConfig> dbsToCheck, MessageHubConfig messageHubConfig, ObjectStoreConfig objectStore, EurekaConfig eurekaConfig) {

        eurekaHealthCheck = new EurekaHealthCheck(eurekaConfig);
        if (initService(eurekaHealthCheck)) {
            logger.info("Eureka service was initialized successfully");
        } else {
            logger.error("Failed to initialize Eureka service");
        }

        initAndAddService(eurekaConfig.getName(), eurekaHealthCheck, new NonServiceHealthStatus(), servicesCheckers, serviceToStatus);
        initAndAddService(objectStore.getName(), new ObjectStoreHealthChecker(objectStore), new NonServiceHealthStatus(), infrastructuresCheckers, infrastructureToStatus);
        initAndAddService(messageHubConfig.getName(), new MessageHubHealthCheck(messageHubConfig), new NonServiceHealthStatus(), infrastructuresCheckers, infrastructureToStatus);

        for (SimpleServiceConfig sc : serviceToCheck) {
            initAndAddService(sc.getName(), new BasicHealthChecker(sc.getName(), sc.getUrl()), new ServiceHealthStatus(sc.getK8sName()), servicesCheckers, serviceToStatus);
        }
        for (DatabaseConfig dbc : dbsToCheck) {
            initAndAddService(dbc.getName(), new DBHealthCheck(dbc), new NonServiceHealthStatus(), infrastructuresCheckers, infrastructureToStatus);
        }

        startChecksThread(frequencyInSec * 1000, servicesSync, servicesCheckers, serviceToStatus);
        startChecksThread(frequencyInSec * 1000, infrastructureSync, infrastructuresCheckers, infrastructureToStatus);
        logger.info("The check thread has been started");
    }

    private void initAndAddService(String service, HealthChecker checker, AbstractHealthStatus status, Map<String, HealthChecker> checkersMap, Map<String, AbstractHealthStatus> statusMap) {
        if (!initService(checker)) {
            logger.error("Failed to initialize health checker for service - " + service);
        } else {
            logger.info("Successfully initialized health check for service - " + service);
            statusMap.put(service, status);
            checkersMap.put(service, checker);
        }
    }

    private boolean initService(HealthChecker checker) {
        try {
            checker.init();
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    private void startChecksThread(int sleepDuration, Object sync, Map<String, HealthChecker> checks, Map<String, AbstractHealthStatus> statuses) {
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
            Map<String, NonServiceHealthStatus> eurekaService = eurekaHealthCheck.getRegisteredServicesResults();
            List<String> tmp = eurekaService.entrySet().stream().map(e -> String.format(statusRowTemplate, "Eureka (" + e.getKey() + ")", e.getValue().generateHtml())).collect(Collectors.toList());
            tmp.addAll(serviceToStatus.entrySet().stream().map(e -> String.format(statusRowTemplate, e.getKey(), e.getValue().generateHtml())).collect(Collectors.toList()));
            return tmp;
        }
    }

    public List<String> getInfrastructureStatusesHtmls() {
        synchronized (infrastructureSync) {
            return infrastructureToStatus.entrySet().stream().map(e -> String.format(statusRowTemplate, e.getKey(), e.getValue().generateHtml())).collect(Collectors.toList());
        }
    }
}

