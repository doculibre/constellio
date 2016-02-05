package com.constellio.app.services.schemasDisplay;

import static java.util.Arrays.asList;

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

	void remove() {
		transaction.updateAllSchemas(schemaType.getCode(), new SchemaDisplayAlteration() {
			@Override
			public SchemaDisplayConfig alter(SchemaDisplayConfig schemaDisplayConfig) {
				List<String> list = getList(schemaDisplayConfig);
				List<String> metadataCodes = getMetadataCodesFor(schemaDisplayConfig.getSchemaCode());

				list.removeAll(metadataCodes);

				return withList(schemaDisplayConfig, list);
			}

		});
	}

	public SchemaTypesDisplayTransactionBuilder afterMetadata(final String afterMetadata) {
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
		return transaction;
	}

	public SchemaTypesDisplayTransactionBuilder beforeMetadata(final String beforeMetadata) {
		return beforeMetadatas(asList(beforeMetadata));
	}

	public SchemaTypesDisplayTransactionBuilder beforeMetadatas(final List<String> beforeMetadatas) {
		transaction.updateAllSchemas(schemaType.getCode(), new SchemaDisplayAlteration() {
			@Override
			public SchemaDisplayConfig alter(SchemaDisplayConfig schemaDisplayConfig) {
				List<String> list = getList(schemaDisplayConfig);
				List<String> metadataCodes = getMetadataCodesFor(schemaDisplayConfig.getSchemaCode());

				list.removeAll(metadataCodes);

				int index = -1;
				for (String beforeMetadata : beforeMetadatas) {
					int metadataIndex = list.indexOf(schemaDisplayConfig.getSchemaCode() + "_" + beforeMetadata);
					if (index == -1) {
						index = metadataIndex;
					} else if (metadataIndex != -1) {
						index = Math.min(index, metadataIndex);
					}
				}

				if (index != -1) {
					list.addAll(index, metadataCodes);
				} else {
					list.addAll(metadataCodes);
				}

				return withList(schemaDisplayConfig, list);
			}

		});
		return transaction;
	}

	public SchemaTypesDisplayTransactionBuilder atTheEnd() {
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
		return transaction;
	}

	public SchemaTypesDisplayTransactionBuilder atFirstPosition() {
		transaction.updateAllSchemas(schemaType.getCode(), new SchemaDisplayAlteration() {
			@Override
			public SchemaDisplayConfig alter(SchemaDisplayConfig schemaDisplayConfig) {
				List<String> list = getList(schemaDisplayConfig);
				List<String> metadataCodes = getMetadataCodesFor(schemaDisplayConfig.getSchemaCode());

				list.removeAll(metadataCodes);
				list.addAll(0, metadataCodes);

				return withList(schemaDisplayConfig, list);
			}

		});
		return transaction;
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

	public List<String> getList(SchemaDisplayConfig schemaDisplayConfig) {
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

	public SchemaDisplayConfig withList(SchemaDisplayConfig schemaDisplayConfig, List<String> newValue) {
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
