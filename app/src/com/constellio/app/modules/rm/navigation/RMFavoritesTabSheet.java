package com.constellio.app.modules.rm.navigation;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.cart.CartViewImpl;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.RMUser;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.tree.RecordLazyTreeTabSheet.PlaceHolder;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.vaadin.ui.TabSheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class RMFavoritesTabSheet extends TabSheet {

	private transient AppLayerFactory appLayerFactory;
	private transient SessionContext sessionContext;
	private transient RMSchemasRecordsServices rm;
	private transient User user;
	private transient List<String> favoriteIds;

	public RMFavoritesTabSheet(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		init(appLayerFactory, sessionContext);
		buildContent();
	}

	private void init(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		this.appLayerFactory = appLayerFactory;
		this.sessionContext = sessionContext;
		rm = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), appLayerFactory);
		user = new PresenterService(appLayerFactory.getModelLayerFactory()).getCurrentUser(sessionContext);
		favoriteIds = new ArrayList<>();
	}

	public void buildContent() {

		PresenterService presenterService = new PresenterService(ConstellioFactories.getInstance().getModelLayerFactory());
		User currentUser = presenterService.getCurrentUser(ConstellioUI.getCurrentSessionContext());

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), appLayerFactory);
		List<String> favoritesDisplayOrder = currentUser.getList(RMUser.FAVORITES_DISPLAY_ORDER);

		favoritesDisplayOrder.stream().forEach(favoriteId -> {
			boolean stillHasAccess = false;
			String caption = "";
			if (user.getId().equals(favoriteId)) {
				caption = $("CartView.defaultFavorites");
				stillHasAccess = user.has(RMPermissionsTo.USE_MY_CART).globally();
			} else {
				try {
					Cart cart = rm.getCart(favoriteId);
					caption = cart.getTitle();
					stillHasAccess = cart.getSharedWithUsers().contains(user.getId()) || cart.getOwner().equals(user.getId());
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					caption = "";
					stillHasAccess = false;
				}
			}

			if (stillHasAccess) {
				favoriteIds.add(favoriteId);
				PlaceHolder placeHolder = new PlaceHolder();
				addTab(placeHolder, caption);
			}
		});

		addSelectedTabChangeListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				selectTab(getTab(getSelectedTab()));
			}
		});
	}

	private void selectTab(Tab tab) {
		if (tab == null) {
			return;
		}

		int position = getTabPosition(tab);
		setSelectedTab(position);

		PlaceHolder tabComponent = (PlaceHolder) getSelectedTab();
		if (tabComponent.getComponentCount() == 0) {
			CartViewImpl view = new CartViewImpl(favoriteIds.get(position), true);
			view.enter(null);
			tabComponent.setCompositionRoot(view);
		}
	}

	@Override
	public Locale getLocale() {
		return sessionContext.getCurrentLocale();
	}

	@Override
	public void attach() {
		super.attach();
		selectTab(getTab(getSelectedTab()));
	}
}
