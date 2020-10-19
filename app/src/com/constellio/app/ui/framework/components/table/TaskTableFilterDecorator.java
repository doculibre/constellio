package com.constellio.app.ui.framework.components.table;

import com.constellio.app.ui.framework.components.table.BaseFilteringTable.State;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.datefield.Resolution;
import org.tepi.filtertable.FilterDecorator;
import org.tepi.filtertable.numberfilter.NumberFilterPopupConfig;

import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class TaskTableFilterDecorator implements FilterDecorator {

	@Override
	public String getEnumFilterDisplayName(Object propertyId, Object value) {
		if ("state".equals(propertyId)) {
			State state = (State) value;
			switch (state) {
				case CREATED:
					return "Order has been created";
				case PROCESSING:
					return "Order is being processed";
				case PROCESSED:
					return "Order has been processed";
				case FINISHED:
					return "Order is delivered";
			}
		}
		// returning null will output default value
		return null;
	}

	@Override
	public Resource getEnumFilterIcon(Object propertyId, Object value) {
		if ("state".equals(propertyId)) {
			State state = (State) value;
			switch (state) {
				case CREATED:
					return new ThemeResource("../runo/icons/16/document.png");
				case PROCESSING:
					return new ThemeResource("../runo/icons/16/reload.png");
				case PROCESSED:
					return new ThemeResource("../runo/icons/16/ok.png");
				case FINISHED:
					return new ThemeResource("../runo/icons/16/globe.png");
			}
		}
		return null;
	}

	@Override
	public String getBooleanFilterDisplayName(Object propertyId, boolean value) {
		if ("validated".equals(propertyId)) {
			return value ? "Validated" : "Not validated";
		}
		// returning null will output default value
		return null;
	}

	@Override
	public Resource getBooleanFilterIcon(Object propertyId, boolean value) {
		if ("validated".equals(propertyId)) {
			return value ? new ThemeResource("../runo/icons/16/ok.png")
						 : new ThemeResource("../runo/icons/16/cancel.png");
		}
		return null;
	}

	@Override
	public Locale getLocale() {
		// will use the application locale
		return null;
	}

	@Override
	public String getFromCaption() {
		return ":" + $("StatisticsView.startDate");
	}

	@Override
	public String getToCaption() {
		return ":" + $("StatisticsView.endDate");
	}

	@Override
	public String getSetCaption() {
		// use default caption
		return null;
	}

	@Override
	public String getClearCaption() {
		return null;
	}

	@Override
	public boolean isTextFilterImmediate(Object propertyId) {
		return false;
	}

	@Override
	public int getTextChangeTimeout(Object propertyId) {
		return 0;
	}

	@Override
	public Resolution getDateFieldResolution(Object propertyId) {
		return null;
	}

	@Override
	public String getDateFormatPattern(Object propertyId) {
		return null;
	}

	@Override
	public String getAllItemsVisibleString() {
		return null;
	}

	@Override
	public NumberFilterPopupConfig getNumberFilterPopupConfig() {
		return null;
	}

	@Override
	public boolean usePopupForNumericProperty(Object propertyId) {
		return false;
	}

}
