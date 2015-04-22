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
package com.constellio.app.modules.rm.wrappers.type;

import com.constellio.app.modules.rm.wrappers.ValueListItem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class MediumType extends ValueListItem {

	public static final String SCHEMA_TYPE = "ddvMediumType";

	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String ANALOGICAL = "analogical";

	public MediumType(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public MediumType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public MediumType setCode(String code) {
		super.setCode(code);
		return this;
	}

	public MediumType setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	public boolean isAnalogical() {
		return getBooleanWithDefaultValue(ANALOGICAL, false);
	}

	public MediumType setAnalogical(boolean analogical) {
		set(ANALOGICAL, analogical);
		return this;
	}
}
