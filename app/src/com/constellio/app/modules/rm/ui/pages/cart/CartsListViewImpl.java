package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.util.MessageUtils;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import static com.constellio.app.ui.i18n.i18n.$;

public class CartsListViewImpl extends BaseViewImpl implements CartsListView {

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
		tabLayout.setCaption($("CartView.ownedCarts"));
		Button addButton = new WindowButton($("add"), $("CartsListView.creatingCart")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();
				final BaseTextField titleField = new BaseTextField($("CartsListView.cartTitleField"));
				titleField.setRequired(true);
				titleField.focus();
				new OnEnterKeyHandler() {
					@Override
					public void onEnterKeyPressed() {
						presenter.saveButtonClicked(titleField.getValue());
						getWindow().close();
					}
				}.installOn(titleField);

				BaseButton saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						try {
							presenter.saveButtonClicked(titleField.getValue());
							getWindow().close();
						} catch (Exception e) {
							showErrorMessage(MessageUtils.toMessage(e));
						}
					}
				};
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				layout.addComponent(titleField);
				layout.addComponent(saveButton);
				layout.setSpacing(true);
				return layout;
			}
		};
		Table table = buildTable();
		tabLayout.addComponents(addButton, buildDefaultFavorites(), table);
		tabLayout.setExpandRatio(table, 1);
		tabLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		return tabLayout;
	}

	private Table buildTable() {
		RecordVOLazyContainer container = new RecordVOLazyContainer(presenter.getOwnedCartsDataProvider());
		final ButtonsContainer<RecordVOLazyContainer> buttonsContainer = new ButtonsContainer<>(container);
		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
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
		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
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

		RecordVOTable table = new RecordVOTable("", buttonsContainer);
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 90);
		table.setPageLength(Math.min(15, container.size()));
		table.setWidth("100%");
		return table;
	}

	private Table buildDefaultFavorites() {
		Component displayButton = new DisplayButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.displayDefaultFavorites();
			}
		};
		Table table = new Table();
		table.addContainerProperty("Titre", String.class, null);
		table.addContainerProperty("", DisplayButton.class, null);
		table.addItem(new Object[]{"Favoris par défaut", displayButton}, 0);
		table.setWidth("100%");
		return table;
	}

	private Layout buildSharedCartsTab() {
		VerticalLayout tabLayout = new VerticalLayout();
		tabLayout.setCaption($("CartView.sharedCarts"));
		RecordVOLazyContainer container = new RecordVOLazyContainer(presenter.getSharedCartsDataProvider());

		final ButtonsContainer<RecordVOLazyContainer> buttonsContainer = new ButtonsContainer<>(container);
		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
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
		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
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

		RecordVOTable table = new RecordVOTable("", buttonsContainer);
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 90);
		table.setPageLength(Math.min(15, container.size()));
		table.setWidth("100%");
		tabLayout.addComponent(table);
		tabLayout.setExpandRatio(table, 1);
		return tabLayout;
	}
}
