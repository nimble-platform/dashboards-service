package checks;

import common.Common;
import org.apache.log4j.Logger;

import static common.Common.isNullOrEmpty;

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
        String res = Common.executeHttpGet(healthUrl, true, true);
        return (res.equals("OK")) ?
                new CheckResult(CheckResult.Result.GOOD, null) :
                new CheckResult(CheckResult.Result.BAD, "Response wasn't ok, it was - " + res);
    }
}
