/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.conf;

import java.io.File;
import java.util.Map;

import org.joda.time.Duration;

public class PropertiesDataLayerConfiguration extends PropertiesConfiguration implements DataLayerConfiguration {

	private File defaultTempFolder;

	private File defaultFileSystemBaseFolder;

	public PropertiesDataLayerConfiguration(Map<String, String> configs, File defaultTempFolder,
			File defaultFileSystemBaseFolder) {
		super(configs);
		this.defaultTempFolder = defaultTempFolder;
		this.defaultFileSystemBaseFolder = defaultFileSystemBaseFolder;
	}

	public SolrServerType getRecordsDaoSolrServerType() {
		return (SolrServerType) getRequiredEnum("dao.records.type", SolrServerType.class);
	}

	public String getRecordsDaoHttpSolrServerUrl() {
		return getRequiredString("dao.records.http.url");
	}

	public String getRecordsDaoCloudSolrServerZKHost() {
		return getRequiredString("dao.records.cloud.zkHost");
	}

	public boolean isRecordsDaoHttpSolrServerFaultInjectionEnabled() {
		return getBoolean("dao.records.http.faultInjection", false);
	}

	public ContentDaoType getContentDaoType() {
		return (ContentDaoType) getRequiredEnum("dao.contents.type", ContentDaoType.class);
	}

	public String getContentDaoHadoopUrl() {
		return getRequiredString("dao.contents.server.address");
	}

	public String getContentDaoHadoopUser() {
		return getRequiredString("dao.contents.server.user");
	}

	public File getContentDaoFileSystemFolder() {
		return getRequiredFile("dao.contents.filesystem.folder");
	}

	public String getSettingsZookeeperAddress() {
		return getRequiredString("dao.settings.server.address");
	}

	public File getTempFolder() {
		return getFile("tempFolder", defaultTempFolder);
	}

	public IdGeneratorType getIdGeneratorType() {
		return (IdGeneratorType) getEnum("idGenerator.type", IdGeneratorType.SEQUENTIAL);
	}

	@Override
	public boolean isSecondTransactionLogEnabled() {
		return getBoolean("secondTransactionLog.enabled", false);
	}

	@Override
	public File getSecondTransactionLogBaseFolder() {
		return getRequiredFile("secondTransactionLog.folder");
	}

	public ConfigManagerType getSettingsConfigType() {
		return (ConfigManagerType) getRequiredEnum("dao.settings.type", ConfigManagerType.class);
	}

	public File getSettingsFileSystemBaseFolder() {
		return getFile("dao.settings.filesystem.baseFolder", defaultFileSystemBaseFolder);
	}

	@Override
	public void validate() {

	}

	@Override
	public int getBackgroudThreadsPoolSize() {
		return 2;
	}

	@Override
	public Duration getSecondTransactionLogMergeFrequency() {
		return Duration.standardMinutes(15);
	}

	@Override
	public int getSecondTransactionLogBackupCount() {
		return 2;
	}

}