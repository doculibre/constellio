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
package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.events.EventCategory;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

public class EventByIdSearchPanel extends Panel {
	public static final String LOOKUP_STYLE_CODE = "seleniumLookupFieldCode";
	private EventCategory eventCategory;
	private LookupRecordField lookupField;

	public EventByIdSearchPanel(EventCategory eventCategory, String id) {
		this.eventCategory = eventCategory;
		HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.setSizeFull();

		String schemaTypeCode = getLookupSchemaTypeCode();

		lookupField = new LookupRecordField(schemaTypeCode);
		lookupField.setValue(id);
		lookupField.setCaption(getByIdFieldCaption());
		lookupField.addStyleName(LOOKUP_STYLE_CODE);
		hLayout.addComponent(lookupField);
		hLayout.setComponentAlignment(lookupField, Alignment.MIDDLE_CENTER);

		setContent(hLayout);
		addStyleName(ValoTheme.PANEL_BORDERLESS);
	}

	public String getIdValue(){
		if(lookupField == null){
			return "";
		}
		return lookupField.getValue();
	}

	protected String getByIdFieldCaption(){
		switch (eventCategory){
		case EVENTS_BY_ADMINISTRATIVE_UNIT : return $("ListEventsView.byFilingSpace.textFieldCaption");
		case EVENTS_BY_FOLDER: return $("ListEventsView.byFolder.textFieldCaption");
		case EVENTS_BY_USER : return $("ListEventsView.byUser.textFieldCaption");
		default: return "";
		}
	}

	protected String getLookupSchemaTypeCode() {
		switch(eventCategory){
		case EVENTS_BY_USER: return User.SCHEMA_TYPE;
		case EVENTS_BY_FOLDER: return Folder.SCHEMA_TYPE;
		case EVENTS_BY_ADMINISTRATIVE_UNIT: return AdministrativeUnit.SCHEMA_TYPE;
		default : return null;
		}
	}
}
