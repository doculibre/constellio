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
package com.constellio.app.modules.rm.ui.pages.administrativeUnit;

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
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ListAdministrativeUnitsViewImpl extends BaseViewImpl implements ListAdministrativeUnitsView {

	// FIXME Hard-coded values
	private static final String PREFIX = "administrativeUnit_default_";
	private static final String PROPERTY_ID = PREFIX + "id";
	//	private static final String PROPERTY_CODE = PREFIX + AdministrativeUnit.CODE;
	//	private static final String PROPERTY_TITLE = PREFIX + "title";
	private static final String PROPERTY_BUTTONS = "buttons";

	ListAdministrativeUnitsPresenter presenter;

	VerticalLayout viewLayout;
	Table table;
	Button addButton;

	public ListAdministrativeUnitsViewImpl() {
		presenter = new ListAdministrativeUnitsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListAdministrativeUnitsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();

		addButton = new AddButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addLinkClicked();
			}
		};

		table = buildTable();

		viewLayout.addComponents(addButton, table);
		viewLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		viewLayout.setExpandRatio(table, 1);

		return viewLayout;
	}

	protected Table buildTable() {
		final RecordVODataProvider dataProvider = presenter.getDataProvider();
		Container recordsContainer = new RecordVOLazyContainer(dataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(recordsContainer, PROPERTY_BUTTONS);

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
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						Integer index = (Integer) itemId;
						RecordVO entity = dataProvider.getRecordVO(index);
						presenter.deleteButtonClicked(entity);
					}
				};
			}
		});
		recordsContainer = buttonsContainer;

		Table table = new RecordVOTable($("ListAdministrativeUnitsView.tableTitle", recordsContainer.size()), recordsContainer);
		table.setSizeFull();

		//		table.setColumnHeader(PROPERTY_ID, $("ListAdministrativeUnitsView.id"));
		//		table.setColumnHeader(PROPERTY_CODE, $("ListAdministrativeUnitsView.code"));
		//		table.setColumnHeader(PROPERTY_TITLE, $("ListAdministrativeUnitsView.title"));
		table.setColumnHeader(PROPERTY_BUTTONS, "");

		table.setColumnWidth(PROPERTY_ID, 120);
		table.setColumnWidth(PROPERTY_BUTTONS, 120);
		table.setPageLength(table.getItemIds().size());

		//		table.addItemClickListener(new ItemClickListener() {
		//			@Override
		//			public void itemClick(ItemClickEvent event) {
		//				if (MouseButton.LEFT.equals(event.getButton())) {
		//					Integer index = (Integer) event.getItemId();
		//					RecordVO entity = dataProvider.getRecordVO(index);
		//					presenter.displayButtonClicked(entity);
		//				}
		//			}
		//		});
		return table;
	}

	public void refreshTable() {
		Table newTable = buildTable();
		viewLayout.replaceComponent(table, newTable);
		table = newTable;
	}

}
