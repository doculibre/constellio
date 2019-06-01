package com.constellio.app.ui.framework.components.table;

import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.table.TablePropertyCache.CellKey;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;
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

import java.io.Serializable;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public class BasePagedTable<T extends Container> extends PagedTable {

	public static final int DEFAULT_PAGE_LENGTH = 10;

	protected T container;
	protected ComboBox itemsPerPageField;
	private String tableId;
	private TableColumnsManager columnsManager;
	protected TablePropertyCache cellProperties = new TablePropertyCache();

	public BasePagedTable(String tableId, T container) {
		this.tableId = tableId;
		this.container = container;
		init();
	}

	private void init() {
		setContainerDataSource(container);
		itemsPerPageField = new BaseComboBox();
		addAttachListener(new AttachListener() {
			@Override
			public void attach(AttachEvent event) {
				String tableId = getTableId();
				if (tableId != null && columnsManager == null) {
					columnsManager = newColumnsManager();
					columnsManager.manage(BasePagedTable.this, tableId);
				}
			}
		});
	}

	protected TableColumnsManager newColumnsManager() {
		return new TableColumnsManager();
	}

	protected String getTableId() {
		return tableId;
	}

	@Override
	public HorizontalLayout createControls() {
		int totalAmountOfPages = getTotalAmountOfPages();
		
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

		HorizontalLayout pageSize = new I18NHorizontalLayout(itemsPerPageLabel, itemsPerPageField);
		pageSize.setComponentAlignment(itemsPerPageLabel, Alignment.MIDDLE_LEFT);
		pageSize.setComponentAlignment(itemsPerPageField, Alignment.MIDDLE_LEFT);
		pageSize.setSpacing(true);

		Label page = new Label($("SearchResultTable.page"));
		final TextField currentPage = new TextField();
		currentPage.setConverter(Integer.class);
		currentPage.setConvertedValue(getCurrentPage());
		currentPage.setWidth("45px");
		currentPage.addValidator(
				new IntegerRangeValidator("Wrong page number", 1, totalAmountOfPages));
		currentPage.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (currentPage.isValid() && currentPage.getValue() != null) {
					setCurrentPage((int) currentPage.getConvertedValue());
				}
			}
		});
		currentPage.setEnabled(totalAmountOfPages > 1);

		Label separator = new Label($("SearchResultTable.of"));
		final Label totalPages = new Label(String.valueOf(totalAmountOfPages));

		final Button first = new Button("\uF100", new ClickListener() {
			public void buttonClick(ClickEvent event) {
				onSetPageButtonClicked(0);
			}
		});
		first.setStyleName(ValoTheme.BUTTON_LINK);
		first.setEnabled(getCurrentPage() > 1);

		final Button previous = new Button("\uF104", new ClickListener() {
			public void buttonClick(ClickEvent event) {
				onPreviousPageButtonClicked();
			}
		});
		previous.setStyleName(ValoTheme.BUTTON_LINK);
		previous.setEnabled(getCurrentPage() > 1);

		final Button next = new Button("\uF105", new ClickListener() {
			public void buttonClick(ClickEvent event) {
				onNextPageButtonClicked();
			}
		});
		next.setStyleName(ValoTheme.BUTTON_LINK);
		next.setEnabled(getCurrentPage() < getTotalAmountOfPages());

		final Button last = new Button("\uF101", new ClickListener() {
			public void buttonClick(ClickEvent event) {
				onSetPageButtonClicked(getTotalAmountOfPages());
			}
		});
		last.setStyleName(ValoTheme.BUTTON_LINK);
		last.setEnabled(getCurrentPage() < getTotalAmountOfPages());

		if (isRightToLeft()) {
			String rtlFirstCaption = last.getCaption();
			String rtlPreviousCaption = next.getCaption();
			String rtlNextCaption = previous.getCaption();
			String rtlLastCaption = first.getCaption();
			first.setCaption(rtlFirstCaption);
			previous.setCaption(rtlPreviousCaption);
			next.setCaption(rtlNextCaption);
			last.setCaption(rtlLastCaption);
		}

		HorizontalLayout pageManagement = new I18NHorizontalLayout(
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

		HorizontalLayout controlBar = new I18NHorizontalLayout(pageSize, pageManagement);
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

	protected void onPreviousPageButtonClicked() {
		previousPage();
	}

	protected void onNextPageButtonClicked() {
		nextPage();
	}

	protected void onSetPageButtonClicked(int page) {
		setCurrentPage(page);
	}

	public ComboBox getItemsPerPageField() {
		return itemsPerPageField;
	}

	@Override
	public void paintContent(PaintTarget target)
			throws PaintException {
		super.paintContent(new PaintTargetAdapter(target));
	}

	public void setItemsPerPageValue(int value) {
		itemsPerPageField.setValue(value);
	}

	@Override
	public void setPageLength(int pageLength) {
		super.setPageLength(pageLength);
	}

	protected CellKey getCellKey(Object itemId, Object propertyId) {
		return null;
	}

	@Override
	public void containerItemSetChange(ItemSetChangeEvent event) {
		super.containerItemSetChange(event);
		cellProperties.clear();
	}

	@Override
	public void refreshRenderedCells() {
		super.refreshRenderedCells();
	}

	@Override
	public final Property<?> getContainerProperty(Object itemId, Object propertyId) {
		Property<?> containerProperty;
		CellKey cellKey = getCellKey(itemId, propertyId);
		if (cellKey != null) {
			containerProperty = cellProperties.get(cellKey);
			if (containerProperty == null) {
				containerProperty = loadContainerProperty(itemId, propertyId);
				cellProperties.put(cellKey, containerProperty);
			}
		} else {
			containerProperty = loadContainerProperty(itemId, propertyId);
		}
		return containerProperty;
	}

	protected Property<?> loadContainerProperty(final Object itemId, final Object propertyId) {
		return super.getContainerProperty(itemId, propertyId);
	}

	public T getContainer() {
		return container;
	}

	@SuppressWarnings("deprecation")
	private static class PaintTargetAdapter implements Serializable, PaintTarget {

		private PaintTarget adaptee;

		private PaintTargetAdapter(PaintTarget adaptee) {
			this.adaptee = adaptee;
		}

		public void addSection(String sectionTagName, String sectionData)
				throws PaintException {
			adaptee.addSection(sectionTagName, sectionData);
		}

		public PaintStatus startPaintable(Component paintable, String tag)
				throws PaintException {
			return adaptee.startPaintable(paintable, tag);
		}

		public void endPaintable(Component paintable)
				throws PaintException {
			adaptee.endPaintable(paintable);
		}

		public void startTag(String tagName)
				throws PaintException {
			adaptee.startTag(tagName);
		}

		public void endTag(String tagName)
				throws PaintException {
			adaptee.endTag(tagName);
		}

		private boolean isIgnoredAttribute(String name) {
			return "pagelength".equals(name);
		}

		public void addAttribute(String name, boolean value)
				throws PaintException {
			if (!isIgnoredAttribute(name)) {
				adaptee.addAttribute(name, value);
			}
		}

		public void addAttribute(String name, int value)
				throws PaintException {
			if (!isIgnoredAttribute(name)) {
				adaptee.addAttribute(name, value);
			}
		}

		public void addAttribute(String name, Resource value)
				throws PaintException {
			if (!isIgnoredAttribute(name)) {
				adaptee.addAttribute(name, value);
			}
		}

		public void addVariable(VariableOwner owner, String name, StreamVariable value)
				throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addAttribute(String name, long value)
				throws PaintException {
			adaptee.addAttribute(name, value);
		}

		public void addAttribute(String name, float value)
				throws PaintException {
			adaptee.addAttribute(name, value);
		}

		public void addAttribute(String name, double value)
				throws PaintException {
			adaptee.addAttribute(name, value);
		}

		public void addAttribute(String name, String value)
				throws PaintException {
			adaptee.addAttribute(name, value);
		}

		public void addAttribute(String name, Map<?, ?> value)
				throws PaintException {
			adaptee.addAttribute(name, value);
		}

		public void addAttribute(String name, Component value)
				throws PaintException {
			adaptee.addAttribute(name, value);
		}

		public void addVariable(VariableOwner owner, String name, String value)
				throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addVariable(VariableOwner owner, String name, int value)
				throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addVariable(VariableOwner owner, String name, long value)
				throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addVariable(VariableOwner owner, String name, float value)
				throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addVariable(VariableOwner owner, String name, double value)
				throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addVariable(VariableOwner owner, String name, boolean value)
				throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addVariable(VariableOwner owner, String name, String[] value)
				throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addVariable(VariableOwner owner, String name, Component value)
				throws PaintException {
			adaptee.addVariable(owner, name, value);
		}

		public void addUploadStreamVariable(VariableOwner owner, String name)
				throws PaintException {
			adaptee.addUploadStreamVariable(owner, name);
		}

		public void addXMLSection(String sectionTagName, String sectionData, String namespace)
				throws PaintException {
			adaptee.addXMLSection(sectionTagName, sectionData, namespace);
		}

		public void addUIDL(String uidl)
				throws PaintException {
			adaptee.addUIDL(uidl);
		}

		public void addText(String text)
				throws PaintException {
			adaptee.addText(text);
		}

		public void addCharacterData(String text)
				throws PaintException {
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
