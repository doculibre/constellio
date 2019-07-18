package com.constellio.model.extensions;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils;
import com.constellio.data.frameworks.extensions.ExtensionUtils.BooleanCaller;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordCacheExtension;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.behaviors.UserAuthenticationExtension;
import com.constellio.model.extensions.events.recordsCache.CacheHitParams;
import com.constellio.model.extensions.events.recordsCache.CacheMissParams;
import com.constellio.model.extensions.events.recordsCache.CachePutParams;
import com.constellio.model.extensions.events.recordsCache.CacheQueryParams;
import com.constellio.model.extensions.params.CanAuthenticateUsingPasswordFileIfLDAPFailedParams;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class ModelLayerSystemExtensions {

	//------------ Extension points -----------

	public VaultBehaviorsList<RecordExtension> recordExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<RecordCacheExtension> recordCacheExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<UserAuthenticationExtension> userAuthenticationExtensions = new VaultBehaviorsList<>();

	//----------------- Callers ---------------

	public void onGetByUniqueMetadataCacheHit(Record record, Metadata metadata, String value, long duration) {
		for (RecordCacheExtension extension : recordCacheExtensions) {
			extension.onCacheHit(new CacheHitParams(metadata, value, record, duration));
		}
	}

	public void onGetByIdCacheHit(Record record, long duration) {
		for (RecordCacheExtension extension : recordCacheExtensions) {
			extension.onCacheHit(new CacheHitParams(Schemas.IDENTIFIER, record.getId(), record, duration));
		}
	}

	public void onGetByUniqueMetadataCacheMiss(Metadata metadata, String value, long duration) {
		for (RecordCacheExtension extension : recordCacheExtensions) {
			extension.onCacheMiss(new CacheMissParams(metadata, value, duration));
		}
	}

	public void onGetByIdCacheMiss(String id, long duration) {
		for (RecordCacheExtension extension : recordCacheExtensions) {
			extension.onCacheMiss(new CacheMissParams(Schemas.IDENTIFIER, id, duration));
		}
	}

	public void onPutInCache(Record record, long duration) {
		for (RecordCacheExtension extension : recordCacheExtensions) {
			extension.onCachePut(new CachePutParams(record, duration));
		}
	}

	public void onQueryExecution(LogicalSearchQuery query, long duration) {
		for (RecordCacheExtension extension : recordCacheExtensions) {
			extension.onCacheQuery(new CacheQueryParams(query, duration));
		}
	}

	public boolean canAuthenticateUsingPasswordFileIfLDAPFailed(final String username) {
		return ExtensionUtils.getBooleanValue(userAuthenticationExtensions, false,
				new BooleanCaller<UserAuthenticationExtension>() {
					@Override
					public ExtensionBooleanResult call(UserAuthenticationExtension behavior) {
						return behavior.canAuthenticateUsingPasswordFileIfLDAPFailed(
								new CanAuthenticateUsingPasswordFileIfLDAPFailedParams(username));
					}
				});
	}
}
