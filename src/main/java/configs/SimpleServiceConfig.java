package configs;

/**
 * Created by evgeniyh on 2/17/18.
 */


public class SimpleServiceConfig extends BasicConfig {
    private final String url;
    private final String k8sName;

    public SimpleServiceConfig(String name, String url, String k8sName) {
        super(name);
        this.url = url;
        this.k8sName = k8sName;
    }

    public String getUrl() {
        return url;
    }

    public String getK8sName() {
        return k8sName;
    }
}