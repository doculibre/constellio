package com.constellio.app.modules.tasks.ui.pages.workflow;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class ListWorkflowsViewImpl extends BaseViewImpl implements ListWorkflowsView {
	private final ListWorkflowsPresenter presenter;

	public ListWorkflowsViewImpl() {
		this.presenter = new ListWorkflowsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListWorkflowsView.viewTitle");
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
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);
		buttons.add(new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		});
		return buttons;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		Container workflows = buildContainer(presenter.getWorkflows());
		Table table = new RecordVOTable($("ListWorkflowsView.workflows", workflows.size()), workflows);
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 130);
		table.setPageLength(Math.min(15, workflows.size()));
		table.setWidth("100%");

		VerticalLayout layout = new VerticalLayout(table);
		layout.setSizeFull();

		return layout;
	}

	private Container buildContainer(RecordVODataProvider provider) {
		final RecordVOLazyContainer records = new RecordVOLazyContainer(provider);
		ButtonsContainer<RecordVOLazyContainer> container = new ButtonsContainer<>(records);
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						int index = (int) itemId;
						presenter.displayButtonClicked(records.getRecordVO(index));
					}
				};
			}
		});
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						int index = (int) itemId;
						presenter.editButtonClicked(records.getRecordVO(index));
					}
				};
			}
		});
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						int index = (int) itemId;
						presenter.deleteButtonClicked(records.getRecordVO(index));
					}
				};
			}
		});
		return container;
	}
}
