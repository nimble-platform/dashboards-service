package checks;

/**
 * Created by evgeniyh on 2/8/18.
 */

public class DBVariables {
    private final String driverName;
    private final String envUsername;
    private final String envUrl;
    private final String envPassword;

    public DBVariables(String driverName, String envUsername, String envUrl, String envPassword) {
        this.driverName = driverName;
        this.envUsername = envUsername;
        this.envUrl = envUrl;
        this.envPassword = envPassword;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getEnvUsername() {
        return envUsername;
    }

    public String getEnvUrl() {
        return envUrl;
    }

    public String getEnvPassword() {
        return envPassword;
    }
}
