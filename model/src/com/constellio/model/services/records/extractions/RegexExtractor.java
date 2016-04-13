package com.constellio.model.services.records.extractions;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Majid
 */
public class RegexExtractor implements Extractor<String> {
	private String regexPattern;
	private Pattern pattern;
	private String value;
	private boolean substitute;

	public RegexExtractor() {
	}


	public String getRegexPattern() {
		return regexPattern;
	}

	public void setRegexPattern(String regexPattern) {
		this.regexPattern = regexPattern;
		this.pattern = Pattern.compile(regexPattern);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isSubstitute() {
		return substitute;
	}

	public void setSubstitute(boolean substitute) {
		this.substitute = substitute;
	}

	public RegexExtractor(String regexPattern, boolean substitute, String value) {
		setRegexPattern(regexPattern);
		this.substitute = substitute;
		this.value = value;
	}

	@Override
	public List<? extends Object> extractFrom(String feed) {
		Matcher matcher = pattern.matcher(feed);
		if (matcher.find()) {
			if (substitute) {
				String match = matcher.group();
				return Collections.singletonList(pattern.matcher(match).replaceAll(value));
			} else {
				return Collections.singletonList(value);
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		RegexExtractor rhs = (RegexExtractor) obj;
		return new EqualsBuilder()
				.append(regexPattern, rhs.regexPattern)
				.append(value, rhs.value)
				.append(substitute, rhs.substitute)
				.isEquals();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
