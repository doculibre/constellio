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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;

@SuppressWarnings("serial")
public class RecordToVOBuilder implements Serializable {

	@Deprecated
	public RecordVO build(Record record, VIEW_MODE viewMode) {
		return build(record, viewMode, null, ConstellioUI.getCurrentSessionContext());
	}

	@Deprecated
	public RecordVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO) {
		return build(record, viewMode, schemaVO, ConstellioUI.getCurrentSessionContext());
	}

	public RecordVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return build(record, viewMode, null, sessionContext);
	}

	@SuppressWarnings("unchecked")
	public RecordVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO, SessionContext sessionContext) {
		String id = record.getId();
		String schemaCode = record.getSchemaCode();
		String collection = record.getCollection();

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchema schema = metadataSchemasManager.getSchemaTypes(collection).getSchema(schemaCode);

		if (schemaVO == null) {
			schemaVO = new MetadataSchemaToVOBuilder().build(schema, viewMode, sessionContext);
		}

		ContentVersionToVOBuilder contentVersionVOBuilder = new ContentVersionToVOBuilder();

		List<MetadataValueVO> metadataValueVOs = new ArrayList<MetadataValueVO>();
		for (MetadataVO metadataVO : schemaVO.getMetadatas()) {
			String metadataCode = metadataVO.getCode();
			Metadata metadata = schema.getMetadata(metadataCode);

			Object recordVOValue = record.get(metadata);
			if (recordVOValue instanceof Content) {
				recordVOValue = contentVersionVOBuilder.build((Content) recordVOValue);
			} else if (recordVOValue instanceof List) {
				List<Object> listRecordVOValue = new ArrayList<Object>();
				List<Object> listRecordValue = (List<Object>) recordVOValue;
				for (Iterator<Object> it = listRecordValue.iterator(); it.hasNext(); ) {
					Object listVOValueElement = it.next();
					if (listVOValueElement instanceof Content) {
						listVOValueElement = contentVersionVOBuilder.build((Content) listVOValueElement);
					}
					if (listVOValueElement != null) {
						listRecordVOValue.add(listVOValueElement);
					}
				}
				recordVOValue = listRecordVOValue;
			}
			MetadataValueVO metadataValueVO = new MetadataValueVO(metadataVO, recordVOValue);
			metadataValueVOs.add(metadataValueVO);
		}

		return newRecordVO(id, metadataValueVOs, viewMode);
	}

	protected RecordVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode) {
		return new RecordVO(id, metadataValueVOs, viewMode);
	}
	
	protected int getIndexOfMetadataCode(String metadataCode, List<MetadataValueVO> metadataValueVOs) {
		int index = -1;
		for (int i = 0; i < metadataValueVOs.size(); i++) {
			MetadataValueVO metadataValueVO = metadataValueVOs.get(i);
			MetadataVO metadataVO = metadataValueVO.getMetadata();
			if (metadataVO.codeMatches(metadataCode)) {
				index = i;
				break;
			}
		}
		return index;
	}

}
