package status;

import checks.CheckResult;

/**
 * Created by evgeniyh on 1/17/18.
 */

public class ServiceHealthStatus extends AbstractHealthStatus {
    private final String buttonHtml;
    private String rawTemplate = "</td><td class=\"statusData\">%s</td><td bgcolor=\"%s\" title=\"%s\" class=\"statusData\">%s %s</td>";

    public ServiceHealthStatus(String k8sServiceName) {
        buttonHtml = String.format("<button onclick=\"restartService('%s')\">Restart</button>", k8sServiceName);
    }

    @Override
    public String generateHtml() {
        String date = getDateString();
        String description = getDescription();

        CheckResult.Result res = lastCheck.getResult();
        String buttonString = (res == CheckResult.Result.GOOD) ? "" : buttonHtml;

        return String.format(rawTemplate, date, res.getColor(), description, res.toString(), buttonString);
    }
}
