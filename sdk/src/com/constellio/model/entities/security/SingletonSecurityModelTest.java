package com.constellio.model.entities.security;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class SingletonSecurityModelTest {

	@Mock User user;
	@Mock RecordId recordId;
	@Mock Authorization authorization;
	private final String authorizationId = "fakeAuthId";
	private SingletonSecurityModel singletonSecurityModel;

	@Before
	public void setUp() {
		initMocks(this);

		when(user.getWrappedRecordId()).thenReturn(recordId);
		when(authorization.getId()).thenReturn(authorizationId);

		singletonSecurityModel = SingletonSecurityModel.empty("zeCollection");
		singletonSecurityModel.updateCache(singletonList(authorization), Collections.emptyList());
	}

	@Test
	public void whenGetCachedValueAndCachedUserSecurityCacheEmptyThenValueCached() {
		String value = singletonSecurityModel.getCachedValue(user, "valueKey", () -> "value1");
		assertThat(value).isEqualTo("value1");
		value = singletonSecurityModel.getCachedValue(user, "valueKey", () -> "value2");
		assertThat(value).isEqualTo("value1");
		assertThat(singletonSecurityModel.cachedUserSecurityValues.get(user.getWrappedRecordId()).get("valueKey")).isEqualTo("value1");
	}

	@Test
	public void whenUserSecurityValueCachedAndAuthRemovedThenCacheCleared() {
		String value = singletonSecurityModel.getCachedValue(user, "valueKey", () -> "value1");
		assertThat(value).isEqualTo("value1");
		singletonSecurityModel.removeAuth(authorizationId);
		assertThat(singletonSecurityModel.cachedUserSecurityValues).isEmpty();
		value = singletonSecurityModel.getCachedValue(user, "valueKey", () -> "value2");
		assertThat(value).isEqualTo("value2");
		assertThat(singletonSecurityModel.cachedUserSecurityValues.get(user.getWrappedRecordId()).get("valueKey")).isEqualTo("value2");
	}

	@Test
	public void whenUserSecurityValueCachedAndUpdateCacheThenCacheCleared() {
		String value = singletonSecurityModel.getCachedValue(user, "valueKey", () -> "value1");
		assertThat(value).isEqualTo("value1");
		singletonSecurityModel.updateCache(Collections.emptyList(), Collections.emptyList());
		assertThat(singletonSecurityModel.cachedUserSecurityValues).isEmpty();
		value = singletonSecurityModel.getCachedValue(user, "valueKey", () -> "value2");
		assertThat(value).isEqualTo("value2");
		assertThat(singletonSecurityModel.cachedUserSecurityValues.get(user.getWrappedRecordId()).get("valueKey")).isEqualTo("value2");
	}

}
