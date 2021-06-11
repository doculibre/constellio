package com.constellio.app.ui.framework.builders;

import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.extensions.records.params.AddSyntheticMetadataValuesParams;
import com.constellio.app.extensions.records.params.AddSyntheticMetadataValuesParams.SyntheticMetadataVOBuilder;
import com.constellio.app.extensions.records.params.AddSyntheticMetadataValuesParams.WhereToAddMetadata;
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
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordAutomaticMetadataServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.users.UserServices;
import org.h2.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
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

		List<MetadataValueVO> extensionsAdditionnalMetadataVOs = appLayerFactory.getExtensions().forCollection(record.getCollection()).addSyntheticMetadataValues(new AddSyntheticMetadataValuesParams(
				record, createSyntheticMetadataVOBuilder(appLayerFactory, sessionContext, schemaVO)));

		if (extensionsAdditionnalMetadataVOs != null) {
			metadataValueVOs.addAll(extensionsAdditionnalMetadataVOs);
		}

		RecordVO recordVO = newRecordVO(id, metadataValueVOs, viewMode, metadataCodeExcludedList);
		recordVO.setExcludedFormMetadataCodes(metadataFormExcludedCodes);
		recordVO.setSaved(saved);
		recordVO.setRecord(record);
		if (recordVO.getSchema() != null) {
			BuildRecordVOParams buildRecordVOParams = new BuildRecordVOParams(record, recordVO);
			appLayerFactory.getExtensions()
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

	private SyntheticMetadataVOBuilder createSyntheticMetadataVOBuilder(AppLayerFactory appLayerFactory,
																		SessionContext sessionContext,
																		MetadataSchemaVO schema) {
		return syntheticMetadataVOBuildingArgs -> {
			String syntheticMetadataCode = syntheticMetadataVOBuildingArgs.getMetadataCode();
			String referencedSchemaType = syntheticMetadataVOBuildingArgs.getReferencedSchemaType();

			Map<Locale, String> labels = new HashMap<>();
			labels.put(sessionContext.getCurrentLocale(), syntheticMetadataVOBuildingArgs.getLabel());

			String[] taxoCodes = new String[0];

			Set<String> references = new HashSet<>();
			references.add(syntheticMetadataVOBuildingArgs.getReferencedSchema());

			String typeCode = SchemaUtils.getSchemaTypeCode(schema.getCode());

			Map<String, Map<Language, String>> groups = appLayerFactory.getMetadataSchemasDisplayManager().getType(schema.getCollection(), typeCode)
					.getMetadataGroup();
			Language language = Language.withCode(sessionContext.getCurrentLocale().getLanguage());
			String groupLabel = groups.keySet().isEmpty() ? null : groups.entrySet().iterator().next().getValue().get(language);

			EnumSet<WhereToAddMetadata> whereToAddMetadata = syntheticMetadataVOBuildingArgs.getWhereToAddMetadata();
			if (whereToAddMetadata.contains(WhereToAddMetadata.DISPLAY)) {
				List<String> displayMetadataCodes = schema.getDisplayMetadataCodes();

				String insertBeforeThisMetadataInDisplay = syntheticMetadataVOBuildingArgs.getInsertBeforeThisMetadataInDisplay();
				if (!StringUtils.isNullOrEmpty(insertBeforeThisMetadataInDisplay)) {
					insertMetadataCodeBeforeIfNotExist(syntheticMetadataCode, insertBeforeThisMetadataInDisplay, displayMetadataCodes);
				} else {
					putMetadataAtTheEndIfNotExist(syntheticMetadataCode, displayMetadataCodes);
				}
			}

			if (whereToAddMetadata.contains(WhereToAddMetadata.FORM)) {
				List<String> formMetadataCodes = schema.getFormMetadataCodes();

				String insertBeforeThisMetadataInForm = syntheticMetadataVOBuildingArgs.getInsertBeforeThisMetadataInForm();
				if (!StringUtils.isNullOrEmpty(insertBeforeThisMetadataInForm)) {
					insertMetadataCodeBeforeIfNotExist(syntheticMetadataCode, insertBeforeThisMetadataInForm, formMetadataCodes);
				} else {
					putMetadataAtTheEndIfNotExist(syntheticMetadataCode, formMetadataCodes);
				}
			}

			return new MetadataVO((short) 0, syntheticMetadataCode, MetadataVO.getCodeWithoutPrefix(syntheticMetadataCode), MetadataValueType.REFERENCE, schema.getCollection(), schema, false, true, syntheticMetadataVOBuildingArgs.isReadOnly(),
					labels, null, taxoCodes, referencedSchemaType, MetadataInputType.LOOKUP, MetadataDisplayType.VERTICAL, syntheticMetadataVOBuildingArgs.getMetadataSortingType(),
					new AllowedReferences(referencedSchemaType, references), groupLabel, null, false, new HashSet<String>(), false, null,
					new HashMap<String, Object>(), schema.getCollectionInfoVO(), false, true, false, null, null, null);
		};
	}

	private void insertMetadataCodeBeforeIfNotExist(String codeToInsert, String codeToSearch, List<String> codes) {
		if (codes.stream().noneMatch(code -> code.equals(codeToInsert))) {
			int index = codes.indexOf(codeToSearch);

			if (index >= 0) {
				codes.add(index, codeToInsert);
			} else {
				putMetadataAtTheEndIfNotExist(codeToInsert, codes);
			}
		}
	}

	private void putMetadataAtTheEndIfNotExist(String codeToInsert, List<String> codes) {
		if (codes.stream().noneMatch(code -> code.equals(codeToInsert))) {
			codes.add(codeToInsert);
		}
	}
}
