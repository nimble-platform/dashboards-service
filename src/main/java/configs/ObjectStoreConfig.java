package configs;

/**
 * Created by evgeniyh on 3/13/18.
 */

public class ObjectStoreConfig {
    private final String objectStoreName;
    private final String envCredentials;
    private final String containerName;
    private final String fileName;

    public ObjectStoreConfig(String objectStoreName, String envCredentials, String containerName, String fileName) {
        this.objectStoreName = objectStoreName;
        this.envCredentials = envCredentials;
        this.containerName = containerName;
        this.fileName = fileName;
    }

    public String getObjectStoreName() {
        return objectStoreName;
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
