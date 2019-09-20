package com.constellio.app.ui.framework.components.search;

import static com.constellio.app.ui.i18n.i18n.$;

import org.vaadin.sliderpanel.SliderPanel;
import org.vaadin.sliderpanel.SliderPanelBuilder;
import org.vaadin.sliderpanel.client.SliderMode;
import org.vaadin.sliderpanel.client.SliderTabPosition;

import com.vaadin.ui.Component;

public class FacetsSliderPanel extends SliderPanel {

	public FacetsSliderPanel(Component component) {
		super(builder(component));
	}
	
	private static SliderPanelBuilder builder(Component component) {
		SliderPanelBuilder builder = new SliderPanelBuilder(component)
				.mode(SliderMode.RIGHT)
				.caption($("SearchView.filter"))
				.tabPosition(SliderTabPosition.BEGINNING)
				.style("facets-sliderpanel")
				.expanded(false);
		return builder;
	}

}
