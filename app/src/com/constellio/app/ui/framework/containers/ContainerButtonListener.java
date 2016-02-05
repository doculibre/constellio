package com.constellio.app.ui.framework.containers;

import java.io.Serializable;

import com.vaadin.ui.Button.ClickEvent;

public interface ContainerButtonListener extends Serializable {
	
	void buttonClick(ClickEvent event, Object itemId);

}
