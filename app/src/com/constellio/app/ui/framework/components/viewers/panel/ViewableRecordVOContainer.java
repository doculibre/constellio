package com.constellio.app.ui.framework.components.viewers.panel;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.RecordDisplayFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.resource.ConstellioResourceHandler;
import com.constellio.app.ui.framework.containers.RecordVOContainer;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeNotifier;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ViewableRecordVOContainer extends IndexedContainer implements ItemSetChangeNotifier, RecordVOContainer {

	public final static String THUMBNAIL_PROPERTY = "thumbnail";

	public final static String SEARCH_RESULT_PROPERTY = "searchResult";
	public final static int THUMBNAIL_WIDTH = 80;
	
	private boolean compressed;

	private RecordVOContainer recordVOContainer;
	
	private List<Container.ItemSetChangeListener> itemSetChangeListeners = new ArrayList<>();

	public ViewableRecordVOContainer(RecordVOContainer recordVOContainer) {
		this.recordVOContainer = recordVOContainer;
		addCompressedProperties();
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
		addContainerProperty(THUMBNAIL_PROPERTY, Image.class, null);
		addContainerProperty(SEARCH_RESULT_PROPERTY, Component.class, null);
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
	
	private Property<Image> newThumbnailProperty(final Object itemId) {
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

	private Property<Component> newSearchResultProperty(final Object itemId) {
		return new AbstractProperty<Component>() {
			@Override
			public Component getValue() {
				Component recordDisplay = getRecordDisplay(itemId);
				ReferenceDisplay referenceDisplay = ComponentTreeUtils.getFirstChild(recordDisplay, ReferenceDisplay.class);
				if (referenceDisplay != null) {
					referenceDisplay.setIcon(null);
				}
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

	public static Image getThumbnail(RecordVO recordVO) {
		Image image = new Image(null);
		String schemaTypeCode = recordVO.getSchema().getTypeCode();
		if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
			final ContentVersionVO contentVersionVO = recordVO.get(Document.CONTENT);
			if (contentVersionVO != null) {
				String filename = contentVersionVO.getFileName();
				String recordId = recordVO.getId();
				String metadataCode = recordVO.getMetadata(Document.CONTENT).getLocalCode();
				String version = contentVersionVO.getVersion();

				if (ConstellioResourceHandler.hasContentThumbnail(recordId, metadataCode, version)) {
					Resource thumbnailResource = ConstellioResourceHandler.createThumbnailResource(recordId, metadataCode, version, filename);
					image.setSource(thumbnailResource);
				} else {
					Resource thumbnailResource = new ThemeResource("images/icons/64/document_64.png");
					image.setSource(thumbnailResource);
				}
			} else {
				Resource thumbnailResource = new ThemeResource("images/icons/64/document_64.png");
				image.setSource(thumbnailResource);
			}
		} else if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
			ThemeResource iconResource = (ThemeResource) FileIconUtils.getIconForRecordVO(recordVO);
			String resourceId = iconResource.getResourceId();
			resourceId = resourceId.replace(".png", "_64.png");
			Resource thumbnailResource = new ThemeResource(resourceId);
			image.setSource(thumbnailResource);
		} else if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)) {
			Resource thumbnailResource = new ThemeResource("images/icons/64/box_64.png");
			image.setSource(thumbnailResource);
		} else if (schemaTypeCode.toLowerCase().contains("task")) {
			Resource thumbnailResource = new ThemeResource("images/icons/64/task_64.png");
			image.setSource(thumbnailResource);
		} else {
			Resource thumbnailResource = new ThemeResource("images/icons/64/default_64.png");
			image.setSource(thumbnailResource);
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
			listener.containerItemSetChange(event);
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
		UserVO currentUser = ConstellioUI.getCurrentSessionContext().getCurrentUser();
		RecordDisplayFactory displayFactory = new RecordDisplayFactory(currentUser);
		RecordVO recordVO = recordVOContainer.getRecordVO(itemId);
		SearchResultVO searchResultVO = new SearchResultVO(recordVO, new HashMap<String, List<String>>());
		SearchResultDisplay searchResultDisplay = displayFactory.build(searchResultVO, null, null, null, null);
		searchResultDisplay.getTitleLink().setIcon(null);
		return searchResultDisplay;
	}

	@Override
	public List<MetadataSchemaVO> getSchemas() {
		return recordVOContainer.getSchemas();
	}

}
