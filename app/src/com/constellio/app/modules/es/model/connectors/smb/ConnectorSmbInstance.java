package com.constellio.app.modules.es.model.connectors.smb;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;

public class ConnectorSmbInstance extends ConnectorInstance<ConnectorSmbInstance> {
	public static final String SCHEMA_LOCAL_CODE = "smb";
	public static final String SCHEMA_CODE = SCHEMA_TYPE + "_" + SCHEMA_LOCAL_CODE;

	public static final String SEEDS = "smbSeeds";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String DOMAIN = "domain";
	public static final String INCLUSIONS = "inclusions";
	public static final String EXCLUSIONS = "exclusions";
	public static final String RESUME_URL = "resumeUrl";

	public static final String SKIP_SHARE_ACCESS_CONTROL = "skipShareAccessControl";
	public static final String SKIP_CONTENT_AND_ACL = "skipContentAndACL";

	public ConnectorSmbInstance(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_CODE);
	}

	public List<String> getSeeds() {
		return getList(SEEDS);
	}

	public ConnectorSmbInstance setSeeds(List<String> seeds) {
		set(SEEDS, seeds);
		return this;
	}

	public String getUsername() {
		return get(USERNAME);
	}

	public ConnectorSmbInstance setUsername(String username) {
		set(USERNAME, username);
		return this;
	}

	public String getPassword() {
		return get(PASSWORD);
	}

	public ConnectorSmbInstance setPassword(String password) {
		set(PASSWORD, password);
		return this;
	}

	public String getDomain() {
		return get(DOMAIN);
	}

	public ConnectorSmbInstance setDomain(String domain) {
		set(DOMAIN, domain);
		return this;
	}

	@Override
	public ConnectorSmbInstance setCode(String code) {
		super.setCode(code);
		return this;
	}

	@Override
	public ConnectorSmbInstance setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public List<String> getInclusions() {
		return get(INCLUSIONS);
	}

	public ConnectorSmbInstance setInclusions(List<String> inclusions) {
		set(INCLUSIONS, inclusions);
		return this;
	}

	public List<String> getExclusions() {
		return get(EXCLUSIONS);
	}

	public ConnectorSmbInstance setExclusions(List<String> exclusions) {
		set(EXCLUSIONS, exclusions);
		return this;
	}

	public ConnectorSmbInstance setResumeUrl(String url) {
		set(RESUME_URL, url);
		return this;
	}

	public String getResumeUrl() {
		return get(RESUME_URL);
	}

	public boolean isSkipShareAccessControl() {
		return getBooleanWithDefaultValue(SKIP_SHARE_ACCESS_CONTROL, false);
	}

	public ConnectorSmbInstance setSkipShareAccessControl(boolean skipShareAccessControl) {
		set(SKIP_SHARE_ACCESS_CONTROL, skipShareAccessControl);
		return this;
	}

	public boolean isSkipContentAndAcl() {
		return getBooleanWithDefaultValue(SKIP_CONTENT_AND_ACL, false);
	}

	public ConnectorSmbInstance setSkipContentAndAcl(boolean skipContentAndAcl) {
		set(SKIP_CONTENT_AND_ACL, skipContentAndAcl);
		return this;
	}

}