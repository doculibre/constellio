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

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class DisplaySchemaRecordViewImpl extends BaseViewImpl implements DisplaySchemaRecordView {

	DisplaySchemaRecordPresenter presenter;

	RecordVO recordVO;

	RecordDisplay recordDisplay;

	public DisplaySchemaRecordViewImpl() {
		this.presenter = new DisplaySchemaRecordPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		String id = event.getParameters();
		recordVO = presenter.getRecordVO(id);
		presenter.forSchema(recordVO.getSchema().getCode());
	}

	@Override
	protected String getTitle() {
		return $("DisplaySchemaRecordView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		recordDisplay = new RecordDisplay(recordVO);
		return recordDisplay;
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

}
