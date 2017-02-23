package com.constellio.app.ui.framework.components.table;

import static com.constellio.app.ui.i18n.i18n.$;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public abstract class SelectionTableAdapter extends VerticalLayout {
	
	public static final String SELECT_PROPERTY_ID = "select";
	
	private SelectDeselectAllButton toggleButton;
	
	private ContainerAdapter<?> dataSourceAdapter;
	
	private Map<Object, Property<?>> itemSelectProperties = new HashMap<>();
	
	private Table table;
	
	public SelectionTableAdapter() {
		this(null);
	}
	
	public SelectionTableAdapter(Table table) {
		setSpacing(true);
		setTable(table);
	}
	
	public Table getTable() {
		return table;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setTable(Table table) {
		this.table = table;
		if (table != null) {
			removeAllComponents();
			
			boolean allItemsSelected = isAllItemsSelected();
			toggleButton = new SelectDeselectAllButton($("selectAll"), $("deselectAll"), !allItemsSelected) {
				@Override
				protected void onSelectAll(ClickEvent event) {
					SelectionTableAdapter.this.selectAll();
				}
				
				@Override
				protected void onDeselectAll(ClickEvent event) {
					SelectionTableAdapter.this.deselectAll();
				}
			};
			if (table.getItemIds().isEmpty()) {
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
							Property<?> selectProperty = itemSelectProperties.get(itemId);
							selectProperty = new AbstractProperty<Boolean>() {
								@Override
								public Boolean getValue() {
									return isSelected(itemId);
								}

								@Override
								public void setValue(Boolean newValue)
										throws com.vaadin.data.Property.ReadOnlyException {
									boolean selected = Boolean.TRUE.equals(newValue);
									setSelected(itemId, selected);
								}

								@Override
								public Class<? extends Boolean> getType() {
									return Boolean.class;
								}
							};
							CheckBox checkBox = new CheckBox();
							checkBox.setPropertyDataSource(selectProperty);
							property = new ObjectProperty<CheckBox>(checkBox);
							itemSelectProperties.put(itemId, property);
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
			
			List<Object> newVisibleColumns = new ArrayList<>(oldVisibleColumns);
			if (!newVisibleColumns.contains(SELECT_PROPERTY_ID)) {
				newVisibleColumns.add(0, SELECT_PROPERTY_ID);
			}
			table.setVisibleColumns(newVisibleColumns.toArray(new Object[0]));
			
			setWidth("100%");
			addComponents(toggleButton, table);
			setExpandRatio(table, 1);
			setComponentAlignment(toggleButton, Alignment.TOP_LEFT);
		}
	}

	public SelectDeselectAllButton getToggleButton() {
		return toggleButton;
	}

	public ContainerAdapter<?> getContainerAdapter() {
		return dataSourceAdapter;
	}
	
	private CheckBox getCheckBox(Object itemId) {
		return (CheckBox) dataSourceAdapter.getContainerProperty(itemId, SELECT_PROPERTY_ID).getValue();
	}
	
	private boolean isAllItemsSelected() {
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
	
	public void selectAll() {
		if (table != null) {
			for (Object itemId : table.getItemIds()) {
				getCheckBox(itemId).setValue(true);
			}
		}
	}
	
	public void deselectAll() {
		if (table != null) {
			for (Object itemId : table.getItemIds()) {
				getCheckBox(itemId).setValue(false);
			}
		}
	}
	
	public abstract boolean isSelected(Object itemId);
	
	public abstract void setSelected(Object itemId, boolean selected);

}
