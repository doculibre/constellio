package com.constellio.app.ui.pages.search;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.conversations.ConversationFacetsHandler;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.Message;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

public class ConversationSearchService {
	private final String collection;
	private final SessionContext sessionContext;
	private final ModelLayerFactory modelLayerFactory;
	private final AppLayerFactory appLayerFactory;
	private final ConversationFacetsHandler conversationFacetsHandler;

	private final MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	private final MetadataSchemaTypes schemaTypes;

	public ConversationSearchService(String collection,
									 SessionContext sessionContext,
									 ModelLayerFactory modelLayerFactory,
									 AppLayerFactory appLayerFactory,
									 ConversationFacetsHandler conversationFacetsHandler) {
		this.collection = collection;
		this.sessionContext = sessionContext;
		this.modelLayerFactory = modelLayerFactory;
		this.appLayerFactory = appLayerFactory;
		this.conversationFacetsHandler = conversationFacetsHandler;

		schemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
	}

	public RecordVODataProvider searchInConversation(String conversationId, String searchValue) {

		MetadataSchemaType messageSchemaType = getSchemaType(Message.SCHEMA_TYPE);
		MetadataSchema messageSchema = messageSchemaType.getDefaultSchema();

		MetadataSchemaType documentSchemaType = getSchemaType(Document.SCHEMA_TYPE);
		MetadataSchema documentSchema = documentSchemaType.getDefaultSchema();

		MetadataSchemaVO documentSchemaVO = schemaVOBuilder.build(documentSchema, VIEW_MODE.TABLE, sessionContext);
		DocumentToVOBuilder documentVOBuilder = new DocumentToVOBuilder(modelLayerFactory);

		MetadataSchemaVO metadataSchemaVO = schemaVOBuilder.build(messageSchema, VIEW_MODE.TABLE, sessionContext);
		RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();

		Map<String, RecordToVOBuilder> voBuilders = new HashMap<>();
		voBuilders.put(metadataSchemaVO.getCode(), recordToVOBuilder);
		voBuilders.put(documentSchemaVO.getCode(), documentVOBuilder);

		return new RecordVODataProvider(Arrays.asList(metadataSchemaVO, documentSchemaVO), voBuilders, modelLayerFactory, sessionContext) {

			@Override
			public LogicalSearchQuery getQuery() {
				return buildQueryForFacetsSelection(conversationId, searchValue);
			}
		};
	}

	private LogicalSearchQuery buildSearchConversationQuery(String conversationId) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = modelLayerFactory.newSearchServices();

		MetadataSchemaType folderSchemaType = getSchemaType(Folder.SCHEMA_TYPE);
		MetadataSchema folderSchema = folderSchemaType.getDefaultSchema();

		MetadataSchemaType messageSchemaType = getSchemaType(Message.SCHEMA_TYPE);
		MetadataSchema messageSchema = messageSchemaType.getDefaultSchema();

		MetadataSchemaType documentSchemaType = getSchemaType(Document.SCHEMA_TYPE);

		Folder folder = rm.wrapFolder(searchServices.search(
				new LogicalSearchQuery(from(folderSchemaType).where(folderSchema.get(Folder.CONVERSATION)).is(conversationId))
		).get(0));

		LogicalSearchCondition condition = from(messageSchemaType, documentSchemaType)
				.whereAnyCondition(
						where(messageSchema.get(Message.CONVERSATION)).is(conversationId),
						where(Schemas.PATH_PARTS).isContaining(Collections.singletonList(folder.getId()))
				);


		LogicalSearchQuery query = new LogicalSearchQuery(condition);

		query.filteredByStatus(StatusFilter.ACTIVES);

		conversationFacetsHandler.applyFacetsFilterOnQuery(query);

		return query;
	}

	protected String filterSolrOperators(String expression) {
		String userSearchExpression = expression;

		if (StringUtils.isNotBlank(userSearchExpression) && userSearchExpression.startsWith("\"") && userSearchExpression.endsWith("\"")) {
			userSearchExpression = ClientUtils.escapeQueryChars(userSearchExpression);
			userSearchExpression = "\"" + userSearchExpression + "\"";
		}

		return userSearchExpression;
	}

	MetadataSchemaType getSchemaType(String schemaTypeCode) {
		return schemaTypes.getSchemaType(schemaTypeCode);
	}

	public LogicalSearchQuery buildQueryForFacetsSelection(String conversationId, String searchValue) {
		final String value = searchValue.endsWith("*") ? searchValue : searchValue + "*";
		String userSearchExpression = filterSolrOperators(value);

		LogicalSearchQuery logicalSearchQuery = buildSearchConversationQuery(conversationId);

		logicalSearchQuery.setFreeTextQuery(userSearchExpression);
		logicalSearchQuery.setPreferAnalyzedFields(true);

		if (!"*".equals(value)) {
			logicalSearchQuery.setHighlighting(true);
		}

		return logicalSearchQuery;
	}
}
