package com.constellio.app.modules.restapi.apis.v1.validation.dao;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;
import org.assertj.core.util.Maps;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ValidationDaoTest {

	@Mock private RecordServices recordServices;
	@Mock private UserServices userServices;
	@Mock private UserCredential userCredential;
	@Mock private SchemasRecordsServices schemas;
	@Mock private MetadataSchemasManager metadataSchemasManager;

	@Mock private User user;
	@Mock private Record record;
	@Mock private Metadata metadata;
	@Mock private MetadataSchema metadataSchema;
	@InjectMocks private ValidationDao validationDao;

	private String serviceKey = "serviceKey";
	private String username = "username";
	private String collection = "collection";
	private String token = "token";

	@Before
	public void setUp() {
		initMocks(this);

		when(userServices.getUserConfigs(username)).thenReturn(userCredential);
		when(userServices.getUserInCollection(username, collection)).thenReturn(user);

		when(recordServices.getRecordByMetadata(metadata, serviceKey)).thenReturn(record);

		Map<String, LocalDateTime> tokens = Maps.newHashMap();
		tokens.put(token, TimeProvider.getLocalDateTime().minusDays(5));
		tokens.put("zyxwv", TimeProvider.getLocalDateTime());
		when(userCredential.getAccessTokens()).thenReturn(tokens);

		when(schemas.credentialServiceKey()).thenReturn(metadata);

		when(metadataSchemasManager.getSchemaOf(record)).thenReturn(metadataSchema);

		when(record.get(any(Metadata.class))).thenReturn(username);
	}

	@Test
	public void testGetUserTokens() {
		List<String> tokens = validationDao.getUserTokens(serviceKey);
		assertThat(tokens).containsOnly(token, "zyxwv");
	}

	@Test
	public void testGetUserTokensByDescDate() {
		List<String> tokens = validationDao.getUserTokens(serviceKey, true);
		assertThat(tokens).containsExactly("zyxwv", token);
	}

	@Test
	public void testGetUserTokensUsernameNull() {
		when(recordServices.getRecordByMetadata(metadata, serviceKey)).thenReturn(null);

		List<String> tokens = validationDao.getUserTokens(serviceKey);
		assertThat(tokens).isEmpty();
	}

	@Test
	public void testGetUserTokensUserCredentialNull() {
		when(userServices.getUserConfigs(username)).thenReturn(null);

		List<String> tokens = validationDao.getUserTokens(serviceKey);
		assertThat(tokens).isEmpty();
	}

	@Test
	public void testIsUserAuthenticated() {
		boolean authenticated = validationDao.isUserAuthenticated(token, serviceKey);
		assertThat(authenticated).isTrue();
	}

}
