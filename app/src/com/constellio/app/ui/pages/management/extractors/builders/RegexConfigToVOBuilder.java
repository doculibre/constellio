package com.constellio.app.ui.pages.management.extractors.builders;

import java.io.Serializable;

import com.constellio.app.ui.pages.management.extractors.entities.RegexConfigVO;
import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.entities.schemas.RegexConfig.RegexConfigType;

@SuppressWarnings("serial")
public class RegexConfigToVOBuilder implements Serializable {

	public RegexConfigVO build(RegexConfig regexConfig) {
		String inputMetadata = regexConfig.getInputMetadata();
		String regex = regexConfig.getRegex().toString();
		String value = regexConfig.getValue();
		RegexConfigType type = regexConfig.getRegexConfigType();
		return new RegexConfigVO(inputMetadata, regex, value, type);
	}

}
