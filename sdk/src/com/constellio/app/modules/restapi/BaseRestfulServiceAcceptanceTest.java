package com.constellio.app.modules.restapi;

import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.HashingUtils;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.CommitCounter;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.setups.Users;
import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.sdk.tests.QueryCounter.ON_COLLECTION;
import static java.util.Arrays.asList;

public abstract class BaseRestfulServiceAcceptanceTest extends ConstellioTest {

	protected String host;
	protected String serviceKey = "bobKey", token = "bobToken";
	protected String dateFormat, dateTimeFormat;
	protected String sasquatchServiceKey = "sasquatchKey", sasquatchToken = "sasquatchToken";
	protected LocalDateTime fakeDate = new LocalDateTime();
	protected LocalDateTime fakeDate2 = (new LocalDateTime()).minusDays(1);
	protected LocalDateTime fakeDate3 = (new LocalDateTime()).minusDays(2);
	protected LocalDateTime fakeDate4 = (new LocalDateTime()).plusDays(3);
	protected LocalDateTime fakeDate5 = (new LocalDateTime()).plusDays(4);
	protected String fakeMetadata1 = "USRMetadata1", fakeMetadata2 = "USRMetadata2";
	protected AuthorizationAddRequest authorization1, authorization2;

	protected RMSchemasRecordsServices rm;
	protected RecordServices recordServices;
	protected UserServices userServices;
	protected SearchServices searchServices;
	protected AuthorizationsServices authorizationsServices;
	protected RMTestRecords records = new RMTestRecords(zeCollection);
	protected Users users = new Users();
	protected MetadataSchemasManager metadataSchemasManager;
	protected AuthorizationsServices authorizationsServices;

	protected WebTarget webTarget;
	protected CommitCounter commitCounter;
	protected QueryCounter queryCounter;

	protected SearchServices searchServices;

	protected static final String NOT_NULL_MESSAGE = "javax.validation.constraints.NotNull.message";
	protected static final String NOT_EMPTY_MESSAGE = "org.hibernate.validator.constraints.NotEmpty.message";
	protected static final String JERSEY_NOT_FOUND_MESSAGE = "HTTP 404 Not Found";
	protected static final String OPEN_BRACE = "{";
	protected static final String CLOSE_BRACE = "}";

	protected void setUpTest() {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioRestApiModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus());
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);

		dateFormat = getModelLayerFactory().getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.DATE_FORMAT);
		dateTimeFormat = getModelLayerFactory().getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.DATE_TIME_FORMAT);

		host = "localhost:7070";

		givenConfig(RestApiConfigs.REST_API_URLS, "localhost:7070; localhost2");
		givenTimeIs(fakeDate);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		searchServices = getModelLayerFactory().newSearchServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();

		userServices.addUpdateUserCredential(users.bob().setServiceKey(serviceKey)
				.addAccessToken(token, TimeProvider.getLocalDateTime().plusYears(1)));
		userServices.addUpdateUserCredential(users.sasquatch().setServiceKey(sasquatchServiceKey)
				.addAccessToken(sasquatchToken, TimeProvider.getLocalDateTime().plusYears(1)));

		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();

		searchServices = getModelLayerFactory().newSearchServices();

		commitCounter = new CommitCounter(getDataLayerFactory());
		queryCounter = new QueryCounter(getDataLayerFactory(), ON_COLLECTION(SYSTEM_COLLECTION));
	}

	protected <T> void addUsrMetadata(String id, final String schemaCode, final MetadataValueType type,
									  T value1, T value2)
			throws Exception {
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder schemaBuilder = types.getSchema(schemaCode);

				if (type == MetadataValueType.REFERENCE) {
					schemaBuilder.create(fakeMetadata1).setType(type).defineReferencesTo(types.getSchemaType(User.SCHEMA_TYPE));
					schemaBuilder.create(fakeMetadata2).setType(type).setMultivalue(true).defineReferencesTo(types.getSchemaType(User.SCHEMA_TYPE));
				} else {
					schemaBuilder.create(fakeMetadata1).setType(type);
					schemaBuilder.create(fakeMetadata2).setType(type).setMultivalue(true);
				}
			}
		});

		if (id != null && value1 != null && value2 != null) {
			Record record = recordServices.getDocumentById(id);
			MetadataSchema schema = metadataSchemasManager.getSchemaOf(record);
			record.set(schema.getMetadata(fakeMetadata1), value1);
			record.set(schema.getMetadata(fakeMetadata2), value2);
			recordServices.update(record);
		}
	}

	protected List<Authorization> filterInheritedAuthorizations(List<Authorization> authorizations, String recordId) {
		List<Authorization> filteredAuthorizations = Lists.newArrayList();
		for (Authorization authorization : authorizations) {
			if (authorization.getTarget().equals(recordId)) {
				filteredAuthorizations.add(authorization);
			}
		}
		return filteredAuthorizations;
	}

	protected WebTarget buildQuery(WebTarget target, boolean calculateSignature, List<String> defaultParams,
								   String... excludedParam) throws Exception {
		return buildQuery(getClass(), target, calculateSignature, defaultParams, excludedParam);
	}

	protected WebTarget buildQuery(Class clazz, WebTarget target, boolean calculateSignature,
								   List<String> defaultParams, String... excludedParam)
			throws Exception {
		List<String> excludedParams = excludedParam == null ? new ArrayList<String>() : asList(excludedParam);

		for (String param : defaultParams) {
			if (excludedParams.contains(param)) {
				continue;
			}

			if (param.equals("signature")) {
				String signature = calculateSignature ?
								   calculateSignature(clazz, defaultParams.toArray(new String[0])) : "123";
				target = target.queryParam(param, signature);
			} else {
				Field field = getField(clazz, param);
				field.setAccessible(true);
				Object value = field.get(this);
				if (value == null) {
					continue;
				}
				target = target.queryParam(param, value);
			}
		}
		return target;
	}

	private String calculateSignature(Class clazz, String... params)
			throws Exception {
		String data = host;
		for (String param : params) {
			if (param.equals("signature")) {
				continue;
			}
			if (param.equals("method")) {
				data = data.concat(getSchemaType().name());
			}
			Field field = getField(clazz, param);
			field.setAccessible(true);
			String value = String.valueOf(field.get(this));
			data = !value.equals("null") ? data.concat(value) : data;
		}
		return HashingUtils.hmacSha256Base64UrlEncoded(token, data);
	}

	private static Field getField(Class clazz, String fieldName) throws Exception {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}

	protected LocalDate toLocalDate(String date) {
		return date != null ? DateUtils.parseLocalDate(date, dateFormat) : null;
	}

	protected LocalDateTime toLocalDateTime(String date) {
		return date != null ? DateUtils.parseLocalDateTime(date, dateTimeFormat) : null;
	}

	protected String toDateString(LocalDate date) {
		return date != null ? DateUtils.format(date, dateFormat) : null;
	}

	protected String toDateString(LocalDateTime date) {
		return date != null ? DateUtils.format(date, dateTimeFormat) : null;
	}

	protected void resetCounters() {
		commitCounter.reset();
		queryCounter.reset();
	}

	protected Set<String> toPrincipals(Collection<String> ids) {
		Set<String> principals = new HashSet<>();
		for (String id : ids) {
			Record record = recordServices.getDocumentById(id);
			if (record.isOfSchemaType(User.SCHEMA_TYPE)) {
				principals.add(record.<String>get(rm.user.username()));
			} else {
				principals.add(record.<String>get(rm.group.code()));
			}
		}
		return principals;
	}

	protected void createAuthorizations(Record record) {
		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		authorization1 = authorizationInCollection(zeCollection).forUsers(users.bobIn(zeCollection))
				.on(record).givingReadWriteDeleteAccess();
		authorizationsServices.add(authorization1, users.adminIn(zeCollection));
		authorization2 = authorizationInCollection(zeCollection).forUsers(users.aliceIn(zeCollection))
				.on(record).givingReadWriteAccess();
		authorizationsServices.add(authorization2, users.adminIn(zeCollection));
	}

	protected List<Authorization> filterInheritedAuthorizations(List<Authorization> authorizations, String recordId) {
		List<Authorization> filteredAuthorizations = Lists.newArrayList();
		for (Authorization authorization : authorizations) {
			if (authorization.getTarget().equals(recordId)) {
				filteredAuthorizations.add(authorization);
			}
		}
		return filteredAuthorizations;
	}

	protected List<String> toPrincipalIds(Collection<String> principals) {
		List<String> principalIds = new ArrayList<>(principals.size());
		for (String principal : principals) {
			Record record = recordServices.getRecordByMetadata(rm.user.username(), principal);
			if (record == null) {
				record = recordServices.getRecordByMetadata(rm.group.code(), principal);
			}
			principalIds.add(record.getId());
		}
		return principalIds;
	}

	abstract protected SchemaTypes getSchemaType();


	protected List<String> toPrincipalIds(Collection<String> principals) {
		List<String> principalIds = new ArrayList<>(principals.size());
		for (String principal : principals) {
			Record record = recordServices.getRecordByMetadata(rm.user.username(), principal);
			if (record == null) {
				record = recordServices.getRecordByMetadata(rm.group.code(), principal);
			}
			principalIds.add(record.getId());
		}
		return principalIds;
	}

}
