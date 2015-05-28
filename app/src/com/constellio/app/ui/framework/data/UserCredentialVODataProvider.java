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
package com.constellio.app.ui.framework.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.builders.UserCredentialToVOBuilder;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;

@SuppressWarnings("serial")
public class UserCredentialVODataProvider implements DataProvider {

	private transient UserServices userServices;

	private String globalGroupCode;

	private String filter;

	private transient List<UserCredentialVO> userCredentialVOs;

	private transient List<UserCredentialVO> filteredUserCredentialVOs;

	private transient UserCredentialToVOBuilder voBuilder;

	public void setUserCredentialVOs(List<UserCredentialVO> userCredentialVOs) {
		this.userCredentialVOs = userCredentialVOs;
		setFilter(null);
	}

	public UserCredentialVODataProvider(UserCredentialToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory,
			String globalGroupCode) {
		this.voBuilder = voBuilder;
		init(modelLayerFactory, globalGroupCode);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory(), globalGroupCode);
	}

	void init(ModelLayerFactory modelLayerFactory, String groupCode) {
		this.globalGroupCode = groupCode;
		userServices = modelLayerFactory.newUserServices();
		loadUserCredentialVOs();
	}

	private void loadUserCredentialVOs() {
		List<UserCredentialVO> newUserCredentialVOs = new ArrayList<>();
		List<UserCredential> userCredentials;
		if (globalGroupCode != null) {
			userCredentials = userServices.getGlobalGroupActifUsers(globalGroupCode);
		} else {
			userCredentials = userServices.getAllUserCredentials();
		}
		for (UserCredential userCredential : userCredentials) {
			UserCredentialVO userCredentialVO = voBuilder.build(userCredential);
			newUserCredentialVOs.add(userCredentialVO);
		}
		sort(newUserCredentialVOs);
		setUserCredentialVOs(newUserCredentialVOs);
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		filteredUserCredentialVOs = new ArrayList<>();
		if (filter != null) {
			this.filter = filter.toLowerCase();
			for (UserCredentialVO userCredentialVO : userCredentialVOs) {
				if ((userCredentialVO.getUsername() != null && userCredentialVO.getUsername().toLowerCase().contains(this.filter))
						|| (userCredentialVO.getUsername() != null && userCredentialVO.getUsername().toLowerCase()
						.contains(this.filter))
						|| (userCredentialVO.getEmail() != null && userCredentialVO.getEmail().toLowerCase()
						.contains(this.filter))
						|| (userCredentialVO.getFirstName() != null && userCredentialVO.getFirstName().toLowerCase()
						.contains(this.filter))
						|| (userCredentialVO.getLastName() != null && userCredentialVO.getLastName().toLowerCase()
						.contains(this.filter))) {
					filteredUserCredentialVOs.add(userCredentialVO);
				}
			}
		} else {
			this.filter = null;
			filteredUserCredentialVOs.addAll(userCredentialVOs);
		}
	}

	public List<UserCredentialVO> listUserCredentialVOs() {
		return filteredUserCredentialVOs;
	}

	public List<UserCredentialVO> listUserCredentialVOs(int startIndex, int count) {
		int toIndex = startIndex + count;
		List subList = new ArrayList();
		if (startIndex > filteredUserCredentialVOs.size()) {
			return subList;
		} else if (toIndex > filteredUserCredentialVOs.size()) {
			toIndex = filteredUserCredentialVOs.size();
		}
		return filteredUserCredentialVOs.subList(startIndex, toIndex);
	}

	public List<UserCredentialVO> listActifsUserCredentialVOs() {
		List<UserCredentialVO> allUserCredentialVOs = new ArrayList<>();
		List<UserCredential> userCredentials = userServices.getActifUserCredentials();
		for (UserCredential userCredential : userCredentials) {
			UserCredentialVO userCredentialVO = voBuilder.build(userCredential);
			allUserCredentialVOs.add(userCredentialVO);
		}
		return allUserCredentialVOs;
	}

	public UserCredentialVO getUserCredentialVO(String username) {
		UserCredential userCredential;
		try {
			userCredential = userServices.getUser(username);
		} catch (Exception e) {
			return null;
		}
		return voBuilder.build(userCredential);
	}

	public UserCredentialVO getUserCredentialVO(Integer index) {
		UserCredentialVO userCredentialVO = filteredUserCredentialVOs.get(index);
		return userCredentialVO != null ? userCredentialVO : null;
	}

	public int size() {
		return filteredUserCredentialVOs.size();
	}

	public List<Integer> list() {
		List<Integer> indexes = new ArrayList<>();
		for (int i = 0; i < filteredUserCredentialVOs.size(); i++) {
			UserCredentialVO userCredentialVO = filteredUserCredentialVOs.get(i);
			if (userCredentialVO != null) {
				indexes.add(i);
			}
		}
		return indexes;
	}

	public List<UserCredentialVO> listActifsUserCredentialVOsNotInGlobalGroup(String globalGroupCode) {
		List<UserCredentialVO> newUserCredentialVOs = new ArrayList<>();
		List<String> usernamesInGlobalGroup = listUsernamesInGlobalGroup(globalGroupCode);

		for (UserCredentialVO userCredentialVO : listActifsUserCredentialVOs()) {
			if (!usernamesInGlobalGroup.contains(userCredentialVO.getUsername())) {
				newUserCredentialVOs.add(userCredentialVO);
			}
		}
		sort(newUserCredentialVOs);
		return newUserCredentialVOs;
	}

	private List<String> listUsernamesInGlobalGroup(String globalGroupCode) {
		List<String> usernames = new ArrayList<>();
		for (UserCredential userCredential : userServices.getGlobalGroupActifUsers(globalGroupCode)) {
			usernames.add(userCredential.getUsername());
		}
		return usernames;
	}

	private void sort(List<UserCredentialVO> userCredentialVOs) {
		Collections.sort(userCredentialVOs, new Comparator<UserCredentialVO>() {
			@Override
			public int compare(UserCredentialVO o1, UserCredentialVO o2) {
				return o1.getUsername().toLowerCase().compareTo(o2.getUsername().toLowerCase());
			}
		});
	}

	public List<UserCredentialVO> listUserCredentialVOsWithStatus(UserCredentialStatus status) {
		List<UserCredentialVO> userCredentialVOsWithStatus = new ArrayList<>();
		for (UserCredentialVO userCredentialVO : filteredUserCredentialVOs) {
			if (status == userCredentialVO.getStatus()) {
				userCredentialVOsWithStatus.add(userCredentialVO);
			}
		}
		return userCredentialVOsWithStatus;
	}
}
