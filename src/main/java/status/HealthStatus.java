package status;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by evgeniyh on 1/17/18.
 */

class HealthStatus {
    private int sequencedFails = 0;
    private long lastSuccess = 0;
    private CheckResult lastCheck;
    private String statusRowTemplate = "</td><td class=\"statusData\">%s</td><td bgcolor=%s title=\"%s\" class=\"statusData\">%s</td>";

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss (z)");

    HealthStatus() {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    void updateLastCheck(CheckResult lastCheck) {
        this.lastCheck = lastCheck;
        if (lastCheck.getResult() == CheckResult.Result.GOOD) {
            lastSuccess = System.currentTimeMillis();
            sequencedFails = 0;
        } else {
            sequencedFails++;

        }
    }

    String generateHtml() {
        String date = (lastSuccess == 0) ? "Never" : dateFormatter.format(lastSuccess);
        String title = lastCheck.getErrorMessage();
        if (title == null) {
            title = "";
        }
        return String.format(statusRowTemplate, date, lastCheck.getResult().getColor(), title, lastCheck.getResult().toString());
    }
}
