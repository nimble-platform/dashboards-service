package checks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.Common;
import configs.EurekaConfig;

import java.util.List;

/**
 * Created by evgeniyh on 3/18/18.
 */

public class EurekaHealthCheck extends AbstractHealthChecker {
    private final List<String> services;
    private final String url;

    public EurekaHealthCheck(EurekaConfig config) {
        super(config.getName());

        url = config.getUrl();
        services = config.getServices();
    }

    @Override
    protected void initSpecific() throws Exception {
        if (services == null || services.size() == 0) {
            throw new Exception("Services can't be null or empty list");
        }
        if (Common.isNullOrEmpty(url)) {
            throw new Exception("Eureka URL can't be null or empty");
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
        StringBuilder error = new StringBuilder();

        for (String service : services) {
            JsonElement serviceElement = registeredServices.get(service);
            if (serviceElement == null) {
                error.append(service).append(" is missing from registered \n");
                continue;
            }
            if (!isServiceUp(serviceElement.getAsInt())) {
                // the service is down
                error.append(service).append(" is down \n");
            }
        }
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        String json = gson.toJson(eurekaObject);
//        System.out.println(json);

        return new CheckResult(CheckResult.Result.GOOD, error.toString());
    }

    private boolean isServiceUp(int asInt) {
        return (asInt == 1);
    }
}
