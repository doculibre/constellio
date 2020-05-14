package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListContentAccessAndRoleAuthorizationsViewImpl extends ListContentAccessAuthorizationsViewImpl implements ListContentAccessAndRoleAuthorizationsView {

	protected void initPresenter() {
		presenter = new ListContentAccessAndRoleAuthorizationsPresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		VerticalLayout mainLayout = (VerticalLayout) super.buildMainComponent(event);
		buildSelectionTabSheet(mainLayout);
		return mainLayout;
	}

	@Override
	public void refresh() {
		super.refresh();
		buildSelectionTabSheet(layout);
	}

	private void buildSelectionTabSheet(VerticalLayout mainLayout) {
		TabSheet tabSheet = new TabSheet();
		final VerticalLayout accesses = new VerticalLayout();
		accesses.setCaption($("AuthorizationsView.access"));
		tabSheet.addTab(accesses);
		VerticalLayout roles = new VerticalLayout();
		roles.setCaption($("AuthorizationsView.userRoles"));
		tabSheet.addTab(roles);
		if (!((ListContentAccessAndRoleAuthorizationsPresenter) presenter).isCurrentlyViewingAccesses()) {
			tabSheet.setSelectedTab(roles);
		}
		tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				if (event.getTabSheet().getSelectedTab().equals(accesses)) {
					((ListContentAccessAndRoleAuthorizationsPresenter) presenter).viewAccesses();
					refresh();
				} else {
					((ListContentAccessAndRoleAuthorizationsPresenter) presenter).viewRoles();
					refresh();
				}
			}
		});

		mainLayout.addComponent(tabSheet, 0);
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return ((ListContentAccessAndRoleAuthorizationsPresenter) presenter).getBreadCrumbTrail();
	}
}
