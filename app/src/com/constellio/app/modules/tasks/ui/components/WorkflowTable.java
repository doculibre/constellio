package com.constellio.app.modules.tasks.ui.components;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.vaadin.data.Container;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;

public class WorkflowTable extends RecordVOTable {
	public static final String PREFIX = "images/icons/task/";
	public static final Resource CANCEL_ICON = new ThemeResource(PREFIX + "task_complete.png");

	private final WorkflowPresenter presenter;

	public WorkflowTable(RecordVODataProvider provider, WorkflowPresenter presenter) {
		super($("WorkflowTable.caption", provider.size()));
		this.presenter = presenter;

		final RecordVOLazyContainer recordVOLazyContainer = new RecordVOLazyContainer(provider);
		setContainerDataSource(addButtons(recordVOLazyContainer));
		setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 90);
		setPageLength(Math.min(15, provider.size()));
		setWidth("100%");
	}

	private Container addButtons(final RecordVOLazyContainer records) {
		ButtonsContainer<RecordVOLazyContainer> container = new ButtonsContainer<>(records);
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.displayWorkflowInstanceRequested(records.getRecordVO((int) itemId));
					}
				};
			}
		});
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new IconButton(CANCEL_ICON, $("WorkflowTable.cancel")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.cancelWorkflowInstanceRequested(records.getRecordVO((int) itemId));
					}
				};
			}
		});
		return container;
	}

	public interface WorkflowPresenter {
		void cancelWorkflowInstanceRequested(RecordVO recordVO);

		void displayWorkflowInstanceRequested(RecordVO recordVO);
	}
}
