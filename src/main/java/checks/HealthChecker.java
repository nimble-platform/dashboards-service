package checks;

public interface HealthChecker {
    void init() throws Exception;
    CheckResult runCheck();
}
