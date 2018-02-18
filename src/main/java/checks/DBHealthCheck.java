package checks;

import common.Common;
import configs.DatabaseConfig;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Created by evgeniyh on 2/8/18.
 */

public class DBHealthCheck implements HealthChecker {
    private final static Logger logger = Logger.getLogger(DBHealthCheck.class);

    private String connectionUrl;

    public DBHealthCheck(DatabaseConfig dbc) {
        try {
            Class.forName(dbc.getDriverName()); // Check that the driver is ok

            String user = System.getenv(dbc.getEnvUsername());
            String password = System.getenv(dbc.getEnvPassword());
            String url = System.getenv(dbc.getEnvUrl());

            if (Common.isNullOrEmpty(user) || Common.isNullOrEmpty(url) || Common.isNullOrEmpty(password)) {
                throw new Exception("Credential values can't be null or empty");
            }

            this.connectionUrl = String.format("jdbc:postgresql://%s?user=%s&password=%s", url, user, password);
        } catch (Throwable e) {
            e.printStackTrace();
            this.connectionUrl = null;
        }
    }

    @Override
    public CheckResult runCheck() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(connectionUrl);

            return (connection.isValid(5)) ?
                    new CheckResult(CheckResult.Result.GOOD, null) :
                    new CheckResult(CheckResult.Result.BAD, "Failed to test connection to the database");

        } catch (Exception e) {
            logger.error("Failed to connect to the db", e);
            return new CheckResult(CheckResult.Result.BAD, e.toString());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
