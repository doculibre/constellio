package com.constellio.app.modules.restapi.apis.v2.record;

import com.constellio.app.modules.restapi.apis.v2.core.BaseDaoV2;
import com.constellio.app.modules.restapi.apis.v2.core.BaseServiceV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.FilterMode;
import com.constellio.app.modules.restapi.apis.v2.record.dto.QueryDtoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.RecordDtoV2;
import com.constellio.app.modules.restapi.apis.v2.record.dto.RecordsResultDtoV2;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.SPEQueryResponse;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RecordServiceV2 extends BaseServiceV2 {

	@Inject
	private RecordDaoV2 recordDaoV2;
	@Inject
	private RecordAdaptorV2 recordAdaptorV2;

	@Override
	protected BaseDaoV2 getDao() {
		return recordDaoV2;
	}

	public List<RecordDtoV2> getByIds(Set<String> ids, FilterMode filterMode, String token, String host) {
		validateHost(host);

		List<Record> records = recordDaoV2.getRecordsById(ids, filterMode);
		records.forEach(record -> {
			User user = getUserByToken(token, record.getCollection());
			validateUserAccess(user, record, true, false, false);
		});

		return recordAdaptorV2.adaptRecords(records);
	}

	public List<RecordDtoV2> getSuggestions(String collection, String schemaType, String expression,
											FilterMode filterMode, String token, String host) {
		validateHost(host);
		validateCollection(collection);
		validateSchemaType(collection, schemaType);

		User user = getUserByToken(token, collection);

		List<Record> records = recordDaoV2.getAutocompleteSuggestions(user, schemaType, expression, filterMode);
		return recordAdaptorV2.adaptRecords(records);
	}

	public RecordsResultDtoV2 search(QueryDtoV2 query, FilterMode filterMode, String token, String host,
									 List<Locale> acceptLanguages) {
		validateHost(host);

		String collection = query.getCollection();
		validateCollection(collection);

		User user = getUserByToken(token, collection);

		SPEQueryResponse response = recordDaoV2.search(user, query, filterMode);
		return recordAdaptorV2.adapt(response, collection, acceptLanguages);
	}
}
