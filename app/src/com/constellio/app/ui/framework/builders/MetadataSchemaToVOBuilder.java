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
package com.constellio.app.ui.framework.builders;

import java.io.Serializable;
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
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;

@SuppressWarnings("serial")
public class MetadataSchemaToVOBuilder implements Serializable {

	static final List<String> DISPLAYED_SYSTEM_RESERVED_METADATA_CODES = Arrays.asList(
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
		if (metadataCodes == null) {
			if (viewMode == VIEW_MODE.FORM) {
				metadataCodes = schemaDisplayConfig.getFormMetadataCodes();
			} else if (viewMode == VIEW_MODE.DISPLAY) {
				metadataCodes = schemaDisplayConfig.getDisplayMetadataCodes();
			} else if (viewMode == VIEW_MODE.TABLE) {
				metadataCodes = schemaDisplayConfig.getSearchResultsMetadataCodes();
			} else {
				throw new IllegalArgumentException("Invalid view mode : " + viewMode);
			}
		}

		MetadataToVOBuilder metadataToVOBuilder = newMetadataToVOBuilder();
		MetadataSchemaVO schemaVO = new MetadataSchemaVO(code, collection, labels);
		for (String metadataCode : metadataCodes) {
			Metadata metadata = schema.getMetadata(metadataCode);
			boolean systemReserved = metadata.isSystemReserved();
			boolean ignored;
			if (viewMode == VIEW_MODE.FORM) {
				ignored = systemReserved;
			} else if (!systemReserved) {
				ignored = false;
			} else {
				String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
				ignored = !DISPLAYED_SYSTEM_RESERVED_METADATA_CODES.contains(metadataCodeWithoutPrefix);
			}
			if (!ignored && metadata.isEnabled()) {
				metadataToVOBuilder.build(metadata, schemaVO, sessionContext);
			}
		}

		return schemaVO;
	}

	protected MetadataToVOBuilder newMetadataToVOBuilder() {
		return new MetadataToVOBuilder();
	}

}
