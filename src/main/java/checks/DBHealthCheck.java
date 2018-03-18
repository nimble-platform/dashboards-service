package checks;

import common.Common;
import configs.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;


/**
 * Created by evgeniyh on 2/8/18.
 */

public class DBHealthCheck extends AbstractHealthChecker {
    private final DatabaseConfig dbcConfig;

    private String connectionUrl;

    public DBHealthCheck(DatabaseConfig dbcConfig) {
        super(dbcConfig.getName());
        this.dbcConfig = dbcConfig;
    }

    @Override
    protected void initSpecific() throws Exception {
        Class.forName(dbcConfig.getDriverName()); // Check that the driver is ok

        String user = System.getenv(dbcConfig.getEnvUsername());
        String password = System.getenv(dbcConfig.getEnvPassword());
        String url = System.getenv(dbcConfig.getEnvUrl());

        if (Common.isNullOrEmpty(user) || Common.isNullOrEmpty(url) || Common.isNullOrEmpty(password)) {
            throw new Exception("Credential values can't be null or empty");
        }

        this.connectionUrl = String.format("jdbc:postgresql://%s?user=%s&password=%s", url, user, password);
    }

    @Override
    protected CheckResult runSpecificCheck() throws Exception {
        try (Connection connection = DriverManager.getConnection(connectionUrl)) {
            return (connection.isValid(5)) ?
                    new CheckResult(CheckResult.Result.GOOD, null) :
                    new CheckResult(CheckResult.Result.BAD, "Failed to test connection to the database");
        }
    }
}
