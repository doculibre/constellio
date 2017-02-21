package com.constellio.app.ui.framework.containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
import com.vaadin.data.util.filter.UnsupportedFilterException;

@SuppressWarnings("serial")
public class ContainerAdapter<T extends Container & Indexed & Sortable> extends AbstractContainer
		implements Indexed, Sortable, Filterable, PropertySetChangeNotifier, ValueChangeNotifier, ItemSetChangeNotifier {

	protected T adapted;

	public ContainerAdapter(T adapted) {
		this.adapted = adapted;
	}

	public T getNestedContainer() {
		return adapted;
	}
	
	@Override
	public Collection<?> getContainerPropertyIds() {
		List<Object> propertyIds = new ArrayList<>();
		Collection<?> adaptedPropertyIds = adapted.getContainerPropertyIds();
		Collection<?> ownPropertyIds = getOwnContainerPropertyIds();
		propertyIds.addAll(adaptedPropertyIds);
		propertyIds.addAll(ownPropertyIds);
		return propertyIds;
	}

	@Override
	public Property<?> getContainerProperty(Object itemId, Object propertyId) {
		Property<?> ownProperty = getOwnContainerProperty(itemId, propertyId);
		return ownProperty != null ? ownProperty : adapted.getContainerProperty(itemId, propertyId);
	}

	@Override
	public Class<?> getType(Object propertyId) {
		Class<?> ownType = getOwnType(propertyId);
		return ownType != null ? ownType : adapted.getType(propertyId);
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
     *             {@link #addPropertySetChangeListener(com.vaadin.data.Container.PropertySetChangeListener)}
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
     *             {@link #removePropertySetChangeListener(com.vaadin.data.Container.PropertySetChangeListener)}
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
     *             {@link #addItemSetChangeListener(com.vaadin.data.Container.ItemSetChangeListener)}
     **/
    @Deprecated
    @Override
    public void addListener(Container.ItemSetChangeListener listener) {
		if (adapted instanceof ItemSetChangeNotifier) {
			((ItemSetChangeNotifier) adapted).addListener(listener);
		}
    }

    @Override
    public void addItemSetChangeListener(
            Container.ItemSetChangeListener listener) {
		if (adapted instanceof ItemSetChangeNotifier) {
			((ItemSetChangeNotifier) adapted).addItemSetChangeListener(listener);
		}
    }

    @Override
    public void removeItemSetChangeListener(
            Container.ItemSetChangeListener listener) {
		if (adapted instanceof ItemSetChangeNotifier) {
			((ItemSetChangeNotifier) adapted).removeItemSetChangeListener(listener);
		}
    }

    /**
     * @deprecated As of 7.0, replaced by
     *             {@link #removeItemSetChangeListener(com.vaadin.data.Container.ItemSetChangeListener)}
     **/
    @Deprecated
    @Override
    public void removeListener(Container.ItemSetChangeListener listener) {
		if (adapted instanceof ItemSetChangeNotifier) {
			((ItemSetChangeNotifier) adapted).removeListener(listener);
		}
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

	@Override
	public void fireItemSetChange() {
		super.fireItemSetChange();
	}
	
}
