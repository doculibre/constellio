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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
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
public class ListBorrowingsViewImpl extends BaseViewImpl implements ListBorrowingsView {
	private final ListBorrowingsPresenter presenter;

	private LookupRecordField administrativeUnitFilter;
	private CheckBox overdueFilter;
	private TabSheet tabSheet;
	private List<BorrowingTab> tabs;

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
				presenter.setAdministrativeUnit((String) administrativeUnitFilter.getValue());
				refreshTab();
			}
		});
		mainLayout.addComponent(administrativeUnitFilter);

		overdueFilter = new CheckBox($("ListBorrowingsView.overdueFilter"));
		overdueFilter.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.setShowOverdueOnly(overdueFilter.getValue());
				refreshTab();
			}
		});
		mainLayout.addComponent(overdueFilter);

		tabs = new ArrayList<>();
		tabs.add(new BorrowingTab(Document.SCHEMA_TYPE));
		tabs.add(new BorrowingTab(Folder.SCHEMA_TYPE));
		tabs.add(new BorrowingTab(ContainerRecord.SCHEMA_TYPE));

		tabSheet = new TabSheet();
		for (BorrowingTab tab : tabs) {
			tabSheet.addTab(tab.getLayout(), tab.getCaption());
		}
		tabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				refreshTab();
			}
		});
		mainLayout.addComponent(tabSheet);
		refreshTab();

		return mainLayout;
	}

	private void refreshTab() {
		for (BorrowingTab tab : tabs) {
			tab.resetLayout();
			if (tab.layout.equals(tabSheet.getSelectedTab())) {
				tab.setTable(buildBorrowingsTable(tab.getSchemaType()));
			}
		}
	}

	private Component buildBorrowingsTable(String schemaType) {
		final ViewableRecordVOTablePanel table = new ViewableRecordItemTablePanel(presenter.getDataProvider(schemaType));
		table.addStyleName("record-table");
		table.setSizeFull();
		table.setAllItemsVisible(true);
		return table;
	}

	private class BorrowingTab {
		private String schemaType;
		private HorizontalLayout layout;

		private BorrowingTab(String schemaType) {
			this.schemaType = schemaType;
			layout = new HorizontalLayout();
			layout.setSizeFull();
		}

		private void setTable(Component table) {
			layout.addComponent(table);
		}

		private void resetLayout() {
			layout.removeAllComponents();
		}

		private String getSchemaType() {
			return schemaType;
		}

		private Component getLayout() {
			return layout;
		}

		private String getCaption() {
			return schemaType;
		}
	}

	private class ViewableRecordItemTablePanel extends SelectionTable {

		public ViewableRecordItemTablePanel(RecordVODataProvider dataProvider) {
			super(new RecordVOLazyContainer(dataProvider) {
			});

			// TODO::JOLA --> Add visible column (borrowing user, borrowed date, return date, action)
			// TODO::JOLA --> Add a style to "return date" column to highlight overdue in red (see TaskStyleGenerator in TaskTable.java)
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
