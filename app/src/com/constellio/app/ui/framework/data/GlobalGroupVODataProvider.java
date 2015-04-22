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
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.framework.builders.GlobalGroupToVOBuilder;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserServices;

@SuppressWarnings("serial")
public class GlobalGroupVODataProvider implements DataProvider {

	private transient UserServices userServices;
	private transient GlobalGroupsManager globalGroupsManager;
	private transient Integer size = null;
	private transient List<GlobalGroupVO> filteredGlobalGroupVOs;
	private transient List<GlobalGroupVO> globalGroupVOs;
	private GlobalGroupToVOBuilder voBuilder;
	private String filter;

	public GlobalGroupVODataProvider(GlobalGroupToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory) {
		this.voBuilder = voBuilder;
		init(modelLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory());
	}

	void init(ModelLayerFactory modelLayerFactory) {
		globalGroupsManager = modelLayerFactory.getGlobalGroupsManager();
		userServices = modelLayerFactory.newUserServices();
		loadGlobalGroupsVOs();
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {

		filteredGlobalGroupVOs = new ArrayList<>();
		if (filter != null) {
			this.filter = filter.toLowerCase();
			for (GlobalGroupVO globalGroupVO : globalGroupVOs) {
				if (globalGroupVO.getCode().toLowerCase().contains(this.filter)
						|| (globalGroupVO.getName() != null && globalGroupVO.getName().toLowerCase().contains(this.filter))) {
					filteredGlobalGroupVOs.add(globalGroupVO);
				}
			}
		} else {
			this.filter = null;
			filteredGlobalGroupVOs.addAll(globalGroupVOs);
		}
	}

	public List<GlobalGroupVO> getGlobalGroupVOs() {
		return filteredGlobalGroupVOs;
	}

	public void setGlobalGroupVOs(List<GlobalGroupVO> globalGroupVOs) {
		this.globalGroupVOs = globalGroupVOs;
		setFilter(null);
	}

	public GlobalGroupVO getGlobalGroupVO(String code) {
		GlobalGroupVO globalGroupVO = null;
		for (GlobalGroupVO newGlobalGroupVO : filteredGlobalGroupVOs) {
			if (newGlobalGroupVO.getCode().equals(code)) {
				globalGroupVO = newGlobalGroupVO;
			}
		}
		return globalGroupVO != null ? globalGroupVO : null;
	}

	public GlobalGroupVO getGlobalGroupVO(Integer index) {
		GlobalGroupVO globalGroupVO = filteredGlobalGroupVOs.get(index);
		return globalGroupVO != null ? globalGroupVO : null;
	}

	public int size() {
		return filteredGlobalGroupVOs.size();
	}

	public List<Integer> list() {
		List<Integer> indexes = new ArrayList<>();
		for (int i = 0; i < filteredGlobalGroupVOs.size(); i++) {
			GlobalGroupVO globalGroupVO = filteredGlobalGroupVOs.get(i);
			if (globalGroupVO != null) {
				indexes.add(i);
			}
		}
		return indexes;
	}

	public List<String> listCodes(List<GlobalGroupVO> globalGroupVOs) {
		List<String> codes = new ArrayList<>();
		for (GlobalGroupVO globalGroupVO : globalGroupVOs) {
			codes.add(globalGroupVO.getCode());
		}
		return codes;
	}

	private void loadGlobalGroupsVOs() {
		List<GlobalGroupVO> newGlobalGroupVOs = new ArrayList<>();
		List<GlobalGroup> globalGroups = globalGroupsManager.getAllGroups();
		for (GlobalGroup globalGroup : globalGroups) {
			GlobalGroupVO globalGroupVO = voBuilder.build(globalGroup);
			newGlobalGroupVOs.add(globalGroupVO);
		}
		sort(newGlobalGroupVOs);
		setGlobalGroupVOs(newGlobalGroupVOs);
	}

	public List<GlobalGroupVO> listGlobalGroupVOs() {
		return filteredGlobalGroupVOs;
	}

	public List<GlobalGroupVO> listActiveGlobalGroupVOsFromUser(String username) {
		List<GlobalGroupVO> newGlobalGroupVOs = new ArrayList<>();
		for (GlobalGroupVO globalGroupVO : filteredGlobalGroupVOs) {
			List<UserCredential> userCredentials = userServices.getGlobalGroupActifUsers(globalGroupVO.getCode());
			for (UserCredential userCredential : userCredentials) {
				if (userCredential.getUsername().equals(username)) {
					newGlobalGroupVOs.add(globalGroupVO);
				}
			}
		}
		sort(newGlobalGroupVOs);
		return newGlobalGroupVOs;
	}

	public List<GlobalGroupVO> listGlobalGroupVOsNotContainingUser(String username) {

		List<GlobalGroupVO> newGlobalGroupVOs = new ArrayList<>();
		List<GlobalGroupVO> userGlobalGroupVOs = listActiveGlobalGroupVOsFromUser(username);
		List<String> userCodes = listCodes(userGlobalGroupVOs);

		for (GlobalGroupVO globalGroupVO : listGlobalGroupVOs()) {
			if (!userCodes.contains(globalGroupVO.getCode())) {
				newGlobalGroupVOs.add(globalGroupVO);
			}
		}
		sort(newGlobalGroupVOs);
		return newGlobalGroupVOs;
	}

	public List<GlobalGroupVO> listGlobalGroupVOsWithUsersInCollection(String collection) {

		List<GlobalGroupVO> newGlobalGroupVOsInWithUsersInCollection = new ArrayList<>();
		for (GlobalGroupVO globalGroupVO : filteredGlobalGroupVOs) {
			if (globalGroupVO.getCollections().contains(collection)) {
				newGlobalGroupVOsInWithUsersInCollection.add(globalGroupVO);
			}
		}
		sort(newGlobalGroupVOsInWithUsersInCollection);
		return newGlobalGroupVOsInWithUsersInCollection;
	}

	private void sort(List<GlobalGroupVO> globalGroupVOs) {
		Collections.sort(globalGroupVOs, new Comparator<GlobalGroupVO>() {
			@Override
			public int compare(GlobalGroupVO o1, GlobalGroupVO o2) {
				return o1.getCode().toLowerCase().compareTo(o2.getCode().toLowerCase());
			}
		});
	}

	public List<GlobalGroupVO> listActiveSubGlobalGroupsVOsFromGroup(String code) {
		List<GlobalGroupVO> newGlobalGroupVOs = new ArrayList<>();
		for (GlobalGroupVO globalGroupVO : filteredGlobalGroupVOs) {
			if (globalGroupVO.getParent() != null && globalGroupVO.getParent().equals(code)
					&& globalGroupVO.getStatus() == GlobalGroupStatus.ACTIVE) {
				newGlobalGroupVOs.add(globalGroupVO);
			}
		}
		sort(newGlobalGroupVOs);
		return newGlobalGroupVOs;
	}

	public List<GlobalGroupVO> listBaseGlobalGroupsVOs() {
		List<GlobalGroupVO> newGlobalGroupVOs = new ArrayList<>();
		for (GlobalGroupVO globalGroupVO : filteredGlobalGroupVOs) {
			if (globalGroupVO.getParent() == null) {
				newGlobalGroupVOs.add(globalGroupVO);
			}
		}
		sort(newGlobalGroupVOs);
		return newGlobalGroupVOs;
	}

	public List<GlobalGroupVO> listBaseGlobalGroupsVOsWithStatus(GlobalGroupStatus status) {
		List<GlobalGroupVO> globalGroupVOsWithStatus = new ArrayList<>();
		for (GlobalGroupVO globalGroupVO : listBaseGlobalGroupsVOs()) {
			if (status == globalGroupVO.getStatus()) {
				globalGroupVOsWithStatus.add(globalGroupVO);
			}
		}
		return globalGroupVOsWithStatus;
	}
}
