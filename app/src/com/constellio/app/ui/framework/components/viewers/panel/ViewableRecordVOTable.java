package com.constellio.app.ui.framework.components.viewers.panel;

import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.ContainerAdapter;
import com.constellio.app.ui.framework.containers.SearchResultContainer;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.MenuBar;

import java.util.Collection;
import java.util.List;

public class ViewableRecordVOTable extends RecordVOTable {
	
//	private int uncompressedPageLength;
	
	private boolean compressed;

	public ViewableRecordVOTable(ViewableRecordVOContainer dataSource) {
		super();
		removeMenuBarColumn();
		setContainerDataSource(dataSource);
		init();
	}

	public ViewableRecordVOTable(ContainerAdapter<ViewableRecordVOContainer> dataSource) {
		super(dataSource);
		removeMenuBarColumn();
		setContainerDataSource(dataSource);
		init();
	}
	
	private void init() {
		setColumnHeader(SearchResultContainer.THUMBNAIL_PROPERTY, "");
	}

	public boolean isCompressed() {
		return compressed;
	}
	
	private ViewableRecordVOContainer getViewableRecordVOContainer() {
		ViewableRecordVOContainer result = null;
		Container dataSource = getContainerDataSource();
		if (dataSource instanceof ContainerAdapter) {
			ContainerAdapter<?> currentAdapter = (ContainerAdapter<?>) dataSource;
			while (result == null && currentAdapter != null) {
				Container nestedContainer = currentAdapter.getNestedContainer();
				if (nestedContainer instanceof ViewableRecordVOContainer) {
					result = (ViewableRecordVOContainer) nestedContainer;
				} else if (nestedContainer instanceof ContainerAdapter) {
					currentAdapter = (ContainerAdapter<?>) nestedContainer;
				} else {
					currentAdapter = null;
				}
			}
		} else if (dataSource instanceof ViewableRecordVOContainer) {
			result = (ViewableRecordVOContainer) dataSource;
		} 
		return result;
	}

	public void setCompressed(boolean compressed) {
		boolean compressedChanged = this.compressed != compressed;
		this.compressed = compressed;
		
		if (compressedChanged) {
			ViewableRecordVOContainer container = getViewableRecordVOContainer();
			container.setCompressed(compressed);
			
			manageColumns(getTableId());
			
			if (compressed) {
//				uncompressedPageLength = getPageLength();
//				setPageLength(25);
			} else {
//				setPageLength(uncompressedPageLength);
			}
		}
	}

	@Override
	protected TableColumnsManager newColumnsManager() {
		return new ViewableRecordVOTableColumnsManager();
	}

	@Override
	protected Property<?> loadContainerProperty(final Object itemId, final Object propertyId) {
		Property<?> property = super.loadContainerProperty(itemId, propertyId);
		if (SearchResultContainer.SEARCH_RESULT_PROPERTY.equals(propertyId)) {
			Object propertyValue = property.getValue();
			if (propertyValue instanceof AbstractOrderedLayout) {
				AbstractOrderedLayout layout = (AbstractOrderedLayout) propertyValue;
				layout.addLayoutClickListener(new LayoutClickListener() {
					@Override
					public void layoutClick(LayoutClickEvent event) {
						if (!(event.getSource() instanceof MenuBar)) {
							Collection<?> itemClickListeners = getListeners(ItemClickEvent.class); 
							MouseEventDetails mouseEventDetails = new MouseEventDetails();
							mouseEventDetails.setButton(event.getButton());
							mouseEventDetails.setClientX(event.getClientX());
							mouseEventDetails.setClientY(event.getClientY());
							mouseEventDetails.setRelativeX(event.getRelativeX());
							mouseEventDetails.setRelativeY(event.getRelativeY());
							Item item = getItem(itemId);
							for (Object itemClickListenerObj : itemClickListeners) {
								ItemClickListener itemClickListener = (ItemClickListener) itemClickListenerObj;
								itemClickListener.itemClick(new ItemClickEvent(ViewableRecordVOTable.this, item, itemId, propertyId, mouseEventDetails));
							}
						}
					}
				});
				
				List<Button> buttons = ComponentTreeUtils.getChildren(layout, Button.class);
				for (Button button : buttons) {
					button.addClickListener(new Button.ClickListener() {
						@Override
						public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
							MouseEventDetails mouseEventDetails = new MouseEventDetails();
							mouseEventDetails.setButton(MouseButton.LEFT);
							mouseEventDetails.setClientX(event.getClientX());
							mouseEventDetails.setClientY(event.getClientY());
							mouseEventDetails.setRelativeX(event.getRelativeX());
							mouseEventDetails.setRelativeY(event.getRelativeY());
							
							Item item = getItem(itemId);
							Collection<?> itemClickListeners = getListeners(ItemClickEvent.class);
							for (Object itemClickListenerObj : itemClickListeners) {
								ItemClickListener itemClickListener = (ItemClickListener) itemClickListenerObj;
								itemClickListener.itemClick(new ItemClickEvent(ViewableRecordVOTable.this, item, itemId, propertyId, mouseEventDetails));
							}
						}
					});
				}
				property = new ObjectProperty<>(layout);
			} 
		} else if (SearchResultContainer.THUMBNAIL_PROPERTY.equals(propertyId)) {
			Object propertyValue = property.getValue();
			if (propertyValue instanceof Image) {
				Image image = (Image) propertyValue;
				image.addClickListener(new ClickListener() {
					@Override
					public void click(ClickEvent event) {
						Collection<?> itemClickListeners = getListeners(ItemClickEvent.class); 
						MouseEventDetails mouseEventDetails = new MouseEventDetails();
						mouseEventDetails.setButton(event.getButton());
						mouseEventDetails.setClientX(event.getClientX());
						mouseEventDetails.setClientY(event.getClientY());
						mouseEventDetails.setRelativeX(event.getRelativeX());
						mouseEventDetails.setRelativeY(event.getRelativeY());
						Item item = getItem(itemId);
						for (Object itemClickListenerObj : itemClickListeners) {
							ItemClickListener itemClickListener = (ItemClickListener) itemClickListenerObj;
							itemClickListener.itemClick(new ItemClickEvent(ViewableRecordVOTable.this, item, itemId, propertyId, mouseEventDetails));
						}
					}
				});
				property = new ObjectProperty<>(image);
			}
		}
		return property;
	}

}
