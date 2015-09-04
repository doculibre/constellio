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
package com.constellio.app.ui.pages.management.schemaRecords;

import static com.constellio.app.ui.i18n.i18n.$;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class ListSchemaRecordsViewImpl extends BaseViewImpl implements ListSchemaRecordsView {

	ListSchemaRecordsPresenter presenter;
	VerticalLayout viewLayout;
	Table table;

	public ListSchemaRecordsViewImpl() {
		this.presenter = new ListSchemaRecordsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListSchemaRecordsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		final String schemaName = event.getParameters();
		presenter.forSchema(schemaName);

		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		Button addButton = new AddButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addLinkClicked();
			}
		};
		table = buildRecordsTables();
		viewLayout.addComponents(addButton, table);
		viewLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		return viewLayout;
	}

	private Table buildRecordsTables() {
		final RecordVODataProvider dataProvider = presenter.getDataProvider();
		Container recordsContainer = new RecordVOLazyContainer(dataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(recordsContainer, "buttons");
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = dataProvider.getRecordVO(index);
						presenter.displayButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = dataProvider.getRecordVO(index);
						presenter.editButtonClicked(entity);
					}
				};
			}
		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				DeleteButton deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						Integer index = (Integer) itemId;
						RecordVO entity = dataProvider.getRecordVO(index);
						presenter.deleteButtonClicked(entity);
					}
				};
				return deleteButton;
			}
		});
		recordsContainer = buttonsContainer;

		Table table = new RecordVOTable(
				$("ListSchemaRecordsView.tableTitle", dataProvider.size(), dataProvider.getSchema().getLabel(
						getSessionContext().getCurrentLocale())),
				recordsContainer);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
		table.setColumnWidth("buttons", 120);
		table.setPageLength(table.getItemIds().size());
		table.setCaption(table.getPageLength() + " " + table.getCaption());

		return table;
	}

	public void refreshTable() {
		Table newTable = buildRecordsTables();
		viewLayout.replaceComponent(table, newTable);
		table = newTable;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				navigateTo().adminModule();
			}
		};
	}
}
