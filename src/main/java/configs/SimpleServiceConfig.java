package configs;

/**
 * Created by evgeniyh on 2/17/18.
 */


public class SimpleServiceConfig implements ServiceConfig {
    private final String name;
    private final String url;

    public SimpleServiceConfig(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}