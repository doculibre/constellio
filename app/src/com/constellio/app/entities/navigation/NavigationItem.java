/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.entities.navigation;

import java.io.Serializable;

import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.viewGroups.MenuViewGroup;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public interface NavigationItem extends CodedItem, Serializable, Comparable<NavigationItem> {
	String getCode();

	String getIcon();

	int getOrderValue();

	Class<? extends MenuViewGroup> getViewGroup();

	void activate(ConstellioNavigator navigateTo);

	ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory);

	abstract class BaseNavigationItem implements NavigationItem {

		@Override
		public int compareTo(NavigationItem o) {
			return new Integer(getOrderValue()).compareTo(o.getOrderValue());
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
		public int getOrderValue() {
			return 999;
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
		public Class<? extends MenuViewGroup> getViewGroup() {
			return item.getViewGroup();
		}

		@Override
		public void activate(ConstellioNavigator navigateTo) {
			item.activate(navigateTo);
		}
	}

	class Inactive extends Active {
		public Inactive(String code, String icon) {
			super(code, icon);
		}

		@Override
		public void activate(ConstellioNavigator navigateTo) {
			// Nothing to be done here
		}

		@Override
		public int getOrderValue() {
			return 999;
		}

		@Override
		public ComponentState getStateFor(User user, ModelLayerFactory modelLayerFactory) {
			return ComponentState.DISABLED;
		}
	}
}
