/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.pages.management;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.themes.ValoTheme;

public class ArchiveManagementViewImpl extends BaseViewImpl implements ArchiveManagementView {
	private final ArchiveManagementPresenter presenter;

	private Button decommissioning, containers, robots, reportsButton;

	public ArchiveManagementViewImpl() {
		presenter = new ArchiveManagementPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ArchiveManagementView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		addStyleName("view-group");

		decommissioning = new Button(
				$("ArchiveManagementView.decommissioning"), new ThemeResource("images/icons/config/platform_truck.png"));
		decommissioning.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.decommissioningButtonClicked();
			}
		});
		decommissioning.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		decommissioning.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		decommissioning.addStyleName("decommissioning-caption");

		containers = new Button(
				$("ArchiveManagementView.containers"), new ThemeResource("images/icons/config/box.png"));
		containers.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.containersButtonClicked();
			}
		});
		containers.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		containers.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		containers.addStyleName("containers-caption");

		robots = new Button($("ArchiveManagementView.robots"),
				new ThemeResource("images/icons/config/robot_platform_truck.png"));
		robots.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.robotsButtonClicked();
			}
		});
		robots.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		robots.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		robots.addStyleName("robots-caption");
		robots.setEnabled(false);

		reportsButton = new Button($("ArchiveManagementView.reports"), new ThemeResource("images/icons/config/report.png"));
		reportsButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.reportsButtonClicked();
			}
		});
		reportsButton.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		reportsButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		reportsButton.addStyleName("reports-caption");

		presenter.onViewAssembled();
		return new CssLayout(decommissioning, containers, reportsButton, robots);
	}

	@Override
	public void setDecommissioningButtonVisible(boolean visible) {
		decommissioning.setVisible(visible);
	}

	@Override
	public void setRobotsButtonVisible(boolean visible) {
		robots.setVisible(visible);
	}

	@Override
	public void setContainersButtonVisible(boolean visible) {
		containers.setVisible(visible);
	}

	@Override
	public void setPrintReportsButtonVisible(boolean visible) {

	}
}
