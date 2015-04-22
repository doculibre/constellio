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
package com.constellio.app.services.systemSetup;

import java.util.Map;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;

public class SystemGlobalConfigsManager implements StatefulService {

	final static String SYSTEM_GLOBAL_PROPERTIES = "/globalProperties";
	final static String IS_SYSTEM_SETTED_UP = "systemSettedUp";
	final static String MAIN_DATA_LANGUAGE = "mainLanguage";
	final static String TOKEN_DURATION = "tokenDuration";
	final static String NOTIFICATION_MINUTES = "notificationMinutes";
	final static int TOKEN_DURATION_VALUE = 30;
	final static int NOTIFICATION_MINUTES_VALUE = 60;

	private final ConfigManager configManager;

	private final SystemSetupService systemSetupService;

	public SystemGlobalConfigsManager(ConfigManager configManager, SystemSetupService systemSetupService) {
		this.configManager = configManager;
		this.systemSetupService = systemSetupService;
	}

	@Override
	public void initialize() {
		configManager.createPropertiesDocumentIfInexistent(SYSTEM_GLOBAL_PROPERTIES, ConfigManager.EMPTY_PROPERTY_ALTERATION);

		if (!isSystemSettedUp()) {
			systemSetupService.setup();
		}
		configManager.updateProperties(SYSTEM_GLOBAL_PROPERTIES, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.put(IS_SYSTEM_SETTED_UP, "true");
				properties.put(TOKEN_DURATION, Integer.toString(TOKEN_DURATION_VALUE));
				properties.put(NOTIFICATION_MINUTES, Integer.toString(NOTIFICATION_MINUTES_VALUE));
			}
		});
	}

	public String getMainDataLanguage() {
		return getGlobalProperties().get(MAIN_DATA_LANGUAGE);
	}

	boolean isSystemSettedUp() {
		return "true".equals(getGlobalProperties().get(IS_SYSTEM_SETTED_UP));
	}

	public int getTokenDuration() {
		return Integer.parseInt(getGlobalProperties().get(TOKEN_DURATION));
	}

	public int getDelayBeforeSendingNotificationEmailsInMinutes() {
		return Integer.parseInt(getGlobalProperties().get(NOTIFICATION_MINUTES));
	}

	private Map<String, String> getGlobalProperties() {
		Map<String, String> p = configManager.getProperties(SYSTEM_GLOBAL_PROPERTIES).getProperties();
		return p;
	}

	public void setProperty(final String key, final String value) {
		configManager.updateProperties(SystemGlobalConfigsManager.SYSTEM_GLOBAL_PROPERTIES, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.put(key, value);
			}
		});
	}

	@Override
	public void close() {

	}

}
