package checks;

import org.apache.log4j.Logger;
import status.StatusHandler;

public class CheckResult {
    private final static Logger logger = Logger.getLogger(CheckResult.class);

    private final Result result;
    private final String errorMessage;

    public Result getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public CheckResult(Result result, String errorMessage) {
        this.result = result;
        this.errorMessage = errorMessage;
    }

    public static void logResult(String serviceName, CheckResult result) {
        if (result.getResult().equals(CheckResult.Result.GOOD)) {
            logger.info("The health check was successful for service - " + serviceName);
        } else {
            logger.error("Failed on the health check of service - " + serviceName + " due to - " + result.getErrorMessage());
        }
    }

    public enum Result {
        GOOD("green"),
        BAD("red");

        private String htmlColor;

        Result(String htmlColor) {

            this.htmlColor = htmlColor;
        }

        public String getColor() {
            return htmlColor;
        }
    }

    public static CheckResult createResult(int statusCode, String response, String expectedResponse) {
        String errorMessage = null;
        if (response == null) {
            errorMessage = "Response was null";
        } else if (statusCode != 200) {
            errorMessage = "Status wasn't 200, it was - " + String.valueOf(statusCode);
        } else if (expectedResponse != null && !response.equals(expectedResponse)) {
            errorMessage = "Response wasn't OK, it was - " + response;
        }
        return (errorMessage == null) ?
                new CheckResult(CheckResult.Result.GOOD, null) :
                new CheckResult(CheckResult.Result.BAD, errorMessage);
    }
}
