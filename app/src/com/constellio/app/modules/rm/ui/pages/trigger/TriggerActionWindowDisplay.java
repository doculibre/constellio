package com.constellio.app.modules.rm.ui.pages.trigger;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.i18n.i18n;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.VerticalLayout;

public class TriggerActionWindowDisplay extends BaseWindow {
	public TriggerActionWindowDisplay(RecordVO recordVO) {
		super(i18n.$("TriggerActionWindowDisplay.windowTile"));

		VerticalLayout mainVLayout = new VerticalLayout();
		mainVLayout.setSizeUndefined();

		RecordDisplay recordDisplay = new RecordDisplay(recordVO);

		mainVLayout.addComponent(recordDisplay);
		mainVLayout.setMargin(new MarginInfo(true));

		this.setContent(mainVLayout);
		this.setHeightUndefined();
		this.setWidth("400px");

		this.addStyleName(WINDOW_STYLE_NAME);
		this.setModal(true);
		this.setResizable(true);
	}
}
