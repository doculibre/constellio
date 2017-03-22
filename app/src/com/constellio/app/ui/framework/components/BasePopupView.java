package com.constellio.app.ui.framework.components;

import java.util.EventObject;

import com.vaadin.ui.Component;
import com.vaadin.ui.PopupView;

public class BasePopupView extends PopupView {
	
	private boolean ignorePopupVisibilityEvent;

	public BasePopupView(Content content) {
		super(content);
	}

	public BasePopupView(String small, Component large) {
		super(small, large);
	}

    /**
     * Set the visibility of the popup. Does not hide the minimal
     * representation.
     *
     * @param visible
     */
    public void setPopupVisible(boolean visible, boolean fireEvent) {
    	if (!fireEvent) {
    		ignorePopupVisibilityEvent = true;
    	} 
    	super.setPopupVisible(visible);
    	ignorePopupVisibilityEvent = false;
    }

	@Override
	protected void fireEvent(EventObject event) {
		if (!ignorePopupVisibilityEvent || !(event instanceof PopupVisibilityEvent)) {
			super.fireEvent(event);
		}
	}

}
