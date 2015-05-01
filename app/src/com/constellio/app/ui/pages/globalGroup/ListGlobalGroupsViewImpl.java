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
package com.constellio.app.ui.pages.globalGroup;

import static com.constellio.app.ui.i18n.i18n.$;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.TableStringFilter;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.GlobalGroupVOLazyContainer;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.vaadin.data.Container;
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

public class ListGlobalGroupsViewImpl extends BaseViewImpl implements ListGlobalGroupsView {

	private ListGlobalGroupsPresenter presenter;
	private static final String PROPERTY_BUTTONS = "buttons";
	private VerticalLayout viewLayout;
	private Table table;
	private HorizontalLayout filterAndAddButtonLayout;
	private TableStringFilter tableFilter;
	private Button addButton;
	private GlobalGroupStatus status;

	public ListGlobalGroupsViewImpl() {
		this.presenter = new ListGlobalGroupsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListGlobalGroupsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {

		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.setSpacing(true);

		table = buildTable(GlobalGroupStatus.ACTIVE);

		filterAndAddButtonLayout = new HorizontalLayout();
		filterAndAddButtonLayout.setWidth("100%");

		addButton = new AddButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};
		addButton.setEnabled(presenter.canAddOrModify());

		tableFilter = new TableStringFilter(table);

		OptionGroup statusFilter = new OptionGroup();
		statusFilter.addStyleName("horizontal");
		statusFilter.addStyleName("status");
		for (GlobalGroupStatus status : GlobalGroupStatus.values()) {
			statusFilter.addItem(status);
			statusFilter.setItemCaption(status, $("GlobalGroupView.status." + status));
			if (this.status == null) {
				this.status = GlobalGroupStatus.ACTIVE;
				statusFilter.setValue(GlobalGroupStatus.ACTIVE);
			} else if (status == this.status) {
				statusFilter.setValue(status);
			}
		}
		statusFilter.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				status = (GlobalGroupStatus) event.getProperty().getValue();
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

	private Table buildTable(GlobalGroupStatus status) {
		final GlobalGroupVODataProvider dataProvider = presenter.getDataProvider();
		dataProvider.setGlobalGroupVOs(dataProvider.listBaseGlobalGroupsVOsWithStatus(status));
		Container container = new GlobalGroupVOLazyContainer(dataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, PROPERTY_BUTTONS);
		addButtons(dataProvider, buttonsContainer);
		container = buttonsContainer;

		Table table = new Table($("ListGlobalGroupsView.viewTitle"), container);
		table.setPageLength(table.getItemIds().size());
		table.setWidth("100%");
		table.setSelectable(true);
		table.setColumnHeader("code", $("ListGlobalGroupsView.codeColumn"));
		table.setColumnHeader("name", $("ListGlobalGroupsView.nameColumn"));
		table.setColumnHeader(PROPERTY_BUTTONS, "");
		table.setColumnWidth(PROPERTY_BUTTONS, 120);
		return table;
	}

	private void addButtons(final GlobalGroupVODataProvider provider, ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						GlobalGroupVO entity = getGlobalGroupVO((Integer) itemId, provider);
						presenter.displayButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				final GlobalGroupVO entity = getGlobalGroupVO((Integer) itemId, provider);
				Button editButton = new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.editButtonClicked(entity);

					}
				};
				editButton.setEnabled(presenter.canAddOrModify());
				editButton.setVisible(presenter.canAddOrModify());
				return editButton;
			}
		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				final GlobalGroupVO entity = getGlobalGroupVO((Integer) itemId, provider);
				Button deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteButtonClicked(entity);
					}
				};
				deleteButton.setVisible(entity.getStatus() == GlobalGroupStatus.ACTIVE && presenter.canAddOrModify());
				deleteButton.setEnabled(entity.getStatus() == GlobalGroupStatus.ACTIVE && presenter.canAddOrModify());
				return deleteButton;
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

	private GlobalGroupVO getGlobalGroupVO(Integer itemId, GlobalGroupVODataProvider provider) {
		Integer index = itemId;
		return provider.getGlobalGroupVO(index);
	}

}