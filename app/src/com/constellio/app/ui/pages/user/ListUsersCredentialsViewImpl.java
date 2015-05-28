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
package com.constellio.app.ui.pages.user;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.TableStringFilter;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.UserCredentialVOLazyContainer;
import com.constellio.app.ui.framework.data.UserCredentialVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class ListUsersCredentialsViewImpl extends BaseViewImpl implements ListUsersCredentialsView {

	public static final String ADMIN = "admin";
	private ListUserCredentialsPresenter presenter;
	private static final String PROPERTY_BUTTONS = "buttons";
	private VerticalLayout viewLayout;

	private HorizontalLayout filterAndAddButtonLayout;
	private TableStringFilter tableFilter;
	private Button addButton;
	private Table table;
	private UserCredentialStatus status;
	private final int batchSize = 100;

	public ListUsersCredentialsViewImpl() {
		this.presenter = new ListUserCredentialsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListUserCredentialsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.setSpacing(true);

		filterAndAddButtonLayout = new HorizontalLayout();
		filterAndAddButtonLayout.setWidth("100%");

		addButton = new AddButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};
		addButton.setEnabled(presenter.canAddOrModify());
		table = buildTable(UserCredentialStatus.ACTIVE);
		tableFilter = new TableStringFilter(table);
		OptionGroup statusFilter = new OptionGroup();
		statusFilter.addStyleName("horizontal");
		statusFilter.addStyleName("status");
		for (UserCredentialStatus status : UserCredentialStatus.values()) {
			statusFilter.addItem(status);
			statusFilter.setItemCaption(status, $("UserCredentialView.status." + status.getCode()));
			if (this.status == null) {
				statusFilter.setValue(UserCredentialStatus.ACTIVE);
			} else if (status == this.status) {
				statusFilter.setValue(status);
			}
		}
		statusFilter.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				status = (UserCredentialStatus) event.getProperty().getValue();
				refreshTable();
			}
		});

		viewLayout.addComponents(filterAndAddButtonLayout, statusFilter, table);
		viewLayout.setExpandRatio(table, 1);

		filterAndAddButtonLayout.addComponents(tableFilter, addButton);
		filterAndAddButtonLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);

		return viewLayout;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	Table buildTable(UserCredentialStatus status) {
		final UserCredentialVODataProvider dataProvider = presenter.getDataProvider();

		List<UserCredentialVO> userCredentialVOs = dataProvider.listUserCredentialVOsWithStatus(status);
		dataProvider.setUserCredentialVOs(userCredentialVOs);

		Filterable tableContainer = new UserCredentialVOLazyContainer(dataProvider, batchSize);
		ButtonsContainer buttonsContainer = new ButtonsContainer(tableContainer, PROPERTY_BUTTONS);
		addButtons(dataProvider, buttonsContainer);
		tableContainer = buttonsContainer;

		Table table = new Table($("ListUserCredentialsView.viewTitle"), tableContainer);
		int tableSize = batchSize;
		if (tableSize > table.getItemIds().size()) {
			tableSize = table.getItemIds().size();
		}
		table.setPageLength(tableSize);
		table.setWidth("100%");
		table.setColumnHeader("username", $("ListUsersCredentialsView.usernameColumn"));
		table.setColumnHeader("firstName", $("ListUsersCredentialsView.firstNameColumn"));
		table.setColumnHeader("lastName", $("ListUsersCredentialsView.lastNameColumn"));
		table.setColumnHeader("email", $("ListUsersCredentialsView.emailColumn"));
		table.setColumnHeader(PROPERTY_BUTTONS, "");
		table.setColumnWidth(PROPERTY_BUTTONS, 120);
		return table;
	}

	private void addButtons(final UserCredentialVODataProvider provider, ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						UserCredentialVO entity = getUserCredentialVO((Integer) itemId, provider);
						presenter.displayButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				final UserCredentialVO entity = getUserCredentialVO((Integer) itemId, provider);
				Button editButton = new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.editButtonClicked(entity);

					}
				};
				if (ADMIN.equals(entity.getUsername())) {
					editButton.setEnabled(presenter.canModifyPassword(entity.getUsername()));
					editButton.setVisible(presenter.canModifyPassword(entity.getUsername()));
				} else {
					editButton.setEnabled(presenter.canAddOrModify());
					editButton.setVisible(presenter.canAddOrModify());
				}
				return editButton;
			}
		});
	}

	public void refreshTable() {
		Table newTable = buildTable(status);
		viewLayout.replaceComponent(table, newTable);
		table = newTable;

		refreshFilter();
	}

	private void refreshFilter() {
		TableStringFilter newTableFilter = new TableStringFilter(table);
		filterAndAddButtonLayout.replaceComponent(tableFilter, newTableFilter);
		tableFilter = newTableFilter;
	}

	private UserCredentialVO getUserCredentialVO(Integer itemId, UserCredentialVODataProvider provider) {
		Integer index = itemId;
		return provider.getUserCredentialVO(index);
	}
}