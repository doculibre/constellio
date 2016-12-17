package com.constellio.app.ui.pages.profile;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.modules.rm.model.enums.DefaultTabInFolderDisplay;
import com.constellio.app.ui.entities.ContentVersionVO;

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
	String loginLanguageCode;

	public ProfileVO(ContentVersionVO image, String username, String firstName, String lastName, String email, String personalEmails,
			String phone, String startTab, DefaultTabInFolderDisplay defaultTabInFolderDisplay, String defaultTaxonomy,
			String password, String confirmPassword, String oldPassword) {
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
	}

	public ProfileVO(String username, String firstName, String lastName, String email, String personalEmails, String phone, String startTab,
			DefaultTabInFolderDisplay defaultTabInFolderDisplay, String defaultTaxonomy, String password, String confirmPassword,
			String oldPassword) {
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
}
