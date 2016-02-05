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
