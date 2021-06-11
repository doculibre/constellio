package com.constellio.app.ui.framework.buttons.toggle;

import com.vaadin.ui.Component;

public class ToggleableComponent extends AbstractToggleableComponent {

	private Component defaultView;
	private Component toggledView;

	private final ToggleViewFactoryWithToggleCallback defaultViewFactory;
	private final ToggleViewFactoryWithToggleCallback toggledViewFactory;

	public ToggleableComponent(ToggleViewFactory defaultViewFactory,
							   ToggleViewFactory toggledViewFactory) {
		this(toggleCallback -> defaultViewFactory.build(), toggleCallback -> toggledViewFactory.build());
	}

	public ToggleableComponent(ToggleViewFactoryWithToggleCallback defaultViewFactory,
							   ToggleViewFactory toggledViewFactory) {
		this(defaultViewFactory, toggleCallback -> toggledViewFactory.build());
	}

	public ToggleableComponent(ToggleViewFactory defaultViewFactory,
							   ToggleViewFactoryWithToggleCallback toggledViewFactory) {
		this(toggleCallback -> defaultViewFactory.build(), toggledViewFactory);
	}

	public ToggleableComponent(ToggleViewFactoryWithToggleCallback defaultViewFactory,
							   ToggleViewFactoryWithToggleCallback toggledViewFactory) {
		this.defaultViewFactory = defaultViewFactory;
		this.toggledViewFactory = toggledViewFactory;
	}

	@Override
	protected void initComponent() {
		defaultView = null;
		toggledView = null;
	}

	@Override
	public Component getDefaultView() {
		if (defaultView == null) {
			defaultView = defaultViewFactory.build(() -> {
				if (!isToggled()) {
					toggle();
				}
			});
		}

		return defaultView;
	}

	@Override
	public Component getToggledView() {
		if (toggledView == null) {
			toggledView = toggledViewFactory.build(() -> {
				if (isToggled()) {
					toggle();
				}
			});
		}

		return toggledView;
	}

	public interface ToggleCallback {
		void toggle();
	}

	public interface ToggleViewFactory {
		Component build();
	}

	public interface ToggleViewFactoryWithToggleCallback {
		Component build(ToggleCallback toggleCallback);
	}
}
