package com.constellio.app.entities.navigation;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.MenuViewGroup;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.server.FontAwesome;

import java.io.Serializable;

public interface NavigationItem extends CodedItem, Serializable, Comparable<NavigationItem> {

	FontAwesome getFontAwesome();

	void setFontAwesome(FontAwesome fontAwesome);

	String getCode();

	void setCode(String code);

	String getIcon();

	void setIcon(String icon);

	int getOrderValue();

	String getBadge(User user, AppLayerFactory appLayerFactory);

	Class<? extends MenuViewGroup> getViewGroup();

	void activate(Navigation navigate);

	ComponentState getStateFor(User user, AppLayerFactory appLayerFactory);

	void viewChanged(BaseView oldView, BaseView newView);

	abstract class BaseNavigationItem implements NavigationItem {

		private FontAwesome fontAwesome;

		private String code;

		private String icon;

		private Class<? extends MenuViewGroup> viewGroup;

		public BaseNavigationItem() {
		}

		public BaseNavigationItem(String code, String icon, FontAwesome fontAwesome,
								  Class<? extends MenuViewGroup> viewGroup) {
			this.code = code;
			this.icon = icon;
			this.fontAwesome = fontAwesome;
			this.viewGroup = viewGroup;
		}

		@Override
		public FontAwesome getFontAwesome() {
			return fontAwesome;
		}

		@Override
		public void setFontAwesome(FontAwesome fontAwesome) {
			this.fontAwesome = fontAwesome;
		}

		@Override
		public String getCode() {
			return code;
		}

		@Override
		public void setCode(String code) {
			this.code = code;
		}

		@Override
		public String getIcon() {
			return icon;
		}

		@Override
		public void setIcon(String icon) {
			this.icon = icon;
		}

		@Override
		public Class<? extends MenuViewGroup> getViewGroup() {
			return viewGroup;
		}

		@Override
		public int compareTo(NavigationItem o) {
			return new Integer(getOrderValue()).compareTo(o.getOrderValue());
		}

		@Override
		public int getOrderValue() {
			return 999;
		}

		@Override
		public String getBadge(User user, AppLayerFactory appLayerFactory) {
			return null;
		}

		@Override
		public void viewChanged(BaseView oldView, BaseView newView) {
		}

		public String urlNeedToEndWith() {
			return null;
		}
	}

	abstract class Active extends BaseNavigationItem implements NavigationItem {

		public Active(String code, String icon, FontAwesome fontAwesome, Class<? extends MenuViewGroup> viewGroup) {
			super(code, icon, fontAwesome, viewGroup);
		}

		public Active(String code, FontAwesome fontAwesome, Class<? extends MenuViewGroup> viewGroup) {
			this(code, null, fontAwesome, viewGroup);
		}

		public Active(String code, Class<? extends MenuViewGroup> viewGroup) {
			this(code, null, null, viewGroup);
		}

		public Active(String code, String icon) {
			this(code, icon, null, null);
		}

		public Active(String code, FontAwesome fontAwesome) {
			this(code, null, fontAwesome, null);
		}

		public Active(String code) {
			this(code, null, null, null);
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
		public FontAwesome getFontAwesome() {
			return item.getFontAwesome();
		}

		@Override
		public void setFontAwesome(FontAwesome fontAwesome) {
			item.setFontAwesome(fontAwesome);
		}

		@Override
		public void setCode(String code) {
			item.setCode(code);
		}

		@Override
		public void setIcon(String icon) {
			item.setIcon(icon);
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

		@Override
		public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
			return item.getStateFor(user, appLayerFactory);
		}

		@Override
		public int compareTo(NavigationItem o) {
			return item.compareTo(o);
		}

		@Override
		public String getBadge(User user, AppLayerFactory appLayerFactory) {
			return item.getBadge(user, appLayerFactory);
		}

		@Override
		public void viewChanged(BaseView oldView, BaseView newView) {
			item.viewChanged(oldView, newView);
		}
	}

	class Inactive extends Active {

		public Inactive(String code, String icon) {
			super(code, icon);
		}

		public Inactive(String code, FontAwesome fontAwesome) {
			super(code, fontAwesome);
		}

		@Override
		public void activate(Navigation navigate) {
			// Nothing to be done here
		}

		@Override
		public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
			return ComponentState.DISABLED;
		}
	}
}
