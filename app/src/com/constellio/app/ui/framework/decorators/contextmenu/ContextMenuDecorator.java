package com.constellio.app.ui.framework.decorators.contextmenu;

import java.io.Serializable;

import org.vaadin.peter.contextmenu.ContextMenu;

import com.vaadin.navigator.View;

public interface ContextMenuDecorator extends Serializable {
	
	ContextMenu decorate(View view, ContextMenu contextMenu);

}
