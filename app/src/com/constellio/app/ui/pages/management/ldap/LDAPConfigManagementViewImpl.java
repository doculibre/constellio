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
package com.constellio.app.ui.pages.management.ldap;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.DurationPanel;
import com.constellio.app.ui.framework.components.StringListComponent;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.configs.BaseComboBox;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.RegexFilter;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.hadoop.util.StringUtils;
import org.joda.time.Duration;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class LDAPConfigManagementViewImpl extends BaseViewImpl implements LDAPConfigManagementView {
    private LDAPConfigManagementPresenter presenter;

    private Button saveButton;
    private CheckBox ldapAuthenticationActive;
    private DurationPanel durationField;

    private Field directoryTypeField;
    private StringListComponent urlsField;
    private StringListComponent domainsField;
    private StringListComponent groupsField;
    private StringListComponent usersField;
    private Field userField;
    private Field passwordField;
    private Field usersAcceptanceRegexField;
    private Field usersRejectionRegexField;
    private Field groupsAcceptanceRegexField;
    private Field groupsRejectionRegexField;
    private TextArea testAuthentication;


    public LDAPConfigManagementViewImpl() {
        super();
        this.presenter = new LDAPConfigManagementPresenter(this);
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);

        buildLdapServerConfigComponent(layout);
        buildLdapUserSyncConfigComponent(layout);
        builSaveAndTestButtonsPanel(layout);
        return layout;
    }

    private void builSaveAndTestButtonsPanel(final VerticalLayout layout) {
        Panel buttonsPanel = new Panel();
        buttonsPanel.setSizeUndefined();
        buttonsPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);

        Button testButton = new BaseButton($("ldap.test.button")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                testAuthentication.setVisible(true);
                LDAPServerConfiguration ldapServerConfiguration = new LDAPServerConfiguration(urlsField.getValues(), domainsField.getValues(), getDirectoryType(), ldapAuthenticationActive.getValue());
                LDAPUserSyncConfiguration ldapUserSyncConfiguration = new LDAPUserSyncConfiguration(userField.getValue().toString(), passwordField.getValue().toString(),
                        getUserFilter(), getGroupsFilter(),
                        durationField.getDuration(), groupsField.getValues(), usersField.getValues());
                testAuthentication.setValue(presenter.getAuthenticationResultMessage(ldapServerConfiguration, ldapUserSyncConfiguration) + "\n" + presenter.getSynchResultMessage(ldapServerConfiguration, ldapUserSyncConfiguration));
                layout.replaceComponent(testAuthentication, testAuthentication);
            }
        };


        saveButton = new BaseButton($("save")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                LDAPServerConfiguration ldapServerConfigurationVO = new LDAPServerConfiguration(urlsField.getValues(), domainsField.getValues(), getDirectoryType(), ldapAuthenticationActive.getValue());
                LDAPUserSyncConfiguration ldapUserSyncConfigurationVO = new LDAPUserSyncConfiguration(userField.getValue().toString(), passwordField.getValue().toString(),
                        getUserFilter(), getGroupsFilter(),
                        durationField.getDuration(), groupsField.getValues(), usersField.getValues());
                presenter.saveConfigurations(ldapServerConfigurationVO, ldapUserSyncConfigurationVO);
            }
        };
        saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

        HorizontalLayout hlayout = new HorizontalLayout(testButton, saveButton);
        hlayout.addComponent(saveButton);
        buttonsPanel.setContent(hlayout);
        layout.addComponent(buttonsPanel);
        layout.setComponentAlignment(buttonsPanel, Alignment.BOTTOM_RIGHT);
    }

    private RegexFilter getGroupsFilter() {
        return new RegexFilter(groupsAcceptanceRegexField.getValue().toString(), groupsRejectionRegexField.getValue().toString());
    }

    private RegexFilter getUserFilter() {
        return new RegexFilter(usersAcceptanceRegexField.getValue().toString(), usersRejectionRegexField.getValue().toString());
    }

    private LDAPDirectoryType getDirectoryType() {
        return LDAPDirectoryType.valueOf(directoryTypeField.getValue().toString());
    }

    private void buildLdapServerConfigComponent(VerticalLayout layout) {
        LDAPServerConfiguration ldapServerConfiguration = presenter.getLDAPServerConfiguration();

        ldapAuthenticationActive = new CheckBox($("ldap.authentication.active"));
        ldapAuthenticationActive.setValue(ldapServerConfiguration.getLdapAuthenticationActive());
        layout.addComponent(ldapAuthenticationActive);
        LDAPDirectoryType directoryType = ldapServerConfiguration.getDirectoryType();
        directoryTypeField = createEnumField(directoryType, LDAPDirectoryType.class.getEnumConstants());
        directoryTypeField.setCaption($("ldap.serverConfiguration.directoryType"));
        layout.addComponent(directoryTypeField);
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

    private void buildLdapUserSyncConfigComponent(VerticalLayout layout) {
        LDAPUserSyncConfiguration ldapUserSyncConfiguration = presenter.getLDAPUserSyncConfiguration();
        Duration duration = ldapUserSyncConfiguration.getDurationBetweenExecution();
        durationField = new DurationPanel();
        durationField.setCaption($("ldap.syncConfiguration.durationBetweenExecution"));
        durationField.setDuration(duration);
        layout.addComponent(durationField);
        List<String> groups = ldapUserSyncConfiguration.getGroupBaseContextList();
        groupsField = new StringListComponent();
        groupsField.setCaption($("ldap.syncConfiguration.groupsBaseContextList"));
        groupsField.setValues(groups);
        layout.addComponent(groupsField);
        List<String> users = ldapUserSyncConfiguration.getUsersWithoutGroupsBaseContextList();
        usersField = new StringListComponent();
        usersField.setCaption($("ldap.syncConfiguration.usersWithoutGroupsBaseContextList"));
        usersField.setValues(users);
        layout.addComponent(usersField);
        String user = ldapUserSyncConfiguration.getUser();
        userField = createStringField(user, true);
        userField.setCaption($("ldap.syncConfiguration.user.login"));
        layout.addComponent(userField);
        String password = ldapUserSyncConfiguration.getPassword();
        passwordField = new PasswordField($("ldap.syncConfiguration.user.password"));//PasswordField($("ldap.syncConfiguration.user.password"));
        passwordField.setValue(password);
        passwordField.setRequired(true);
        layout.addComponent(passwordField);
        String usersAcceptanceRegex = ldapUserSyncConfiguration.getUsersFilterAcceptanceRegex();
        usersAcceptanceRegexField = createStringField(usersAcceptanceRegex, false);
        usersAcceptanceRegexField.setCaption($("ldap.syncConfiguration.userFilter.acceptedRegex"));
        layout.addComponent(usersAcceptanceRegexField);
        String usersRejectionRegex = ldapUserSyncConfiguration.getUsersFilterRejectionRegex();
        usersRejectionRegexField = createStringField(usersRejectionRegex, false);
        usersRejectionRegexField.setCaption($("ldap.syncConfiguration.userFilter.rejectedRegex"));
        layout.addComponent(usersRejectionRegexField);
        String groupsAcceptanceRegex = ldapUserSyncConfiguration.getGroupsFilterAcceptanceRegex();
        groupsAcceptanceRegexField = createStringField(groupsAcceptanceRegex, false);
        groupsAcceptanceRegexField.setCaption($("ldap.syncConfiguration.groupFilter.acceptedRegex"));
        layout.addComponent(groupsAcceptanceRegexField);
        String groupsRejectionRegex = ldapUserSyncConfiguration.getGroupsFilterRejectionRegex();
        groupsRejectionRegexField = createStringField(groupsRejectionRegex, false);
        groupsRejectionRegexField.setCaption($("ldap.syncConfiguration.groupFilter.rejectedRegex"));
        layout.addComponent(groupsRejectionRegexField);

        testAuthentication = new TextArea($("ldap.test.results"));
        testAuthentication.setSizeFull();
        testAuthentication.setEnabled(false);
        testAuthentication.setVisible(false);
        layout.addComponent(testAuthentication);
    }

    private Field createStringField(String value, boolean required) {
        TextField textField = new TextField();
        textField.setRequired(required);
        if (value!= null){
            textField.setValue(value);
        }
        return textField;
    }

    private Field createEnumField(Enum enumeration, Enum[] constants) {
        ComboBox combobox = new BaseComboBox();
        combobox.setNullSelectionAllowed(false);
        for (Enum value : constants) {
            combobox.addItem(value.name());
            combobox.setItemCaption(value.name(), $(value.getClass().getSimpleName() + "." + value.name()));
        }
        combobox.setValue(enumeration.name());
        combobox.setRequired(true);
        return combobox;
    }

    @Override
    protected String getTitle() {
        return $("LDAPConfigManagementView.viewTitle");
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
