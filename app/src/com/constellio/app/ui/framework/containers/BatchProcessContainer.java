package com.constellio.app.ui.framework.containers;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.BatchProcessVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.converters.CollectionCodeToLabelConverter;
import com.constellio.app.ui.framework.data.BatchProcessDataProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.batchprocess.ListBatchProcessesPresenter;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class BatchProcessContainer extends DataContainer<BatchProcessDataProvider> {

	public static final Resource ICON_RESOURCE = new ThemeResource("images/icons/actions/printer.png");

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static final String RANK = "rank";

	public static final String TITLE = "title";

	private static final String STATUS = "status";

	private static final String REQUEST_DATE_TIME = "requestDateTime";

	private static final String START_DATE_TIME = "startDateTime";

	private static final String HANDLED_RECORDS_COUNT = "handledRecordsCount";

	private static final String TOTAL_RECORDS_COUNT = "totalRecordsCount";

	private static final String PROGRESS = "progress";

	private static final String USERNAME = "username";

	private static final String COLLECTION = "collection";

	private static final String REPORT = "report";

	private CollectionCodeToLabelConverter collectionCodeToLabelConverter = new CollectionCodeToLabelConverter();

	private boolean systemBatchProcesses;

	private ListBatchProcessesPresenter presenter;

	public BatchProcessContainer(BatchProcessDataProvider dataProvider, ListBatchProcessesPresenter presenter) {
		this(dataProvider, true, presenter);
	}

	public BatchProcessContainer(BatchProcessDataProvider dataProvider, boolean systemBatchProcesses,
								 ListBatchProcessesPresenter presenter) {
		super(dataProvider);
		this.systemBatchProcesses = systemBatchProcesses;
		this.presenter = presenter;
		populateFromData(dataProvider);
	}

	@Override
	protected void populateFromData(BatchProcessDataProvider dataProvider) {
		int currentRank = 1;
		for (BatchProcessVO batchProcessVO : dataProvider.getBatchProcessVOs()) {
			if (batchProcessVO.getStatus() != BatchProcessStatus.FINISHED) {
				batchProcessVO.setRank(currentRank++);
			}
			addItem(batchProcessVO);
		}
	}

	@Override
	protected Collection<?> getOwnContainerPropertyIds() {
		List<String> containerPropertyIds = new ArrayList<>();
		containerPropertyIds.add(RANK);
		containerPropertyIds.add(TITLE);
		containerPropertyIds.add(STATUS);
		containerPropertyIds.add(REQUEST_DATE_TIME);
		containerPropertyIds.add(START_DATE_TIME);
		containerPropertyIds.add(HANDLED_RECORDS_COUNT);
		containerPropertyIds.add(TOTAL_RECORDS_COUNT);
		containerPropertyIds.add(PROGRESS);
		containerPropertyIds.add(REPORT);

		ConstellioFactories constellioFactories = ConstellioUI.getCurrent().getConstellioFactories();
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		CollectionsListManager collectionsListManager = modelLayerFactory.getCollectionsListManager();
		List<String> collections = collectionsListManager.getCollectionsExcludingSystem();
		String username = sessionContext.getCurrentUser().getUsername();
		SystemWideUserInfos userCredentials = userServices.getUserInfos(username);

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
		if (RANK.equals(propertyId)) {
			type = Integer.class;
		} else if (TITLE.equals(propertyId)) {
			type = String.class;
		} else if (STATUS.equals(propertyId)) {
			type = String.class;
		} else if (REQUEST_DATE_TIME.equals(propertyId)) {
			type = String.class;
		} else if (START_DATE_TIME.equals(propertyId)) {
			type = String.class;
		} else if (HANDLED_RECORDS_COUNT.equals(propertyId)) {
			type = Integer.class;
		} else if (TOTAL_RECORDS_COUNT.equals(propertyId)) {
			type = Integer.class;
		} else if (PROGRESS.equals(propertyId)) {
			type = String.class;
		} else if (USERNAME.equals(propertyId)) {
			type = String.class;
		} else if (COLLECTION.equals(propertyId)) {
			type = String.class;
		} else if (REPORT.equals(propertyId)) {
			type = WindowButton.class;
		} else {
			throw new IllegalArgumentException("Invalid propertyId : " + propertyId);
		}
		return type;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	protected Property<?> getOwnContainerProperty(Object itemId, Object propertyId) {
		final BatchProcessVO batchProcessVO = (BatchProcessVO) itemId;
		Object value;
		if (RANK.equals(propertyId)) {
			value = batchProcessVO.getRank();
		} else if (TITLE.equals(propertyId)) {
			value = getTitle(batchProcessVO);
		} else if (STATUS.equals(propertyId)) {
			BatchProcessStatus status = batchProcessVO.getStatus();
			value = $("BatchProcess.status." + status.toString());
		} else if (REQUEST_DATE_TIME.equals(propertyId)) {
			value = format(batchProcessVO.getRequestDateTime());
		} else if (START_DATE_TIME.equals(propertyId)) {
			value = format(batchProcessVO.getStartDateTime());
		} else if (HANDLED_RECORDS_COUNT.equals(propertyId)) {
			value = batchProcessVO.getHandledRecordsCount();
		} else if (TOTAL_RECORDS_COUNT.equals(propertyId)) {
			value = batchProcessVO.getTotalRecordsCount();
		} else if (PROGRESS.equals(propertyId)) {
			value = getProgress(batchProcessVO);
		} else if (USERNAME.equals(propertyId)) {
			value = batchProcessVO.getUsername();
		} else if (COLLECTION.equals(propertyId)) {
			value = collectionCodeToLabelConverter
					.convertToPresentation(batchProcessVO.getCollection(), String.class, i18n.getLocale());
		} else if (REPORT.equals(propertyId)) {
			value = new WindowButton(ICON_RESOURCE, "", true, WindowButton.WindowConfiguration.modalDialog("65%", "65%")) {
				@Override
				protected Component buildWindowContent() {
					return new RecordDisplay(presenter.getBatchProcessReportVO(batchProcessVO.getId()));
				}

				@Override
				public void buttonClick(ClickEvent event) {
					if (presenter.getBatchProcessReport(batchProcessVO.getId()) != null) {
						super.buttonClick(event);
					} else {
						presenter.showErrorMessage($("BachProcessContainer.noAvailableReport"));
					}
				}
			};
		} else {
			throw new IllegalArgumentException("Invalid propertyId : " + propertyId);
		}
		Class<?> type = getType(propertyId);
		return new ObjectProperty(value, type);
	}

	private String getTitle(BatchProcessVO batchProcessVO) {
		String title;
		String titleCode = batchProcessVO.getTitle();
		if (StringUtils.isBlank(titleCode)) {
			title = $("BatchProcess.title.systemBatchProcess");
		} else if (titleCode.indexOf(" ") != -1) {
			String titleKey = StringUtils.substringBefore(titleCode, " ");
			String titleValue = StringUtils.substringAfter(titleCode, " ");
			title = $("BatchProcess.title." + titleKey + ".with", titleValue);
		} else {
			title = $("BatchProcess.title." + titleCode);
		}

		if (title.startsWith("BatchProcess.title")) {
			title = titleCode;
		}

		return title;
	}

	private String getProgress(BatchProcessVO batchProcessVO) {
		String progress;
		int handledRecordsCount = batchProcessVO.getHandledRecordsCount();
		int totalRecordsCount = batchProcessVO.getTotalRecordsCount();
		if (totalRecordsCount > 0) {
			if (handledRecordsCount > 0) {
				progress = (int) ((handledRecordsCount * 100.f) / totalRecordsCount) + "%";
			} else {
				progress = "0%";
			}
		} else {
			progress = "";
		}
		return progress;
	}

	private String format(LocalDateTime localDateTime) {
		String result;
		if (localDateTime != null) {
			result = sdf.format(localDateTime.toDate());
		} else {
			result = "";
		}
		return result;
	}

}
