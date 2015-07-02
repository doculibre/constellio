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
package com.constellio.app.services.schemasDisplay;

import static com.constellio.app.services.schemasDisplay.OngoingAddMetadatasToSchemas.OngoingAddMetadatasToSchemasMode.DISPLAY;
import static com.constellio.app.services.schemasDisplay.OngoingAddMetadatasToSchemas.OngoingAddMetadatasToSchemasMode.FORM;
import static com.constellio.app.services.schemasDisplay.OngoingAddMetadatasToSchemas.OngoingAddMetadatasToSchemasMode.SEARCH;
import static java.util.Arrays.asList;

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
}
