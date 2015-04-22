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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.Extension;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.Tree.ItemStyleGenerator;

public class LazyTree<T extends Serializable> extends CustomField<T> {
	
	private static final String LOADER_NEXT_LOADED_INDEX_ID = "loaderNextLoadedIndex";
	private static final String LOADER_PARENT_ITEM_ID = "loaderParentItemId";
	
	private LazyTreeDataProvider<T> dataProvider;
	
	private int bufferSize;
	
	private Tree adaptee;
	
	private List<ItemClickListener> itemClickListeners = new ArrayList<ItemClickListener>();

	public LazyTree(LazyTreeDataProvider<T> treeDataProvider) {
		this(treeDataProvider, Integer.MAX_VALUE);
	}
	
	public LazyTree(LazyTreeDataProvider<T> treeDataProvider, int bufferSize) {
		super();
		this.dataProvider = treeDataProvider;
		this.bufferSize = bufferSize;

		adaptee = new Tree() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public Collection getContainerPropertyIds() {
				Collection propertyIds = LazyTree.this.getContainerPropertyIds();
				if (propertyIds == null) {
					propertyIds = super.getContainerPropertyIds();
				}
				propertyIds.add(LOADER_NEXT_LOADED_INDEX_ID);
				propertyIds.add(LOADER_PARENT_ITEM_ID);
				return propertyIds;
			}

			@Override
			public Property<?> getContainerProperty(Object itemId, Object propertyId) {
				Property<?> property;
				if (isLoader(itemId)) {
					property = super.getContainerProperty(itemId, propertyId);
				} else {
					property = LazyTree.this.getContainerProperty(itemId, propertyId);
					if (property == null) {
						property = super.getContainerProperty(itemId, propertyId);
					}
				}
				return property;
			}

			@SuppressWarnings("unchecked")
			@Override
			public String getItemCaption(Object itemId) {
				String itemCaption;
				if (isLoader(itemId)) {
					itemCaption = getLoaderItemCaption(itemId);
				} else {
					itemCaption = LazyTree.this.getItemCaption((T) itemId);
					if (itemCaption == null) {
						itemCaption = super.getItemCaption(itemId);
					}
				}
				return itemCaption;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Resource getItemIcon(Object itemId) {
				Resource itemIcon;
				if (isLoader(itemId)) {
					itemIcon = null;
				} else {
					itemIcon = LazyTree.this.getItemIcon((T) itemId);
					if (itemIcon == null) {
						itemIcon = super.getItemIcon(itemId);
					}
				}
				return itemIcon;
			}
		};
		adaptee.addContainerProperty(LOADER_NEXT_LOADED_INDEX_ID, Integer.class, null);
		adaptee.addContainerProperty(LOADER_PARENT_ITEM_ID, LazyTree.this.getType(), null);
	}

	@Override
	protected Component initContent() {
		final int rootObjectsCount = dataProvider.getRootObjectsCount();
		List<T> rootObjects = dataProvider.getRootObjects(0, bufferSize);
		for (T rootObject : rootObjects) {
			addItem(rootObject, null);
		}
		if (rootObjectsCount > rootObjects.size()) {
			addLoaderItem(null, rootObjects.size());
		}
		
		adaptee.addItemClickListener(new ItemClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void itemClick(ItemClickEvent event) {
				Object itemId = event.getItemId();
				if (isLoader(itemId)) {
					// Clicking on a loader, we will replace it with the actual children
					T parent = getParentForLoader(itemId);
					int nextLoadedIndex = getNextLoadedIndexForLoader(itemId);
					if (parent != null) {
						int childrenCount = dataProvider.getChildrenCount(parent);
						List<T> children = dataProvider.getChildren(parent, nextLoadedIndex, bufferSize);
						replaceLoaderWithChildren(itemId, parent, children);
						if (childrenCount > nextLoadedIndex + children.size()) {
							addLoaderItem(parent, nextLoadedIndex + children.size());
						}
					} else {
						// Root object
						List<T> rootObjects = dataProvider.getRootObjects(nextLoadedIndex, bufferSize);
						adaptee.removeItem(itemId);
						for (T rootObject : rootObjects) {
							addItem(rootObject, null);
						}
						if (rootObjectsCount > nextLoadedIndex + rootObjects.size()) {
							addLoaderItem(null, nextLoadedIndex + rootObjects.size());
						}
					}
				} else {
					setValue((T) itemId);
					// Forward to the adaptee
					for (ItemClickListener itemClickListener : itemClickListeners) {
						itemClickListener.itemClick(event);
					}
				}
			}
		});
		
		adaptee.addExpandListener(new ExpandListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void nodeExpand(ExpandEvent event) {
				T parent = (T) event.getItemId();
				Object loaderId = getLoaderId(parent);
				if (loaderId != null) {
					// Since the loader is present, it means that the children have not been removed
					int childrenCount = dataProvider.getChildrenCount(parent);
					List<T> children = dataProvider.getChildren(parent, 0, bufferSize);
					replaceLoaderWithChildren(loaderId, parent, children);
					if (childrenCount > children.size()) {
						addLoaderItem(parent, children.size());
					}
				}
			}
		});
		
		return adaptee;
	}
	
	public final LazyTreeDataProvider<T> getDataProvider() {
		return dataProvider;
	}

	private void addItem(T child, T parent) {
		adaptee.addItem(child);
		if (parent != null) {
			adaptee.setParent(child, parent);
		}
		if (!dataProvider.isLeaf(child) && dataProvider.hasChildren(child)) {
			addLoaderItem(child, 0);
		} else {
			adaptee.setChildrenAllowed(child, false);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addLoaderItem(T parent, int nextLoadedIndex) {
		Object loaderItemId = adaptee.addItem();
		Item loadingItem = adaptee.getItem(loaderItemId);
		loadingItem.getItemProperty(LOADER_NEXT_LOADED_INDEX_ID).setValue(nextLoadedIndex);
		loadingItem.getItemProperty(LOADER_PARENT_ITEM_ID).setValue(parent);
		if (parent != null) {
			adaptee.setParent(loaderItemId, parent);
		}
		adaptee.setChildrenAllowed(loaderItemId, false);
	}
	
	@SuppressWarnings("unchecked")
	private T getParentForLoader(Object loaderItemId) {
		Item loaderItem = adaptee.getItem(loaderItemId);
		return (T) loaderItem.getItemProperty(LOADER_PARENT_ITEM_ID).getValue();
	}
	
	@SuppressWarnings("unchecked")
	private Integer getNextLoadedIndexForLoader(Object loaderItemId) {
		Item loaderItem = adaptee.getItem(loaderItemId);
		Property<Integer> nextLoadedIndexProperty = loaderItem.getItemProperty(LOADER_NEXT_LOADED_INDEX_ID);
		return nextLoadedIndexProperty != null ? nextLoadedIndexProperty.getValue() : null;
	}
	
	@SuppressWarnings("unchecked")
	private boolean isLoader(Object itemId) {
		Item item = adaptee.getItem(itemId);
		Property<Integer> nextLoadedIndexProperty = item.getItemProperty(LOADER_NEXT_LOADED_INDEX_ID);
		return nextLoadedIndexProperty != null && nextLoadedIndexProperty.getValue() != null;
	}
	
	private Object getLoaderId(T parent) {
		Object initialLoaderId;
		Collection<?> children = adaptee.getChildren(parent);
		if (children.size() == 1) {
			Object childId = children.iterator().next();
			if (isLoader(childId)) {
				initialLoaderId = childId;
			} else {
				initialLoaderId = null;
			}
		} else {
			initialLoaderId = null;
		}	
		return initialLoaderId;
	}
	
	private void replaceLoaderWithChildren(Object loaderId, T parent, List<T> children) {
		adaptee.removeItem(loaderId);
		for (T child : children) {
			addItem(child, parent);
		}
	}
	
	private String getLoaderItemCaption(Object itemId) {
		String loaderItemCaption;
		Item loaderItem = adaptee.getItem(itemId);
		int lowerLimit = (int) loaderItem.getItemProperty(LOADER_NEXT_LOADED_INDEX_ID).getValue();
		int upperLimit;
		int sameLevelNodeCount; 
		T parent = getParentForLoader(itemId);
		if (parent != null) {
			sameLevelNodeCount = dataProvider.getChildrenCount(parent);
		} else {
			sameLevelNodeCount = dataProvider.getRootObjectsCount();
		}
		if ((lowerLimit + bufferSize) >= sameLevelNodeCount) {
			upperLimit = sameLevelNodeCount;
		} else {
			upperLimit = lowerLimit + bufferSize;
		}
		loaderItemCaption = getLoaderItemCaption(lowerLimit + 1, upperLimit);
		return loaderItemCaption;
	}
	
	protected String getLoaderItemCaption(int lowerLimit, int upperLimit) {
		String caption;
		if (lowerLimit < upperLimit) {
			caption = "[" + (lowerLimit) + "-" + upperLimit + "]...";
		} else {
			caption = "[" + lowerLimit + "]...";
		}
		return caption;
	}

	public String getItemCaption(T object) {
		return null;
	}
	
	public void addAttachListener(AttachListener listener) {
		adaptee.addAttachListener(listener);
	}

	public void addDetachListener(DetachListener listener) {
		adaptee.addDetachListener(listener);
	}

	public void addExpandListener(ExpandListener listener) {
		adaptee.addExpandListener(listener);
	}

	public void addCollapseListener(CollapseListener listener) {
		adaptee.addCollapseListener(listener);
	}

	public void addItemClickListener(ItemClickListener listener) {
		itemClickListeners.add(listener);
	}

	public void removeAttachListener(AttachListener listener) {
		adaptee.removeAttachListener(listener);
	}

	public void removeDetachListener(DetachListener listener) {
		adaptee.removeDetachListener(listener);
	}

	public void removeExtension(Extension extension) {
		adaptee.removeExtension(extension);
	}

	public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue)
			throws UnsupportedOperationException {
		return adaptee.addContainerProperty(propertyId, type, defaultValue);
	}

	public boolean removeContainerProperty(Object propertyId)
			throws UnsupportedOperationException {
		return adaptee.removeContainerProperty(propertyId);
	}

	public void removeExpandListener(ExpandListener listener) {
		adaptee.removeExpandListener(listener);
	}

	public void removeCollapseListener(CollapseListener listener) {
		adaptee.removeCollapseListener(listener);
	}

	public void removeItemClickListener(ItemClickListener listener) {
		itemClickListeners.remove(listener);
	}

	public void setItemIcon(Object itemId, Resource icon, String altText) {
		adaptee.setItemIcon(itemId, icon, altText);
	}

	public void setItemIconAlternateText(Object itemId, String altText) {
		adaptee.setItemIconAlternateText(itemId, altText);
	}

	public void setItemCaption(Object itemId, String caption) {
		adaptee.setItemCaption(itemId, caption);
	}

	public void setItemCaptionMode(ItemCaptionMode mode) {
		adaptee.setItemCaptionMode(mode);
	}

	public void setItemStyleGenerator(ItemStyleGenerator itemStyleGenerator) {
		adaptee.setItemStyleGenerator(itemStyleGenerator);
	}

	public void setItemCaptionPropertyId(Object propertyId) {
		adaptee.setItemCaptionPropertyId(propertyId);
	}

	public void setItemIconPropertyId(Object propertyId)
			throws IllegalArgumentException {
		adaptee.setItemIconPropertyId(propertyId);
	}

	public void setItemDescriptionGenerator(ItemDescriptionGenerator generator) {
		adaptee.setItemDescriptionGenerator(generator);
	}

	public final String getItemIconAlternateText(Object itemId) {
		return adaptee.getItemIconAlternateText(itemId);
	}

	public Resource getItemIcon(Object itemId) {
		return null;
	}

	public final ItemCaptionMode getItemCaptionMode() {
		return adaptee.getItemCaptionMode();
	}

	public final Object getItemCaptionPropertyId() {
		return adaptee.getItemCaptionPropertyId();
	}

	public final Object getItemIconPropertyId() {
		return adaptee.getItemIconPropertyId();
	}

	public final ItemDescriptionGenerator getItemDescriptionGenerator() {
		return adaptee.getItemDescriptionGenerator();
	}

	public Collection<?> getContainerPropertyIds() {
		return null;
	}

	public Property<?> getContainerProperty(Object itemId, Object propertyId) {
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class getType() {
		return Object.class;
	}

	public final Tree getNestedTree() {
		return adaptee;
	}

}
