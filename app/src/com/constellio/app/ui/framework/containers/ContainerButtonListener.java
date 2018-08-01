package com.constellio.app.ui.framework.containers;

import com.vaadin.ui.Button.ClickEvent;

import java.io.Serializable;

public interface ContainerButtonListener extends Serializable {

	void buttonClick(ClickEvent event, Object itemId);

}
