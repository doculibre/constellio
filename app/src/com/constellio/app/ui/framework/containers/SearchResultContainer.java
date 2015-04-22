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
package com.constellio.app.ui.framework.containers;

import java.util.Arrays;
import java.util.Collection;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.ui.Component;

public class SearchResultContainer extends ContainerAdapter<SearchResultVOLazyContainer> {
	public final static String SEARCH_RESULT_PROPERTY = "searchResult";

	private RecordDisplayFactory displayFactory;

	public SearchResultContainer(SearchResultVOLazyContainer adapted, RecordDisplayFactory displayFactory) {
		super(adapted);
		this.displayFactory = displayFactory;
	}

	@Override
	protected Collection<?> getOwnContainerPropertyIds() {
		return Arrays.asList(SEARCH_RESULT_PROPERTY);
	}

	@Override
	protected Property getOwnContainerProperty(Object itemId, Object propertyId) {
		return SEARCH_RESULT_PROPERTY.equals(propertyId) ? newSearchResultProperty(itemId) : null;
	}

	@Override
	protected Class<?> getOwnType(Object propertyId) {
		return SEARCH_RESULT_PROPERTY.equals(propertyId) ? Component.class : null;
	}

	private Property newSearchResultProperty(final Object itemId) {
		return new AbstractProperty<Component>() {
			@Override
			public Component getValue() {
				SearchResultVO searchResultVO = getSearchResultVO((int) itemId);
				return displayFactory.build(searchResultVO);
			}

			@Override
			public void setValue(Component newValue)
					throws ReadOnlyException {
				throw new ReadOnlyException();
			}

			@Override
			public Class getType() {
				return Component.class;
			}
		};
	}

	public RecordVO getRecordVO(int itemId) {
		return adapted.getRecordVO(itemId);
	}

	public SearchResultVO getSearchResultVO(int itemId) {
		return adapted.getSearchResultVO(itemId);
	}
}
