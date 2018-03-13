package configs;

/**
 * Created by evgeniyh on 2/19/18.
 */

public class MessageHubConfig {
    private final String messageHubName;
    private final String envCredentials;
    private final String testTopic;

    public MessageHubConfig(String messageHubName, String envCredentials, String testTopic) {
        this.messageHubName = messageHubName;
        this.envCredentials = envCredentials;
        this.testTopic = testTopic;
    }

    public String getMessageHubName() {
        return messageHubName;
    }

    public String getEnvCredentials() {
        return envCredentials;
    }

    public String getTestTopic() {
        return testTopic;
    }
}
