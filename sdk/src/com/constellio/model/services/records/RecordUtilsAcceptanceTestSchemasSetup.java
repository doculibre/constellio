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
package com.constellio.model.services.records;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.setups.SchemaShortcuts;

public class RecordUtilsAcceptanceTestSchemasSetup extends TestsSchemasSetup {

	public RecordUtilsAcceptanceTestSchemasSetup withFolderAndDocumentSchemas() {
		MetadataSchemaTypeBuilder folderType = typesBuilder.createNewSchemaType("folder");
		MetadataSchemaTypeBuilder documentType = typesBuilder.createNewSchemaType("document");

		setupFolderType(folderType);
		setupDocumentType(documentType, folderType);
		return this;
	}

	private void setupFolderType(MetadataSchemaTypeBuilder folderType) {
		folderType.getDefaultSchema().create("parent").defineChildOfRelationshipToType(folderType);
	}

	private void setupDocumentType(MetadataSchemaTypeBuilder documentType, MetadataSchemaTypeBuilder folderType) {
		documentType.getDefaultSchema().create("parent").defineChildOfRelationshipToType(folderType);
	}

	public class FolderSchema implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get("folder");
		}

		public String code() {
			return "folder_default";
		}

		public String collection() {
			return "zeCollection";
		}

		public Metadata title() {
			return getMetadata(code() + "_title");
		}

		public Metadata parent() {
			return getMetadata(code() + "_parent");
		}
	}

	public class DocumentSchema implements SchemaShortcuts {
		public MetadataSchemaType type() {
			return get("documentr");
		}

		public String code() {
			return "document_default";
		}

		public String collection() {
			return "zeCollection";
		}

		public Metadata title() {
			return getMetadata(code() + "_title");
		}

		public Metadata parent() {
			return getMetadata(code() + "_parent");
		}
	}
}
