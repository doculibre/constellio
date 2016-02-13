package com.constellio.app.ui.framework.builders;

import java.io.Serializable;

import com.constellio.app.ui.entities.SystemConfigurationVO;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.services.configs.SystemConfigurationsManager;

public class SystemConfigurationToVOBuilder implements Serializable {

	public SystemConfigurationVO build(SystemConfiguration config, Object value) {
		return new SystemConfigurationVO(config.getCode(),
				value, config.getType(),
				config.getEnumClass(), config.isRebootRequired(), config.isHiddenValue());

	}

}
