package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.extensions.menu.MenuItemActionsExtension;
import com.constellio.app.modules.rm.services.actions.FolderRecordActionsServices;
import com.constellio.app.modules.rm.ui.buttons.DecommissioningButton;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningBuilderViewImpl;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.server.FontAwesome;

import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.DISABLED;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
import static com.constellio.app.ui.i18n.i18n.$;

public class RMDecommissioningBuilderMenuItemActionsExtension extends MenuItemActionsExtension {

	private String collection;
	private AppLayerFactory appLayerFactory;

	private FolderRecordActionsServices folderRecordActionsServices;

	private static final String RMRECORDS_CREATE_DECOMMISSIONING_LIST = "RMRECORDS_CREATE_DECOMMISSIONING_LIST";
	private static final int CREATE_DECOMMISSIONING_LIST_LIMIT = 100000;

	public RMDecommissioningBuilderMenuItemActionsExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		folderRecordActionsServices = new FolderRecordActionsServices(collection, appLayerFactory);
	}

	@Override
	public void addMenuItemActionsForRecords(MenuItemActionExtensionAddMenuItemActionsForRecordsParams params) {
		if (!(params.getBehaviorParams().getView() instanceof DecommissioningBuilderViewImpl)) {
			return;
		}

		MenuItemAction menuItemAction = MenuItemAction.builder()
				.type(RMRECORDS_CREATE_DECOMMISSIONING_LIST)
				.state(getActionState(params.getRecords(), params.getBehaviorParams().getUser()))
				.caption($("DecommissioningBuilderView.createDecommissioningList"))
				.icon(FontAwesome.ARCHIVE)
				.group(-1)
				.priority(1200)
				.recordsLimit(CREATE_DECOMMISSIONING_LIST_LIMIT)
				.command((ids) -> createDecommissioningList(params.getRecords(), params.getBehaviorParams()))
				.build();
		params.getMenuItemActions().add(menuItemAction);
	}

	@Override
	public MenuItemActionState getActionStateForRecords(MenuItemActionExtensionGetActionStateForRecordsParams params) {
		return getActionState(params.getRecords(), params.getBehaviorParams().getUser());
	}

	private MenuItemActionState getActionState(List<Record> records, User user) {
		if (records.isEmpty()) {
			return new MenuItemActionState(DISABLED, $("RMRecordsMenuItemServices.noRecordSelected"));
		}

		if (records.size() > CREATE_DECOMMISSIONING_LIST_LIMIT) {
			return new MenuItemActionState(DISABLED,
					$("RMRecordsMenuItemServices.recordsLimitReached", String.valueOf(CREATE_DECOMMISSIONING_LIST_LIMIT)));
		}

		boolean actionPossible;
		int possibleCount = 0;

		for (Record record : records) {
			if (!record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
				actionPossible = false;
			} else {
				actionPossible = folderRecordActionsServices.isCreateDecommissioningListActionPossible(record, user);
			}

			possibleCount += actionPossible ? 1 : 0;
		}
		return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
				$("RMRecordsMenuItemServices.actionImpossible"));
	}

	private void createDecommissioningList(List<Record> records, MenuItemActionBehaviorParams params) {
		List<String> ids = records.stream().map(Record::getId).collect(Collectors.toList());
		DecommissioningButton button = new DecommissioningButton($("DecommissioningBuilderView.createDecommissioningList"),
				ids, params, appLayerFactory);
		button.click();
	}

	private MenuItemActionState calculateCorrectActionState(int possibleCount, int notPossibleCount, String reason) {
		if (possibleCount > 0 && notPossibleCount == 0) {
			return new MenuItemActionState(VISIBLE);
		} else if (possibleCount == 0 && notPossibleCount > 0) {
			return new MenuItemActionState(HIDDEN, reason);
		}
		return new MenuItemActionState(DISABLED, reason);
	}

}
