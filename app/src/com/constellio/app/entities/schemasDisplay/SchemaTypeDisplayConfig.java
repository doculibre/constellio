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
package com.constellio.app.entities.schemasDisplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SchemaTypeDisplayConfig {

	private final String collection;

	private final String schemaType;

	private final boolean manageable;

	private final boolean advancedSearch;

	private final boolean simpleSearch;

	private final List<String> metadataGroup;

	public SchemaTypeDisplayConfig(String collection, String schemaType, boolean manageable, boolean advancedSearch,
			boolean simpleSearch, List<String> metadataGroup) {
		this.collection = collection;
		this.schemaType = schemaType;
		this.manageable = manageable;
		this.advancedSearch = advancedSearch;
		this.simpleSearch = simpleSearch;
		this.metadataGroup = Collections.unmodifiableList(metadataGroup);
	}

	public SchemaTypeDisplayConfig(String collection, String schemaType, List<String> metadataGroup) {
		this.collection = collection;
		this.schemaType = schemaType;
		this.manageable = false;
		this.advancedSearch = false;
		this.simpleSearch = false;
		this.metadataGroup = Collections.unmodifiableList(metadataGroup);
	}

	public boolean isManageable() {
		return manageable;
	}

	public boolean isAdvancedSearch() {
		return advancedSearch;
	}

	public boolean isSimpleSearch() {
		return simpleSearch;
	}

	public String getCollection() {
		return collection;
	}

	public String getSchemaType() {
		return schemaType;
	}

	public List<String> getMetadataGroup() {
		return metadataGroup;
	}

	public SchemaTypeDisplayConfig withManageableStatus(boolean manageable) {
		return new SchemaTypeDisplayConfig(collection, schemaType, manageable, advancedSearch, simpleSearch, metadataGroup);
	}

	public SchemaTypeDisplayConfig withAdvancedSearchStatus(boolean advancedSearch) {
		return new SchemaTypeDisplayConfig(collection, schemaType, manageable, advancedSearch, simpleSearch, metadataGroup);
	}

	public SchemaTypeDisplayConfig withSimpleSearchStatus(boolean simpleSearch) {
		return new SchemaTypeDisplayConfig(collection, schemaType, manageable, advancedSearch, simpleSearch, metadataGroup);
	}

	public SchemaTypeDisplayConfig withMetadataGroup(List<String> metadataGroup) {
		return new SchemaTypeDisplayConfig(collection, schemaType, manageable, advancedSearch, simpleSearch, metadataGroup);
	}

	public SchemaTypeDisplayConfig withNewMetadataGroup(String newGroup) {
		List<String> groups = new ArrayList<>();
		groups.addAll(metadataGroup);
		groups.add(newGroup);
		return withMetadataGroup(groups);
	}
}
