package com.constellio.data.utils;

import com.constellio.data.io.EncodingService;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadableDuration;

import java.util.HashMap;
import java.util.Map;

import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;
import static com.constellio.data.utils.TimeProvider.getLocalDateTime;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class AuthCache {

	Map<String, AuthCacheEntry> cache = new HashMap<>();

	HashingService hashingService;

	ReadableDuration durationInCache;

	public AuthCache(ReadableDuration durationInCache) {
		this.hashingService = HashingService.forMD5(new EncodingService(), BASE64_URL_ENCODED);
		this.durationInCache = durationInCache;
	}

	public synchronized void insert(String serviceKey, String token, String username) {
		insert(serviceKey + token, username);
	}

	public synchronized void insert(String authKey, String username) {

		if (isNotEmpty(authKey) && username != null) {
			AuthCacheEntry entry = new AuthCacheEntry(username, getLocalDateTime().plus(durationInCache));
			cache.put(sha1(authKey), entry);
		}
	}

	public String get(String serviceKey, String token) {
		return get(serviceKey + token);
	}

	public String get(String authKey) {
		AuthCacheEntry entry = cache.get(sha1(authKey));
		if (entry != null && entry.endingTime.isAfter(getLocalDateTime())) {
			return entry.username;
		} else {
			return null;
		}
	}

	private String sha1(String authKey) {
		try {
			return hashingService.getHashFromString(authKey);
		} catch (HashingServiceException e) {
			throw new ImpossibleRuntimeException(e);
		}
	}

	public static class AuthCacheEntry {
		String username;
		LocalDateTime endingTime;

		public AuthCacheEntry(String username, LocalDateTime endingTime) {
			this.username = username;
			this.endingTime = endingTime;
		}
	}
}
