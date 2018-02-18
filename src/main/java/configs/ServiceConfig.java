package configs;

/**
 * Created by evgeniyh on 2/17/18.
 */


public class ServiceConfig {
    private final String name;
    private final String url;

    public ServiceConfig(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}