package com.constellio.app.ui.framework.containers;

import java.util.Arrays;
import java.util.Collection;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.data.utils.dev.Toggle;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;

import java.util.Arrays;
import java.util.Collection;

public class SearchResultContainer extends ContainerAdapter<SearchResultVOLazyContainer> {

	public final static String THUMBNAIL_PROPERTY = "thumbnail";

	public final static String SEARCH_RESULT_PROPERTY = "searchResult";
	public final static int THUMBNAIL_WIDTH = 90;

	private RecordDisplayFactory displayFactory;
	String query;

	public SearchResultContainer(SearchResultVOLazyContainer adapted, RecordDisplayFactory displayFactory,
								 String query) {
		super(adapted, true);
		this.displayFactory = displayFactory;
		this.query = query;
	}

	public SearchResultVODataProvider getDataProvider() {
		return adapted.getDataProvider();
	}

	@Override
	protected Collection<?> getOwnContainerPropertyIds() {
		if (Toggle.SEARCH_RESULTS_VIEWER.isEnabled()) {
			return Arrays.asList(THUMBNAIL_PROPERTY, SEARCH_RESULT_PROPERTY);
		} else {
			return Arrays.asList(SEARCH_RESULT_PROPERTY);
		}
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
				Image image = new Image(null);
				Integer index = (Integer) itemId;
				RecordVO recordVO = getRecordVO(index);
				String schemaTypeCode = recordVO.getSchema().getTypeCode();
				boolean thumbnail;
				if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
					final ContentVersionVO contentVersionVO = recordVO.get(Document.CONTENT);
					if (contentVersionVO != null) {
						String filename = contentVersionVO.getFileName();
						String recordId = recordVO.getId();
						String metadataCode = recordVO.getMetadata(Document.CONTENT).getLocalCode();
						String version = contentVersionVO.getVersion();

						if (ConstellioResourceHandler.hasContentThumbnail(recordId, metadataCode, version)) {
							thumbnail = true;
							Resource thumnailResource = ConstellioResourceHandler.createThumbnailResource(recordId, metadataCode, version, filename);
							image.setSource(thumnailResource);
						} else {
							thumbnail = false;
						}
					} else {
						thumbnail = false;
					}
				} else {
					thumbnail = false;
				}
				if (!thumbnail) {
					image.setVisible(false);
				}
				return image;
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

	public String getLastCallQTime() {
		return adapted.getLastCallQTime();
	}
}
