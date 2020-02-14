package com.constellio.data.conf;

import java.io.File;
import java.util.Map;

public class PropertiesSqlDataLayerConfiguration extends PropertiesConfiguration {


	public static final String RECORD_TYPE = "dao.records.type";

	private boolean backgroundThreadsEnable = true;

	public PropertiesSqlDataLayerConfiguration(Map<String, String> configs, File constellioProperties) {
		super(configs, constellioProperties);
	}

	public String getSqlServerUrl() {
		return getRequiredString("sql.server.url");
	}

	public String getSqlServerDatabase() {
		return getRequiredString("sql.server.database");
	}

	public String getSqlServerUser() {
		return getRequiredString("sql.server.user");
	}

	public String getSqlServerPassword() {
		return getRequiredString("sql.server.password");
	}

	public Boolean getSqlServerEncrypt() {
		return getRequiredBoolean("sql.server.encrypt");
	}

	public Boolean getSqlServerTrustServerCertificate() {
		return getRequiredBoolean("sql.server.trustServerCertificate");
	}

	public Integer getSqlServerLoginTimeout() {
		return getRequiredInt("sql.server.loginTimeout");
	}

}
