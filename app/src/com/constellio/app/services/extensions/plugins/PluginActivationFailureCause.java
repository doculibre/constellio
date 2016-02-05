package com.constellio.app.services.extensions.plugins;

import com.constellio.model.entities.EnumWithSmallCode;

public enum PluginActivationFailureCause implements EnumWithSmallCode {
	INVALID_ID_FORMAT("IIF"),
	INVALID_EXISTING_ID("IEI"),
	INVALID_VERSION("IV"),
	CANNOT_INSTALL_OLDER_VERSION("CIOV"),
	MORE_THAN_ONE_INSTALLABLE_MODULE_PER_JAR("MTOIMPJ"),
	ID_MISMATCH("IM"),
	INVALID_MIGRATION_SCRIPT("IMS"),
	INVALID_START("IS"),
	JAR_NOT_SAVED_CORRECTLY("JNSC"),
	INVALID_MANIFEST("IMA"),
	INVALID_JAR("IJ"),
	NO_ID("NI"),
	NO_VERSION("NV"),
	IO_EXCEPTION("IOE"),
	JAR_NOT_FOUND("JNF"),
	NO_INSTALLABLE_MODULE_DETECTED_FROM_JAR("NIMD"),
	//not used yet
	INVALID_INSTANTIATE("II"),
	PLUGIN_CAUSING_CYCLIC_DEPENDENCY("PCCD"),
	PLUGIN_COMPLEMENTARY_TO_INVALID_PLUGIN("PCTIP");

	private String code;

	PluginActivationFailureCause(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}