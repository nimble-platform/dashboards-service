package status;

import checks.CheckResult;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by evgeniyh on 1/17/18.
 */

public class HealthStatus {
    private int sequencedFails = 0;
    private long lastSuccess = 0;
    private CheckResult lastCheck;

    protected String statusRowTemplate = "</td><td class=\"statusData\">%s</td><td bgcolor=\"%s\" title=\"%s\" class=\"statusData\">%s</td>";

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss (z)");

    public HealthStatus() {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    public void updateLastCheck(CheckResult lastCheck) {
        this.lastCheck = lastCheck;
        if (lastCheck.getResult() == CheckResult.Result.GOOD) {
            lastSuccess = System.currentTimeMillis();
            sequencedFails = 0;
        } else {
            sequencedFails++;
        }
    }

    public String generateHtml() {
        String date = (lastSuccess == 0) ? "Never" : dateFormatter.format(lastSuccess);
        String title = lastCheck.getDescriptionMessage();
        if (title == null) {
            title = "";
        }
        return String.format(statusRowTemplate, date, lastCheck.getResult().getColor(), title, lastCheck.getResult().toString());
    }
}
