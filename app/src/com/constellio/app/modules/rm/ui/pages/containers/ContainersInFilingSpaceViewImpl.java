package com.constellio.app.modules.rm.ui.pages.containers;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class ContainersInFilingSpaceViewImpl extends BaseViewImpl implements ContainersInFilingSpaceView {

	private ContainersInFilingSpacePresenter presenter;

	public ContainersInFilingSpaceViewImpl() {
		presenter = new ContainersInFilingSpacePresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
		VerticalLayout layout = new VerticalLayout();

		layout.addComponent(new RecordDisplay(presenter.getFilingSpace()));

		layout.addComponent(buildContainersTable());

		return layout;
	}

	private Component buildContainersTable() {
		RecordVOLazyContainer recordVOLazyContainer = new RecordVOLazyContainer(presenter.getContainersDataProvider());
		ButtonsContainer buttonsContainer = new ButtonsContainer(recordVOLazyContainer, "buttons");
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = presenter.getContainersDataProvider().getRecordVO(index);
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

		return table;
	}

	@Override
	protected String getTitle() {
		return $("ContainersInFilingSpaceView.viewTitle");
	}
}
