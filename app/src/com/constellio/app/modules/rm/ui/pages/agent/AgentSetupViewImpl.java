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
