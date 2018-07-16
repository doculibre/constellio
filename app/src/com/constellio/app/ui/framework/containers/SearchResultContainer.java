package com.constellio.app.ui.framework.containers;

import java.util.Arrays;
import java.util.Collection;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;

public class SearchResultContainer extends ContainerAdapter<SearchResultVOLazyContainer> {
	
	public final static String THUMBNAIL_PROPERTY = "thumbnail";
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
		return Arrays.asList(THUMBNAIL_PROPERTY, SEARCH_RESULT_PROPERTY);
	}

	@Override
	protected Property<?> getOwnContainerProperty(Object itemId, Object propertyId) {
		Property<?> result;
		if (THUMBNAIL_PROPERTY.equals(propertyId)) {
			result = newThumbnailProperty(itemId);
		} else if (SEARCH_RESULT_PROPERTY.equals(propertyId)) {
			result = newSearchResultProperty(itemId);
		} else {
			result = null;
		}
		return result;
	}

	@Override
	protected Class<?> getOwnType(Object propertyId) {
		Class<?> result;
		if (THUMBNAIL_PROPERTY.equals(propertyId)) {
			result = Image.class;
		} else if (SEARCH_RESULT_PROPERTY.equals(propertyId)) {
			result = Component.class;
		} else {
			result = null;
		}
		return result;
	}
	
	private Property<Image> newThumbnailProperty(final Object itemId) {
		return new AbstractProperty<Image>() {
			@Override
			public Image getValue() {
				return new Image(null, new ThemeResource("images/commun/chargement.gif"));
			}

			@Override
			public void setValue(Image newValue) throws ReadOnlyException {
				throw new ReadOnlyException();
			}

			@Override
			public Class<? extends Image> getType() {
				return Image.class;
			}
		};
	}

	private Property<Component> newSearchResultProperty(final Object itemId) {
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
			public Class<Component> getType() {
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
