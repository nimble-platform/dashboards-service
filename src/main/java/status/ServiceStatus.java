package status;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Created by evgeniyh on 1/15/18.
 */

public class ServiceStatus {
    private String serviceName;
    private StatusCheck statusCheck;

    private int sequencedFails = 0;
    private long lastSuccess = 0;
    private Status lastCheck;
    private String statusRowTemplate = "<tr><td class=\"statusData\">%s</td><td class=\"statusData\">%s</td><td bgcolor=%s class=\"statusData\">%s</td></tr>";

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");

    public ServiceStatus(String serviceName, StatusCheck statusCheck) {
        this.serviceName = serviceName;
        this.statusCheck = statusCheck;
        dateFormatter.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    }

    public void runCheck() {
        lastCheck = statusCheck.runCheck();
        if (lastCheck == Status.GOOD) {
            lastSuccess = System.currentTimeMillis();
            sequencedFails =0;
        } else {
            sequencedFails++;

        }
    }

    public String generateHtml() {
        Instant instant = Instant.ofEpochSecond(lastSuccess);
        ZonedDateTime dt =ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
        return String.format(statusRowTemplate, serviceName, dateFormatter.format(lastSuccess), lastCheck.getColor(), lastCheck.toString());
    }
}
