package configs;

import com.google.gson.annotations.SerializedName;

/**
 * Created by evgeniyh on 2/19/18.
 */

public class MessageHubCredentials {
    private final String user;
    private final String password;

    @SerializedName("mqlight_lookup_url")
    private final String lookupUrl;

    @SerializedName("instance_id")
    private final String instanceId;

    @SerializedName("api_key")
    private final String apiKey;

    @SerializedName("kafka_admin_url")
    private final String adminUrl;

    @SerializedName("kafka_brokers_sasl")
    private final String[] brokers;

    @SerializedName("kafka_rest_url")
    private final String restUrl;

    public MessageHubCredentials(String instanceId, String lookupUrl, String apiKey, String adminUrl, String restUrl, String user, String password, String[] brokers) {
        this.instanceId = instanceId;
        this.lookupUrl = lookupUrl;
        this.apiKey = apiKey;
        this.adminUrl = adminUrl;
        this.restUrl = restUrl;
        this.user = user;
        this.password = password;
        this.brokers = brokers;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getLookupUrl() {
        return lookupUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getAdminUrl() {
        return adminUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String[] getBrokers() {
        return brokers;
    }

    public String getRestUrl() {
        return restUrl;
    }
}


