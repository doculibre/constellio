/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.pages.home;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedListener.TreeListener;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTreeItemEvent;

import com.constellio.app.modules.rm.ui.components.contextmenu.DocumentContextMenuImpl;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.tree.RecordLazyTree;
import com.constellio.app.ui.framework.components.tree.RecordLazyTreeTabSheet;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;

public class RecordsManagementViewImpl extends BaseViewImpl implements RecordsManagementView {

	private List<RecordsManagementViewTab> tabs = new ArrayList<RecordsManagementViewTab>();

	private TabSheet tabSheet;

	private RecordsManagementPresenter presenter;
	
	private SelectedTabChangeListener selectedTabChangeListener;

	public RecordsManagementViewImpl() {
		tabSheet = new TabSheet();
		tabSheet.addStyleName("records-management");
		presenter = new RecordsManagementPresenter(this);
		
		selectedTabChangeListener = new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				Component selectedComponent = tabSheet.getSelectedTab();
				Tab selectedTab = tabSheet.getTab(selectedComponent);
				selectTab(selectedTab);
			}
		};
	}
	
	private void selectTab(Tab selectedTab) {
		if (selectedTab != null) {
			int indexOfSelectedTab = tabSheet.getTabPosition(selectedTab);
			tabSheet.setSelectedTab(indexOfSelectedTab);
			RecordsManagementViewTab tabSource = tabs.get(indexOfSelectedTab);
			String tabName = tabSource.getTabName();
			PlaceHolder tabComponent = (PlaceHolder) selectedTab.getComponent();
			if (tabComponent.getComponentCount() == 0) {
				tabComponent.setCompositionRoot(getTabComponent(selectedTab));
			}
			ParamUtils.setParams(tabName);
		}
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected String getTitle() {
		return $("RecordsManagementView.viewTitle");
	}

	@SuppressWarnings("unchecked")
	private Component getTabComponent(Tab tab) {
		int indexOfSelectedTab = tabSheet.getTabPosition(tab);
		RecordsManagementViewTab tabSource = tabs.get(indexOfSelectedTab);
		Component tabComponent;
		switch (tabSource.getTabType()) {
		case RECORD_LIST:
			tabComponent = newRecordListTable((RecordVODataProvider) tabSource.getDataProvider());
			break;
		case RECORD_TREE:
			List<RecordLazyTreeDataProvider> dataProviders = (List<RecordLazyTreeDataProvider>) tabSource.getDataProviders();
			if (dataProviders.size() > 1) {
				TabSheet subTabSheet = new RecordLazyTreeTabSheet(dataProviders) {
					@Override
					protected RecordLazyTree newLazyTree(RecordLazyTreeDataProvider dataProvider, int bufferSize) {
						return newRecordTree(dataProvider);
					}
				};
				selectDefaultUserTaxonomyTab(dataProviders, subTabSheet);
				tabComponent = subTabSheet;
			} else {
				tabComponent = newRecordTree((RecordLazyTreeDataProvider) tabSource.getDataProvider());
			}
			break;
		default:
			throw new RuntimeException("Invalid tab type : " + tabSource.getTabType());
		}
		return tabComponent;
	}

	@Override
	public List<RecordsManagementViewTab> getTabs() {
		return tabs;
	}

	@Override
	public void setTabs(List<RecordsManagementViewTab> viewTabs, RecordsManagementViewTab initialTab) {
		this.tabs = viewTabs;
		
		tabSheet.removeAllComponents();
		tabSheet.removeSelectedTabChangeListener(selectedTabChangeListener);
		
		Tab selectedTab = null;
		for (RecordsManagementViewTab viewTab : viewTabs) {
			String tabName = viewTab.getTabName();
			String tabCaption = $("RecordsManagementView.tab." + tabName);
			TabSheet.Tab tab = tabSheet.addTab(new PlaceHolder(), tabCaption);
			tab.setEnabled(viewTab.isEnabled());
			if (viewTab == initialTab) {
				selectedTab = tab;
			}
		}
		
		tabSheet.addSelectedTabChangeListener(selectedTabChangeListener);
		if (viewTabs.isEmpty()) {
			tabSheet.setVisible(false);
		} else {
			selectTab(selectedTab);
		}
	}

	private RecordLazyTree newRecordTree(RecordLazyTreeDataProvider dataProvider) {
		final RecordLazyTree lazyTree = new RecordLazyTree(dataProvider, 100);
		ItemClickListener itemClickListener = new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getButton() == MouseButton.LEFT) {
					String recordId = (String) event.getItemId();
					presenter.recordClicked(recordId);
				}
			}
		};
		lazyTree.addItemClickListener(itemClickListener);

		final DocumentContextMenuImpl contextMenu = new DocumentContextMenuImpl();
		contextMenu.setAsTreeContextMenu(lazyTree.getNestedTree());
		TreeListener contextMenuTreeListener = new TreeListener() {
			@Override
			public void onContextMenuOpenFromTreeItem(ContextMenuOpenedOnTreeItemEvent event) {
				String recordId = (String) event.getItemId();
				contextMenu.openFor(recordId);
			}
		};
		contextMenu.addContextMenuTreeListener(contextMenuTreeListener);

		return lazyTree;
	}

	@SuppressWarnings("unchecked")
	private Table newRecordListTable(RecordVODataProvider dataProvider) {
		final Container recordsContainer = new RecordVOLazyContainer(dataProvider);
		Table table = new RecordVOTable();
		table.setContainerDataSource(recordsContainer);
		table.setSizeFull();
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 47);
		Collection<MetadataVO> properties = (Collection<MetadataVO>) recordsContainer.getContainerPropertyIds();
		for (MetadataVO property : properties) {
			if (property.getCode() != null && property.getCode().contains(Schemas.MODIFIED_ON.getLocalCode())) {
				table.setColumnWidth(property, 180);
			}
		}

		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getButton() == MouseButton.LEFT) {
					RecordVOItem recordItem = (RecordVOItem) event.getItem();
					RecordVO recordVO = recordItem.getRecord();
					presenter.recordClicked(recordVO.getId());
				}
			}
		});

		return table;
	}

	private void selectDefaultUserTaxonomyTab(List<RecordLazyTreeDataProvider> dataProviders, TabSheet subTabSheet) {
		for (int i = 0; i < dataProviders.size(); i++) {
			RecordLazyTreeTabSheet recordLazyTreeTabSheet = (RecordLazyTreeTabSheet) subTabSheet;
			RecordLazyTree recordLazyTree = (RecordLazyTree) recordLazyTreeTabSheet.getTab(i).getComponent();
			RecordLazyTreeDataProvider recordLazyTreeDataProvider = (RecordLazyTreeDataProvider) recordLazyTree
					.getDataProvider();
			if (recordLazyTreeDataProvider.getTaxonomyCode().equals(presenter.getCurrentUser().getDefaultTaxonomy())) {
				subTabSheet.setSelectedTab(i);
			}
		}
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		return tabSheet;
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();
		if (!tabs.isEmpty()) {
			Button addFolderButton = new AddButton($("RecordsManagementView.addFolder")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.addFolderButtonClicked();
				}
			};
			addFolderButton.addStyleName("addFolder");

			Button addDocumentButton = new AddButton($("RecordsManagementView.addDocument")) {
				@Override
				protected void buttonClick(ClickEvent event) {
					presenter.addDocumentButtonClicked();
				}
			};
			addDocumentButton.addStyleName("addDocument");

			actionMenuButtons.add(addFolderButton);
			actionMenuButtons.add(addDocumentButton);
		}
		return actionMenuButtons;
	}

	private static class PlaceHolder extends CustomComponent {

		@Override
		public void setCompositionRoot(Component compositionRoot) {
			super.setCompositionRoot(compositionRoot);
		}

	}

}
