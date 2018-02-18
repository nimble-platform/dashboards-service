package configs;

/**
 * Created by evgeniyh on 2/17/18.
 */

public class DatabaseConfig {
    private final String name;
    private final String envUsername;
    private final String envPassword;
    private final String envUrl;
    private final String driverName;

    public DatabaseConfig(String name, String envUsername, String envPassword, String envUrl, String driverName) {
        this.name = name;
        this.envUsername = envUsername;
        this.envPassword = envPassword;
        this.envUrl = envUrl;
        this.driverName = driverName;
    }

    public String getName() {
        return name;
    }

    public String getEnvUsername() {
        return envUsername;
    }

    public String getEnvPassword() {
        return envPassword;
    }

    public String getEnvUrl() {
        return envUrl;
    }

    public String getDriverName() {
        return driverName;
    }
}
