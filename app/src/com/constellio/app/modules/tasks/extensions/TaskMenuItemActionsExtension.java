package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.extensions.menu.MenuItemActionsExtension;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices;
import com.constellio.app.modules.tasks.services.menu.behaviors.util.TaskUrlUtil;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.window.ConsultLinkWindow.ConsultLinkParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.vaadin.server.FontAwesome;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.framework.clipboard.CopyToClipBoard.copyConsultationLinkToClipBoard;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.util.UrlUtil.getConstellioUrl;

public class TaskMenuItemActionsExtension extends MenuItemActionsExtension {

	private TasksSchemasRecordsServices tasksSchema;
	private AppLayerFactory appLayerFactory;
	private RecordServices recordServices;
	private String collection;

	private TaskMenuItemServices taskMenuItemServices;

	private static final String CONSULTATION_LINK = "CONSULTATION_LINK";

	public TaskMenuItemActionsExtension(String collection, AppLayerFactory appLayerFactory) {
		tasksSchema = new TasksSchemasRecordsServices(collection, appLayerFactory);
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		taskMenuItemServices = new TaskMenuItemServices(collection, appLayerFactory);
		this.appLayerFactory = appLayerFactory;
	}


	private void showConsultLink(List<String> recordIds, MenuItemActionBehaviorParams behaviorParams) {
		String constellioURL = getConstellioUrl(appLayerFactory.getModelLayerFactory());

		List<ConsultLinkParams> linkList = new ArrayList<>();

		List<Record> recordList = recordServices.getRecordsById(collection, recordIds);

		for (Record currentRecord : recordList) {
			if (currentRecord.getSchemaCode().startsWith(RMTask.SCHEMA_TYPE)) {
				linkList.add(new ConsultLinkParams(constellioURL + TaskUrlUtil.getPathToConsultLinkForTask(currentRecord.getId())
						, currentRecord.getTitle()));
			}
		}

		copyConsultationLinkToClipBoard(linkList);
	}


	private MenuItemActionState actionStateForConsultationLink(List<Record> recordList) {

		if (recordList == null || recordList.isEmpty()) {
			return MenuItemActionState.visibleOrHidden(false);
		}

		for (Record record : recordList) {
			if (!record.isOfSchemaType(Task.SCHEMA_TYPE)) {
				return MenuItemActionState.visibleOrHidden(false);
			}
		}

		return new MenuItemActionState(MenuItemActionStateStatus.VISIBLE);
	}


	@Override
	public void addMenuItemActionsForRecord(MenuItemActionExtensionAddMenuItemActionsForRecordParams params) {
		Record record = params.getRecord();
		User user = params.getBehaviorParams().getUser();
		List<MenuItemAction> menuItemActions = params.getMenuItemActions();
		List<String> excludedActionTypes = params.getExcludedActionTypes();
		MenuItemActionBehaviorParams behaviorParams = params.getBehaviorParams();


		if (record != null) {
			if (record.isOfSchemaType(Task.SCHEMA_TYPE)) {
				menuItemActions.addAll(taskMenuItemServices.getActionsForRecord(tasksSchema.wrapTask(record), user,
						excludedActionTypes, behaviorParams));
			}
		}
	}


	@Override
	public MenuItemActionState getActionStateForRecord(MenuItemActionExtensionGetActionStateForRecordParams params) {
		Record record = params.getRecord();
		User user = params.getBehaviorParams().getUser();
		String actionType = params.getMenuItemActionType();
		MenuItemActionBehaviorParams behaviorParams = params.getBehaviorParams();

		if (record.isOfSchemaType(Task.SCHEMA_TYPE)) {
			return toState(taskMenuItemServices.isMenuItemActionPossible(actionType, tasksSchema.wrapTask(record),
					user, behaviorParams));
		}

		return null;
	}

	@Override
	public void addMenuItemActionsForRecords(MenuItemActionExtensionAddMenuItemActionsForRecordsParams params) {
		MenuItemAction menuItemAction = MenuItemAction.builder()
				.type(CONSULTATION_LINK)
				.state(actionStateForConsultationLink(params.getRecords()))
				.caption($("consultationLink"))
				.icon(FontAwesome.LINK)
				.group(-1)
				.priority(1000)
				.recordsLimit(-1)
				.command((ids) -> showConsultLink(ids, params.getBehaviorParams()))
				.build();
		params.getMenuItemActions().add(menuItemAction);
	}
}
