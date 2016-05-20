package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.*;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import static com.constellio.app.ui.i18n.i18n.$;

public class CartsListViewImpl  extends BaseViewImpl implements CartsListView{

	private final CartsListPresenter presenter;

	public CartsListViewImpl() {
		presenter = new CartsListPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("CartsListView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();

		TabSheet tabSheet = new TabSheet();

		tabSheet.addTab(buildOwnedCartsTab());
		tabSheet.addTab(buildSharedCartsTab());
		layout.addComponent(tabSheet);

		return layout;
	}

	private Layout buildOwnedCartsTab() {
		VerticalLayout tabLayout = new VerticalLayout();
		tabLayout.setCaption("OwnedCarts");
		Button addButton = new WindowButton($("add"),$("CartsListView.creatingCart")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();
				final BaseTextField titleField = new BaseTextField($("CartsListView.cartTitleField"));
				BaseButton saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.saveButtonClicked(titleField.getValue());
						getWindow().close();
					}
				};
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				layout.addComponent(titleField);
				layout.addComponent(saveButton);
				return layout;
			}
		};
		Table table = buildTable();
		tabLayout.addComponents(addButton, table);
		tabLayout.setExpandRatio(table, 1);
		tabLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		return tabLayout;
	}

	private Table buildTable() {
		RecordVOLazyContainer container = new RecordVOLazyContainer(presenter.getOwnedCartsDataProvider());

		final ButtonsContainer<RecordVOLazyContainer> buttonsContainer = new ButtonsContainer<>(container);
		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						RecordVO recordVO = buttonsContainer.getNestedContainer().getRecordVO((int) itemId);
						presenter.displayButtonClicked(recordVO);
					}
				};
			}
		});
		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
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

		RecordVOTable table = new RecordVOTable("", buttonsContainer);
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 90);
		table.setPageLength(Math.min(15, container.size()));
		table.setWidth("100%");
		return table;
	}

	private Layout buildSharedCartsTab() {
		VerticalLayout tabLayout = new VerticalLayout();
		tabLayout.setCaption("SharedCarts");
		RecordVOLazyContainer container = new RecordVOLazyContainer(presenter.getSharedCartsDataProvider());

		final ButtonsContainer<RecordVOLazyContainer> buttonsContainer = new ButtonsContainer<>(container);
		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						RecordVO recordVO = buttonsContainer.getNestedContainer().getRecordVO((int) itemId);
						presenter.displayButtonClicked(recordVO);
					}
				};
			}
		});
		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
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

		RecordVOTable table = new RecordVOTable("",buttonsContainer);
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 90);
		table.setPageLength(Math.min(15, container.size()));
		table.setWidth("100%");
		tabLayout.addComponent(table);
		tabLayout.setExpandRatio(table, 1);
		return tabLayout;
	}
}
