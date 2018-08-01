package com.constellio.app.ui.util;

import com.constellio.app.api.extensions.params.FilterCapsuleParam;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Capsule;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;

public class CapsuleUtils {

	public static Capsule findCapsule(String collection, String language, String query,
									  LogicalSearchQuery searchQuery) {
		return findCapsule(collection, language, query, searchQuery, null);
	}

	public static Capsule findCapsule(String collection, String language, String query,
									  ModifiableSolrParams solrParams) {
		return findCapsule(collection, language, query, null, solrParams);
	}

	private static Capsule findCapsule(String collection, String language, String query, LogicalSearchQuery searchQuery,
									   ModifiableSolrParams solrParams) {
		Capsule match = null;
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		if (StringUtils.isNotEmpty(query)) {
			String cleanedSearchTerms = AccentApostropheCleaner.cleanAll(query);
			loop1:
			for (Capsule capsule : schemasRecordsServices.getAllCapsules()) {
				boolean validLanguage;
				if (capsule.getLanguage() == null) {
					validLanguage = true;
				} else {
					String languageId = capsule.getLanguage();
					Record languageRecord = recordServices.getDocumentById(languageId);
					String languageCode = languageRecord.get(Schemas.CODE);
					validLanguage = language.equalsIgnoreCase(languageCode);
				}
				if (validLanguage) {
					for (String keyword : capsule.getKeywords()) {
						String cleanedKeyword = AccentApostropheCleaner.cleanAll(keyword);
						if (StringUtils.equalsIgnoreCase(cleanedKeyword, cleanedSearchTerms)) {

							FilterCapsuleParam param = null;
							if (searchQuery != null) {
								param = new FilterCapsuleParam(capsule, searchQuery);
							} else {
								param = new FilterCapsuleParam(capsule, solrParams);
							}

							Capsule filtered = appLayerFactory.getExtensions().forCollection(collection).filter(param);
							if (filtered != null) {
								match = capsule;
								break loop1;
							}
						}
					}
				}
			}
		}
		return match;
	}
}
