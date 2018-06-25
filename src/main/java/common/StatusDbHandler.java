package common;

import connector.DBManager;
import connector.ManagerConfig;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by evgeniyh on 6/25/18.
 */

public class StatusDbHandler extends DBManager {
    private final static Logger logger = Logger.getLogger(StatusDbHandler.class);

    private final String tableName;
    private final String addIncidentQuery;

    public StatusDbHandler(ManagerConfig config, String tableName) throws SQLException, ClassNotFoundException {
        super(config);
        this.tableName = tableName;

        addIncidentQuery = String.format("INSERT INTO %s VALUES (?,?,?);", tableName);
    }

    public void addIncident(Incident incident) {
        logger.info("Adding incident to the db" + incident);
        try {
            PreparedStatement ps = prepareStatement(addIncidentQuery);

            ps.setLong(1, incident.getTime());
            ps.setString(2, incident.getService());
            ps.setString(3, incident.getMessage());

            executeUpdateStatement(ps, false);
        } catch (SQLException e) {
            logger.error("Error during add of an incident to the DB", e);
        }
    }
}
