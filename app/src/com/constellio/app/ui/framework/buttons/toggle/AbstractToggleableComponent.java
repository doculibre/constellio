package com.constellio.app.ui.framework.buttons.toggle;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;

public abstract class AbstractToggleableComponent extends CustomComponent implements Toggleable {

	private boolean toggled;
	private boolean initialized;

	private final ToggleEventObservable toggleToDefaultViewListeners;
	private final ToggleEventObservable toggleToToggledViewListeners;
	private final ToggleEventObservable toggledListeners;

	public AbstractToggleableComponent() {
		this(false);
	}

	public AbstractToggleableComponent(boolean toggled) {
		this.toggled = toggled;

		toggleToDefaultViewListeners = new ToggleEventObservable();
		toggleToToggledViewListeners = new ToggleEventObservable();
		toggledListeners = new ToggleEventObservable();
		initialized = false;
	}

	@Override
	public void toggle() {
		setToggled(!isToggled());
	}

	private Component getDisplayComponent() {
		Component displayComponent;

		if (initialized) {
			displayComponent = toggled ? getToggledView() : getDefaultView();
		} else {
			displayComponent = new CustomComponent();
		}

		return displayComponent;
	}

	@Override
	public void attach() {
		super.attach();

		initComponent();
		initialized = true;

		setCompositionRoot(getDisplayComponent());
	}

	protected abstract void initComponent();

	public void setToggled(boolean toggled) {

		if (toggled != isToggled()) {
			this.toggled = toggled;
			setCompositionRoot(getDisplayComponent());

			ToggleEventArgs toggleEventArgs = new ToggleEventArgs(this);
			if (toggled) {
				toggleToToggledViewListeners.fire(toggleEventArgs);
			} else {
				toggleToDefaultViewListeners.fire(toggleEventArgs);
			}

			toggledListeners.fire(toggleEventArgs);
		}
	}

	@Override
	public boolean isToggled() {
		return toggled;
	}

	@Override
	public void addToggledListener(ToggledListener listener) {
		toggledListeners.addListener(listener);
	}

	@Override
	public void removeToggledListener(ToggledListener listener) {
		toggledListeners.removeListener(listener);
	}

	@Override
	public void addToggleToDefaultViewListener(ToggleToDefaultViewListener listener) {
		toggleToDefaultViewListeners.addListener(listener);
	}

	@Override
	public void removeToggleToDefaultViewListener(ToggleToDefaultViewListener listener) {
		toggleToDefaultViewListeners.removeListener(listener);
	}

	@Override
	public void addToggleToToggledViewListener(ToggleToToggledViewListener listener) {
		toggleToToggledViewListeners.addListener(listener);
	}

	@Override
	public void removeToggleToToggledViewListener(ToggleToToggledViewListener listener) {
		toggleToToggledViewListeners.removeListener(listener);
	}
}
