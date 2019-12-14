package com.constellio.app.modules.robots.ui.pages;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListRootRobotsViewImpl extends BaseViewImpl implements ListRootRobotsView {

	private final ListRootRobotsPresenter presenter;

	public ListRootRobotsViewImpl() {
		this.presenter = new ListRootRobotsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListRootRobotsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();

		Label legacyIdIndexDisabledWarning = new Label($("ListRootRobotsView.legacyIdIndexDisabledWarning"));
		legacyIdIndexDisabledWarning.addStyleName("system-state-component-important-message");
		legacyIdIndexDisabledWarning.setVisible(presenter.isLegacyIdIndexDisabledWarningVisible());

		Button addButton = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};
		Table table = buildTable();

		layout.addComponents(legacyIdIndexDisabledWarning, addButton, table);
		layout.setExpandRatio(table, 1);
		layout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);

		return layout;
	}

	private Table buildTable() {
		RecordVOLazyContainer container = new RecordVOLazyContainer(presenter.getRootRobotsDataProvider());

		final ButtonsContainer<RecordVOLazyContainer> buttonsContainer = new ButtonsContainer<>(container);
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						RecordVO recordVO = buttonsContainer.getNestedContainer().getRecordVO((int) itemId);
						presenter.displayButtonClicked(recordVO);
					}
				};
			}
		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				DeleteButton deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						RecordVO recordVO = buttonsContainer.getNestedContainer().getRecordVO((int) itemId);
						presenter.deleteButtonClicked(recordVO);
					}
				};
				return deleteButton;
			}
		});

		RecordVOTable table = new RecordVOTable($("ListRootRobotsView.tableCaption", container.size()), buttonsContainer);
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 90);
		table.setPageLength(Math.min(15, container.size()));
		table.setWidth("100%");
		return table;
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
}
