package com.constellio.app.modules.rm.ui.pages.borrowing;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionChangeEvent;
import com.constellio.app.ui.framework.components.selection.SelectionComponent.SelectionManager;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.constellio.app.ui.framework.containers.RecordVOContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

// TODO::JOLA --> Fix document "forced return" issue
// TODO::JOLA --> Add batch return option
// TODO::JOLA --> Add batch reminder option
// TODO::JOLA --> Add a config for borrowed document time ***in a side branch: 9.0-patchedVersion-[name]
// TODO::JOLA --> Add a copied metadata in document that track the borrowed date of its content	***in a side branch: 9.0-patchedVersion-[name]
public class ListBorrowingsViewImpl extends BaseViewImpl implements ListBorrowingsView {
	private final ListBorrowingsPresenter presenter;

	private LookupRecordField administrativeUnitFilter;
	private CheckBox overdueFilter;

	public ListBorrowingsViewImpl() {
		presenter = new ListBorrowingsPresenter(this);
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	@Override
	protected String getTitle() {
		return $("ListBorrowingsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);

		administrativeUnitFilter = new LookupRecordField(AdministrativeUnit.SCHEMA_TYPE);
		administrativeUnitFilter.setCaption(AdministrativeUnit.SCHEMA_TYPE);
		administrativeUnitFilter.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				refreshTable();
			}
		});
		mainLayout.addComponent(administrativeUnitFilter);

		overdueFilter = new CheckBox($("ListBorrowingsView.overdueFilter"));
		overdueFilter.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				refreshTable();
			}
		});
		mainLayout.addComponent(overdueFilter);

		// TODO::JOLA --> Optimize tab to load only 1 table at a time
		TabSheet tabSheet = new TabSheet();
		tabSheet.addTab(buildBorrowingsTable(Document.SCHEMA_TYPE), Document.SCHEMA_TYPE);
		tabSheet.addTab(buildBorrowingsTable(Folder.SCHEMA_TYPE), Folder.SCHEMA_TYPE);
		tabSheet.addTab(buildBorrowingsTable(ContainerRecord.SCHEMA_TYPE), ContainerRecord.SCHEMA_TYPE);
		mainLayout.addComponent(tabSheet);

		return mainLayout;
	}

	private void refreshTable() {
		// TODO::JOLA --> Refresh displayed table!
	}

	private Component buildBorrowingsTable(String schemaType) {
		final ViewableRecordVOTablePanel table = new ViewableRecordItemTablePanel(presenter.getDataProvider(schemaType));
		table.addStyleName("record-table");
		table.setSizeFull();
		table.setAllItemsVisible(true);
		return table;
	}

	private class ViewableRecordItemTablePanel extends SelectionTable {

		public ViewableRecordItemTablePanel(RecordVODataProvider dataProvider) {
			super(new RecordVOLazyContainer(dataProvider) {
			});

			// TODO::JOLA --> Add visible column (Action, borrowing user, borrowed date, return date)
			// TODO::JOLA --> Add a style to return date column to highlight overdue in red (see TaskStyleGenerator in TaskTable.java)
			setVisibleColumns();
			setSelectionActionButtons();
			setTableMode(TableMode.TABLE);
		}
	}

	private class SelectionTable extends ViewableRecordVOTablePanel {
		protected Set<Object> selectedItemIds;

		public SelectionTable(RecordVOContainer container) {
			super(container, TableMode.LIST, null, false);
			setAllItemsVisible(true);
		}

		public void initSelectedItemCache() {
			if (selectedItemIds == null) {
				selectedItemIds = new HashSet<>();
			}
		}

		@Override
		protected boolean isSelectColumn() {
			return true;
		}

		@Override
		protected SelectionManager newSelectionManager() {
			return new SelectionManager() {
				@Override
				public void selectionChanged(SelectionChangeEvent event) {
					initSelectedItemCache();

					List<Object> selectedItemIdsFromEvent = event.getSelectedItemIds();
					List<Object> deselectedItemIdsFromEvent = event.getDeselectedItemIds();

					if (deselectedItemIdsFromEvent != null && !deselectedItemIdsFromEvent.isEmpty()) {
						for (Object currentDeselectedItem : deselectedItemIdsFromEvent) {
							selectedItemIds.remove(currentDeselectedItem);
						}
					}

					if (selectedItemIdsFromEvent != null && !selectedItemIdsFromEvent.isEmpty()) {
						for (Object currentselectedItem : selectedItemIdsFromEvent) {
							selectedItemIds.add(currentselectedItem);
						}
					}


					boolean allItemsSelected = event.isAllItemsSelected();
					boolean allItemsDeselected = event.isAllItemsDeselected();
					if (allItemsSelected) {
						Collection<?> itemIds = getItemIds();

						selectedItemIds.addAll(itemIds);
					} else if (allItemsDeselected) {
						selectedItemIds.clear();
					}
				}

				@Override
				public List<Object> getAllSelectedItemIds() {
					initSelectedItemCache();
					return new ArrayList<>(selectedItemIds);
				}

				@Override
				public boolean isAllItemsSelected() {
					boolean allItemsSelected = true;
					for (Object itemId : getItemIds()) {
						if (!isSelected(itemId)) {
							allItemsSelected = false;
							break;
						}
					}
					return allItemsSelected;
				}

				@Override
				public boolean isAllItemsDeselected() {
					boolean allItemsDeselected = true;
					for (Object itemId : getItemIds()) {
						if (isSelected(itemId)) {
							allItemsDeselected = false;
							break;
						}
					}
					return allItemsDeselected;
				}

				@Override
				public boolean isSelected(Object itemId) {
					initSelectedItemCache();


					return selectedItemIds.contains(itemId);
				}

				protected Collection<?> getItemIds() {
					return getActualTable().getItemIds();
				}
			};
		}
	}
}
