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

public class BasicHealthChecker extends AbstractHealthChecker {
    private final static Logger logger = Logger.getLogger(BasicHealthChecker.class);

    private final String healthUrl;

    public BasicHealthChecker(String serviceName, String healthUrl) {
        super(serviceName);
        this.healthUrl = healthUrl;
    }

    @Override
    protected void initSpecific() throws Exception {
        if (isNullOrEmpty(healthUrl)) {
            throw new Exception("Health url cannot be empty");
        }
    }

    @Override
    protected CheckResult runSpecificCheck() throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet getHealth = new HttpGet(healthUrl);
            CloseableHttpResponse response = httpclient.execute(getHealth);

            if (response == null) {
                throw new RuntimeException("http response was null for - " + healthUrl);
            }
            String res = Common.inputStreamToString(response.getEntity().getContent());
            logger.info("For - " + healthUrl + " response is " + res);

            return CheckResult.createResult(response.getStatusLine().getStatusCode(), res, "OK");
        }
    }


//    @Override
//    public CheckResult runCheck() {
//
//    }
    // Untrusted connection (without tls)

//    SSLContextBuilder builder = new SSLContextBuilder();
//
//            builder.loadTrustMaterial(null, (TrustStrategy) (x509Certificates, s) -> true);
//
//    SSLConnectionSocketFactory notTrusted = new SSLConnectionSocketFactory(builder.build());
//    CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(notTrusted).build();
}
