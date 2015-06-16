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

public class SchemaDisplayConfig {

	private final String schemaCode;

	private final String collection;

	private final List<String> displayMetadataCodes;

	private final List<String> formMetadataCodes;

	private final List<String> searchResultsMetadataCodes;

	public SchemaDisplayConfig(String collection, String schemaCode, List<String> displayMetadataCodes,
			List<String> formMetadataCodes, List<String> searchResultsMetadataCodes) {
		this.collection = collection;
		this.schemaCode = schemaCode;
		this.displayMetadataCodes = Collections.unmodifiableList(displayMetadataCodes);
		this.formMetadataCodes = Collections.unmodifiableList(formMetadataCodes);
		this.searchResultsMetadataCodes = Collections.unmodifiableList(searchResultsMetadataCodes);
	}

	public List<String> getDisplayMetadataCodes() {
		return displayMetadataCodes;
	}

	public List<String> getFormMetadataCodes() {
		return formMetadataCodes;
	}

	public List<String> getSearchResultsMetadataCodes() {
		return searchResultsMetadataCodes;
	}

	public String getSchemaCode() {
		return schemaCode;
	}

	public String getCollection() {
		return collection;
	}

	public SchemaDisplayConfig withDisplayMetadataCodes(List<String> displayMetadataCodes) {
		return new SchemaDisplayConfig(collection, schemaCode, displayMetadataCodes, formMetadataCodes,
				searchResultsMetadataCodes);
	}

	public SchemaDisplayConfig withFormMetadataCodes(List<String> formMetadataCodes) {
		return new SchemaDisplayConfig(collection, schemaCode, displayMetadataCodes, formMetadataCodes,
				searchResultsMetadataCodes);
	}

	public SchemaDisplayConfig withSearchResultsMetadataCodes(List<String> searchResultsMetadataCodes) {
		return new SchemaDisplayConfig(collection, schemaCode, displayMetadataCodes, formMetadataCodes,
				searchResultsMetadataCodes);
	}

	public SchemaDisplayConfig withNewDisplayMetadataBefore(String metadataCode, String before) {
		int index = displayMetadataCodes.indexOf(before);
		List<String> displayMetadataCodes = new ArrayList<>();
		displayMetadataCodes.addAll(this.displayMetadataCodes);
		displayMetadataCodes.add(index, metadataCode);
		return withDisplayMetadataCodes(displayMetadataCodes);
	}

	public SchemaDisplayConfig withNewFormMetadata(String metadataCode) {
		List<String> formMetadatas = new ArrayList<>();
		formMetadatas.addAll(this.formMetadataCodes);
		formMetadatas.add(metadataCode);
		return withFormMetadataCodes(formMetadatas);
	}

}

