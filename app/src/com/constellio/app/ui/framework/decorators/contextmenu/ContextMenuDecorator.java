package com.constellio.app.ui.framework.decorators.contextmenu;

import com.vaadin.navigator.View;
import org.vaadin.peter.contextmenu.ContextMenu;

import java.io.Serializable;

public interface ContextMenuDecorator extends Serializable {

	ContextMenu decorate(View view, ContextMenu contextMenu);

}
