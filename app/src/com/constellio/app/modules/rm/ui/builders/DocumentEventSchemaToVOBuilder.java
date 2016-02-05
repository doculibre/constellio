package com.constellio.app.modules.rm.ui.builders;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.StructureFactory;

public class DocumentEventSchemaToVOBuilder extends MetadataSchemaToVOBuilder {

	@Override
	protected MetadataToVOBuilder newMetadataToVOBuilder() {
		return new MetadataToVOBuilder() {
			@Override
			protected MetadataVO newMetadataVO(String metadataCode, MetadataValueType type, String collection,
					MetadataSchemaVO schemaVO, boolean required, boolean multivalue, boolean readOnly,
					Map<Locale, String> labels, Class<? extends Enum<?>> enumClass, String[] taxonomyCodes,
					String schemaTypeCode, MetadataInputType metadataInputType, AllowedReferences allowedReferences,
					boolean enabled, StructureFactory structureFactory, String metadataGroup, Object defaultValue,
					boolean isWriteNullValues) {
				MetadataVO metadataVO;
				String modifiedOnCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(Schemas.MODIFIED_ON.getCode());
				String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
				if (modifiedOnCodeWithoutPrefix.equals(metadataCodeWithoutPrefix)) {
					String newLabel = $("DocumentEventSchemaToVOBuilder.modifiedOn");
					Map<Locale, String> newLabels = new HashMap<>();
					for (Locale labelLocale : labels.keySet()) {
						newLabels.put(labelLocale, newLabel);
					}
					metadataVO = super.newMetadataVO(metadataCode, type, collection, schemaVO, required, multivalue, readOnly,
							newLabels, enumClass, taxonomyCodes, schemaTypeCode, metadataInputType, allowedReferences, enabled,
							structureFactory, metadataGroup, defaultValue, isWriteNullValues);
				} else {
					metadataVO = super.newMetadataVO(metadataCode, type, collection, schemaVO, required, multivalue, readOnly,
							labels, enumClass, taxonomyCodes, schemaTypeCode, metadataInputType, allowedReferences, enabled,
							structureFactory, metadataGroup, defaultValue, isWriteNullValues);
				}
				return metadataVO;
			}
		};
	}

}
