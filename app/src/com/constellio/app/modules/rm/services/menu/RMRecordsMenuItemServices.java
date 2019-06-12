package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.services.menu.behaviors.RMRecordsMenuItemBehaviors;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_ADD_CART;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_ADD_SELECTION;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_BATCH;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_COPY;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CREATE_PDF;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_DOWNLOAD_ZIP;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_GENERATE_REPORT;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_LABEL;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_MOVE;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_SEND_EMAIL;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_SIP_ARCHIVE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class RMRecordsMenuItemServices {

	private String collection;
	private AppLayerFactory appLayerFactory;

	public RMRecordsMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	public List<MenuItemAction> getActionsForRecords(List<Record> records, User user, List<String> filteredActionTypes,
													 MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		// TODO
		if (!filteredActionTypes.contains(RMRECORDS_ADD_CART.name())) {
			menuItemActions.add(buildMenuItemAction(RMRECORDS_ADD_CART,
					getMenuItemActionState(RMRECORDS_ADD_CART, records, user, params),
					"TODO", FontAwesome.FILE_O, -1, 100,
					() -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).todo(params)));
		}

		if (!filteredActionTypes.contains(RMRECORDS_MOVE.name())) {
			menuItemActions.add(buildMenuItemAction(RMRECORDS_MOVE,
					getMenuItemActionState(RMRECORDS_MOVE, records, user, params),
					"TODO", FontAwesome.FILE_O, -1, 100,
					() -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).todo(params)));
		}

		if (!filteredActionTypes.contains(RMRECORDS_COPY.name())) {
			menuItemActions.add(buildMenuItemAction(RMRECORDS_COPY,
					getMenuItemActionState(RMRECORDS_COPY, records, user, params),
					"TODO", FontAwesome.FILE_O, -1, 100,
					() -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).todo(params)));
		}

		if (!filteredActionTypes.contains(RMRECORDS_GENERATE_REPORT.name())) {
			menuItemActions.add(buildMenuItemAction(RMRECORDS_GENERATE_REPORT,
					getMenuItemActionState(RMRECORDS_GENERATE_REPORT, records, user, params),
					"TODO", FontAwesome.FILE_O, -1, 100,
					() -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).todo(params)));
		}

		if (!filteredActionTypes.contains(RMRECORDS_SIP_ARCHIVE.name())) {
			menuItemActions.add(buildMenuItemAction(RMRECORDS_SIP_ARCHIVE,
					getMenuItemActionState(RMRECORDS_SIP_ARCHIVE, records, user, params),
					"TODO", FontAwesome.FILE_O, -1, 100,
					() -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).todo(params)));
		}

		if (!filteredActionTypes.contains(RMRECORDS_SEND_EMAIL.name())) {
			menuItemActions.add(buildMenuItemAction(RMRECORDS_SEND_EMAIL,
					getMenuItemActionState(RMRECORDS_SEND_EMAIL, records, user, params),
					"TODO", FontAwesome.FILE_O, -1, 100,
					() -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).todo(params)));
		}

		if (!filteredActionTypes.contains(RMRECORDS_CREATE_PDF.name())) {
			menuItemActions.add(buildMenuItemAction(RMRECORDS_CREATE_PDF,
					getMenuItemActionState(RMRECORDS_CREATE_PDF, records, user, params),
					"TODO", FontAwesome.FILE_O, -1, 100,
					() -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).todo(params)));
		}

		if (!filteredActionTypes.contains(RMRECORDS_BATCH.name())) {
			menuItemActions.add(buildMenuItemAction(RMRECORDS_BATCH,
					getMenuItemActionState(RMRECORDS_BATCH, records, user, params),
					"TODO", FontAwesome.FILE_O, -1, 100,
					() -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).todo(params)));
		}

		if (!filteredActionTypes.contains(RMRECORDS_LABEL.name())) {
			menuItemActions.add(buildMenuItemAction(RMRECORDS_LABEL,
					getMenuItemActionState(RMRECORDS_LABEL, records, user, params),
					"TODO", FontAwesome.FILE_O, -1, 100,
					() -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).todo(params)));
		}

		if (!filteredActionTypes.contains(RMRECORDS_ADD_SELECTION.name())) {
			menuItemActions.add(buildMenuItemAction(RMRECORDS_ADD_SELECTION,
					getMenuItemActionState(RMRECORDS_ADD_SELECTION, records, user, params),
					"TODO", FontAwesome.FILE_O, -1, 100,
					() -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).todo(params)));
		}

		if (!filteredActionTypes.contains(RMRECORDS_DOWNLOAD_ZIP.name())) {
			menuItemActions.add(buildMenuItemAction(RMRECORDS_DOWNLOAD_ZIP,
					getMenuItemActionState(RMRECORDS_DOWNLOAD_ZIP, records, user, params),
					"TODO", FontAwesome.FILE_O, -1, 100,
					() -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).todo(params)));
		}

		return menuItemActions;
	}

	public MenuItemActionState getMenuItemActionState(RMRecordsMenuItemActionType menuItemActionType,
													  List<Record> records, User user,
													  MenuItemActionBehaviorParams params) {
		long recordWithSupportedSchemaTypeCount = getRecordWithSupportedSchemaTypeCount(records, menuItemActionType);
		if (recordWithSupportedSchemaTypeCount != records.size()) {
			return MenuItemActionState.HIDDEN;
		}

		long recordsWithActionPossibleCount =
				getRecordsWithMenuItemActionPossibleCount(menuItemActionType, records, user, params);
		if (recordsWithActionPossibleCount == 0) {
			return MenuItemActionState.HIDDEN;
		}

		return recordsWithActionPossibleCount == recordWithSupportedSchemaTypeCount ?
			   MenuItemActionState.VISIBLE : MenuItemActionState.DISABLED;
	}

	private String getSchemaType(Record record) {
		return record.getSchemaCode().substring(0, record.getSchemaCode().indexOf("_"));
	}

	private long getRecordWithSupportedSchemaTypeCount(List<Record> records, RMRecordsMenuItemActionType type) {
		return records.stream()
				.filter(r -> type.getSchemaTypes().isEmpty() || type.getSchemaTypes().contains(getSchemaType(r)))
				.count();
	}

	private long getRecordsWithMenuItemActionPossibleCount(RMRecordsMenuItemActionType menuItemActionType,
														   List<Record> records, User user,
														   MenuItemActionBehaviorParams params) {
		return records.stream().filter(r -> isMenuItemActionPossible(menuItemActionType, r, user, params)).count();
	}

	private boolean isMenuItemActionPossible(RMRecordsMenuItemActionType menuItemActionType, Record record, User user,
											 MenuItemActionBehaviorParams params) {
		switch (menuItemActionType) {
			case RMRECORDS_ADD_CART:
				// TODO
			case RMRECORDS_MOVE:
				// TODO
			case RMRECORDS_COPY:
				// TODO
			case RMRECORDS_GENERATE_REPORT:
				// TODO
			case RMRECORDS_SIP_ARCHIVE:
				// TODO
			case RMRECORDS_SEND_EMAIL:
				// TODO
			case RMRECORDS_CREATE_PDF:
				// TODO
			case RMRECORDS_BATCH:
				// TODO
			case RMRECORDS_LABEL:
				// TODO
			case RMRECORDS_ADD_SELECTION:
				// TODO
			case RMRECORDS_DOWNLOAD_ZIP:
				// TODO
			default:
				throw new RuntimeException("Unknown MenuItemActionType : " + menuItemActionType);
		}
	}

	private MenuItemAction buildMenuItemAction(RMRecordsMenuItemActionType type, MenuItemActionState state,
											   String caption, Resource icon, int group, int priority,
											   Runnable command) {
		return MenuItemAction.builder()
				.type(type.name())
				.state(state)
				.caption(caption)
				.icon(icon)
				.group(group)
				.priority(priority)
				.command(command)
				.build();
	}

	@AllArgsConstructor
	@Getter
	enum RMRecordsMenuItemActionType {
		RMRECORDS_ADD_CART(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)),
		RMRECORDS_MOVE(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
		RMRECORDS_COPY(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
		RMRECORDS_GENERATE_REPORT(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
		RMRECORDS_SIP_ARCHIVE(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
		RMRECORDS_SEND_EMAIL(singletonList(Document.SCHEMA_TYPE)),
		RMRECORDS_CREATE_PDF(singletonList(Document.SCHEMA_TYPE)),
		RMRECORDS_BATCH(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)),
		RMRECORDS_LABEL(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)),
		RMRECORDS_ADD_SELECTION(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)),
		RMRECORDS_DOWNLOAD_ZIP(singletonList(Document.SCHEMA_TYPE));

		private final List<String> schemaTypes;
	}
}
