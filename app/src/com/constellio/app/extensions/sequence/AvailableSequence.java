package com.constellio.app.extensions.sequence;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.constellio.model.entities.Language;

public class AvailableSequence {

	String code;

	Map<Language, String> titles;

	public AvailableSequence(String code, Map<Language, String> titles) {
		this.code = code;
		this.titles = titles;
	}

	public String getCode() {
		return code;
	}

	public Map<Language, String> getTitles() {
		return titles;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "titles");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
