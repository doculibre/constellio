package com.constellio.app.modules.rm.ui.field;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerAction;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerActionType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.containers.DataContainer;
import com.constellio.app.ui.framework.data.AbstractDataProvider;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class TableAddRemoveTriggerActionFieldPresenter {
	private final AppLayerFactory appLayerFactory;
	private ConstellioFactories constellioFactories;
	private RecordVO triggerRecordVO;
	private RecordServices recordServices;
	private RecordToVOBuilder recordToVOBuilder;
	private SessionContext sessionContext;
	private RMSchemasRecordsServices rm;
	private List<RecordVO> triggerActionVOTOSave;
	private SchemaPresenterUtils triggerActionsSchemaPresenterUtils;
	private MetadataSchemasManager metadataSchemasManager;

	public TableAddRemoveTriggerActionFieldPresenter(TableAddRemoveTriggerActionField tableAddRemoveTriggerActionField,
													 ConstellioFactories constellioFactories,
													 RecordVO triggerRecordVO) {
		this.constellioFactories = constellioFactories;
		this.recordServices = constellioFactories.getModelLayerFactory().newRecordServices();
		this.recordToVOBuilder = new RecordToVOBuilder();
		this.sessionContext = ConstellioUI.getCurrentSessionContext();
		this.triggerRecordVO = triggerRecordVO;
		this.appLayerFactory = constellioFactories.getAppLayerFactory();
		this.rm = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), appLayerFactory);
		this.triggerActionVOTOSave = new ArrayList<>();
		this.triggerActionsSchemaPresenterUtils = new SchemaPresenterUtils(TriggerAction.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
	}

	public TriggerContainer getContainer() {
		TriggerActionDataProvider recordVODataProvider = new TriggerActionDataProvider();

		return new TriggerContainer(recordVODataProvider);
	}

	public List<RecordVO> getTriggerActionToSave() {
		return triggerActionVOTOSave;
	}

	public void saveActionTrigger(RecordVO triggerActionToSave) {
		Iterator<RecordVO> triggerActionVOTOSaveIterator = this.triggerActionVOTOSave.iterator();

		while (triggerActionVOTOSaveIterator.hasNext()) {
			RecordVO currentTriggerActionTOSave = triggerActionVOTOSaveIterator.next();
			if (currentTriggerActionTOSave.getId().equals(triggerActionToSave.getId())) {
				triggerActionVOTOSaveIterator.remove();
				break;
			}
		}

		this.triggerActionVOTOSave.add(triggerActionToSave);
	}

	public RecordVO newTriggerAction() {
		return recordToVOBuilder.build(rm.newTriggerAction().getWrappedRecord(), VIEW_MODE.TABLE, sessionContext);
	}

	public ConstellioFactories getConstellioFactories() {
		return constellioFactories;
	}

	public RecordVO changeSchemaAfterTypeChange(RecordVO triggerActionVO, boolean isAddMode) {
		triggerActionsSchemaPresenterUtils.setSchemaCode(triggerActionVO.getSchemaCode());
		Record record = toRecord(triggerActionVO, isAddMode);
		String id = triggerActionVO.get(TriggerAction.TYPE);
		TriggerActionType triggerActionType = rm.getTriggerActionType(id);

		if (!triggerActionType.getLinkedSchema().equals(triggerActionVO.getSchemaCode())) {
			MetadataSchemaTypes metadataSchemaTypes = this.metadataSchemasManager.getSchemaTypes(sessionContext.getCurrentCollection());
			record.changeSchema(metadataSchemaTypes.getSchemaOf(record), metadataSchemaTypes.getSchema(triggerActionType.getLinkedSchema()));
			triggerActionsSchemaPresenterUtils.setSchemaCode(record.getSchemaCode());
			return recordToVOBuilder.build(record, VIEW_MODE.TABLE, sessionContext);
		} else {
			return null;
		}
	}

	protected Record toRecord(RecordVO recordVO, boolean isAddMode) {
		if (isAddMode) {
			return triggerActionsSchemaPresenterUtils.toNewRecord(recordVO);
		} else {
			return triggerActionsSchemaPresenterUtils.toRecord(recordVO);
		}
	}

	public class TriggerActionDataProvider extends AbstractDataProvider {

		private List<RecordVO> recordVOList;

		public TriggerActionDataProvider() {
			this(new ArrayList<RecordVO>());
		}

		public TriggerActionDataProvider(List<RecordVO> recordVOList) {
			this.recordVOList = recordVOList;
		}

		public RecordVO get(int index) {
			return recordVOList.get(index);
		}

		public RecordVO get(String id) {
			RecordVO match = null;
			for (RecordVO batchProcessVO : recordVOList) {
				if (batchProcessVO.getId().equals(id)) {
					match = batchProcessVO;
					break;
				}
			}
			return match;
		}

		public List<RecordVO> getTriggerVOs() {
			return Collections.unmodifiableList(recordVOList);
		}

		public void setTriggers(List<RecordVO> batchProcessVOs) {
			this.recordVOList = batchProcessVOs;
		}

		public void addTrigger(RecordVO batchProcessVO) {
			recordVOList.add(batchProcessVO);
		}

		public void removeTrigger(RecordVO batchProcessVO) {
			recordVOList.remove(batchProcessVO);
		}

		public void clear() {
			recordVOList.clear();
		}

	}


	public class TriggerContainer extends DataContainer<TriggerActionDataProvider> {

		public TriggerContainer(
				TriggerActionDataProvider dataProvider) {
			super(dataProvider);
		}

		@Override
		protected void populateFromData(TriggerActionDataProvider dataProvider) {
			for (RecordVO triggerVO : dataProvider.getTriggerVOs()) {
				addItem(triggerVO);
			}
		}

		public void setContent(List<String> ids) {
			List<RecordVO> recordVOS = this.getDataProvider().getTriggerVOs();
			List<String> currentIds = recordVOS.stream().map(element -> element.getId()).collect(Collectors.toList());

			for (String id : ids) {
				if (!currentIds.contains(id)) {
					RecordVO recordVO = recordToVOBuilder.build(TableAddRemoveTriggerActionFieldPresenter.this.recordServices.getRecordSummaryById(sessionContext.getCurrentCollection(), id), VIEW_MODE.TABLE, sessionContext);
					addItem(recordVO);
				}
			}
		}
	}
}
