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

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;

@SuppressWarnings("serial")
public class SchemaTypeVODataProvider implements Serializable {

	transient MetadataSchemasManager schemasManager;

	transient List<MetadataSchemaTypeVO> schemaTypes;

	MetadataSchemaTypeToVOBuilder voBuilder;
	String collection;

	public SchemaTypeVODataProvider(MetadataSchemaTypeToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory,
			String collection) {
		this.voBuilder = voBuilder;
		this.collection = collection;
		init(modelLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory());
	}

	void init(ModelLayerFactory modelLayerFactory) {
		schemasManager = modelLayerFactory.getMetadataSchemasManager();
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
			for (MetadataSchemaType type : types.getSchemaTypes()) {
				typeVOs.add(voBuilder.build(type));
			}
		}

		return typeVOs;
	}

	public void sort(String[] propertyId, boolean[] ascending) {
	}
}
