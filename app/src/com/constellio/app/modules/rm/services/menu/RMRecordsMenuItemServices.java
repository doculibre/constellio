package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.ContainerRecordActionsServices;
import com.constellio.app.modules.rm.services.actions.DocumentRecordActionsServices;
import com.constellio.app.modules.rm.services.actions.FolderRecordActionsServices;
import com.constellio.app.modules.rm.services.menu.behaviors.RMRecordsMenuItemBehaviors;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.services.actions.TaskRecordActionsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_ADD_CART;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_ADD_SELECTION;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_BATCH_DELETE;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CHECKIN;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CHECKOUT;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_BATCH_UNSHARE;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CONSULT_LINK;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_COPY;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CREATE_PDF;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CREATE_SIP;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CREATE_TASK;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_DOWNLOAD_ZIP;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_MOVE;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_PRINT_LABEL;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_SEND_EMAIL;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.DISABLED;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.getLanguage;
import static com.vaadin.server.FontAwesome.STAR;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class RMRecordsMenuItemServices {

	private String collection;
	private AppLayerFactory appLayerFactory;

	private DocumentRecordActionsServices documentRecordActionsServices;
	private FolderRecordActionsServices folderRecordActionsServices;
	private ContainerRecordActionsServices containerRecordActionsServices;
	private TaskRecordActionsServices taskRecordActionsServices;
	private RMSchemasRecordsServices rm;

	//public static final Resource SELECTION_ICON_RESOURCE = new ThemeResource("images/icons/clipboard_12x16.png");
	public static final Resource SELECTION_ICON_RESOURCE = FontAwesome.SHOPPING_BASKET;

	public RMRecordsMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		documentRecordActionsServices = new DocumentRecordActionsServices(collection, appLayerFactory);
		folderRecordActionsServices = new FolderRecordActionsServices(collection, appLayerFactory);
		containerRecordActionsServices = new ContainerRecordActionsServices(collection, appLayerFactory);
		taskRecordActionsServices = new TaskRecordActionsServices(collection, appLayerFactory);
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecords(List<Record> records, User user, List<String> excludedActionTypes,
													 MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		List<RMRecordsMenuItemActionType> actionTypes = getRMRecordsMenuItemActionTypes(excludedActionTypes);
		for (RMRecordsMenuItemActionType actionType : actionTypes) {
			MenuItemActionState state = getMenuItemActionStateForRecords(actionType, records, user, params);
			addMenuItemAction(actionType, state, params, menuItemActions);
		}

		return menuItemActions;
	}

	public MenuItemActionState getMenuItemActionStateForRecords(RMRecordsMenuItemActionType menuItemActionType,
																List<Record> records, User user,
																MenuItemActionBehaviorParams params) {
		return computeActionState(menuItemActionType, records, user, params);
	}

	private MenuItemActionState computeActionState(RMRecordsMenuItemActionType menuItemActionType, List<Record> records,
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
			List<String> schemaTypes = getLocalizedSchemaTypes(menuItemActionType.getSchemaTypes());
			return new MenuItemActionState(DISABLED, $("RMRecordsMenuItemServices.unsupportedSchema",
					StringUtils.join(schemaTypes, ", ")));
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
						$("RMRecordsMenuItemServices.actionImpossible"));
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
						$("RMRecordsMenuItemServices.actionImpossible"));
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
						$("RMRecordsMenuItemServices.actionImpossible"));
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
						$("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_SEND_EMAIL:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isSendEmailActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						$("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_CONSULT_LINK:
				int numberOfTaskRecord = 0;
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isConsultLinkActionPossible(record, user);
					} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isConsultLinkActionPossible(record, user);
					} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
						actionPossible = containerRecordActionsServices.isConsultLinkActionPossible(record, user);
					} else if (record.isOfSchemaType(RMTask.SCHEMA_TYPE)) {
						actionPossible = taskRecordActionsServices.isConsultLinkActionPossible(record, user);
						numberOfTaskRecord++;
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				if (numberOfTaskRecord != records.size()) {
					return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
							$("RMRecordsMenuItemServices.actionImpossible"));
				} else {
					return MenuItemActionState.visibleOrHidden(false);
				}
			case RMRECORDS_CREATE_PDF:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isCreatePdfActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						$("RMRecordsMenuItemServices.actionImpossible"));
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
						$("RMRecordsMenuItemServices.actionImpossible"));
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
						$("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_BATCH_DELETE:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isDeleteActionPossible(record, user);
					} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isDeleteActionPossible(record, user);
					} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
						actionPossible = containerRecordActionsServices.isDeleteActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						$("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_CREATE_TASK:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isCreateTaskActionPossible(record, user);
					} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isCreateTaskActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						$("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_CHECKOUT:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isCheckOutActionPossible(record, user);
						if (!actionPossible) {
							actionPossible = documentRecordActionsServices.isCurrentBorrower(record, user);
						}
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						$("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_CHECKIN:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isCheckInActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						$("RMRecordsMenuItemServices.actionImpossible"));
		}

		return new MenuItemActionState(HIDDEN);
	}

	private int getRecordsLimit(RMRecordsMenuItemActionType menuItemActionType) {
		return menuItemActionType.getRecordsLimit();
	}

	private MenuItemActionState calculateCorrectActionState(int possibleCount, int notPossibleCount, String reason) {
		if (possibleCount > 0 && notPossibleCount == 0) {
			return new MenuItemActionState(VISIBLE);
		} else if (possibleCount == 0 && notPossibleCount > 0) {
			return new MenuItemActionState(HIDDEN, reason);
		}
		return new MenuItemActionState(DISABLED, reason);
	}

	private void addMenuItemAction(RMRecordsMenuItemActionType actionType, MenuItemActionState state,
								   MenuItemActionBehaviorParams params, List<MenuItemAction> menuItemActions) {
		MenuItemAction menuItemAction = null;

		switch (actionType) {
			case RMRECORDS_ADD_CART:
				menuItemAction = buildMenuItemAction(RMRECORDS_ADD_CART, state,
						$("ConstellioHeader.selection.actions.addToCart"), STAR, -1, 100,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).addToCart(ids, params));
				break;
			case RMRECORDS_MOVE:
				menuItemAction = buildMenuItemAction(RMRECORDS_MOVE, state,
						$("ConstellioHeader.selection.actions.moveInFolder"), null, -1, 200,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).move(ids, params));
				break;
			case RMRECORDS_COPY:
				menuItemAction = buildMenuItemAction(RMRECORDS_COPY, state,
						$("ConstellioHeader.selection.actions.duplicate"), null, -1, 300,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).copy(ids, params));
				break;
			case RMRECORDS_CREATE_SIP:
				menuItemAction = buildMenuItemAction(RMRECORDS_CREATE_SIP, state,
						$("SIPButton.caption"), FontAwesome.FILE_ARCHIVE_O, -1, 400,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).createSipArchive(ids, params));
				break;
			case RMRECORDS_SEND_EMAIL:
				menuItemAction = buildMenuItemAction(RMRECORDS_SEND_EMAIL, state,
						$("ConstellioHeader.selection.actions.prepareEmail"), null, -1, 500,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).sendEmail(ids, params));
				break;

			case RMRECORDS_CONSULT_LINK:
				menuItemAction = buildMenuItemAction(RMRECORDS_CONSULT_LINK, state,
						$("consultationLink"), FontAwesome.LINK, -1, 510,
						getRecordsLimit(actionType), (ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).showConsultLink(ids, params));
				break;
			case RMRECORDS_CREATE_PDF:
				menuItemAction = buildMenuItemAction(RMRECORDS_CREATE_PDF, state,
						$("ConstellioHeader.selection.actions.pdf"), FontAwesome.FILE_PDF_O, -1, 600,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).createPdf(ids, params));
				break;
			case RMRECORDS_PRINT_LABEL:
				menuItemAction = buildMenuItemAction(RMRECORDS_PRINT_LABEL, state,
						$("SearchView.printLabels"), FontAwesome.PRINT, -1, 700,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).printLabels(ids, params));
				break;
			case RMRECORDS_ADD_SELECTION:
				menuItemAction = buildMenuItemAction(RMRECORDS_ADD_SELECTION, state,
						$("SearchView.addToSelection"), SELECTION_ICON_RESOURCE, -1, 800,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).addToSelection(ids, params));
				break;
			case RMRECORDS_DOWNLOAD_ZIP:
				menuItemAction = buildMenuItemAction(RMRECORDS_DOWNLOAD_ZIP, state,
						$("ReportViewer.download", "(zip)"), FontAwesome.FILE_ARCHIVE_O, -1, 900,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).downloadZip(ids, params));
				break;
			case RMRECORDS_BATCH_DELETE:
				menuItemAction = buildMenuItemAction(RMRECORDS_BATCH_DELETE, state,
						$("deleteWithIcon"), null, -1, 1000,
						getRecordsLimit(actionType), (ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).batchDelete(ids, params));
				break;
			case RMRECORDS_CHECKOUT:
				menuItemAction = buildMenuItemAction(RMRECORDS_CHECKOUT, state,
						$("DocumentContextMenu.checkOut"), FontAwesome.LOCK, -1, 1100,
						getRecordsLimit(actionType), (ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).checkoutDocuments(ids, params));
				break;
			case RMRECORDS_CREATE_TASK:
				menuItemAction = buildMenuItemAction(RMRECORDS_CREATE_TASK, state,
						$("ConstellioHeader.selection.actions.createTask"), FontAwesome.TASKS, -1, 1100,
						getRecordsLimit(actionType), (ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).createTask(ids, params));
				break;
			case RMRECORDS_BATCH_UNSHARE:
				menuItemAction = buildMenuItemAction(RMRECORDS_BATCH_UNSHARE, state,
						$("unshare"), FontAwesome.SHARE_SQUARE_O, -1, 1100,
						getRecordsLimit(actionType), (ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).batchUnshare(ids, params));
				break;
			case RMRECORDS_CHECKIN:
				menuItemAction = buildMenuItemAction(RMRECORDS_CHECKIN, state,
						$("DocumentContextMenu.checkIn"), FontAwesome.UNLOCK, -1, 1200,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).checkInDocuments(ids, params));
		}

		if (menuItemAction != null) {
			menuItemActions.add(menuItemAction);
		}
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

	private List<RMRecordsMenuItemActionType> getRMRecordsMenuItemActionTypes(List<String> excludedActionTypes) {
		return Arrays.stream(RMRecordsMenuItemActionType.values())
				.filter(t -> !excludedActionTypes.contains(t.name()))
				.collect(Collectors.toList());
	}

	public List<String> getLocalizedSchemaTypes(List<String> schemaTypes) {
		ArrayList<String> localizedSchemas = new ArrayList<>();
		MetadataSchemasManager schemaManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		for (String schema : schemaTypes) {
			localizedSchemas.add(schemaManager.getSchemaTypes(collection).getSchemaType(schema).getLabel(getLanguage()));
		}
		return localizedSchemas;
	}


	@AllArgsConstructor
	@Getter
	public enum RMRecordsMenuItemActionType {
		RMRECORDS_ADD_CART(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 100000),
		RMRECORDS_MOVE(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE), 100000),
		RMRECORDS_COPY(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE), 100000),
		RMRECORDS_CREATE_SIP(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE), 100000),
		RMRECORDS_SEND_EMAIL(singletonList(Document.SCHEMA_TYPE), 100000),
		RMRECORDS_CREATE_PDF(singletonList(Document.SCHEMA_TYPE), 100000),
		RMRECORDS_PRINT_LABEL(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 100000),
		RMRECORDS_ADD_SELECTION(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 100000),
		RMRECORDS_DOWNLOAD_ZIP(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE), 100000),
		RMRECORDS_BATCH_DELETE(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 100000),
		RMRECORDS_CONSULT_LINK(asList(RMTask.SCHEMA_TYPE, Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 10000),
		RMRECORDS_CHECKOUT(asList(Document.SCHEMA_TYPE), 25),
		RMRECORDS_CHECKIN(asList(Document.SCHEMA_TYPE), 25),
		RMRECORDS_CREATE_TASK(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE), 10000),
		RMRECORDS_BATCH_UNSHARE(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE), 10000);

		private final List<String> schemaTypes;
		private final int recordsLimit;

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
