package checks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.Common;
import configs.EurekaConfig;
import status.HealthStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by evgeniyh on 3/18/18.
 */

public class EurekaHealthCheck extends AbstractHealthChecker {
    private final List<String> services;
    private final String url;
    private final Map<String, HealthStatus> serviceToResult = new HashMap<>();

    public EurekaHealthCheck(EurekaConfig config) {
        super(config.getName());

        url = config.getUrl();
        services = config.getServices();
    }

    @Override
    protected void initSpecific() throws Exception {
        if (Common.isNullOrEmpty(url)) {
            throw new Exception("Eureka URL can't be null or empty");
        }
        if (services == null || services.size() == 0) {
            throw new Exception("Services can't be null or empty list");
        }
        for (String s : services) {
            serviceToResult.put(s, new HealthStatus());
        }
    }

    @Override
    protected CheckResult runSpecificCheck() throws Exception {
        String res = Common.executeHttpGet(url, false, true);

        JsonObject jsonObject = (JsonObject) new JsonParser().parse(res);

        if (!jsonObject.get("status").getAsString().equals("UP")) {
            throw new Exception("The status of the response isn't up");
        }

        JsonObject eurekaObject = jsonObject.getAsJsonObject("discoveryComposite").getAsJsonObject("eureka");

        if (!eurekaObject.get("status").getAsString().equals("UP")) {
            throw new Exception("The status of Eureka isn't up");
        }

        JsonObject registeredServices = eurekaObject.getAsJsonObject("applications");

        synchronized (serviceToResult) {
            for (String service : services) {
                JsonElement serviceElement = registeredServices.get(service);
                String errorMessage = null;
                if (serviceElement == null) {
                    errorMessage = "Missing from registered services list";
                } else if (!isServiceUp(serviceElement.getAsInt())) {
                    // the service is down
                    errorMessage = "The service seems to be down";
                }
                if (errorMessage != null) {
                    serviceToResult.get(service).updateLastCheck(new CheckResult(CheckResult.Result.BAD, errorMessage));
                } else {
                    serviceToResult.get(service).updateLastCheck(new CheckResult(CheckResult.Result.GOOD, null));
                }
            }
        }
        return new CheckResult(CheckResult.Result.GOOD, null);
    }

    public Map<String, HealthStatus> getRegisteredServicesResults() {
        synchronized (serviceToResult) {
            return new HashMap<>(serviceToResult);
        }
    }

    private boolean isServiceUp(int asInt) {
        return (asInt == 1);
    }
}
