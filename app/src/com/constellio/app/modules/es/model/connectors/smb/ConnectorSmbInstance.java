/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.es.model.connectors.smb;

import java.util.List;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ConnectorSmbInstance extends ConnectorInstance<ConnectorSmbInstance> {
	public static final String SCHEMA_LOCAL_CODE = "smb";
	public static final String SCHEMA_CODE = SCHEMA_TYPE + "_" + SCHEMA_LOCAL_CODE;

	public static final String SEEDS = "smbSeeds";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String DOMAIN = "domain";
	public static final String INCLUSIONS = "inclusions";
	public static final String EXCLUSIONS = "exclusions";

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

}
