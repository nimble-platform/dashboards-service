package configs;

/**
 * Created by evgeniyh on 3/13/18.
 */

public class ObjectStoreConfig implements ServiceConfig {
    private final String name;
    private final String envCredentials;
    private final String containerName;
    private final String fileName;

    public ObjectStoreConfig(String name, String envCredentials, String containerName, String fileName) {
        this.name = name;
        this.envCredentials = envCredentials;
        this.containerName = containerName;
        this.fileName = fileName;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getEnvCredentials() {
        return envCredentials;
    }

    public String getContainerName() {
        return containerName;
    }

    public String getFileName() {
        return fileName;
    }
}
