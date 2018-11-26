package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.util.DateFormatUtils;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.datefield.Resolution;
import org.tepi.filtertable.FilterDecorator;
import org.tepi.filtertable.numberfilter.NumberFilterPopupConfig;

import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class DemoFilterDecorator implements FilterDecorator {

	@Override
	public String getEnumFilterDisplayName(Object propertyId, Object value) {
		// returning null will output default value
		return null;
	}

	@Override
	public Resource getEnumFilterIcon(Object propertyId, Object value) {
		return null;
	}

	@Override
	public String getBooleanFilterDisplayName(Object propertyId, boolean value) {
		if (((MetadataVO) propertyId).codeMatches(Task.READ_BY_USER)) {
			return value ? "Oui" : "Non";
		}
		// returning null will output default value
		return null;
	}

	@Override
	public Resource getBooleanFilterIcon(Object propertyId, boolean value) {
		return null;
	}

	@Override
	public Locale getLocale() {
		// will use the application locale
		return null;
	}

	@Override
	public String getFromCaption() {
		return ":"+$("StatisticsView.startDate");
	}

	@Override
	public String getToCaption() {
		return ":"+$("StatisticsView.endDate");
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
		return true;
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
		return DateFormatUtils.getDateFormat();
	}

	@Override
	public String getAllItemsVisibleString() {
		return null;
	}

	@Override
	public NumberFilterPopupConfig getNumberFilterPopupConfig() {
		NumberFilterPopupConfig config = new NumberFilterPopupConfig();
		config.setLtPrompt("Plus petit que");
		config.setGtPrompt("Plus grand que");
		config.setEqPrompt("Égal à");
		config.setResetCaption("Annuler");
		config.setOkCaption("Appliquer");
		return config;
	}

	@Override
	public boolean usePopupForNumericProperty(Object propertyId) {
		return true;
	}

}