package com.constellio.app.ui.framework.components.table;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.framework.components.SearchResultDetailedTable;
import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

public class BasePagedTable<T extends Container> extends PagedTable {
	protected T container;
	protected ComboBox itemsPerPage;

	public BasePagedTable(T container) {
		this.container = container;
		itemsPerPage = new ComboBox();
	}

	public HorizontalLayout createControls() {
		HorizontalLayout pageSize;

		Label itemsPerPageLabel = new Label($("SearchResultTable.itemsPerPage"));
		itemsPerPage.addItem(SearchResultDetailedTable.DEFAULT_PAGE_LENGTH);
		if (container.size() >= 10) {
			itemsPerPage.addItem(10);
		}
		if (container.size() >= 25) {
			itemsPerPage.addItem(25);
		}
		if (container.size() >= 50) {
			itemsPerPage.addItem(50);
		}
		if (container.size() >= 100) {
			itemsPerPage.addItem(100);
		}
		itemsPerPage.setNullSelectionAllowed(false);
		itemsPerPage.setWidth("85px");

		itemsPerPage.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				setPageLength((int) itemsPerPage.getValue());
			}
		});
		itemsPerPage.setEnabled(itemsPerPage.size() > 1);

		pageSize = new HorizontalLayout(itemsPerPageLabel, itemsPerPage);
		pageSize.setComponentAlignment(itemsPerPageLabel, Alignment.MIDDLE_LEFT);
		pageSize.setComponentAlignment(itemsPerPage, Alignment.MIDDLE_LEFT);
		pageSize.setSpacing(true);

		Label page = new Label($("SearchResultTable.page"));
		final TextField currentPage = new TextField();
		currentPage.setConverter(Integer.class);
		currentPage.setConvertedValue(getCurrentPage());
		currentPage.setWidth("45px");
		currentPage.addValidator(
				new IntegerRangeValidator("Wrong page number", 1, getTotalAmountOfPages()));
		currentPage.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (currentPage.isValid() && currentPage.getValue() != null) {
					setCurrentPage((int) currentPage.getConvertedValue());
				}
			}
		});
		currentPage.setEnabled(getTotalAmountOfPages() > 1);

		Label separator = new Label($("SearchResultTable.of"));
		final Label totalPages = new Label(String.valueOf(getTotalAmountOfPages()));

		final Button first = new Button("\uF100", new ClickListener() {
			public void buttonClick(ClickEvent event) {
				setCurrentPage(0);
			}
		});
		first.setStyleName(ValoTheme.BUTTON_LINK);
		first.setEnabled(getCurrentPage() > 1);

		final Button previous = new Button("\uF104", new ClickListener() {
			public void buttonClick(ClickEvent event) {
				previousPage();
			}
		});
		previous.setStyleName(ValoTheme.BUTTON_LINK);
		previous.setEnabled(getCurrentPage() > 1);

		final Button next = new Button("\uF105", new ClickListener() {
			public void buttonClick(ClickEvent event) {
				nextPage();
			}
		});
		next.setStyleName(ValoTheme.BUTTON_LINK);
		next.setEnabled(getCurrentPage() < getTotalAmountOfPages());

		final Button last = new Button("\uF101", new ClickListener() {
			public void buttonClick(ClickEvent event) {
				setCurrentPage(getTotalAmountOfPages());
			}
		});
		last.setStyleName(ValoTheme.BUTTON_LINK);
		last.setEnabled(getCurrentPage() < getTotalAmountOfPages());

		HorizontalLayout pageManagement = new HorizontalLayout(
				first, previous, page, currentPage, separator, totalPages, next, last);
		pageManagement.setComponentAlignment(first, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(previous, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(page, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(currentPage, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(separator, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(totalPages, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(next, Alignment.MIDDLE_LEFT);
		pageManagement.setComponentAlignment(last, Alignment.MIDDLE_LEFT);
		pageManagement.setSpacing(true);

		HorizontalLayout controlBar = new HorizontalLayout(pageSize, pageManagement);
		controlBar.setComponentAlignment(pageManagement, Alignment.MIDDLE_CENTER);
		controlBar.setExpandRatio(pageSize, 1);
		controlBar.setWidth("100%");

		addListener(new SearchResultDetailedTable.PageChangeListener() {
			public void pageChanged(PagedTableChangeEvent event) {
				first.setEnabled(getCurrentPage() > 1);
				previous.setEnabled(getCurrentPage() > 1);
				next.setEnabled(getCurrentPage() < getTotalAmountOfPages());
				last.setEnabled(getCurrentPage() < getTotalAmountOfPages());
				currentPage.setValue(String.valueOf(getCurrentPage()));
				currentPage.setEnabled(getTotalAmountOfPages() > 1);
				totalPages.setValue(String.valueOf(getTotalAmountOfPages()));
			}
		});

		return controlBar;
	}

	public void setItemsPerPageValue(int value) {
		itemsPerPage.setValue(value);
	}
}
