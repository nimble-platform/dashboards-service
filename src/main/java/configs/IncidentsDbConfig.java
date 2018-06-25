package configs;

/**
 * Created by evgeniyh on 6/25/18.
 */

public class IncidentsDbConfig extends DatabaseConfig {
    private final String tableName;

    public IncidentsDbConfig(String name, String envUsername, String envPassword, String envUrl, String driverName, String tableName) {
        super(name, envUsername, envPassword, envUrl, driverName);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
