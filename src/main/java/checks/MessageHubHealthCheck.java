package checks;

import com.google.gson.Gson;
import common.Common;
import configs.MessageHubConfig;
import configs.MessageHubCredentials;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by evgeniyh on 2/19/18.
 */

public class MessageHubHealthCheck extends AbstractHealthChecker {
    private final static Logger logger = Logger.getLogger(MessageHubHealthCheck.class);

    private final String CONSUMER_FILE = "consumer.properties";
    private final String PRODUCER_FILE = "producer.properties";
    private final String JAAS_TEMPLATE_FILE = "jaas.conf.template";
    private final String JAAS_TARGET_FILE = System.getProperty("java.io.tmpdir") + File.separator + "jaas.conf";

    private final String JAAS_CONFIG_PROPERTY = "java.security.auth.login.config";


    private final MessageHubConfig messageHubConfig;
    private String healthTopic;

    private Properties consumerProperties;
    private Properties producerProperties;

    public MessageHubHealthCheck(MessageHubConfig messageHubConfig) {
        super(messageHubConfig.getName());
        this.messageHubConfig = messageHubConfig;

    }

    @Override
    protected void initSpecific() throws Exception {
        String envCredentials = System.getenv(messageHubConfig.getEnvCredentials());
        healthTopic = messageHubConfig.getTestTopic();

        if (Common.isNullOrEmpty(healthTopic)) {
            throw new Exception("Health topic can't be null or empty");
        }
        if (Common.isNullOrEmpty(envCredentials)) {
            throw new Exception("Missing message hub credentials in the environment");
        }
        if (Common.isNullOrEmpty(messageHubConfig.getConsumerGroupId())) {
            throw new Exception("Consumer group id can't be null or empty");
        }

        consumerProperties = loadProperties(CONSUMER_FILE);
        consumerProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, messageHubConfig.getConsumerGroupId());
        producerProperties = loadProperties(PRODUCER_FILE);

        InputStream template = MessageHubHealthCheck.class.getClassLoader().getResourceAsStream(JAAS_TEMPLATE_FILE);
        String jaasTemplate = new BufferedReader(new InputStreamReader(template)).lines().parallel().collect(Collectors.joining("\n"));

        System.setProperty(JAAS_CONFIG_PROPERTY, JAAS_TARGET_FILE);

        MessageHubCredentials credentials = (new Gson()).fromJson(envCredentials, MessageHubCredentials.class);

        try (OutputStream jaasOutStream = new FileOutputStream(JAAS_TARGET_FILE, false)) {
            String fileContents = jaasTemplate
                    .replace("$USERNAME", credentials.getUser())
                    .replace("$PASSWORD", credentials.getPassword());

            jaasOutStream.write(fileContents.getBytes(Charset.forName("UTF-8")));
            logger.info("Successfully updated and set the JAAS credentials");
        } catch (Throwable t) {
            logger.error("Failed to set JAAS access credentials", t);
            throw t;
        }
    }

    @Override
    protected CheckResult runSpecificCheck() throws Exception {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);
             KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties)) {

            logger.info("Subscribing to - " + healthTopic);
            consumer.subscribe(new HashSet<String>() {{
                add(healthTopic);
            }});
            waitUntilMessageReceived(consumer, producer);

            //TODO: maybe add timeout for the check

            return new CheckResult(CheckResult.Result.GOOD, null);
        }
    }

    private static boolean messageReceived;

    private void waitUntilMessageReceived(KafkaConsumer<String, String> consumer, KafkaProducer<String, String> producer) {
        final Object sync = new Object();
        messageReceived = false;
        String sentMessage = UUID.randomUUID().toString();

        Thread t = new Thread(() -> {
            while (true) {
                try {
                    synchronized (sync) {
                        ConsumerRecords<String, String> records = consumer.poll(250);
                        logger.info(String.format("Polled %d records from the topic", records.count()));

                        boolean found = false;
                        for (ConsumerRecord<String, String> record : records) {
                            if (record.value().equals(sentMessage)) {
                                found = true;
                            }
                        }
                        if (found) {
                            messageReceived = true;
                            sync.notify();
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("Exception during wait for the message to be received");
                }
            }
        });
        t.start();

        logger.info(String.format("Sending %s to topic %s", sentMessage, healthTopic));
        producer.send(new ProducerRecord<>(healthTopic, sentMessage));

        while (true) {
            synchronized (sync) {
                try {
                    if (messageReceived) {
                        logger.info("The message was received - returning");
                        break;
                    }
                    logger.info("Message still hasn't been received - going to sleep");
                    sync.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Exception during wait for message - " + e.getMessage());
                }
            }
        }
    }

    private Properties loadProperties(String file) throws Exception {
        try (InputStream inputStream = MessageHubHealthCheck.class.getClassLoader().getResourceAsStream(file)) {

            Properties prop = new Properties();
            prop.load(inputStream);
            return prop;
        } catch (Throwable e) {
            logger.error("Failed to load properties for - " + file);
            e.printStackTrace();
            throw e;
        }
    }
}
