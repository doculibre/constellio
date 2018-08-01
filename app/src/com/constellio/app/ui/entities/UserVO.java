package com.constellio.app.ui.entities;

import com.constellio.model.entities.enums.SearchPageLength;
import com.constellio.model.entities.records.wrappers.User;

import java.util.List;

@SuppressWarnings("serial")
public class UserVO extends RecordVO {

	public UserVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		super(id, metadataValues, viewMode);
	}

	public String getUsername() {
		return get(User.USERNAME);
	}

	public void setUsername(String username) {
		set(User.USERNAME, username);
	}

	public String getFirstName() {
		return get(User.FIRSTNAME);
	}

	public int getDefaultPageLength() {
		try {
			if (getMetadataCodes().contains(User.DEFAULT_PAGE_LENGTH)) {
				SearchPageLength searchPageLength = get(User.DEFAULT_PAGE_LENGTH);
				return searchPageLength == null ? 10 : searchPageLength.getValue();
			} else {
				return 10;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 10;
		}
	}

	public void setFirstName(String firstName) {
		set(User.FIRSTNAME, firstName);
	}

	public String getLastName() {
		return get(User.LASTNAME);
	}

	public void setLastName(String lastName) {
		set(User.LASTNAME, lastName);
	}

	public String getEmail() {
		return get(User.EMAIL);
	}

	public void setEmail(String email) {
		set(User.EMAIL, email);
	}

}
