package com.constellio.app.ui.framework.containers;

import static com.constellio.app.ui.i18n.i18n.$;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.BatchProcessVO;
import com.constellio.app.ui.framework.data.BatchProcessDataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;

public class BatchProcessContainer extends DataContainer<BatchProcessDataProvider> {
	
	public static final String TITLE = "title";

	private static final String STATUS = "status";

	private static final String REQUEST_DATE_TIME = "requestDateTime";

	private static final String START_DATE_TIME = "startDateTime";

	private static final String HANDLED_RECORDS_COUNT = "handledRecordsCount";

	private static final String TOTAL_RECORDS_COUNT = "totalRecordsCount";

	private static final String USERNAME = "username";

	private static final String COLLECTION = "collection";
	
	private boolean systemBatchProcesses;
	
	public BatchProcessContainer(BatchProcessDataProvider dataProvider) {
		this(dataProvider, true);
	}

	public BatchProcessContainer(BatchProcessDataProvider dataProvider, boolean systemBatchProcesses) {
		super(dataProvider);
		this.systemBatchProcesses = systemBatchProcesses;
	}

	@Override
	protected void populateFromData(BatchProcessDataProvider dataProvider) {
		for (BatchProcessVO batchProcessVO : dataProvider.getBatchProcessVOs()) {
			addItem(batchProcessVO);
		}
	}

	@Override
	protected Collection<?> getOwnContainerPropertyIds() {
		List<String> containerPropertyIds = new ArrayList<>();
		containerPropertyIds.add(TITLE);
		containerPropertyIds.add(STATUS);
		containerPropertyIds.add(REQUEST_DATE_TIME);
		containerPropertyIds.add(START_DATE_TIME);
		containerPropertyIds.add(HANDLED_RECORDS_COUNT);
		containerPropertyIds.add(TOTAL_RECORDS_COUNT);
		
		ConstellioFactories constellioFactories = ConstellioUI.getCurrent().getConstellioFactories();
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		CollectionsListManager collectionsListManager = modelLayerFactory.getCollectionsListManager();
		List<String> collections = collectionsListManager.getCollectionsExcludingSystem();
		String username = sessionContext.getCurrentUser().getUsername();
		UserCredential userCredentials = userServices.getUser(username);
		
		if (systemBatchProcesses) {
			containerPropertyIds.add(USERNAME);
			if (collections.size() > 1) {
				containerPropertyIds.add(COLLECTION);
			}
		} else if (userCredentials.getCollections().size() > 1) {
			containerPropertyIds.add(COLLECTION);
		}
		return containerPropertyIds;
	}

	@Override
	protected Class<?> getOwnType(Object propertyId) {
		Class<?> type;
		if (TITLE.equals(propertyId)) {
			type = String.class;
		} else if (STATUS.equals(propertyId)) {	
			type = String.class;
		} else if (REQUEST_DATE_TIME.equals(propertyId)) {	
			type = LocalDateTime.class;
		} else if (START_DATE_TIME.equals(propertyId)) {	
			type = LocalDateTime.class;
		} else if (HANDLED_RECORDS_COUNT.equals(propertyId)) {	
			type = Integer.class;
		} else if (TOTAL_RECORDS_COUNT.equals(propertyId)) {	
			type = Integer.class;
		} else if (USERNAME.equals(propertyId)) {	
			type = String.class;
		} else if (COLLECTION.equals(propertyId)) {	
			type = String.class;
		} else {
			throw new IllegalArgumentException("Invalid propertyId : " + propertyId);
		}
		return type;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected Property<?> getOwnContainerProperty(Object itemId, Object propertyId) {
		BatchProcessVO batchProcessVO = (BatchProcessVO) itemId;
		Object value;
		if (TITLE.equals(propertyId)) {
			value = batchProcessVO.getTitle();
			if (StringUtils.isBlank((String) value)) {
				value = batchProcessVO.getId();
			}
		} else if (STATUS.equals(propertyId)) {	
			BatchProcessStatus status = batchProcessVO.getStatus();
			value = $("BatchProcess.status." + status.toString());
		} else if (REQUEST_DATE_TIME.equals(propertyId)) {	
			value = batchProcessVO.getRequestDateTime();
		} else if (START_DATE_TIME.equals(propertyId)) {	
			value = batchProcessVO.getStartDateTime();
		} else if (HANDLED_RECORDS_COUNT.equals(propertyId)) {	
			value = batchProcessVO.getHandledRecordsCount();
		} else if (TOTAL_RECORDS_COUNT.equals(propertyId)) {	
			value = batchProcessVO.getTotalRecordsCount();
		} else if (USERNAME.equals(propertyId)) {	
			value = batchProcessVO.getUsername();
		} else if (COLLECTION.equals(propertyId)) {	
			value = batchProcessVO.getCollection();
		} else {
			throw new IllegalArgumentException("Invalid propertyId : " + propertyId);
		}
		Class<?> type = getType(propertyId);
		return new ObjectProperty(value, type);
	}

}
