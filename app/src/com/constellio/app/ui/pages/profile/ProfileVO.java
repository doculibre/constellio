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
package com.constellio.app.ui.pages.profile;

import java.io.Serializable;

import com.constellio.app.modules.rm.model.enums.StartTab;
import com.constellio.app.ui.entities.ContentVersionVO;

public class ProfileVO implements Serializable {

	ContentVersionVO image;

	String username;

	String firstName;

	String lastName;

	String email;

	String phone;

	String password;

	String confirmPassword;

	String oldPassword;

	StartTab startTab;

	String defaultTaxonomy;

	public ProfileVO(ContentVersionVO image, String username, String firstName, String lastName, String email,
			String phone, StartTab startTab, String defaultTaxonomy, String password, String confirmPassword,
			String oldPassword) {
		this.image = image;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phone = phone;
		this.startTab = startTab;
		this.defaultTaxonomy = defaultTaxonomy;
		this.password = password;
		this.confirmPassword = confirmPassword;
		this.oldPassword = oldPassword;
	}

	public ProfileVO(String username, String firstName, String lastName, String email, String phone, StartTab startTab,
			String defaultTaxonomy, String password, String confirmPassword,
			String oldPassword) {
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phone = phone;
		this.startTab = startTab;
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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public StartTab getStartTab() {
		return startTab;
	}

	public void setStartTab(StartTab startTab) {
		this.startTab = startTab;
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
}
