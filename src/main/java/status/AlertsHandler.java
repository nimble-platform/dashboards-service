package status;

/**
 * Created by evgeniyh on 1/15/18.
 */

public class AlertsHandler {
    private int failsThreshold;

    public AlertsHandler(int failsThreshold) {
        this.failsThreshold = failsThreshold;
    }

    public void handlerAlert(String serviceName, int currentFailures) {
        if (currentFailures <= failsThreshold) {
            return;
        }

        System.out.println(String.format("Executing alert - received '%d' failures for service '%s'", currentFailures, serviceName));
    }
}
