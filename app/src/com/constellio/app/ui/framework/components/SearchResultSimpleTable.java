package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.records.RecordNavigationExtension;
import com.constellio.app.extensions.records.params.NavigationParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class SearchResultSimpleTable extends RecordVOTable implements SearchResultTable {
	public static final String TABLE_STYLE = "search-result-table";
	public static final String CHECKBOX_PROPERTY = "checkbox";

	private Set<Object> selected;
	private Set<Object> deselected;
	private Set<SelectionChangeListener> listeners;
	private RecordVOLazyContainer container;
	private boolean selectAll;
	private int maxSelectableResults;

	public SearchResultSimpleTable(RecordVOLazyContainer container, int maxSelectableResults) {
		this(container, true);
		this.maxSelectableResults = maxSelectableResults;
	}

	public SearchResultSimpleTable(final RecordVOLazyContainer container, boolean withCheckBoxes) {
		super("",container);
		
		setColumnCollapsingAllowed(true);
		setColumnReorderingAllowed(true);
		addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
//				if (event.isDoubleClick()) {
					Object itemId = event.getItemId();
					RecordVO recordVO = container.getRecordVO((int) itemId);
					
					Window recordWindow = new BaseWindow();
					recordWindow.setWidth("90%");
					recordWindow.setHeight("90%");
					recordWindow.setContent(new RecordDisplay(recordVO));
					UI.getCurrent().addWindow(recordWindow);
//				}
			}
		});

		// TODO Make header visible
		// TODO Make all columns appear (DataProvider in AdvancedSearchPresenter

		listeners = new HashSet<>();
		selected = new HashSet<>();
		deselected = new HashSet<>();
		if (withCheckBoxes) {
			addGeneratedColumn(CHECKBOX_PROPERTY, new ColumnGenerator() {
				@Override
				public Object generateCell(Table source, final Object itemId, Object columnId) {
					final CheckBox checkBox = new CheckBox();
					boolean checkBoxValue;
					if (selectAll) {
						checkBoxValue = !deselected.contains(itemId);
					} else {
						checkBoxValue = selected.contains(itemId);
					}
					checkBox.setValue(checkBoxValue);
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
					return checkBox;
				}
			});
			setColumnAlignment(CHECKBOX_PROPERTY, Align.CENTER);
		}
		this.container = container;
		setContainerDataSource(this.container);
		setColumnHeader(CHECKBOX_PROPERTY, "");
//		setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		if (withCheckBoxes) {
			// TODO Find out how to access other columns to place checkbox correctly
//			setVisibleColumns(CHECKBOX_PROPERTY,getContainerPropertyIds());
		} else {
			setVisibleColumns(SearchResultContainer.SEARCH_RESULT_PROPERTY);
		}
		setColumnExpandRatio(SearchResultContainer.SEARCH_RESULT_PROPERTY, 1);
//		addStyleName(TABLE_STYLE);
	}

	public List<String> getSelectedRecordIds() {
		List<String> result = new ArrayList<>();
		if (selectAll) {
			for (Object itemId : container.getItemIds(0, maxSelectableResults)) {
				if (!deselected.contains(itemId)) {
					RecordVO record = container.getRecordVO((int) itemId);
					result.add(record.getId());
				}
			}
		} else {
			for (Object itemId : selected) {
				RecordVO record = container.getRecordVO((int) itemId);
				result.add(record.getId());
			}
		}
		return result;
	}
	
	public boolean isSelectAll() {
		return selectAll;
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
		Label totalCount = new Label($("SearchResultTable.count", container.size()));
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
			component.setEnabled(selected.size() > 0);
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
					component.setEnabled(somethingSelected);
				}
			}
		});

		return summaryBar;
	}

	private void fireSelectionChangeEvent() {
		((RecordVOLazyContainer) getContainerDataSource()).refresh();
		if (listeners.isEmpty()) {
			return;
		}

		SelectionChangeEvent event = new SelectionChangeEvent(this, selected, deselected, selectAll);
		for (SelectionChangeListener listener : listeners) {
			listener.selectionChanged(event);
		}
	}

	public static class SelectionChangeEvent implements Serializable {
		private final SearchResultSimpleTable table;
		private final Set<Object> selected;
		private final Set<Object> deselected;
		private final boolean selectAll;

		public SelectionChangeEvent(SearchResultSimpleTable table, Set<Object> selected, Set<Object> deselected, boolean selectAll) {
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

	public void selectAll() {
		selectAll = true;
		selected.clear();
		deselected.clear();
		fireSelectionChangeEvent();
	}

	public void deselectAll() {
		selectAll = false;
		selected.clear();
		deselected.clear();
		fireSelectionChangeEvent();
	}
	
}
