package com.constellio.app.ui.framework.components.table;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.Map;

import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.server.ClientConnector;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamVariable;
import com.vaadin.server.VariableOwner;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

public class BasePagedTable<T extends Container> extends PagedTable {

	public static final int DEFAULT_PAGE_LENGTH = 10;
	
	protected T container;
	protected ComboBox itemsPerPageField;

	public BasePagedTable(T container) {
		this.container = container;
		itemsPerPageField = new ComboBox();
	}

	@Override
	public HorizontalLayout createControls() {
		HorizontalLayout pageSize;

		Label itemsPerPageLabel = new Label($("SearchResultTable.itemsPerPage"));
		itemsPerPageField.addItem(DEFAULT_PAGE_LENGTH);
		if (container.size() >= 10) {
			itemsPerPageField.addItem(10);
		}
		if (container.size() > 10) {
			itemsPerPageField.addItem(25);
		}
		if (container.size() > 25) {
			itemsPerPageField.addItem(50);
		}
		if (container.size() > 50) {
			itemsPerPageField.addItem(100);
		}
		itemsPerPageField.setNullSelectionAllowed(false);
		itemsPerPageField.setWidth("85px");

		itemsPerPageField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				setPageLength((int) itemsPerPageField.getValue());
			}
		});
		itemsPerPageField.setEnabled(itemsPerPageField.size() > 1);

		pageSize = new HorizontalLayout(itemsPerPageLabel, itemsPerPageField);
		pageSize.setComponentAlignment(itemsPerPageLabel, Alignment.MIDDLE_LEFT);
		pageSize.setComponentAlignment(itemsPerPageField, Alignment.MIDDLE_LEFT);
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

		addListener(new PageChangeListener() {
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

	public ComboBox getItemsPerPageField() {
		return itemsPerPageField;
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(new PaintTargetAdapter(target));
	}

	public void setItemsPerPageValue(int value) {
		itemsPerPageField.setValue(value);
	}

	@Override
	public void setPageLength(int pageLength) {
		super.setPageLength(pageLength);
	}

	@SuppressWarnings("deprecation")
	private static class PaintTargetAdapter implements Serializable, PaintTarget {
		
		private PaintTarget adaptee;
		
		private PaintTargetAdapter(PaintTarget adaptee) {
			this.adaptee = adaptee;
		}

		public void addSection(String sectionTagName, String sectionData) throws PaintException {
			adaptee.addSection(sectionTagName, sectionData);
		}

		public PaintStatus startPaintable(Component paintable, String tag) throws PaintException {
			return adaptee.startPaintable(paintable, tag);
		}

		public void endPaintable(Component paintable) throws PaintException {
			adaptee.endPaintable(paintable);
		}

		public void startTag(String tagName) throws PaintException {
			adaptee.startTag(tagName);
		}

		public void endTag(String tagName) throws PaintException {
			adaptee.endTag(tagName);
		}
		
		private boolean isIgnoredAttribute(String name) {
			return "pagelength".equals(name);
		}

		public void addAttribute(String name, boolean value) throws PaintException {
			if (!isIgnoredAttribute(name)) {
				adaptee.addAttribute(name, value);
			}
		}

		public void addAttribute(String name, int value) throws PaintException {
			if (!isIgnoredAttribute(name)) {
				adaptee.addAttribute(name, value);
			}
		}

		public void addAttribute(String name, Resource value) throws PaintException {
			if (!isIgnoredAttribute(name)) {
				adaptee.addAttribute(name, value);
			}
		}

		public void addVariable(VariableOwner owner, String name, StreamVariable value) throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addAttribute(String name, long value) throws PaintException {
			adaptee.addAttribute(name, value);
		}

		public void addAttribute(String name, float value) throws PaintException {
			adaptee.addAttribute(name, value);
		}

		public void addAttribute(String name, double value) throws PaintException {
			adaptee.addAttribute(name, value);
		}

		public void addAttribute(String name, String value) throws PaintException {
			adaptee.addAttribute(name, value);
		}

		public void addAttribute(String name, Map<?, ?> value) throws PaintException {
			adaptee.addAttribute(name, value);
		}

		public void addAttribute(String name, Component value) throws PaintException {
			adaptee.addAttribute(name, value);
		}

		public void addVariable(VariableOwner owner, String name, String value) throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addVariable(VariableOwner owner, String name, int value) throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addVariable(VariableOwner owner, String name, long value) throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addVariable(VariableOwner owner, String name, float value) throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addVariable(VariableOwner owner, String name, double value) throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addVariable(VariableOwner owner, String name, boolean value) throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addVariable(VariableOwner owner, String name, String[] value) throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addVariable(VariableOwner owner, String name, Component value) throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addUploadStreamVariable(VariableOwner owner, String name) throws PaintException {
			adaptee.addUploadStreamVariable(owner, name);
		}

		public void addXMLSection(String sectionTagName, String sectionData, String namespace) throws PaintException {
			adaptee.addXMLSection(sectionTagName, sectionData, namespace);
		}

		public void addUIDL(String uidl) throws PaintException {
			adaptee.addUIDL(uidl);
		}

		public void addText(String text) throws PaintException {
			adaptee.addText(text);
		}

		public void addCharacterData(String text) throws PaintException {
			adaptee.addCharacterData(text);
		}

		public void addAttribute(String string, Object[] keys) {
			adaptee.addAttribute(string, keys);
		}

		public String getTag(ClientConnector paintable) {
			return adaptee.getTag(paintable);
		}

		public boolean isFullRepaint() {
			return adaptee.isFullRepaint();
		}
		
	}	
}
