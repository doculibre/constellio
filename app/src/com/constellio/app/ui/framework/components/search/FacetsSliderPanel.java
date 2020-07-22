package com.constellio.app.ui.framework.components.search;

import com.vaadin.ui.Component;
import org.vaadin.sliderpanel.SliderPanel;
import org.vaadin.sliderpanel.SliderPanelBuilder;
import org.vaadin.sliderpanel.client.SliderMode;
import org.vaadin.sliderpanel.client.SliderTabPosition;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public class FacetsSliderPanel extends SliderPanel {

	public FacetsSliderPanel(Component component) {
		super(builder(component));
	}

	private static SliderPanelBuilder builder(Component component) {
		SliderPanelBuilder builder = new SliderPanelBuilder(component)
				.caption($("SearchView.filter"))
				.tabPosition(SliderTabPosition.BEGINNING)
				.style("facets-sliderpanel")
				.expanded(false);
		if (isRightToLeft()) {
			builder.mode(SliderMode.LEFT);
		} else {
			builder.mode(SliderMode.RIGHT);
		}
		return builder;
	}

}
