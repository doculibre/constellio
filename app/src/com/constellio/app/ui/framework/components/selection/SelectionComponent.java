package com.constellio.app.ui.framework.components.selection;

import com.vaadin.ui.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface SelectionComponent {

	public static class SelectionChangeEvent {

		private Component component;

		private List<Object> selectedItemIds;

		private List<Object> deselectedItemIds;

		private boolean allItemsSelected;

		private boolean allItemsDeselected;

		public Component getComponent() {
			return component;
		}

		public void setComponent(Component component) {
			this.component = component;
		}

		public List<Object> getSelectedItemIds() {
			return selectedItemIds;
		}

		public void setSelectedItemIds(List<Object> selectedItemIds) {
			this.selectedItemIds = selectedItemIds;
		}

		@SuppressWarnings("unchecked")
		public void setSelectedItemId(Object selectedItemId) {
			if (selectedItemId instanceof List) {
				setSelectedItemIds((List<Object>) selectedItemId);
			} else if (selectedItemId != null) {
				setSelectedItemIds(Arrays.asList(selectedItemId));
			}
		}

		public List<Object> getDeselectedItemIds() {
			return deselectedItemIds;
		}

		public void setDeselectedItemIds(List<Object> deselectedItemIds) {
			this.deselectedItemIds = deselectedItemIds;
		}

		@SuppressWarnings("unchecked")
		public void setDeselectedItemId(Object deselectedItemId) {
			if (deselectedItemId instanceof List) {
				setDeselectedItemIds((List<Object>) deselectedItemId);
			} else if (deselectedItemId != null) {
				setDeselectedItemIds(Arrays.asList(deselectedItemId));
			}
		}

		public boolean isAllItemsSelected() {
			return allItemsSelected;
		}

		public void setAllItemsSelected(boolean allItemsSelected) {
			this.allItemsSelected = allItemsSelected;
		}

		public boolean isAllItemsDeselected() {
			return allItemsDeselected;
		}

		public void setAllItemsDeselected(boolean allItemsDeselected) {
			this.allItemsDeselected = allItemsDeselected;
		}

	}

	public static interface SelectionChangeListener {

		void selectionChanged(SelectionChangeEvent event);

	}

	public static interface SelectionManager extends SelectionChangeListener {

		List<Object> getAllSelectedItemIds();

		boolean isAllItemsSelected();

		boolean isAllItemsDeselected();

		boolean isSelected(Object itemId);

	}

	public static abstract class ValueSelectionManager implements SelectionManager {

		@SuppressWarnings({"rawtypes", "unchecked"})
		private List<Object> ensureListValue() {
			List<Object> listValue;
			Object objectValue = getValue();
			if (objectValue instanceof List) {
				listValue = (List) objectValue;
			} else {
				listValue = new ArrayList<>();
			}
			return listValue;
		}

		@Override
		public List<Object> getAllSelectedItemIds() {
			List<Object> allSelectedItemIds;
			if (isAllItemsSelected()) {
				allSelectedItemIds = new ArrayList<>(getItemIds());
			} else {
				allSelectedItemIds = ensureListValue();
			}
			return allSelectedItemIds;
		}

		@Override
		public boolean isAllItemsSelected() {
			List<Object> listValue = ensureListValue();
			return listValue.containsAll(getItemIds());
		}

		@Override
		public boolean isAllItemsDeselected() {
			List<Object> listValue = ensureListValue();
			return listValue.isEmpty();
		}

		@Override
		public boolean isSelected(Object itemId) {
			List<Object> listValue = ensureListValue();
			return listValue.contains(itemId);
		}

		@Override
		public void selectionChanged(SelectionChangeEvent event) {
			if (event.isAllItemsSelected()) {
				setValue(getItemIds());
			} else if (event.isAllItemsDeselected()) {
				setValue(new ArrayList<>());
			} else {
				List<Object> selectedItemIds = event.getSelectedItemIds();
				List<Object> deselectedItemIds = event.getDeselectedItemIds();
				List<Object> listValue = ensureListValue();
				if (selectedItemIds != null) {
					for (Object selectedItemId : selectedItemIds) {
						if (!listValue.contains(selectedItemId)) {
							listValue.add(selectedItemId);
						}
					}
				} else if (deselectedItemIds != null) {
					for (Object deselectedItemId : deselectedItemIds) {
						listValue.remove(deselectedItemId);
					}
				}
				setValue(listValue);
			}
		}

		protected abstract Object getValue();

		protected abstract void setValue(Object newValue);

		protected abstract Collection<?> getItemIds();

	}

}
