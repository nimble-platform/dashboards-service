package status;

import checks.BasicHealthChecker;
import checks.CheckResult;
import checks.DBHealthCheck;
import checks.EurekaHealthCheck;
import checks.HealthChecker;
import checks.MessageHubHealthCheck;
import common.Incident;
import common.SlackBotHandler;
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

    private static long ONE_HOUR_IN_MILLIS = 60 * 60 * 1000;
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
        try {
            eurekaHealthCheck.init();
            logger.info("Eureka service was initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Eureka service", e);
        }

        initAndAddService(eurekaConfig.getName(), eurekaHealthCheck, new NonServiceHealthStatus(), servicesCheckers, serviceToStatus);
        initAndAddService(messageHubConfig.getName(), new MessageHubHealthCheck(messageHubConfig), new NonServiceHealthStatus(), infrastructuresCheckers, infrastructureToStatus);

//        TODO: add when object storage becomes a service on the platform and add the missing configs
//        initAndAddService(objectStore.getName(), new ObjectStoreHealthChecker(objectStore), new NonServiceHealthStatus(), infrastructuresCheckers, infrastructureToStatus);

        for (SimpleServiceConfig sc : serviceToCheck) {
            initAndAddService(sc.getName(), new BasicHealthChecker(sc.getName(), sc.getUrl()), new ServiceHealthStatus(sc.getK8sName()), servicesCheckers, serviceToStatus);
        }
        for (DatabaseConfig dbc : dbsToCheck) {
            initAndAddService(dbc.getName(), new DBHealthCheck(dbc), new NonServiceHealthStatus(), infrastructuresCheckers, infrastructureToStatus);
        }

        startChecksThread(frequencyInSec * 1000, servicesSync, servicesCheckers, serviceToStatus);
        startChecksThread(frequencyInSec * 1000, infrastructureSync, infrastructuresCheckers, infrastructureToStatus);

        startSlackThread();
        logger.info("The check thread has been started");
    }

    private void startSlackThread() {
        new Thread(() -> {
            try {
                while (true) {
                    StringBuilder sb = new StringBuilder();
                    logger.info("Starting services statuses data collection");
                    sb.append("_*Infrastructure Services:*_\n");
                    synchronized (infrastructureSync) {
                        infrastructureToStatus.forEach((key, value) -> insertSlackRowForService(sb, key, value));
                    }
                    sb.append("\n_*Platform Services:*_\n");
                    synchronized (servicesSync) {
                        Map<String, NonServiceHealthStatus> eurekaService = eurekaHealthCheck.getRegisteredServicesResults();
                        eurekaService.forEach((key, value) -> insertSlackRowForService(sb, "Eureka - " + key, value));
                        serviceToStatus.forEach((key, value) -> insertSlackRowForService(sb, key, value));
                    }
                    SlackBotHandler.sendMessageToChannel(sb.toString());
                    Thread.sleep(ONE_HOUR_IN_MILLIS);
                }
            } catch (Throwable t) {
                logger.error("Exception during thread of slack notification", t);
            }
        }).start();
    }

    private void insertSlackRowForService(StringBuilder sb, String serviceName, AbstractHealthStatus status) {
        CheckResult res = status.getLastCheck();
        CheckResult.Result lastResult = res.getResult();
        sb.append(serviceName).append(" = ");
        if (lastResult == CheckResult.Result.BAD) {
            sb.append("*").append(lastResult).append("*").append(", Last good check was - ").append(status.getDateString());
        } else {
            sb.append(lastResult);
        }
        sb.append("\n");
    }

    private void initAndAddService(String service, HealthChecker checker, AbstractHealthStatus status, Map<String, HealthChecker> checkersMap, Map<String, AbstractHealthStatus> statusMap) {
        try {
            logger.info("Running init method for service - " + service);
            checker.init();
            logger.info("Successfully initialized health check for service - " + service);
            statusMap.put(service, status);
            checkersMap.put(service, checker);
        } catch (Exception e) {
            String msg = "Failed to initialize health checker for service: " + service + ", error: " + e.getMessage();
            logger.error(msg, e);
            Incident i = new Incident(System.currentTimeMillis(), service, msg);
            StatusDashboard.dbManager.addIncident(i);
        }
    }

    private void startChecksThread(int sleepDuration, final Object sync, Map<String, HealthChecker> checks, Map<String, AbstractHealthStatus> statuses) {
        new Thread(() -> {
            synchronized (sync) {
                setInitialStatuses(checks, statuses);
            }
            while (true) {
                try {
                    Map<String, CheckResult> serviceToCheckResult = runHealthChecks(checks);
                    synchronized (sync) {
                        serviceToCheckResult.forEach((k, v) -> {
                            AbstractHealthStatus serviceStatus = statuses.get(k);
                            CheckResult.Result previous = serviceStatus.getLastCheck().getResult();
                            if (previous != v.getResult()) {
                                long currentTime = System.currentTimeMillis();

                                String message = generateIncidentMessage(previous, k, serviceStatus.getDateString(), currentTime);
                                SlackBotHandler.sendMessageToChannel(message);

                                Incident i = new Incident(currentTime, k, message);
                                StatusDashboard.dbManager.addIncident(i);
                            }
                            statuses.get(k).setLastCheck(v);
                        });
                    }
                    logger.info("Sleeping for - " + sleepDuration);
                    Thread.sleep(sleepDuration);
                } catch (Throwable t) {
                    logger.error("Failure during execution of the health checks thread", t);
                }
            }
        }).start();
    }

    private void setInitialStatuses(Map<String, HealthChecker> checks, Map<String, AbstractHealthStatus> statuses) {
        logger.info("Setting initial statuses for the services");
        Map<String, CheckResult> serviceToCheckResult = runHealthChecks(checks);
        serviceToCheckResult.forEach((k, v) -> statuses.get(k).setLastCheck(v));
    }

    private Map<String, CheckResult> runHealthChecks(Map<String, HealthChecker> checks) {
        logger.info("Running health checks");
        Map<String, CheckResult> serviceToCheckResult = new HashMap<>();

        checks.forEach((service, check) -> {
            CheckResult result = check.runCheck();
            serviceToCheckResult.put(service, result);
        });

        logger.info("Running health checks is completed");
        return serviceToCheckResult;
    }

    private String generateIncidentMessage(CheckResult.Result previous, String service, String previousSuccessfulCheck, long currentTime) {
        return (previous == CheckResult.Result.GOOD) ?
                String.format("Service '%s' seems to go down - the last good check was on '%s'", service, previousSuccessfulCheck) :
                String.format("Service '%s' has returned to healthy state - the good health check was on '%s'", service, StatusDashboard.dateFormatter.format(currentTime));
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

