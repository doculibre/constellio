package com.constellio.app.ui.framework.components.table;

import com.constellio.app.ui.framework.buttons.SelectDeselectAllButton;
import com.constellio.app.ui.framework.containers.ContainerAdapter;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class SelectionTableAdapter extends VerticalLayout {

	public static final String SELECT_PROPERTY_ID = "select";

	protected SelectDeselectAllButton toggleButton;

	protected ContainerAdapter<?> dataSourceAdapter;

	protected Map<Object, Property<?>> itemSelectProperties = new HashMap<>();

	protected Table table;

	public SelectionTableAdapter() {
		this(null);
	}

	public SelectionTableAdapter(Table table) {
		addStyleName("selection-table");
		setSpacing(true);
		setTable(table);
	}

	public Table getTable() {
		return table;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public void setTable(Table table) {
		this.table = table;
		if (table != null) {
			removeAllComponents();

			boolean allItemsSelected = isAllItemsSelected();
			toggleButton = new SelectDeselectAllButton($("selectAll"), $("deselectAll"), !allItemsSelected) {
				@Override
				protected void onSelectAll(ClickEvent event) {
					SelectionTableAdapter.this.selectAll();
					updateVisibleCheckBoxes();
				}

				@Override
				protected void onDeselectAll(ClickEvent event) {
					SelectionTableAdapter.this.deselectAll();
					updateVisibleCheckBoxes();
				}

				@Override
				protected void buttonClickCallBack(boolean selectAllMode) {
				}
			};
			toggleButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
			toggleButton.addStyleName(ValoTheme.BUTTON_LINK);
			if (table.size() == 0) {
				toggleButton.setVisible(false);
			}

			Container tableDataSource = table.getContainerDataSource();
			dataSourceAdapter = new ContainerAdapter(tableDataSource) {
				@Override
				public Collection<?> getContainerPropertyIds() {
					List<Object> propertyIds = new ArrayList<>(super.getContainerPropertyIds());
					propertyIds.add(0, SELECT_PROPERTY_ID);
					return propertyIds;
				}

				@Override
				public Property getContainerProperty(final Object itemId, Object propertyId) {
					Property<?> property;
					if (SELECT_PROPERTY_ID.equals(propertyId)) {
						property = itemSelectProperties.get(itemId);
						if (property == null) {
							Property<?> selectProperty = new AbstractProperty<Boolean>() {
								@Override
								public Boolean getValue() {
									return itemId != null ? isSelected(itemId) : null;
								}

								@Override
								public void setValue(Boolean newValue)
										throws com.vaadin.data.Property.ReadOnlyException {
									if (itemId != null) {
										boolean selected = Boolean.TRUE.equals(newValue);
										setSelected(itemId, selected);
									}
								}

								@Override
								public Class<? extends Boolean> getType() {
									return Boolean.class;
								}
							};
							CheckBox checkBox = new SelectionCheckBox();
							checkBox.setPropertyDataSource(selectProperty);
							checkBox.setImmediate(true);
							property = new ObjectProperty<CheckBox>(checkBox);
							itemSelectProperties.put(itemId, property);
						} else {
							SelectionCheckBox checkBox = (SelectionCheckBox) property.getValue();
							if (checkBox != null) {
								checkBox.setInternalValue(isSelected(itemId));
							}
						}
					} else {
						property = super.getContainerProperty(itemId, propertyId);
					}
					return property;
				}

				@Override
				public Class getType(Object propertyId) {
					Class<?> propertyType;
					if (SELECT_PROPERTY_ID.equals(propertyId)) {
						propertyType = CheckBox.class;
					} else {
						propertyType = super.getType(propertyId);
					}
					return propertyType;
				}
			};

			List<Object> oldVisibleColumns = Arrays.asList(table.getVisibleColumns());
			table.setContainerDataSource(dataSourceAdapter);
			table.setColumnHeader(SELECT_PROPERTY_ID, "");
			table.setColumnWidth(SELECT_PROPERTY_ID, 44);
			table.setColumnCollapsible(SELECT_PROPERTY_ID, false);

			if (!oldVisibleColumns.contains(SELECT_PROPERTY_ID)) {
				List<Object> newVisibleColumns = new ArrayList<>(oldVisibleColumns);
				newVisibleColumns.add(0, SELECT_PROPERTY_ID);
				table.setVisibleColumns(newVisibleColumns.toArray(new Object[0]));
			}

			setWidth("100%");
			addComponents(toggleButton, table);
			setExpandRatio(table, 1);
			setComponentAlignment(toggleButton, Alignment.TOP_LEFT);
		}
	}

	protected void updateVisibleCheckBoxes() {
		for (Object visibleItemId : SelectionTableAdapter.this.table.getVisibleItemIds()) {
			setChecked(visibleItemId, isSelected(visibleItemId));
		}
	}

	public void refresh() {
		dataSourceAdapter.fireItemSetChange();
	}

	public SelectDeselectAllButton getToggleButton() {
		return toggleButton;
	}

	public ContainerAdapter<?> getContainerAdapter() {
		return dataSourceAdapter;
	}

	private SelectionCheckBox getCheckBox(Object itemId) {
		return (SelectionCheckBox) dataSourceAdapter.getContainerProperty(itemId, SELECT_PROPERTY_ID).getValue();
	}

	protected boolean isChecked(Object itemId) {
		SelectionCheckBox checkBox = getCheckBox(itemId);
		return checkBox != null ? checkBox.getInternalValue() : null;
	}

	protected void setChecked(Object itemId, boolean checked) {
		SelectionCheckBox checkBox = getCheckBox(itemId);
		if (checkBox != null) {
			checkBox.setInternalValue(checked);
		}
		adjustSelectAllButton(checked);
	}

	//	public void adjustSelectAllButton() {
	//		adjustSelectAllButton(toggleButton.isSelectAllMode());
	//	}

	public void adjustSelectAllButton(boolean checked) {
		//		boolean selectAllMode = toggleButton.isSelectAllMode();
		//		if (checked && selectAllMode && isAllItemsSelected()) {
		//			toggleButton.setSelectAllMode(false);
		//		} else if (!checked && !selectAllMode && isAllItemsUnselected()) {
		//			toggleButton.setSelectAllMode(true);
		//		}
	}

	protected boolean isAllItemsSelectedByItemId() {
		boolean allItemsSelected;
		if (table != null) {
			Collection<?> itemIds = table.getItemIds();
			allItemsSelected = !itemIds.isEmpty();
			for (Object itemId : itemIds) {
				if (!isSelected(itemId)) {
					allItemsSelected = false;
					break;
				}
			}
		} else {
			allItemsSelected = false;
		}
		return allItemsSelected;
	}

	protected boolean isAllItemsDeselectedByItemId() {
		boolean allItemsUnselected;
		if (table != null) {
			Collection<?> itemIds = table.getItemIds();
			allItemsUnselected = !itemIds.isEmpty();
			for (Object itemId : itemIds) {
				if (isSelected(itemId)) {
					allItemsUnselected = false;
					break;
				}
			}
		} else {
			allItemsUnselected = false;
		}
		return allItemsUnselected;
	}

	public void selectAllByItemId() {
		if (table != null) {
			for (Object itemId : table.getItemIds()) {
				CheckBox checkBox = getCheckBox(itemId);
				if (checkBox != null && !Boolean.TRUE.equals(checkBox.getValue())) {
					checkBox.setValue(true);
				}
			}
		}
	}

	public void deselectAllByItemId() {
		if (table != null) {
			for (Object itemId : table.getItemIds()) {
				CheckBox checkBox = getCheckBox(itemId);
				if (checkBox != null && !Boolean.FALSE.equals(checkBox.getValue())) {
					checkBox.setValue(false);
				}
			}
		}
	}

	public abstract void selectAll();

	public abstract void deselectAll();

	public abstract boolean isAllItemsSelected();

	public abstract boolean isAllItemsDeselected();

	public abstract boolean isSelected(Object itemId);

	public abstract void setSelected(Object itemId, boolean selected);

	private static class SelectionCheckBox extends CheckBox {

		@Override
		public Boolean getInternalValue() {
			return super.getInternalValue();
		}

		@Override
		public void setInternalValue(Boolean newValue) {
			super.setInternalValue(newValue);
		}

	}

	public void refreshUI() {
		dataSourceAdapter.fireItemSetChange();
	}
}
