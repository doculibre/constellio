package com.constellio.app.ui.framework.components.tree;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;

public interface TreeItemClickListener extends ItemClickListener {

	boolean shouldExpandOrCollapse(ItemClickEvent event);

}
