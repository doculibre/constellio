package com.constellio.app.modules.rm.ui.pages.trigger;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrailPresenter.TriggerFormBreadcrumbItem;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrailPresenter.TriggerManagerBreadcrumbItem;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderPresenter;
import com.constellio.app.modules.rm.wrappers.triggers.Trigger;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerAction;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

@Slf4j
public class AddEditTriggerPresenter extends BasePresenter<AddEditTriggerView> {
	private RecordServices recordServices;
	private MetadataSchemasManager metadataSchemasManager;
	private RMSchemasRecordsServices rm;
	private Record currentTargetRecord;
	private Trigger trigger;
	private boolean isAddMode;
	private Map<String, String> params;

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
		params = ParamUtils.getParamsMap(parameters);

		String targetRecordId = params.get("id");
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

	public void saveButtonClick(List<RecordVO> recordVOSToSave, List<RecordVO> recordVOsToDelete, RecordVO recordVO) {
		SchemaPresenterUtils schemaPresenterUtils = new SchemaPresenterUtils(recordVO.getSchemaCode(), view.getConstellioFactories(), view.getSessionContext());
		Record newRecord = schemaPresenterUtils.toRecord(recordVO);
		recordVO.setRecord(newRecord);
		Trigger triggerToSave = rm.wrapTrigger(newRecord);

		triggerToSave.setTarget(trigger.getTarget());

		SchemaPresenterUtils schemaPresenterUtilsForTriggerAction = new SchemaPresenterUtils(TriggerAction.DEFAULT_SCHEMA, view.getConstellioFactories(), view.getSessionContext());
		Transaction transaction = new Transaction();

		for (RecordVO currentTriggerActionToSave : recordVOSToSave) {
			schemaPresenterUtilsForTriggerAction.setSchemaCode(currentTriggerActionToSave.getSchemaCode());

			Record triggerActionRecord;
			if (currentTriggerActionToSave.isSaved()) {
				triggerActionRecord = schemaPresenterUtilsForTriggerAction.toRecord(currentTriggerActionToSave);
			} else {
				triggerActionRecord = schemaPresenterUtilsForTriggerAction.toNewRecord(currentTriggerActionToSave);
			}

			transaction.add(triggerActionRecord);
		}

		transaction.addUpdate(triggerToSave.getWrappedRecord());

		try {
			recordServices.execute(transaction);
			List<Record> recordsToDelete = recordVOsToDelete.stream().map(element -> convertToRecordForDeletion(element, schemaPresenterUtilsForTriggerAction)).collect(Collectors.toList());

			recordsToDelete.stream().forEach(recordToDelete -> schemaPresenterUtils.delete(recordToDelete, "", getCurrentUser()));
		} catch (Exception e) {
			log.error("error saving and or deleting records in AddEditTriggerPresenter", e);
			view.showErrorMessage("AddEditTriggerViewImpl.unExpectedError");
		}

		Map<String, String> paramsForCancel = new HashMap<>();
		paramsForCancel.putAll(this.params);
		paramsForCancel.remove("trigger");

		view.navigate().to(RMViews.class).recordTriggerManager(paramsForCancel);
	}

	private Record convertToRecordForDeletion(RecordVO recordVO, SchemaPresenterUtils schemaPresenterUtils) {
		schemaPresenterUtils.setSchemaCode(recordVO.getSchemaCode());
		return schemaPresenterUtils.toRecord(recordVO);
	}

	public void cancelButtonClicked() {
		Map<String, String> paramsForCancel = new HashMap<>();
		paramsForCancel.putAll(this.params);
		paramsForCancel.remove("trigger");

		view.navigate().to(RMViews.class).recordTriggerManager(paramsForCancel);
	}

	public RecordVO getRecordVO() {
		return new RecordToVOBuilder().build(trigger.getWrappedRecord(), VIEW_MODE.FORM, view.getSessionContext());
	}

	public BaseBreadcrumbTrail getBuildBreadcrumbTrail() {
		BaseBreadcrumbTrail baseBreadcrumbTrail = DisplayFolderPresenter.getBreadCrumbTrail(params, view, currentTargetRecord.getId(), null);

		String schemaLabel = metadataSchemasManager.getSchemaOf(currentTargetRecord).getSchemaType().getLabel(Language.withLocale(view.getSessionContext().getCurrentLocale()));
		baseBreadcrumbTrail
				.addItem(new TriggerManagerBreadcrumbItem(params, $("RecordTriggerManagerViewImpl.title", schemaLabel.toLowerCase() + " "
																										  + currentTargetRecord.getTitle())));
		baseBreadcrumbTrail.addItem(new TriggerFormBreadcrumbItem(params, view.getTitle()) {
			@Override
			public boolean isEnabled() {
				return false;
			}
		});

		return baseBreadcrumbTrail;
	}
}
