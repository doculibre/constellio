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
package com.constellio.model.services.users;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.services.users.UserPhotosServicesRuntimeException.UserPhotosServicesRuntimeException_UserHasNoPhoto;

import java.io.InputStream;

public class UserPhotosServices {

	private ConfigManager configManager;

	public UserPhotosServices(ConfigManager configManager) {
		this.configManager = configManager;
	}

	public void changePhoto(InputStream inputStream, String username) {
		String configPath = getPhotoConfigPath(username);

		if (configManager.exist(configPath)) {
			String hash = configManager.getBinary(configPath).getHash();
			try {
				configManager.update(configPath, hash, inputStream);
			} catch (OptimisticLockingConfiguration optimisticLockingConfiguration) {
				throw new ImpossibleRuntimeException(optimisticLockingConfiguration);
			}
		} else {
			configManager.add(configPath, inputStream);
		}

	}

	public StreamFactory<InputStream> getPhotoInputStream(String username) {
		String configPath = getPhotoConfigPath(username);
		if (configManager.exist(configPath)) {
			BinaryConfiguration binaryConfiguration = configManager.getBinary(configPath);
			return binaryConfiguration.getInputStreamFactory();
		} else {
			throw new UserPhotosServicesRuntimeException_UserHasNoPhoto(username);
		}
	}

	private String getPhotoConfigPath(String username) {
		return "/photos/" + username;
	}

	public boolean hasPhoto(String username) {
		String configPath = getPhotoConfigPath(username);

		return configManager.exist(configPath);
	}
}
