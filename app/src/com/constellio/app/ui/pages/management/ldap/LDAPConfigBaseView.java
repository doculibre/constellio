package com.constellio.app.ui.pages.management.ldap;

import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.CollectionsSelectionPanel;
import com.constellio.app.ui.framework.components.ScheduleComponent;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.services.users.sync.LDAPUserSyncManager.LDAPSynchProgressionInfo;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class LDAPConfigBaseView extends BaseViewImpl implements LDAPConfigManagementView {
	private static final Logger LOGGER = LoggerFactory.getLogger(LDAPConfigBaseView.class);
	protected final LDAPConfigManagementPresenter presenter;

	protected Field directoryTypeField;
	protected String previousDirectoryType;

	protected Field usersAcceptanceRegexField;
	protected Field usersRejectionRegexField;
	protected Field groupsAcceptanceRegexField;
	protected Field groupsRejectionRegexField;
	protected TextArea testAuthentication;
	protected CollectionsSelectionPanel collectionsComponent;
	protected ScheduleComponent scheduleComponentField;
	protected Button saveButton;
	private BaseButton forceUsersSynchronization;
	protected CheckBox activateLDAPCheckBox;

	protected Button deleteUnusedUserButton, activateLDAPButton;


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
	public void updateComponents() {
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

	protected void buildSynchronizationScheduleFields(LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		scheduleComponentField = new ScheduleComponent(ldapUserSyncConfiguration.getScheduleTime(), ldapUserSyncConfiguration.getDurationBetweenExecution());
	}

	protected void buildCollectionsPanel() {
		String title = $("ImportUsersFileViewImpl.collection");
		collectionsComponent = new CollectionsSelectionPanel(title, presenter.getAllCollections(),
				presenter.getSelectedCollections());
	}

	protected void buildButtonsPanel(final VerticalLayout layout) {
		Panel buttonsPanel = new Panel();
		buttonsPanel.setSizeUndefined();
		buttonsPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);

		Button testButton = new BaseButton($("ldap.test.button")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				try {
					testAuthentication.setVisible(true);
					LDAPServerConfiguration ldapServerConfiguration = getLDAPServerConfiguration();

					LDAPUserSyncConfiguration ldapUserSyncConfiguration = getLDAPUserSyncConfiguration();
					testAuthentication.setValue(
							presenter.getAuthenticationResultMessage(ldapServerConfiguration, getAuthenticationUser(),
									getAuthenticationPassword()) + "\n"
							+ presenter.getSynchResultMessage(ldapServerConfiguration, ldapUserSyncConfiguration));
					layout.replaceComponent(testAuthentication, testAuthentication);
				} catch (Throwable e) {
					LOGGER.warn("Error when testing ldap configuration ", e);
					showErrorMessage(e.getMessage());
				}

			}
		};

		forceUsersSynchronization = new BaseButton($("ldap.forceSynch.button")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				LDAPSynchProgressionInfo progression = presenter
						.forceSynchronization();
			}
		};
		forceUsersSynchronization.setVisible(presenter.isForceSynchVisible());

		saveButton = new BaseButton($("save")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				LDAPServerConfiguration ldapServerConfigurationVO = getLDAPServerConfiguration();
				LDAPUserSyncConfiguration ldapUserSyncConfigurationVO = getLDAPUserSyncConfiguration();
				presenter.saveConfigurations(ldapServerConfigurationVO, ldapUserSyncConfigurationVO);
				forceUsersSynchronization.setVisible(presenter.isForceSynchVisible());
			}
		};
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		HorizontalLayout hLayout = new HorizontalLayout(testButton, forceUsersSynchronization, saveButton);
		hLayout.setSpacing(true);
		hLayout.addComponent(saveButton);
		buttonsPanel.setContent(hLayout);
		layout.addComponent(buttonsPanel);
		layout.setComponentAlignment(buttonsPanel, Alignment.BOTTOM_RIGHT);
	}

	protected void buildActionCheckbox() {

		activateLDAPCheckBox = new CheckBox($("ldap.authentication.active"));
		activateLDAPCheckBox.setValue(presenter.isLDAPActive());
		activateLDAPCheckBox.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				presenter.setLDAPActive(!presenter.isLDAPActive());
				if (presenter.isLDAPActive()) {
					ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
							presenter.isLDAPActive() ? $("ldap.authentication.active.caption") : $("ldap.authentication.inactive.caption"),
							presenter.isLDAPActive() ? $("ldap.authentication.active.msg") : $("ldap.authentication.inactive.msg"),
							$("OK"),
							null,
							null);
					confirmDialog.getOkButton().addClickListener(new ClickListener() {
						@Override
						public void buttonClick(ClickEvent event) {
						}
					});
					confirmDialog.getCancelButton().addClickListener(new ClickListener() {
						@Override
						public void buttonClick(ClickEvent event) {
							activateLDAPCheckBox.setValue(!presenter.isLDAPActive());
						}
					});
					confirmDialog.show(UI.getCurrent(), new ConfirmDialog.Listener() {
						@Override
						public void onClose(ConfirmDialog dialog) {
						}
					}, true);
				}
			}
		});
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

	protected List<String> selectedCollections() {
		return collectionsComponent.getSelectedCollections();
	}

	protected abstract LDAPUserSyncConfiguration getLDAPUserSyncConfiguration();

	protected abstract LDAPServerConfiguration getLDAPServerConfiguration();
}
