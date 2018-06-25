package configs;

import java.util.List;

/**
 * Created by evgeniyh on 3/18/18.
 */

public class EurekaConfig extends BasicConfig {
    private final String url;
    private final List<String> services;

    public EurekaConfig(String name, String url, List<String> services) {
        super(name);
        this.url = url;
        this.services = services;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getServices() {
        return services;
    }
}
