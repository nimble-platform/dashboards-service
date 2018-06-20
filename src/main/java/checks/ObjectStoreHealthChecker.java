package checks;

import com.google.gson.Gson;
import common.Common;
import configs.ObjectStoreConfig;
import configs.ObjectStoreCredentials;
import org.apache.log4j.Logger;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.storage.ObjectStorageService;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.common.Payload;
import org.openstack4j.model.storage.object.SwiftObject;
import org.openstack4j.openstack.OSFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import static common.Common.isNullOrEmpty;

/**
 * Created by evgeniyh on 3/13/18.
 */

public class ObjectStoreHealthChecker extends AbstractHealthChecker {
    private final static Logger logger = Logger.getLogger(ObjectStoreHealthChecker.class);

    private ObjectStoreConfig objectStoreConfig;

    private String filename;
    private String containerName;

    private String username;
    private String password;
    private String projectId;
    private String authUrl;
    private Identifier domainIdentifier;

    public ObjectStoreHealthChecker(ObjectStoreConfig objectStoreConfig) {
        super(objectStoreConfig.getName());
        this.objectStoreConfig = objectStoreConfig;
    }

    @Override
    protected void initSpecific() throws Exception {
        logger.info("Loading credentials from the environment");

        filename = objectStoreConfig.getFileName();
        containerName = objectStoreConfig.getContainerName();

        if (isNullOrEmpty(filename) || isNullOrEmpty(containerName)) {
            throw new Exception("Filename and container can't be empty");
        }

        String credentialsJson = System.getenv(objectStoreConfig.getEnvCredentials());
        if (isNullOrEmpty(credentialsJson)) {
            throw new Exception("Missing the message hub credentials");
        }
        ObjectStoreCredentials credentials = (new Gson()).fromJson(credentialsJson, ObjectStoreCredentials.class);

        username = credentials.getUsername();
        password = credentials.getPassword();
        projectId = credentials.getProjectId();
        authUrl = credentials.getAuth_url() + "/v3";

        String domainId = credentials.getDomainId();
        domainIdentifier = Identifier.byId(domainId);

        if (isNullOrEmpty(username) || isNullOrEmpty(password) || isNullOrEmpty(domainId) || isNullOrEmpty(projectId) || isNullOrEmpty(authUrl)) {
            throw new Exception("Credentials values can't be empty");
        }

        logger.info("Authenticating against - " + authUrl);
    }

    private ObjectStorageService getObjectStorage() {
        OSClient.OSClientV3 os = OSFactory.builderV3()
                .endpoint(authUrl)
                .credentials(username, password, domainIdentifier)
                .scopeToProject(Identifier.byId(projectId))
                .authenticate();
        logger.info("Creating and authenticating OS client was successful!");

        return os.objectStorage();
    }

    @Override
    protected CheckResult runSpecificCheck() throws Exception {
        ObjectStorageService objectStorage = getObjectStorage();

        String randomUid = UUID.randomUUID().toString();
        InputStream stream = new ByteArrayInputStream(randomUid.getBytes(Charset.forName("UTF-8")));

        Payload<InputStream> payload = new PayloadImpl(stream);
        objectStorage.objects().put(containerName, filename, payload);
        logger.info("Successfully stored the file inside the container - will now retrieve it");

        SwiftObject fileObj = objectStorage.objects().get(containerName, filename);

        if (fileObj == null) { //The specified file was not found
            logger.error("Failed to retrieve the file");
            throw new Exception("Failed to retrieve the file");
        }

        String fileContent;
        try (InputStream in = fileObj.download().getInputStream()) {
            fileContent = Common.inputStreamToString(in);
            logger.info("loaded content " + fileContent);
        }

        if (fileContent == null || !fileContent.equals(randomUid)) {
            throw new Exception("The file content is null or the stored data doesn't match the retrieved");
        }

        logger.info("Successfully retrieved the file - will delete it now");

        ActionResponse deleteResponse = objectStorage.objects().delete(containerName, filename);

        if (!deleteResponse.isSuccess()) {
            throw new Exception("Failed to delete the file");
        }
        logger.info("Successfully deleted file from ObjectStorage!");

        return new CheckResult(CheckResult.Result.GOOD, null);
    }

    private class PayloadImpl implements Payload<InputStream> {
        private final InputStream stream;

        PayloadImpl(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public InputStream open() {
            return stream;
        }

        @Override
        public void closeQuietly() {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }

        @Override
        public InputStream getRaw() {
            return stream;
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }
}
