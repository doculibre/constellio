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

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class EditConnectorInstanceViewImpl extends BaseViewImpl implements EditConnectorInstanceView {

	private VerticalLayout mainLayout;
	private RecordVO recordVO;
	private Component addConnectorInstanceComponent;

	private EditConnectorInstancePresenter presenter;

	public EditConnectorInstanceViewImpl() {

		presenter = new EditConnectorInstancePresenter(this);
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
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		addConnectorInstanceComponent = newForm();

		mainLayout.addComponents(addConnectorInstanceComponent);
		return mainLayout;
	}

	@Override
	public void setRecordVO(RecordVO recordVO) {
		this.recordVO = recordVO;
	}

	@Override
	protected String getTitle() {
		return presenter.getTitle();
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

	//
	@Override
	public void setConnectorTypeListTable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAddConnectorInstanceForm() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void selectConnectorTypeTab() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void selectAddConnectorInstanceTab() {
		throw new UnsupportedOperationException();
	}
}
