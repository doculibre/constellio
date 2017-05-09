package com.constellio.app.ui.pages.management.ldap;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.ui.framework.components.StringListComponent;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.config.AzureADServerConfig;
import com.constellio.model.conf.ldap.config.AzureADUserSynchConfig;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

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
		layout.addComponent(super.directoryTypeField);

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
		private Field passwordField;
		private Field activateAuthentication;

		private AzurAuthenticationTab() {
			LDAPServerConfiguration ldapServerConfiguration = presenter.getLDAPServerConfiguration();
			setSpacing(true);
			setSizeFull();

			activateAuthentication = createCheckmarkField(ldapServerConfiguration.getLdapAuthenticationActive(), true);
			activateAuthentication.setCaption($("ldap.authentication.active"));
			activateAuthentication.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					presenter.setLDAPActive(!presenter.isLDAPActive());
					changePageState(presenter.isLDAPActive());
				}
			});
			addComponent(activateAuthentication);

			clientId = createStringField(ldapServerConfiguration.getClientId(), true);
			clientId.setCaption($("LDAPConfigManagementView.clientId"));
			addComponent(clientId);

			authorityTenantId = createStringField(ldapServerConfiguration.getTenantName(), true);
			authorityTenantId.setCaption($("LDAPConfigManagementView.authorityTenantId"));
			HorizontalLayout authority = new HorizontalLayout(authorityTenantId);
			addComponent(authority);

			userField = new TextField($("LDAPConfigManagementView.testAuthenticationUser"));
			addComponent(userField);

			passwordField = new PasswordField($("LDAPConfigManagementView.testAuthenticationPassword"));
			addComponent(passwordField);

			changePageState(ldapServerConfiguration.getLdapAuthenticationActive());

		}

		private void changePageState(boolean state) {
			clientId.setEnabled(state);
			authorityTenantId.setEnabled(state);
			userField.setEnabled(state);
			passwordField.setEnabled(state);
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
			return new LDAPServerConfiguration(serverConfig, presenter.isLDAPActive(), presenter.isLDAPSyncActive());
		}
	}

	private class AzurSynchTab extends VerticalLayout {
		Field applicationKey, clientId, activateSynch;

		private AzurSynchTab() {
			LDAPUserSyncConfiguration ldapUserSyncConfiguration = presenter.getLDAPUserSyncConfiguration();
			setSpacing(true);
			setSizeFull();

			activateSynch = createCheckmarkField(getLDAPServerConfiguration().getLdapSyncActive(), true);
			activateSynch.setCaption($("ldap.synchronization.active"));
			activateSynch.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					presenter.setLDAPSyncActive(!presenter.isLDAPSyncActive());
					changePageState(presenter.isLDAPSyncActive());
				}
			});
			addComponent(activateSynch);

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

			changePageState(getLDAPServerConfiguration().getLdapSyncActive());
		}

		private void changePageState(boolean state) {
			scheduleComponentField.setEnabled(state);
			collectionsComponent.setEnabled(state);
			clientId.setEnabled(state);
			applicationKey.setEnabled(state);

			usersRejectionRegexField.setEnabled(state);
			groupsAcceptanceRegexField.setEnabled(state);
			groupsRejectionRegexField.setEnabled(state);
			usersAcceptanceRegexField.setEnabled(state);
		}

		public String getApplicationKey() {
			return (String) applicationKey.getValue();
		}

		public LDAPUserSyncConfiguration getLDAPUserSyncConfiguration() {
			AzureADUserSynchConfig azurUserSynchConfig = new AzureADUserSynchConfig()
					.setApplicationKey(azurSynchTab.getApplicationKey())
					.setClientId(azurSynchTab.getClientId());
			return new LDAPUserSyncConfiguration(azurUserSynchConfig, getUserFilter(), getGroupsFilter(),
					scheduleComponentField.getPeriod(), selectedCollections());
		}

		private String getClientId() {
			return (String) clientId.getValue();
		}
	}

	private class DefaultAuthenticationTab extends VerticalLayout {
		private CheckBox followReferences;
		private Field activateAuthentication;

		private StringListComponent urlsField;
		private StringListComponent domainsField;

		private DefaultAuthenticationTab() {
			setSizeFull();
			setSpacing(true);

			buildLdapServerConfigComponent(this);
		}

		private void buildLdapServerConfigComponent(VerticalLayout layout) {
			LDAPServerConfiguration ldapServerConfiguration = presenter.getLDAPServerConfiguration();

			activateAuthentication = createCheckmarkField(ldapServerConfiguration.getLdapAuthenticationActive(), true);
			activateAuthentication.setCaption($("ldap.authentication.active"));
			activateAuthentication.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					presenter.setLDAPActive(!presenter.isLDAPActive());
					changePageState(presenter.isLDAPActive());
				}
			});
			layout.addComponent(activateAuthentication);

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

		private void changePageState(boolean state) {
			followReferences.setEnabled(state);
			urlsField.setEnabled(state);
			domainsField.setEnabled(state);
		}

		public LDAPServerConfiguration getLDAPServerConfiguration() {
			return new LDAPServerConfiguration(urlsField.getValues(),
					domainsField.getValues(), getDirectoryType(), presenter.isLDAPActive(),
					followReferences.getValue(), presenter.isLDAPSyncActive());
		}
	}

	private class DefaultSynchTab extends VerticalLayout {
		private StringListComponent groupsField;
		private StringListComponent usersField;
        private CheckBox membershipAutomaticDerivationActivatedCheckbox;
        private StringListComponent userFilterGroupsField;
		private Field userField;
		private Field passwordField;
		private Field activateSynch;

		private DefaultSynchTab() {
			setSizeFull();
			setSpacing(true);

			buildLdapUserSyncConfigComponent(this);
		}

		private void buildLdapUserSyncConfigComponent(VerticalLayout layout) {
			LDAPUserSyncConfiguration ldapUserSyncConfiguration = presenter.getLDAPUserSyncConfiguration();

			activateSynch = createCheckmarkField(getLDAPServerConfiguration().getLdapSyncActive(), true);
			activateSynch.setCaption($("ldap.synchronization.active"));
			activateSynch.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					presenter.setLDAPSyncActive(!presenter.isLDAPSyncActive());
					changePageState(presenter.isLDAPSyncActive());
				}
			});
			addComponent(activateSynch);

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
			passwordField = new PasswordField(
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

			changePageState(getLDAPServerConfiguration().getLdapSyncActive());
		}

		private void changePageState(boolean state) {
			scheduleComponentField.setEnabled(state);
			collectionsComponent.setEnabled(state);

			scheduleComponentField.setEnabled(state);
			collectionsComponent.setEnabled(state);
			userField.setEnabled(state);
			passwordField.setEnabled(state);
			groupsField.setEnabled(state);
			groupsAcceptanceRegexField.setEnabled(state);
			groupsRejectionRegexField.setEnabled(state);
			usersField.setEnabled(state);
			userFilterGroupsField.setEnabled(state);
			usersAcceptanceRegexField.setEnabled(state);
			usersRejectionRegexField.setEnabled(state);
			membershipAutomaticDerivationActivatedCheckbox.setEnabled(state);

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
					userFilterGroupsField.getValues(),	membershipAutomaticDerivationActivatedCheckbox.getValue(), selectedCollections());
		}
	}

	private String notNull(Field field) {
		return (field.getValue() != null)? field.getValue().toString() : "";
	}
}
