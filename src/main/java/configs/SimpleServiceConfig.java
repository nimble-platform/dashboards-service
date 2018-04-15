package configs;

/**
 * Created by evgeniyh on 2/17/18.
 */


public class SimpleServiceConfig implements ServiceConfig {
    private final String name;
    private final String url;
    private final String k8sName;

    public SimpleServiceConfig(String name, String url, String k8sName) {
        this.name = name;
        this.url = url;
        this.k8sName = k8sName;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getK8sName() {
        return k8sName;
    }
}