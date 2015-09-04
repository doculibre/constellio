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
import java.util.List;

import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenu;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.services.factories.ModelLayerFactory;

public abstract class PageItem implements CodedItem, Serializable {
	public enum Type {RECORD_TABLE, RECORD_TREE}

	private final String code;
	private final Type type;

	protected PageItem(String code, Type type) {
		this.code = code;
		this.type = type;
	}

	@Override
	public String getCode() {
		return code;
	}

	public Type getType() {
		return type;
	}

	public static abstract class RecordTable extends PageItem {
		protected RecordTable(String code) {
			super(code, Type.RECORD_TABLE);
		}

		public abstract RecordVODataProvider getDataProvider(
				ModelLayerFactory modelLayerFactory, SessionContext sessionContext);
	}

	public static abstract class RecordTree extends PageItem {
		protected RecordTree(String code) {
			super(code, Type.RECORD_TREE);
		}

		public int getDefaultTab() {
			return 0;
		}

		public abstract List<RecordLazyTreeDataProvider> getDataProviders(
				ModelLayerFactory modelLayerFactory, SessionContext sessionContext);

		public abstract BaseContextMenu getContextMenu();
	}
}
