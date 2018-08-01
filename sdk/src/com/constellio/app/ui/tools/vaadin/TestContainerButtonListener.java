package com.constellio.app.ui.tools.vaadin;

import com.constellio.app.ui.framework.containers.ContainerButtonListener;
import com.constellio.app.ui.tools.pageloadtime.PageLoadTimeWriter;
import com.vaadin.ui.Button.ClickEvent;

import java.util.Date;

@SuppressWarnings("serial")
public class TestContainerButtonListener implements ContainerButtonListener {

	@Override
	public void buttonClick(ClickEvent event, Object itemId) {
		new PageLoadTimeWriter().write(new Date());
	}

}
