package com.constellio.app.modules.rm.ui.field;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.triggers.Trigger;
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
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;

import java.util.ArrayList;
import java.util.Collection;
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
	private List<RecordVO> triggerActionVOListToSave;
	private List<RecordVO> triggerActionVOListToDelete;
	private SchemaPresenterUtils triggerActionsSchemaPresenterUtils;
	private MetadataSchemasManager metadataSchemasManager;
	private TableAddRemoveTriggerActionField tableAddRemoveTriggerActionField;

	public TableAddRemoveTriggerActionFieldPresenter(TableAddRemoveTriggerActionField tableAddRemoveTriggerActionField,
													 ConstellioFactories constellioFactories,
													 RecordVO triggerRecordVO) {
		this.constellioFactories = constellioFactories;
		this.recordServices = constellioFactories.getModelLayerFactory().newRecordServices();
		this.tableAddRemoveTriggerActionField = tableAddRemoveTriggerActionField;
		this.recordToVOBuilder = new RecordToVOBuilder();
		this.sessionContext = ConstellioUI.getCurrentSessionContext();
		this.triggerRecordVO = triggerRecordVO;
		this.appLayerFactory = constellioFactories.getAppLayerFactory();
		this.rm = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), appLayerFactory);
		this.triggerActionVOListToSave = new ArrayList<>();
		this.triggerActionVOListToDelete = new ArrayList<>();
		this.triggerActionsSchemaPresenterUtils = new SchemaPresenterUtils(TriggerAction.DEFAULT_SCHEMA, constellioFactories, sessionContext);
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
	}

	public TriggerActionContainer getCurrentContainer() {
		TriggerActionDataProvider recordVODataProvider = new TriggerActionDataProvider();

		return new TriggerActionContainer(recordVODataProvider);
	}

	public List<RecordVO> getTriggerActionVOListToDelete() {
		return triggerActionVOListToDelete;
	}

	public List<RecordVO> getTriggerActionVOListToSave() {
		return triggerActionVOListToSave;
	}

	public void deleteTriggerAction(RecordVO triggerActionToDelete) {
		Iterator<RecordVO> triggerActionIterator = this.triggerActionVOListToSave.iterator();

		while (triggerActionIterator.hasNext()) {
			RecordVO currentTriggerActionVO = triggerActionIterator.next();

			if (currentTriggerActionVO.getId().equals(triggerActionToDelete.getId())) {
				if (currentTriggerActionVO.isSaved()) {
					triggerActionVOListToDelete.add(triggerActionToDelete);
				}

				triggerActionIterator.remove();
			}
		}

		this.tableAddRemoveTriggerActionField.getTriggerContainer().removeItem(triggerActionToDelete);
	}

	public void addUpdateActionTrigger(RecordVO triggerActionToSave) {
		Iterator<RecordVO> triggerActionVOTOSaveIterator = this.triggerActionVOListToSave.iterator();

		while (triggerActionVOTOSaveIterator.hasNext()) {
			RecordVO currentTriggerActionTOSave = triggerActionVOTOSaveIterator.next();
			if (currentTriggerActionTOSave.getId().equals(triggerActionToSave.getId())) {
				triggerActionVOTOSaveIterator.remove();
				break;
			}
		}

		this.tableAddRemoveTriggerActionField.getTriggerContainer().addUpdateItem(triggerActionToSave);
		this.triggerActionVOListToSave.add(triggerActionToSave);
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

		public void setTriggerActions(List<RecordVO> triggerActionsVO) {
			this.recordVOList = triggerActionsVO;
		}

		public void addTriggerActions(RecordVO triggerAction) {
			recordVOList.add(triggerAction);
		}

		public void removeTriggerActions(String id) {
			Iterator<RecordVO> iterator = recordVOList.iterator();

			while (iterator.hasNext()) {
				RecordVO currentTriggerActionVO = iterator.next();

				if (currentTriggerActionVO.getId().equals(id)) {
					iterator.remove();
					break;
				}
			}
		}

		public void clear() {
			recordVOList.clear();
		}

	}


	public class TriggerActionContainer extends DataContainer<TriggerActionDataProvider> {

		public TriggerActionContainer(
				TriggerActionDataProvider dataProvider) {
			super(dataProvider);
		}

		@Override
		protected Property<?> getOwnContainerProperty(Object itemId, Object propertyId) {
			final RecordVO triggerActionVO = (RecordVO) itemId;
			Object value;
			if (Trigger.TITLE.equals(propertyId)) {
				value = triggerActionVO.getTitle();
			} else if (Trigger.TYPE.equals(propertyId)) {
				TriggerActionType triggerActionType = rm.getTriggerActionType(triggerActionVO.get(TriggerAction.TYPE));
				value = triggerActionType.getTitle();
			} else {
				throw new IllegalArgumentException("Invalid propertyId : " + propertyId);
			}
			Class<?> type = getType(propertyId);
			return new ObjectProperty(value, type);
		}


		@Override
		protected void populateFromData(TriggerActionDataProvider dataProvider) {
			for (RecordVO triggerVO : dataProvider.getTriggerVOs()) {
				addItem(triggerVO);
			}
		}

		public void addUpdateItem(RecordVO newTriggerActionVO) {
			List<RecordVO> recordVOS = this.getDataProvider().getTriggerVOs();
			List<String> currentIds = recordVOS.stream().map(element -> element.getId()).collect(Collectors.toList());

			if (currentIds.contains(newTriggerActionVO.getId())) {
				this.getDataProvider().removeTriggerActions(newTriggerActionVO.getId());
			}

			this.getDataProvider().addTriggerActions(newTriggerActionVO);
			this.forceRefresh();
		}

		public void removeItem(RecordVO triggerToRemoveVO) {
			this.getDataProvider().removeTriggerActions(triggerToRemoveVO.getId());
			this.forceRefresh();
		}

		@Override
		protected Class<?> getOwnType(Object propertyId) {
			Class<?> type;
			if (TriggerAction.TITLE.equals(propertyId)) {
				type = String.class;
			} else if (TriggerAction.TYPE.equals(propertyId)) {
				type = String.class;
			} else {
				throw new IllegalArgumentException("Invalid propertyId : " + propertyId);
			}
			return type;
		}

		@Override
		protected Collection<?> getOwnContainerPropertyIds() {
			List<String> containerPropertyIds = new ArrayList<>();
			containerPropertyIds.add(TriggerAction.TITLE);
			containerPropertyIds.add(TriggerAction.TYPE);

			return containerPropertyIds;
		}

		public void setContent(List<String> ids) {
			List<RecordVO> recordVOS = this.getDataProvider().getTriggerVOs();
			List<String> currentIds = recordVOS.stream().map(element -> element.getId()).collect(Collectors.toList());

			for (String id : ids) {
				if (!currentIds.contains(id)) {
					RecordVO recordVO = recordToVOBuilder.build(TableAddRemoveTriggerActionFieldPresenter.this.recordServices.getRecordSummaryById(sessionContext.getCurrentCollection(), id), VIEW_MODE.TABLE, sessionContext);
					getDataProvider().addTriggerActions(recordVO);
					addItem(recordVO);
				}
			}
		}
	}
}
