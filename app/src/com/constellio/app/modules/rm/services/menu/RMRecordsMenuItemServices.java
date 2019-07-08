package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.services.actions.ContainerRecordActionsServices;
import com.constellio.app.modules.rm.services.actions.DocumentRecordActionsServices;
import com.constellio.app.modules.rm.services.actions.FolderRecordActionsServices;
import com.constellio.app.modules.rm.services.menu.behaviors.RMRecordsMenuItemBehaviors;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_ADD_CART;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_ADD_SELECTION;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_COPY;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CREATE_PDF;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CREATE_SIP;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_DOWNLOAD_ZIP;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_MOVE;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_PRINT_LABEL;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_SEND_EMAIL;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.DISABLED;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class RMRecordsMenuItemServices {

	private String collection;
	private AppLayerFactory appLayerFactory;

	private RecordServices recordServices;
	private SearchServices searchServices;
	private DocumentRecordActionsServices documentRecordActionsServices;
	private FolderRecordActionsServices folderRecordActionsServices;
	private ContainerRecordActionsServices containerRecordActionsServices;

	private static final Resource SELECTION_ICON_RESOURCE = new ThemeResource("images/icons/clipboard_12x16.png");

	public RMRecordsMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		documentRecordActionsServices = new DocumentRecordActionsServices(collection, appLayerFactory);
		folderRecordActionsServices = new FolderRecordActionsServices(collection, appLayerFactory);
		containerRecordActionsServices = new ContainerRecordActionsServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecords(List<Record> records, User user, List<String> filteredActionTypes,
													 MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		List<RMRecordsMenuItemActionType> actionTypes = getRMRecordsMenuItemActionTypes(filteredActionTypes);
		for (RMRecordsMenuItemActionType actionType : actionTypes) {
			MenuItemActionState state = getMenuItemActionStateForRecords(actionType, records, user, params);
			addMenuItemAction(actionType, state, params, menuItemActions);
		}

		return menuItemActions;
	}

	public List<MenuItemAction> getActionsForQuery(LogicalSearchQuery query, User user,
												   List<String> filteredActionTypes,
												   MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		List<RMRecordsMenuItemActionType> actionTypes = getRMRecordsMenuItemActionTypes(filteredActionTypes);
		if (!actionTypes.isEmpty()) {
			Map<RMRecordsMenuItemActionType, MenuItemActionState> actionStateByType = new HashMap<>();

			SearchResponseIterator<List<Record>> recordsIterator = searchServices.recordsIterator(query).inBatches();
			while (recordsIterator.hasNext()) {
				List<Record> records = recordsIterator.next();

				Iterator<RMRecordsMenuItemActionType> actionTypesIterator = actionTypes.iterator();
				while (actionTypesIterator.hasNext()) {
					RMRecordsMenuItemActionType actionType = actionTypesIterator.next();
					MenuItemActionState previousState = actionStateByType.get(actionType);

					MenuItemActionState state = computeActionState(actionType, records, user, params, previousState);
					actionStateByType.put(actionType, state);

					if (state.getStatus() == DISABLED) {
						actionTypesIterator.remove();
						break;
					}
				}
			}

			for (RMRecordsMenuItemActionType actionType : actionStateByType.keySet()) {
				addMenuItemAction(actionType, actionStateByType.get(actionType), params, menuItemActions);
			}
		}

		return menuItemActions;
	}

	public MenuItemActionState getMenuItemActionStateForRecords(RMRecordsMenuItemActionType menuItemActionType,
																List<Record> records, User user,
																MenuItemActionBehaviorParams params) {
		return computeActionState(menuItemActionType, records, user, params, null);
	}

	public MenuItemActionState getMenuItemActionStateForQuery(RMRecordsMenuItemActionType actionType,
															  LogicalSearchQuery query,
															  User user, MenuItemActionBehaviorParams params) {
		if (query.getCondition() == null) {
			return new MenuItemActionState(HIDDEN);
		} else if (!actionType.getSchemaTypes().containsAll(query.getCondition().getFilterSchemaTypesCodes())) {
			return new MenuItemActionState(HIDDEN);
		} /*else if (records.size() == 0) {
			return new MenuItemActionState(DISABLED, "RMRecordsMenuItemServices.noRecordSelected");
		}

		return computeActionState(actionType, records, user, params, null);*/

		MenuItemActionState state = null;
		SearchResponseIterator<List<Record>> recordsIterator = searchServices.recordsIterator(query).inBatches();
		while (recordsIterator.hasNext()) {
			List<Record> records = recordsIterator.next();

			state = computeActionState(actionType, records, user, params, state);

			if (state.getStatus() == DISABLED) {
				return state;
			}
		}
		return state;
	}

	public MenuItemActionState computeActionState(RMRecordsMenuItemActionType menuItemActionType, List<Record> records,
												  User user, MenuItemActionBehaviorParams params,
												  MenuItemActionState previousState) {
		long recordWithSupportedSchemaTypeCount = getRecordWithSupportedSchemaTypeCount(records, menuItemActionType);
		if (recordWithSupportedSchemaTypeCount == 0) {
			return new MenuItemActionState(HIDDEN, $("RMRecordsMenuItemServices.unsupportedSchema",
					StringUtils.join(menuItemActionType.getSchemaTypes(), ",")));
		} else if (recordWithSupportedSchemaTypeCount != records.size()) {
			return new MenuItemActionState(DISABLED, $("RMRecordsMenuItemServices.unsupportedSchema",
					StringUtils.join(menuItemActionType.getSchemaTypes(), ",")));
		}

		int possibleCount = 0;
		switch (menuItemActionType) {
			case RMRECORDS_ADD_CART:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isAddToCartActionPossible(record, user);
					} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isAddToCartActionPossible(record, user);
					} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
						actionPossible = containerRecordActionsServices.isAddToCartActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						previousState, $("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_MOVE:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isMoveActionPossible(record, user);
					} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isMoveActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						previousState, $("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_COPY:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isCopyActionPossible(record, user);
					} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isCopyActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						previousState, $("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_CREATE_SIP:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isCreateSipActionPossible(record, user);
					} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isCreateSipActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						previousState, $("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_SEND_EMAIL:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isSendEmailActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						previousState, $("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_CREATE_PDF:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isCreatePdfActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						previousState, $("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_PRINT_LABEL:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isPrintLabelActionPossible(record, user);
					} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isPrintLabelActionPossible(record, user);
					} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
						actionPossible = containerRecordActionsServices.isPrintLabelActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						previousState, $("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_ADD_SELECTION:
				return new MenuItemActionState(VISIBLE);
			case RMRECORDS_DOWNLOAD_ZIP:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isDownloadActionPossible(record, user);
					} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isDownloadActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						previousState, $("RMRecordsMenuItemServices.actionImpossible"));
		}

		return new MenuItemActionState(HIDDEN);
	}

	private MenuItemActionState calculateCorrectActionState(int possibleCount, int notPossibleCount,
															MenuItemActionState previousState, String reason) {
		if (previousState == null) {
			if (possibleCount > 0 && notPossibleCount == 0) {
				return new MenuItemActionState(VISIBLE);
			} else if (possibleCount == 0 && notPossibleCount > 0) {
				return new MenuItemActionState(HIDDEN, reason);
			} else {
				return new MenuItemActionState(DISABLED, reason);
			}
		}

		if (previousState.getStatus() == VISIBLE && notPossibleCount > 1) {
			return new MenuItemActionState(DISABLED, reason);
		} else if (previousState.getStatus() == HIDDEN && possibleCount > 1) {
			return new MenuItemActionState(DISABLED, previousState.getReason());
		}
		return previousState;

		// Logic
		// if old is VISIBLE and 1 not possible then DISABLED with reason
		// if old is HIDDEN and 1 is possible then DISABLED with reason from old
		// if old is empty and all possible then VISIBLE
		// if old is empty and all not possible then HIDDEN with reason
		// if 1 possible and 1 not possible then DISABLED with reason
	}

	private void addMenuItemAction(RMRecordsMenuItemActionType actionType, MenuItemActionState state,
								   MenuItemActionBehaviorParams params, List<MenuItemAction> menuItemActions) {
		MenuItemAction menuItemAction = null;

		switch (actionType) {
			case RMRECORDS_ADD_CART:
				menuItemAction = buildMenuItemAction(RMRECORDS_ADD_CART, state,
						$("ConstellioHeader.selection.actions.addToCart"), null, -1, 100,
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).addToCart(ids, params));
				break;
			case RMRECORDS_MOVE:
				menuItemAction = buildMenuItemAction(RMRECORDS_MOVE, state,
						$("ConstellioHeader.selection.actions.moveInFolder"), null, -1, 200,
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).move(ids, params));
				break;
			case RMRECORDS_COPY:
				menuItemAction = buildMenuItemAction(RMRECORDS_COPY, state,
						$("ConstellioHeader.selection.actions.duplicate"), null, -1, 300,
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).copy(ids, params));
				break;
			case RMRECORDS_CREATE_SIP:
				menuItemAction = buildMenuItemAction(RMRECORDS_CREATE_SIP, state,
						$("SIPButton.caption"), null, -1, 400,
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).createSipArchive(ids, params));
				break;
			case RMRECORDS_SEND_EMAIL:
				menuItemAction = buildMenuItemAction(RMRECORDS_SEND_EMAIL, state,
						$("ConstellioHeader.selection.actions.prepareEmail"), null, -1, 500,
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).sendEmail(ids, params));
				break;
			case RMRECORDS_CREATE_PDF:
				menuItemAction = buildMenuItemAction(RMRECORDS_CREATE_PDF, state,
						$("ConstellioHeader.selection.actions.pdf"), FontAwesome.FILE_PDF_O, -1, 600,
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).createPdf(ids, params));
				break;
			case RMRECORDS_PRINT_LABEL:
				menuItemAction = buildMenuItemAction(RMRECORDS_PRINT_LABEL, state,
						$("SearchView.printLabels"), FontAwesome.PRINT, -1, 700,
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).printLabels(ids, params));
				break;
			case RMRECORDS_ADD_SELECTION:
				menuItemAction = buildMenuItemAction(RMRECORDS_ADD_SELECTION, state,
						$("SearchView.addToSelection"), SELECTION_ICON_RESOURCE, -1, 800,
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).addToSelection(ids, params));
				break;
			case RMRECORDS_DOWNLOAD_ZIP:
				menuItemAction = buildMenuItemAction(RMRECORDS_DOWNLOAD_ZIP, state,
						$("ReportViewer.download", "(zip)"), FontAwesome.FILE_ARCHIVE_O, -1, 900,
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).downloadZip(ids, params));
				break;
		}

		if (menuItemAction != null) {
			menuItemActions.add(menuItemAction);
		}
	}

	private String getSchemaType(LogicalSearchQuery query) {
		List<String> schemaTypes = query.getCondition().getFilterSchemaTypesCodes();
		return schemaTypes != null && !schemaTypes.isEmpty() ? schemaTypes.get(0) : null;
	}

	private String getSchemaType(Record record) {
		return record.getSchemaCode().substring(0, record.getSchemaCode().indexOf("_"));
	}

	private long getRecordWithSupportedSchemaTypeCount(List<Record> records, RMRecordsMenuItemActionType type) {
		return records.stream()
				.filter(r -> type.getSchemaTypes().contains(getSchemaType(r)))
				.count();
	}

	private MenuItemAction buildMenuItemAction(RMRecordsMenuItemActionType type, MenuItemActionState state,
											   String caption, Resource icon, int group, int priority,
											   Consumer<List<String>> command) {
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

	private List<RMRecordsMenuItemActionType> getRMRecordsMenuItemActionTypes(List<String> filteredActionTypes) {
		return Arrays.stream(RMRecordsMenuItemActionType.values())
				.filter(t -> !filteredActionTypes.contains(t.name()))
				.collect(Collectors.toList());
	}

	@AllArgsConstructor
	@Getter
	public enum RMRecordsMenuItemActionType {
		RMRECORDS_ADD_CART(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)),
		RMRECORDS_MOVE(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
		RMRECORDS_COPY(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
		RMRECORDS_CREATE_SIP(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE)),
		RMRECORDS_SEND_EMAIL(singletonList(Document.SCHEMA_TYPE)),
		RMRECORDS_CREATE_PDF(singletonList(Document.SCHEMA_TYPE)),
		RMRECORDS_PRINT_LABEL(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)),
		RMRECORDS_ADD_SELECTION(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE)),
		RMRECORDS_DOWNLOAD_ZIP(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE));

		private final List<String> schemaTypes;

		public static boolean contains(String typeAsString) {
			for (RMRecordsMenuItemActionType type : RMRecordsMenuItemActionType.values()) {
				if (type.name().equals(typeAsString)) {
					return true;
				}
			}
			return false;
		}
	}
}
