package configs;

import java.util.List;

/**
 * Created by evgeniyh on 3/18/18.
 */

public class EurekaConfig implements ServiceConfig {
    private final String name;
    private final String url;
    private final List<String> services;

    public EurekaConfig(String name, String url, List<String> services) {
        this.name = name;
        this.url = url;
        this.services = services;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getServices() {
        return services;
    }
}
