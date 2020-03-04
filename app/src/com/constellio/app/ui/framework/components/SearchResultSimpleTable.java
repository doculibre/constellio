package com.constellio.app.ui.framework.components;

import com.constellio.app.api.extensions.params.GetSearchResultSimpleTableWindowComponentParam;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.fields.number.BaseIntegerField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.SelectionTableAdapter;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.search.AdvancedSearchPresenter;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingModifyingOneMetadataButton;
import com.vaadin.data.Validator;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public class SearchResultSimpleTable extends SelectionTableAdapter implements SearchResultTable {

	public static final String CHECKBOX_PROPERTY = "checkbox";

	private static final int MAX_SELECTION_RANGE = 100;

	private int lastSelectedItemIndex = -1;
	private Set<Object> selectedItemIds;
	private Set<Object> deselectedItemIds;
	private Set<SelectionChangeListener> listeners;
	private RecordVOLazyContainer recordVOContainer;
	private boolean allItemsSelected;
	private AdvancedSearchPresenter presenter;
	RecordVOTable adaptee;

	public SearchResultSimpleTable(RecordVOLazyContainer container, final AdvancedSearchPresenter presenter) {
		super();
		this.recordVOContainer = container;
		this.presenter = presenter;

		adaptee = new RecordVOTable(container) {
			@Override
			public void sort(Object[] propertyId, boolean[] ascending) throws UnsupportedOperationException {
				super.sort(propertyId, ascending);
				deselectAll();
			}
		};
		adaptee.setWidth("100%");

		adaptee.setColumnCollapsingAllowed(true);
		adaptee.setColumnReorderingAllowed(true);
		adaptee.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				//				if (event.isDoubleClick()) {
				Object itemId = event.getItemId();
				RecordVO recordVO = recordVOContainer.getRecordVO((int) itemId);

				AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
				GetSearchResultSimpleTableWindowComponentParam param = new GetSearchResultSimpleTableWindowComponentParam(recordVO, presenter.getUser());

				Component windowComponent = appLayerFactory.getExtensions()
						.forCollection(recordVO.getSchema().getCollection()).getSimpleTableWindowComponent(param);
				if (windowComponent == null) {
					windowComponent = new RecordDisplay(recordVO);
				}

				final Component finalWindowComponent = windowComponent;
				DragAndDropWrapper dragAndDropWrapper = new DragAndDropWrapper(windowComponent);
				if(finalWindowComponent instanceof DropHandler) {
					dragAndDropWrapper.setDropHandler((DropHandler) windowComponent);
				}
				dragAndDropWrapper.setSizeFull();

				BaseWindow recordWindow = new BaseWindow();
				recordWindow.setWidth("95%");
				recordWindow.setHeight("98%");
				recordWindow.center();
				recordWindow.setContent(dragAndDropWrapper);

				presenter.logRecordView(recordVO);
				UI.getCurrent().addWindow(recordWindow);
				//				}
			}
		});

		// TODO Make header visible
		// TODO Make all columns appear (DataProvider in AdvancedSearchPresenter

		listeners = new HashSet<>();
		selectedItemIds = new LinkedHashSet<>();
		deselectedItemIds = new LinkedHashSet<>();
		adaptee.setColumnExpandRatio(SearchResultContainer.SEARCH_RESULT_PROPERTY, 1);
		//		addStyleName(TABLE_STYLE);

		setTable(adaptee);
		getToggleButton().setVisible(false);
	}

	public void addItemClickListener(final ItemClickListener listener) {
		adaptee.addItemClickListener(listener);
	}

	public List<String> getSelectedRecordIds() {
		List<String> result = new ArrayList<>();
		for (Object itemId : selectedItemIds) {
			//			RecordVO record = recordVOContainer.getRecordVO((int) itemId);
			result.add((String) itemId);
		}
		Collections.sort(result, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return 0;
			}
		});
		return result;
	}

	public List<String> getUnselectedRecordIds() {
		List<String> result = new ArrayList<>();
		for (Object itemId : deselectedItemIds) {
			//			RecordVO record = recordVOContainer.getRecordVO((int) itemId);
			result.add((String) itemId);
		}
		return result;
	}

	public boolean isSelectAll() {
		return allItemsSelected;
	}

	public void addSelectionChangeListener(SelectionChangeListener listener) {
		listeners.add(listener);
	}

	public VerticalLayout createSummary(Component alwaysActive, final Component... extra) {
		return createSummary(Arrays.asList(alwaysActive), extra);
	}

	public VerticalLayout createSummary(List<Component> alwaysActive, final Component... extra) {
		return createSummary(alwaysActive, Arrays.asList(extra));
	}

	public VerticalLayout createSummary(List<Component> alwaysActive, final List<Component> extra) {

		int size = recordVOContainer.size();
		String key = size <= 1 ? "SearchResultTable.count1" : "SearchResultTable.counts";
		Label totalCount = new Label($(key, size, recordVOContainer.getLastCallQTime()));
		totalCount.addStyleName(ValoTheme.LABEL_BOLD);

		HorizontalLayout count = new HorizontalLayout(totalCount);
		count.setComponentAlignment(totalCount, Alignment.MIDDLE_LEFT);
		count.setSizeUndefined();
		count.setSpacing(true);

		for (Component component : alwaysActive) {
			count.addComponent(component);
			count.setComponentAlignment(component, Alignment.MIDDLE_LEFT);
		}

		final HorizontalLayout selection = new HorizontalLayout();
		selection.setSizeUndefined();
		selection.setSpacing(true);
		for (Component component : extra) {
			if (component instanceof BatchProcessingButton || component instanceof BatchProcessingModifyingOneMetadataButton) {
				component.setEnabled(recordVOContainer != null && recordVOContainer.size() > 0);
			} else {
				component.setEnabled(selectedItemIds.size() > 0);
			}
			selection.addComponent(component);
			selection.setComponentAlignment(component, Alignment.MIDDLE_LEFT);
		}

		VerticalLayout summaryBar = new VerticalLayout(count, selection);
		summaryBar.setWidth("100%");

		addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void selectionChanged(SelectionChangeEvent event) {
				boolean somethingSelected = event.isSelectAll() || !event.getSelected().isEmpty();
				for (Component component : extra) {
					if (component instanceof BatchProcessingButton
						|| component instanceof BatchProcessingModifyingOneMetadataButton) {
						component.setEnabled(recordVOContainer != null && recordVOContainer.size() > 0);
					} else {
						component.setEnabled(somethingSelected);
					}
				}
			}
		});

		return summaryBar;
	}

	private void fireSelectionChangeEvent() {
		//		recordVOContainer.refresh();
		SelectionChangeEvent event = new SelectionChangeEvent(this, selectedItemIds, deselectedItemIds, allItemsSelected);
		for (SelectionChangeListener listener : listeners) {
			listener.selectionChanged(event);
		}
		updateVisibleCheckBoxes();
	}

	public static class SelectionChangeEvent implements Serializable {
		private final SearchResultSimpleTable table;
		private final Set<Object> selected;
		private final Set<Object> deselected;
		private final boolean selectAll;

		public SelectionChangeEvent(SearchResultSimpleTable table, Set<Object> selected, Set<Object> deselected,
									boolean selectAll) {
			this.table = table;
			this.selected = selected;
			this.deselected = deselected;
			this.selectAll = selectAll;
		}

		public SearchResultSimpleTable getTable() {
			return table;
		}

		public Set<Object> getSelected() {
			return selected;
		}

		public Set<Object> getDeselected() {
			return deselected;
		}

		public boolean isSelectAll() {
			return selectAll;
		}
	}

	public interface SelectionChangeListener extends Serializable {
		void selectionChanged(SelectionChangeEvent event);
	}

	@Override
	public void selectAll() {
		lastSelectedItemIndex = recordVOContainer.size() - 1;
		allItemsSelected = true;
		selectedItemIds.clear();
		deselectedItemIds.clear();
		updateVisibleCheckBoxes();
		fireSelectionChangeEvent();
	}

	@Override
	public void deselectAll() {
		lastSelectedItemIndex = -1;
		allItemsSelected = false;
		selectedItemIds.clear();
		deselectedItemIds.clear();
		updateVisibleCheckBoxes();
		fireSelectionChangeEvent();
	}

	@Override
	public boolean isAllItemsSelected() {
		return allItemsSelected;
	}

	@Override
	public boolean isAllItemsDeselected() {
		return !allItemsSelected && deselectedItemIds.isEmpty();
	}

	@Override
	public boolean isSelected(Object itemId) {
		boolean selectedItem;
		if (allItemsSelected) {
			selectedItem = !this.deselectedItemIds.contains(recordVOContainer.getRecordVO((int) itemId).getId());
		} else {
			selectedItem = this.selectedItemIds.contains(recordVOContainer.getRecordVO((int) itemId).getId());
		}
		return selectedItem;
	}

	@Override
	public void setSelected(Object itemId, boolean selected) {
		setSelected(itemId, selected, true);
	}

	private void setSelected(Object itemId, boolean selected, boolean fireSelectionChangeEvent) {
		if (selected) {
			this.selectedItemIds.add(recordVOContainer.getRecordVO((int) itemId).getId());
			deselectedItemIds.remove(recordVOContainer.getRecordVO((int) itemId).getId());
			presenter.fireSomeRecordsSelected();
		} else {
			this.selectedItemIds.remove(recordVOContainer.getRecordVO((int) itemId).getId());
			deselectedItemIds.add(recordVOContainer.getRecordVO((int) itemId).getId());
			if (selectedItemIds.isEmpty()) {
				presenter.fireNoRecordSelected();
			}
		}
		if (fireSelectionChangeEvent) {
			fireSelectionChangeEvent();
		}
	}

	public void askSelectionRange() {
		final Window window = new Window();

		Label windowTitleLabel = new Label($("SearchResultSimpleTable.selectionWindowTitle"));

		Label fromLabel = new Label($("SearchResultSimpleTable.selectionFrom"));
		final BaseIntegerField fromField = new BaseIntegerField();
		Label toLabel = new Label($("SearchResultSimpleTable.selectionTo"));
		final BaseIntegerField toField = new BaseIntegerField();
		Button selectButton = new BaseButton($("SearchResultSimpleTable.selectButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (fromField.isValid() && toField.isValid()) {
					int start = (Integer) fromField.getConvertedValue() - 1;
					int end = (Integer) toField.getConvertedValue();
					List<?> itemIds = recordVOContainer.getItemIds(start, Math.min(MAX_SELECTION_RANGE, (end - start)));
					for (Object itemId : itemIds) {
						setSelected(itemId, true, false);
					}
					lastSelectedItemIndex = Math.max(0, end - 1);
					window.close();
					fireSelectionChangeEvent();
				}
			}
		};

		fromField.addValidator(new Validator() {
			@Override
			public void validate(Object value)
					throws InvalidValueException {
				boolean invalidValue;
				int start = (Integer) fromField.getConvertedValue() - 1;
				if (start < 0) {
					invalidValue = true;
				} else if (start > recordVOContainer.size() + 1) {
					invalidValue = true;
				} else {
					invalidValue = false;
				}
				if (invalidValue) {
					int maxValue = Math.min(MAX_SELECTION_RANGE, recordVOContainer.size());
					String errorMessage = $("SearchResultSimpleTable.invalidSelection", maxValue, 1, recordVOContainer.size());
					showErrorMessage(errorMessage);
					throw new InvalidValueException(errorMessage);
				}
			}
		});

		toField.addValidator(new Validator() {
			@Override
			public void validate(Object value)
					throws InvalidValueException {
				boolean invalidValue;
				int start = (Integer) fromField.getConvertedValue() - 1;
				int end = ((Integer) value);
				if (end < 0 || end < start) {
					invalidValue = true;
				} else if (end - start > MAX_SELECTION_RANGE) {
					invalidValue = true;
				} else if (end > recordVOContainer.size()) {
					invalidValue = true;
				} else {
					invalidValue = false;
				}
				if (invalidValue) {
					int maxValue = Math.min(MAX_SELECTION_RANGE, recordVOContainer.size());
					String errorMessage = $("SearchResultSimpleTable.invalidSelection", maxValue, 1, recordVOContainer.size());
					showErrorMessage(errorMessage);
					throw new InvalidValueException(errorMessage);
				}
			}
		});

		int nextSelectionStartIndex = computeNextSelectionStartIndex() + 1;
		if (nextSelectionStartIndex > recordVOContainer.size()) {
			nextSelectionStartIndex = recordVOContainer.size();
		}
		int nextSelectionEndIndex = computeNextSelectionEndIndex();

		fromField.setRequired(true);
		fromField.setImmediate(true);
		fromField.setWidth("80px");
		fromField.setValue("" + nextSelectionStartIndex);

		toField.setRequired(true);
		toField.setImmediate(true);
		toField.setWidth("80px");
		toField.setValue("" + nextSelectionEndIndex);

		Button deselectAllButton = new BaseButton($("deselectAll")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				deselectAll();
				window.close();
			}
		};
		if (selectedItemIds.isEmpty()) {
			deselectAllButton.setVisible(false);
			window.setHeight("120px");
		} else {
			window.setHeight("180px");
		}

		HorizontalLayout formLayout = new HorizontalLayout(fromLabel, fromField, toLabel, toField, selectButton);
		formLayout.setSpacing(true);

		VerticalLayout mainLayout = new VerticalLayout(windowTitleLabel, formLayout, deselectAllButton);
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		mainLayout.setComponentAlignment(deselectAllButton, Alignment.MIDDLE_CENTER);

		window.setContent(mainLayout);

		window.setWidth("450px");
		window.center();
		UI.getCurrent().addWindow(window);
	}

	private int computeNextSelectionStartIndex() {
		int nextSelectionStartIndex;
		if (lastSelectedItemIndex == -1) {
			nextSelectionStartIndex = 0;
		} else {
			nextSelectionStartIndex = lastSelectedItemIndex + 1;
		}
		return nextSelectionStartIndex;
	}

	private int computeNextSelectionEndIndex() {
		int nextSelectionEndIndex;
		if (lastSelectedItemIndex == -1) {
			nextSelectionEndIndex = Math.min(recordVOContainer.size(), MAX_SELECTION_RANGE);
		} else {
			nextSelectionEndIndex = Math.min(recordVOContainer.size(), computeNextSelectionStartIndex() + MAX_SELECTION_RANGE);
		}
		return nextSelectionEndIndex;
	}

	private void showErrorMessage(String errorMessage) {
		BaseViewImpl view = (BaseViewImpl) ConstellioUI.getCurrent().getCurrentView();
		view.showErrorMessage(errorMessage);
	}

	@Override
	protected boolean isIndexProperty() {
		return true;
	}

}
