package com.constellio.app.ui.framework.data;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.framework.builders.GlobalGroupToVOBuilder;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class GlobalGroupVODataProvider extends AbstractDataProvider {

	private transient UserServices userServices;
	private transient Integer size = null;
	private transient List<GlobalGroupVO> filteredGlobalGroupVOs;
	private transient List<GlobalGroupVO> globalGroupVOs;
	private GlobalGroupToVOBuilder voBuilder;
	private String filter;
	private boolean hierarchical;
	private String collection;

	public GlobalGroupVODataProvider(GlobalGroupToVOBuilder voBuilder, ModelLayerFactory modelLayerFactory,
									 boolean hierarchical, String collection) {
		this.voBuilder = voBuilder;
		this.hierarchical = hierarchical;
		this.collection = collection;
		init(modelLayerFactory);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		init(constellioFactories.getModelLayerFactory());
	}

	void init(ModelLayerFactory modelLayerFactory) {
		userServices = modelLayerFactory.newUserServices();
		loadGlobalGroupsVOs();
	}

	public String getFilter() {
		return filter;
	}

	public void setCollection(String collection) {
		this.collection = collection;
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
	//
	//	private doFilter(GLobalGr) {
	//
	//	}

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
		for (SystemWideGroup globalGroup : userServices.getAllGroups()) {
			GlobalGroupVO globalGroupVO = voBuilder.build(globalGroup);
			if (collection == null || globalGroupVO.getCollections().contains(collection)) {
				newGlobalGroupVOs.add(globalGroupVO);
			}
		}
		sort(newGlobalGroupVOs);
		setGlobalGroupVOs(newGlobalGroupVOs);
	}

	public List<GlobalGroupVO> listGlobalGroupVOs() {
		return filteredGlobalGroupVOs;
	}

	public List<GlobalGroupVO> listGlobalGroupVOs(int startIndex, int count) {
		int toIndex = startIndex + count;
		List subList = new ArrayList();
		if (startIndex > filteredGlobalGroupVOs.size()) {
			return subList;
		} else if (toIndex > filteredGlobalGroupVOs.size()) {
			toIndex = filteredGlobalGroupVOs.size();
		}
		return filteredGlobalGroupVOs.subList(startIndex, toIndex);
	}

	public List<GlobalGroupVO> getGlobalGroupThatUserisNotIn(String username) {
		SystemWideUserInfos currentUser = userServices.getUserInfos(username);

		List<GlobalGroupVO> globalGroupVoList = userServices.streamGroupInfos().filter(u -> u.getCollections().contains(collection) && !currentUser.getGroupCodes(this.collection).contains(u.getCode())).map(u -> voBuilder.build(u)).collect(Collectors.toList());

		sort(globalGroupVoList);
		return globalGroupVoList;
	}

	public List<GlobalGroupVO> getGlobalGroupThatUserisIn(String username) {
		SystemWideUserInfos currentUser = userServices.getUserInfos(username);

		List<GlobalGroupVO> globalGroupVoList = userServices.streamGroupInfos().filter(u -> u.getCollections().contains(collection) && currentUser.getGroupCodes(this.collection).contains(u.getCode())).map(u -> voBuilder.build(u)).collect(Collectors.toList());

		sort(globalGroupVoList);
		return globalGroupVoList;
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

	public List<GlobalGroupVO> listActiveGlobalGroupVOsWithUsersInCollection(String collection) {

		List<GlobalGroupVO> newGlobalGroupVOsInWithUsersInCollection = new ArrayList<>();
		for (GlobalGroupVO globalGroupVO : filteredGlobalGroupVOs) {
			GlobalGroupStatus status = globalGroupVO.getStatus();
			if ((status == null || status == GlobalGroupStatus.ACTIVE) &&
				(globalGroupVO.getCollections().contains(collection))) {
				newGlobalGroupVOsInWithUsersInCollection.add(globalGroupVO);
			}
		}
		sort(newGlobalGroupVOsInWithUsersInCollection);
		return newGlobalGroupVOsInWithUsersInCollection;
	}

	public List<GlobalGroupVO> listInactiveGlobalGroupVOsWithUsersInCollection(String collection) {

		List<GlobalGroupVO> newGlobalGroupVOsInWithUsersInCollection = new ArrayList<>();
		for (GlobalGroupVO globalGroupVO : filteredGlobalGroupVOs) {
			GlobalGroupStatus status = globalGroupVO.getStatus();
			if (status == GlobalGroupStatus.INACTIVE &&
				(globalGroupVO.getCollections().contains(collection))) {
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

	public List<GlobalGroupVO> listActiveSubGlobalGroupsVOsFromGroup(String id) {
		List<GlobalGroupVO> newGlobalGroupVOs = new ArrayList<>();


		for (GlobalGroupVO globalGroupVO : filteredGlobalGroupVOs) {
			if (globalGroupVO.getParent() != null && globalGroupVO.getParent().equals(id)
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
			if (!hierarchical || globalGroupVO.getParent() == null) {
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
