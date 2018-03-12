package configs;

/**
 * Created by evgeniyh on 2/19/18.
 */

public class MessageHubConfig {
    private final String envCredentials;
    private final String testTopic;

    public MessageHubConfig(String envCredentials, String testTopic) {
        this.envCredentials = envCredentials;
        this.testTopic = testTopic;
    }

    public String getEnvCredentials() {
        return envCredentials;
    }

    public String getTestTopic() {
        return testTopic;
    }
}
