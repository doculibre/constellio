package com.constellio.app.services.schemasDisplay;

import static com.constellio.app.services.schemasDisplay.OngoingAddMetadatasToSchemas.OngoingAddMetadatasToSchemasMode.*;
import static com.constellio.app.services.schemasDisplay.OngoingAddMetadatasToSchemas.OngoingAddMetadatasToSchemasMode.DISPLAY;
import static com.constellio.app.services.schemasDisplay.OngoingAddMetadatasToSchemas.OngoingAddMetadatasToSchemasMode.FORM;
import static com.constellio.app.services.schemasDisplay.OngoingAddMetadatasToSchemas.OngoingAddMetadatasToSchemasMode.SEARCH;
import static java.util.Arrays.asList;

import com.constellio.app.services.schemasDisplay.OngoingAddMetadatasToSchemas.OngoingAddMetadatasToSchemasMode;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public class SchemaTypeDisplayTransactionBuilder {

	MetadataSchemaType schemaType;
	SchemasDisplayManager schemasDisplayManager;
	SchemaTypesDisplayTransactionBuilder transaction;

	public SchemaTypeDisplayTransactionBuilder(MetadataSchemaType schemaType, SchemasDisplayManager schemasDisplayManager,
			SchemaTypesDisplayTransactionBuilder transaction) {
		this.schemaType = schemaType;
		this.schemasDisplayManager = schemasDisplayManager;
		this.transaction = transaction;
	}

	public OngoingAddMetadatasToSchemas addToForm(String... metadatas) {
		return new OngoingAddMetadatasToSchemas(schemaType, schemasDisplayManager, asList(metadatas), transaction, FORM);
	}

	public OngoingAddMetadatasToSchemas addToDisplay(String... metadatas) {
		return new OngoingAddMetadatasToSchemas(schemaType, schemasDisplayManager, asList(metadatas), transaction, DISPLAY);
	}

	public OngoingAddMetadatasToSchemas addToTable(String... metadatas) {
		return new OngoingAddMetadatasToSchemas(schemaType, schemasDisplayManager, asList(metadatas), transaction, TABLE);
	}

	public OngoingAddMetadatasToSchemas addToSearchResult(String... metadatas) {
		return new OngoingAddMetadatasToSchemas(schemaType, schemasDisplayManager, asList(metadatas), transaction, SEARCH);
	}

	public void removeFromForm(String... metadatas) {
		new OngoingAddMetadatasToSchemas(schemaType, schemasDisplayManager, asList(metadatas), transaction, FORM).remove();
	}

	public void removeFromDisplay(String... metadatas) {
		new OngoingAddMetadatasToSchemas(schemaType, schemasDisplayManager, asList(metadatas), transaction, DISPLAY).remove();
	}

	public void removeFromSearchResult(String... metadatas) {
		new OngoingAddMetadatasToSchemas(schemaType, schemasDisplayManager, asList(metadatas), transaction, SEARCH).remove();
	}

	public void removeFromTable(String... metadatas) {
		new OngoingAddMetadatasToSchemas(schemaType, schemasDisplayManager, asList(metadatas), transaction, TABLE).remove();
	}
}
