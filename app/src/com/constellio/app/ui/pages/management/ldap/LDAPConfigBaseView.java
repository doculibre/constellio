package com.constellio.app.ui.pages.management.ldap;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.CollectionsSelectionPanel;
import com.constellio.app.ui.framework.components.DurationPanel;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public abstract class LDAPConfigBaseView extends BaseViewImpl implements LDAPConfigManagementView {
	private static final Logger LOGGER = LoggerFactory.getLogger(LDAPConfigBaseView.class);
	protected final LDAPConfigManagementPresenter presenter;

	protected CheckBox ldapAuthenticationActive;
	protected Field directoryTypeField;
	protected String previousDirectoryType;

	protected Field usersAcceptanceRegexField;
	protected Field usersRejectionRegexField;
	protected Field groupsAcceptanceRegexField;
	protected Field groupsRejectionRegexField;
	protected TextArea testAuthentication;
	protected CollectionsSelectionPanel collectionsComponent;
	protected DurationPanel durationField;
	protected Button saveButton;

	protected LDAPConfigBaseView() {
		this.presenter = new LDAPConfigManagementPresenter(this);
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
	public void updateComponents(){

	}

	protected Field createStringField(String value, boolean required) {
		TextField textField = new TextField();
		textField.setRequired(required);
		if (value != null) {
			textField.setValue(value);
		}
		return textField;
	}

	protected Field createEnumField(Enum enumeration, Enum[] constants) {
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

	protected void buildDirectoryTypeField() {
		LDAPServerConfiguration ldapServerConfiguration = presenter.getLDAPServerConfiguration();
		LDAPDirectoryType directoryType = ldapServerConfiguration.getDirectoryType();
		directoryTypeField = createEnumField(directoryType, LDAPDirectoryType.class.getEnumConstants());
		previousDirectoryType = (String) directoryTypeField.getValue();
		directoryTypeField.setCaption($("ldap.serverConfiguration.directoryType"));
		directoryTypeField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				String newValue = (String) event.getProperty().getValue();
				presenter.typeChanged(previousDirectoryType, newValue);
				previousDirectoryType = newValue;
			}
		});
	}

	protected void buildLDAPActiveCheckBox() {
		LDAPServerConfiguration ldapServerConfiguration = presenter.getLDAPServerConfiguration();
		ldapAuthenticationActive = new CheckBox($("ldap.authentication.active"));
		ldapAuthenticationActive.setValue(ldapServerConfiguration.getLdapAuthenticationActive());
	}

	protected void buildUsersAcceptRegex(LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		String usersAcceptanceRegex = ldapUserSyncConfiguration.getUsersFilterAcceptanceRegex();
		usersAcceptanceRegexField = createStringField(usersAcceptanceRegex, false);
		usersAcceptanceRegexField.setCaption($("ldap.syncConfiguration.userFilter.acceptedRegex"));
	}

	protected void buildUsersRejectRegex(LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		String usersRejectionRegex = ldapUserSyncConfiguration.getUsersFilterRejectionRegex();
		usersRejectionRegexField = createStringField(usersRejectionRegex, false);
		usersRejectionRegexField.setCaption($("ldap.syncConfiguration.userFilter.rejectedRegex"));
	}

	protected void buildGroupsAcceptRegex(LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		String groupsAcceptanceRegex = ldapUserSyncConfiguration.getGroupsFilterAcceptanceRegex();
		groupsAcceptanceRegexField = createStringField(groupsAcceptanceRegex, false);
		groupsAcceptanceRegexField.setCaption($("ldap.syncConfiguration.groupFilter.acceptedRegex"));
	}

	protected void buildGroupsRejectRegex(LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		String groupsRejectionRegex = ldapUserSyncConfiguration.getGroupsFilterRejectionRegex();
		groupsRejectionRegexField = createStringField(groupsRejectionRegex, false);
		groupsRejectionRegexField.setCaption($("ldap.syncConfiguration.groupFilter.rejectedRegex"));
	}

	protected void buildDurationField(LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		Duration duration = ldapUserSyncConfiguration.getDurationBetweenExecution();
		durationField = new DurationPanel();
		durationField.setCaption($("ldap.syncConfiguration.durationBetweenExecution"));
		durationField.setDuration(duration);
	}

	protected void buildCollectionsPanel() {
		String title = $("ImportUsersFileViewImpl.collection");
		collectionsComponent = new CollectionsSelectionPanel(title, presenter.getAllCollections(),
				presenter.getSelectedCollections());
	}

	protected void buildSaveAndTestButtonsPanel(final VerticalLayout layout) {
		Panel buttonsPanel = new Panel();
		buttonsPanel.setSizeUndefined();
		buttonsPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);

		Button testButton = new BaseButton($("ldap.test.button")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				try{
					testAuthentication.setVisible(true);
					LDAPServerConfiguration ldapServerConfiguration = getLDAPServerConfiguration();

					LDAPUserSyncConfiguration ldapUserSyncConfiguration = getLDAPUserSyncConfiguration();
					testAuthentication.setValue(
							presenter.getAuthenticationResultMessage(ldapServerConfiguration, getAuthenticationUser(), getAuthenticationPassword()) + "\n"
									+ presenter.getSynchResultMessage(ldapServerConfiguration, ldapUserSyncConfiguration));
					layout.replaceComponent(testAuthentication, testAuthentication);
				}catch(Throwable e){
					LOGGER.warn("Error when testing ldap configuration ", e);
					showErrorMessage(e.getMessage());
				}

			}
		};

		saveButton = new BaseButton($("save")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				LDAPServerConfiguration ldapServerConfigurationVO = getLDAPServerConfiguration();
				LDAPUserSyncConfiguration ldapUserSyncConfigurationVO = getLDAPUserSyncConfiguration();
				presenter.saveConfigurations(ldapServerConfigurationVO, ldapUserSyncConfigurationVO);
			}
		};
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		HorizontalLayout hLayout = new HorizontalLayout(testButton, saveButton);
		hLayout.addComponent(saveButton);
		buttonsPanel.setContent(hLayout);
		layout.addComponent(buttonsPanel);
		layout.setComponentAlignment(buttonsPanel, Alignment.BOTTOM_RIGHT);
	}

	protected abstract String getAuthenticationPassword();

	protected abstract String getAuthenticationUser();

	protected RegexFilter getGroupsFilter() {
		return new RegexFilter(groupsAcceptanceRegexField.getValue().toString(), groupsRejectionRegexField.getValue().toString());
	}

	protected RegexFilter getUserFilter() {
		return new RegexFilter(usersAcceptanceRegexField.getValue().toString(), usersRejectionRegexField.getValue().toString());
	}

	protected LDAPDirectoryType getDirectoryType() {
		return LDAPDirectoryType.valueOf(directoryTypeField.getValue().toString());
	}

	protected List<String> selectedCollections(){
		return collectionsComponent.getSelectedCollections();
	}
	protected abstract LDAPUserSyncConfiguration getLDAPUserSyncConfiguration();

	protected abstract LDAPServerConfiguration getLDAPServerConfiguration();
}
