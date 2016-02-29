package com.constellio.model.services.migrations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.entities.configs.core.listeners.UserTitlePatternConfigScript;
import com.constellio.model.entities.enums.MetadataPopulatePriority;
import com.constellio.model.entities.enums.TitleMetadataPopulatePriority;
import com.constellio.model.services.configs.SystemConfigurationsManager;

public class ConstellioEIMConfigs {

	private static List<SystemConfiguration> modifiableConfigs = new ArrayList<>();
	public static List<SystemConfiguration> configurations;

	//Retention calendar configs
	public static final SystemConfiguration USER_TITLE_PATTERN;

	public static final SystemConfiguration USER_ROLES_IN_AUTHORIZATIONS;
	public static final SystemConfiguration PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS;
	public static final SystemConfiguration CONTENT_MAX_LENGTH_FOR_PARSING_IN_MEGAOCTETS;

	public static final SystemConfiguration METADATA_POPULATE_PRIORITY, TITLE_METADATA_POPULATE_PRIORITY;
	public static final SystemConfiguration LOGO;
	public static final SystemConfiguration LOGO_LINK;
	public static final SystemConfiguration CONSTELLIO_URL;
	public static final SystemConfiguration CLEAN_DURING_INSTALL;
	public static final SystemConfiguration ENABLE_RECOVERY_MODE;

	static {
		SystemConfigurationGroup others = new SystemConfigurationGroup(null, "others");
		add(USER_TITLE_PATTERN = others.createString("userTitlePattern").scriptedBy(UserTitlePatternConfigScript.class)
				.withDefaultValue("${firstName} ${lastName}"));

		// Associer ou non des rôles utilisateur aux autorisations
		add(USER_ROLES_IN_AUTHORIZATIONS = others.createBooleanFalseByDefault("userRolesInAuthorizations"));

		add(LOGO = others.createBinary("logo"));
		add(LOGO_LINK = others.createString("logoLink", "http://www.constellio.com"));
		add(METADATA_POPULATE_PRIORITY = others.createEnum("metadataPopulatePriority", MetadataPopulatePriority.class)
				.withDefaultValue(MetadataPopulatePriority.STYLES_REGEX_PROPERTIES));
		add(TITLE_METADATA_POPULATE_PRIORITY = others
				.createEnum("titleMetadataPopulatePriority", TitleMetadataPopulatePriority.class)
				.withDefaultValue(TitleMetadataPopulatePriority.STYLES_FILENAME_PROPERTIES));
		add(CONSTELLIO_URL = others.createString("constellioUrl", "http://localhost:8080/constellio/"));

		SystemConfigurationGroup advanced = new SystemConfigurationGroup(null, "advanced");
		add(PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS = advanced.createInteger("parsedContentMaxLengthInKilooctets")
				.withDefaultValue(3000));
		add(CONTENT_MAX_LENGTH_FOR_PARSING_IN_MEGAOCTETS = advanced.createInteger("contentMaxLengthForParsingInMegaoctets")
				.withDefaultValue(10));

		add(CLEAN_DURING_INSTALL = others.createBooleanFalseByDefault("cleanDuringInstall"));

		add(ENABLE_RECOVERY_MODE = others.createBooleanFalseByDefault("enableRecoveryMode").whichIsHidden());

		configurations = Collections.unmodifiableList(modifiableConfigs);
	}

	static void add(SystemConfiguration configuration) {
		modifiableConfigs.add(configuration);
	}

	SystemConfigurationsManager manager;

	public ConstellioEIMConfigs(SystemConfigurationsManager manager) {
		this.manager = manager;
	}

	public MetadataPopulatePriority getMetadataPopulatePriority() {
		return manager.getValue(METADATA_POPULATE_PRIORITY);
	}

	public TitleMetadataPopulatePriority getTitleMetadataPopulatePriority() {
		return manager.getValue(TITLE_METADATA_POPULATE_PRIORITY);
	}

	public String getUserTitlePattern() {
		return manager.getValue(USER_TITLE_PATTERN);
	}

	public boolean seeUserRolesInAuthorizations() {
		return manager.getValue(USER_ROLES_IN_AUTHORIZATIONS);
	}

	public String getConstellioUrl() {
		return manager.getValue(CONSTELLIO_URL);
	}

	public Boolean isCleanDuringInstall() {
		return manager.getValue(CLEAN_DURING_INSTALL);
	}

	public Boolean isRecoveryModeEnabled() {
		return manager.getValue(ENABLE_RECOVERY_MODE);
	}

	public static Collection<? extends SystemConfiguration> getCoreConfigs() {
		return configurations;
	}
}
