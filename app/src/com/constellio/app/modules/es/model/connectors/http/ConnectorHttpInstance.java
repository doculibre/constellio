package com.constellio.app.modules.es.model.connectors.http;

import com.constellio.app.modules.es.model.connectors.AuthenticationScheme;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class ConnectorHttpInstance extends ConnectorInstance<ConnectorHttpInstance> {

	public static final String SCHEMA_LOCAL_CODE = "http";
	public static final String SCHEMA_CODE = SCHEMA_TYPE + "_" + SCHEMA_LOCAL_CODE;

	public static final String SEEDS = "seeds";

	public static final String ON_DEMANDS = "onDemands";

	public static final String INCLUDE_PATTERNS = "includePatterns";

	public static final String EXCLUDE_PATTERNS = "excludePatterns";

	public static final String NUMBER_OF_JOBS_IN_PARALLEL = "jobsInParallel";
	public static final String NUMBER_OF_DOCUMENTS_PER_JOBS = "documentsPerJobs";
	public static final String DAYS_BEFORE_REFETCHING = "daysBeforeRefetching";
	public static final String MAX_LEVEL = "maxLevel";

	public static final String AUTHENTICATION_SCHEME = "authenticationScheme";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String DOMAIN = "domain";

	public static final String IGNORE_ROBOTS_TXT = "ignoreRobotsTxt";

	public ConnectorHttpInstance(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_CODE);
	}

	public int getNumberOfJobsInParallel() {
		return getInteger(NUMBER_OF_JOBS_IN_PARALLEL);
	}

	public ConnectorHttpInstance setNumberOfJobsInParallel(int jobsInParallel) {
		set(NUMBER_OF_JOBS_IN_PARALLEL, jobsInParallel);
		return this;
	}

	public int getDocumentsPerJobs() {
		return getInteger(NUMBER_OF_DOCUMENTS_PER_JOBS);
	}

	public ConnectorHttpInstance setDocumentsPerJobs(int documentsPerJobs) {
		set(NUMBER_OF_DOCUMENTS_PER_JOBS, documentsPerJobs);
		return this;
	}

	public int getDaysBeforeRefetching() {
		return getInteger(DAYS_BEFORE_REFETCHING);
	}

	public ConnectorHttpInstance setDaysBeforeRefetching(double delayBeforeRefetching) {
		set(DAYS_BEFORE_REFETCHING, delayBeforeRefetching);
		return this;
	}

	public List<String> getSeedsList() {
		return StringUtils.isBlank(getSeeds()) ? new ArrayList<String>() : asList(getSeeds().split("\n"));
	}

	public String getSeeds() {
		return get(SEEDS);
	}

	public ConnectorHttpInstance setSeeds(String seeds) {
		set(SEEDS, seeds);
		return this;
	}

	public int getMaxLevel() {
		return getInteger(MAX_LEVEL);
	}

	public ConnectorHttpInstance setMaxLevel(int maxLevel) {
		set(MAX_LEVEL, maxLevel);
		return this;
	}

	public List<String> getOnDemandsList() {
		return StringUtils.isBlank(getOnDemands()) ? new ArrayList<String>() : asList(getOnDemands().split("\n"));
	}

	public String getOnDemands() {
		return get(ON_DEMANDS);
	}

	public ConnectorHttpInstance setOnDemands(String onDemands) {
		set(ON_DEMANDS, onDemands);
		return this;
	}

	public String getIncludePatterns() {
		return get(INCLUDE_PATTERNS);
	}

	public ConnectorHttpInstance setIncludePatterns(String includePatterns) {
		set(INCLUDE_PATTERNS, includePatterns);
		return this;
	}

	public String getExcludePatterns() {
		return get(EXCLUDE_PATTERNS);
	}

	public ConnectorHttpInstance setExcludePatterns(String excludePatterns) {
		set(EXCLUDE_PATTERNS, excludePatterns);
		return this;
	}

	public AuthenticationScheme getAuthenticationScheme() {
		return get(AUTHENTICATION_SCHEME);
	}

	public ConnectorHttpInstance setAuthenticationScheme(AuthenticationScheme authenticationScheme) {
		set(AUTHENTICATION_SCHEME, authenticationScheme);
		return this;
	}

	public String getUsername() {
		return get(USERNAME);
	}

	public ConnectorHttpInstance setUsername(String username) {
		set(USERNAME, username);
		return this;
	}

	public String getPasssword() {
		return get(PASSWORD);
	}

	public ConnectorHttpInstance setPassword(String password) {
		set(PASSWORD, password);
		return this;
	}

	public String getDomain() {
		return get(DOMAIN);
	}

	public ConnectorHttpInstance setDomain(String domain) {
		set(DOMAIN, domain);
		return this;
	}

	@Override
	public ConnectorHttpInstance setCode(String code) {
		super.setCode(code);
		return this;
	}

	@Override
	public ConnectorHttpInstance setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public boolean isIgnoreRobotsTxt() {
		return getBooleanWithDefaultValue(IGNORE_ROBOTS_TXT, false);
	}

	public ConnectorHttpInstance setIgnoreRobotsTxt(boolean ignoreRobotsTxt) {
		set(IGNORE_ROBOTS_TXT, ignoreRobotsTxt);
		return this;
	}

}
