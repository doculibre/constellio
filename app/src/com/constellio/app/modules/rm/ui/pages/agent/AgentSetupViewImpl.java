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
package com.constellio.app.modules.rm.ui.pages.agent;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class AgentSetupViewImpl extends BaseViewImpl implements AgentSetupView {
	
	private String agentVersion;
	
	private String agentDownloadURL;
	
	private String agentInitURL;
	
	private VerticalLayout mainLayout;
	
	private Label downloadTextLabel;
	
	private Label installTextLabel;
	
	private Label initTextLabel;
	
	private AgentSetupPresenter presenter;
	
	public AgentSetupViewImpl() {
		this.presenter = new AgentSetupPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.viewEntered();
	}

	@Override
	protected String getTitle() {
		return $("AgentSetupView.viewTitle", agentVersion);
	}

	@Override
	public void setAgentVersion(String agentVersion) {
		this.agentVersion = agentVersion;
	}

	@Override
	public void setAgentDownloadURL(String agentDownloadURL) {
		this.agentDownloadURL = agentDownloadURL;
	}

	@Override
	public void setAgentInitURL(String agentInitURL) {
		this.agentInitURL = agentInitURL;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		
		downloadTextLabel = new Label($("AgentSetupView.downloadText", agentDownloadURL), ContentMode.HTML);
		installTextLabel = new Label($("AgentSetupView.installText"));
		initTextLabel = new Label($("AgentSetupView.initText", agentInitURL), ContentMode.HTML);
		
		mainLayout.addComponents(downloadTextLabel, installTextLabel, initTextLabel);
		
		return mainLayout;
	}

}
