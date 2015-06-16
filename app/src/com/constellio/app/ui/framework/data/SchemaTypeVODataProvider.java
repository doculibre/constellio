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
package com.constellio.app.ui.framework.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.api.extensions.SchemaTypeAccessExtension;
import com.constellio.app.extensions.AppLayerCollectionEventsListeners;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils;
import com.constellio.data.frameworks.extensions.ExtensionUtils.BehaviorCaller;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

@SuppressWarnings("serial")
public class SchemaTypeVODataProvider implements Serializable {

	transient MetadataSchemasManager schemasManager;

	transient TaxonomiesManager taxonomiesManager;

	transient List<MetadataSchemaTypeVO> schemaTypes;

	transient AppLayerCollectionEventsListeners extensions;

	MetadataSchemaTypeToVOBuilder voBuilder;
	String collection;

	public SchemaTypeVODataProvider(MetadataSchemaTypeToVOBuilder voBuilder, AppLayerFactory appLayerFactory,
			String collection) {
		this.voBuilder = voBuilder;
		this.collection = collection;
		init(appLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getAppLayerFactory());
	}

	void init(AppLayerFactory appLayerFactory) {
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		schemasManager = modelLayerFactory.getMetadataSchemasManager();
		taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		extensions = appLayerFactory.getExtensions().getCollectionListeners(collection);
		schemaTypes = listSchemaTypeVO();

	}

	public MetadataSchemaTypeVO getSchemaTypeVO(String code) {
		for (MetadataSchemaTypeVO type : schemaTypes) {
			if (type.getCode().equals(code)) {
				return type;
			}
		}
		return null;
	}

	public MetadataSchemaTypeVO getSchemaTypeVO(Integer index) {
		return schemaTypes.get(index);
	}

	public int size() {
		return schemaTypes.size();
	}

	public List<Integer> list() {
		List<Integer> listInt = new ArrayList<>();
		for (int i = 0; i < schemaTypes.size(); i++) {
			listInt.add(i);
		}

		return listInt;
	}

	public List<MetadataSchemaTypeVO> listSchemaTypeVO() {
		List<MetadataSchemaTypeVO> typeVOs = new ArrayList<>();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
		if (types != null) {
			for (final MetadataSchemaType type : types.getSchemaTypes()) {

				boolean visible = false;
				Taxonomy taxonomy = taxonomiesManager.getTaxonomyFor(type.getCollection(), type.getCode());
				if (taxonomy != null) {
					visible = true;
					//!taxonomy.hasSameCode(taxonomiesManager.getPrincipalTaxonomy(type.getCollection()));

				} else if (type.hasSecurity() || type.getCode().startsWith("ddv")) {
					visible = true;

				} else if (type.getCode().equals(RetentionRule.SCHEMA_TYPE)) {
					visible = true;
				}

				visible = ExtensionUtils.getBooleanValue(extensions.schemaTypeAccessExtensions, visible,
						new BehaviorCaller<SchemaTypeAccessExtension, ExtensionBooleanResult>() {
							@Override
							public ExtensionBooleanResult call(SchemaTypeAccessExtension behavior) {
								return behavior.isSchemaTypeConfigurable(type);
							}
						});

				if (visible) {
					typeVOs.add(voBuilder.build(type));
				}
			}
		}

		return typeVOs;
	}

	public void sort(String[] propertyId, boolean[] ascending) {
	}
}
