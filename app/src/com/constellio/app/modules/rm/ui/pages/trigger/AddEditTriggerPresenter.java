package com.constellio.app.modules.rm.ui.pages.trigger;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.triggers.Trigger;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AddEditTriggerPresenter extends BasePresenter<AddEditTriggerView> {
	private RecordServices recordServices;
	private MetadataSchemasManager metadataSchemasManager;
	private RMSchemasRecordsServices rm;
	private Record currentTargetRecord;
	private Trigger trigger;
	private boolean isAddMode;


	public AddEditTriggerPresenter(AddEditTriggerView view) {
		super(view);

		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		this.rm = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return user.hasWriteAccess().on(restrictedRecord);
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		return Arrays.asList(currentTargetRecord.getId());
	}

	public void forParams(String parameters) {
		Map<String, String> params = ParamUtils.getParamsMap(parameters);

		String targetRecordId = params.get("record");
		if (params == null || targetRecordId == null) {
			throw new IllegalStateException("record is required for this page");
		}

		currentTargetRecord = recordServices.getRecordSummaryById(view.getCollection(), targetRecordId);

		if (params.get("trigger") != null) {
			trigger = rm.getTrigger(params.get("trigger"));
			isAddMode = false;
		} else {
			trigger = rm.newTrigger();

			isAddMode = true;
			currentTargetRecord = recordServices.getRecordSummaryById(view.getCollection(), targetRecordId);
			trigger.setTarget(Arrays.asList(targetRecordId));
		}


		String schemaLabel = metadataSchemasManager.getSchemaOf(currentTargetRecord).getSchemaType().getLabel(Language.withLocale(view.getSessionContext().getCurrentLocale()));
		view.setRecordTitle(schemaLabel.toLowerCase() + " " + currentTargetRecord.getTitle());
	}

	public boolean isAddMode() {
		return isAddMode;
	}

	public void saveButtonClick(RecordVO recordVO) {
		SchemaPresenterUtils schemaPresenterUtils = new SchemaPresenterUtils(recordVO.getSchemaCode(), view.getConstellioFactories(), view.getSessionContext());
		Record newRecord = schemaPresenterUtils.toRecord(recordVO);
		recordVO.setRecord(newRecord);
		Trigger triggerToSave = rm.wrapTrigger(newRecord);

		triggerToSave.setTarget(trigger.getTarget());

		schemaPresenterUtils.addOrUpdate(triggerToSave.getWrappedRecord(), getCurrentUser());

		view.navigate().to(RMViews.class).recordTriggerManager(currentTargetRecord.getId());
	}

	public void cancelButtonClicked() {
		view.navigate().to(RMViews.class).recordTriggerManager(getCurrentTriggerId());
	}

	public String getCurrentTriggerId() {
		return trigger.getId();
	}

	public RecordVO getRecordVO() {
		return new RecordToVOBuilder().build(trigger.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
	}
}
