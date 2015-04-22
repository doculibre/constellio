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
package com.constellio.app.ui.framework.builders;

import java.io.Serializable;

import com.constellio.app.ui.entities.SystemConfigurationVO;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.services.configs.SystemConfigurationsManager;

public class SystemConfigurationToVOBuilder implements Serializable {

	public SystemConfigurationVO build(SystemConfiguration config, Object value) {
		return new SystemConfigurationVO(config.getCode(),
				value, config.getType(),
				config.getEnumClass());

	}

}
