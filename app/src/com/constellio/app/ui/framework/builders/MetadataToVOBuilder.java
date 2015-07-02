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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;

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
		String metadataCode = metadata.getCode();
		String collection = metadata.getCollection();
		MetadataValueType type = metadata.getType();

		boolean required = metadata.isDefaultRequirement();
		boolean multivalue = metadata.isMultivalue();
		boolean readOnly = false;
		boolean enabled = metadata.isEnabled();
		StructureFactory structureFactory = metadata.getStructureFactory();

		String schemaTypeCode;
		if (type == MetadataValueType.REFERENCE) {
			schemaTypeCode = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
		} else {
			schemaTypeCode = null;
		}

		String[] taxonomyCodes;
		MetadataInputType metadataInputType;
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
			metadataGroup = metadataDisplayConfig.getMetadataGroup();

			if (StringUtils.isBlank(metadataGroup)) {
				String typeCode = new SchemaUtils().getSchemaTypeCode(metadataCode);
				List<String> groups = schemasDisplayManager.getType(collection, typeCode).getMetadataGroup();
				metadataGroup = groups.isEmpty() ? null : groups.get(0);
			}
		} else {
			taxonomyCodes = new String[0];
			metadataInputType = null;
			metadataGroup = null;
		}

		Map<Locale, String> labels = new HashMap<Locale, String>();
		labels.put(sessionContext.getCurrentLocale(), metadata.getLabel());
		Class<? extends Enum<?>> enumClass = metadata.getEnumClass(); // EnumWithSmallCode
		AllowedReferences allowedReferences = metadata.getAllowedReferences();

		return newMetadataVO(metadataCode, type, collection, schemaVO, required, multivalue, readOnly, labels, enumClass,
				taxonomyCodes, schemaTypeCode, metadataInputType, allowedReferences, enabled, structureFactory,
				metadataGroup, metadata.getDefaultValue());
	}

	protected MetadataVO newMetadataVO(
			String metadataCode,
			MetadataValueType type,
			String collection,
			MetadataSchemaVO schemaVO,
			boolean required,
			boolean multivalue,
			boolean readOnly,
			Map<Locale, String> labels,
			Class<? extends Enum<?>> enumClass,
			String[] taxonomyCodes,
			String schemaTypeCode,
			MetadataInputType metadataInputType,
			AllowedReferences allowedReferences,
			boolean enabled,
			StructureFactory structureFactory,
			String metadataGroup,
			Object defaultValue) {
		return new MetadataVO(metadataCode, type, collection, schemaVO, required, multivalue, readOnly, labels, enumClass,
				taxonomyCodes, schemaTypeCode, metadataInputType, allowedReferences, enabled, structureFactory,
				metadataGroup, defaultValue);
	}

}
