package com.constellio.app.ui.framework.builders;

import com.constellio.app.ui.entities.SystemConfigurationVO;
import com.constellio.model.entities.configs.SystemConfiguration;

import java.io.Serializable;

public class SystemConfigurationToVOBuilder implements Serializable {

	public SystemConfigurationVO build(SystemConfiguration config, Object value) {
		return new SystemConfigurationVO(config.getCode(), config.getConfigSubGroupCode(),
				value, config.getType(),
				config.getEnumClass(), config.isRebootRequired(), config.isHiddenValue());

	}

}
