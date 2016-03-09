package com.constellio.model.services.records.extractions;

import com.constellio.model.entities.schemas.RegexConfig;

import java.util.regex.Matcher;

/**
 * @author Majid
 */
class RegexExtractor implements Extractor<String> {
	private RegexConfig regexConfig;
	public RegexExtractor(RegexConfig regexConfig) {
		this.regexConfig = regexConfig;
	}

	@Override
	public Object extractFrom(String feed) {
		Matcher matcher = regexConfig.getRegex().matcher(feed);
		if (matcher.find()) {
			if (regexConfig.getRegexConfigType() == RegexConfig.RegexConfigType.TRANSFORMATION) {
				String match = matcher.group();
				return regexConfig.getRegex().matcher(match).replaceAll(regexConfig.getValue());
			} else {
				return regexConfig.getValue();
			}
		}
		return null;
	}


}
