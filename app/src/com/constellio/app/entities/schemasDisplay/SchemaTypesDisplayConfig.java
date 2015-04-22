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

import java.util.Collections;
import java.util.List;

public class SchemaTypesDisplayConfig {

	final String collection;

	final List<String> facetMetadataCodes;

	public SchemaTypesDisplayConfig(String collection, List<String> facetMetadataCodes) {
		this.collection = collection;
		this.facetMetadataCodes = Collections.unmodifiableList(facetMetadataCodes);
	}

	public SchemaTypesDisplayConfig(String collection) {
		this.collection = collection;
		this.facetMetadataCodes = Collections.emptyList();
	}

	public List<String> getFacetMetadataCodes() {
		return facetMetadataCodes;
	}

	public String getCollection() {
		return collection;
	}

	public SchemaTypesDisplayConfig withFacetMetadataCodes(List<String> facetMetadataCodes) {
		return new SchemaTypesDisplayConfig(collection, facetMetadataCodes);
	}
}

