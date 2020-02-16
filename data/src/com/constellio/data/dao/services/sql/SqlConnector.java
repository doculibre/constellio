package com.constellio.data.dao.services.sql;

import com.constellio.data.conf.DataLayerConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlConnector {

	void setConnection(DataLayerConfiguration dataLayerConfiguration) throws SQLException;

	Connection getConnection();

	void closeConnection() throws SQLException;

	boolean hasConnection();
}
