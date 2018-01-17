package status;

class CheckResult {
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

    enum Result {
        GOOD("green"),
        BAD("red");

        private String htmlColor;

        Result(String htmlColor) {

            this.htmlColor = htmlColor;
        }

        String getColor() {
            return htmlColor;
        }
    }
}
