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
import java.util.List;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public class OngoingAddMetadatasToSchemas {

	enum OngoingAddMetadatasToSchemasMode {FORM, DISPLAY, SEARCH}

	SchemaTypesDisplayTransactionBuilder transaction;
	OngoingAddMetadatasToSchemasMode mode;
	MetadataSchemaType schemaType;
	SchemasDisplayManager schemasDisplayManager;
	List<String> metadatasLocalCodes;

	public OngoingAddMetadatasToSchemas(MetadataSchemaType schemaType, SchemasDisplayManager schemasDisplayManager,
			List<String> metadatasLocalCodes, SchemaTypesDisplayTransactionBuilder transaction,
			OngoingAddMetadatasToSchemasMode mode) {
		this.transaction = transaction;
		this.schemaType = schemaType;
		this.schemasDisplayManager = schemasDisplayManager;
		this.metadatasLocalCodes = metadatasLocalCodes;
		this.mode = mode;
	}

	public void afterMetadata(final String afterMetadata) {
		transaction.updateAllSchemas(schemaType.getCode(), new SchemaDisplayAlteration() {
			@Override
			public SchemaDisplayConfig alter(SchemaDisplayConfig schemaDisplayConfig) {
				List<String> list = getList(schemaDisplayConfig);
				List<String> metadataCodes = getMetadataCodesFor(schemaDisplayConfig.getSchemaCode());

				list.removeAll(metadataCodes);

				int index = list.indexOf(schemaDisplayConfig.getSchemaCode() + "_" + afterMetadata);
				if (index != -1) {
					list.addAll(index + 1, metadataCodes);
				} else {
					list.addAll(metadataCodes);
				}

				return withList(schemaDisplayConfig, list);
			}

		});
	}

	public void beforeMetadata(final String afterMetadata) {
		transaction.updateAllSchemas(schemaType.getCode(), new SchemaDisplayAlteration() {
			@Override
			public SchemaDisplayConfig alter(SchemaDisplayConfig schemaDisplayConfig) {
				List<String> list = getList(schemaDisplayConfig);
				List<String> metadataCodes = getMetadataCodesFor(schemaDisplayConfig.getSchemaCode());

				list.removeAll(metadataCodes);

				int index = list.indexOf(schemaDisplayConfig.getSchemaCode() + "_" + afterMetadata);
				if (index != -1) {
					list.addAll(index, metadataCodes);
				} else {
					list.addAll(metadataCodes);
				}

				return withList(schemaDisplayConfig, list);
			}

		});
	}

	public void atTheEnd() {
		transaction.updateAllSchemas(schemaType.getCode(), new SchemaDisplayAlteration() {
			@Override
			public SchemaDisplayConfig alter(SchemaDisplayConfig schemaDisplayConfig) {
				List<String> list = getList(schemaDisplayConfig);
				List<String> metadataCodes = getMetadataCodesFor(schemaDisplayConfig.getSchemaCode());

				list.removeAll(metadataCodes);
				list.addAll(metadataCodes);

				return withList(schemaDisplayConfig, list);
			}

		});
	}

	private List<String> getMetadataCodesFor(String schemaCode) {
		List<String> metadataCodes = new ArrayList<>();

		for (String metadataLocalCode : metadatasLocalCodes) {
			metadataCodes.add(schemaCode + "_" + metadataLocalCode);
		}

		return metadataCodes;
	}

	public void beforeTheHugeCommentMetadata() {
		beforeMetadata("comments");
	}

	private List<String> getList(SchemaDisplayConfig schemaDisplayConfig) {
		switch (mode) {

		case FORM:
			return new ArrayList<>(schemaDisplayConfig.getFormMetadataCodes());

		case DISPLAY:
			return new ArrayList<>(schemaDisplayConfig.getDisplayMetadataCodes());

		case SEARCH:
			return new ArrayList<>(schemaDisplayConfig.getSearchResultsMetadataCodes());

		}
		return null;
	}

	private SchemaDisplayConfig withList(SchemaDisplayConfig schemaDisplayConfig, List<String> newValue) {
		switch (mode) {

		case FORM:
			return schemaDisplayConfig.withFormMetadataCodes(newValue);

		case DISPLAY:
			return schemaDisplayConfig.withDisplayMetadataCodes(newValue);

		case SEARCH:
			return schemaDisplayConfig.withSearchResultsMetadataCodes(newValue);

		}
		return null;
	}
}
