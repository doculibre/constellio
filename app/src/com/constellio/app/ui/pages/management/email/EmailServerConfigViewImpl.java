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
package com.constellio.app.ui.pages.management.email;

import com.constellio.app.ui.entities.EmailServerConfigVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.StringListComponent;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.conf.email.EmailServerConfiguration;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class EmailServerConfigViewImpl extends BaseViewImpl implements EmailServerConfigView {

	private EmailServerConfigPresenter presenter;

	private TextField userField;
	private TextField defaultEmailSenderField;
	private TextField testEmailField;
	private PasswordField passwordField;

	private StringListComponent propertiesField;
	private TextArea testAuthentication;
	private Button saveButton;

	public EmailServerConfigViewImpl() {
		super();
		this.presenter = new EmailServerConfigPresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);

		buildEmailServerConfigComponent(layout);
		buildTestArea(layout);
		buildSaveAndTestButtonsPanel(layout);
		return layout;
	}

	private void buildTestArea(VerticalLayout layout) {
		testEmailField = new TextField($("EmailServerConfigView.testEmail"));
		testEmailField.setNullRepresentation("");
		layout.addComponent(testEmailField);

		testAuthentication = new TextArea($("EmailServerConfigView.results"));
		testAuthentication.setSizeFull();
		testAuthentication.setEnabled(false);
		testAuthentication.setVisible(false);
		layout.addComponent(testAuthentication);
	}

	private void buildSaveAndTestButtonsPanel(final VerticalLayout layout) {
		Panel buttonsPanel = new Panel();
		buttonsPanel.setSizeUndefined();
		buttonsPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);

		Button testButton = new BaseButton($("EmailServerConfigView.testButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				testAuthentication.setVisible(true);
				try {
					EmailServerConfigVO emailServerConfigVO = getEmailServerConfig();
					testAuthentication.setValue(presenter.getTestServerMessage(emailServerConfigVO, testEmailField.getValue()));
					layout.replaceComponent(testAuthentication, testAuthentication);
				} catch (InvalidPropertiesField invalidPropertiesField) {
					showErrorMessage($("EmailServerConfigView.invalidProperties"));
				}
			}
		};

		saveButton = new BaseButton($("save")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				EmailServerConfigVO emailServerConfigVO = null;
				try {
					emailServerConfigVO = getEmailServerConfig();
					presenter.saveButtonClicked(emailServerConfigVO);
				} catch (InvalidPropertiesField invalidPropertiesField) {
					showErrorMessage($("EmailServerConfigView.invalidProperties"));
				}
			}
		};
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		HorizontalLayout hlayout = new HorizontalLayout(testButton, saveButton);
		hlayout.addComponent(saveButton);
		buttonsPanel.setContent(hlayout);
		layout.addComponent(buttonsPanel);
		layout.setComponentAlignment(buttonsPanel, Alignment.BOTTOM_RIGHT);
	}

	private EmailServerConfigVO getEmailServerConfig() throws InvalidPropertiesField{
		Map<String, String> properties = asMap(propertiesField.getValues());
		return new EmailServerConfigVO().setPassword(passwordField.getValue()).setUsername(userField.getValue())
				.setProperties(properties).setDefaultEmailServer(defaultEmailSenderField.getValue());
	}

	private Map<String, String> asMap(List<String> values) throws InvalidPropertiesField {
		Map<String, String> returnMap = new HashMap<>();
		for(String currentValue : values){
			if(StringUtils.isBlank(currentValue)){
				continue;
			}
			String[] elements = currentValue.split("=");
			if(elements.length != 2){
				throw new InvalidPropertiesField();
			}
			String key = elements[0];
			String value = elements[1];
			returnMap.put(key, value);
		}
		return returnMap;
	}

	private void buildEmailServerConfigComponent(VerticalLayout layout) {
		EmailServerConfiguration emailServerConfiguration = presenter.getEmailServerConfiguration();
		String user;
		String password;
		List<String> properties;
		if(emailServerConfiguration != null){
			user = emailServerConfiguration.getUsername();
			password = emailServerConfiguration.getPassword();
			properties = asList(emailServerConfiguration.getProperties());
		} else {
			user = "";
			password = "";
			properties = new ArrayList<>();
		}

		userField = new TextField($("EmailServerConfigView.username"));
		userField.setValue(user);
		userField.setRequired(true);
		userField.setNullRepresentation("");
		layout.addComponent(userField);

		defaultEmailSenderField = new TextField($("EmailServerConfigView.defaultEmailSender"));
		defaultEmailSenderField.setValue(user);
		defaultEmailSenderField.setRequired(true);
		defaultEmailSenderField.setNullRepresentation("");
		layout.addComponent(defaultEmailSenderField);

		passwordField = new PasswordField($("EmailServerConfigView.password"));
		passwordField.setValue(password);
		passwordField.setRequired(true);
		layout.addComponent(passwordField);

		propertiesField = new StringListComponent();
		propertiesField.setCaption($("EmailServerConfigView.properties"));
		propertiesField.setValues(properties);
		propertiesField.setRequired(true);
		layout.addComponent(propertiesField);
	}

	private List<String> asList(Map<String, String> properties) {
		List<String> returnList = new ArrayList<>();
		if(properties != null){
			for(Map.Entry<String, String> entry : properties.entrySet()){
				returnList.add(entry.getKey() + "=" + entry.getValue());
			}
		}
		return returnList;
	}

	private class InvalidPropertiesField extends Exception {
	}

	@Override
	protected String getTitle() {
		return $("EmailServerConfigView.viewTitle");
	}

	@Override
	protected Button.ClickListener getBackButtonClickListener() {
		return new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				presenter.backButtonClick();
			}
		};
	}
}
