package com.constellio.app.ui.framework.containers;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Container.ItemSetChangeNotifier;
import com.vaadin.data.Container.PropertySetChangeNotifier;
import com.vaadin.data.Container.Sortable;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Property.ValueChangeNotifier;
import com.vaadin.data.util.AbstractContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.filter.UnsupportedFilterException;
import com.vaadin.ui.Label;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class ContainerAdapter<T extends Container & Indexed & Sortable> extends AbstractContainer
		implements Indexed, Sortable, Filterable, PropertySetChangeNotifier, ValueChangeNotifier, ItemSetChangeNotifier, RefreshableContainer {
	
	public static final String INDEX_PROPERTY_ID = "index";

	protected T adapted;
	private boolean indexProperty;

	private List<Container.ItemSetChangeListener> itemSetChangeListeners = new ArrayList<>();

	public ContainerAdapter(T adapted) {
		this(adapted, false);
	}

	public ContainerAdapter(T adapted, boolean indexProperty) {
		this.adapted = adapted;
		this.indexProperty = indexProperty;
	}

	public T getNestedContainer() {
		return adapted;
	}

	@SuppressWarnings("unchecked")
	public T getNestedContainerRecursive() {
		T result = adapted;
		while (result instanceof ContainerAdapter) {
			result = ((ContainerAdapter<T>) result).getNestedContainer();
		}
		return result;
	}

	@Override
	public Collection<?> getContainerPropertyIds() {
		List<Object> propertyIds = new ArrayList<>();
		Collection<?> adaptedPropertyIds = adapted.getContainerPropertyIds();
		Collection<?> ownPropertyIds = getOwnContainerPropertyIds();
		if (indexProperty) {
			propertyIds.add(INDEX_PROPERTY_ID);
		}
		propertyIds.addAll(adaptedPropertyIds);
		propertyIds.addAll(ownPropertyIds);
		return propertyIds;
	}

	@Override
	public Property<?> getContainerProperty(Object itemId, Object propertyId) {
		Property<?> result;
		if (INDEX_PROPERTY_ID.equals(propertyId)) {
			int index = indexOfId(itemId) + 1;
			Label label = new Label("" + index);
			label.addStyleName("row-index");
			result = new ObjectProperty<>(label);
		} else {
			Property<?> ownProperty = getOwnContainerProperty(itemId, propertyId);
			result = ownProperty != null ? ownProperty : adapted.getContainerProperty(itemId, propertyId);
		}
		return result;
	}

	@Override
	public Class<?> getType(Object propertyId) {
		Class<?> result;
		if (INDEX_PROPERTY_ID.equals(propertyId)) {
			result = Label.class;
		} else {
			Class<?> ownType = getOwnType(propertyId);
			result = ownType != null ? ownType : adapted.getType(propertyId);
		}
		return result;
	}

	@Override
	public Object nextItemId(Object itemId) {
		return adapted.nextItemId(itemId);
	}

	@Override
	public Object prevItemId(Object itemId) {
		return adapted.prevItemId(itemId);
	}

	@Override
	public Object firstItemId() {
		return adapted.firstItemId();
	}

	@Override
	public Object lastItemId() {
		return adapted.lastItemId();
	}

	@Override
	public boolean isFirstId(Object itemId) {
		return adapted.isFirstId(itemId);
	}

	@Override
	public boolean isLastId(Object itemId) {
		return adapted.isLastId(itemId);
	}

	@Override
	public Object addItemAfter(Object previousItemId)
			throws UnsupportedOperationException {
		return adapted.addItemAfter(previousItemId);
	}

	@Override
	public Item addItemAfter(Object previousItemId, Object newItemId)
			throws UnsupportedOperationException {
		return adapted.addItemAfter(previousItemId, newItemId);
	}

	@Override
	public Item getItem(Object itemId) {
		return adapted.getItem(itemId);
	}

	@Override
	public Collection<?> getItemIds() {
		return adapted.getItemIds();
	}

	@Override
	public int size() {
		return adapted.size();
	}

	@Override
	public boolean containsId(Object itemId) {
		return adapted.containsId(itemId);
	}

	@Override
	public Item addItem(Object itemId)
			throws UnsupportedOperationException {
		return adapted.addItem(itemId);
	}

	@Override
	public Object addItem()
			throws UnsupportedOperationException {
		return adapted.addItem();
	}

	@Override
	public boolean removeItem(Object itemId)
			throws UnsupportedOperationException {
		return adapted.removeItem(itemId);
	}

	@Override
	public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue)
			throws UnsupportedOperationException {
		return adapted.addContainerProperty(propertyId, type, defaultValue);
	}

	@Override
	public boolean removeContainerProperty(Object propertyId)
			throws UnsupportedOperationException {
		return adapted.removeContainerProperty(propertyId);
	}

	@Override
	public boolean removeAllItems()
			throws UnsupportedOperationException {
		return adapted.removeAllItems();
	}

	@Override
	public int indexOfId(Object itemId) {
		return adapted.indexOfId(itemId);
	}

	@Override
	public Object getIdByIndex(int index) {
		return adapted.getIdByIndex(index);
	}

	@Override
	public List<?> getItemIds(int startIndex, int numberOfItems) {
		return adapted.getItemIds(startIndex, numberOfItems);
	}

	@Override
	public Object addItemAt(int index)
			throws UnsupportedOperationException {
		return adapted.addItemAt(index);
	}

	@Override
	public Item addItemAt(int index, Object newItemId)
			throws UnsupportedOperationException {
		return adapted.addItemAt(index, newItemId);
	}

	@Override
	public void sort(Object[] propertyId, boolean[] ascending) {
		adapted.sort(propertyId, ascending);
	}

	@Override
	public Collection<?> getSortableContainerPropertyIds() {
		return adapted.getSortableContainerPropertyIds();
	}

	@Override
	public void addValueChangeListener(ValueChangeListener listener) {
		if (adapted instanceof ValueChangeNotifier) {
			((ValueChangeNotifier) adapted).addValueChangeListener(listener);
		}
	}

	@Deprecated
	@Override
	public void addListener(ValueChangeListener listener) {
		if (adapted instanceof ValueChangeNotifier) {
			((ValueChangeNotifier) adapted).addListener(listener);
		}
	}

	@Override
	public void removeValueChangeListener(ValueChangeListener listener) {
		if (adapted instanceof ValueChangeNotifier) {
			((ValueChangeNotifier) adapted).removeValueChangeListener(listener);
		}
	}

	@Deprecated
	@Override
	public void removeListener(ValueChangeListener listener) {
		if (adapted instanceof ValueChangeNotifier) {
			((ValueChangeNotifier) adapted).removeListener(listener);
		}
	}

	@Override
	public void addPropertySetChangeListener(
			Container.PropertySetChangeListener listener) {
		if (adapted instanceof PropertySetChangeNotifier) {
			((PropertySetChangeNotifier) adapted).addPropertySetChangeListener(listener);
		}
	}

	/**
	 * @deprecated As of 7.0, replaced by
	 * {@link #addPropertySetChangeListener(com.vaadin.data.Container.PropertySetChangeListener)}
	 **/
	@Deprecated
	@Override
	public void addListener(Container.PropertySetChangeListener listener) {
		if (adapted instanceof PropertySetChangeNotifier) {
			((PropertySetChangeNotifier) adapted).addListener(listener);
		}
	}

	@Override
	public void removePropertySetChangeListener(
			Container.PropertySetChangeListener listener) {
		if (adapted instanceof PropertySetChangeNotifier) {
			((PropertySetChangeNotifier) adapted).removePropertySetChangeListener(listener);
		}
	}

	/**
	 * @deprecated As of 7.0, replaced by
	 * {@link #removePropertySetChangeListener(com.vaadin.data.Container.PropertySetChangeListener)}
	 **/
	@Deprecated
	@Override
	public void removeListener(Container.PropertySetChangeListener listener) {
		if (adapted instanceof PropertySetChangeNotifier) {
			((PropertySetChangeNotifier) adapted).removeListener(listener);
		}
	}

	// ItemSetChangeNotifier

	/**
	 * @deprecated As of 7.0, replaced by
	 * {@link #addItemSetChangeListener(com.vaadin.data.Container.ItemSetChangeListener)}
	 **/
	@Deprecated
	@Override
	public void addListener(Container.ItemSetChangeListener listener) {
		if (adapted instanceof ItemSetChangeNotifier) {
			((ItemSetChangeNotifier) adapted).addListener(listener);
		}
		itemSetChangeListeners.add(listener);
	}

	@Override
	public void addItemSetChangeListener(
			Container.ItemSetChangeListener listener) {
		if (adapted instanceof ItemSetChangeNotifier) {
			((ItemSetChangeNotifier) adapted).addItemSetChangeListener(listener);
		}
		itemSetChangeListeners.add(listener);
	}

	@Override
	public void removeItemSetChangeListener(
			Container.ItemSetChangeListener listener) {
		if (adapted instanceof ItemSetChangeNotifier) {
			((ItemSetChangeNotifier) adapted).removeItemSetChangeListener(listener);
		}
		itemSetChangeListeners.remove(listener);
	}

	/**
	 * @deprecated As of 7.0, replaced by
	 * {@link #removeItemSetChangeListener(com.vaadin.data.Container.ItemSetChangeListener)}
	 **/
	@Deprecated
	@Override
	public void removeListener(Container.ItemSetChangeListener listener) {
		if (adapted instanceof ItemSetChangeNotifier) {
			((ItemSetChangeNotifier) adapted).removeListener(listener);
		}
		itemSetChangeListeners.remove(listener);
	}

	@Override
	public void addContainerFilter(Filter filter)
			throws UnsupportedFilterException {
		if (adapted instanceof Filterable) {
			((Filterable) adapted).addContainerFilter(filter);
		}
	}

	@Override
	public void removeContainerFilter(Filter filter) {
		if (adapted instanceof Filterable) {
			((Filterable) adapted).removeContainerFilter(filter);
		}
	}

	@Override
	public void removeAllContainerFilters() {
		if (adapted instanceof Filterable) {
			((Filterable) adapted).removeAllContainerFilters();
		}
	}

	@Override
	public Collection<Filter> getContainerFilters() {
		if (adapted instanceof Filterable) {
			return ((Filterable) adapted).getContainerFilters();
		} else {
			return new ArrayList<>();
		}
	}

	protected Collection<?> getOwnContainerPropertyIds() {
		return Collections.emptyList();
	}

	protected Class<?> getOwnType(Object propertyId) {
		return null;
	}

	protected Property<?> getOwnContainerProperty(Object itemId, Object propertyId) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fireItemSetChange() {
		super.fireItemSetChange();
		if (adapted instanceof ContainerAdapter) {
			((ContainerAdapter<T>) adapted).fireItemSetChange();
		} else if (adapted instanceof RefreshableContainer) {
			((RefreshableContainer) adapted).refresh();
		}
	}

	@Override
	protected void fireItemSetChange(ItemSetChangeEvent event) {
		super.fireItemSetChange(event);
		for (ItemSetChangeListener listener : itemSetChangeListeners) {
			listener.containerItemSetChange(event);
		}
	}

	@Override
	public void refresh() {
		if (adapted instanceof RefreshableContainer) {
			((RefreshableContainer) adapted).refresh();
		}
	}

}
