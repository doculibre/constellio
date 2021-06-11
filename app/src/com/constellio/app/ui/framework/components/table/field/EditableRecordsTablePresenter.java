package com.constellio.app.ui.framework.components.table.field;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.DirtyableListenable;
import com.constellio.app.ui.framework.DirtyableListenable.DirtiedArgs.DirtiedListener;
import com.constellio.app.ui.framework.DirtyableListenable.DirtiedArgs.DirtiedObservable;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.GetRecordOptions;
import com.constellio.model.services.records.RecordDeleteServices;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EditableRecordsTablePresenter implements DirtyableListenable {
	private final AppLayerFactory appLayerFactory;
	private final SessionContext sessionContext;
	private final MetadataSchemaVO schema;
	private final List<MetadataVO> metadataThatMustBeHiddenByDefault;

	protected List<String> useTheseIdsInstead;

	protected List<Record> loadedRecords;

	protected List<Record> addedRecords;
	protected List<Record> deletedRecords;
	protected List<Record> updatedRecords;

	private final DirtiedObservable dirtiedObservable;


	public EditableRecordsTablePresenter(MetadataSchemaVO schema, AppLayerFactory appLayerFactory,
										 SessionContext sessionContext) {
		this.appLayerFactory = appLayerFactory;
		this.sessionContext = sessionContext;
		this.schema = schema;

		loadedRecords = null;

		addedRecords = new ArrayList<>();
		deletedRecords = new ArrayList<>();
		updatedRecords = new ArrayList<>();
		metadataThatMustBeHiddenByDefault = new ArrayList<>();

		dirtiedObservable = new DirtiedObservable();
	}

	public void getRecordVOS(Consumer<RecordVODataProvider> newRecordAvailablesCallback) {
		if (newRecordAvailablesCallback != null && getUseTheseRecordIdsInstead() != null) {
			getRecordVOsWithSpecifiedIds(getUseTheseRecordIdsInstead(), newRecordAvailablesCallback);
		}
	}

	private void getRecordVOsWithSpecifiedIds(List<String> ids,
											  Consumer<RecordVODataProvider> newRecordAvailablesCallback) {
		if (newRecordAvailablesCallback != null) {
			MetadataSchemaVO schema = getSchema();

			metadataThatMustBeHiddenByDefault.clear();
			metadataThatMustBeHiddenByDefault.addAll(getMetadatasThatMustBeHiddenByDefault(schema.getMetadatas()));

			Map<String, RecordToVOBuilder> builders = new HashMap<>();
			builders.put(schema.getCode(), getRecordToVOBuilder());

			loadedRecords = ids != null && !ids.isEmpty() ? getRecords(ids) : new ArrayList<>();

			newRecordAvailablesCallback.accept(buildRecordDataProvider(loadedRecords, Collections.singletonList(schema), builders));
		}
	}

	protected RecordVODataProvider buildRecordDataProvider(final List<Record> loadedRecords,
														   final List<MetadataSchemaVO> schemas,
														   final Map<String, RecordToVOBuilder> builders) {
		return new RecordVODataProvider(
				schemas,
				builders,
				getAppLayerFactory().getModelLayerFactory(),
				getSessionContext(),
				loadedRecords) {
			@Override
			public LogicalSearchQuery getQuery() {
				return null;
			}

			@Override
			public void sort(MetadataVO[] propertyId, boolean[] ascending) {
				loadedRecords.sort((record1, record2) -> {
					RecordVO recordVO1 = builders.get(record1.getSchemaCode()).build(record1, VIEW_MODE.TABLE, sessionContext);
					RecordVO recordVO2 = builders.get(record2.getSchemaCode()).build(record2, VIEW_MODE.TABLE, sessionContext);

					int result = 0;

					for (int i = 0; i < propertyId.length; i++) {
						result = compareMetadataVO(propertyId[i], recordVO1, recordVO2, ascending[i]);

						if (result != 0) {
							break;
						}
					}

					return result;
				});
			}
		};
	}

	protected List<Record> getRecords(List<String> ids) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), appLayerFactory);
		return rm.get(ids);
	}

	public void setUseTheseRecordIdsInstead(List<String> useTheseIdsInstead) {
		this.useTheseIdsInstead = useTheseIdsInstead;
		loadedRecords = null;

		addedRecords.clear();
		deletedRecords.clear();
		updatedRecords.clear();
	}

	public List<String> getUseTheseRecordIdsInstead() {
		return useTheseIdsInstead;
	}

	protected AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}

	protected SessionContext getSessionContext() {
		return sessionContext;
	}

	public RecordToVOBuilder getRecordToVOBuilder() {
		return new RecordToVOBuilder();
	}

	private MetadataSchemaVO getSchema() {
		return schema;
	}

	public List<MetadataVO> getMetadatasToHideEvenIfItsInTheDisplayConfig() {
		return new ArrayList<>(metadataThatMustBeHiddenByDefault);
	}

	protected List<MetadataVO> getMetadatasThatMustBeHiddenByDefault(List<MetadataVO> availableMetadatas) {
		return new ArrayList<>();
	}

	public void setMetadata(RecordVO recordVO, String localCode, Object value) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(getSessionContext().getCurrentCollection(), getAppLayerFactory());
		MetadataSchema schema = rm.schema(recordVO.getSchemaCode());

		Metadata metadata = schema.getMetadataWithCodeOrNull(localCode);

		if (metadata != null) {
			recordVO.set(localCode, value);
			recordVO.getRecord().set(metadata, value);
		}
	}

	public void addRecord(RecordVO newRecord, Consumer<RecordVO> recordAddedCallback) {
		if (loadedRecords != null) {
			Record record = newRecord.getRecord();

			loadedRecords.add(record);
			addedRecords.add(record);

			if (recordAddedCallback != null) {
				recordAddedCallback.accept(newRecord);
				dirty();
			}
		}
	}

	public void removeRecord(RecordVO recordVO, Consumer<RecordVO> recordDeletedCallback) {
		if (loadedRecords != null) {
			Record record = recordVO.getRecord();

			loadedRecords.remove(record);

			updatedRecords.remove(record);

			if (!addedRecords.contains(record)) {
				deletedRecords.add(record);
			}

			addedRecords.remove(record);

			if (recordDeletedCallback != null) {
				recordDeletedCallback.accept(recordVO);
				dirty();
			}
		}
	}

	public void updateRecord(RecordVO recordVO, Consumer<RecordVO> recordUpdatedCallback) {
		if (loadedRecords != null) {
			Record record = recordVO.getRecord();

			if (!addedRecords.contains(record)) {
				updatedRecords.add(record);
			}

			if (recordUpdatedCallback != null) {
				recordUpdatedCallback.accept(recordVO);
				dirty();
			}
		}
	}

	public void deleteRemovedRecords() {
		RecordDeleteServices recordDeleteServices = new RecordDeleteServices(getAppLayerFactory().getModelLayerFactory());

		deletedRecords.forEach(deletedRecord -> recordDeleteServices.physicallyDeleteNoMatterTheStatus(
				deletedRecord,
				User.GOD,
				new RecordPhysicalDeleteOptions().setMostReferencesToNull(true))
		);
	}

	public List<Record> getRecordsToPersist() {
		List<Record> recordsToPersist = new ArrayList<>(updatedRecords);
		recordsToPersist.addAll(addedRecords);

		return recordsToPersist;
	}

	protected void dirty() {
		dirtiedObservable.fire(new DirtiedArgs(this));
	}

	@Override
	public boolean isDirty() {
		return CollectionUtils.isNotEmpty(updatedRecords)
			   || CollectionUtils.isNotEmpty(addedRecords)
			   || CollectionUtils.isNotEmpty(deletedRecords);
	}

	@Override
	public void addComponentDirtiedListener(DirtiedListener listener) {
		dirtiedObservable.addListener(listener);
	}

	@Override
	public void removeComponentDirtiedListener(DirtiedListener listener) {
		dirtiedObservable.removeListener(listener);
	}

	protected int compareMetadataVO(MetadataVO metadataVO, RecordVO recordVO1, RecordVO recordVO2, boolean asc) {
		return compareMetadataVO(metadataVO, recordVO1, recordVO2) * (asc ? 1 : -1);
	}

	protected int compareMetadataVO(MetadataVO metadataVO, RecordVO recordVO1, RecordVO recordVO2) {
		Object value1 = recordVO1.get(metadataVO);
		Object value2 = recordVO2.get(metadataVO);

		if (value1 == value2) {
			return 0;
		}

		if (value1 == null) {
			return -1;
		}

		if (value2 == null) {
			return 1;
		}

		switch (metadataVO.getType()) {
			case DATE:
			case DATE_TIME:
				return DateTime.parse((String) value1).compareTo(DateTime.parse((String) value2));
			case STRING:
			case TEXT:
			case ENUM:
				return String.CASE_INSENSITIVE_ORDER.compare((String) value1, (String) value2);
			case INTEGER:
				return Integer.compare((int) value1, (int) value2);
			case NUMBER:
				return Double.compare((double) value1, (double) value2);
			case BOOLEAN:
				boolean boolean1 = (boolean) value1;
				boolean boolean2 = (boolean) value2;

				if (boolean1 == boolean2) {
					return 0;
				} else if (boolean1) {
					return 1;
				} else {
					return -1;
				}

			case REFERENCE:
				RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();

				return String.CASE_INSENSITIVE_ORDER.compare(
						recordServices.get((String) value1, GetRecordOptions.RETURNING_SUMMARY).getTitle(),
						recordServices.get((String) value2, GetRecordOptions.RETURNING_SUMMARY).getTitle());
			default:
				return 0;
		}
	}
}
