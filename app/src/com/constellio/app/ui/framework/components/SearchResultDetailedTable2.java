package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.SearchResultDetailedTable.SelectionChangeEvent;
import com.constellio.app.ui.framework.components.SearchResultDetailedTable.SelectionChangeListener;
import com.constellio.app.ui.framework.components.converters.BaseStringToIntegerConverter;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingModifyingOneMetadataButton;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class SearchResultDetailedTable2 extends VerticalLayout implements SearchResultTable {

	public static final int DEFAULT_PAGE_LENGTH = 10;
	
	public static final String TABLE_STYLE = "search-result-table";
	public static final String CHECKBOX_PROPERTY = "checkbox";
	
	private boolean withCheckBoxes;
	private Set<Object> selected = new LinkedHashSet<>();
	private Set<Object> deselected = new LinkedHashSet<>();
	
	private List<SelectionChangeListener> selectionChangeListeners = new ArrayList<>();
	private List<PageChangeListener> pageChangeListeners = new ArrayList<>();
	
	private SearchResultContainer container;
	
	private int currentPage;

	public SearchResultDetailedTable2(SearchResultContainer container) {
		this(container, true);
	}

	public SearchResultDetailedTable2(SearchResultContainer container, boolean withCheckBoxes) {
		super();
		this.container = container;
		this.withCheckBoxes = withCheckBoxes;
		setSizeFull();
		addStyleName(TABLE_STYLE);
		
		int start = 0;
		int end = Math.min(container.size(), 100);
		for (int i = start; i < end; i++) {
			final Integer itemId = i;
			if (withCheckBoxes) {
				final CheckBox checkBox = new CheckBox();
				checkBox.addStyleName("search-result-checkbox");
				checkBox.setValue(selected.contains(itemId));
				checkBox.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						if (checkBox.getValue()) {
							selected.add(itemId);
							deselected.remove(itemId);
						} else {
							selected.remove(itemId);
							deselected.add(itemId);
						}
						fireSelectionChangeEvent();
					}
				});
				addComponent(checkBox);
			}
			
			if (container.isIndexProperty()) {
				Label indexLabel = new Label("" + itemId);
				indexLabel.addStyleName("search-result-index");
				indexLabel.setConverter(new BaseStringToIntegerConverter());
			}
			
			SearchResultDisplay searchResultDisplay = (SearchResultDisplay) container.getContainerProperty(itemId, SearchResultContainer.SEARCH_RESULT_PROPERTY).getValue();
			if (withCheckBoxes && container.isIndexProperty()) {
				searchResultDisplay.addStyleName("search-result-with-checkbox-and-index");
			} else if (container.isIndexProperty()) {
				searchResultDisplay.addStyleName("search-result-with-index");
			} else if (withCheckBoxes) {
				searchResultDisplay.addStyleName("search-result-with-checkbox");
			}
			addComponent(searchResultDisplay);
		}
	}

	private void fireSelectionChangeEvent() {
	}

	public List<String> getSelectedRecordIds() {
		List<String> result = new ArrayList<>();
		for (Object itemId : selected) {
			RecordVO record = container.getRecordVO((int) itemId);
			result.add(record.getId());
		}
		return result;
	}

	public List<String> getUnselectedRecordIds() {
		List<String> result = new ArrayList<>();
		for (Object itemId : deselected) {
			RecordVO record = container.getRecordVO((int) itemId);
			result.add(record.getId());
		}
		return result;
	}

	public VerticalLayout createSummary(Component alwaysActive, final Component... extra) {
		return createSummary(Arrays.asList(alwaysActive), extra);
	}

	public VerticalLayout createSummary(List<Component> alwaysActive, final Component... extra) {
		return createSummary(alwaysActive, Arrays.asList(extra));
	}

	public VerticalLayout createSummary(List<Component> alwaysActive, final List<Component> extra) {
		int total = container.getDataProvider().getQTime();

		double totalInSeconds;

		if (total < 10) {
			totalInSeconds = total / 1000.0;
		} else {
			totalInSeconds = Math.round(total / 10.0) / 100.0;
		}
		String qtime = "" + totalInSeconds;

		int size = container.size();
		String key = size <= 1 ? "SearchResultTable.count1" : "SearchResultTable.counts";
		Label totalCount = new Label($(key, size, qtime));
		totalCount.addStyleName(ValoTheme.LABEL_BOLD);

		HorizontalLayout count = new HorizontalLayout(totalCount);
		count.setComponentAlignment(totalCount, Alignment.MIDDLE_LEFT);
		count.setSizeUndefined();
		count.setSpacing(true);

		for (Component component : alwaysActive) {
			count.addComponent(component);
			count.setComponentAlignment(component, Alignment.MIDDLE_LEFT);
		}

		final Label selectedCount = new Label($("SearchResultTable.selection", selected.size()));
		selectedCount.setSizeUndefined();
		selectedCount.setVisible(withCheckBoxes);

		final HorizontalLayout selection = new HorizontalLayout(selectedCount);
		selection.setComponentAlignment(selectedCount, Alignment.MIDDLE_LEFT);
		selection.setSizeUndefined();
		selection.setSpacing(true);
		for (Component component : extra) {
			if (isComponentDisabledBySelection(component)) {
				component.setEnabled(selected.size() > 0);
			}
			selection.addComponent(component);
			selection.setComponentAlignment(component, Alignment.MIDDLE_LEFT);
		}

		VerticalLayout summaryBar = new VerticalLayout(count, selection);
		summaryBar.setWidth("100%");

		addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void selectionChanged(SelectionChangeEvent event) {
				selectedCount.setValue($("SearchResultTable.selection", event.getSelectionSize()));
				for (Component component : extra) {
					if (isComponentDisabledBySelection(component)) {
						component.setEnabled(event.getSelectionSize() > 0);
					} else if (component instanceof BatchProcessingButton) {
						((BatchProcessingButton) component).hasResultSelected(event.getSelectionSize() > 0);
					} else if (component instanceof BatchProcessingModifyingOneMetadataButton) {
						((BatchProcessingModifyingOneMetadataButton) component).hasResultSelected(event.getSelectionSize() > 0);
					}
				}
			}
		});

		return summaryBar;
	}

	private boolean isComponentDisabledBySelection(Component component) {
		return !(component instanceof BatchProcessingButton || component instanceof BatchProcessingModifyingOneMetadataButton || component instanceof ReportTabButton);
	}

	@Override
	public void addItemClickListener(final ItemClickListener listener) {
		addLayoutClickListener(new LayoutClickListener() {
			@Override
			public void layoutClick(LayoutClickEvent event) {
				Component source = event.getComponent();
				if (source instanceof SearchResultDisplay) {
					Integer itemId = getComponentIndex(source);
					if (withCheckBoxes) {
						itemId = 2 * itemId;
					}
					SearchResultVO searchResultVO = container.getSearchResultVO((int) itemId);
					Item item = new BeanItem<SearchResultVO>(searchResultVO);
					Object propertyId = SearchResultContainer.SEARCH_RESULT_PROPERTY;
					MouseEventDetails details = new MouseEventDetails();
					details.setButton(event.getButton());
					details.setAltKey(event.isAltKey());
					details.setCtrlKey(event.isCtrlKey());
					details.setMetaKey(event.isMetaKey());
					details.setShiftKey(event.isShiftKey());
					details.setClientX(event.getClientX());
					details.setClientY(event.getClientY());
					details.setRelativeX(event.getRelativeX());
					details.setRelativeY(event.getRelativeY());
					ItemClickEvent itemClickEvent = new ItemClickEvent(source, item, itemId, propertyId, details);
					listener.itemClick(itemClickEvent);
				}
			}
		});
	}

	public void addSelectionChangeListener(SelectionChangeListener listener) {
		selectionChangeListeners.add(listener);
	}

	public List<PageChangeListener> getPageChangeListeners() {
		return pageChangeListeners;
	}

	public void addPageChangeListener(PageChangeListener listener) {
		if (!pageChangeListeners.contains(listener)) {
			pageChangeListeners.add(listener);
		}
	}

	public void removePageChangeListener(PageChangeListener listener) {
		pageChangeListeners.remove(listener);
	}

	public void selectCurrentPage() {
	}

	public void deselectCurrentPage() {
	}
	
	public int getCurrentPage() {
		return currentPage;
	}
	
	public void setCurrentPage(int page) {
		this.currentPage = page;
	}

	protected void onPreviousPageButtonClicked() {
//		previousPage();
	}

	protected void onNextPageButtonClicked() {
//		nextPage();
	}

	protected void onSetPageButtonClicked(int page) {
//		setCurrentPage(page);
	}

	public int getTotalAmountOfPages() {
		// TODO Auto-generated method stub
		return 1;
	}
	
	public int getPageLength() {
		return 100;
	}

	public void setPageLength(int selectedPageLength) {
		// TODO Auto-generated method stub
		
	}

	public void setItemsPerPageValue(int selectedPageLength) {
		// TODO Auto-generated method stub
		
	}

	public boolean isPaged() {
		return false;
	}

	public PagingControls createControls() {
		return new PagingControls();
	}

	public static interface PageChangeListener {
		public void pageChanged(PagedTableChangeEvent event);
	}

	public class PagedTableChangeEvent {

		public SearchResultDetailedTable2 getTable() {
			return SearchResultDetailedTable2.this;
		}

		public int getCurrentPage() {
			return SearchResultDetailedTable2.this.getCurrentPage();
		}

		public int getTotalAmountOfPages() {
			return SearchResultDetailedTable2.this.getTotalAmountOfPages();
		}
	}

	public class PagingControls extends I18NHorizontalLayout {

		private int itemsPerPageValue = getPageLength();

		private ComboBox itemsPerPageField;
		private Label itemsPerPageLabel;
		private HorizontalLayout pageSizeLayout;
		private Label currentPageLabel;
		private TextField currentPageField;
		private Label separator;
		private Label totalPagesLabel;
		private Button firstPageButton;
		private Button previousPageButton;
		private Button nextPageButton;
		private Button lastPageButton;
		private HorizontalLayout pageManagementLayout;

		public PagingControls() {
			if (isPaged()) {
				itemsPerPageField = new BaseComboBox();
				itemsPerPageField.setValue(itemsPerPageValue);

				int totalAmountOfPages = getTotalAmountOfPages();

				itemsPerPageLabel = new Label($("SearchResultTable.itemsPerPage"));
				itemsPerPageField.addItem(DEFAULT_PAGE_LENGTH);

				int realSize = container.size();
				if (realSize >= 10) {
					itemsPerPageField.addItem(10);
				}
				if (realSize > 10) {
					itemsPerPageField.addItem(25);
				}
				if (realSize > 25) {
					itemsPerPageField.addItem(50);
				}
				if (realSize > 50) {
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

				pageSizeLayout = new I18NHorizontalLayout(itemsPerPageLabel, itemsPerPageField);
				pageSizeLayout.setComponentAlignment(itemsPerPageLabel, Alignment.MIDDLE_LEFT);
				pageSizeLayout.setComponentAlignment(itemsPerPageField, Alignment.MIDDLE_LEFT);
				pageSizeLayout.setSpacing(true);

				currentPageLabel = new Label($("SearchResultTable.page"));
				currentPageField = new TextField();
				currentPageField.setConverter(Integer.class);
				currentPageField.setConvertedValue(getCurrentPage());
				currentPageField.setWidth("45px");
				currentPageField.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						boolean valid;
						String newValue = currentPageField.getValue();
						if (StringUtils.isNotBlank(newValue)) {
							try {
								int newIntValue = Integer.parseInt(newValue);
								valid = newIntValue > 0 && newIntValue < getTotalAmountOfPages();
							} catch (NumberFormatException e) {
								valid = false;
							}
						} else {
							valid = false;
						}
						if (valid) {
							setCurrentPage((int) currentPageField.getConvertedValue());
						}
					}
				});
				currentPageField.setEnabled(totalAmountOfPages > 1);

				separator = new Label($("SearchResultTable.of"));
				totalPagesLabel = new Label(String.valueOf(totalAmountOfPages));

				firstPageButton = new Button("\uF100", new ClickListener() {
					public void buttonClick(ClickEvent event) {
						onSetPageButtonClicked(0);
					}
				});
				firstPageButton.setStyleName(ValoTheme.BUTTON_LINK);
				firstPageButton.setEnabled(getCurrentPage() > 1);

				previousPageButton = new Button("\uF104", new ClickListener() {
					public void buttonClick(ClickEvent event) {
						onPreviousPageButtonClicked();
					}
				});
				previousPageButton.setStyleName(ValoTheme.BUTTON_LINK);
				previousPageButton.setEnabled(getCurrentPage() > 1);

				nextPageButton = new Button("\uF105", new ClickListener() {
					public void buttonClick(ClickEvent event) {
						onNextPageButtonClicked();
					}
				});
				nextPageButton.setStyleName(ValoTheme.BUTTON_LINK);
				nextPageButton.setEnabled(getCurrentPage() < getTotalAmountOfPages());

				lastPageButton = new Button("\uF101", new ClickListener() {
					public void buttonClick(ClickEvent event) {
						onSetPageButtonClicked(getTotalAmountOfPages());
					}
				});
				lastPageButton.setStyleName(ValoTheme.BUTTON_LINK);
				lastPageButton.setEnabled(getCurrentPage() < getTotalAmountOfPages());

				if (isRightToLeft()) {
					String rtlFirstCaption = lastPageButton.getCaption();
					String rtlPreviousCaption = nextPageButton.getCaption();
					String rtlNextCaption = previousPageButton.getCaption();
					String rtlLastCaption = firstPageButton.getCaption();
					firstPageButton.setCaption(rtlFirstCaption);
					previousPageButton.setCaption(rtlPreviousCaption);
					nextPageButton.setCaption(rtlNextCaption);
					lastPageButton.setCaption(rtlLastCaption);
				}

				pageManagementLayout = new I18NHorizontalLayout(
						firstPageButton, previousPageButton, currentPageLabel, currentPageField, separator, totalPagesLabel, nextPageButton, lastPageButton);
				pageManagementLayout.setComponentAlignment(firstPageButton, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(previousPageButton, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(currentPageLabel, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(currentPageField, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(separator, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(totalPagesLabel, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(nextPageButton, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setComponentAlignment(lastPageButton, Alignment.MIDDLE_LEFT);
				pageManagementLayout.setSpacing(true);

				addComponents(pageSizeLayout, pageManagementLayout);
				setComponentAlignment(pageManagementLayout, Alignment.MIDDLE_CENTER);
				setExpandRatio(pageSizeLayout, 1);
				setWidth("100%");

				addPageChangeListener(new PageChangeListener() {
					public void pageChanged(PagedTableChangeEvent event) {
						firstPageButton.setEnabled(getCurrentPage() > 1);
						previousPageButton.setEnabled(getCurrentPage() > 1);
						nextPageButton.setEnabled(getCurrentPage() < getTotalAmountOfPages());
						lastPageButton.setEnabled(getCurrentPage() < getTotalAmountOfPages());
						currentPageField.setValue(String.valueOf(getCurrentPage()));
						currentPageField.setEnabled(getTotalAmountOfPages() > 1);
						totalPagesLabel.setValue(String.valueOf(getTotalAmountOfPages()));
					}
				});
			} else {
				setVisible(false);
			}
		}

		public void setItemsPerPageValue(int value) {
			this.itemsPerPageValue = value;
			if (itemsPerPageField != null) {
				itemsPerPageField.setValue(value);
			}
		}

	}
	
}
