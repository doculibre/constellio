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
package com.constellio.app.ui.framework.components.tree;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.vaadin.server.Resource;

public class RecordLazyTree extends LazyTree<String> {

	private RecordIdToCaptionConverter itemsConverter;

	public RecordLazyTree(String taxonomyCode, int bufferSize) {
		super(new RecordLazyTreeDataProvider(taxonomyCode), bufferSize);
		init();
	}

	public RecordLazyTree(String taxonomyCode) {
		super(new RecordLazyTreeDataProvider(taxonomyCode));
		init();
	}

	public RecordLazyTree(LazyTreeDataProvider<String> dataProvider, int bufferSize) {
		super(dataProvider, bufferSize);
		init();
	}

	public RecordLazyTree(LazyTreeDataProvider<String> dataProvider) {
		super(dataProvider);
		init();
	}

	private void init() {
		itemsConverter = new RecordIdToCaptionConverter();
	}

	@Override
	public String getItemCaption(String id) {
		return getDataProvider().getCaption(id);
	}

	@Override
	public Resource getItemIcon(Object itemId) {
		boolean expanded = isExpanded(itemId);
		return itemsConverter.getIcon((String) itemId, expanded);
	}

	@Override
	public Class<String> getType() {
		return String.class;
	}

}
