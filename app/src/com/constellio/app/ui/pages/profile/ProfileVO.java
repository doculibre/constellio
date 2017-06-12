package com.constellio.app.ui.pages.profile;

import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.ui.entities.ContentVersionVO;

import java.io.Serializable;

public class ProfileVO implements Serializable {
	ContentVersionVO image;
	String username;
	String firstName;
	String lastName;
	String email;
    String personalEmails;
	String phone;
	String password;
	String confirmPassword;
	String oldPassword;
	String startTab;
	DefaultTabInFolderDisplay defaultTabInFolderDisplay;
	String defaultTaxonomy;
	String defaultAdministrativeUnit;
	String loginLanguageCode;
	String jobTitle;
	String fax;
	String address;
	boolean agentManuallyDisabled;

	public ProfileVO(ContentVersionVO image, String username, String firstName, String lastName, String email, String personalEmails,
			String phone, String startTab, DefaultTabInFolderDisplay defaultTabInFolderDisplay, String defaultTaxonomy,
			String password, String confirmPassword, String oldPassword, boolean agentManuallyDisabled) {
		this.image = image;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
        this.personalEmails = personalEmails;
		this.phone = phone;
		this.startTab = startTab;
		this.defaultTabInFolderDisplay = defaultTabInFolderDisplay;
		this.defaultTaxonomy = defaultTaxonomy;
		this.password = password;
		this.confirmPassword = confirmPassword;
		this.oldPassword = oldPassword;
		this.agentManuallyDisabled = agentManuallyDisabled;
		this.defaultAdministrativeUnit = null;
	}

	public ProfileVO(String username, String firstName, String lastName, String email, String personalEmails, String phone, String startTab,
			DefaultTabInFolderDisplay defaultTabInFolderDisplay, String defaultTaxonomy, String password, String confirmPassword,
			String oldPassword, boolean agentManuallyDisabled) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
        this.personalEmails = personalEmails;
		this.phone = phone;
		this.startTab = startTab;
		this.defaultTabInFolderDisplay = defaultTabInFolderDisplay;
		this.defaultTaxonomy = defaultTaxonomy;
		this.password = password;
		this.confirmPassword = confirmPassword;
		this.oldPassword = oldPassword;
		this.agentManuallyDisabled = agentManuallyDisabled;
		this.defaultAdministrativeUnit = null;
	}

	public ProfileVO(String username, String firstName, String lastName, String email, String personalEmails, String phone, String fax, String jobTitle, String address, String startTab,
					 DefaultTabInFolderDisplay defaultTabInFolderDisplay, String defaultTaxonomy, String password, String confirmPassword,
					 String oldPassword, boolean agentManuallyDisabled, String defaultAdministrativeUnit) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.personalEmails = personalEmails;
		this.phone = phone;
		this.fax = fax;
		this.jobTitle = jobTitle;
		this.address = address;
		this.startTab = startTab;
		this.defaultTabInFolderDisplay = defaultTabInFolderDisplay;
		this.defaultTaxonomy = defaultTaxonomy;
		this.password = password;
		this.confirmPassword = confirmPassword;
		this.oldPassword = oldPassword;
		this.agentManuallyDisabled = agentManuallyDisabled;
		this.defaultAdministrativeUnit = defaultAdministrativeUnit;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

    public String getPersonalEmails() {
        return personalEmails;
    }

    public void setPersonalEmails(String personalEmails) {
        this.personalEmails = personalEmails;
    }

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getStartTab() {
		return startTab;
	}

	public void setStartTab(String startTab) {
		this.startTab = startTab;
	}

	public DefaultTabInFolderDisplay getDefaultTabInFolderDisplay() {
		return defaultTabInFolderDisplay;
	}

	public void setDefaultTabInFolderDisplay(DefaultTabInFolderDisplay defaultTab) {
		this.defaultTabInFolderDisplay = defaultTab;
	}

	public String getDefaultTaxonomy() {
		return defaultTaxonomy;
	}

	public void setDefaultTaxonomy(String defaultTaxonomy) {
		this.defaultTaxonomy = defaultTaxonomy;
	}

	public ContentVersionVO getImage() {
		return image;
	}

	public void setImage(ContentVersionVO image) {
		this.image = image;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getLoginLanguageCode() {
		return loginLanguageCode;
	}

	public void setLoginLanguageCode(String loginLanguageCode) {
		this.loginLanguageCode = loginLanguageCode;
	}

	public boolean isAgentManuallyDisabled() {
		return agentManuallyDisabled;
	}

	public void setAgentManuallyDisabled(boolean agentManuallyDisabled) {
		this.agentManuallyDisabled = agentManuallyDisabled;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getDefaultAdministrativeUnit() {
		return defaultAdministrativeUnit;
	}

	public void setDefaultAdministrativeUnit(String defaultAdministrativeUnit) {
		this.defaultAdministrativeUnit = defaultAdministrativeUnit;
	}
}
