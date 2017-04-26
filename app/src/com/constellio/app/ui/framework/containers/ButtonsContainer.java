package com.constellio.app.ui.framework.containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Container.Sortable;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;

@SuppressWarnings("serial")
public class ButtonsContainer<T extends Container & Indexed & Sortable> extends ContainerAdapter<T> {
	
	public static final String DEFAULT_BUTTONS_PROPERTY_ID = "constellio-buttons-container";

	private String buttonsPropertyId = "constellio-buttons-container";

	private List<ContainerButton> containerButtons = new ArrayList<ContainerButton>();

	/**
	 * @param adaptee Must implement {@link com.vaadin.data.Container.Indexed} and {@link com.vaadin.data.Container.Sortable}
	 */
	public ButtonsContainer(T adaptee) {
		this(adaptee, DEFAULT_BUTTONS_PROPERTY_ID);
	}

	/**
	 * @param adaptee Must implement {@link com.vaadin.data.Container.Indexed} and {@link com.vaadin.data.Container.Sortable}
	 */
	public ButtonsContainer(T adaptee, String buttonsPropertyId) {
		super(adaptee);
		if (buttonsPropertyId == null) {
			throw new IllegalArgumentException("buttonsPropertyId cannot be null");
		}
		this.buttonsPropertyId = buttonsPropertyId;
	}

	public void addButton(ContainerButton button) {
		containerButtons.add(button);
	}

	public void addButton(int index, ContainerButton button) {
		containerButtons.add(index, button);
	}

	public ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	@Override
	protected Collection<?> getOwnContainerPropertyIds() {
		return Arrays.asList(buttonsPropertyId);
	}

	@Override
	protected Property<?> getOwnContainerProperty(Object itemId, Object propertyId) {
		return buttonsPropertyId.equals(propertyId) ? newButtonsLayout(itemId) : null;
	}

	@Override
	protected Class<?> getOwnType(Object propertyId) {
		return buttonsPropertyId.equals(propertyId) ? HorizontalLayout.class : null;
	}
	
	public List<Button> getButtons(Object itemId) {
		HorizontalLayout buttonsLayout = (HorizontalLayout) getContainerProperty(itemId, buttonsPropertyId).getValue();
		return ComponentTreeUtils.getChildren(buttonsLayout, Button.class);
	}

	private Property<?> newButtonsLayout(final Object itemId) {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.addStyleName("buttons-container");

		for (ContainerButton containerButton : containerButtons) {
			Button button = containerButton.newButton(itemId, this);
			button.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					for (ContainerButtonListener containerButtonListener : getConstellioFactories()
							.getAppLayerFactory().getContainerButtonListeners()) {
						containerButtonListener.buttonClick(event, itemId);
					}
				}
			});
			horizontalLayout.addComponent(button);
		}
		return new ObjectProperty<HorizontalLayout>(horizontalLayout, HorizontalLayout.class, true);
	}

	public static abstract class ContainerButton implements Serializable {

		// TODO Merge with newButtonInstance
		public final Button newButton(final Object itemId, ButtonsContainer<?> container) {
			Button button = newButtonInstance(itemId, container);
			return button;
		}

		protected abstract Button newButtonInstance(Object itemId, ButtonsContainer<?> container);

	}
	
}
