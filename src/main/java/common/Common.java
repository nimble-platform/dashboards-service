package common;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.StringWriter;

/**
 * Created by evgeniyh on 1/16/18.
 */

public class Common {
    private final static Logger logger = Logger.getLogger(Common.class);

    public static String inputStreamToString(InputStream stream) {
        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer, "UTF-8");
            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String executeHttpGet(String url, boolean logResponse, boolean verifyResponseOk) throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            CloseableHttpResponse response = httpclient.execute(httpGet);
            if (response == null) {
                throw new RuntimeException("http response was null for - " + url);
            }
            if (verifyResponseOk && response.getStatusLine().getStatusCode() != 200) {
                throw new Exception("Response wasn't 200");
            }
            String responseString = Common.inputStreamToString(response.getEntity().getContent());
            if (logResponse) {
                logger.info(String.format("Response for url - %s was - %s", url, responseString));
            }
            return responseString;
        } catch (Throwable t) {
            logger.error("Error during execution of GET on - " + url, t);
            throw t;
        }
    }

    public static boolean isNullOrEmpty(String s) {
        return (s == null || s.isEmpty());
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
