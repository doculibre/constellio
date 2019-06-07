package com.constellio.app.modules.rm.ui.components.tree;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.PlatformDetectionUtils;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Tree;

public abstract class RMTreeDropHandlerImpl implements DropHandler, RMTreeDropHander {

	private RMTreeDropPresenter presenter;

	public RMTreeDropHandlerImpl() {
		this.presenter = new RMTreeDropPresenter(this);
	}

	@Override
	public void drop(DragAndDropEvent dragEvent) {
		if(PlatformDetectionUtils.isMobile()) {
			return;
		}

		AbstractSelectTargetDetails dropTargetData = (AbstractSelectTargetDetails) dragEvent.getTargetDetails();
		Tree tree = (Tree) dragEvent.getTargetDetails().getTarget();
		Transferable transferable = dragEvent.getTransferable();

		Object sourceItemId = transferable.getData("itemId");
		Object targetItemId = dropTargetData.getItemIdOver();
		if ((sourceItemId instanceof String) && (targetItemId instanceof String)) {
			String newParentId = presenter.recordDropped((String) sourceItemId, (String) targetItemId);
			if (newParentId != null) {
				if (tree.isExpanded(newParentId)) {
					tree.setParent(sourceItemId, newParentId);
				} else {
					tree.removeItem(sourceItemId);
				}
			}
		}
	}

	@Override
	public AcceptCriterion getAcceptCriterion() {
		return AcceptAll.get();
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioUI.getCurrent().getConstellioFactories();
	}

}
