package com.constellio.app.entities.navigation;

import java.io.Serializable;

import com.constellio.app.ui.application.ConstellioUI.Navigation;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.viewGroups.MenuViewGroup;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public interface NavigationItem extends CodedItem, Serializable, Comparable<NavigationItem> {
	String getCode();

	String getIcon();

	int getOrderValue();

	Class<? extends MenuViewGroup> getViewGroup();

	void activate(Navigation navigate);

	ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory);

	abstract class BaseNavigationItem implements NavigationItem {
		@Override
		public int compareTo(NavigationItem o) {
			return new Integer(getOrderValue()).compareTo(o.getOrderValue());
		}

		@Override
		public int getOrderValue() {
			return 999;
		}
	}

	abstract class Active extends BaseNavigationItem implements NavigationItem {
		private final String code;
		private final String icon;
		private final Class<? extends MenuViewGroup> viewGroup;

		public Active(String code, String icon, Class<? extends MenuViewGroup> viewGroup) {
			this.code = code;
			this.icon = icon;
			this.viewGroup = viewGroup;
		}

		public Active(String code, Class<? extends MenuViewGroup> viewGroup) {
			this(code, null, viewGroup);
		}

		public Active(String code, String icon) {
			this(code, icon, null);
		}

		public Active(String code) {
			this(code, null, null);
		}

		@Override
		public String getCode() {
			return code;
		}

		public String getIcon() {
			return icon;
		}

		public Class<? extends MenuViewGroup> getViewGroup() {
			return viewGroup;
		}
	}

	abstract class Decorator extends BaseNavigationItem implements NavigationItem {
		protected final NavigationItem item;

		protected Decorator(NavigationItem item) {
			this.item = item;
		}

		@Override
		public String getCode() {
			return item.getCode();
		}

		@Override
		public String getIcon() {
			return item.getIcon();
		}

		@Override
		public int getOrderValue() {
			return item.getOrderValue();
		}

		@Override
		public Class<? extends MenuViewGroup> getViewGroup() {
			return item.getViewGroup();
		}

		@Override
		public void activate(Navigation navigate) {
			item.activate(navigate);
		}
	}

	class Inactive extends Active {
		public Inactive(String code, String icon) {
			super(code, icon);
		}

		@Override
		public void activate(Navigation navigate) {
			// Nothing to be done here
		}

		@Override
		public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
			return ComponentState.DISABLED;
		}
	}
}
