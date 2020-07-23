package com.constellio.app.modules.tasks.caches;

import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheOptions;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.services.caches.UserCache;
import com.constellio.model.services.users.UserServices;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;
import static com.constellio.model.services.records.RecordUtils.toWrappedRecordIdsSet;

public class IncompleteTasksUserCache implements UserCache {

	public static final String NAME = TaskModule.ID + ":incompleteTasks";

	ConstellioCache cache;

	AppLayerFactory appLayerFactory;

	UserServices userServices;

	public IncompleteTasksUserCache(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		this.cache = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getDistributedCacheManager()
				.createCache(NAME, new ConstellioCacheOptions());
	}

	@Override
	public void invalidateUser(String username) {
		for (User user : userServices.getUserForEachCollection(username)) {
			invalidateUser(user);
		}
	}

	@Override
	public void invalidateUsersInGroup(GlobalGroup globalGroup) {
		for (Group group : userServices.getGroupForEachCollection(globalGroup)) {
			invalidateGroup(group);
		}
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

	public Long getCachedIncompleteTasks(User user) {
		return cache.get(user.getId());
	}

	public void insertIncompleteTasks(User user, long value) {
		cache.put(user.getId(), value, WAS_OBTAINED);
	}
}
