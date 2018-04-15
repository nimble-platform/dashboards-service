package status;

import checks.CheckResult;

/**
 * Created by evgeniyh on 4/15/18.
 */

public class NonServiceHealthStatus extends AbstractHealthStatus {
    private String rawTemplate = "</td><td class=\"statusData\">%s</td><td bgcolor=\"%s\" title=\"%s\" class=\"statusData\">%s</td>";

    @Override
    public String generateHtml() {
        String date = getDateString();
        String description = getDescription();

        CheckResult.Result res = lastCheck.getResult();

        return String.format(rawTemplate, date, res.getColor(), description, res.toString());
    }
}
