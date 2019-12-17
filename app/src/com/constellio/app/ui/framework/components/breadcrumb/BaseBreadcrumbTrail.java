package com.constellio.app.ui.framework.components.breadcrumb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.pages.base.SessionContext;
import com.lexaden.breadcrumb.Breadcrumb;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;

public abstract class BaseBreadcrumbTrail extends CustomComponent implements BreadcrumbTrail {

	public static final String TAXONOMY_CODE = "taxonomyCode";

	public static final String SEARCH_ID = "searchId";

	public static final String ADVANCED_SEARCH = "advancedSearch";

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
	public List<BreadcrumbItem> getItems(Class<? extends BreadcrumbItem> clazz) {
		List<BreadcrumbItem> matches = new ArrayList<>();
		for (Iterator<BreadcrumbItem> it = items.iterator(); it.hasNext();) {
			BreadcrumbItem item = it.next();
			if (clazz.isAssignableFrom(item.getClass())) {
				matches.add(item);
			}
		}
		return matches;
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
	public CoreViews navigateTo() {
		return ConstellioUI.getCurrent().navigateTo();
	}

	@Override
	public Navigation navigate() {
		return ConstellioUI.getCurrent().navigate();
	}

	protected abstract void itemClick(BreadcrumbItem item);
}
