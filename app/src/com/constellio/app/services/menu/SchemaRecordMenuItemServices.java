package com.constellio.app.services.menu;

import com.constellio.app.services.action.SchemaRecordActionsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.services.menu.behavior.SchemaRecordMenuItemActionBehaviors;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
import static com.constellio.app.services.menu.SchemaRecordMenuItemServices.SchemaRecordMenuItemActionType.SCHEMA_RECORD_DELETE;
import static com.constellio.app.services.menu.SchemaRecordMenuItemServices.SchemaRecordMenuItemActionType.SCHEMA_RECORD_EDIT;
import static com.constellio.app.services.menu.SchemaRecordMenuItemServices.SchemaRecordMenuItemActionType.SCHEMA_RECORD_SEQUENCES;
import static com.constellio.app.ui.i18n.i18n.$;

public class SchemaRecordMenuItemServices {
	private SchemaRecordActionsServices schemaRecordActionsServices;
	private String collection;
	private AppLayerFactory appLayerFactory;

	public SchemaRecordMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		schemaRecordActionsServices = new SchemaRecordActionsServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecord(Record record, User user, List<String> excludedActionTypes,
													MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		if (!excludedActionTypes.contains(SCHEMA_RECORD_EDIT.name())) {
			menuItemActions.add(buildMenuItemAction(SCHEMA_RECORD_EDIT.name(),
					isMenuItemActionPossible(SCHEMA_RECORD_EDIT.name(), record, user, params),
					$("editWithIcon"), null, -1, 100,
					(ids) -> new SchemaRecordMenuItemActionBehaviors(collection, appLayerFactory).edit(record, params)));
		}

		if (!excludedActionTypes.contains(SCHEMA_RECORD_SEQUENCES.name())) {
			menuItemActions.add(buildMenuItemAction(SCHEMA_RECORD_SEQUENCES.name(),
					isMenuItemActionPossible(SCHEMA_RECORD_SEQUENCES.name(), record, user, params),
					$("DisplaySchemaRecordView.sequences"), null, -1, 200,
					(ids) -> new SchemaRecordMenuItemActionBehaviors(collection, appLayerFactory).sequences(record, params)));
		}
		if (!excludedActionTypes.contains(SCHEMA_RECORD_DELETE.name())) {
			menuItemActions.add(buildMenuItemAction(SCHEMA_RECORD_DELETE.name(),
					isMenuItemActionPossible(SCHEMA_RECORD_DELETE.name(), record, user, params),
					$("delete"), null, -1, Integer.MAX_VALUE,
					(ids) -> new SchemaRecordMenuItemActionBehaviors(collection, appLayerFactory).delete(record, params)));
		}

		return menuItemActions;
	}

	public boolean isMenuItemActionPossible(String menuItemActionType, Record record, User user,
											MenuItemActionBehaviorParams params) {

		switch (SchemaRecordMenuItemActionType.valueOf(menuItemActionType)) {
			case SCHEMA_RECORD_EDIT:
				return schemaRecordActionsServices.isEditActionPossible(record, user);
			case SCHEMA_RECORD_DELETE:
				return schemaRecordActionsServices.isDeleteActionPossible(record, user);
			case SCHEMA_RECORD_SEQUENCES:
				return schemaRecordActionsServices.isSequencesActionPossible(record, user, params.getView());
			default:
				throw new RuntimeException("Unknown MenuItemActionType : " + menuItemActionType);
		}
	}

	private MenuItemAction buildMenuItemAction(String type, boolean possible, String caption,
											   Resource icon, int group, int priority, Consumer<List<String>> command) {
		return MenuItemAction.builder()
				.type(type)
				.state(new MenuItemActionState(possible ? VISIBLE : HIDDEN))
				.caption(caption)
				.icon(icon)
				.group(group)
				.priority(priority)
				.command(command)
				.recordsLimit(1)
				.build();
	}

	enum SchemaRecordMenuItemActionType {
		SCHEMA_RECORD_EDIT,
		SCHEMA_RECORD_DELETE,
		SCHEMA_RECORD_SEQUENCES,
	}
}
