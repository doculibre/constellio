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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;

public class SchemaDisplayManagerTransaction {

	String collection;

	SchemaTypesDisplayConfig modifiedCollectionTypes;

	List<SchemaTypeDisplayConfig> modifiedTypes = new ArrayList<>();

	List<SchemaDisplayConfig> modifiedSchemas = new ArrayList<>();

	List<MetadataDisplayConfig> modifiedMetadatas = new ArrayList<>();

	public SchemaTypesDisplayConfig getModifiedCollectionTypes() {
		return modifiedCollectionTypes;
	}

	public List<SchemaTypeDisplayConfig> getModifiedTypes() {
		return modifiedTypes;
	}

	public List<SchemaDisplayConfig> getModifiedSchemas() {
		return modifiedSchemas;
	}

	public List<MetadataDisplayConfig> getModifiedMetadatas() {
		return modifiedMetadatas;
	}

	public void add(SchemaTypeDisplayConfig typeDisplayConfig) {
		modifiedTypes.add(typeDisplayConfig);
	}

	public void add(SchemaDisplayConfig schemaDisplayConfig) {
		for (Iterator<SchemaDisplayConfig> iterator = modifiedSchemas.iterator(); iterator.hasNext(); ) {
			SchemaDisplayConfig modifiedSchema = iterator.next();
			if (modifiedSchema.getSchemaCode().equals(schemaDisplayConfig.getSchemaCode())) {
				iterator.remove();
			}
		}
		modifiedSchemas.add(schemaDisplayConfig);
	}

	public void add(MetadataDisplayConfig metadataDisplayConfig) {
		modifiedMetadatas.add(metadataDisplayConfig);
	}

	public void setModifiedCollectionTypes(SchemaTypesDisplayConfig modifiedCollectionTypes) {
		this.modifiedCollectionTypes = modifiedCollectionTypes;
	}

	public String getCollection() {

		if (modifiedCollectionTypes != null) {
			collection = modifiedCollectionTypes.getCollection();
		}

		for (SchemaTypeDisplayConfig config : modifiedTypes) {
			ensureSameCollection(config.getCollection());
		}

		for (SchemaDisplayConfig config : modifiedSchemas) {
			ensureSameCollection(config.getCollection());
		}

		for (MetadataDisplayConfig config : modifiedMetadatas) {
			ensureSameCollection(config.getCollection());
		}
		return collection;
	}

	private void ensureSameCollection(String collection) {
		if (this.collection == null) {
			this.collection = collection;
		} else if (!this.collection.equals(collection)) {
			throw new RuntimeException("Configs must be in same collection");
		}

	}

}
