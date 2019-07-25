package com.constellio.app.ui.framework.components.tree;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.components.table.TablePropertyCache;
import com.constellio.app.ui.framework.components.table.TablePropertyCache.CellKey;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.ObjectsResponse;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.server.Extension;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.Tree.ItemStyleGenerator;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.peter.contextmenu.ContextMenu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LazyTree<T extends Serializable> extends CustomField<Object> {

	private static final String CAPTION_PROPERTY = "caption";
	private static final String LOADER_NEXT_LOADED_INDEX_ID = "loaderNextLoadedIndex";
	private static final String LOADER_PARENT_ITEM_ID = "loaderParentItemId";

	private LazyTreeDataProvider<T> dataProvider;

	private int bufferSize;
	
	private boolean multiValue = false;

	private BaseTreeComponent adaptee;

	private List<ItemClickListener> itemClickListeners = new ArrayList<ItemClickListener>();
	
	private Map<Object, List<Object>> loaderPropertyCache = new HashMap<>();
	
	public LazyTree(LazyTreeDataProvider<T> treeDataProvider) {
		this(treeDataProvider, false);
	}

	public LazyTree(LazyTreeDataProvider<T> treeDataProvider, boolean multiValue) {
		this(treeDataProvider, getBufferSizeFromConfig(), multiValue);
	}

	private static int getBufferSizeFromConfig() {
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		return modelLayerFactory.getSystemConfigs().getLazyTreeBufferSize();
	}

	public LazyTree(LazyTreeDataProvider<T> treeDataProvider, int bufferSize) {
		this(treeDataProvider, bufferSize, false);
	}

	public LazyTree(LazyTreeDataProvider<T> treeDataProvider, int bufferSize, boolean multiValue) {
		super();
		this.dataProvider = treeDataProvider;
		this.bufferSize = bufferSize;
		this.multiValue = multiValue;

		if (this.multiValue) {
			adaptee = new TreeTableComponent();
		} else {
			adaptee = new TreeComponent();
		}
		adaptee.setItemDescriptionGenerator(new ItemDescriptionGenerator() {
			@SuppressWarnings("unchecked")
			@Override
			public String generateDescription(Component source, Object itemId, Object propertyId) {
				String itemDescription;
				if (isLoader(itemId)) {
					itemDescription = getLoaderItemDescription(itemId);
				} else {
					itemDescription = dataProvider.getDescription((T) itemId);
				}
				return itemDescription;
			}
		});
		adaptee.addContainerProperty(CAPTION_PROPERTY, Component.class, null);
		adaptee.addContainerProperty(LOADER_NEXT_LOADED_INDEX_ID, Integer.class, null);
		adaptee.addContainerProperty(LOADER_PARENT_ITEM_ID, LazyTree.this.getType(), null);
		adaptee.setVisibleColumns(CAPTION_PROPERTY);
		//		adaptee.setWidth("100%");
		adaptee.setPageLength(0);
		adaptee.addStyleName(ValoTheme.TREETABLE_BORDERLESS);
		adaptee.addStyleName(ValoTheme.TREETABLE_NO_HEADER);
		adaptee.addStyleName(ValoTheme.TREETABLE_NO_HORIZONTAL_LINES);
		adaptee.addStyleName(ValoTheme.TREETABLE_NO_VERTICAL_LINES);
		adaptee.addStyleName(ValoTheme.TREETABLE_NO_STRIPES);
		adaptee.addStyleName(ValoTheme.TREETABLE_COMPACT);
	}

	protected CellKey getCellKey(Object itemId, Object propertyId) {
		return null;
	}

	@Override
	protected Component initContent() {
		ObjectsResponse<T> rootObjectsResponse = dataProvider.getRootObjects(0, bufferSize);
		final int rootObjectsCount = rootObjectsResponse.getCount();

		for (T rootObject : rootObjectsResponse.getObjects()) {
			addItem(rootObject, null);
		}
		if (rootObjectsCount > rootObjectsResponse.getObjects().size()) {
			addLoaderItem(null, rootObjectsResponse.getObjects().size());
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
						ObjectsResponse<T> response = dataProvider.getChildren(parent, nextLoadedIndex, bufferSize);
						int childrenCount = response.getCount();
						replaceLoaderWithChildren(itemId, parent, response.getObjects());
						if (childrenCount > nextLoadedIndex + response.getObjects().size()) {
							addLoaderItem(parent, nextLoadedIndex + response.getObjects().size());
						}
					} else {
						// Root object
						ObjectsResponse<T> rootObjects = dataProvider.getRootObjects(nextLoadedIndex, bufferSize);
						adaptee.removeItem(itemId);
						for (T rootObject : rootObjects.getObjects()) {
							addItem(rootObject, null);
						}
						if (rootObjects.getCount() > nextLoadedIndex + rootObjects.getObjects().size()) {
							addLoaderItem(null, nextLoadedIndex + rootObjects.getObjects().size());
						}
					}
				} else {
					if (!multiValue) {
						setValue((T) itemId);
						// Forward to the adaptee
					}

					boolean shouldExpandOrCollapse = true;
					for (ItemClickListener itemClickListener : itemClickListeners) {
						itemClickListener.itemClick(event);
						if (itemClickListener instanceof TreeItemClickListener) {
							shouldExpandOrCollapse &= ((TreeItemClickListener) itemClickListener).shouldExpandOrCollapse(event);
						}
					}

					if (shouldExpandOrCollapse) {
						if (isExpanded(itemId)) {
//							adaptee.collapseItem(itemId);
							adaptee.setCollapsed(itemId, true);
						} else {
//							adaptee.expandItem(itemId);
							adaptee.setCollapsed(itemId, false);
						}
					}

				}
			}
		});

		adaptee.addExpandListener(new ExpandListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void nodeExpand(ExpandEvent event) {
				T itemId = (T) event.getItemId();
				adjustLoaderAfterExpand(itemId);
			}
		});

		adaptee.addCollapseListener(new CollapseListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void nodeCollapse(CollapseEvent event) {
				T itemId = (T) event.getItemId();
				adjustLoaderAfterCollapse(itemId);
			}
		});

		return (Component) adaptee;
	}
	
	private Object adjustValue(Object value) {
		Object adjustedValue;
		if (multiValue) {
			if (!(value instanceof List)) {
				adjustedValue = Arrays.asList(value);
			} else {
				adjustedValue = value;
			}
		} else {
			adjustedValue = value;
		}
		return adjustedValue;
	}

	private void adjustLoaderAfterExpand(T itemId) {
		Object loaderId = getLoaderId(itemId);
		if (loaderId != null) {
			// Since the loader is present, it means that the children have not been removed
			ObjectsResponse<T> childrenResponse = dataProvider.getChildren(itemId, 0, bufferSize);
			replaceLoaderWithChildren(loaderId, itemId, childrenResponse.getObjects());
			if (childrenResponse.getCount() > childrenResponse.getObjects().size()) {
				addLoaderItem(itemId, childrenResponse.getObjects().size());
			}
		}
		if (!multiValue) {
			Resource icon = getItemIcon(itemId);
			setItemIcon(itemId, icon, "");
		}
	}

	private void adjustLoaderAfterCollapse(T itemId) {
		if (!multiValue) {
			Resource icon = getItemIcon(itemId);
			setItemIcon(itemId, icon, "");
		}
	}

	public final LazyTreeDataProvider<T> getDataProvider() {
		return dataProvider;
	}

	private void addItem(T child, T parent) {
		addItem(child, parent, true);
	}

	private void addItem(T child, T parent, boolean addLoaderItemIfPossible) {
		adaptee.addItem(child);
		if (parent != null) {
			adaptee.setParent(child, parent);
		}
		if (!dataProvider.isLeaf(child) && dataProvider.hasChildren(child)) {
			if (addLoaderItemIfPossible) {
				addLoaderItem(child, 0);
			}
		} else {
			adaptee.setChildrenAllowed(child, false);
		}
	}

	private void addLoaderItem(T parent, int nextLoadedIndex) {
		Object loaderItemId = loaderPropertyCache.size() + 1;
		loaderPropertyCache.put(loaderItemId, new ArrayList<Object>(Arrays.asList(nextLoadedIndex, parent)));
		adaptee.addItem(loaderItemId);
		if (parent != null) {
			adaptee.setParent(loaderItemId, parent);
		}
		adaptee.setChildrenAllowed(loaderItemId, false);
	}

	private Integer getNextLoadedIndexForLoader(Object loaderItemId) {
		return (Integer) ((List<Object>) loaderPropertyCache.get(loaderItemId)).get(0);
	}
	
	@SuppressWarnings("unchecked")
	private T getParentForLoader(Object loaderItemId) {
		return (T) ((List<Object>) loaderPropertyCache.get(loaderItemId)).get(1);
	}

	private boolean isLoader(Object itemId) {
		return loaderPropertyCache.containsKey(itemId);
	}

	private Object getLoaderId(T parent) {
		Object initialLoaderId;
		Collection<?> children = adaptee.getChildren(parent);
		if (children != null && children.size() == 1) {
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
		int lowerLimit = (int) getNextLoadedIndexForLoader(itemId);
		int upperLimit;
		int sameLevelNodeCount;
		T parent = getParentForLoader(itemId);
		if (parent != null) {
			sameLevelNodeCount = dataProvider.getEstimatedChildrenNodesCount(parent);
		} else {
			sameLevelNodeCount = dataProvider.getEstimatedRootNodesCount();
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

	protected String getLoaderItemDescription(Object itemId) {
		return null;
	}
	
	protected boolean isSelectable(T object) {
		return true;
	}
	
	protected Component getItemCaptionComponent(final T object) {
		Component itemCaptionComponent;
		if (multiValue) {
			if (isSelectable(object)) {
				String itemCaption = getItemCaption(object);
				final CheckBox checkBox = new CheckBox(itemCaption);
				checkBox.addValueChangeListener(new Property.ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						List<T> listValue = ensureListValue();
						Boolean selected = checkBox.getValue();
						if (selected && !listValue.contains(object)) {
							listValue.add(object);
							setValue(listValue);
						} else if (!selected && listValue.contains(object)) {
							listValue.remove(object);
							setValue(listValue);
						}
					}
				});
				Resource icon = getItemIcon(object);
				checkBox.setIcon(icon);
				
				itemCaptionComponent = checkBox;
			} else {
				itemCaptionComponent = null;
			}
			
		} else {
			itemCaptionComponent = null;
		}
		return itemCaptionComponent;
		
	}
	
	private List<T> ensureListValue() {
		List<T> value = (List<T>) getValue();
		if (value == null) {
			value = new ArrayList<>();
		}
		return value;
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
//		adaptee.setItemIcon(itemId, icon, altText);
	}

	public void setItemIconAlternateText(Object itemId, String altText) {
//		adaptee.setItemIconAlternateText(itemId, altText);
	}

	public void setItemCaption(Object itemId, String caption) {
		adaptee.setItemCaption(itemId, caption);
	}

	public void setItemCaptionMode(ItemCaptionMode mode) {
		adaptee.setItemCaptionMode(mode);
	}

	public void setItemStyleGenerator(ItemStyleGenerator itemStyleGenerator) {
//		adaptee.setItemStyleGenerator(itemStyleGenerator);
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
//		return adaptee.getItemIconAlternateText(itemId);
		return null;
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

	public boolean isMultiValue() {
		return multiValue;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public Class getType() {
		return Object.class;
	}

	//	public final TreeTable getNestedTreeTable() {
	//		return (TreeTable) adaptee;
	//	}

	public boolean isExpanded(Object itemId) {
//		return adaptee.isExpanded(itemId);
		return !adaptee.isCollapsed(itemId);
	}

	protected Item getItem(Object itemId) {
		return adaptee.getItem(itemId);
	}

	/**
	 * Will load the nodes corresponding to the item ids passed. Each item id corresponds to a parent
	 * except for the last one, which is the last node, which can be a leaf.
	 * <p>
	 * The item ids are ordered from the root level to the leaf level. Omitting any level will prevent
	 * the tree from loading the desired node.
	 * <p>
	 * Note: The lazy loading of a level will stop after 10 attempts. This is necessary to prevent
	 * performance issues.
	 *
	 * @param itemIds The item ids to load, from the root level to the leaf level.
	 * @return
	 */
	public boolean loadAndExpand(List<T> itemIds) {
		boolean match = true;
		T currentParentId = null;
		for (T itemId : itemIds) {
			Item itemFound = loadUntil(itemId, currentParentId);
			if (itemFound == null) {
				match = false;
				break;
			}
			currentParentId = itemId;
		}
		return match;
	}

	public Item loadUntil(T itemId, T parentId) {
		// Ensures that initContent() has been called
		getContent();

		Item match = adaptee.getItem(itemId);
		if (match == null) {
			loop1:
			for (int i = 0; match == null && i < 10; i++) {
				int start;
				ObjectsResponse<T> extraObjectsResponse;
				Object lastLoaderItemId = null;
				if (parentId == null) {
					Collection<?> rootItemIds = adaptee.rootItemIds();
					start = rootItemIds.size();
					if (start > 0) {
						List<?> rootItemIdsList = new ArrayList<Object>(rootItemIds);
						Object lastItemId = rootItemIdsList.get(start - 1);
						if (isLoader(lastItemId)) {
							lastLoaderItemId = lastItemId;
							start--;
						}
					}
					extraObjectsResponse = dataProvider.getRootObjects(start, bufferSize);
				} else {
					Item existingParentItem = adaptee.getItem(parentId);
					if (existingParentItem != null) {
						Collection<?> children = adaptee.getChildren(parentId);
						if (children != null) {
							start = children.size();
							if (start == 1) {
								Object firstItemId = children.iterator().next();
								if (isLoader(firstItemId)) {
									adaptee.removeItem(firstItemId);
								}
							}
							if (start > 0) {
								List<?> childrenList = new ArrayList<Object>(children);
								Object lastItemId = childrenList.get(start - 1);
								if (isLoader(lastItemId)) {
									lastLoaderItemId = lastItemId;
									start--;
								}
							}
						} else {
							start = 0;
						}
						extraObjectsResponse = dataProvider.getChildren(parentId, start, bufferSize);
					} else {
						break loop1;
					}
				}
				int extraObjectsCount = extraObjectsResponse.getCount();
				List<T> extraObjects = extraObjectsResponse.getObjects();
				boolean isMoreItems = (start + extraObjects.size()) < extraObjectsCount;
				if (!extraObjects.isEmpty()) {
					if (lastLoaderItemId != null) {
						adaptee.removeItem(lastLoaderItemId);
					}
					for (T extraObject : extraObjects) {
						boolean matchingItemId = itemId.equals(extraObject);
						addItem(extraObject, parentId);
						if (matchingItemId) {
							match = adaptee.getItem(extraObject);
						}
					}
					if (!isMoreItems) {
						break loop1;
					} else {
						int nextLoadedIndex = start + extraObjects.size();
						addLoaderItem(parentId, nextLoadedIndex);
					}
				} else {
					break loop1;
				}
			}
		}
		if (match != null) {
//			adaptee.expandItem(itemId);
			adaptee.setCollapsed(itemId, false);
			adjustLoaderAfterExpand(itemId);
		}
		return match;
	}

	public void setContextMenu(ContextMenu contextMenu) {
		adaptee.setContextMenu(contextMenu);
	}

	public void setDragMode(TreeDragMode dragMode) {
		adaptee.setDragMode(dragMode);
	}

	public void setDropHandler(DropHandler dropHandler) {
		adaptee.setDropHandler(dropHandler);
	}

	protected abstract class BaseTreeComponentHelper {

		protected final TablePropertyCache cellProperties = new TablePropertyCache();

		@SuppressWarnings({"rawtypes", "unchecked"})
		private Collection getContainerPropertyIds() {
			Collection propertyIds = LazyTree.this.getContainerPropertyIds();
			if (propertyIds == null) {
				propertyIds = new ArrayList<>();
			}
			propertyIds.add(CAPTION_PROPERTY);
			propertyIds.add(LOADER_NEXT_LOADED_INDEX_ID);
			propertyIds.add(LOADER_PARENT_ITEM_ID);
			return propertyIds;
		}

		private void containerItemSetChange(Container.ItemSetChangeEvent event) {
			cellProperties.clear();
			defaultContainerItemSetChange(event);
		}

		private final Property<?> getContainerProperty(Object itemId, Object propertyId) {
			Property<?> containerProperty;
			CellKey cellKey = getCellKey(itemId, propertyId);
			if (cellKey != null) {
				containerProperty = cellProperties.get(cellKey);
				if (containerProperty == null) {
					containerProperty = loadContainerProperty(itemId, propertyId);
					cellProperties.put(cellKey, containerProperty);
				}
			} else {
				containerProperty = loadContainerProperty(itemId, propertyId);
			}
			return containerProperty;
		}

		@SuppressWarnings("unchecked")
		private Property<?> loadContainerProperty(Object itemId, Object propertyId) {
			Property<?> property;
			if (isLoader(itemId)) {
				Object propertyValue;
				if (CAPTION_PROPERTY.equals(propertyId)) {
					String itemCaption = getItemCaption(itemId);
					propertyValue = itemCaption;
				} else if (LOADER_NEXT_LOADED_INDEX_ID.equals(propertyId)) {
					propertyValue = getNextLoadedIndexForLoader(itemId);
				} else if (LOADER_PARENT_ITEM_ID.equals(propertyId)) {
					propertyValue = ((List<Object>) loaderPropertyCache.get(itemId)).get(1);
				} else {
					propertyValue = "UNKNOWN";
				}
				property = new ObjectProperty<>(propertyValue);
			} else if (CAPTION_PROPERTY.equals(propertyId)) {
				Component itemCaptionComponent = getItemCaptionComponent((T) itemId);
				if (itemCaptionComponent != null) {
					property = new ObjectProperty<>(itemCaptionComponent);
				} else {
					String itemCaption = getItemCaption(itemId);
					property = new ObjectProperty<>(itemCaption);
				}
			} else {
				property = LazyTree.this.getContainerProperty(itemId, propertyId);
				if (property == null) {
					property = defaultGetContainerProperty(itemId, propertyId);
				}
			}
			return property;
		}

		@SuppressWarnings("unchecked")
		private String getItemCaption(Object itemId) {
			String itemCaption;
			if (isLoader(itemId)) {
				itemCaption = getLoaderItemCaption(itemId);
			} else {
				itemCaption = LazyTree.this.getItemCaption((T) itemId);
				if (itemCaption == null) {
					itemCaption = defaultGetItemCaption(itemId);
				}
			}
			return itemCaption;
		}

		@SuppressWarnings("unchecked")
		private Resource getItemIcon(Object itemId) {
			Resource itemIcon;
			if (isLoader(itemId)) {
				itemIcon = null;
			} else if (!multiValue) {
				itemIcon = LazyTree.this.getItemIcon((T) itemId);
				if (itemIcon == null) {
					itemIcon = defaultGetItemIcon(itemId);
				}
			} else {
				itemIcon = null;
			}
			return itemIcon;
		}

		protected abstract void defaultContainerItemSetChange(ItemSetChangeEvent event);

		protected abstract Property<?> defaultGetContainerProperty(Object itemId, Object propertyId);

		protected abstract String defaultGetItemCaption(Object itemId);

		protected abstract Resource defaultGetItemIcon(Object itemId);
	}

	private interface BaseTreeComponent {

		void setItemDescriptionGenerator(ItemDescriptionGenerator itemDescriptionGenerator);

		Collection<?> getChildren(Object parentId);

		Collection<?> rootItemIds();

		Item getItem(Object itemId);

		boolean isCollapsed(Object itemId);

		ItemDescriptionGenerator getItemDescriptionGenerator();

		Object getItemIconPropertyId();

		Object getItemCaptionPropertyId();

		ItemCaptionMode getItemCaptionMode();

		void setItemIconPropertyId(Object propertyId);

		void setItemCaptionPropertyId(Object propertyId);

		void setItemCaptionMode(ItemCaptionMode mode);

		void setItemCaption(Object itemId, String caption);

		void removeCollapseListener(CollapseListener listener);

		void removeExpandListener(ExpandListener listener);

		boolean removeContainerProperty(Object propertyId);

		void removeExtension(Extension extension);

		void removeDetachListener(DetachListener listener);

		void removeAttachListener(AttachListener listener);

		void addDetachListener(DetachListener listener);

		void addAttachListener(AttachListener listener);

		boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed);

		boolean setParent(Object itemId, Object parent);

		Item addItem(Object itemId);

		void addCollapseListener(CollapseListener collapseListener);

		void addExpandListener(ExpandListener expandListener);

		void setCollapsed(Object itemId, boolean b);

		boolean removeItem(Object itemId);

		void addItemClickListener(ItemClickListener itemClickListener);

		void addStyleName(String treetableBorderless);

		void setPageLength(int i);

		void setWidth(String string);

		void setVisibleColumns(Object... visibleColumns);

		boolean addContainerProperty(Object propertyId, Class<?> class1, Object object);

		void setDropHandler(DropHandler dropHandler);

		void setDragMode(TreeDragMode dragMode);

		void setContextMenu(ContextMenu contextMenu);

	}

	private class TreeComponent extends Tree implements BaseTreeComponent {

		private BaseTreeComponentHelper baseTreeComponentHelper;

		private TreeComponent() {
			baseTreeComponentHelper = new BaseTreeComponentHelper() {
				@Override
				protected void defaultContainerItemSetChange(ItemSetChangeEvent event) {
					TreeComponent.super.containerItemSetChange(event);
				}

				@Override
				protected Property<?> defaultGetContainerProperty(Object itemId, Object propertyId) {
					return TreeComponent.super.getContainerProperty(itemId, propertyId);
				}

				@Override
				protected String defaultGetItemCaption(Object itemId) {
					return TreeComponent.super.getItemCaption(itemId);
				}

				@Override
				protected Resource defaultGetItemIcon(Object itemId) {
					return TreeComponent.super.getItemIcon(itemId);
				}
			};
		}

		@Override
		public boolean isCollapsed(Object itemId) {
			return !isExpanded(itemId);
		}

		@Override
		public void setCollapsed(Object itemId, boolean collapsed) {
			if (collapsed) {
				super.collapseItem(itemId);
			} else {
				super.expandItem(itemId);
			}
		}

		@Override
		public void setPageLength(int i) {
			// Ignored
		}

		@Override
		public void setVisibleColumns(Object... visibleColumns) {
			// Ignored
		}

		@Override
		public void setContextMenu(ContextMenu contextMenu) {
			contextMenu.setAsTreeContextMenu(this);
		}

		@Override
		@SuppressWarnings({"rawtypes", "unchecked"})
		public Collection getContainerPropertyIds() {
			return baseTreeComponentHelper.getContainerPropertyIds();
		}

		@Override
		public void containerItemSetChange(Container.ItemSetChangeEvent event) {
			baseTreeComponentHelper.containerItemSetChange(event);
		}

		@Override
		public final Property<?> getContainerProperty(Object itemId, Object propertyId) {
			return baseTreeComponentHelper.getContainerProperty(itemId, propertyId);
		}

		@Override
		public String getItemCaption(Object itemId) {
			return baseTreeComponentHelper.getItemCaption(itemId);
		}

		@Override
		public Resource getItemIcon(Object itemId) {
			return baseTreeComponentHelper.getItemIcon(itemId);
		}

	}

	private class TreeTableComponent extends TreeTable implements BaseTreeComponent {

		private BaseTreeComponentHelper baseTreeComponentHelper;

		private TreeTableComponent() {
			setWidth("100%");
			baseTreeComponentHelper = new BaseTreeComponentHelper() {
				@Override
				protected void defaultContainerItemSetChange(ItemSetChangeEvent event) {
					TreeTableComponent.super.containerItemSetChange(event);
				}

				@Override
				protected Property<?> defaultGetContainerProperty(Object itemId, Object propertyId) {
					return TreeTableComponent.super.getContainerProperty(itemId, propertyId);
				}

				@Override
				protected String defaultGetItemCaption(Object itemId) {
					return TreeTableComponent.super.getItemCaption(itemId);
				}

				@Override
				protected Resource defaultGetItemIcon(Object itemId) {
					return TreeTableComponent.super.getItemIcon(itemId);
				}
			};
		}

		@Override
		public void setContextMenu(ContextMenu contextMenu) {
			contextMenu.setAsTableContextMenu(this);
		}

		@Override
		public void setDragMode(TreeDragMode dragMode) {
			TableDragMode tableDragMode;
			if (dragMode == TreeDragMode.NONE) {
				tableDragMode = TableDragMode.NONE;
			} else {
				tableDragMode = TableDragMode.ROW;
			}
			setDragMode(tableDragMode);
		}

		@Override
		@SuppressWarnings({"rawtypes", "unchecked"})
		public Collection getContainerPropertyIds() {
			return baseTreeComponentHelper != null ? baseTreeComponentHelper.getContainerPropertyIds() : new ArrayList<>();
		}

		@Override
		public void containerItemSetChange(Container.ItemSetChangeEvent event) {
			baseTreeComponentHelper.containerItemSetChange(event);
		}

		@Override
		public final Property<?> getContainerProperty(Object itemId, Object propertyId) {
			return baseTreeComponentHelper.getContainerProperty(itemId, propertyId);
		}

		@Override
		public String getItemCaption(Object itemId) {
			return baseTreeComponentHelper.getItemCaption(itemId);
		}

		@Override
		public Resource getItemIcon(Object itemId) {
			return baseTreeComponentHelper.getItemIcon(itemId);
		}

	}

}
