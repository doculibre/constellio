package com.constellio.app.ui.pages.management.ldap;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.joda.time.Duration;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.CollectionsSelectionPanel;
import com.constellio.app.ui.framework.components.DurationPanel;
import com.constellio.app.ui.framework.components.StringListComponent;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.RegexFilter;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class LDAPConfigManagementViewImpl extends BaseViewImpl implements LDAPConfigManagementView {
    private LDAPConfigManagementPresenter presenter;

    private Button saveButton;
    private CheckBox ldapAuthenticationActive;
    private CheckBox followReferences;
    private DurationPanel durationField;

    private Field directoryTypeField;
    private String previousDirectoryType;
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
    private CollectionsSelectionPanel collectionsComponent;


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
        buildSaveAndTestButtonsPanel(layout);
        return layout;
    }

    private void buildSaveAndTestButtonsPanel(final VerticalLayout layout) {
        Panel buttonsPanel = new Panel();
        buttonsPanel.setSizeUndefined();
        buttonsPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);

        Button testButton = new BaseButton($("ldap.test.button")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                testAuthentication.setVisible(true);
                LDAPServerConfiguration ldapServerConfiguration = new LDAPServerConfiguration(urlsField.getValues(), domainsField.getValues(), getDirectoryType(), ldapAuthenticationActive.getValue(), followReferences.getValue());
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
                LDAPServerConfiguration ldapServerConfigurationVO = new LDAPServerConfiguration(urlsField.getValues(), domainsField.getValues(), getDirectoryType(), ldapAuthenticationActive.getValue(), followReferences.getValue());
                LDAPUserSyncConfiguration ldapUserSyncConfigurationVO = new LDAPUserSyncConfiguration(userField.getValue().toString(), passwordField.getValue().toString(),
                        getUserFilter(), getGroupsFilter(),
                        durationField.getDuration(), groupsField.getValues(), usersField.getValues(), collectionsComponent.getSelectedCollections());
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
        followReferences = new CheckBox($("ldap.authentication.followReferences"));
        followReferences.setValue(ldapServerConfiguration.getFollowReferences());
        layout.addComponent(followReferences);
        LDAPDirectoryType directoryType = ldapServerConfiguration.getDirectoryType();
        directoryTypeField = createEnumField(directoryType, LDAPDirectoryType.class.getEnumConstants());
        previousDirectoryType = (String) directoryTypeField.getValue();
        directoryTypeField.setCaption($("ldap.serverConfiguration.directoryType"));
        layout.addComponent(directoryTypeField);
        directoryTypeField.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                String newValue = (String) event.getProperty().getValue();
                presenter.typeChanged(previousDirectoryType, newValue);
                previousDirectoryType = newValue;
            }
        });
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
        followReferences.setVisible(presenter.isFollowReferencesVisible(directoryType));
        domainsField.setVisible(presenter.isDomainsFieldVisible(directoryType));
        urlsField.setVisible(presenter.isUrlsFieldVisible(directoryType));
        layout.addComponent(domainsField);
    }

    private void buildLdapUserSyncConfigComponent(VerticalLayout layout) {
        String title = $("ImportUsersFileViewImpl.collection");
        collectionsComponent = new CollectionsSelectionPanel(title, presenter.getAllCollections(), presenter.getSelectedCollections());
        layout.addComponent(collectionsComponent);
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

    @Override
    public void refreshTypeDependantFields(LDAPDirectoryType directoryType) {
        followReferences.setVisible(presenter.isFollowReferencesVisible(directoryType));
        domainsField.setVisible(presenter.isDomainsFieldVisible(directoryType));
        urlsField.setVisible(presenter.isUrlsFieldVisible(directoryType));

    }
}
