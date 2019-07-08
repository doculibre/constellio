package com.constellio.app.ui.framework.components.lazyLoad;

import com.constellio.app.ui.application.ConstellioUI;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class LazyLoadComponent extends CustomComponent {

	public LazyLoadComponent() {
		this(new LoaderComponent());
	}

	public LazyLoadComponent(Component loadingComponent) {
		super(loadingComponent);

		ConstellioUI.getCurrent().runAsync(new Runnable() {
			@Override
			public void run() {
				final Component lazyLoadedComponent = load();
				ConstellioUI.getCurrent().access(new Runnable() {
					@Override
					public void run() {
						System.out.println("Loaded! Accessing...");
						setCompositionRoot(lazyLoadedComponent);
					}
				});
			}
		}, 100, this);
	}

	@Override
	protected void setCompositionRoot(Component compositionRoot) {
		super.setCompositionRoot(compositionRoot);
		setWidth(compositionRoot.getWidth(), compositionRoot.getWidthUnits());
		setHeight(compositionRoot.getHeight(), compositionRoot.getHeightUnits());
	}

	protected abstract Component load();

	public static class LoaderComponent extends Label {

		public LoaderComponent() {
			super($("loading"));
			setIcon(FontAwesome.COG);
			addStyleName("fa-spin");
		}

	}

}
