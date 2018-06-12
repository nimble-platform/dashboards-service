package status;

import checks.CheckResult;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by evgeniyh on 4/15/18.
 */

public abstract class AbstractHealthStatus {
    private int sequencedFails = 0;
    private long lastSuccess = 0;
    protected CheckResult lastCheck;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss (z)");

    protected AbstractHealthStatus() {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    protected String getDateString() {
        return (lastSuccess == 0) ? "Never" : dateFormatter.format(lastSuccess);
    }

    protected String getDescription() {
        String description = lastCheck.getDescriptionMessage();
        return (description == null) ? "" : description;
    }

    public void setLastCheck(CheckResult lastCheck) {
        this.lastCheck = lastCheck;
        if (lastCheck.getResult() == CheckResult.Result.GOOD) {
            lastSuccess = System.currentTimeMillis();
            sequencedFails = 0;
        } else {
            sequencedFails++;
        }
    }

    public CheckResult getLastCheck() {
        return lastCheck;
    }

    public abstract String generateHtml();
}
