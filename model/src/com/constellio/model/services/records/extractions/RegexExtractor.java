package com.constellio.model.services.records.extractions;

import com.constellio.model.entities.schemas.RegexConfig;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Majid
 */
@XmlRootElement
public class RegexExtractor extends Extractor<String> {
	private String regexPattern;
	private Pattern pattern;
	@XmlElement
	private String value;
	@XmlElement
	private boolean substitute;

	public RegexExtractor() {
	}


	@XmlElement
	public String getRegexPattern() {
		return regexPattern;
	}

	public void setRegexPattern(String regexPattern) {
		this.regexPattern = regexPattern;
		this.pattern = Pattern.compile(regexPattern);
	}

	public RegexExtractor(String regexPattern, boolean substitute, String value) {
		setRegexPattern(regexPattern);
		this.substitute = substitute;
		this.value = value;
	}

	@Override
	public Object extractFrom(String feed) {
		Matcher matcher = pattern.matcher(feed);
		if (matcher.find()) {
			if (substitute) {
				String match = matcher.group();
				return pattern.matcher(match).replaceAll(value);
			} else {
				return value;
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
