package com.constellio.app.ui.framework.builders;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.CollectionInfoVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("serial")
public class MetadataSchemaToVOBuilder implements Serializable {

	static final List<String> DISPLAYED_SYSTEM_RESERVED_METADATA_CODES = Arrays.asList(
			//			"id",
			"createdOn",
			"createdBy",
			"modifiedOn",
			"modifiedBy");

	@Deprecated
	public MetadataSchemaVO build(MetadataSchema schema, VIEW_MODE viewMode) {
		return build(schema, viewMode, null, ConstellioUI.getCurrentSessionContext());
	}

	public MetadataSchemaVO build(MetadataSchema schema, VIEW_MODE viewMode, SessionContext sessionContext) {
		return build(schema, viewMode, null, sessionContext);
	}

	@Deprecated
	public MetadataSchemaVO build(MetadataSchema schema, VIEW_MODE viewMode, List<String> metadataCodes) {
		return build(schema, viewMode, metadataCodes, ConstellioUI.getCurrentSessionContext());
	}

	public MetadataSchemaVO build(MetadataSchema schema, VIEW_MODE viewMode, List<String> metadataCodes,
								  SessionContext sessionContext, boolean addMetadataCodes) {
		return build(schema, viewMode, metadataCodes, sessionContext, addMetadataCodes, false);
	}

	public MetadataSchemaVO build(MetadataSchema schema, VIEW_MODE viewMode, List<String> metadataCodes,
								  SessionContext sessionContext, boolean addMetadataCodes,
								  boolean withoutBuildingMetadatas) {
		String code = schema.getCode();
		String collection = schema.getCollection();
		String localCode = schema.getLocalCode();

		Map<Locale, String> labels = new HashMap<Locale, String>();
		Language language = Language.withCode(sessionContext.getCurrentLocale().getLanguage());
		labels.put(sessionContext.getCurrentLocale(), schema.getLabel(language));

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaDisplayConfig schemaDisplayConfig = schemasDisplayManager.getSchema(collection, code);

		List<String> formMetadataCodes = new ArrayList<>();
		List<String> displayMetadataCodes = new ArrayList<>();
		List<String> searchMetadataCodes = new ArrayList<>();
		List<String> tableMetadataCodes = new ArrayList<>();

		if (viewMode == VIEW_MODE.FORM) {
			if (metadataCodes != null) {
				if (addMetadataCodes) {
					formMetadataCodes.addAll(schemaDisplayConfig.getFormMetadataCodes());
				}
				formMetadataCodes.addAll(metadataCodes);
			} else {
				formMetadataCodes.addAll(schemaDisplayConfig.getFormMetadataCodes());
			}
			displayMetadataCodes.addAll(schemaDisplayConfig.getDisplayMetadataCodes());
			searchMetadataCodes.addAll(schemaDisplayConfig.getSearchResultsMetadataCodes());
			tableMetadataCodes.addAll(schemaDisplayConfig.getTableMetadataCodes());
		} else if (viewMode == VIEW_MODE.DISPLAY) {
			if (metadataCodes != null) {
				if (addMetadataCodes) {
					displayMetadataCodes.addAll(schemaDisplayConfig.getDisplayMetadataCodes());
				}
				displayMetadataCodes.addAll(metadataCodes);
			} else {
				displayMetadataCodes.addAll(schemaDisplayConfig.getDisplayMetadataCodes());
			}
			formMetadataCodes.addAll(schemaDisplayConfig.getFormMetadataCodes());
			searchMetadataCodes.addAll(schemaDisplayConfig.getSearchResultsMetadataCodes());
			tableMetadataCodes.addAll(schemaDisplayConfig.getTableMetadataCodes());
		} else if (viewMode == VIEW_MODE.TABLE) {
			if (metadataCodes != null) {
				if (addMetadataCodes) {
					tableMetadataCodes.addAll(schemaDisplayConfig.getTableMetadataCodes());
				}
				tableMetadataCodes.addAll(metadataCodes);
			} else {
				tableMetadataCodes.addAll(schemaDisplayConfig.getTableMetadataCodes());
			}
			formMetadataCodes.addAll(schemaDisplayConfig.getFormMetadataCodes());
			displayMetadataCodes.addAll(schemaDisplayConfig.getDisplayMetadataCodes());
			searchMetadataCodes.addAll(schemaDisplayConfig.getSearchResultsMetadataCodes());
		} else if (viewMode == VIEW_MODE.SEARCH) {
			if (metadataCodes != null) {
				if (addMetadataCodes) {
					searchMetadataCodes.addAll(schemaDisplayConfig.getSearchResultsMetadataCodes());
				}
				searchMetadataCodes.addAll(metadataCodes);
			} else {
				searchMetadataCodes.addAll(schemaDisplayConfig.getSearchResultsMetadataCodes());
			}
			formMetadataCodes.addAll(schemaDisplayConfig.getFormMetadataCodes());
			displayMetadataCodes.addAll(schemaDisplayConfig.getDisplayMetadataCodes());
			tableMetadataCodes.addAll(schemaDisplayConfig.getTableMetadataCodes());
		} else {
			throw new IllegalArgumentException("Invalid view mode : " + viewMode);
		}

		MetadataToVOBuilder metadataToVOBuilder = newMetadataToVOBuilder();
		CollectionInfo collectionInfo = schema.getCollectionInfo();

		CollectionInfoVO collectionInfoVO = new CollectionInfoVO(collectionInfo.getMainSystemLanguage(), collectionInfo.getCode(), collectionInfo.getCollectionLanguages(),
				collectionInfo.getMainSystemLocale(), collectionInfo.getSecondaryCollectionLanguesCodes(), collectionInfo.getCollectionLanguesCodes(), collectionInfo.getCollectionLocales());
		MetadataSchemaVO schemaVO = new MetadataSchemaVO(code, collection, localCode, formMetadataCodes, displayMetadataCodes,
				tableMetadataCodes, searchMetadataCodes, labels, collectionInfoVO);

		if (!withoutBuildingMetadatas) {
			for (Metadata metadata : schema.getMetadatas()) {
				if (viewMode == VIEW_MODE.FORM && metadata.isMultiLingual()) {
					List<Locale> supportedLocales = schema.getCollectionInfo().getCollectionLocales();
					for (Locale supportedLocale : supportedLocales) {
						metadataToVOBuilder.build(metadata, supportedLocale, schemaVO, sessionContext);
					}
				} else {
					metadataToVOBuilder.build(metadata, schemaVO, sessionContext);
				}
			}
		}

		return schemaVO;
	}

	public MetadataSchemaVO build(MetadataSchema schema, VIEW_MODE viewMode, List<String> metadataCodes,
								  SessionContext sessionContext) {
		return build(schema, viewMode, metadataCodes, sessionContext, false);
	}

	protected MetadataToVOBuilder newMetadataToVOBuilder() {
		return new MetadataToVOBuilder();
	}

	public MetadataSchemaVO buildCommon(VIEW_MODE viewMode, SessionContext sessionContext) {
		MetadataSchemaVO schemaVO = new MetadataSchemaVO("null", sessionContext.getCurrentCollection(), new HashMap<>(), null);
		MetadataToVOBuilder metadataToVOBuilder = newMetadataToVOBuilder();
		MetadataVO titleMetadataVO = metadataToVOBuilder.build(Schemas.TITLE, sessionContext);
		MetadataVO modifiedOnMetadataVO = metadataToVOBuilder.build(Schemas.MODIFIED_ON, sessionContext);
		schemaVO.getMetadatas().add(titleMetadataVO);
		schemaVO.getMetadatas().add(modifiedOnMetadataVO);
		return schemaVO;
	}

}
