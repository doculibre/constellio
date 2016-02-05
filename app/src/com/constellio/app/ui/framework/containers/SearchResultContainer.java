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
