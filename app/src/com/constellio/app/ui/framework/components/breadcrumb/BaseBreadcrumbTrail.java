package com.constellio.app.ui.framework.components.breadcrumb;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.SessionContext;
import com.lexaden.breadcrumb.Breadcrumb;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;

public abstract class BaseBreadcrumbTrail extends CustomComponent implements BreadcrumbTrail {

	private Breadcrumb breadcrumb;
	
	private List<BreadcrumbItem> items = new ArrayList<>();
	
	public BaseBreadcrumbTrail() {
		setWidth("100%");
		breadcrumb = new Breadcrumb();
		setCompositionRoot(breadcrumb);
	}

	public final Breadcrumb getBreadcrumb() {
		return breadcrumb;
	}

	@Override
	public void addItem(final BreadcrumbItem item) {
		items.add(item);
		
		Button itemButton = newButton(item);
		itemButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				itemClick(item);
			}
		});
		itemButton.setEnabled(item.isEnabled());
		breadcrumb.addLink(itemButton);
		itemButton.removeStyleName("xbreadcrumbbutton-home");
	}
	
	protected Button newButton(BreadcrumbItem item) {
		String itemLabel = item.getLabel();
		return new Button(itemLabel);
	}

	@Override
	public List<BreadcrumbItem> getItems() {
		return items;
	}

	@Override
	public SessionContext getSessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioUI.getCurrent().getConstellioFactories();
	}

	@Override
	public ConstellioNavigator navigateTo() {
		return ConstellioUI.getCurrent().navigateTo();
	}
	
	protected abstract void itemClick(BreadcrumbItem item);

}
