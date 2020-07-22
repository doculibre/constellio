package com.constellio.app.ui.framework.data;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.builders.UserCredentialToVOBuilder;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("serial")
public class UserCredentialVODataProvider extends AbstractDataProvider {

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
		List<SystemWideUserInfos> userCredentials;
		if (globalGroupCode != null) {
			userCredentials = userServices.getGlobalGroupActifUsers(globalGroupCode);
		} else {
			userCredentials = userServices.getAllUserCredentials();
		}
		for (SystemWideUserInfos userCredential : userCredentials) {

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

	public List<UserCredentialVO> listActiveUserCredentialVOs() {
		List<UserCredentialVO> activeUserCredentialVOs = new ArrayList<>();
		List<SystemWideUserInfos> userCredentials = userServices.getActiveUserCredentials();
		for (SystemWideUserInfos userCredential : userCredentials) {
			UserCredentialVO userCredentialVO = voBuilder.build(userCredential);
			activeUserCredentialVOs.add(userCredentialVO);
		}
		return activeUserCredentialVOs;
	}

	public UserCredentialVO getUserCredentialVO(String username) {
		SystemWideUserInfos userCredential;
		try {
			userCredential = userServices.getUserInfos(username);
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

		for (UserCredentialVO userCredentialVO : listActiveUserCredentialVOs()) {
			if (!usernamesInGlobalGroup.contains(userCredentialVO.getUsername())) {
				newUserCredentialVOs.add(userCredentialVO);
			}
		}
		sort(newUserCredentialVOs);
		return newUserCredentialVOs;
	}

	private List<String> listUsernamesInGlobalGroup(String globalGroupCode) {
		List<String> usernames = new ArrayList<>();
		for (SystemWideUserInfos userCredential : userServices.getGlobalGroupActifUsers(globalGroupCode)) {
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
