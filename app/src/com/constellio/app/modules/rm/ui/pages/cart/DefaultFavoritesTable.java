package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.util.SchemaCaptionUtils;
import com.constellio.model.entities.records.Record;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;

import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class DefaultFavoritesTable extends BaseTable {
	public DefaultFavoritesTable(String tableId, final Container container) {
		super(tableId);

		setContainerDataSource(container);
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

	public static class CartItem {
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

		public CartItem(Cart record, String title, Record createdBy, Record ModifiedBy, Record owner) {
			this.cart = record;
			this.title = title;
			this.displayButton = null;
			this.modifiedOn = cart.getModifiedOn().toString("yyyy-MM-dd HH:mm:ss");
			this.createdOn = cart.getCreatedOn().toString("yyyy-MM-dd HH:mm:ss");
			this.createdBy = SchemaCaptionUtils.getCaptionForRecord(createdBy, Locale.FRENCH);
			this.modifiedBy = SchemaCaptionUtils.getCaptionForRecord(ModifiedBy, Locale.FRENCH);
			this.sharedWith = separatedByLine(cart.getSharedWithUsers());
			this.owner = SchemaCaptionUtils.getCaptionForRecord(owner, Locale.FRENCH);
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

	public static class FavoritesContainer extends BeanItemContainer {
		public FavoritesContainer(Class type, List<CartItem> items) throws IllegalArgumentException {
			super(type, items);
		}

		public Cart getCart(CartItem item) {
			return item.getCart();
		}
	}

	private static String separatedByLine(List<String> users) {
		StringBuffer stringBuffer = new StringBuffer();
		for (String userId : users) {
			stringBuffer.append(SchemaCaptionUtils.getCaptionForRecordId(userId));
			stringBuffer.append("\n");
		}
		return stringBuffer.toString();
	}
}
