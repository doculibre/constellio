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
package com.constellio.app.ui.pages.management.collections;

import static com.constellio.app.ui.i18n.i18n.$;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.CollectionVOLazyContainer;
import com.constellio.app.ui.framework.data.CollectionVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.services.collections.CollectionsListManagerListener;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class CollectionManagementViewImpl extends BaseViewImpl implements CollectionManagementView,
																		  CollectionsListManagerListener {
	public static final String TABLE_STYLE_CODE = "seleniumTableStyle";
	public static final String EDIT_BUTTON_STYLE = "seleniumEditButtonStyle";
	public static final String DELETE_BUTTON_STYLE = "seleniumDeleteButtonStyle";
	public static final String ADD_BUTTON_STYLE = "seleniumAddButtonStyle";
	private static final String PROPERTY_BUTTONS = "buttons";

	private CollectionManagementPresenter presenter;
	private Table table;
	private VerticalLayout layout;

	public CollectionManagementViewImpl() {
		super();
		this.presenter = new CollectionManagementPresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		this.layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		table = buildTable();
		layout.addComponent(table);
		Component addButton = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addButtonClick();
			}
		};
		addButton.addStyleName(ADD_BUTTON_STYLE);
		layout.addComponent(addButton);
		layout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);

		return layout;
	}

	private Table buildTable() {
		final CollectionVODataProvider dataProvider = presenter.getDataProvider();
		Container container = new CollectionVOLazyContainer(dataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, PROPERTY_BUTTONS);
		addButtons(dataProvider, buttonsContainer);
		container = buttonsContainer;

		RecordVOTable table = new RecordVOTable($(""), container);
		table.setColumnHeader(CollectionVOLazyContainer.CODE, $("code"));
		table.setColumnHeader(CollectionVOLazyContainer.NAME, $("name"));
		table.setPageLength(table.getItemIds().size());
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.setColumnHeader(PROPERTY_BUTTONS, "");
		table.setColumnWidth(PROPERTY_BUTTONS, 120);
		table.addStyleName(TABLE_STYLE_CODE);

		return table;
	}

	private void addButtons(final CollectionVODataProvider provider, ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				DisplayButton button = new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.editButtonClicked(provider, (Integer) itemId);
					}
				};
				button.addStyleName(EDIT_BUTTON_STYLE);
				return button;
			}
		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				DeleteButton button = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteButtonClicked(provider, (Integer) itemId);
						refreshTable();
					}

					@Override
					public boolean isEnabled() {
						return super.isEnabled() && presenter.isDeletePossible(provider, (Integer) itemId);
					}
				};
				button.addStyleName(DELETE_BUTTON_STYLE);
				if (!button.isEnabled()) {
					button.setVisible(false);
				}
				return button;
			}
		});
	}

	@Override
	protected String getTitle() {
		return $("CollectionManagementView.viewTitle");
	}

	public void refreshTable() {
		Table newTable = buildTable();
		layout.replaceComponent(table, newTable);
		table = newTable;
	}

	@Override
	public void onCollectionCreated(String collection) {
		refreshTable();
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClick();
			}
		};
	}
}
