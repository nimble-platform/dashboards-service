package configs;

import java.util.List;

/**
 * Created by evgeniyh on 1/17/18.
 */

public class StatusConfigurations {
    private final int frequency;
    private final List<ServiceConfig> services;
    private final List<DatabaseConfig> databases;

    public StatusConfigurations(int frequency, List<ServiceConfig> services, List<DatabaseConfig> databases) {
        this.frequency = frequency;
        this.services = services;
        this.databases = databases;
    }

    public int getFrequency() {
        return frequency;
    }

    public List<ServiceConfig> getServices() {
        return services;
    }

    public List<DatabaseConfig> getDatabases() {
        return databases;
    }
}
