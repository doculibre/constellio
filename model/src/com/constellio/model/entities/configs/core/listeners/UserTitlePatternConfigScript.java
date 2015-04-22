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
package com.constellio.model.entities.configs.core.listeners;

import com.constellio.model.entities.configs.SystemConfigurationScript;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;

public class UserTitlePatternConfigScript implements SystemConfigurationScript<String> {

	@Override
	public void validate(String newValue, ValidationErrors errors) {
	}

	@Override
	public void onValueChanged(String previousValue, String newValue, ModelLayerFactory modelLayerFactory) {
	}

	@Override
	public void onValueChanged(String previousValue, String newValue, ModelLayerFactory modelLayerFactory, String collection) {
	}
}
