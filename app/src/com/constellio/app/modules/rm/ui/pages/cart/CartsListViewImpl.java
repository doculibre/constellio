package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.util.MessageUtils;
import com.vaadin.data.util.BeanItemContainer;
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
		tabLayout.addComponents(addButton, table);
		tabLayout.setExpandRatio(table, 1);
		tabLayout.setSpacing(true);
		tabLayout.setComponentAlignment(addButton, Alignment.BOTTOM_RIGHT);
		return tabLayout;
	}

	private Table buildTable() {
		List<CartItem> cartItems = new ArrayList<>();
		cartItems.add(new CartItem($("CartView.defaultFavorites")));
		for (Cart cart : presenter.getOwnedCarts()) {
			cartItems.add(new CartItem(cart, cart.getTitle()));
		}
		return new FavoritesTable("favoritesTable", cartItems);
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

	private class FavoritesTable extends BaseTable {
		public FavoritesTable(String tableId, List<CartItem> carts) {
			super(tableId);
			FavoritesContainer container = new FavoritesContainer(CartItem.class, carts);

			final ButtonsContainer<FavoritesContainer> buttonsContainer = new ButtonsContainer(container, CartItem.DISPLAY_BUTTON);
			buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object item, ButtonsContainer<?> container) {
					return new DisplayButton() {
						@Override
						protected void buttonClick(ClickEvent event) {
							Cart cart = buttonsContainer.getNestedContainer().getCart((CartItem) item);
							if (cart != null) {
								presenter.displayButtonClicked(cart);
							} else {
								presenter.displayDefaultFavorites();
							}
						}
					};
				}
			});
			buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object item, ButtonsContainer<?> container) {
					DeleteButton deleteButton = new DeleteButton() {
						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							Cart cart = buttonsContainer.getNestedContainer().getCart((CartItem) item);
							presenter.deleteButtonClicked(cart);
						}
					};
					deleteButton.setVisible(buttonsContainer.getNestedContainer().getCart((CartItem) item) != null);
					return deleteButton;
				}
			});

			setContainerDataSource(buttonsContainer);
			setWidth("100%");
			setVisibleColumns(CartItem.TITLE, CartItem.MODIFIED_ON, CartItem.CREATED_BY, CartItem.CREATED_ON, CartItem.MODIFIED_BY, CartItem.SHARED_WITH, CartItem.OWNER, CartItem.DISPLAY_BUTTON);
			setColumnHeader(CartItem.TITLE, $(CartItem.TITLE));
			setColumnHeader(CartItem.CREATED_BY, $("CartsListView." + CartItem.CREATED_BY));
			setColumnHeader(CartItem.CREATED_ON, $("CartsListView." + CartItem.CREATED_ON));
			setColumnHeader(CartItem.MODIFIED_ON, $("CartsListView." + CartItem.MODIFIED_ON));
			setColumnHeader(CartItem.MODIFIED_BY, $("CartsListView." + CartItem.MODIFIED_BY));
			setColumnHeader(CartItem.SHARED_WITH, $("CartsListView." + CartItem.SHARED_WITH));
			setColumnHeader(CartItem.OWNER, $("CartsListView." + CartItem.OWNER));
			setColumnHeader(CartItem.DISPLAY_BUTTON, "");
			setColumnExpandRatio(CartItem.TITLE, 1);
		}
	}

	public class FavoritesContainer extends BeanItemContainer {
		public FavoritesContainer(Class type, List<CartItem> items) throws IllegalArgumentException {
			super(type, items);
		}

		public Cart getCart(CartItem item) {
			return item.getCart();
		}
	}

	public class CartItem {
		public static final String TITLE = "title";
		public static final String MODIFIED_ON = "modifiedOn";
		public static final String CREATED_BY = "createdBy";
		public static final String CREATED_ON = "createdOn";
		public static final String MODIFIED_BY = "modifiedBy";
		public static final String SHARED_WITH = "sharedWith";
		public static final String OWNER = "owner";
		public static final String DISPLAY_BUTTON = "displayButton";

		private final Cart cart;
		private final String title;
		private final String modifiedOn;
		private final String createdBy;
		private final String modifiedBy;
		private final String createdOn;
		private final String sharedWith;
		private final String owner;
		private final Object displayButton;

		public CartItem(Cart record, String title) {
			this.cart = record;
			this.title = title;
			this.displayButton = null;
			this.modifiedOn = cart.getModifiedOn().toString("yyyy-MM-dd HH:mm:ss");
			this.createdOn = cart.getCreatedOn().toString("yyyy-MM-dd HH:mm:ss");
			this.createdBy = presenter.getUsernameById(cart.getCreatedBy());
			this.modifiedBy = presenter.getUsernameById(cart.getModifiedBy());
			this.sharedWith = separatedByLine(cart.getSharedWithUsers());
			this.owner = presenter.getUsernameById(cart.getOwner());
		}

		public CartItem(String title) {
			this.title = title;
			this.cart = null;
			this.displayButton = null;
			this.modifiedOn = null;
			this.createdOn = null;
			this.createdBy = null;
			this.modifiedBy = null;
			this.sharedWith = null;
			this.owner = null;
		}

		public Cart getCart() {
			return cart;
		}

		public String getTitle() {
			return title;
		}

		public Object getDisplayButton() {
			return displayButton;
		}

		public String getModifiedOn() {
			return modifiedOn;
		}

		public String getCreatedOn() {
			return createdOn;
		}

		public String getCreatedBy() {
			return createdBy;
		}

		public String getModifiedBy() {
			return modifiedBy;
		}

		public String getSharedWith() {
			return sharedWith;
		}

		public String getOwner() {
			return owner;
		}
	}

	private String separatedByLine(List<String> users) {
		StringBuffer stringBuffer = new StringBuffer();
		for (String username : users) {
			stringBuffer.append(username);
			stringBuffer.append("\n");
		}
		return stringBuffer.toString();
	}
}
