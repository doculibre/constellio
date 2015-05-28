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
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;

@SuppressWarnings("serial")
public class MetadataVODataProvider implements Serializable {

	transient MetadataSchemasManager schemasManager;

	transient List<MetadataVO> metadataVOs;

	MetadataToVOBuilder voBuilder;

	String schemaCode;

	String collection;

	public MetadataVODataProvider(MetadataToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory, String collection,
			String code) {
		this.voBuilder = voBuilder;
		this.collection = collection;
		this.schemaCode = code;
		init(modelLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory());
	}

	void init(ModelLayerFactory modelLayerFactory) {
		schemasManager = modelLayerFactory.getMetadataSchemasManager();
		metadataVOs = listMetadataVO();
	}

	public MetadataVO getMetadataVO(String code) {
		for (MetadataVO type : metadataVOs) {
			if (type.getCode().equals(code)) {
				return type;
			}
		}
		return null;
	}

	public MetadataVO getMetadataVO(Integer index) {
		return metadataVOs.get(index);
	}

	public int size() {
		return metadataVOs.size();
	}

	public List<Integer> list() {
		List<Integer> listInt = new ArrayList<>();
		for (int i = 0; i < metadataVOs.size(); i++) {
			listInt.add(i);
		}

		return listInt;
	}

	public List<MetadataVO> listMetadataVO() {
		List<MetadataVO> schemaVOs = new ArrayList<>();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
		if (types != null) {
			MetadataSchema schema = types.getSchema(schemaCode);
			for (Metadata meta : schema.getMetadatas()) {
				if (!meta.isSystemReserved()) {
					schemaVOs.add(voBuilder.build(meta));
				}
			}
		}

		return schemaVOs;
	}

	public List<MetadataVO> listMetadataVO(int startIndex, int count) {
		List<MetadataVO> schemaVOs = listMetadataVO();
		int toIndex = startIndex + count;
		List subList = new ArrayList();
		if (startIndex > schemaVOs.size()) {
			return subList;
		} else if (toIndex > schemaVOs.size()) {
			toIndex = schemaVOs.size();
		}
		return schemaVOs.subList(startIndex, toIndex);
	}

	public void sort(String[] propertyId, boolean[] ascending) {
	}
}
