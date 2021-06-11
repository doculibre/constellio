package com.constellio.app.ui.framework.data;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.conversations.ConversationFacetsHandler;
import com.constellio.app.ui.framework.data.ConversationMessageDataProviderEvents.DataRefreshed;
import com.constellio.app.ui.framework.data.ConversationMessageDataProviderEvents.DataRefreshed.DataRefreshedListener;
import com.constellio.app.ui.framework.data.ConversationMessageDataProviderEvents.DataRefreshed.DataRefreshedObservable;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.SearchPresenter.SortOrder;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.wrappers.Message;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.cache.SerializableSearchCache;
import com.constellio.model.services.search.cache.SerializedCacheSearchService;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;


public class ConversationMessageDataProvider {
	private static Logger LOGGER = LoggerFactory.getLogger(ConversationMessageDataProvider.class);

	private final DataRefreshedObservable dataRefreshedObservable;

	private SerializableSearchCache queryCache = new SerializableSearchCache();
	private final String conversationId;
	private final String parentMessageId;

	private final RMSchemasRecordsServices rm;

	private int batchSize;

	private List<Message> loadedMessages;

	private boolean olderMessagesAvailable;
	private boolean newerMessagesAvailable;

	private KeySetMap<String, String> facetSelections = new KeySetMap<>();
	private Map<String, Boolean> facetStatus = new HashMap<>();
	private String sortCriterion;
	private SortOrder sortOrder = SortOrder.ASCENDING;

	private final ModelLayerFactory modelLayerFactory;
	private final SessionContext sessionContext;
	private final ConversationFacetsHandler conversationFacetsHandler;

	private final String collection;


	public ConversationMessageDataProvider(String collection, String conversationId, String parentMessageId,
										   SessionContext sessionContext,
										   AppLayerFactory appLayerFactory, ModelLayerFactory modelLayerFactory,
										   ConversationFacetsHandler conversationFacetsHandler) {

		this.collection = collection;
		batchSize = 5;

		this.dataRefreshedObservable = new DataRefreshedObservable();

		this.modelLayerFactory = modelLayerFactory;
		this.sessionContext = sessionContext;
		this.conversationFacetsHandler = conversationFacetsHandler;

		loadedMessages = new ArrayList<>();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.conversationId = conversationId;
		this.parentMessageId = parentMessageId;
	}

	public void loadMessagesAroundThisMessage(String targetedMessageId) {
		Message targetedMessage;

		Optional<Message> optionalMessage = loadedMessages.stream().filter(loadedMessage -> loadedMessage.getId().equals(targetedMessageId)).findFirst();
		targetedMessage = optionalMessage.orElseGet(() -> rm.getMessage(targetedMessageId));

		loadMessagesAroundThisMessage(targetedMessage);
	}

	public void loadMessagesAroundThisMessage(Message targetedMessage) {

		loadOlderMessagesThanThisMessage(targetedMessage, false);
		loadNewerMessagesThanThisMessage(targetedMessage, false);

		int targetedMessageIndex = getTargetedMessageIndexAndInsertIfNotExist(targetedMessage);

		int subListIndexStart = targetedMessageIndex - getBatchSize();
		if (subListIndexStart < 0) {
			subListIndexStart = 0;
		}

		int subListIndexEnd = targetedMessageIndex + getBatchSize() + 1;
		if (subListIndexEnd > loadedMessages.size()) {
			subListIndexEnd = loadedMessages.size();
		}

		dataRefreshedObservable.fire(new DataRefreshed(this, loadedMessages.subList(subListIndexStart, subListIndexEnd)) {
			@Override
			public Message getTargetedMessage() {
				return targetedMessage;
			}

			@Override
			public boolean isNewContext() {
				return true;
			}
		});
	}

	public LogicalSearchCondition buildSharedQueryConditionBase(Message targetedMessage) {

		LogicalSearchCondition queryConditionBase = from(rm.message.schemaType())
				.where(rm.message.conversation()).is(conversationId);

		if (parentMessageId != null) {
			queryConditionBase = queryConditionBase.andWhere(rm.message.messageParent()).is(parentMessageId);
		} else {
			queryConditionBase = queryConditionBase.andWhere(rm.message.messageParent()).isNull();
		}

		return queryConditionBase;
	}

	public void loadOlderMessagesThanThisMessage(Message targetedMessage) {
		loadOlderMessagesThanThisMessage(targetedMessage, true);
	}

	private void loadOlderMessagesThanThisMessage(Message targetedMessage, boolean eventIsFired) {
		int olderMessagesIndexStart;
		int olderMessageIndexEnd = 0;

		int targetedMessageIndex = getTargetedMessageIndexAndInsertIfNotExist(targetedMessage);

		if (targetedMessageIndex <= getBatchSize()) {
			LogicalSearchCondition sharedQueryConditionBase = buildSharedQueryConditionBase(targetedMessage);
			List<Message> olderMessageList = doSearch(buildOlderMessagesQuery(sharedQueryConditionBase, targetedMessage));

			olderMessagesAvailable = olderMessageList.size() > getBatchSize();

			for (Message message : olderMessageList) {
				loadedMessages.add(0, message);
				olderMessageIndexEnd++;
			}
		} else {
			olderMessageIndexEnd = targetedMessageIndex;
		}

		olderMessagesIndexStart = olderMessageIndexEnd - getBatchSize();
		if (olderMessagesIndexStart < 0) {
			olderMessagesIndexStart = 0;
		}

		if (targetedMessage == null) {
			newerMessagesAvailable = false;
		}

		if (eventIsFired) {
			dataRefreshedObservable.fire(new DataRefreshed(this, loadedMessages.subList(olderMessagesIndexStart, olderMessageIndexEnd)) {
				@Override
				public boolean isAddedAfterTargetedMessage() {
					return targetedMessage == null;
				}

				@Override
				public Message getTargetedMessage() {
					return targetedMessage;
				}

				@Override
				public boolean isNewContext() {
					return targetedMessage == null;
				}
			});
		}
	}

	public LogicalSearchQuery buildOlderMessagesQuery(LogicalSearchCondition sharedQueryConditionBase,
													  Message targetedMessage) {
		LogicalSearchCondition olderMessageSearchCondition = sharedQueryConditionBase;

		if (targetedMessage != null) {
			olderMessageSearchCondition = olderMessageSearchCondition
					.andWhere(Schemas.IDENTIFIER).isNotEqual(targetedMessage.getId())
					.andWhere(rm.message.createdOn()).isLessOrEqualThan(targetedMessage.getCreatedOn());
		}

		LogicalSearchQuery olderMessageSearchQuery = new LogicalSearchQuery(olderMessageSearchCondition);
		olderMessageSearchQuery.setNumberOfRows(getBatchSize() + 1);
		olderMessageSearchQuery.sortDesc(Schemas.CREATED_ON);

		conversationFacetsHandler.applyFacetsFilterOnQuery(olderMessageSearchQuery);

		return olderMessageSearchQuery;
	}

	public void loadNewerMessagesThanThisMessage(Message targetedMessage) {
		loadNewerMessagesThanThisMessage(targetedMessage, true);
	}

	public void loadNewerMessagesThanThisMessage(Message targetedMessage, boolean eventIsFired) {


		int targetedMessageIndex = getTargetedMessageIndexAndInsertIfNotExist(targetedMessage);

		int newerMessageFirstIndex = targetedMessageIndex + 1;
		int newerMessageLastIndex = newerMessageFirstIndex + getBatchSize();

		if (isNewerMessagesAvailable() && targetedMessageIndex + getBatchSize() >= loadedMessages.size()) {
			Message newestMessage = loadedMessages.isEmpty() ? targetedMessage : loadedMessages.get(loadedMessages.size() - 1);

			LogicalSearchCondition sharedQueryConditionBase = buildSharedQueryConditionBase(newestMessage);

			List<Message> newerMessageList = doSearch(buildNewerMessagesQuery(sharedQueryConditionBase, newestMessage));

			newerMessagesAvailable = newerMessageList.size() > getBatchSize();

			loadedMessages.addAll(newerMessageList);
		}

		if (newerMessageLastIndex > loadedMessages.size()) {
			newerMessageLastIndex = loadedMessages.size();
		}

		if (targetedMessage == null) {
			olderMessagesAvailable = false;
		}

		if (eventIsFired) {
			dataRefreshedObservable.fire(new DataRefreshed(this, loadedMessages.subList(newerMessageFirstIndex, newerMessageLastIndex)) {
				@Override
				public Message getTargetedMessage() {
					return targetedMessage;
				}

				@Override
				public boolean isNewContext() {
					return targetedMessage == null;
				}

				@Override
				public boolean isAddedAfterTargetedMessage() {
					return targetedMessage != null;
				}
			});
		}
	}

	public LogicalSearchQuery buildNewerMessagesQuery(LogicalSearchCondition sharedQueryConditionBase,
													  Message targetedMessage) {
		LogicalSearchCondition newerMessageSearchCondition = sharedQueryConditionBase;

		if (targetedMessage != null) {
			newerMessageSearchCondition = newerMessageSearchCondition
					.andWhere(Schemas.IDENTIFIER).isNotEqual(targetedMessage.getId())
					.andWhere(rm.message.createdOn()).isGreaterOrEqualThan(targetedMessage.getCreatedOn());
		}

		LogicalSearchQuery newerMessageSearchQuery = new LogicalSearchQuery(newerMessageSearchCondition);
		newerMessageSearchQuery.setNumberOfRows(getBatchSize() + 1);
		newerMessageSearchQuery.sortAsc(Schemas.CREATED_ON);

		conversationFacetsHandler.applyFacetsFilterOnQuery(newerMessageSearchQuery);

		return newerMessageSearchQuery;
	}

	private List<Message> doSearch(LogicalSearchQuery query) {

		query.setLanguage(sessionContext.getCurrentLocale());
		SerializedCacheSearchService searchServices = new SerializedCacheSearchService(modelLayerFactory, queryCache, false);

		return rm.wrapMessages(searchServices.search(query, batchSize));
	}

	private int getTargetedMessageIndexAndInsertIfNotExist(Message targetedMessage) {

		int indexOfTargetedMessage;

		if (!loadedMessages.contains(targetedMessage)) {
			loadedMessages.clear();

			if (targetedMessage != null) {
				loadedMessages.add(targetedMessage);
				indexOfTargetedMessage = 0;
			} else {
				indexOfTargetedMessage = -1;
			}

			newerMessagesAvailable = true;
			olderMessagesAvailable = true;
		} else {
			indexOfTargetedMessage = loadedMessages.indexOf(targetedMessage);
		}

		return indexOfTargetedMessage;
	}

	public boolean isNewerMessagesAvailable() {
		return newerMessagesAvailable;
	}

	public boolean isNewerMessagesThanThisMessageAvailable(Message message) {
		if (message == null) {
			return false;
		}

		int indexOfMessage = loadedMessages.indexOf(message);
		boolean existInLoadedMessages = indexOfMessage != -1;
		boolean isNotLastLoadedItem = indexOfMessage < loadedMessages.size() - 1;

		return isNewerMessagesAvailable() || (existInLoadedMessages && isNotLastLoadedItem);
	}

	public boolean isOlderMessagesAvailable() {
		return olderMessagesAvailable;
	}

	public boolean isOlderMessagesThanThisMessageAvailable(Message message) {
		if (message == null) {
			return false;
		}

		int indexOfMessage = loadedMessages.indexOf(message);
		boolean existInLoadedMessages = indexOfMessage != -1;
		boolean isNotFirstLoadedItem = indexOfMessage > 0;

		return isOlderMessagesAvailable() || (existInLoadedMessages && isNotFirstLoadedItem);
	}

	public LogicalSearchQuery buildQueryForFacetsSelection(Message targetedMessage) {
		return new LogicalSearchQuery(buildSharedQueryConditionBase(targetedMessage));
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public void addDataRefreshedListener(DataRefreshedListener listener) {
		dataRefreshedObservable.addListener(listener);
	}

	public void removeDataRefreshedListener(DataRefreshedListener listener) {
		dataRefreshedObservable.removeListener(listener);
	}
}
