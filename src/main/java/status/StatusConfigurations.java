package status;

import java.util.List;

/**
 * Created by evgeniyh on 1/17/18.
 */

public class StatusConfigurations {
    private final int frequency;
    private final List<String> services;

    public StatusConfigurations(int frequency, List<String> services) {
        this.frequency = frequency;
        this.services = services;
    }


    public int getFrequency() {
        return frequency;
    }

    public List<String> getServices() {
        return services;
    }
}
