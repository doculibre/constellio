package com.constellio.app.ui.framework.builders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;

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
			SessionContext sessionContext) {
		String code = schema.getCode();
		String collection = schema.getCollection();

		Map<Locale, String> labels = new HashMap<Locale, String>();
		labels.put(sessionContext.getCurrentLocale(), schema.getLabel());

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
				formMetadataCodes.addAll(metadataCodes);
			} else {
				formMetadataCodes.addAll(schemaDisplayConfig.getFormMetadataCodes());
			}
			displayMetadataCodes.addAll(schemaDisplayConfig.getDisplayMetadataCodes());
			searchMetadataCodes.addAll(schemaDisplayConfig.getSearchResultsMetadataCodes());
			tableMetadataCodes.addAll(schemaDisplayConfig.getTableMetadataCodes());
		} else if (viewMode == VIEW_MODE.DISPLAY) {
			if (metadataCodes != null) {
				displayMetadataCodes.addAll(metadataCodes);
			} else {
				displayMetadataCodes.addAll(schemaDisplayConfig.getDisplayMetadataCodes());
			}
			formMetadataCodes.addAll(schemaDisplayConfig.getFormMetadataCodes());
			searchMetadataCodes.addAll(schemaDisplayConfig.getSearchResultsMetadataCodes());
			tableMetadataCodes.addAll(schemaDisplayConfig.getTableMetadataCodes());
		} else if (viewMode == VIEW_MODE.TABLE) {
			if (metadataCodes != null) {
				tableMetadataCodes.addAll(metadataCodes);
			} else {
				tableMetadataCodes.addAll(schemaDisplayConfig.getTableMetadataCodes());
			}
			formMetadataCodes.addAll(schemaDisplayConfig.getFormMetadataCodes());
			displayMetadataCodes.addAll(schemaDisplayConfig.getDisplayMetadataCodes());
			searchMetadataCodes.addAll(schemaDisplayConfig.getSearchResultsMetadataCodes());
		} else if (viewMode == VIEW_MODE.SEARCH) {
			if (metadataCodes != null) {
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
		MetadataSchemaVO schemaVO = new MetadataSchemaVO(code, collection, formMetadataCodes, displayMetadataCodes,
				tableMetadataCodes, searchMetadataCodes, labels);
		for (Metadata metadata : schema.getMetadatas()) {
			//			String metadataCode = metadata.getCode();
			//			boolean systemReserved = metadata.isSystemReserved();
			//			boolean ignored;
			//			if (viewMode == VIEW_MODE.FORM) {
			//				ignored = systemReserved;
			//			} else if (!systemReserved) {
			//				ignored = false;
			//			} else {
			//				String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
			//				ignored = !DISPLAYED_SYSTEM_RESERVED_METADATA_CODES.contains(metadataCodeWithoutPrefix);
			//			}
			//			if (!ignored && metadata.isEnabled()) {
			metadataToVOBuilder.build(metadata, schemaVO, sessionContext);
			//			}
		}

		return schemaVO;
	}

	protected MetadataToVOBuilder newMetadataToVOBuilder() {
		return new MetadataToVOBuilder();
	}

}
