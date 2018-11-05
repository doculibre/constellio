package com.constellio.app.ui.framework.components.table;

import com.constellio.app.ui.framework.components.table.BaseFilteringTable.State;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import org.tepi.filtertable.FilterDecorator;
import org.tepi.filtertable.FilterGenerator;
import org.tepi.filtertable.FilterTable;
import org.tepi.filtertable.numberfilter.NumberFilterPopupConfig;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import static com.constellio.app.ui.i18n.i18n.$;

public class BaseFilteringTable extends CustomComponent {

	/**
	 * Example enum for enum filtering feature
	 */
	public enum State {
		CREATED, PROCESSING, PROCESSED, FINISHED;
	}

	private FilterTable filterTable;

	public BaseFilteringTable() {
		/* Create FilterTable */
		filterTable = buildFilterTable();

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		mainLayout.setMargin(true);
		mainLayout.addComponent(filterTable);
		mainLayout.setExpandRatio(filterTable, 1);
		mainLayout.addComponent(buildButtons());
		setCompositionRoot(mainLayout);
	}

	private FilterTable buildFilterTable() {
		FilterTable filterTable = new FilterTable();
		filterTable.setSizeFull();
		filterTable.setFilterDecorator(new DemoFilterDecorator());
		filterTable.setFilterGenerator(new DemoFilterGenerator());
		filterTable.setContainerDataSource(buildContainer());
		filterTable.setFilterBarVisible(true);
		return filterTable;
	}

	private Component buildButtons() {
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSizeUndefined();
		buttonLayout.setSpacing(true);
		Button showFilters = new Button("Show filter bar");
		showFilters.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				filterTable.setFilterBarVisible(true);
			}
		});
		buttonLayout.addComponent(showFilters);
		Button hideFilters = new Button("Hide filter bar");
		hideFilters.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				filterTable.setFilterBarVisible(false);
			}
		});
		buttonLayout.addComponent(hideFilters);
		return buttonLayout;
	}

	private Container buildContainer() {
		IndexedContainer cont = new IndexedContainer();
		Calendar c = Calendar.getInstance();

		cont.addContainerProperty("name", String.class, null);
		cont.addContainerProperty("id", Integer.class, null);
		cont.addContainerProperty("state", State.class, null);
		cont.addContainerProperty("date", Date.class, null);
		cont.addContainerProperty("validated", Boolean.class, null);

		Random random = new Random();
		for (int i = 0; i < 10000; i++) {
			cont.addItem(i);
			/* Set name and id properties */
			cont.getContainerProperty(i, "name").setValue("Order " + i);
			cont.getContainerProperty(i, "id").setValue(i);
			/* Set state property */
			int rndInt = random.nextInt(4);
			State stateToSet = State.CREATED;
			if (rndInt == 0) {
				stateToSet = State.PROCESSING;
			} else if (rndInt == 1) {
				stateToSet = State.PROCESSED;
			} else if (rndInt == 2) {
				stateToSet = State.FINISHED;
			}
			cont.getContainerProperty(i, "state").setValue(stateToSet);
			/* Set date property */
			cont.getContainerProperty(i, "date").setValue(c.getTime());
			c.add(Calendar.DAY_OF_MONTH, 1);
			/* Set validated property */
			cont.getContainerProperty(i, "validated").setValue(
					random.nextBoolean());
		}
		return cont;
	}
}

class DemoFilterGenerator implements FilterGenerator {

	@Override
	public Filter generateFilter(Object propertyId, Object value) {
		if ("id".equals(propertyId)) {
			/* Create an 'equals' filter for the ID field */
			if (value != null && value instanceof String) {
				try {
					return new Compare.Equal(propertyId,
							Integer.parseInt((String) value));
				} catch (NumberFormatException ignored) {
					// If no integer was entered, just generate default filter
				}
			}
		}
		// For other properties, use the default filter
		return null;
	}

	@Override
	public Filter generateFilter(Object propertyId, Field<?> originatingField) {
		Object value = originatingField.getValue();
		return generateFilter(propertyId, value);
	}

	@Override
	public AbstractField<?> getCustomFilterComponent(Object propertyId) {
		return null;
	}

	@Override
	public void filterRemoved(Object propertyId) {
	}

	@Override
	public void filterAdded(Object propertyId, Class<? extends Filter> filterType, Object value) {
	}

	@Override
	public Filter filterGeneratorFailed(Exception reason, Object propertyId, Object value) {
		return null;
	}

}

class DemoFilterDecorator implements FilterDecorator {

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
