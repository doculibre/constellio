package com.constellio.app.modules.rm.ui.pages.containers;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.AdminUnitsWithContainersCountContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class ContainersInAdministrativeUnitViewImpl extends BaseViewImpl implements ContainersInAdministrativeUnitView {

	private ContainersInAdministrativeUnitPresenter presenter;

	public ContainersInAdministrativeUnitViewImpl() {
		presenter = new ContainersInAdministrativeUnitPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		layout.addComponent(new RecordDisplay(presenter.getAdministrativeUnit()));

		layout.addComponent(buildChildrenAdminUnitsTable());

		layout.addComponent(buildContainersTable());

		return layout;
	}

	private Component buildChildrenAdminUnitsTable() {
		final RecordVOLazyContainer recordVOLazyContainer = new RecordVOLazyContainer(presenter.getChildrenAdminUnitsDataProvider());

		AdminUnitsWithContainersCountContainer adaptedContainer = new AdminUnitsWithContainersCountContainer(
				recordVOLazyContainer, getCollection(), getSessionContext().getCurrentUser().getId(), presenter.tabName);

		ButtonsContainer buttonsContainer = new ButtonsContainer(adaptedContainer, "buttons");
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = recordVOLazyContainer.getRecordVO(index);
						presenter.displayAdminUnitButtonClicked(presenter.tabName, entity);
					}
				};
			}
		});

		RecordVOTable table = new RecordVOTable($("ContainersInAdministrativeUnitView.childrenAdminUnitsTableTitle"),
				buttonsContainer);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		table.setColumnHeader(AdminUnitsWithContainersCountContainer.CONTAINERS_COUNT, $("containersCount"));
		table.setColumnHeader(AdminUnitsWithContainersCountContainer.SUB_ADMINISTRATIVE_UNITS_COUNT,
				$("subAdministrativeUnitsCount"));
		//		table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
		table.setPageLength(table.getItemIds().size());

		return table;
	}

	private Component buildContainersTable() {
		final RecordVOLazyContainer recordVOLazyContainer = new RecordVOLazyContainer(presenter.getContainersDataProvider());
		ButtonsContainer buttonsContainer = new ButtonsContainer(recordVOLazyContainer, "buttons");
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = recordVOLazyContainer.getRecordVO(index);
						presenter.displayContainerButtonClicked(entity);
					}
				};
			}
		});

		RecordVOTable table = new RecordVOTable($("ContainersInFilingSpaceView.containersTableTitle"), buttonsContainer);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		//		table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
		table.setPageLength(table.getItemIds().size());
		table.setVisible(table.getItemIds().size() > 0);

		return table;
	}

	@Override
	protected String getTitle() {
		return $("ContainersInAdministrativeUnitView.viewTitle");
	}
}
