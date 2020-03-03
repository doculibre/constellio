package com.constellio.app.ui.pages.events;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class EventCategoriesViewImpl extends BaseViewImpl implements EventCategoriesView {

	public static final String CATEGORY_BUTTON = "seleniumCategoryButton";
	public static final String SYSTEM_USAGE_LINK_BUTTON = "systemUsageLinkButton";
	public static final String USERS_AND_GROUPS_LINK_BUTTON = "usersAndGroupsLinkButton";
	public static final String RECORDS_CREATION_LINK_BUTTON = "recordsCreationLinkButton";
	public static final String RECORDS_MODIFICATION_LINK_BUTTON = "recordsModificationLinkButton";
	public static final String RECORDS_DELETION_LINK_BUTTON = "recordsDeletionLinkButton";
	public static final String CURRENTLY_BORROWED_DOCUMENTS_LINK_BUTTON = "currentlyBorrowedDocumentsLinkButton";
	public static final String BORROWED_DOCUMENTS_LINK_BUTTON = "borrowedDocumentsLinkButton";
	public static final String FILING_SPACE_EVENTS_LINK_BUTTON = "filingSpaceEventsLinkButton";
	public static final String BY_FOLDER_EVENTS_LINK_BUTTON = "byFolderEventsLinkButton";
	public static final String BY_CONTAINER_EVENTS_LINK_BUTTON = "byContainerEventsLinkButton";
	public static final String BY_USER_EVENTS_LINK_BUTTON = "byUserEventsLinkButton";
	public static final String DECOMMISSIONING_EVENTS_LINK_BUTTON = "decommissioningEventsLinkButton";
	public static final String AGENT_EVENTS_LINK_BUTTON = "agentEventsLinkButton";
	public static final String SYSTEM_OPERATION = "systemOperation";
	public static final String REINDEX_AND_RESTART_BUTTON = "reindexAndRestartButton";
	public static final String RECORDS_REQUEST_LINK_BUTTON = "recordRequestLinkButton";

	private boolean agentEventsVisible;

	private EventCategoriesPresenter presenter;

	public EventCategoriesViewImpl() {
		this.presenter = new EventCategoriesPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.viewEntered();
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		CssLayout layout = new CssLayout();

		Button systemUsageLink = newSystemUsageLink();
		systemUsageLink.addStyleName(SYSTEM_USAGE_LINK_BUTTON);
		layout.addComponent(systemUsageLink);

		Button usersAndGroupsLink = newUsersAndGroupsAddOrRemoveLink();
		usersAndGroupsLink.addStyleName(USERS_AND_GROUPS_LINK_BUTTON);
		layout.addComponent(usersAndGroupsLink);

		Button recordsCreationLink = newRecordsCreationLink();
		recordsCreationLink.addStyleName(RECORDS_CREATION_LINK_BUTTON);
		layout.addComponent(recordsCreationLink);

		Button recordsModificationLink = newRecordsModificationLink();
		recordsModificationLink.addStyleName(RECORDS_MODIFICATION_LINK_BUTTON);
		layout.addComponent(recordsModificationLink);

		Button recordsDeletionLink = newRecordsDeletionLink();
		recordsDeletionLink.addStyleName(RECORDS_DELETION_LINK_BUTTON);
		layout.addComponent(recordsDeletionLink);

		Button recordRequestLink = newDocumentRequestLink();
		recordRequestLink.addStyleName(RECORDS_REQUEST_LINK_BUTTON);
		layout.addComponents(recordRequestLink);

		Button borrowedFoldersLink = newBorrowedOrReturnedFoldersEventsLink();
		layout.addComponent(borrowedFoldersLink);

		Button borrowedContainersLink = newBorrowedOrReturnedContainersEventsLink();
		layout.addComponent(borrowedContainersLink);

		Button currentlyBorrowedDocumentsLink = newCurrentlyBorrowedDocumentsLink();
		currentlyBorrowedDocumentsLink.addStyleName(CURRENTLY_BORROWED_DOCUMENTS_LINK_BUTTON);
		layout.addComponent(currentlyBorrowedDocumentsLink);

		Button borrowedDocumentsLink = newBorrowedOrReturnedDocumentsEventsLink();
		borrowedDocumentsLink.addStyleName(BORROWED_DOCUMENTS_LINK_BUTTON);
		layout.addComponent(borrowedDocumentsLink);

		Button filingSpaceEventsLink = newByFilingSpaceEventsLink();
		filingSpaceEventsLink.addStyleName(FILING_SPACE_EVENTS_LINK_BUTTON);
		layout.addComponent(filingSpaceEventsLink);

		Button byFolderEventsLink = newByFolderEventsLink();
		byFolderEventsLink.addStyleName(BY_FOLDER_EVENTS_LINK_BUTTON);
		layout.addComponent(byFolderEventsLink);

		Button byContainerEventsLink = newByContainerEventsLink();
		byContainerEventsLink.addStyleName(BY_CONTAINER_EVENTS_LINK_BUTTON);
		layout.addComponent(byContainerEventsLink);

		Button byUserEventsLink = newByUserEventsLink();
		byUserEventsLink.addStyleName(BY_USER_EVENTS_LINK_BUTTON);
		layout.addComponent(byUserEventsLink);

		Button decommissioningEventsLink = newDecommissioningEventsLink();
		decommissioningEventsLink.addStyleName(DECOMMISSIONING_EVENTS_LINK_BUTTON);
		layout.addComponent(decommissioningEventsLink);

		Button reindexAndRestartEventLink = newReIndexAndRestartLink();
		reindexAndRestartEventLink.addStyleName(REINDEX_AND_RESTART_BUTTON);
		layout.addComponents(reindexAndRestartEventLink);

		Button batchProcessEventLink = newBatchProcessLink();
		batchProcessEventLink.addStyleName(AGENT_EVENTS_LINK_BUTTON);
		layout.addComponents(batchProcessEventLink);

		if (agentEventsVisible) {
			Button agentEventsLink = newAgentEventsLink();
			agentEventsLink.addStyleName(AGENT_EVENTS_LINK_BUTTON);
			layout.addComponent(agentEventsLink);
		}

		if (presenter.isTaskModuleInstalled()) {
			Button tasksEventsLink = newTasksEventsLink();
			tasksEventsLink.addStyleName(AGENT_EVENTS_LINK_BUTTON);
			layout.addComponent(tasksEventsLink);
		}

		VerticalLayout container = new VerticalLayout(layout);
		container.addStyleName("view-group");

		return container;
	}

	@Override
	protected String getTitle() {
		return $("ListEventsView.viewTitle");
	}

	private Button newCurrentlyBorrowedDocumentsLink() {
		return createLink($("ListEventsView.currentlyBorrowedDocuments"), EventCategory.CURRENTLY_BORROWED_DOCUMENTS,
				"document_out");
	}

	private Button newImportExportLink() {
		return createLink($("ListEventsView.importExport"), EventCategory.IMPORT_EXPORT,
				"importExportEvent");
	}

	private Button newCurrentlyBorrowedFoldersLink() {
		return createLink($("ListEventsView.currentlyBorrowedFolders"), EventCategory.CURRENTLY_BORROWED_FOLDERS, "folder_out");
	}

	private Button newBorrowedOrReturnedDocumentsEventsLink() {
		return createLink($("ListEventsView.documentsBorrowOrReturn"), EventCategory.DOCUMENTS_BORROW_OR_RETURN, "document_into");
	}

	private Button newBorrowedOrReturnedFoldersEventsLink() {
		return createLink($("ListEventsView.foldersBorrowOrReturn"), EventCategory.FOLDERS_BORROW_OR_RETURN, "folder_into");
	}

	private Button newBorrowedOrReturnedContainersEventsLink() {
		return createLink($("ListEventsView.containersBorrowOrReturn"), EventCategory.CONTAINERS_BORROW_OR_RETURN, "box_into");
	}

	private Button newByFilingSpaceEventsLink() {
		return createLink($("ListEventsView.eventsByAdministrativeUnit"), EventCategory.EVENTS_BY_ADMINISTRATIVE_UNIT,
				"administrative-unit_clock");
	}

	private Button newByFolderEventsLink() {
		return createLink($("ListEventsView.eventsByFolder"), EventCategory.EVENTS_BY_FOLDER, "folder_time");
	}

	private Button newByContainerEventsLink() {
		return createLink($("ListEventsView.eventsByContainer"), EventCategory.EVENTS_BY_CONTAINER, "container_time");
	}

	private Button newByUserEventsLink() {
		return createLink($("ListEventsView.eventsByUser"), EventCategory.EVENTS_BY_USER, "user_clock");
	}

	private Button newConnectedUsersLink() {
		return createLink($("ListEventsView.connectedUsersEvent"), EventCategory.CONNECTED_USERS_EVENT, "holmes");
	}

	private Button newDecommissioningEventsLink() {
		return createLink($("ListEventsView.decommissioningEvents"), EventCategory.DECOMMISSIONING_EVENTS,
				"platform_truck_clock");
	}

	private Button newAgentEventsLink() {
		return createLink($("ListEventsView.agentEvents"), EventCategory.AGENT_EVENTS,
				"agent_clock");
	}

	private Button newTasksEventsLink() {
		return createLink($("ListEventsView.tasksEvents"), EventCategory.TASKS_EVENTS,
				"task");
	}

	private Button newRecordsDeletionLink() {
		return createLink($("ListEventsView.foldersAndDocumentsDeletion"), EventCategory.FOLDERS_AND_DOCUMENTS_DELETION,
				"folder_document_delete");
	}

	private Button newRecordsModificationLink() {
		return createLink($("ListEventsView.foldersAndDocumentsModification"), EventCategory.FOLDERS_AND_DOCUMENTS_MODIFICATION,
				"folder_document_edit");
	}

	private Button newSystemUsageLink() {
		return createLink($("ListEventsView.systemUsage"), EventCategory.SYSTEM_USAGE, "radar");
	}

	private Button newUsersAndGroupsAddOrRemoveLink() {
		return createLink($("ListEventsView.usersAndGroupsAddOrRemoveEvents"), EventCategory.USERS_AND_GROUPS_ADD_OR_REMOVE,
				"group_into");
	}

	private Button newRecordsCreationLink() {
		return createLink($("ListEventsView.foldersAndDocumentsCreation"), EventCategory.FOLDERS_AND_DOCUMENTS_CREATION,
				"folder_document_new");
	}

	private Button newReIndexAndRestartLink() {
		return createLink($("ListEventsView.reIndexAndRestart"), EventCategory.REINDEX_AND_RESTART,
				"system-reboot-reindex");
	}

	private Button newDocumentRequestLink() {
		return createLink($("ListEventsView.requestTask"), EventCategory.REQUEST_TASKS, "borrowing-audit");
	}

	private Button newBatchProcessLink() {
		return createLink($("ListEventsView.batchProcessEvents"), EventCategory.BATCH_PROCESS_EVENTS, "traitementenlot");
	}

	private Button createLink(String caption, final EventCategory eventCategory, String iconName) {
		Button returnLink = new Button(caption, new ThemeResource("images/icons/logs/" + iconName + ".png"));
		returnLink.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		returnLink.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		returnLink.addStyleName(CATEGORY_BUTTON);
		returnLink.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.eventButtonClicked(eventCategory);
			}
		});
		return returnLink;
	}

	@Override
	public void setAgentEventsVisible(boolean visible) {
		this.agentEventsVisible = visible;
	}

}
