package com.constellio.app.ui.framework.builders;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.CollectionInfoVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("serial")
public class MetadataToVOBuilder implements Serializable {

	@Deprecated
	public MetadataVO build(Metadata metadata) {
		return build(metadata, (MetadataSchemaVO) null);
	}

	public MetadataVO build(Metadata metadata, SessionContext sessionContext) {
		return build(metadata, null, sessionContext);
	}

	@Deprecated
	public MetadataVO build(Metadata metadata, MetadataSchemaVO schemaVO) {
		return build(metadata, schemaVO, ConstellioUI.getCurrentSessionContext());
	}

	public MetadataVO build(Metadata metadata, MetadataSchemaVO schemaVO, SessionContext sessionContext) {
		return build(metadata, null, schemaVO, sessionContext);
	}

	public MetadataVO build(Metadata metadata, Locale locale, MetadataSchemaVO schemaVO,
							SessionContext sessionContext) {
		String metadataCode = metadata.getCode();
		String metadataLocalCode = metadata.getLocalCode();
		String collection = metadata.getCollection();
		String datastoreCode = metadata.getDataStoreCode();
		MetadataValueType type = metadata.getType();

		boolean required = metadata.isDefaultRequirement();
		boolean multivalue = metadata.isMultivalue();
		boolean readOnly = false;
		boolean unmodifiable = metadata.isUnmodifiable();
		boolean enabled = metadata.isEnabled();
		boolean isMultiLingual = metadata.isMultiLingual();
		boolean sortable = metadata.isSortable();

		StructureFactory structureFactory = metadata.getStructureFactory();

		String schemaTypeCode;
		if (type == MetadataValueType.REFERENCE) {
			schemaTypeCode = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
		} else {
			schemaTypeCode = null;
		}

		String[] taxonomyCodes;
		MetadataInputType metadataInputType;
		MetadataDisplayType metadataDisplayType;
		String metadataGroup;

		if (collection != null) {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			if (schemaTypeCode != null) {
				//				SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
				UserVO userVO = sessionContext.getCurrentUser();

				ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
				MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
				TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
				UserServices userServices = modelLayerFactory.newUserServices();

				if (userVO != null) {
					User user = userServices.getUserInCollection(userVO.getUsername(), collection);
					List<Taxonomy> taxonomies = taxonomiesManager
							.getAvailableTaxonomiesForSelectionOfType(schemaTypeCode, user, metadataSchemasManager);
					taxonomyCodes = new String[taxonomies.size()];
					for (int i = 0; i < taxonomies.size(); i++) {
						Taxonomy taxonomy = taxonomies.get(i);
						taxonomyCodes[i] = taxonomy.getCode();
					}
				} else {
					taxonomyCodes = new String[0];
				}
			} else {
				taxonomyCodes = new String[0];
			}

			AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
			SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
			MetadataDisplayConfig metadataDisplayConfig = schemasDisplayManager.getMetadata(collection, metadataCode);

			metadataInputType = metadataDisplayConfig.getInputType();
			metadataDisplayType = metadataDisplayConfig.getDisplayType();

			Language language = Language.withCode(sessionContext.getCurrentLocale().getLanguage());
			metadataGroup = metadataDisplayConfig.getMetadataGroupCode();
			String typeCode = metadata.getSchemaTypeCode();
			Map<String, Map<Language, String>> groups = schemasDisplayManager.getType(collection, typeCode)
					.getMetadataGroup();
			if (StringUtils.isBlank(metadataGroup)) {
				if (groups.keySet().isEmpty()) {
					metadataGroup = null;
				} else {
					metadataGroup = groups.entrySet().iterator().next().getValue().get(language);
					for (Map.Entry<String, Map<Language, String>> entry : groups.entrySet()) {
						if (entry.getKey().startsWith("default")) {
							metadataGroup = entry.getValue().get(language);
						}
					}
				}
			} else if (groups.get(metadataGroup) != null && !metadataGroup.equals(groups.get(metadataGroup).get(language))) {
				metadataGroup = groups.get(metadataGroup).get(language);
			}
		} else {
			taxonomyCodes = new String[0];
			metadataInputType = null;
			metadataDisplayType = null;
			metadataGroup = null;
		}

		Map<Locale, String> labels = new HashMap<Locale, String>();

		//TODO FIXME Vincent est-ce necessaire?
		for (Language keyset : metadata.getLabels().keySet()) {
			labels.put(keyset.getLocale(), metadata.getLabels().get(keyset));
		}


		//labels.put(sessionContext.getCurrentLocale(), metadata.getLabel(
		//		Language.withCode(sessionContext.getCurrentLocale().getLanguage())));
		Locale currentLocale = sessionContext.getCurrentLocale();
		String metadataLabel = metadata.getLabel(Language.withCode(currentLocale.getLanguage()));

		if (schemaVO != null) {
			CollectionInfoVO collectionInfoVO = schemaVO.getCollectionInfoVO();

			if (isMultiLingual && locale != null && collectionInfoVO.getCollectionLanguages() != null && collectionInfoVO.getCollectionLanguages().size() > 1) {
				metadataLabel += " (" + locale.getLanguage().toUpperCase() + ")";
			}
		}
		labels.put(currentLocale, metadataLabel);

		Class<? extends Enum<?>> enumClass = metadata.getEnumClass(); // EnumWithSmallCode
		AllowedReferences allowedReferences = metadata.getAllowedReferences();

		return newMetadataVO(metadata.getId(), metadataCode, metadataLocalCode, datastoreCode, type, collection,
				schemaVO, required, multivalue, readOnly, unmodifiable,
				labels, enumClass, taxonomyCodes, schemaTypeCode, metadataInputType, metadataDisplayType, allowedReferences,
				enabled,
				structureFactory, metadataGroup, metadata.getDefaultValue(), metadata.getInputMask(),
				metadata.getCustomAttributes(), isMultiLingual, locale, metadata.getCustomParameter(),
				schemaVO != null ? schemaVO.getCollectionInfoVO() : null, sortable);
	}

	protected MetadataVO newMetadataVO(
			short id,
			String metadataCode,
			String metadataLocalCode,
			String datastoreCode,
			MetadataValueType type,
			String collection,
			MetadataSchemaVO schemaVO,
			boolean required,
			boolean multivalue,
			boolean readOnly,
			boolean unmodifiable,
			Map<Locale, String> labels,
			Class<? extends Enum<?>> enumClass,
			String[] taxonomyCodes,
			String schemaTypeCode,
			MetadataInputType metadataInputType,
			MetadataDisplayType metadataDisplayType,
			AllowedReferences allowedReferences,
			boolean enabled,
			StructureFactory structureFactory,
			String metadataGroup,
			Object defaultValue,
			String inputMask, Set<String> customAttributes,
			boolean isMultiLingual, Locale locale,
			Map<String, Object> customParameters,
			CollectionInfoVO collectionInfoVO, boolean sortable) {
		return new MetadataVO(id, metadataCode, metadataLocalCode, datastoreCode, type, collection, schemaVO, required, multivalue, readOnly, unmodifiable,
				labels, enumClass, taxonomyCodes, schemaTypeCode, metadataInputType, metadataDisplayType, allowedReferences,
				enabled,
				structureFactory, metadataGroup, defaultValue, inputMask, customAttributes, isMultiLingual, locale, customParameters, collectionInfoVO, sortable, false);
	}

}
