package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.events.EventCategory;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

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
