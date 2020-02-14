package com.constellio.app.ui.framework.builders;

import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.extensions.records.params.IsMetadataSpecialCaseToNotBeShownParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordAutomaticMetadataServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.users.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.entities.RecordVO.VIEW_MODE.FORM;

@SuppressWarnings("serial")
public class RecordToVOBuilder implements Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordToVOBuilder.class);

	Map<String, MetadataSchemaVO> cachedSchemaVOs = new HashMap<>(1);
	Map<String, User> cachedUsers = new HashMap<>(1);


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
		Set<String> metadataFormExcludedCodes = new HashSet<>();

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchema schema = metadataSchemasManager.getSchemaTypes(collection).getSchema(schemaCode);

		RecordAutomaticMetadataServices recordAutomaticMetadataServices =
				new RecordAutomaticMetadataServices(modelLayerFactory);


		User user = null;
		if (sessionContext.getCurrentUser() != null && sessionContext.getCurrentCollection() != null) {
			String userCacheKey = sessionContext.getCurrentUser() + "@" + sessionContext.getCurrentCollection();
			user = cachedUsers.get(userCacheKey);

			if (user == null) {
				UserServices userServices = modelLayerFactory.newUserServices();
				user = userServices.getUserInCollection(sessionContext.getCurrentUser().getUsername(), sessionContext.getCurrentCollection());
				cachedUsers.put(userCacheKey, user);
			}
		}

		String cacheKey = record.getCollection() + ":" + record.getSchemaCode() + ":" + viewMode.name();
		if (schemaVO == null) {
			schemaVO = cachedSchemaVOs.get(cacheKey);
		}

		if (schemaVO == null) {
			schemaVO = new MetadataSchemaToVOBuilder().build(schema, viewMode, sessionContext);
			cachedSchemaVOs.put(cacheKey, schemaVO);
		}

		ContentVersionToVOBuilder contentVersionVOBuilder = new ContentVersionToVOBuilder(modelLayerFactory);

		List<MetadataValueVO> metadataValueVOs = new ArrayList<MetadataValueVO>();
		List<MetadataVO> metadatas = schemaVO.getMetadatas();
		for (MetadataVO metadataVO : metadatas) {
			String metadataCode = metadataVO.getCode();

			// Recreated in newRecordVO override
			if (metadataVO.isSynthetic()) {
				continue;
			}
			Metadata metadata;
			if (metadataVO.getId() != 0) {
				metadata = schema.getSchemaType().getMetadataById(metadataVO.getId());

			} else {
				metadata = schema.getSchemaType().getMetadata(metadataCode);
			}

			if (record.getRecordDTO() != null &&
				record.getLoadedFieldsMode() == RecordDTOMode.SUMMARY && !SchemaUtils.isSummary(metadata)) {
				continue;
			}

			Object recordVOValue;
			try {
				if (metadata.isMultiLingual() && metadataVO.getLocale() != null) {
					recordVOValue = record.get(metadata, metadataVO.getLocale());
				} else {
					recordVOValue = record.get(metadata, sessionContext.getCurrentLocale());

				}
			} catch (IllegalArgumentException e) {
				if (viewMode == VIEW_MODE.DISPLAY || viewMode == FORM) {
					throw new RuntimeException("Could not load metadata '" + metadata + "'", e);

				} else {
					recordVOValue = metadata.isMultivalue() ? Collections.emptyList() : null;
				}
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
			if ((user == null || user.hasAccessToMetadata(metadata, record) || viewMode == VIEW_MODE.FORM && metadataVO
					.isRequired()) && !isMetadataSpecialCaseToNotBeShown(
					constellioFactories.getAppLayerFactory(), metadataVO, record)) {
				metadataValueVOs.add(metadataValueVO);
			} else {
				metadataCodeExcludedList.add(metadataVO.getCode());
			}

			if (viewMode == FORM && metadata.getDataEntry().getType() == DataEntryType.CALCULATED &&
				isMetadataFilledAutomatically(metadata, record, recordAutomaticMetadataServices)) {
				metadataFormExcludedCodes.add(metadataVO.getCode());
			}
		}

		RecordVO recordVO = newRecordVO(id, metadataValueVOs, viewMode, metadataCodeExcludedList);
		recordVO.setExcludedFormMetadataCodes(metadataFormExcludedCodes);
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

	private boolean isMetadataSpecialCaseToNotBeShown(AppLayerFactory appLayerFactory, final MetadataVO metadataVO,
													  final Record record) {
		return appLayerFactory.getExtensions()
				.forCollection(record.getCollection())
				.isMetadataSpecialCaseToNotBeShown(new IsMetadataSpecialCaseToNotBeShownParams() {
					@Override
					public MetadataVO getMetadataVO() {
						return metadataVO;
					}

					@Override
					public Record getRecord() {
						return record;
					}
				});
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

	protected boolean isMetadataFilledAutomatically(Metadata metadata, Record record,
													RecordAutomaticMetadataServices recordAutomaticMetadataServices) {
		return recordAutomaticMetadataServices.isValueAutomaticallyFilled(metadata, record);
	}

}
