package configs;

/**
 * Created by evgeniyh on 2/19/18.
 */

public class MessageHubConfig implements ServiceConfig {
    private final String name;
    private final String envCredentials;
    private final String testTopic;
    private final String consumerGroupId;

    public MessageHubConfig(String name, String envCredentials, String testTopic, String consumerGroupId) {
        this.name = name;
        this.envCredentials = envCredentials;
        this.testTopic = testTopic;
        this.consumerGroupId = consumerGroupId;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getEnvCredentials() {
        return envCredentials;
    }

    public String getTestTopic() {
        return testTopic;
    }

    public String getConsumerGroupId() {
        return consumerGroupId;
    }
}
