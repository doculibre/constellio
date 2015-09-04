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
package com.constellio.app.modules.rm.ui.pages.containers;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.AdminUnitsWithContainersCountContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;

public class ContainersByAdministrativeUnitsViewImpl extends BaseViewImpl implements ContainersByAdministrativeUnitsView {

	private List<ContainersViewTab> tabs = new ArrayList<ContainersViewTab>();

	private TabSheet tabSheet;

	private ContainersByAdministrativeUnitsPresenter presenter;

	public ContainersByAdministrativeUnitsViewImpl() {
		tabSheet = new TabSheet();
		tabSheet.addStyleName("containers-by-admin-units");
		tabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				Component selectedComponent = tabSheet.getSelectedTab();
				Tab selectedTab = tabSheet.getTab(selectedComponent);
				if (selectedTab != null) {
					int indexOfSelectedTab = tabSheet.getTabPosition(selectedTab);
					ContainersViewTab tabSource = tabs.get(indexOfSelectedTab);
					String tabName = tabSource.getTabName();
					PlaceHolder tabComponent = (PlaceHolder) selectedTab.getComponent();
					if (tabComponent.getComponentCount() == 0) {
						tabComponent.setCompositionRoot(getRecordsList(selectedTab));
					}
					ParamUtils.setParams(tabName);
				}
			}
		});
		presenter = new ContainersByAdministrativeUnitsPresenter(this);
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

	private Component getRecordsList(Tab selectedTab) {

		int indexOfSelectedTab = tabSheet.getTabPosition(selectedTab);
		final ContainersViewTab tabSource = tabs.get(indexOfSelectedTab);
		RecordVOLazyContainer recordVOLazyContainer = new RecordVOLazyContainer(tabSource.getDataProvider());
		AdminUnitsWithContainersCountContainer adaptedContainer = new AdminUnitsWithContainersCountContainer(
				recordVOLazyContainer, getCollection(), getSessionContext().getCurrentUser().getId(), tabSource.getTabName());

		ButtonsContainer buttonsContainer = new ButtonsContainer(adaptedContainer, "buttons");
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = tabSource.getDataProvider().getRecordVO(index);
						presenter.displayButtonClicked(tabSource.getTabName(), entity);
					}
				};
			}
		});

		RecordVOTable table = new RecordVOTable($("ContainersByAdministrativeUnitsView.tableTitle"), buttonsContainer);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		table.setColumnHeader(AdminUnitsWithContainersCountContainer.CONTAINERS_COUNT, $("containersCount"));
		table.setColumnHeader(AdminUnitsWithContainersCountContainer.SUB_ADMINISTRATIVE_UNITS_COUNT,
				$("subAdministrativeUnitsCount"));
		//		table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
		table.setPageLength(table.getItemIds().size());

		return table;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		return tabSheet;
	}

	@Override
	public List<ContainersViewTab> getTabs() {
		return tabs;
	}

	@Override
	public void setTabs(List<ContainersViewTab> tabs) {
		this.tabs = tabs;
		tabSheet.removeAllComponents();
		for (final ContainersViewTab tab : tabs) {
			String tabName = tab.getTabName();
			String tabCaption = $("ContainersByAdministrativeUnitsView.tab." + tabName);
			tabSheet.addTab(new PlaceHolder(), tabCaption);
		}
		if (tabs.isEmpty()) {
			tabSheet.setVisible(false);
		}
	}

	@Override
	public void selectTab(ContainersViewTab tab) {
		int indexOfTab = tabs.indexOf(tab);
		tabSheet.setSelectedTab(indexOfTab);
	}

	private static class PlaceHolder extends CustomComponent {

		@Override
		public void setCompositionRoot(Component compositionRoot) {
			super.setCompositionRoot(compositionRoot);
		}

	}

	@Override
	protected String getTitle() {
		return $("ContainersByAdministrativeUnitsView.viewTitle");
	}
}
