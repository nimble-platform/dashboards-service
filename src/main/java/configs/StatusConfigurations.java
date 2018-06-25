package configs;

import java.util.List;

/**
 * Created by evgeniyh on 1/17/18.
 */

public class StatusConfigurations {
    private final int frequency;
    private final List<SimpleServiceConfig> services;
    private final List<DatabaseConfig> databases;
    private final MessageHubConfig messageHub;
    private final ObjectStoreConfig objectStore;
    private final EurekaConfig eureka;
    private final IncidentsDbConfig incidentsDbConfig;

    public StatusConfigurations(int frequency,
                                List<SimpleServiceConfig> services,
                                List<DatabaseConfig> databases,
                                MessageHubConfig messageHub,
                                ObjectStoreConfig objectStore,
                                EurekaConfig eureka,
                                IncidentsDbConfig incidentsDbConfig) {
        this.frequency = frequency;
        this.services = services;
        this.databases = databases;
        this.messageHub = messageHub;
        this.objectStore = objectStore;
        this.eureka = eureka;
        this.incidentsDbConfig = incidentsDbConfig;
    }

    public int getFrequency() {
        return frequency;
    }

    public List<SimpleServiceConfig> getServices() {
        return services;
    }

    public List<DatabaseConfig> getDatabases() {
        return databases;
    }

    public MessageHubConfig getMessageHub() {
        return messageHub;
    }

    public ObjectStoreConfig getObjectStore() {
        return objectStore;
    }

    public EurekaConfig getEureka() {
        return eureka;
    }

    public IncidentsDbConfig getIncidentsDbConfig() {
        return incidentsDbConfig;
    }
}
