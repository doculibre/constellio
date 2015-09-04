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
package com.constellio.model.conf;

import java.io.File;
import java.util.Map;

import org.joda.time.Duration;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.PropertiesConfiguration;

public class PropertiesModelLayerConfiguration extends PropertiesConfiguration implements ModelLayerConfiguration {

	private final DataLayerConfiguration dataLayerConfiguration;
	private final FoldersLocator foldersLocator;

	public PropertiesModelLayerConfiguration(Map<String, String> configs, DataLayerConfiguration dataLayerConfiguration,
			FoldersLocator foldersLocator, File constellioProperties) {
		super(configs, constellioProperties);
		this.dataLayerConfiguration = dataLayerConfiguration;
		this.foldersLocator = foldersLocator;
	}

	@Override
	public void validate() {

	}

	@Override
	public boolean isDocumentsParsedInForkProcess() {
		return getBoolean("parsing.useForkProcess", false);
	}

	@Override
	public File getTempFolder() {
		return dataLayerConfiguration.getTempFolder();
	}

	@Override
	public String getComputerName() {
		return "mainserver";
	}

	@Override
	public int getBatchProcessesPartSize() {
		//return getRequiredInt("batchProcess.partSize");
		return 500;
	}

	@Override
	public int getNumberOfRecordsPerTask() {
		return 100;
	}

	@Override
	public int getForkParsersPoolSize() {
		return 20;
	}

	@Override
	public File getImportationFolder() {
		return getFile("importationFolder", foldersLocator.getDefaultImportationFolder());
	}

	@Override
	public Duration getDelayBeforeDeletingUnreferencedContents() {
		return Duration.standardMinutes(10);
	}

	@Override
	public Duration getUnreferencedContentsThreadDelayBetweenChecks() {
		return Duration.standardMinutes(5);
	}

	public Duration getTokenRemovalThreadDelayBetweenChecks() {
		return Duration.standardHours(1);
	}

	@Override
	public Duration getTokenDuration() {
		return Duration.standardHours(10);
	}

	@Override
	public int getDelayBeforeSendingNotificationEmailsInMinutes() {
		return 42;
	}

	@Override
	public String getMainDataLanguage() {
		return getString("mainDataLanguage", "fr");
	}

	@Override
	public void setMainDataLanguage(String language) {
		setString("mainDataLanguage", language);

	}

}