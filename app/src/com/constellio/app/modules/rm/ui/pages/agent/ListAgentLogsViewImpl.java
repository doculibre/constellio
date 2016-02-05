package com.constellio.app.modules.rm.ui.pages.agent;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.InputStream;
import java.util.List;

import com.constellio.app.modules.rm.ui.entities.AgentLogVO;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.data.Item;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class ListAgentLogsViewImpl extends BaseViewImpl implements ListAgentLogsView {
	
	private static final String FILENAME_PROPERTY = "filename";
	
	private String selectedUserId;
	
	private VerticalLayout mainLayout;
	
	private HorizontalLayout userFieldAndButtonLayout;
	
	private Label userLabel;
	
	private LookupRecordField userField;
	
	private Button showLogsButton;
	
	private Table agentLogsTable;
	
	private ListAgentLogsPresenter presenter;

	public ListAgentLogsViewImpl() {
		presenter = new ListAgentLogsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListAgentLogsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		
		userFieldAndButtonLayout = new HorizontalLayout();
		userFieldAndButtonLayout.setSpacing(true);
		
		userLabel = new Label($("ListAgentLogsView.user"));
		
		userField = new LookupRecordField(User.SCHEMA_TYPE);
		userField.setPropertyDataSource(new MethodProperty<String>(this, "selectedUserId"));
		
		showLogsButton = new Button($("ListAgentLogsView.showLogs"));
		showLogsButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.showLogsButtonClicked();
			}
		});
		showLogsButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		
		agentLogsTable = new Table($("ListAgentLogsView.tableTitle"));
		agentLogsTable.setWidth("100%");
		agentLogsTable.addContainerProperty(FILENAME_PROPERTY, DownloadLink.class, null);
		agentLogsTable.setColumnHeader(FILENAME_PROPERTY, $("ListAgentLogsView.filename"));
		
		mainLayout.addComponents(userFieldAndButtonLayout, agentLogsTable);
		userFieldAndButtonLayout.addComponents(userLabel, userField, showLogsButton);
		
		mainLayout.setExpandRatio(agentLogsTable, 1);
		userFieldAndButtonLayout.setExpandRatio(userField, 1);
		
		return mainLayout;
	}

	@Override
	public String getSelectedUserId() {
		return selectedUserId;
	}
	
	public void setSelectedUserId(String selectedUserId) {
		this.selectedUserId = selectedUserId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setAgentLogs(List<AgentLogVO> agentLogVOs) {
		agentLogsTable.removeAllItems();
		for (final AgentLogVO agentLogVO : agentLogVOs) {
			Item item = agentLogsTable.addItem(agentLogVO);
			String filename = agentLogVO.getFilename();
			StreamSource zipStreamSource = new StreamResource.StreamSource() {
				@Override
				public InputStream getStream() {
					return presenter.getInputStream(agentLogVO);
				}
			};
			Resource logResource = new StreamResource(zipStreamSource, filename);
			DownloadLink downloadAgentLogLink = new DownloadLink(logResource, filename);
			item.getItemProperty(FILENAME_PROPERTY).setValue(downloadAgentLogLink);
		}
	}

}
