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
package com.constellio.app.ui.pages.search.criteria;

import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.sdk.tests.schemas.SchemasSetup;
import com.constellio.sdk.tests.setups.SchemaShortcuts;

public class SearchCriterionTestSetup extends SchemasSetup {
	public static final String SCHEMA_TYPE = "criterionTestRecord";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String A_STRING = "aString";
	public static final String A_BOOLEAN = "aBoolean";
	public static final String AN_INT = "anInt";
	public static final String A_DOUBLE = "aDouble";
	public static final String A_DATE = "aDate";
	public static final String AN_ENUM = "anEnum";

	public enum TestEnum implements EnumWithSmallCode {
		VALUE1("a"), VALUE2("b"), VALUE3("c");

		private final String code;

		TestEnum(String code) {
			this.code = code;
		}

		@Override
		public String getCode() {
			return code;
		}
	}

	public SearchCriterionTestSetup(String collection) {
		super(collection);
	}

	@Override
	public void setUp() {
		MetadataSchemaBuilder builder = typesBuilder.createNewSchemaType(SCHEMA_TYPE).getDefaultSchema();
		builder.create(A_STRING).setType(MetadataValueType.STRING);
		builder.create(A_BOOLEAN).setType(MetadataValueType.BOOLEAN);
		// We cannot have integers yet
		//builder.create(AN_INT).setType(MetadataValueType.INTEGER);
		builder.create(A_DOUBLE).setType(MetadataValueType.NUMBER);
		builder.create(A_DATE).setType(MetadataValueType.DATE);
		builder.create(AN_ENUM).defineAsEnum(TestEnum.class);
	}

	public CriterionTestRecord getShortcuts() {
		return new CriterionTestRecord();
	}

	public class CriterionTestRecord implements SchemaShortcuts {
		@Override
		public String code() {
			return SCHEMA_TYPE;
		}

		@Override
		public String collection() {
			return collection;
		}

		public Metadata aString() {
			return getMetadata(DEFAULT_SCHEMA + "_" + A_STRING);
		}

		public Metadata aBoolean() {
			return getMetadata(DEFAULT_SCHEMA + "_" + A_BOOLEAN);
		}

		public Metadata anInt() {
			return getMetadata(DEFAULT_SCHEMA + "_" + AN_INT);
		}

		public Metadata aDouble() {
			return getMetadata(DEFAULT_SCHEMA + "_" + A_DOUBLE);
		}

		public Metadata aDate() {
			return getMetadata(DEFAULT_SCHEMA + "_" + A_DATE);
		}

		public Metadata anEnum() {
			return getMetadata(DEFAULT_SCHEMA + "_" + AN_ENUM);
		}
	}
}