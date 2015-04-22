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
package com.constellio.app.ui.framework.components.fields.record;

import java.util.List;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;

public class RecordOptionGroup extends ListOptionGroup implements RecordOptionField {
	
	private RecordIdToCaptionConverter captionConverter = new RecordIdToCaptionConverter();
	
	private RecordOptionFieldPresenter presenter;
	
	public RecordOptionGroup(String schemaCode) {
		super();
		this.presenter = new RecordOptionFieldPresenter(this);
		this.presenter.forSchemaCode(schemaCode);
	}

	@Override
	public void setDataProvider(RecordVODataProvider dataProvider) {
		int size = dataProvider.size();
		List<RecordVO> records = dataProvider.listRecordVOs(0, size);
		for (RecordVO recordVO : records) {
			String recordId = recordVO.getId();
			String itemCaption = captionConverter.convertToPresentation(recordId, String.class, getLocale());
			addItem(recordId);
			setItemCaption(recordId, itemCaption);
		}
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

}
