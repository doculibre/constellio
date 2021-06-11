package com.constellio.app.ui.pages.events;

import au.com.bytecode.opencsv.CSVWriter;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.ui.framework.components.content.InputStreamWrapper;
import com.constellio.app.ui.framework.components.content.InputStreamWrapper.SimpleAction;
import com.constellio.app.ui.framework.data.DataProvider;
import com.constellio.app.ui.framework.data.event.EventStatistics;
import com.constellio.app.ui.framework.data.event.category.EventsListDataProviderFactory;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import org.apache.commons.lang.CharEncoding;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class BaseEventCategoryPresenter extends BasePresenter<BaseEventCategoryView> {

	private IOServices ioServices;
	public static final String STREAM_NAME = BaseEventCategoryPresenter.class.getName() + "-stream";
	public static final String TEMPORARY_FILE = BaseEventCategoryPresenter.class.getName() + "-file";

	public BaseEventCategoryPresenter(BaseEventCategoryView view) {
		super(view);
		recordServices().flush();
		ioServices = view.getConstellioFactories().getAppLayerFactory().getModelLayerFactory().getIOServicesFactory().newIOServices();
	}

	public void displayEvent(Integer itemId, EventCategory eventCategory) {
		Map<String, Object> eventParameters = new HashMap<>();

		String eventType = getEventListDataProvider(eventCategory).getEventType(itemId);
		eventParameters.put(EventViewParameters.EVENT_TYPE, eventType);
		eventParameters.put(EventViewParameters.EVENT_CATEGORY, eventCategory);
		if (view.getEventId() != null) {
			eventParameters.put(EventViewParameters.BY_ID_EVENT_PARAMETER, view.getEventId());
		}
		if (view.getEventStartDate() != null) {
			eventParameters.put(EventViewParameters.EVENT_START_DATE, LocalDateTime.fromDateFields(view.getEventStartDate()).withTime(0, 0, 0, 0));
		}
		if (view.getEventEndDate() != null) {
			eventParameters.put(EventViewParameters.EVENT_END_DATE, LocalDateTime.fromDateFields(view.getEventEndDate()).withTime(23, 59, 59, 999));
		}
		/*switch (eventCategory){
		case CURRENTLY_BORROWED_FOLDERS:
		case CURRENTLY_BORROWED_DOCUMENTS:
		case CONNECTED_USERS_EVENT : {
			//TODO create appropriate view
			return;
		}
		}*/

		view.navigate().to().showEvent(eventParameters);
	}

	public EventsCategoryDataProvider getEventListDataProvider(EventCategory eventCategory) {
		String collection = view.getCollection();
		User currentUser = getCurrentUser();
		String username = currentUser.getUsername();
		LocalDateTime startDate = (view.getEventStartDate() == null) ?
				null :
				LocalDateTime.fromDateFields(view.getEventStartDate()).withTime(0, 0, 0, 0);
		LocalDateTime endDate = (view.getEventEndDate() == null) ? null : LocalDateTime.fromDateFields(view.getEventEndDate()).withTime(23, 59, 59, 59);
		return EventsListDataProviderFactory
				.getEventsListDataProviderFactory(eventCategory, appLayerFactory, collection, username,
						startDate, endDate, view.getEventId());
	}

	public String getReportTitle(EventCategory eventCategory) {
		return getEventListDataProvider(eventCategory).getDataReportTitle();
	}

	public boolean isWithReportPanel(EventCategory eventCategory) {
		/*switch (eventCategory){
		case DECOMMISSIONING_EVENTS:
		case CONTAINERS_BORROW_OR_RETURN:
		case DOCUMENTS_BORROW_OR_RETURN:
		case FOLDERS_BORROW_OR_RETURN:
		case CONNECTED_USERS_EVENT:
		case EVENTS_BY_ADMINISTRATIVE_UNIT: return false;
		default: return true;
		}*/
		return false;
	}

	public boolean isByRangeDate(EventCategory eventCategory) {
		switch (eventCategory) {
		case CURRENTLY_BORROWED_FOLDERS:
		case CURRENTLY_BORROWED_DOCUMENTS:
		case CONNECTED_USERS_EVENT:
			return false;
		default:
			return true;
		}
	}

	public boolean hasFetchById(EventCategory eventCategory) {
		switch (eventCategory) {
		case EVENTS_BY_ADMINISTRATIVE_UNIT:
		case EVENTS_BY_FOLDER:
		case EVENTS_BY_USER:
		case EVENTS_BY_CONTAINER:
			return true;
		default:
			return false;
		}
	}

	public String getTitle(EventCategory eventCategory) {
		EventsCategoryDataProvider eventProvider = getEventListDataProvider(eventCategory);
		return eventProvider.getDataTitle();
	}

	public void eventAudit() {
		view.navigate().to(RMViews.class).eventAudit();
	}

	public InputStreamWrapper createInputStreamWrapper() {
		InputStreamWrapper inputStreamWrapper = new InputStreamWrapper();
		inputStreamWrapper.addSimpleAction(new SimpleAction() {
			@Override
			public void action(InputStreamWrapper inputStreamWrapper) {
				try {
					final File tempFile = generateCsvReport();
					InputStream inputStream = new FileInputStream(tempFile) {
						@Override
						public void close()
								throws IOException {
							super.close();
							ioServices.deleteQuietly(tempFile);
						}
					};
					inputStreamWrapper.setInputStream(inputStream);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		});

		return inputStreamWrapper;
	}

	public File generateCsvReport() {

		OutputStreamWriter outputStreamWriter = null;
		CSVWriter csvWriter = null;
		OutputStream byteArrayOutputStream = null;
		File temporaryFile = null;
		try {
			temporaryFile = ioServices.newTemporaryFile(TEMPORARY_FILE);
			byteArrayOutputStream = ioServices.newFileOutputStream(temporaryFile, STREAM_NAME);
			outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream, CharEncoding.ISO_8859_1);
			csvWriter = new CSVWriter(outputStreamWriter, ',', CSVWriter.NO_QUOTE_CHARACTER);
			writeCsvReport(csvWriter);
			csvWriter.flush();
			ioServices.closeQuietly(csvWriter);
			ioServices.closeQuietly(outputStreamWriter);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ioServices.closeQuietly(csvWriter);
			ioServices.closeQuietly(outputStreamWriter);
			ioServices.closeQuietly(byteArrayOutputStream);
		}

		return temporaryFile;
	}

	public void writeCsvReport(CSVWriter csvWriter) {
		DataProvider dataProvider = getEventListDataProvider(view.getEventViewParameters().getEventCategory());

		String[] headerRecord = {$("title"), $("value")};
		csvWriter.writeNext(headerRecord);
		for(EventStatistics eventStatistics : ((EventsCategoryDataProvider) dataProvider).getEvents()) {
			csvWriter.writeNext(eventStatistics.getLabel(), eventStatistics.getValue().toString());
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.VIEW_EVENTS).onSomething();
	}
}
