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
package com.constellio.model.services.search.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;

public class ReturnedMetadatasFilter {

	private boolean includeParsedContent;

	private boolean includeLargeText;

	private List<Metadata> acceptedFields;

	public ReturnedMetadatasFilter(boolean includeParsedContent, boolean includeLargeText) {
		this.includeParsedContent = includeParsedContent;
		this.includeLargeText = includeLargeText;
	}

	public ReturnedMetadatasFilter(List<Metadata> acceptedFields) {
		this.acceptedFields = acceptedFields;
	}

	public static ReturnedMetadatasFilter idVersionSchema() {
		return ReturnedMetadatasFilter.onlyFields(new ArrayList<Metadata>());
	}

	public static ReturnedMetadatasFilter idVersionSchemaTitle() {
		return ReturnedMetadatasFilter.onlyFields(Arrays.asList(Schemas.TITLE));
	}

	public static ReturnedMetadatasFilter idVersionSchemaTitlePath() {
		return ReturnedMetadatasFilter.onlyFields(Arrays.asList(Schemas.TITLE, Schemas.PATH));
	}

	public static ReturnedMetadatasFilter onlyFields(Metadata... metadatas) {
		return ReturnedMetadatasFilter.onlyFields(Arrays.asList(metadatas));
	}

	public static ReturnedMetadatasFilter onlyFields(List<Metadata> metadatas) {
		return new ReturnedMetadatasFilter(metadatas);
	}

	public static ReturnedMetadatasFilter allExceptContentAndLargeText() {
		return new ReturnedMetadatasFilter(false, false);
	}

	public static ReturnedMetadatasFilter allExceptLarge() {
		return new ReturnedMetadatasFilter(false, true);
	}

	public static ReturnedMetadatasFilter all() {
		return new ReturnedMetadatasFilter(true, true);
	}

	public boolean isIncludeParsedContent() {
		return includeParsedContent;
	}

	public List<Metadata> getAcceptedFields() {
		return acceptedFields;
	}

	public ReturnedMetadatasFilter withIncludedField(Metadata metadata) {
		if (this.acceptedFields == null) {
			return this;
		} else {
			List<Metadata> acceptedFields = new ArrayList<>(this.acceptedFields);
			acceptedFields.add(metadata);
			return new ReturnedMetadatasFilter(acceptedFields);
		}
	}
}
