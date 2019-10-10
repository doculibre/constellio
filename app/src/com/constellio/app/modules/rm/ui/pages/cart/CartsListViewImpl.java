package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.pages.cart.DefaultFavoritesTable.CartItem;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.util.MessageUtils;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
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

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class CartsListViewImpl extends BaseViewImpl implements CartsListView {

	public static final String TITLE = "CartsListView.viewTitle";
	private final CartsListPresenter presenter;

	public CartsListViewImpl() {
		presenter = new CartsListPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $(TITLE);
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
		addButton.addStyleName(ValoTheme.BUTTON_LINK);
		addButton.addStyleName("marginright");

		Table table = buildDefaultFavoritesTable();
		tabLayout.addComponents(addButton, table);
		tabLayout.setExpandRatio(table, 1);
		tabLayout.setSpacing(true);
		tabLayout.setComponentAlignment(addButton, Alignment.BOTTOM_RIGHT);
		return tabLayout;
	}

	private DefaultFavoritesTable buildDefaultFavoritesTable() {
		List<DefaultFavoritesTable.CartItem> cartItems = new ArrayList<>();
		if (presenter.isMyCartVisible()) {
			cartItems.add(new DefaultFavoritesTable.CartItem($("CartView.defaultFavorites")));
		}

		for (Cart cart : presenter.getOwnedCarts()) {
			cartItems.add(new DefaultFavoritesTable.CartItem(cart, cart.getTitle()));
		}
		DefaultFavoritesTable.FavoritesContainer container = new DefaultFavoritesTable.FavoritesContainer(DefaultFavoritesTable.CartItem.class, cartItems);

		final ButtonsContainer<DefaultFavoritesTable.FavoritesContainer> buttonsContainer = new ButtonsContainer(container, DefaultFavoritesTable.CartItem.DISPLAY_BUTTON);
		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object item, ButtonsContainer<?> container) {
				final Cart cart = buttonsContainer.getNestedContainer().getCart((DefaultFavoritesTable.CartItem) item);
				final RenameDialog renameDialog = new RenameDialog(EditButton.ICON_RESOURCE, $("CartsListView.reNameCartGroup"),
						$("CartsListView.reNameCartGroup"), true) {
					@Override
					public void save(String string) {
						if (presenter.renameFavoritesGroup(cart, string)) {
							getWindow().close();
							navigate().to(RMViews.class).listCarts();
						}
					}
				};
				boolean isVisibleAndEnabled = ((CartItem) item).getCart() != null;
				if (isVisibleAndEnabled) {
					renameDialog.setOriginalValue(cart.getTitle());
				}

				renameDialog.setEnabled(isVisibleAndEnabled);
				renameDialog.setVisible(isVisibleAndEnabled);

				return renameDialog;
			}
		});

		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object item, ButtonsContainer<?> container) {
				DeleteButton deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						Cart cart = buttonsContainer.getNestedContainer().getCart((DefaultFavoritesTable.CartItem) item);
						presenter.deleteButtonClicked(cart);
					}
				};
				deleteButton.setVisible(buttonsContainer.getNestedContainer().getCart((DefaultFavoritesTable.CartItem) item) != null);
				return deleteButton;
			}
		});

		DefaultFavoritesTable table = new DefaultFavoritesTable("favoritesTable", buttonsContainer, presenter.getSchema());
		table.addItemClickListener(new ItemClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void itemClick(ItemClickEvent event) {
				BeanItem<DefaultFavoritesTable.CartItem> beanItem = (BeanItem<DefaultFavoritesTable.CartItem>) event.getItem();
				Cart cart = buttonsContainer.getNestedContainer().getCart(beanItem.getBean());
				cartClickedInTable(cart);
			}
		});
		table.addStyleName(RecordVOTable.CLICKABLE_ROW_STYLE_NAME);
		return table;
	}

	private void cartClickedInTable(Cart cart) {
		if (cart != null) {
			presenter.cartClickedInTable(cart);
		} else {
			presenter.displayDefaultFavorites();
		}
	}

	private Layout buildSharedCartsTab() {
		VerticalLayout tabLayout = new VerticalLayout();
		tabLayout.setCaption($("CartView.sharedCarts"));
		RecordVOLazyContainer container = new RecordVOLazyContainer(presenter.getSharedCartsDataProvider());

		final ButtonsContainer<RecordVOLazyContainer> buttonsContainer = new ButtonsContainer<>(container);

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

		RecordVOTable table = new RecordVOTable("", buttonsContainer) {
		};

		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				presenter.cartClickedInTable(container.getRecordVO(event.getItemId()));
			}
		});

		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 90);
		table.setPageLength(Math.min(15, container.size()));
		table.setWidth("100%");
		tabLayout.addComponent(table);
		tabLayout.setExpandRatio(table, 1);
		return tabLayout;
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

}
