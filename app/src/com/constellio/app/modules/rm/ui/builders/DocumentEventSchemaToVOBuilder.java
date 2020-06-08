package com.constellio.app.modules.rm.ui.builders;

import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataSortingType;
import com.constellio.app.ui.entities.CollectionInfoVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.StructureFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public class DocumentEventSchemaToVOBuilder extends MetadataSchemaToVOBuilder {

	@Override
	protected MetadataToVOBuilder newMetadataToVOBuilder() {
		return new MetadataToVOBuilder() {
			@Override
			protected MetadataVO newMetadataVO(short id, String metadataCode, String metadataLocalCode,
											   String datastoreCode,
											   MetadataValueType type,
											   String collection, MetadataSchemaVO schemaVO, boolean required,
											   boolean multivalue,
											   boolean readOnly, boolean unmodifiable, Map<Locale, String> labels,
											   Class<? extends Enum<?>> enumClass, String[] taxonomyCodes,
											   String schemaTypeCode,
											   MetadataInputType metadataInputType,
											   MetadataDisplayType metadataDisplayType,
											   MetadataSortingType metadataSortingType,
											   AllowedReferences allowedReferences, boolean enabled,
											   StructureFactory structureFactory, String metadataGroup,
											   Object defaultValue, String inputMask,
											   Set<String> customAttributes, boolean isMultiLingual, Locale locale,
											   Map<String, Object> customParameters,
											   CollectionInfoVO collectionInfoVO, boolean sortable) {
				MetadataVO metadataVO;
				String modifiedOnCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(Schemas.MODIFIED_ON.getCode());
				String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
				if (modifiedOnCodeWithoutPrefix.equals(metadataCodeWithoutPrefix)) {
					String newLabel = $("DocumentEventSchemaToVOBuilder.modifiedOn");
					Map<Locale, String> newLabels = new HashMap<>();
					for (Locale labelLocale : labels.keySet()) {
						newLabels.put(labelLocale, newLabel);
					}
					metadataVO = super
							.newMetadataVO(id, metadataCode, metadataLocalCode, datastoreCode, type, collection, schemaVO, required, multivalue,
									readOnly,
									unmodifiable, labels, enumClass, taxonomyCodes, schemaTypeCode, metadataInputType,
									metadataDisplayType, metadataSortingType, allowedReferences,
									enabled, structureFactory, metadataGroup, defaultValue, inputMask, customAttributes,
									isMultiLingual, locale, customParameters, collectionInfoVO, sortable);
				} else {
					metadataVO = super
							.newMetadataVO(id, metadataCode, metadataLocalCode, datastoreCode, type, collection, schemaVO, required, multivalue,
									readOnly,
									unmodifiable, labels, enumClass, taxonomyCodes, schemaTypeCode, metadataInputType,
									metadataDisplayType, metadataSortingType, allowedReferences,
									enabled, structureFactory, metadataGroup, defaultValue, inputMask, customAttributes,
									isMultiLingual, locale, customParameters, collectionInfoVO, sortable);
				}
				return metadataVO;
			}
		};
	}

}
