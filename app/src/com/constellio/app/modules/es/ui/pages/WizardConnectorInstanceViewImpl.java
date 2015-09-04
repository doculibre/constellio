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
package com.constellio.app.modules.es.ui.pages;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

public class WizardConnectorInstanceViewImpl extends BaseViewImpl implements WizardConnectorInstanceView {

	private VerticalLayout mainLayout;
	private TabSheet tabSheet;
	private RecordVO recordVO;
	private RecordVOTable connectorTypesListComponent;
	private Component addConnectorInstanceComponent;

	private WizardConnectorInstancePresenter presenter;

	public WizardConnectorInstanceViewImpl() {

		configureLayout();

		presenter = new WizardConnectorInstancePresenter(this);
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

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		return mainLayout;
	}

	@Override
	public void setRecordVO(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	@Override
	public void setConnectorTypeListTable() {
		RecordVOLazyContainer recordVOLazyContainer = new RecordVOLazyContainer(presenter.getDataProviderSelectConnectorType());
		ButtonsContainer buttonsContainer = new ButtonsContainer(recordVOLazyContainer, "buttons");
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(Object itemId) {
				Integer index = (Integer) itemId;
				final RecordVO entity = presenter.getDataProviderSelectConnectorType().getRecordVO(index);
				Button button = new Button(entity.getTitle());
				button.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						presenter.connectorTypeButtonClicked(entity.getId());
					}
				});
				return button;
			}
		});
		RecordVOTable newRecordVOTable = new RecordVOTable($("WizardConnectorInstanceView.tableTitle"), buttonsContainer);
		newRecordVOTable.setWidth("100%");
		newRecordVOTable.setColumnHeader("buttons", "");
		newRecordVOTable.setPageLength(newRecordVOTable.getItemIds().size());
		tabSheet.replaceComponent(connectorTypesListComponent, newRecordVOTable);
		connectorTypesListComponent = newRecordVOTable;
	}

	@Override
	public void setAddConnectorInstanceForm() {
		RecordForm recordForm = newForm();
		tabSheet.replaceComponent(addConnectorInstanceComponent, recordForm);
		addConnectorInstanceComponent = recordForm;
	}

	private RecordForm newForm() {
		RecordForm recordForm = new RecordForm(recordVO, new ConnectorInstanceFieldFactory()) {

			@Override
			protected void saveButtonClick(RecordVO viewObject)
					throws ValidationException {
				presenter.saveButtonClicked(viewObject);
			}

			@Override
			protected void cancelButtonClick(RecordVO viewObject) {
				presenter.cancelButtonClicked();
			}
		};
		return recordForm;
	}

	@Override
	public void selectConnectorTypeTab() {
		tabSheet.setSelectedTab(connectorTypesListComponent);
	}

	@Override
	public void selectAddConnectorInstanceTab() {
		tabSheet.setSelectedTab(addConnectorInstanceComponent);
	}

	@Override
	protected String getTitle() {
		return presenter.getTitle();
	}

	private void configureLayout() {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		connectorTypesListComponent = new RecordVOTable();
		addConnectorInstanceComponent = new CustomComponent();

		tabSheet = new TabSheet();
		tabSheet.addStyleName("wizard-connector-instance");
		tabSheet.addTab(connectorTypesListComponent, $("WizardConnectorInstanceView.tabs.connectorTypesList"));
		tabSheet.addTab(addConnectorInstanceComponent, $("WizardConnectorInstanceView.tabs.addConnectorInstance"));

		mainLayout.addComponents(tabSheet);
	}
}
