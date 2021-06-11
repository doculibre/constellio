package com.constellio.model.services.records;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.setups.SchemaShortcuts;

public class RecordUtilsAcceptanceTestSchemasSetup extends TestsSchemasSetup {

	public RecordUtilsAcceptanceTestSchemasSetup withFolderAndDocumentSchemas() {
		MetadataSchemaTypeBuilder folderType = typesBuilder.createNewSchemaTypeWithSecurity("folder");
		MetadataSchemaTypeBuilder documentType = typesBuilder.createNewSchemaTypeWithSecurity("document");

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

		@Override
		public MetadataSchema instance() {
			return getSchema(code());
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

		@Override
		public MetadataSchema instance() {
			return getSchema(code());
		}

		public Metadata title() {
			return getMetadata(code() + "_title");
		}

		public Metadata parent() {
			return getMetadata(code() + "_parent");
		}
	}
}
