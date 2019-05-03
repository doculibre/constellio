package com.constellio.app.ui.framework.components.viewers.panel;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.containers.RefreshableContainer;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeNotifier;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class ViewableRecordVOContainer extends IndexedContainer implements ItemSetChangeNotifier, RefreshableContainer {
	
	private boolean compressed;
	
	private RecordVOLazyContainer recordVOContainer;
	
	private List<Container.ItemSetChangeListener> itemSetChangeListeners = new ArrayList<>();

	public ViewableRecordVOContainer(RecordVOLazyContainer recordVOContainer) {
		this.recordVOContainer = recordVOContainer;
		//		addRecordVOContainerProperties();
		addCompressedProperties();
	}
	
	public RecordVOLazyContainer getRecordVOContainer() {
		return recordVOContainer;
	}
	
	public Collection<?> getCompressedPropertyIds() {
		return Arrays.asList(SearchResultContainer.THUMBNAIL_PROPERTY, SearchResultContainer.SEARCH_RESULT_PROPERTY);
	}
	
	public boolean isCompressed() {
		return compressed;
	}

	public void setCompressed(boolean compressed) {
		//		boolean compressedChanged = this.compressed != compressed;
		this.compressed = compressed;
		//		if (compressedChanged) {
		//			if (compressed) {
		//				addCompressedProperties();
		//			} else {
		//				addRecordVOContainerProperties();
		//			}
		//		}
	}

	private void addCompressedProperties() {
		Collection<?> propertyIds = new ArrayList<>(getContainerPropertyIds());
		for (Object propertyId : propertyIds) {
			removeContainerProperty(propertyId);
		}
		addContainerProperty(SearchResultContainer.THUMBNAIL_PROPERTY, Image.class, null);
		addContainerProperty(SearchResultContainer.SEARCH_RESULT_PROPERTY, Component.class, null);
	}

	//	private void addRecordVOContainerProperties() {
	//		Collection<?> propertyIds = new ArrayList<>(getContainerPropertyIds());
	//		for (Object propertyId : propertyIds) {
	//			removeContainerProperty(propertyId);
	//		}
	//		addContainerProperty(SearchResultContainer.THUMBNAIL_PROPERTY, Image.class, null);
	//		for (Object recordVOContainerPropertyId : recordVOContainer.getContainerPropertyIds()) {
	//			addContainerProperty(recordVOContainerPropertyId, recordVOContainer.getType(recordVOContainerPropertyId), null);
	//		}
	//	}

	@Override
	public Property<?> getContainerProperty(Object itemId, Object propertyId) {
		Property<?> result;
		if (SearchResultContainer.THUMBNAIL_PROPERTY.equals(propertyId)) {
			result = newThumbnailProperty(itemId);
		} else if (SearchResultContainer.SEARCH_RESULT_PROPERTY.equals(propertyId)) {
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
				Integer index = (Integer) itemId;
				RecordVO recordVO = getRecordVO(index);
				return SearchResultContainer.getThumbnail(recordVO);
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
				Component recordDisplay = getRecordDisplay((int) itemId);
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
	public void refresh() {
		recordVOContainer.refresh();
	}

	@Override
	public void fireItemSetChange() {
		super.fireItemSetChange();
		recordVOContainer.refresh();
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

	protected abstract Component getRecordDisplay(Integer index);
	
	protected abstract RecordVO getRecordVO(Integer index);

}
