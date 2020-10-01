package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.ContainerRecordActionsServices;
import com.constellio.app.modules.rm.services.actions.DocumentRecordActionsServices;
import com.constellio.app.modules.rm.services.actions.FolderRecordActionsServices;
import com.constellio.app.modules.rm.services.actions.StorageSpaceRecordActionsServices;
import com.constellio.app.modules.rm.services.menu.behaviors.RMRecordsMenuItemBehaviors;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.actions.TaskRecordActionsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.ActionDisplayOption;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.pages.base.SessionContext;
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
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_BATCH_UNPUBLISH;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_BATCH_UNSHARE;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_BORROW;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_BORROW_REQUEST;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CANCEL_RETURN;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CHECKIN;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CONSULT_LINK;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_COPY;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CREATE_PDF;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CREATE_SIP;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_CREATE_TASK;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_DOWNLOAD_ZIP;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_GENERATE_REPORT;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_MOVE;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_PRINT_LABEL;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_PUT_IN_CONTAINER;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_REMOVE_SELECTION;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_RETURN;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_RETURN_REMAINDER;
import static com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType.RMRECORDS_RETURN_REQUEST;
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
	private StorageSpaceRecordActionsServices storageSpaceRecordActionsServices;
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
		storageSpaceRecordActionsServices = new StorageSpaceRecordActionsServices(collection, appLayerFactory);
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

		if (!menuItemActionType.allowDifferentSchemaType) {
			String selectedSchemaType = null;
			for (Record record : records) {
				String recordSchemaType = null;
				for (String schemaType : menuItemActionType.schemaTypes) {
					if (record.isOfSchemaType(schemaType)) {
						recordSchemaType = schemaType;
						break;
					}
				}

				if (selectedSchemaType == null) {
					selectedSchemaType = recordSchemaType;
				} else if (!selectedSchemaType.equals(recordSchemaType)) {
					return new MenuItemActionState(DISABLED, $("RMRecordsMenuItemServices.actionImpossibleOnDifferentSchema"));
				}
			}
		}

		SessionContext sessionContext;
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
					} else if (record.isOfSchemaType(StorageSpace.SCHEMA_TYPE)) {
						actionPossible = storageSpaceRecordActionsServices.isConsultLinkActionPossible(record, user);
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
			case RMRECORDS_BORROW:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isCheckOutActionPossible(record, user);
					} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isBorrowActionPossible(record, user);
					} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
						actionPossible = containerRecordActionsServices.isBorrowActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						$("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_BORROW_REQUEST:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isBorrowRequestActionPossible(record, user);
					} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
						actionPossible = containerRecordActionsServices.isBorrowRequestActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						$("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_RETURN:
			case RMRECORDS_CANCEL_RETURN:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isCheckInActionPossible(record, user);
					} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isReturnActionPossible(record, user);
					} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
						actionPossible = containerRecordActionsServices.isCheckInActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						$("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_RETURN_REQUEST:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isReturnRequestActionPossible(record, user);
					} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
						actionPossible = containerRecordActionsServices.isReturnRequestActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						$("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_RETURN_REMAINDER:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isSendReturnReminderActionPossible(record, user);
					} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isSendReturnReminderActionPossible(record, user);
					} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
						actionPossible = containerRecordActionsServices.isSendReturnReminderActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						$("RMRecordsMenuItemServices.actionImpossible"), ActionDisplayOption.REQUIRE_VISIBLE_FOR_ONE_RECORD);
			case RMRECORDS_ADD_SELECTION:
				sessionContext = params.getView() != null ? params.getView().getSessionContext() : null;
				if (sessionContext != null &&
					(sessionContext.getSelectedRecordIds() == null ||
					 !sessionContext.getSelectedRecordIds()
							 .containsAll(records.stream().map(Record::getId).collect(Collectors.toList())))) {
					return new MenuItemActionState(VISIBLE);
				} else {
					return new MenuItemActionState(HIDDEN);
				}
			case RMRECORDS_REMOVE_SELECTION:
				sessionContext = params.getView() != null ? params.getView().getSessionContext() : null;
				if (sessionContext != null &&
					sessionContext.getSelectedRecordIds() != null &&
					sessionContext.getSelectedRecordIds()
							.containsAll(records.stream().map(Record::getId).collect(Collectors.toList()))) {
					return new MenuItemActionState(VISIBLE);
				} else {
					return new MenuItemActionState(HIDDEN);
				}
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
			//			case RMRECORDS_CHECKOUT:
			//				int ignoredCount = 0;
			//				for (Record record : records) {
			//					boolean actionPossible = false;
			//					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
			//						actionPossible = documentRecordActionsServices.isCheckOutActionPossible(record, user);
			//						if (!actionPossible) {
			//							actionPossible = documentRecordActionsServices.isCurrentBorrower(record, user);
			//							if (actionPossible) {
			//								ignoredCount++;
			//							}
			//						}
			//					}
			//					possibleCount += actionPossible ? 1 : 0;
			//				}
			//				return calculateCorrectActionState(possibleCount - ignoredCount,
			//						ignoredCount == possibleCount && ignoredCount != 0 ? ignoredCount : (records.size() - possibleCount),
			//						$("RMRecordsMenuItemServices.actionImpossible"));
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
			case RMRECORDS_BATCH_UNSHARE:
				for (Record record : records) {
					boolean actionPossible = false;
					if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
						actionPossible = documentRecordActionsServices.isUnshareActionPossible(record, user);
					} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
						actionPossible = folderRecordActionsServices.isUnshareActionPossible(record, user);
					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount, records.size() - possibleCount,
						$("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_BATCH_UNPUBLISH:
				for (Record record : records) {
					boolean actionPossible = documentRecordActionsServices.isUnPublishActionPossible(record, user);
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount,
						records.size() - possibleCount, $("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_GENERATE_REPORT:
				for (Record record : records) {
					boolean actionPossible = false;
					switch (record.getSchemaCode().split("_")[0]) {
						case Document.SCHEMA_TYPE:
							actionPossible = documentRecordActionsServices.isGenerateReportActionPossible(record, user);
							break;
						case Folder.SCHEMA_TYPE:
							actionPossible = folderRecordActionsServices.isGenerateReportActionPossible(record, user);
							break;
						case Task.SCHEMA_TYPE:
							actionPossible = taskRecordActionsServices.isGenerateReportActionPossible(record, user);
							break;
						case ContainerRecord.SCHEMA_TYPE:
							actionPossible = containerRecordActionsServices.isGenerateReportActionPossible(record, user);
							break;
						case StorageSpace.SCHEMA_TYPE:
							actionPossible = storageSpaceRecordActionsServices.isGenerateReportActionPossible(record, user);
							break;

						case RetentionRule.SCHEMA_TYPE:
							actionPossible = true;
							break;

						case Category.SCHEMA_TYPE:
							actionPossible = true;
							break;

					}
					possibleCount += actionPossible ? 1 : 0;
				}
				return calculateCorrectActionState(possibleCount,
						records.size() - possibleCount, $("RMRecordsMenuItemServices.actionImpossible"));
			case RMRECORDS_PUT_IN_CONTAINER:
				for (Record record : records) {
					boolean actionPossible = folderRecordActionsServices.isPutInContainerActionPossible(record, user);
					possibleCount += actionPossible ? 1 : 0;
				}

				return calculateCorrectActionState(possibleCount,
						records.size() - possibleCount, $("RMRecordsMenuItemServices.actionImpossible"));
		}

		return new MenuItemActionState(HIDDEN);
	}

	private int getRecordsLimit(RMRecordsMenuItemActionType menuItemActionType) {
		return menuItemActionType.getRecordsLimit();
	}

	private MenuItemActionState calculateCorrectActionState(int possibleCount, int notPossibleCount, String reason) {
		return calculateCorrectActionState(possibleCount, notPossibleCount, reason, ActionDisplayOption.REQUIRE_VISIBLE_FOR_ALL_RECORDS);
	}

	private MenuItemActionState calculateCorrectActionState(int possibleCount, int notPossibleCount, String reason,
															ActionDisplayOption displayOption) {
		if (possibleCount > 0 && (displayOption == ActionDisplayOption.REQUIRE_VISIBLE_FOR_ONE_RECORD || notPossibleCount == 0)) {
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
			case RMRECORDS_BORROW:
				menuItemAction = buildMenuItemAction(RMRECORDS_BORROW, state,
						$("DocumentContextMenu.checkOut"), FontAwesome.LOCK, -1, 700,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).checkOut(ids, params));
				break;
			case RMRECORDS_BORROW_REQUEST:
				menuItemAction = buildMenuItemAction(RMRECORDS_BORROW_REQUEST, state,
						$("RMRequestTaskButtonExtension.borrowRequest"), null, -1, 610,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).checkOutRequest(ids, params));
				break;
			case RMRECORDS_RETURN:
				menuItemAction = buildMenuItemAction(RMRECORDS_RETURN, state,
						$("DocumentContextMenu.checkIn"), FontAwesome.UNLOCK, -1, 740,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).checkIn(ids, params));
				break;
			case RMRECORDS_CANCEL_RETURN:
				menuItemAction = buildMenuItemAction(RMRECORDS_CANCEL_RETURN, state,
						$("DocumentContextMenu.cancelCheckOut"), null, -1, 750,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).checkIn(ids, params));
				break;
			case RMRECORDS_RETURN_REQUEST:
				menuItemAction = buildMenuItemAction(RMRECORDS_RETURN_REQUEST, state,
						$("RMRequestTaskButtonExtension.returnRequest"), null, -1, 800,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).checkInRequest(ids, params));
				break;
			case RMRECORDS_RETURN_REMAINDER:
				menuItemAction = buildMenuItemAction(RMRECORDS_RETURN_REMAINDER, state,
						$("SendReturnReminderEmailButton.reminderReturn"), null, -1, 780,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).sendReturnRemainder(ids, params));
				break;
			case RMRECORDS_ADD_SELECTION:
				menuItemAction = buildMenuItemAction(RMRECORDS_ADD_SELECTION, state,
						$("SearchView.addToSelection"), SELECTION_ICON_RESOURCE, -1, 800,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).addToSelection(ids, params));
				break;
			case RMRECORDS_REMOVE_SELECTION:
				menuItemAction = buildMenuItemAction(RMRECORDS_REMOVE_SELECTION, state,
						$("SearchView.removeFromSelection"), SELECTION_ICON_RESOURCE, -1, 850,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).removeFromSelection(ids, params));
				break;
			case RMRECORDS_DOWNLOAD_ZIP:
				menuItemAction = buildMenuItemAction(RMRECORDS_DOWNLOAD_ZIP, state,
						$("ReportViewer.download", "(zip)"), FontAwesome.FILE_ARCHIVE_O, -1, 900,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).downloadZip(ids, params));
				break;
			case RMRECORDS_BATCH_DELETE:
				menuItemAction = buildMenuItemAction(RMRECORDS_BATCH_DELETE, state,
						$("deleteWithIcon"), null, -1, Integer.MAX_VALUE,
						getRecordsLimit(actionType), (ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).batchDelete(ids, params));
				break;
			case RMRECORDS_CREATE_TASK:
				menuItemAction = buildMenuItemAction(RMRECORDS_CREATE_TASK, state,
						$("ConstellioHeader.selection.actions.createTask"), FontAwesome.TASKS, -1, 1100,
						getRecordsLimit(actionType), (ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).createTask(ids, params));
				break;
			case RMRECORDS_BATCH_UNSHARE:
				menuItemAction = buildMenuItemAction(RMRECORDS_BATCH_UNSHARE, state,
						$("DocumentContextMenu.batchunshare"), FontAwesome.REPLY, -1, 1200,
						getRecordsLimit(actionType), (ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).batchUnshare(ids, params));
				break;
			case RMRECORDS_CHECKIN:
				menuItemAction = buildMenuItemAction(RMRECORDS_CHECKIN, state,
						$("DocumentContextMenu.checkIn"), FontAwesome.UNLOCK, -1, 1200,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).checkInDocuments(ids, params));
				break;
			case RMRECORDS_BATCH_UNPUBLISH:
				menuItemAction = buildMenuItemAction(RMRECORDS_BATCH_UNPUBLISH, state, $("DocumentContextMenu.batchunPublish"), FontAwesome.GLOBE, -1, 1300,
						getRecordsLimit(actionType), (ids -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).batchUnPublishDocument(ids, params)));
				break;
			case RMRECORDS_GENERATE_REPORT:
				menuItemAction = buildMenuItemAction(RMRECORDS_GENERATE_REPORT, state,
						$("DocumentContextMenu.ReportGeneratorButton"), FontAwesome.LIST_ALT, -1, 2400,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).generateReport(ids, params));

				break;
			case RMRECORDS_PUT_IN_CONTAINER:
				menuItemAction = buildMenuItemAction(RMRECORDS_PUT_IN_CONTAINER, state, $("ContainersButton.containerAssigner"), FontAwesome.ARCHIVE, -1, 2600,
						getRecordsLimit(actionType),
						(ids) -> new RMRecordsMenuItemBehaviors(collection, appLayerFactory).putInContainer(ids, params));
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
		RMRECORDS_ADD_CART(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 100000, true),
		RMRECORDS_MOVE(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE), 100000, true),
		RMRECORDS_COPY(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE), 100000, true),
		RMRECORDS_CREATE_SIP(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE), 100000, true),
		RMRECORDS_SEND_EMAIL(singletonList(Document.SCHEMA_TYPE), 100000, true),
		RMRECORDS_CREATE_PDF(singletonList(Document.SCHEMA_TYPE), 100000, true),
		RMRECORDS_PRINT_LABEL(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 100000, false),
		RMRECORDS_BORROW(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 100000, false),
		RMRECORDS_BORROW_REQUEST(asList(Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 100000, false),
		RMRECORDS_RETURN(asList(Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 100000, false),
		RMRECORDS_CANCEL_RETURN(asList(Document.SCHEMA_TYPE), 100000, false),
		RMRECORDS_RETURN_REQUEST(asList(Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 100000, false),
		RMRECORDS_RETURN_REMAINDER(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 100000, false),
		RMRECORDS_ADD_SELECTION(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 100000, true),
		RMRECORDS_REMOVE_SELECTION(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 100000, true),
		RMRECORDS_DOWNLOAD_ZIP(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE), 100000, true),
		RMRECORDS_BATCH_DELETE(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE), 100000, true),
		RMRECORDS_CONSULT_LINK(asList(RMTask.SCHEMA_TYPE, Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE, StorageSpace.SCHEMA_TYPE), 10000, true),
		//RMRECORDS_CHECKOUT(asList(Document.SCHEMA_TYPE), 25, false),
		RMRECORDS_CHECKIN(asList(Document.SCHEMA_TYPE), 25, false),
		RMRECORDS_CREATE_TASK(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE), 10000, true),
		RMRECORDS_BATCH_UNSHARE(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE), 10000, false),
		RMRECORDS_BATCH_UNPUBLISH(asList(Document.SCHEMA_TYPE), 10000, false),
		RMRECORDS_GENERATE_REPORT(asList(Document.SCHEMA_TYPE, Folder.SCHEMA_TYPE, Task.SCHEMA_TYPE,
				ContainerRecord.SCHEMA_TYPE, StorageSpace.SCHEMA_TYPE), 10000, false),
		RMRECORDS_PUT_IN_CONTAINER(asList(Folder.SCHEMA_TYPE), 10000, false);

		private final List<String> schemaTypes;
		private final int recordsLimit;
		private final boolean allowDifferentSchemaType;

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
