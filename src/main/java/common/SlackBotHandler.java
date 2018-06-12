package common;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.Consts;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import static common.Common.isNullOrEmpty;

/**
 * Created by evgeniyh on 6/4/18.
 */

public class SlackBotHandler {
    private final static Logger logger = Logger.getLogger(SlackBotHandler.class);

    private static final String POST_MESSAGE_URL = "https://slack.com/api/chat.postMessage";
    private static final String token;

    static {
        token = System.getenv("BOT_TOKEN");
        if (isNullOrEmpty(token)) {
            throw new IllegalStateException("Missing the 'BOT_TOKEN' environment variable");
        }
    }

    public static void sendMessageToChannel(String message) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(POST_MESSAGE_URL);

            post.addHeader("Content-type", "application/json; charset=UTF-8");
            post.addHeader("Authorization", "Bearer " + token);

            JsonObject body = new JsonObject();
            body.addProperty("channel", "CAV6BECQ4");
            body.addProperty("text", message);

            post.setEntity(new StringEntity(new Gson().toJson(body), Consts.UTF_8));

            CloseableHttpResponse response = httpclient.execute(post);
            String resultJson = Common.inputStreamToString(response.getEntity().getContent());
            if (isNullOrEmpty(resultJson)) {
                throw new RuntimeException("Failed to read the response of the POST of the message");
            }
            JsonObject jsonObject = (new JsonParser()).parse(resultJson).getAsJsonObject();
            if (jsonObject.get("ok").getAsBoolean()) {
                logger.info("Message was sent successfully");
            } else {
                logger.error("Failed to send the message to slack");
            }
        } catch (Exception e) {
            logger.error("Error during sending of a message to Slack", e);
        }
    }
}
