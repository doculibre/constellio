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

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Record;

@SuppressWarnings("serial")
public class AddEditAdministrativeUnitPresenter extends SingleSchemaBasePresenter<AddEditAdministrativeUnitView> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AddEditAdministrativeUnitPresenter.class);

	// FIXME Hard-coded values
	private static final String PREFIX = "administrativeUnit_default_";
	//	private static final String PROPERTY_ID = PREFIX + "id";
	private static final String PROPERTY_CODE = PREFIX + AdministrativeUnit.CODE;
	//	private static final String PROPERTY_TITLE = PREFIX + "title";
	
	private String id;

	public AddEditAdministrativeUnitPresenter(AddEditAdministrativeUnitView view) {
		super(view, AdministrativeUnit.DEFAULT_SCHEMA);
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
	}

	public boolean isAddView() {
		return StringUtils.isBlank(id);
	}

	public RecordVO forId(String id) {
		this.id = id;
		
		Record record;
		if (StringUtils.isNotBlank(id)) {
			record = recordServices().getDocumentById(id);
		} else {
			record = newRecord();
		}
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		return voBuilder.build(record, VIEW_MODE.FORM);
	}

	public void saveButtonClicked(RecordVO recordVO) {
		String code = "" + recordVO.get(PROPERTY_CODE);
		if (code.contains("z")) {
			view.showErrorMessage("No z!");
		} else {
			try {
				Record record = toRecord(recordVO);
				addOrUpdate(record);
				view.navigateTo().listAdministrativeUnits();
			} catch (Exception e) {
				view.showErrorMessage(MessageUtils.toMessage(e));
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	public void cancelButtonClicked(RecordVO recordVO) {
		view.navigateTo().listAdministrativeUnits();
	}

}
