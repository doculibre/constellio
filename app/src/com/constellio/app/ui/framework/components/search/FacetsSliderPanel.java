package com.constellio.app.ui.framework.components.search;

import com.vaadin.lazyloadwrapper.LazyLoadWrapper;
import com.vaadin.ui.Component;
import org.vaadin.sliderpanel.SliderPanel;
import org.vaadin.sliderpanel.SliderPanelBuilder;
import org.vaadin.sliderpanel.client.SliderMode;
import org.vaadin.sliderpanel.client.SliderTabPosition;

import static com.constellio.app.ui.i18n.i18n.$;

public class FacetsSliderPanel extends SliderPanel {

	public FacetsSliderPanel(Component component) {
		super(builder(component));
	}

	private static SliderPanelBuilder builder(Component component) {
		LazyLoadWrapper lazyLoadWrapper = new LazyLoadWrapper(component);
		SliderPanelBuilder builder = new SliderPanelBuilder(lazyLoadWrapper)
				.mode(SliderMode.RIGHT)
				.caption($("SearchView.filter"))
				.tabPosition(SliderTabPosition.BEGINNING)
				.style("facets-sliderpanel")
				.expanded(false);
		return builder;
	}

}
