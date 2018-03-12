package checks;

import static common.Common.isNullOrEmpty;

import common.Common;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.log4j.Logger;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by evgeniyh on 2/8/18.
 */

public class BasicHealthChecker implements HealthChecker {
    private final static Logger logger = Logger.getLogger(BasicHealthChecker.class);

    private final String healthUrl;
    private final String serviceName;

    public BasicHealthChecker(String serviceName, String healthUrl) {
        if (isNullOrEmpty(serviceName) || isNullOrEmpty(healthUrl)) {
            throw new NullPointerException("Service name and health url cannot be empty");
        }

        this.serviceName = serviceName;
        this.healthUrl = healthUrl;
    }

    @Override
    public CheckResult runCheck() {
        logger.info("Running health check for - " + serviceName);

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet getHealth = new HttpGet(healthUrl);
            CloseableHttpResponse response = httpclient.execute(getHealth);

            if (response == null) {
                throw new RuntimeException("http response was null for - " + healthUrl);
            }
            String res = Common.inputStreamToString(response.getEntity().getContent());
            logger.info("For - " + healthUrl + " response is " + res);

            CheckResult result = CheckResult.createResult(response.getStatusLine().getStatusCode(), res, "OK");
            CheckResult.logResult(serviceName, result);

            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            logger.error("Exception during health check of service " + serviceName, e);
            Throwable t = e.getCause();
            String exceptionMessage = (t == null) ? e.getMessage() : t.getMessage();
            return new CheckResult(CheckResult.Result.BAD, "Exception - " + exceptionMessage);
        }
    }
    // Untrusted connection (without tls)

//    SSLContextBuilder builder = new SSLContextBuilder();
//
//            builder.loadTrustMaterial(null, (TrustStrategy) (x509Certificates, s) -> true);
//
//    SSLConnectionSocketFactory notTrusted = new SSLConnectionSocketFactory(builder.build());
//    CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(notTrusted).build();
}
