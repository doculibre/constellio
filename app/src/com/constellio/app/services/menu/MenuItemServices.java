package com.constellio.app.services.menu;

import com.constellio.app.extensions.menu.MenuItemActionsExtension;
import com.constellio.app.extensions.menu.MenuItemActionsExtension.MenuItemActionExtensionAddMenuItemActionsForQueryParams;
import com.constellio.app.extensions.menu.MenuItemActionsExtension.MenuItemActionExtensionAddMenuItemActionsForRecordParams;
import com.constellio.app.extensions.menu.MenuItemActionsExtension.MenuItemActionExtensionAddMenuItemActionsForRecordsParams;
import com.constellio.app.extensions.menu.MenuItemActionsExtension.MenuItemActionExtensionGetActionStateForQueryParams;
import com.constellio.app.extensions.menu.MenuItemActionsExtension.MenuItemActionExtensionGetActionStateForRecordParams;
import com.constellio.app.extensions.menu.MenuItemActionsExtension.MenuItemActionExtensionGetActionStateForRecordsParams;
import com.constellio.app.modules.rm.ui.pages.userDocuments.ListUserDocumentsViewImpl;
import com.constellio.app.services.action.UserDocumentActionsServices;
import com.constellio.app.services.action.UserFolderActionsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.DesktopMenuItemActionBehaviors;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.server.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.DISABLED;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
import static com.constellio.app.services.menu.MenuItemServices.RecordsMenuItemActionType.CLASSIFY;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.vaadin.server.FontAwesome.FOLDER_O;
import static java.util.Arrays.asList;

public class MenuItemServices {

	private List<MenuItemActionsExtension> menuItemActionsExtensions;

	private UserCredentialMenuItemServices userCredentialMenuItemServices;
	private GlobalGroupMenuItemServices globalGroupMenuItemServices;
	private SchemaRecordMenuItemServices schemaRecordMenuItemServices;
	private UserDocumentMenuItemServices userDocumentMenuItemServices;
	private UserFolderMenuItemServices userFolderMenuItemServices;
	private UserDocumentActionsServices userDocumentActionsServices;
	private UserFolderActionsServices userFolderActionsServices;

	private String collection;
	private AppLayerFactory appLayerFactory;

	private SchemasRecordsServices schemasRecordsServices;

	public MenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.menuItemActionsExtensions = appLayerFactory.getExtensions()
				.forCollection(collection).menuItemActionsExtensions.getExtensions();

		this.schemasRecordsServices = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());

		this.userCredentialMenuItemServices = new UserCredentialMenuItemServices(appLayerFactory);
		this.globalGroupMenuItemServices = new GlobalGroupMenuItemServices(appLayerFactory);
		this.schemaRecordMenuItemServices = new SchemaRecordMenuItemServices(collection, appLayerFactory);
		this.userDocumentMenuItemServices = new UserDocumentMenuItemServices(collection, appLayerFactory);
		this.userFolderMenuItemServices = new UserFolderMenuItemServices(collection, appLayerFactory);
		this.userDocumentActionsServices = new UserDocumentActionsServices(collection, appLayerFactory);
		this.userFolderActionsServices = new UserFolderActionsServices(collection, appLayerFactory);
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	public List<MenuItemAction> getActionsForRecord(Record record, MenuItemActionBehaviorParams params) {
		return getActionsForRecord(record, Collections.emptyList(), params);
	}

	public List<MenuItemAction> getActionsForRecord(Record record, List<String> excludedActionTypes,
													MenuItemActionBehaviorParams params) {
		if (params.getView() == null) {
			return Collections.emptyList();
		}

		if (excludedActionTypes == null) {
			excludedActionTypes = Collections.emptyList();
		}

		List<MenuItemAction> menuItemActions = new ArrayList<>();
		Object objectRecordVO = params.getObjectRecordVO();
		if (objectRecordVO != null) {
			if (objectRecordVO instanceof UserCredentialVO) {
				menuItemActions.addAll(userCredentialMenuItemServices.getActionsForRecord(userCredentialMenuItemServices
								.getUserCredential((UserCredentialVO) objectRecordVO), params.getUser(),
						excludedActionTypes, params));
			} else if (objectRecordVO instanceof GlobalGroupVO) {
				menuItemActions.addAll(globalGroupMenuItemServices.getActionsForRecord(globalGroupMenuItemServices
								.getGlobalGroup((GlobalGroupVO) objectRecordVO), params.getUser(),
						excludedActionTypes, params));
			}
		}

		if (record != null && record.getSchemaCode().startsWith("ddv")) {
			menuItemActions.addAll(schemaRecordMenuItemServices.getActionsForRecord(record, params.getUser(), excludedActionTypes, params));
		}


		addMenuItemActionsFromExtensions(record, excludedActionTypes, params, menuItemActions);

		return menuItemActions;
	}

	public List<MenuItemAction> getActionsForRecords(List<Record> records, MenuItemActionBehaviorParams params) {
		return getActionsForRecords(records, Collections.emptyList(), params);
	}

	public List<MenuItemAction> getActionsForRecords(List<Record> records, List<String> excludedActionTypes,
													 MenuItemActionBehaviorParams params) {
		//		if (params.getView() == null) {
		//			return Collections.emptyList();
		//		}

		List<MenuItemAction> menuItemActions = new ArrayList<>();

		if (excludedActionTypes == null) {
			excludedActionTypes = new ArrayList<>();
		} else {
			excludedActionTypes = new ArrayList<>(excludedActionTypes);
		}

		if (!(params.getView() instanceof ListUserDocumentsViewImpl)) {
			excludedActionTypes.add(CLASSIFY.name());
		}

		for (RecordsMenuItemActionType actionType : getMenuItemActionTypesForRecordList(excludedActionTypes)) {
			MenuItemActionState state = getMenuItemActionStateForRecords(actionType, records, params.getUser(), params);
			addMenuItemAction(actionType, state, params, menuItemActions);
		}

		addMenuItemActionsFromExtensions(records, excludedActionTypes, params, menuItemActions);

		return menuItemActions;
	}

	private String getSchemaType(Record record) {
		return record.getSchemaCode().substring(0, record.getSchemaCode().indexOf("_"));
	}

	private long getRecordWithSupportedSchemaTypeCount(List<Record> records, RecordsMenuItemActionType type) {
		return records.stream()
				.filter(r -> type.getSchemaTypes().contains(getSchemaType(r)))
				.count();
	}

	public MenuItemActionState getMenuItemActionStateForRecords(RecordsMenuItemActionType menuItemActionType,
																List<Record> records, User user,
																MenuItemActionBehaviorParams params) {
		return computeActionState(menuItemActionType, records, user, params);
	}

	private MenuItemActionState computeActionState(RecordsMenuItemActionType menuItemActionType, List<Record> records,
												   User user, MenuItemActionBehaviorParams params) {
		if (records.isEmpty()) {
			return new MenuItemActionState(DISABLED, $("RMRecordsMenuItemServices.noRecordSelected"));
		}

		int limit = getRecordsLimit(menuItemActionType);
		if (records.size() > limit) {
			return new MenuItemActionState(DISABLED, $("RMRecordsMenuItemServices.recordsLimitReached", String.valueOf(limit)));
		}

		long recordWithSupportedSchemaTypeCount = getRecordWithSupportedSchemaTypeCount(records, menuItemActionType);
		if (recordWithSupportedSchemaTypeCount == 0) {
			return new MenuItemActionState(HIDDEN);
		} else if (recordWithSupportedSchemaTypeCount != records.size()) {
			return new MenuItemActionState(DISABLED, $("RMRecordsMenuItemServices.unsupportedSchema",
					StringUtils.join(menuItemActionType.getSchemaTypes(), ",")));
		}

		int possibleCount = 0;
		switch (menuItemActionType) {
			case CLASSIFY:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(UserDocument.SCHEMA_TYPE)) {
						actionPossible = userDocumentActionsServices.isFileActionPossible(record, user);
					} else if (record.isOfSchemaType(UserFolder.SCHEMA_TYPE)) {
						actionPossible = userFolderActionsServices.isFileActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						$("RMRecordsMenuItemServices.actionImpossible"));
		}

		return new MenuItemActionState(HIDDEN);
	}

	private MenuItemActionState calculateCorrectActionState(int possibleCount, int notPossibleCount, String reason) {
		if (possibleCount > 0 && notPossibleCount == 0) {
			return new MenuItemActionState(VISIBLE);
		} else if (possibleCount == 0 && notPossibleCount > 0) {
			return new MenuItemActionState(HIDDEN, reason);
		}
		return new MenuItemActionState(DISABLED, reason);
	}

	public List<MenuItemAction> getActionsForRecords(LogicalSearchQuery query, MenuItemActionBehaviorParams params) {
		return getActionsForRecords(query, Collections.emptyList(), params);
	}

	public List<MenuItemAction> getActionsForRecords(LogicalSearchQuery query, List<String> excludedActionTypes,
													 MenuItemActionBehaviorParams params) {
		if (params.getView() == null) {
			return Collections.emptyList();
		}

		// only used by extensions for now

		List<MenuItemAction> menuItemActions = new ArrayList<>();
		addMenuItemActionsFromExtensions(query, excludedActionTypes, params, menuItemActions);

		return menuItemActions;
	}

	public MenuItemActionState getStateForAction(String actionType, Record record,
												 MenuItemActionBehaviorParams params) {
		if (record.isOfSchemaType(UserDocument.SCHEMA_TYPE)) {
			return MenuItemActionState.visibleOrHidden(userDocumentMenuItemServices.isMenuItemActionPossible(actionType,
					schemasRecordsServices.wrapUserDocument(record), params.getUser(), params));
		} else if (record.isOfSchemaType(UserFolder.SCHEMA_TYPE)) {
			return MenuItemActionState.visibleOrHidden(userFolderMenuItemServices.isMenuItemActionPossible(actionType,
					schemasRecordsServices.wrapUserFolder(record), params.getUser(), params));
		} else if (record.isOfSchemaType(User.SCHEMA_TYPE)) {
			return MenuItemActionState.visibleOrHidden(userCredentialMenuItemServices.isMenuItemActionPossible(actionType,
					schemasRecordsServices.wrapUserCredential(record), params.getUser(), params));
		} else if (record.isOfSchemaType(Group.SCHEMA_TYPE)) {
			return MenuItemActionState.visibleOrHidden(globalGroupMenuItemServices.isMenuItemActionPossible(actionType,
					schemasRecordsServices.wrapGlobalGroup(record), params.getUser(), params));
		} else if (record.getSchemaCode().startsWith("ddv")) {
			return MenuItemActionState.visibleOrHidden(schemaRecordMenuItemServices.isMenuItemActionPossible(actionType,
					record, params.getUser(), params));
		}

		return getStateForActionFromExtensions(actionType, record, params);
	}

	public MenuItemActionState getStateForAction(String actionType, List<Record> records,
												 MenuItemActionBehaviorParams params) {

		MenuItemActionState state = computeActionStateForRecords(actionType, records, params);
		if (state != null) {
			return state;
		}

		return getStateForActionFromExtensions(actionType, records, params);
	}

	public MenuItemActionState getStateForAction(String actionType, LogicalSearchQuery query,
												 MenuItemActionBehaviorParams params) {
		// only used by extensions for now

		return getStateForActionFromExtensions(actionType, query, params);
	}

	private void addMenuItemActionsFromExtensions(Record record, List<String> excludedActionTypes,
												  MenuItemActionBehaviorParams params,
												  List<MenuItemAction> menuItemActions) {
		for (MenuItemActionsExtension menuItemActionsExtension : menuItemActionsExtensions) {
			menuItemActionsExtension.addMenuItemActionsForRecord(
					new MenuItemActionExtensionAddMenuItemActionsForRecordParams(record, menuItemActions,
							excludedActionTypes, params));
		}
	}

	private void addMenuItemActionsFromExtensions(List<Record> records, List<String> excludedActionTypes,
												  MenuItemActionBehaviorParams params,
												  List<MenuItemAction> menuItemActions) {
		for (MenuItemActionsExtension menuItemActionsExtension : menuItemActionsExtensions) {
			menuItemActionsExtension.addMenuItemActionsForRecords(
					new MenuItemActionExtensionAddMenuItemActionsForRecordsParams(records, menuItemActions,
							excludedActionTypes, params));
		}
	}

	private void addMenuItemActionsFromExtensions(LogicalSearchQuery query, List<String> excludedActionTypes,
												  MenuItemActionBehaviorParams params,
												  List<MenuItemAction> menuItemActions) {
		for (MenuItemActionsExtension menuItemActionsExtension : menuItemActionsExtensions) {
			menuItemActionsExtension.addMenuItemActionsForQuery(
					new MenuItemActionExtensionAddMenuItemActionsForQueryParams(query, menuItemActions,
							excludedActionTypes, params));
		}
	}

	private MenuItemActionState getStateForActionFromExtensions(String actionType, Record record,
																MenuItemActionBehaviorParams behaviorParams) {
		MenuItemActionState state;
		for (MenuItemActionsExtension menuItemActionsExtension : menuItemActionsExtensions) {
			state = menuItemActionsExtension.getActionStateForRecord(
					new MenuItemActionExtensionGetActionStateForRecordParams(record, actionType, behaviorParams));
			if (state != null) {
				return state;
			}
		}
		return new MenuItemActionState(HIDDEN);
	}

	private MenuItemActionState getStateForActionFromExtensions(String actionType, List<Record> records,
																MenuItemActionBehaviorParams behaviorParams) {
		MenuItemActionState state;
		for (MenuItemActionsExtension menuItemActionsExtension : menuItemActionsExtensions) {
			state = menuItemActionsExtension.getActionStateForRecords(
					new MenuItemActionExtensionGetActionStateForRecordsParams(records, actionType, behaviorParams));
			if (state != null) {
				return state;
			}
		}
		return new MenuItemActionState(HIDDEN);
	}

	private MenuItemActionState getStateForActionFromExtensions(String actionType, LogicalSearchQuery query,
																MenuItemActionBehaviorParams behaviorParams) {
		MenuItemActionState state;
		for (MenuItemActionsExtension menuItemActionsExtension : menuItemActionsExtensions) {
			state = menuItemActionsExtension.getActionStateForQuery(
					new MenuItemActionExtensionGetActionStateForQueryParams(query, actionType, behaviorParams));
			if (state != null) {
				return state;
			}
		}
		return new MenuItemActionState(HIDDEN);
	}

	private List<RecordsMenuItemActionType> getMenuItemActionTypesForRecordList(List<String> excludedActionTypes) {
		return Arrays.stream(RecordsMenuItemActionType.values())
				.filter(t -> !excludedActionTypes.contains(t.name()))
				.collect(Collectors.toList());
	}

	private void addMenuItemAction(RecordsMenuItemActionType actionType, MenuItemActionState state,
								   MenuItemActionBehaviorParams params, List<MenuItemAction> menuItemActions) {
		MenuItemAction menuItemAction = null;

		switch (actionType) {
			case CLASSIFY:
				menuItemAction = buildMenuItemAction(CLASSIFY, state,
						$("ConstellioHeader.selection.actions.classify"), FOLDER_O, -1, 100,
						getRecordsLimit(actionType),
						(ids) -> new DesktopMenuItemActionBehaviors(appLayerFactory).classifyWindow(ids, params));
				break;
		}

		if (menuItemAction != null) {
			menuItemActions.add(menuItemAction);
		}
	}


	private MenuItemAction buildMenuItemAction(RecordsMenuItemActionType type, MenuItemActionState state,
											   String caption, Resource icon, int group, int priority, int recordsLimit,
											   Consumer<List<String>> command) {
		return MenuItemAction.builder()
				.type(type.name())
				.state(state)
				.caption(caption)
				.icon(icon)
				.group(group)
				.priority(priority)
				.command(command)
				.recordsLimit(recordsLimit)
				.build();
	}

	private int getRecordsLimit(RecordsMenuItemActionType menuItemActionType) {
		return menuItemActionType.getRecordsLimit();
	}

	private MenuItemActionState computeActionStateForRecords(String actionType, List<Record> records,
															 MenuItemActionBehaviorParams params) {
		return null;
	}

	@AllArgsConstructor
	@Getter
	public enum RecordsMenuItemActionType {
		CLASSIFY(asList(UserDocument.SCHEMA_TYPE, UserFolder.SCHEMA_TYPE), 100000);

		private final List<String> schemaTypes;
		private final int recordsLimit;

		public static boolean contains(String typeAsString) {
			for (RecordsMenuItemActionType type : RecordsMenuItemActionType.values()) {
				if (type.name().equals(typeAsString)) {
					return true;
				}
			}
			return false;
		}
	}

}
