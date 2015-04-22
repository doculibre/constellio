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
package com.constellio.sdk.tests;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.constellio.data.utils.PropertyFileUtils;

public class SDKPropertiesLoader {

	boolean locked;

	Map<String, String> sdkProperties;

	Map<String, String> getSDKProperties() {
		if (locked) {
			return new HashMap<String, String>();
		}
		if (sdkProperties == null) {
			sdkProperties = loadSDKProperties();
		}
		return sdkProperties;
	}

	private Map<String, String> loadSDKProperties() {
		File sdkProperties = new SDKFoldersLocator().getSDKProperties();

		if (!sdkProperties.exists()) {
			throw new RuntimeException("'" + sdkProperties.getAbsolutePath() + "' does not exist in project 'sdk'.");
		}

		return PropertyFileUtils.loadKeyValues(sdkProperties);
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
}
