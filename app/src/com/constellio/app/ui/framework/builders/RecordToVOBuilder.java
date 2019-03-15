package com.constellio.app.ui.framework.builders;

import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("serial")
public class RecordToVOBuilder implements Serializable {

	public RecordVO build(Record record, VIEW_MODE viewMode, SessionContext sessionContext) {
		return build(record, viewMode, null, sessionContext);
	}


	@SuppressWarnings("unchecked")
	public RecordVO build(Record record, VIEW_MODE viewMode, MetadataSchemaVO schemaVO, SessionContext sessionContext) {
		String id = record.getId();
		String schemaCode = record.getSchemaCode();
		String collection = record.getCollection();
		boolean saved = record.isSaved();
		List<String> metadataCodeExcludedList = new ArrayList<>();

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchema schema = metadataSchemasManager.getSchemaTypes(collection).getSchema(schemaCode);
		UserServices userServices = modelLayerFactory.newUserServices();

		User user = null;

		if (sessionContext.getCurrentUser() != null && sessionContext.getCurrentCollection() != null) {
			user = userServices.getUserInCollection(sessionContext.getCurrentUser().getUsername(), sessionContext.getCurrentCollection());
		}

		if (schemaVO == null) {
			schemaVO = new MetadataSchemaToVOBuilder().build(schema, viewMode, sessionContext);
		}

		ContentVersionToVOBuilder contentVersionVOBuilder = new ContentVersionToVOBuilder(modelLayerFactory);

		List<MetadataValueVO> metadataValueVOs = new ArrayList<MetadataValueVO>();
		List<MetadataVO> metadatas = schemaVO.getMetadatas();
		for (MetadataVO metadataVO : metadatas) {
			String metadataCode = metadataVO.getCode();
			Metadata metadata = schema.getMetadata(metadataCode);
			Object recordVOValue;

			if (metadata.isMultiLingual() && metadataVO.getLocale() != null) {
				recordVOValue = record.get(metadata, metadataVO.getLocale());
			} else {
				recordVOValue = record.get(metadata, sessionContext.getCurrentLocale());
			}

			if (recordVOValue instanceof Content) {
				recordVOValue = contentVersionVOBuilder.build((Content) recordVOValue, sessionContext);
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
			if (user == null || user.hasAccessToMetadata(metadata, record) || viewMode == VIEW_MODE.FORM && metadataVO
					.isRequired()) {
				metadataValueVOs.add(metadataValueVO);
			} else {
				metadataCodeExcludedList.add(metadataVO.getCode());
			}

		}

		RecordVO recordVO = newRecordVO(id, metadataValueVOs, viewMode, metadataCodeExcludedList);
		recordVO.setSaved(saved);
		recordVO.setRecord(record);
		if (recordVO.getSchema() != null) {
			BuildRecordVOParams buildRecordVOParams = new BuildRecordVOParams(record, recordVO);
			constellioFactories.getAppLayerFactory().getExtensions()
					.forCollection(record.getCollection()).buildRecordVO(buildRecordVOParams);
			recordVO.setRecord(record);
		} else {
			recordVO = null;
		}

		return recordVO;
	}

	protected Object getValue(Record record, Metadata metadata) {
		return record.get(metadata);
	}

	protected RecordVO newRecordVO(String id, List<MetadataValueVO> metadataValueVOs, VIEW_MODE viewMode,
								   List<String> excludedMetadata) {
		return new RecordVO(id, metadataValueVOs, viewMode, excludedMetadata);
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
