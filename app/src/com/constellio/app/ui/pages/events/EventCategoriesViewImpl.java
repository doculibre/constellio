/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.events;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.themes.ValoTheme;

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
	public static final String BY_USER_EVENTS_LINK_BUTTON = "byUserEventsLinkButton";
	public static final String DECOMMISSIONING_EVENTS_LINK_BUTTON = "decommissioningEventsLinkButton";
	public static final String AGENT_EVENTS_LINK_BUTTON = "agentEventsLinkButton";
	
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
		CssLayout mainLayout = new CssLayout();
		mainLayout.addStyleName("view-group");

		Button systemUsageLink = newSystemUsageLink();
		systemUsageLink.addStyleName(SYSTEM_USAGE_LINK_BUTTON);
		mainLayout.addComponent(systemUsageLink);

		Button usersAndGroupsLink = newUsersAndGroupsAddOrRemoveLink();
		usersAndGroupsLink.addStyleName(USERS_AND_GROUPS_LINK_BUTTON);
		mainLayout.addComponent(usersAndGroupsLink);

		Button recordsCreationLink = newRecordsCreationLink();
		recordsCreationLink.addStyleName(RECORDS_CREATION_LINK_BUTTON);
		mainLayout.addComponent(recordsCreationLink);

		Button recordsModificationLink = newRecordsModificationLink();
		recordsModificationLink.addStyleName(RECORDS_MODIFICATION_LINK_BUTTON);
		mainLayout.addComponent(recordsModificationLink);

		Button recordsDeletionLink = newRecordsDeletionLink();
		recordsDeletionLink.addStyleName(RECORDS_DELETION_LINK_BUTTON);
		mainLayout.addComponent(recordsDeletionLink);

		Button currentlyBorrowedDocumentsLink = newCurrentlyBorrowedDocumentsLink();
		currentlyBorrowedDocumentsLink.addStyleName(CURRENTLY_BORROWED_DOCUMENTS_LINK_BUTTON);
		mainLayout.addComponent(currentlyBorrowedDocumentsLink);

		//		Button currentlyBorrowedFoldersLink = newCurrentlyBorrowedFoldersLink();
		//		currentlyBorrowedDocumentsLink.addStyleName("currentlyBorrowedFoldersLinkButton");
		//		mainLayout.addComponent(currentlyBorrowedFoldersLink);

		Button borrowedDocumentsLink = newBorrowedOrReturnedDocumentsEventsLink();
		borrowedDocumentsLink.addStyleName(BORROWED_DOCUMENTS_LINK_BUTTON);
		mainLayout.addComponent(borrowedDocumentsLink);

		//		Button borrowedFoldersLink = newBorrowedOrReturnedFoldersEventsLink();
		//		page.addComponent(borrowedFoldersLink);

		//		Button borrowedContainersLink = newBorrowedOrReturnedContainersEventsLink();
		//		page.addComponent(borrowedContainersLink);

		Button filingSpaceEventsLink = newByFilingSpaceEventsLink();
		filingSpaceEventsLink.addStyleName(FILING_SPACE_EVENTS_LINK_BUTTON);
		mainLayout.addComponent(filingSpaceEventsLink);

		Button byFolderEventsLink = newByFolderEventsLink();
		byFolderEventsLink.addStyleName(BY_FOLDER_EVENTS_LINK_BUTTON);
		mainLayout.addComponent(byFolderEventsLink);

		Button byUserEventsLink = newByUserEventsLink();
		byUserEventsLink.addStyleName(BY_USER_EVENTS_LINK_BUTTON);
		mainLayout.addComponent(byUserEventsLink);

		//		Button connectedUsersLink = newConnectedUsersLink();
		//		page.addComponent(connectedUsersLink);

		Button decommissioningEventsLink = newDecommissioningEventsLink();
		decommissioningEventsLink.addStyleName(DECOMMISSIONING_EVENTS_LINK_BUTTON);
		mainLayout.addComponent(decommissioningEventsLink);

		if (agentEventsVisible) {
			Button agentEventsLink = newAgentEventsLink();
			agentEventsLink.addStyleName(AGENT_EVENTS_LINK_BUTTON);
			mainLayout.addComponent(agentEventsLink);
		}

		return mainLayout;
	}

	@Override
	protected String getTitle() {
		return $("ListEventsView.viewTitle");
	}

	private Button newCurrentlyBorrowedDocumentsLink() {
		return createLink($("ListEventsView.currentlyBorrowedDocuments"), EventCategory.CURRENTLY_BORROWED_DOCUMENTS,
				"document_out");
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

	private Button createLink(String caption, final EventCategory eventCategory, String iconName) {
		Button returnLink = new Button(caption, new ThemeResource("images/icons/experience/logs/" + iconName + ".png"));
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
