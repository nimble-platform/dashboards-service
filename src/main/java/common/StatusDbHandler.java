package common;

import connector.DBManager;
import connector.ManagerConfig;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by evgeniyh on 6/25/18.
 */

public class StatusDbHandler extends DBManager {
    private final static Logger logger = Logger.getLogger(StatusDbHandler.class);

    private final String addIncidentQuery;
    private final String accidentsTableName;

    public StatusDbHandler(ManagerConfig config, String accidentsTableName) throws SQLException, ClassNotFoundException {
        super(config);
        this.accidentsTableName = accidentsTableName;
        addIncidentQuery = String.format("INSERT INTO %s VALUES (?,?,?);", accidentsTableName);
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

    public List<Incident> getAllIncidents() {
        List<Incident> incidents = new LinkedList<>();
        try {
            ResultSet rs = readAllTable(accidentsTableName);
            Incident i;
            while (rs.next()) {
                i = new Incident(rs.getLong(1), rs.getString(2), rs.getString(3));
                incidents.add(i);
            }
        } catch (SQLException e) {
            logger.error("Error during read of the incidents - " + e);
        }
        return incidents;
    }
}
