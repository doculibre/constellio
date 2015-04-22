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
package com.constellio.app.ui.framework.items;

import java.util.Collection;

import com.constellio.app.ui.entities.SystemConfigurationGroupVO;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

@SuppressWarnings("serial")
public class SystemConfigurationGroupVOItem implements Item {

	final SystemConfigurationGroupVO configGroup;

	public SystemConfigurationGroupVOItem(SystemConfigurationGroupVO configGroup) {
		super();
		this.configGroup = configGroup;
	}

	public SystemConfigurationGroupVO getConfigGroup() {
		return configGroup;
	}

	@Override
	public Property getItemProperty(Object id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<?> getItemPropertyIds() {
		return getConfigGroup().getConfigs();

	}

	@Override
	public boolean addItemProperty(Object id, Property property)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeItemProperty(Object id)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return false;
	}

}
