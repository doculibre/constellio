package com.constellio.app.services.importExport.settings.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

public class ImportedSystemVersion {

	public int majorVersion; //Y.X.X

	public int minorVersion; //X.Y.X

	public int minorRevisionVersion; //X.X.Y

	public String fullVersion;

	public boolean OnlyUSR;

	public List<String> plugins = new ArrayList<>(); //list of plugins installed

	public int getMajorVersion() {
		return majorVersion;
	}

	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}

	public int getMinorVersion() {
		return minorVersion;
	}

	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}

	public int getMinorRevisionVersion() {
		return minorRevisionVersion;
	}

	public void setMinorRevisionVersion(int minorRevisionVersion) {
		this.minorRevisionVersion = minorRevisionVersion;
	}

	public List<String> getPlugins() {
		return plugins;
	}

	public void setPlugins(List<String> plugins) {
		this.plugins = plugins;
	}

	public boolean isOnlyUSR() {
		return OnlyUSR;
	}

	public void setOnlyUSR(boolean onlyUSR) {
		OnlyUSR = onlyUSR;
	}

	public String getFullVersion() {
		return fullVersion;
	}

	public void setFullVersion(String fullVersion) {
		this.fullVersion = fullVersion;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);

	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
