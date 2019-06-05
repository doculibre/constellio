package com.constellio.app.modules.rm.ui.components.userDocument;

import com.constellio.app.modules.rm.ui.pages.userDocuments.ListUserDocumentsViewImpl;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;

public class UserDocumentsWindow extends BaseWindow implements DropHandler {

	private ListUserDocumentsViewImpl view;

	public UserDocumentsWindow() {
		setZIndex(null);
		setWidth("80%");
		setHeight("80%");
		center();
	}

	@Override
	public void drop(DragAndDropEvent event) {
		view = new ListUserDocumentsViewImpl(true);
		view.enter(null);
		setContent(view);

		ConstellioUI.getCurrent().addWindow(this);
		view.drop(event);
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return AcceptAll.get();
	}

}
