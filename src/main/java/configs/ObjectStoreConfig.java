package configs;

/**
 * Created by evgeniyh on 3/13/18.
 */

public class ObjectStoreConfig extends BasicConfig {
    private final String envCredentials;
    private final String containerName;
    private final String fileName;

    public ObjectStoreConfig(String name, String envCredentials, String containerName, String fileName) {
        super(name);
        this.envCredentials = envCredentials;
        this.containerName = containerName;
        this.fileName = fileName;
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
