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
package com.constellio.model.services.search.query.logical;

import com.constellio.model.entities.schemas.DataStoreField;

@SuppressWarnings("serial")
public class LogicalSearchConditionRuntimeException extends RuntimeException {

	public LogicalSearchConditionRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public LogicalSearchConditionRuntimeException(String message) {
		super(message);
	}

	public LogicalSearchConditionRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class UnsupportedConditionForMetadata extends LogicalSearchConditionRuntimeException {

		public UnsupportedConditionForMetadata(DataStoreField dataStoreField) {
			super("Unsupported condition on field '" + dataStoreField.getDataStoreCode() + "' of type '" + dataStoreField
					.getType().name() + "'");
		}

	}

	public static class MetadatasRequired extends LogicalSearchConditionRuntimeException {

		public MetadatasRequired() {
			super("Must define a non-empty list of metadatas");
		}
	}

	public static class SchemaOrSchemaTypeOrCollectionRequired extends LogicalSearchConditionRuntimeException {

		public SchemaOrSchemaTypeOrCollectionRequired() {
			super("Must define a schema or a schema type");
		}
	}
}
