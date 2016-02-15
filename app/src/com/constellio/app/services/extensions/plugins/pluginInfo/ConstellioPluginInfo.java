package com.constellio.app.services.extensions.plugins.pluginInfo;

import org.joda.time.LocalDate;

import com.constellio.app.services.extensions.plugins.PluginActivationFailureCause;

public class ConstellioPluginInfo {
	String code, title, version, requiredConstellioVersion;
	ConstellioPluginStatus pluginStatus;
	LocalDate lastInstallDate;
	PluginActivationFailureCause pluginActivationFailureCause;
	String stackTrace;

	public ConstellioPluginInfo setCode(String code) {
		this.code = code;
		return this;
	}

	public ConstellioPluginInfo setTitle(String title) {
		this.title = title;
		return this;
	}

	public ConstellioPluginInfo setVersion(String version) {
		this.version = version;
		return this;
	}

	public ConstellioPluginInfo setRequiredConstellioVersion(String requiredConstellioVersion) {
		this.requiredConstellioVersion = requiredConstellioVersion;
		return this;
	}

	public ConstellioPluginInfo setPluginStatus(ConstellioPluginStatus pluginStatus) {
		this.pluginStatus = pluginStatus;
		return this;
	}

	public LocalDate getLastInstallDate() {
		return lastInstallDate;
	}

	public ConstellioPluginInfo setLastInstallDate(LocalDate lastInstallDate) {
		this.lastInstallDate = lastInstallDate;
		return this;
	}

	public ConstellioPluginInfo setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
		return this;
	}

	public String getCode() {
		return code;
	}

	public String getTitle() {
		return title;
	}

	public String getVersion() {
		return version;
	}

	public String getRequiredConstellioVersion() {
		return requiredConstellioVersion;
	}

	public ConstellioPluginStatus getPluginStatus() {
		return pluginStatus;
	}

	public PluginActivationFailureCause getPluginActivationFailureCause() {
		return pluginActivationFailureCause;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public ConstellioPluginInfo setPluginActivationFailureCause(PluginActivationFailureCause pluginActivationFailureCause) {
		this.pluginActivationFailureCause = pluginActivationFailureCause;
		return this;
	}

}

