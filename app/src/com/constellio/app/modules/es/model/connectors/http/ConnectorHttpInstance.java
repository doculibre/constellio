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
package com.constellio.app.modules.es.model.connectors.http;

import java.util.List;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ConnectorHttpInstance extends ConnectorInstance<ConnectorHttpInstance> {

	public static final String SCHEMA_LOCAL_CODE = "http";
	public static final String SCHEMA_CODE = SCHEMA_TYPE + "_" + SCHEMA_LOCAL_CODE;

	public static final String SEEDS = "seeds";

	public static final String ON_DEMANDS = "onDemands";

	public ConnectorHttpInstance(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_CODE);
	}

	public List<String> getSeeds() {
		return getList(SEEDS);
	}

	public ConnectorHttpInstance setSeeds(List<String> seeds) {
		set(SEEDS, seeds);
		return this;
	}

	public List<String> getOnDemands() {
		return getList(ON_DEMANDS);
	}

	public ConnectorHttpInstance setOnDemands(List<String> onDemands) {
		set(ON_DEMANDS, onDemands);
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

}
