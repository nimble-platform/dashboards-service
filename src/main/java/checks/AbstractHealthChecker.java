package checks;

import org.apache.log4j.Logger;

/**
 * Created by evgeniyh on 3/13/18.
 */

public abstract class AbstractHealthChecker implements HealthChecker {
    private final static Logger logger = Logger.getLogger(AbstractHealthChecker.class);

    private String serviceName;
    private boolean isInitialized;

    AbstractHealthChecker(String serviceName) {
        this.serviceName = serviceName;
        if (serviceName == null || serviceName.isEmpty()) {
            logger.error("Service name can't be null or empty");
            throw new IllegalArgumentException("Service name can't be null or empty");
        }
    }

    @Override
    public CheckResult runCheck() {
        if (!isInitialized) {
            return new CheckResult(CheckResult.Result.BAD, "Failed to initialize service - " + serviceName);
        }
        try {
            logger.info("Running specific health check for - " + serviceName);

            CheckResult c = runSpecificCheck();
            CheckResult.logResult(serviceName, c);

            return c;
        } catch (Throwable e) {
            e.printStackTrace();
            logger.error("Exception during health check of service " + serviceName, e);
            Throwable t = e.getCause();
            String exceptionMessage = (t == null) ? e.getMessage() : t.getMessage();
            return new CheckResult(CheckResult.Result.BAD, "Exception - " + exceptionMessage);
        }
    }

    @Override
    public void init() throws Exception {
        try {
            initSpecific();
            isInitialized = true;
        } catch (Exception e) {
            logger.error("Error during init - ", e);
            isInitialized = false;
        }
    }

    protected abstract void initSpecific() throws Exception;

    protected abstract CheckResult runSpecificCheck() throws Exception;
}
