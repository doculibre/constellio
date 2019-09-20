package com.constellio.app.ui.framework.components.breadcrumb;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.services.extensions.ConstellioModulesManagerImpl;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.MainLayout;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.viewGroups.MenuViewGroup;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class TitleBreadcrumbTrail extends BaseBreadcrumbTrail {

	private BaseView view;

	private String viewTitle;

	@SuppressWarnings("unchecked")
	public TitleBreadcrumbTrail(final BaseView view, final String viewTitle) {
		this.view = view;
		this.viewTitle = viewTitle;

		String collectionCode = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		if (StringUtils.isNotBlank(collectionCode)) {
			addItem(new CollectionBreadcrumbItem(collectionCode));
		}

		Class<? extends MenuViewGroup> viewGroupClass = null;
		String viewGroupLabel = null;
		List<Class<?>> implementedInterfaces = ClassUtils.getAllInterfaces(view.getClass());
		for (Class<?> implementedInterface : implementedInterfaces) {
			if (!MenuViewGroup.class.equals(implementedInterface) && MenuViewGroup.class.isAssignableFrom(implementedInterface)) {
				String className = implementedInterface.getSimpleName();
				String key = "ViewGroup." + className;
				viewGroupLabel = $(key);
				if (key.equals(viewGroupLabel)) {
					viewGroupLabel = null;
				} else {
					viewGroupClass = (Class<? extends MenuViewGroup>) implementedInterface;
					break;
				}
			}
		}

		if (StringUtils.isNotBlank(viewGroupLabel)) {
			addItem(new ViewGroupBreadcrumbItem(viewGroupClass, viewGroupLabel));
		}

		List<? extends IntermediateBreadCrumbTailItem> intermediateBreadCrumbTailItems = getIntermediateItems();
		if (!intermediateBreadCrumbTailItems.isEmpty()) {
			for (IntermediateBreadCrumbTailItem item : intermediateBreadCrumbTailItems) {
				addItem(item);
			}
		}

		if (StringUtils.isNotBlank(viewTitle) && (StringUtils.isBlank(viewGroupLabel) || !viewGroupLabel.equals(viewTitle))) {
			addItem(newCurrentViewItem(viewTitle));
		}
	}
	
	protected CurrentViewItem newCurrentViewItem(String viewTitle) {
		return new CurrentViewItem(viewTitle);
	}

	public BaseView getView() {
		return view;
	}

	public String getViewTitle() {
		return viewTitle;
	}

	public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
		return new ArrayList<>();
	}

	@Override
	protected void itemClick(BreadcrumbItem item) {
		if (item instanceof ViewGroupBreadcrumbItem) {
			ViewGroupBreadcrumbItem viewGroupBreadcrumbItem = (ViewGroupBreadcrumbItem) item;
			NavigationItem navigationItem = viewGroupBreadcrumbItem.getNavigationItem();
			if (navigationItem != null) {
				navigationItem.activate(navigate());
			}
		} else if (item instanceof CollectionBreadcrumbItem) {
			navigate().to().home();
		} else if (item instanceof IntermediateBreadCrumbTailItem) {
			IntermediateBreadCrumbTailItem intermediateItem = (IntermediateBreadCrumbTailItem) item;
			intermediateItem.activate(navigate());
		}
	}

	public class CurrentViewItem implements BreadcrumbItem {

		private String viewTitle;
		private boolean enabled;

		CurrentViewItem(String viewTitle) {
			this.viewTitle = viewTitle;
		}

		@Override
		public String getLabel() {
			return viewTitle;
		}

		@Override
		public boolean isEnabled() {
			return enabled;
		}
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

	}

	public class ViewGroupBreadcrumbItem implements BreadcrumbItem {

		private Class<? extends MenuViewGroup> viewGroupClass;

		private String viewGroupLabel;

		public ViewGroupBreadcrumbItem(Class<? extends MenuViewGroup> viewGroupClass, String viewGroupLabel) {
			this.viewGroupClass = viewGroupClass;
			this.viewGroupLabel = viewGroupLabel;
		}

		@Override
		public String getLabel() {
			return viewGroupLabel;
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		private NavigationItem getNavigationItem() {
			NavigationItem viewGroupItem = null;
			List<NavigationItem> items = new ArrayList<>();
			SessionContext sessionContext = getSessionContext();
			String collection = sessionContext.getCurrentCollection();
			ConstellioModulesManagerImpl manager = (ConstellioModulesManagerImpl) getConstellioFactories().getAppLayerFactory().getModulesManager();
			NavigationConfig config = manager.getNavigationConfig(collection);
			items.addAll(config.getNavigation(MainLayout.MAIN_LAYOUT_NAVIGATION));
			for (NavigationItem item : items) {
				Class<? extends MenuViewGroup> itemViewGroupClass = item.getViewGroup();
				if (viewGroupClass.equals(itemViewGroupClass)) {
					viewGroupItem = item;
					break;
				}
			}
			return viewGroupItem;
		}

	}

}
