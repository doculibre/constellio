package com.constellio.app.ui.pages.management.ldap;

import com.constellio.app.ui.framework.components.StringListComponent;
import com.constellio.app.ui.framework.components.fields.EditablePasswordField;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.config.AzureADServerConfig;
import com.constellio.model.conf.ldap.config.AzureADUserSynchConfig;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class LDAPConfigManagementViewImpl extends LDAPConfigBaseView implements LDAPConfigManagementView {
	private AzurAuthenticationTab azurAuthenticationTab;
	private AzurSynchTab azurSynchTab;

	private DefaultAuthenticationTab defaultAuthenticationTab;
	private DefaultSynchTab defaultSynchTab;

	protected Component tabsheet;
	protected VerticalLayout layout;

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		buildDirectoryTypeField();
		buildActionCheckbox();
		layout.addComponent(super.directoryTypeField);
		layout.addComponent(super.activateLDAPCheckBox);

		tabsheet = createConfigTabSheet();

		layout.addComponent(tabsheet);

		buildButtonsPanel(layout);

		testAuthentication = new TextArea($("ldap.test.results"));
		testAuthentication.setSizeFull();
		testAuthentication.setEnabled(false);
		testAuthentication.setVisible(false);
		layout.addComponent(testAuthentication);
		//layout.addComponent(saveButton);
		//layout.setComponentAlignment(saveButton, Alignment.BOTTOM_RIGHT);

		return layout;
	}

	@Override
	public void updateComponents() {
		Component newTabSheet = createConfigTabSheet();
		layout.replaceComponent(tabsheet, newTabSheet);
		tabsheet = newTabSheet;
		testAuthentication.setVisible(false);
	}

	@Override
	protected String getAuthenticationPassword() {
		if (getDirectoryType() == LDAPDirectoryType.AZURE_AD) {
			return azurAuthenticationTab.getTestPassword();
		} else {
			return defaultSynchTab.getTestPassword();
		}
	}

	@Override
	protected String getAuthenticationUser() {
		if (getDirectoryType() == LDAPDirectoryType.AZURE_AD) {
			return azurAuthenticationTab.getTestUser();
		} else {
			return defaultSynchTab.getTestUser();
		}
	}

	protected Component createConfigTabSheet() {
		LDAPDirectoryType directoryType = getDirectoryType();
		switch (directoryType) {
			case AZURE_AD:
				return createAzureConfigTabSheet();
			case ACTIVE_DIRECTORY:
			case E_DIRECTORY:
				return createDefaultLDAPTabSheet();
			default:
				throw new RuntimeException("unknown type " + directoryType);
		}
	}

	private Component createDefaultLDAPTabSheet() {
		TabSheet returnTabSheet = new TabSheet();
		defaultAuthenticationTab = new DefaultAuthenticationTab();
		returnTabSheet.addTab(defaultAuthenticationTab, $("LDAPConfigManagementView.authentication"));

		defaultSynchTab = new DefaultSynchTab();
		returnTabSheet.addTab(defaultSynchTab, $("LDAPConfigManagementView.synchronisation"));
		return returnTabSheet;
	}

	private TabSheet createAzureConfigTabSheet() {
		TabSheet returnTabSheet = new TabSheet();
		azurAuthenticationTab = new AzurAuthenticationTab();
		returnTabSheet.addTab(azurAuthenticationTab, $("LDAPConfigManagementView.authentication"));

		azurSynchTab = new AzurSynchTab();
		returnTabSheet.addTab(azurSynchTab, $("LDAPConfigManagementView.synchronisation"));
		return returnTabSheet;
	}

	@Override
	protected LDAPUserSyncConfiguration getLDAPUserSyncConfiguration() {
		if (getDirectoryType() == LDAPDirectoryType.AZURE_AD) {
			return azurSynchTab.getLDAPUserSyncConfiguration();
		} else {
			return defaultSynchTab.getLDAPUserSyncConfiguration();
		}
	}

	@Override
	protected LDAPServerConfiguration getLDAPServerConfiguration() {
		LDAPDirectoryType directoryType = getDirectoryType();
		if (directoryType == LDAPDirectoryType.AZURE_AD) {
			return azurAuthenticationTab.getLDAPServerConfiguration();
		} else {
			return defaultAuthenticationTab.getLDAPServerConfiguration();
		}
	}

	private class AzurAuthenticationTab extends VerticalLayout {
		private Field clientId, authorityTenantId;
		private Field userField;
		private EditablePasswordField passwordField;

		private AzurAuthenticationTab() {
			LDAPServerConfiguration ldapServerConfiguration = presenter.getLDAPServerConfiguration();
			setSpacing(true);
			setSizeFull();

			clientId = createStringField(ldapServerConfiguration.getClientId(), true);
			clientId.setCaption($("LDAPConfigManagementView.clientId"));
			addComponent(clientId);

			authorityTenantId = createStringField(ldapServerConfiguration.getTenantName(), true);
			authorityTenantId.setCaption($("LDAPConfigManagementView.authorityTenantId"));
			HorizontalLayout authority = new HorizontalLayout(authorityTenantId);
			addComponent(authority);

			userField = new TextField($("LDAPConfigManagementView.testAuthenticationUser"));
			addComponent(userField);

			passwordField = new EditablePasswordField($("LDAPConfigManagementView.testAuthenticationPassword"));
			addComponent(passwordField);
		}

		public String getAuthorityTenantId() {
			return (String) authorityTenantId.getValue();
		}

		public String getClientId() {
			return (String) clientId.getValue();
		}

		public String getTestUser() {
			return (String) userField.getValue();
		}

		public String getTestPassword() {
			return (String) passwordField.getValue();
		}

		public LDAPServerConfiguration getLDAPServerConfiguration() {
			AzureADServerConfig serverConfig = new AzureADServerConfig()
					.setAuthorityTenantId(azurAuthenticationTab.getAuthorityTenantId())
					.setClientId(azurAuthenticationTab.getClientId());
			return new LDAPServerConfiguration(serverConfig, presenter.isLDAPActive());
		}
	}

	private class AzurSynchTab extends VerticalLayout {
		Field applicationKey, clientId;

		private AzurSynchTab() {
			LDAPUserSyncConfiguration ldapUserSyncConfiguration = presenter.getLDAPUserSyncConfiguration();
			setSpacing(true);
			setSizeFull();

			buildSynchronizationScheduleFields(ldapUserSyncConfiguration);
			addComponent(scheduleComponentField);

			buildCollectionsPanel();
			addComponent(collectionsComponent);

			clientId = createStringField(ldapUserSyncConfiguration.getClientId(), true);
			clientId.setCaption($("LDAPConfigManagementView.clientId"));
			addComponent(clientId);

			applicationKey = createStringField(ldapUserSyncConfiguration.getClientSecret(), true);
			applicationKey.setCaption($("LDAPConfigManagementView.applicationKey"));
			addComponent(applicationKey);

			buildUsersAcceptRegex(ldapUserSyncConfiguration);
			addComponent(usersAcceptanceRegexField);
			buildUsersRejectRegex(ldapUserSyncConfiguration);
			addComponent(usersRejectionRegexField);

			buildGroupsAcceptRegex(ldapUserSyncConfiguration);
			addComponent(groupsAcceptanceRegexField);
			buildGroupsRejectRegex(ldapUserSyncConfiguration);
			addComponent(groupsRejectionRegexField);
		}

		public String getApplicationKey() {
			return (String) applicationKey.getValue();
		}

		public LDAPUserSyncConfiguration getLDAPUserSyncConfiguration() {
			AzureADUserSynchConfig azurUserSynchConfig = new AzureADUserSynchConfig()
					.setApplicationKey(azurSynchTab.getApplicationKey())
					.setClientId(azurSynchTab.getClientId());
			return new LDAPUserSyncConfiguration(azurUserSynchConfig, getUserFilter(), getGroupsFilter(),
					scheduleComponentField.getPeriod(), scheduleComponentField.getTimeList(), selectedCollections());
		}

		private String getClientId() {
			return (String) clientId.getValue();
		}
	}

	private class DefaultAuthenticationTab extends VerticalLayout {
		private CheckBox followReferences;

		private StringListComponent urlsField;
		private StringListComponent domainsField;

		private DefaultAuthenticationTab() {
			setSizeFull();
			setSpacing(true);

			buildLdapServerConfigComponent(this);
		}

		private void buildLdapServerConfigComponent(VerticalLayout layout) {
			LDAPServerConfiguration ldapServerConfiguration = presenter.getLDAPServerConfiguration();

			followReferences = new CheckBox($("ldap.authentication.followReferences"));
			followReferences.setValue(ldapServerConfiguration.getFollowReferences());
			layout.addComponent(followReferences);
			List<String> urls = ldapServerConfiguration.getUrls();

			urlsField = new StringListComponent();
			urlsField.setCaption($("ldap.serverConfiguration.urls"));
			urlsField.setValues(urls);
			urlsField.setRequired(true);
			layout.addComponent(urlsField);

			List<String> domains = ldapServerConfiguration.getDomains();
			domainsField = new StringListComponent();
			domainsField.setCaption($("ldap.serverConfiguration.domains"));
			domainsField.setValues(domains);
			domainsField.setRequired(true);
			layout.addComponent(domainsField);
		}

		public LDAPServerConfiguration getLDAPServerConfiguration() {
			return new LDAPServerConfiguration(urlsField.getValues(),
					domainsField.getValues(), getDirectoryType(), presenter.isLDAPActive(),
					followReferences.getValue());
		}
	}

	private class DefaultSynchTab extends VerticalLayout {
		private StringListComponent groupsField;
		private StringListComponent usersField;
		private CheckBox membershipAutomaticDerivationActivatedCheckbox;
		private StringListComponent userFilterGroupsField;
		private Field userField;
		private EditablePasswordField passwordField;

		private DefaultSynchTab() {
			setSizeFull();
			setSpacing(true);

			buildLdapUserSyncConfigComponent(this);
		}

		private void buildLdapUserSyncConfigComponent(VerticalLayout layout) {
			LDAPUserSyncConfiguration ldapUserSyncConfiguration = presenter.getLDAPUserSyncConfiguration();
			buildSynchronizationScheduleFields(ldapUserSyncConfiguration);
			layout.addComponent(scheduleComponentField);
			layout.addComponent(new Label("<hr />", ContentMode.HTML));

			buildCollectionsPanel();

			layout.addComponent(collectionsComponent);

			String user = ldapUserSyncConfiguration.getUser();
			userField = createStringField(user, true);
			userField.setCaption($("ldap.syncConfiguration.user.login"));
			layout.addComponent(userField);
			String password = ldapUserSyncConfiguration.getPassword();
			passwordField = new EditablePasswordField(
					$("ldap.syncConfiguration.user.password"));//PasswordField($("ldap.syncConfiguration.user.password"));
			passwordField.setValue(password);
			passwordField.setRequired(true);
			layout.addComponent(passwordField);

			layout.addComponent(new Label("<hr />", ContentMode.HTML));

			List<String> groups = ldapUserSyncConfiguration.getGroupBaseContextList();
			groupsField = new StringListComponent();
			groupsField.setCaption($("ldap.syncConfiguration.groupsBaseContextList"));
			groupsField.setValues(groups);
			layout.addComponent(groupsField);

			buildGroupsAcceptRegex(ldapUserSyncConfiguration);
			layout.addComponent(groupsAcceptanceRegexField);
			buildGroupsRejectRegex(ldapUserSyncConfiguration);
			layout.addComponent(groupsRejectionRegexField);
			layout.addComponent(new Label("<hr />", ContentMode.HTML));

			List<String> users = ldapUserSyncConfiguration.getUsersWithoutGroupsBaseContextList();
			usersField = new StringListComponent();
			usersField.setCaption($("ldap.syncConfiguration.usersWithoutGroupsBaseContextList"));
			usersField.setValues(users);
			layout.addComponent(usersField);
			userFilterGroupsField = new StringListComponent();
			userFilterGroupsField.setCaption($("ldap.syncConfiguration.userFilterGroupsList"));
			userFilterGroupsField.setValues(ldapUserSyncConfiguration.getUserFilterGroupsList());
			layout.addComponent(userFilterGroupsField);
			buildUsersAcceptRegex(ldapUserSyncConfiguration);
			layout.addComponent(usersAcceptanceRegexField);
			buildUsersRejectRegex(ldapUserSyncConfiguration);
			layout.addComponent(usersRejectionRegexField);

			final boolean membershipAutomaticDerivationActivated = ldapUserSyncConfiguration.isMembershipAutomaticDerivationActivated();
			membershipAutomaticDerivationActivatedCheckbox = new CheckBox($("ldap.syncConfiguration.membershipAutomaticDerivationActivated"));
			membershipAutomaticDerivationActivatedCheckbox.setValue(membershipAutomaticDerivationActivated);
			layout.addComponent(membershipAutomaticDerivationActivatedCheckbox);
		}

		public String getTestUser() {
			return (String) userField.getValue();
		}

		public String getTestPassword() {
			return (String) passwordField.getValue();
		}

		public LDAPUserSyncConfiguration getLDAPUserSyncConfiguration() {
			return new LDAPUserSyncConfiguration(
					notNull(userField), notNull(passwordField),
					getUserFilter(), getGroupsFilter(),
					scheduleComponentField.getPeriod(), scheduleComponentField.getTimeList(), groupsField.getValues(), usersField.getValues(),
					userFilterGroupsField.getValues(), membershipAutomaticDerivationActivatedCheckbox.getValue(), selectedCollections());
		}
	}

	private String notNull(Field field) {
		return (field.getValue() != null) ? field.getValue().toString() : "";
	}
}
