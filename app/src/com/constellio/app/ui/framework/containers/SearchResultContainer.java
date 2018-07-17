package com.constellio.app.ui.framework.containers;

import java.util.Arrays;
import java.util.Collection;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

public class SearchResultContainer extends ContainerAdapter<SearchResultVOLazyContainer> {
	public final static String SEARCH_RESULT_PROPERTY = "searchResult";

	private RecordDisplayFactory displayFactory;
	String query;

	public SearchResultContainer(SearchResultVOLazyContainer adapted, RecordDisplayFactory displayFactory, String query) {
		super(adapted);
		this.displayFactory = displayFactory;
		this.query = query;
	}

	public SearchResultVODataProvider getDataProvider() {
		return adapted.getDataProvider();
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
				ClickListener clickListener = getClickListener(searchResultVO);
				ClickListener elevationClickListener = getElevationClickListener(searchResultVO);
				ClickListener exclusionClickListener = getExclusionClickListener(searchResultVO);
				return displayFactory.build(searchResultVO, query, clickListener, elevationClickListener, exclusionClickListener);
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
	
	protected ClickListener getClickListener(SearchResultVO searchResultVO) {
		return null;
	}
	
	protected ClickListener getElevationClickListener(SearchResultVO searchResultVO) {
		return null;
	}
	
	protected ClickListener getExclusionClickListener(SearchResultVO searchResultVO) {
		return null;
	}

	public RecordVO getRecordVO(int itemId) {
		return adapted.getRecordVO(itemId);
	}

	public SearchResultVO getSearchResultVO(int itemId) {
		return adapted.getSearchResultVO(itemId);
	}
}
