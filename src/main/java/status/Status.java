package status;

public enum Status {
    GOOD("green"),
    BAD("red");

    private String htmlColor;

    Status(String htmlColor) {

        this.htmlColor = htmlColor;
    }

    String getColor() {
        return htmlColor;
    }
}
