package com.constellio.app.ui.framework.builders;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.StructureFactory;

public class EventSchemaToVOBuilder extends MetadataSchemaToVOBuilder {

	@Override
	protected MetadataToVOBuilder newMetadataToVOBuilder() {
		return new MetadataToVOBuilder() {
			@Override
			protected MetadataVO newMetadataVO(String metadataCode, String datastoreCode, MetadataValueType type,
					String collection, MetadataSchemaVO schemaVO, boolean required, boolean multivalue,
					boolean readOnly, boolean unmodifiable, Map<Locale, String> labels,
					Class<? extends Enum<?>> enumClass, String[] taxonomyCodes, String schemaTypeCode,
					MetadataInputType metadataInputType,
					MetadataDisplayType metadataDisplayType, AllowedReferences allowedReferences, boolean enabled,
					StructureFactory structureFactory, String metadataGroup, Object defaultValue, String inputMask, Set<String> customAttributes) {
				MetadataVO metadataVO;
				String modifiedOnCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(Schemas.MODIFIED_ON.getCode());
				String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
				if (modifiedOnCodeWithoutPrefix.equals(metadataCodeWithoutPrefix)) {
					String newLabel = $("EventSchemaToVOBuilder.modifiedOn");
					Map<Locale, String> newLabels = new HashMap<>();
					for (Locale labelLocale : labels.keySet()) {
						newLabels.put(labelLocale, newLabel);
					}
					metadataVO = super
							.newMetadataVO(metadataCode, datastoreCode, type, collection, schemaVO, required, multivalue,
									readOnly,
									unmodifiable, labels, enumClass, taxonomyCodes, schemaTypeCode, metadataInputType,
									metadataDisplayType, allowedReferences,
									enabled, structureFactory, metadataGroup, defaultValue, inputMask, customAttributes);
				} else {
					metadataVO = super
							.newMetadataVO(metadataCode, datastoreCode, type, collection, schemaVO, required, multivalue,
									readOnly,
									unmodifiable, labels, enumClass, taxonomyCodes, schemaTypeCode, metadataInputType,
									metadataDisplayType, allowedReferences,
									enabled, structureFactory, metadataGroup, defaultValue, inputMask, customAttributes);
				}
				return metadataVO;
			}
		};
	}

}
