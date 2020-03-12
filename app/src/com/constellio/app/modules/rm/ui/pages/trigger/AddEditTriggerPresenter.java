package com.constellio.app.modules.rm.ui.pages.trigger;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.triggers.Trigger;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
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
	private Record currentTrigger;
	private Record targetRecord;
	private RecordVO recordVO;
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
		return Arrays.asList(targetRecord.getId());
	}

	public void forParams(String parameters) {
		Map<String, String> params = ParamUtils.getParamsMap(parameters);

		if (params == null || params.get("record") == null) {
			throw new IllegalStateException("record is required for this page");
		}
		if (params.get("trigger") != null) {
			currentTrigger = recordServices.getRecordSummaryById(view.getCollection(), parameters);
			trigger = rm.wrapTrigger(currentTrigger);
			isAddMode = false;

		} else {
			trigger = rm.newTrigger();
			currentTrigger = trigger.getWrappedRecord();
			isAddMode = true;
			targetRecord = recordServices.getRecordSummaryById(view.getCollection(), params.get("record"));
		}

		targetRecord = recordServices.getRecordSummaryById(view.getCollection(), params.get("record"));
		String schemaLabel = metadataSchemasManager.getSchemaOf(targetRecord).getSchemaType().getLabel(Language.withLocale(view.getSessionContext().getCurrentLocale()));
		view.setRecordTitle(schemaLabel.toLowerCase() + " " + targetRecord.getTitle());
		recordVO = new RecordToVOBuilder().build(currentTrigger, VIEW_MODE.TABLE, view.getSessionContext());
	}

	public boolean isAddMode() {
		return isAddMode;
	}

	public void saveButtonClick(RecordVO recordVO) {

	}

	public void cancelButtonClicked() {
		view.navigate().to(RMViews.class).recordTriggerManager(getRecordId());
	}

	public String getRecordId() {
		return currentTrigger.getId();
	}

	public RecordVO getRecordVO() {
		return new RecordToVOBuilder().build(currentTrigger, VIEW_MODE.FORM, view.getSessionContext());
	}
}
