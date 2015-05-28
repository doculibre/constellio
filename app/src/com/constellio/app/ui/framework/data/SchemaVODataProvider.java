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
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;

@SuppressWarnings("serial")
public class SchemaVODataProvider implements Serializable {

	transient MetadataSchemasManager schemasManager;

	transient List<MetadataSchemaVO> schemaTypes;

	MetadataSchemaToVOBuilder voBuilder;

	String typeCode;

	String collection;

	SessionContext sessionContext;

	@Deprecated
	public SchemaVODataProvider(MetadataSchemaToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory, String collection,
			String code) {
		this.voBuilder = voBuilder;
		this.collection = collection;
		this.typeCode = code;
		this.sessionContext = ConstellioUI.getCurrentSessionContext();
		init(modelLayerFactory);
	}

	public SchemaVODataProvider(MetadataSchemaToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory, String collection,
			String code, SessionContext sessionContext) {
		this.voBuilder = voBuilder;
		this.collection = collection;
		this.typeCode = code;
		this.sessionContext = sessionContext;
		init(modelLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory());
	}

	void init(ModelLayerFactory modelLayerFactory) {
		schemasManager = modelLayerFactory.getMetadataSchemasManager();
		schemaTypes = listSchemaVO();
	}

	public MetadataSchemaVO getSchemaVO(String code) {
		for (MetadataSchemaVO type : schemaTypes) {
			if (type.getCode().equals(code)) {
				return type;
			}
		}
		return null;
	}

	public MetadataSchemaVO getSchemaVO(Integer index) {
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

	public List<MetadataSchemaVO> listSchemaVO() {
		List<MetadataSchemaVO> schemaVOs = new ArrayList<>();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
		List<MetadataSchema> schemas = new ArrayList<>();
		if (types != null) {
			for (MetadataSchemaType type : types.getSchemaTypes()) {
				if (type.getCode().equals(typeCode)) {
					schemas = type.getAllSchemas();
					break;
				}
			}

			for (MetadataSchema metadata : schemas) {
				schemaVOs.add(voBuilder.build(metadata, VIEW_MODE.TABLE, sessionContext));
			}
		}

		return schemaVOs;
	}

	public List<MetadataSchemaVO> listSchemaVO(int startIndex, int count) {
		List<MetadataSchemaVO> schemaVOs = listSchemaVO();
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
