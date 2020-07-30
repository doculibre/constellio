package com.constellio.app.modules.tasks.caches;

import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheOptions;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.services.caches.UserCache;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;
import static com.constellio.model.services.records.RecordUtils.toWrappedRecordIdsSet;

public class UnreadTasksUserCache implements UserCache {

	public static final String NAME = TaskModule.ID + ":unreadTasks";

	ConstellioCache cache;

	AppLayerFactory appLayerFactory;

	UserServices userServices;

	public UnreadTasksUserCache(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		this.cache = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getDistributedCacheManager()
				.createCache(NAME, new ConstellioCacheOptions());
	}

	@Override
	public void invalidateUser(String username) {
		for (User user : getUserForEachCollection(username)) {
			invalidateUser(user);
		}
	}


	public List<User> getUserForEachCollection(String username) {

		List<User> users = new ArrayList<>();
		SystemWideUserInfos userCredential = userServices.getUserInfos(username);
		for (String collection : userCredential.getCollections()) {
			users.add(userServices.getUserInCollection(userCredential.getUsername(), collection));
		}

		return users;
	}

	@Override
	public void invalidateUsersInGroup(SystemWideGroup globalGroup) {
		for (Group group : getGroupForEachCollection(globalGroup)) {
			invalidateGroup(group);
		}
	}

	public List<Group> getGroupForEachCollection(SystemWideGroup globalGroup) {

		List<Group> groups = new ArrayList<>();
		for (String collection : globalGroup.getCollections()) {
			groups.add(userServices.getGroupInCollection(globalGroup.getCode(), collection));
		}

		return groups;
	}

	public void invalidateUser(User user) {
		cache.remove(user.getId());
	}


	public void invalidateGroup(Group group) {
		cache.removeAll(toWrappedRecordIdsSet(userServices.getAllUsersInGroup(group, true, false)));
	}

	@Override
	public void invalidateAll() {
		cache.clear();
	}

	@Override
	public ConstellioCache getCache() {
		return cache;
	}

	public Long getCachedUnreadTasks(User user) {
		return cache.get(user.getId());
	}

	public void insertUnreadTasks(User user, long value) {
		cache.put(user.getId(), value, WAS_OBTAINED);
	}
}
