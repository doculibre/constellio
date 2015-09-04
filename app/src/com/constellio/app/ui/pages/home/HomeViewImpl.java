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
package com.constellio.app.ui.pages.home;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.entities.navigation.PageItem;
import com.constellio.app.entities.navigation.PageItem.RecordTable;
import com.constellio.app.entities.navigation.PageItem.RecordTree;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenu;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.tree.RecordLazyTree;
import com.constellio.app.ui.framework.components.tree.RecordLazyTreeTabSheet;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;

public class HomeViewImpl extends BaseViewImpl implements HomeView {
	private final HomePresenter presenter;
	private List<PageItem> tabs;
	private TabSheet tabSheet;

	public HomeViewImpl() {
		presenter = new HomePresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		tabs = presenter.forParams(event.getParameters()).getTabs();
	}

	@Override
	protected String getTitle() {
		return $("HomeView.viewTitle");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);
		for (final NavigationItem item : presenter.getMenuItems()) {
			ComponentState state = presenter.getStateFor(item);
			Button button = new Button($("HomeView." + item.getCode()));
			button.setVisible(state.isVisible());
			button.setEnabled(state.isEnabled());
			button.addStyleName(item.getCode());
			button.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					item.activate(navigateTo());
				}
			});
			buttons.add(button);
		}
		return buttons;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		tabSheet = new TabSheet();
		tabSheet.addStyleName("records-management");

		tabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				Tab currentTab = tabSheet.getTab(tabSheet.getSelectedTab());
				selectTab(currentTab);
			}
		});

		Map<String, Tab> tabsByCode = new HashMap<>();
		for (PageItem item : tabs) {
			Tab tab = tabSheet.addTab(new PlaceHolder(), $("HomeView.tab." + item.getCode()));
			tabsByCode.put(item.getCode(), tab);
		}

		selectTab(tabsByCode.get(presenter.getDefaultTab()));

		return tabSheet;
	}

	private void selectTab(Tab tab) {
		if (tab == null) {
			return;
		}

		int position = tabSheet.getTabPosition(tab);
		tabSheet.setSelectedTab(position);
		PageItem item = tabs.get(position);

		PlaceHolder tabComponent = (PlaceHolder) tab.getComponent();
		if (tabComponent.getComponentCount() == 0) {
			tabComponent.setCompositionRoot(buildComponentFor(tab));
		}

		ParamUtils.setParams(item.getCode());
	}

	private Component buildComponentFor(Tab tab) {
		int indexOfSelectedTab = tabSheet.getTabPosition(tab);
		PageItem tabSource = tabs.get(indexOfSelectedTab);
		switch (tabSource.getType()) {
		case RECORD_TABLE:
			return buildRecordTable((RecordTable) tabSource);
		case RECORD_TREE:
			return buildRecordTreeOrRecordMultiTree((RecordTree) tabSource);
		default:
			throw new RuntimeException("Unsupported tab type : " + tabSource.getType());
		}
	}

	private Table buildRecordTable(RecordTable recordTable) {
		Table table = new RecordVOTable(
				recordTable.getDataProvider(getConstellioFactories().getModelLayerFactory(), getSessionContext()));
		table.setSizeFull();
		for (Object item : table.getContainerPropertyIds()) {
			MetadataVO property = (MetadataVO) item;
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

	private Component buildRecordTreeOrRecordMultiTree(RecordTree recordTree) {
		List<RecordLazyTreeDataProvider> providers = recordTree.getDataProviders(
				getConstellioFactories().getModelLayerFactory(), getSessionContext());
		return providers.size() > 1 ?
				buildRecordMultiTree(recordTree, providers) :
				buildRecordTree(recordTree, providers.get(0));
	}

	private RecordLazyTreeTabSheet buildRecordMultiTree(final RecordTree recordTree, List<RecordLazyTreeDataProvider> providers) {
		RecordLazyTreeTabSheet tabSheet = new RecordLazyTreeTabSheet(providers) {
			@Override
			protected RecordLazyTree newLazyTree(RecordLazyTreeDataProvider dataProvider, int bufferSize) {
				return buildRecordTree(recordTree, dataProvider);
			}
		};
		//tabSheet.setSelectedTab(recordTree.getDefaultTab());
		return tabSheet;
	}

	private RecordLazyTree buildRecordTree(RecordTree recordTree, RecordLazyTreeDataProvider provider) {
		RecordLazyTree tree = new RecordLazyTree(provider, 20);
		tree.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getButton() == MouseButton.LEFT) {
					String recordId = (String) event.getItemId();
					presenter.recordClicked(recordId);
				}
			}
		});
		BaseContextMenu menu = recordTree.getContextMenu();
		if (menu != null) {
			menu.setAsTreeContextMenu(tree.getNestedTree());
		}
		return tree;
	}

	private static class PlaceHolder extends CustomComponent {
		@Override
		public void setCompositionRoot(Component compositionRoot) {
			super.setCompositionRoot(compositionRoot);
		}
	}
}
