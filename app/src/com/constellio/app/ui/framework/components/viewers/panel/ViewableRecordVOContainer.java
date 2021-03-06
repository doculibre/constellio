package com.constellio.app.ui.framework.components.viewers.panel;

import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.framework.containers.RecordVOContainer;
import com.constellio.app.ui.util.ResponsiveUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeNotifier;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public class ViewableRecordVOContainer extends IndexedContainer implements ItemSetChangeNotifier, RecordVOContainer {

	public final static String THUMBNAIL_PROPERTY = "thumbnail";
	public final static String SEARCH_RESULT_PROPERTY = "searchResult";
	public final static int THUMBNAIL_WIDTH = 80;
	
	private boolean compressed;

	private RecordVOContainer recordVOContainer;
	
	private List<Container.ItemSetChangeListener> itemSetChangeListeners = new ArrayList<>();

	public ViewableRecordVOContainer(RecordVOContainer recordVOContainer) {
		this.recordVOContainer = recordVOContainer;
//		this.recordVOContainer.addItemSetChangeListener(new ItemSetChangeListener() {
//			@Override
//			public void containerItemSetChange(com.vaadin.data.Container.ItemSetChangeEvent event) {
//				ViewableRecordVOContainer.this.fireItemSetChange(event);
//			}
//		});
		addCompressedProperties();
	}

	protected boolean isShowThumbnailCol() {
		return true;
	}

	public RecordVOContainer getRecordVOContainer() {
		return recordVOContainer;
	}
	
	public boolean isCompressed() {
		return compressed;
	}

	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}

	private void addCompressedProperties() {
		Collection<?> propertyIds = new ArrayList<>(getContainerPropertyIds());
		for (Object propertyId : propertyIds) {
			removeContainerProperty(propertyId);
		}
		if (isRightToLeft()) {
			addContainerProperty(SEARCH_RESULT_PROPERTY, Component.class, null);
		}
		if (!ResponsiveUtils.isPhone() && isShowThumbnailCol()) {
			addContainerProperty(THUMBNAIL_PROPERTY, Image.class, null);
		}
		if (!isRightToLeft()) {
			addContainerProperty(SEARCH_RESULT_PROPERTY, Component.class, null);
		}
	}

	@Override
	public Property<?> getContainerProperty(Object itemId, Object propertyId) {
		Property<?> result;
		if (THUMBNAIL_PROPERTY.equals(propertyId)) {
			result = newThumbnailProperty(itemId);
		} else if (SEARCH_RESULT_PROPERTY.equals(propertyId)) {
			result = newSearchResultProperty(itemId);
		} else if (!compressed) {
			result = recordVOContainer.getContainerProperty(itemId, propertyId);
		} else {
			result = null;
		}
		return result;
	}

	protected Property<Image> newThumbnailProperty(final Object itemId) {
		return new AbstractProperty<Image>() {
			@Override
			public Image getValue() {
				RecordVO recordVO = getRecordVO(itemId);
				return getThumbnail(recordVO);
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

	protected Property<Label> newDragProperty(final Object itemId) {
		Label dragLabel = new Label("");
		dragLabel.addStyleName("viewable-record-row-drag");
		return new ObjectProperty<Label>(dragLabel);
	}

	private Property<Component> newSearchResultProperty(final Object itemId) {
		return new AbstractProperty<Component>() {
			@Override
			public Component getValue() {
				Component recordDisplay = getRecordDisplay(itemId);
				return recordDisplay;
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

	public Image getThumbnail(RecordVO recordVO) {
		Image image = new Image(null);
		Resource thumbnailResource;
		List<String> thumbnailStyles = Collections.emptyList();

		if (recordVO != null) {
			String collection = recordVO.getSchema().getCollection();
			AppLayerFactory appLayerFactory = ConstellioUI.getCurrent().getConstellioFactories().getAppLayerFactory();

			GetIconPathParams iconPathParams = new GetIconPathParams(recordVO, false);
			thumbnailResource = appLayerFactory.getExtensions().forCollection(collection).getThumbnailResourceForRecordVO(
					iconPathParams);

			thumbnailStyles = appLayerFactory.getExtensions().forCollection(collection).getThumbnailStylesForRecordVO(iconPathParams);

		} else {
			thumbnailResource = null;
		}
		if (thumbnailResource == null) {
			thumbnailResource = new ThemeResource("images/icons/64/default_64.png");
		}
		image.setSource(thumbnailResource);

		if (thumbnailStyles != null && !thumbnailStyles.isEmpty()) {
			thumbnailStyles.forEach(image::addStyleName);
		}

		return image;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Object> getItemIds(int startIndex, int numberOfIds) {
		return (List<Object>) recordVOContainer.getItemIds(startIndex, numberOfIds);
	}

	@Override
	public int size() {
		return recordVOContainer.size();
	}

	@Override
	public Item getItem(Object itemId) {
		return recordVOContainer.getItem(itemId);
	}

	@Override
	public boolean containsId(Object itemId) {
		return recordVOContainer.containsId(itemId);
	}

	@Override
	public List<?> getItemIds() {
		return (List<?>) recordVOContainer.getItemIds();
	}

	@Override
	public Object nextItemId(Object itemId) {
		return recordVOContainer.nextItemId(itemId);
	}

	@Override
	public Object prevItemId(Object itemId) {
		return recordVOContainer.prevItemId(itemId);
	}

	@Override
	public Object firstItemId() {
		return recordVOContainer.firstItemId();
	}

	@Override
	public Object lastItemId() {
		return recordVOContainer.lastItemId();
	}

	@Override
	public boolean isFirstId(Object itemId) {
		return recordVOContainer.isFirstId(itemId);
	}

	@Override
	public boolean isLastId(Object itemId) {
		return recordVOContainer.isLastId(itemId);
	}

	@Override
	public Object getIdByIndex(int index) {
		return recordVOContainer.getIdByIndex(index);
	}

	@Override
	public int indexOfId(Object itemId) {
		return recordVOContainer.indexOfId(itemId);
	}

	@Override
	public void forceRefresh() {
		recordVOContainer.forceRefresh();
	}

	@Override
	public void fireItemSetChange() {
		super.fireItemSetChange();
		recordVOContainer.forceRefresh();
	}

	@Override
	protected void fireItemSetChange(com.vaadin.data.Container.ItemSetChangeEvent event) {
		super.fireItemSetChange(event);
		for (ItemSetChangeListener listener : itemSetChangeListeners) {
			if (!(listener instanceof ViewableRecordVOContainer)) {
				listener.containerItemSetChange(event);
			}
		}
	}

    // ItemSetChangeNotifier
    /**
     * @deprecated As of 7.0, replaced by
     *             {@link #addItemSetChangeListener(com.vaadin.data.Container.ItemSetChangeListener)}
     **/
    @Deprecated
    @Override
    public void addListener(Container.ItemSetChangeListener listener) {
    	recordVOContainer.addListener(listener);
    }

    @Override
    public void addItemSetChangeListener(
            Container.ItemSetChangeListener listener) {
		recordVOContainer.addItemSetChangeListener(listener);
		itemSetChangeListeners.add(listener);
    }

    @Override
    public void removeItemSetChangeListener(
            Container.ItemSetChangeListener listener) {
		recordVOContainer.addItemSetChangeListener(listener);
		itemSetChangeListeners.remove(listener);
    }

    /**
     * @deprecated As of 7.0, replaced by
     *             {@link #removeItemSetChangeListener(com.vaadin.data.Container.ItemSetChangeListener)}
     **/
    @Deprecated
    @Override
    public void removeListener(Container.ItemSetChangeListener listener) {
		recordVOContainer.removeListener(listener);
		itemSetChangeListeners.remove(listener);
    }

	@Override
	public RecordVO getRecordVO(Object itemId) {
		return recordVOContainer.getRecordVO(itemId);
	}

	protected Component getRecordDisplay(Object itemId) {
		Integer index = recordVOContainer.indexOfId(itemId);
		UserVO currentUser = ConstellioUI.getCurrentSessionContext().getCurrentUser();
		RecordDisplayFactory displayFactory = new RecordDisplayFactory(currentUser);
		RecordVO recordVO = recordVOContainer.getRecordVO(itemId);
		SearchResultVO searchResultVO = new SearchResultVO(index, recordVO, getHighlights(itemId));
		SearchResultDisplay searchResultDisplay = displayFactory.build(searchResultVO, null, null, null, null);
		return searchResultDisplay;
	}

	@Override
	public List<MetadataSchemaVO> getSchemas() {
		return recordVOContainer.getSchemas();
	}

	@Override
	public Map<String, List<String>> getHighlights(Object itemId) {
		return recordVOContainer.getHighlights(itemId);
	}

}
