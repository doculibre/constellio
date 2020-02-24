package com.constellio.data.dao.services.sql;

import com.constellio.data.conf.DataLayerConfiguration;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlServerConnector implements SqlConnector {

	private Connection connection;

	@Override
	public void setConnection(DataLayerConfiguration dataLayerConfiguration) throws SQLException {

		SQLServerDriver driver = new SQLServerDriver();

		String conUrl = dataLayerConfiguration.getMicrosoftSqlServerUrl()
						+ "; databaseName=" + dataLayerConfiguration.getMicrosoftSqlServerDatabase()
						+ "; user=" + dataLayerConfiguration.getMicrosoftSqlServeruser()
						+ "; password=" + dataLayerConfiguration.getMicrosoftSqlServerpassword()
						+ "; encrypt=" + dataLayerConfiguration.getMicrosoftSqlServerencrypt()
						+ "; loginTimeout=" + dataLayerConfiguration.getMicrosoftSqlServerloginTimeout()
						+ "; trustServerCertificate=" + dataLayerConfiguration.getMicrosoftSqlServertrustServerCertificate()
						+ ";";

		this.connection = DriverManager.getConnection(conUrl);

	}

	@Override
	public Connection getConnection() {
		return this.connection;
	}

	@Override
	public void closeConnection() throws SQLException {
		if (this.connection != null && !this.connection.isClosed()) {
			this.connection.close();
		}
	}

	@Override
	public boolean hasConnection() {
		if (this.connection == null) {
			return false;
		} else {
			try {
				return this.connection.isValid(30);
			} catch (SQLException e) {
				return false;
			}
		}
	}
}
