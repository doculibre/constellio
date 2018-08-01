package com.constellio.model.services.taxonomies.ignite;

import com.constellio.data.dao.services.cache.ignite.ConstellioIgniteCache;
import com.constellio.data.dao.services.cache.ignite.ConstellioIgniteCacheManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServicesCache;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicy;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.configuration.CacheConfiguration;

import javax.cache.Cache;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_OBTAINED;

public class IgniteTaxonomiesSearchServicesCache implements TaxonomiesSearchServicesCache {

	ConstellioIgniteCache constellioIgniteCache;

	public IgniteTaxonomiesSearchServicesCache(ModelLayerFactory modelLayerFactory) {
		ConstellioIgniteCacheManager recordsCacheManager = (ConstellioIgniteCacheManager) modelLayerFactory.getDataLayerFactory()
																										   .getRecordsCacheManager();

		CacheConfiguration<String, Object> cacheCfg = new CacheConfiguration<>("TaxonomiesSearchServicesCache");
		cacheCfg.setCacheMode(CacheMode.PARTITIONED); // Default.
		cacheCfg.setOnheapCacheEnabled(true);
		cacheCfg.setEvictionPolicy(new LruEvictionPolicy<String, Object>(30000));
		cacheCfg.setIndexedTypes(String.class, UserTaxonomyNodeCacheObject.class);

		this.constellioIgniteCache = (ConstellioIgniteCache) recordsCacheManager.getCache(cacheCfg);
	}

	@Override
	public synchronized void insert(String username, String recordId, String mode, Boolean value) {
		if (username != null && recordId != null && mode != null && value != null) {
			UserTaxonomyNodeCacheObject cacheObject = new UserTaxonomyNodeCacheObject(username, recordId, mode, value);
			String cacheKey = cacheObject.getKey();
			constellioIgniteCache.put(cacheKey, cacheObject, WAS_OBTAINED);
		}
	}

	@Override
	public synchronized void invalidateAll() {
		constellioIgniteCache.clear();
	}

	@Override
	public void invalidateWithChildren(String recordId) {
		invalidateRecord(recordId, true);
	}

	@Override
	public void invalidateWithoutChildren(String recordId) {
		invalidateRecord(recordId, false);
	}

	@Override
	public void invalidateRecord(String recordId) {
		invalidateRecord(recordId, null);
	}

	public void invalidateRecord(String recordId, Boolean withChildren) {
		if (recordId != null) {
			String sql = "recordId = ?";
			if (withChildren != null) {
				sql += " AND value = ?";
			}
			SqlQuery<String, UserTaxonomyNodeCacheObject> sqlQuery = new SqlQuery<>(UserTaxonomyNodeCacheObject.class, sql);
			if (withChildren != null) {
				sqlQuery.setArgs(recordId, withChildren);
			} else {
				sqlQuery.setArgs(recordId);
			}

			IgniteCache<String, Object> igniteCache = constellioIgniteCache.getIgniteCache();
			List<Cache.Entry<String, UserTaxonomyNodeCacheObject>> igniteCacheQueryResults = igniteCache.query(sqlQuery).getAll();
			Set<String> removedKeys = new HashSet<>();
			for (Cache.Entry<String, UserTaxonomyNodeCacheObject> entry : igniteCacheQueryResults) {
				String key = entry.getKey();
				removedKeys.add(key);
			}
			if (!removedKeys.isEmpty()) {
				constellioIgniteCache.removeAll(removedKeys);
			}
		}

	}

	@Override
	public synchronized void invalidateUser(String username) {
		if (username != null) {
			String sql = "username = ?";
			SqlQuery<String, UserTaxonomyNodeCacheObject> sqlQuery = new SqlQuery<>(UserTaxonomyNodeCacheObject.class, sql);
			sqlQuery.setArgs(username);

			IgniteCache<String, Object> igniteCache = constellioIgniteCache.getIgniteCache();
			List<Cache.Entry<String, UserTaxonomyNodeCacheObject>> igniteCacheQueryResults = igniteCache.query(sqlQuery).getAll();
			Set<String> removedKeys = new HashSet<>();
			for (Cache.Entry<String, UserTaxonomyNodeCacheObject> entry : igniteCacheQueryResults) {
				String key = entry.getKey();
				removedKeys.add(key);
			}
			if (!removedKeys.isEmpty()) {
				constellioIgniteCache.removeAll(removedKeys);
			}
		}
	}

	@Override
	public synchronized Boolean getCachedValue(String username, String recordId, String mode) {
		if (username != null && recordId != null && mode != null) {
			String key = UserTaxonomyNodeCacheObject.toKey(username, recordId, mode);
			UserTaxonomyNodeCacheObject cachedObject = constellioIgniteCache.get(key);
			return cachedObject != null ? cachedObject.getValue() : null;
		} else {
			return null;
		}
	}

	static class UserTaxonomyNodeCacheObject implements Serializable {

		@QuerySqlField(index = true)
		private String username;

		@QuerySqlField(index = true)
		private String recordId;

		@QuerySqlField(index = true)
		private String mode;

		@QuerySqlField(index = true)
		private Boolean value;

		UserTaxonomyNodeCacheObject() {
			super();
		}

		UserTaxonomyNodeCacheObject(String username, String recordId, String mode, Boolean value) {
			this.username = username;
			this.recordId = recordId;
			this.mode = mode;
			this.value = value;
		}

		public String getKey() {
			return toKey(username, recordId, mode);
		}

		public static String toKey(String username, String recordId, String mode) {
			return "[username=" + username + ", recordId=" + recordId + ", mode=" + mode + "]";
		}

		public Boolean getValue() {
			return value;
		}

		public void setValue(Boolean value) {
			this.value = value;
		}

		public String getUsername() {
			return username;
		}

		public String getRecordId() {
			return recordId;
		}

		public String getMode() {
			return mode;
		}

	}

}
