package com.constellio.app.utils.scripts;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.pages.search.SimpleSearchPresenter;
import com.constellio.app.ui.pages.search.SimpleSearchView;
import com.constellio.app.utils.ScriptsUtils;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.utils.ThreadList;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordCachesServices;

//import static org.mockito.Mockito.when;
//import org.mockito.Mockito;

public class SearchPresenterPerformanceTester {

	private static User user;

	public static void main(String[] args)
			throws Exception {

		String collection = args[0];
		String username = args[1];
		int nbOfThreads = Integer.valueOf(args[2]);
		int nbOfExecute = Integer.valueOf(args[3]);
		String search = "agence rap";
		AppLayerFactory appLayerFactory = ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads();
		runTest(appLayerFactory, collection, username, nbOfThreads, nbOfExecute, search);

	}

	private static SimpleSearchView createDummyView() {
		return null;
		/*
		String collection = user.getCollection();
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();

		SessionContext sessionContext = Mockito.mock(SessionContext.class);
		when(sessionContext.getCurrentCollection()).thenReturn(collection);
		UserVO currentUser = new UserToVOBuilder().build(user.getWrappedRecord(), VIEW_MODE.DISPLAY, sessionContext);
		when(sessionContext.getCurrentUser()).thenReturn(currentUser);

		SimpleSearchView searchView = Mockito.mock(SimpleSearchView.class);
		when(searchView.getCollection()).thenReturn(collection);
		when(searchView.getSessionContext()).thenReturn(sessionContext);
		when(searchView.getConstellioFactories()).thenReturn(constellioFactories);
		return searchView;
		*/
	}

	public static void runTest(AppLayerFactory appLayerFactory, final String collection, String username, int nbOfThreads,
			int nbOfExecute, final String search)
			throws Exception {
		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		final ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		DataLayerFactory dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		user = modelLayerFactory.newUserServices()
				.getUserInCollection(username, collection);
		new RecordCachesServices(modelLayerFactory).loadCachesIn(collection);
		dataLayerFactory.getDataLayerLogger()
				.setPrintAllQueriesLongerThanMS(0);
		final SimpleSearchView view = createDummyView();
		ThreadList<Thread> threads = new ThreadList<>();
		final AtomicBoolean atomicBoolean = new AtomicBoolean(true);
		for (int i = 0; i < (nbOfThreads - 1); i++) {
			threads.addAndStart(new Thread() {
				@Override
				public void run() {
					while (atomicBoolean.get()) {
						doSearch(view, rm, search);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}

		long start = new Date().getTime();

		for (int j = 0; j < nbOfExecute; j++) {
			System.out.println(">" + j);
			doSearch(view, rm, search);
		}
		long duration = (new Date().getTime() - start);
		atomicBoolean.set(false);
		threads.joinAll();

		long durationByRequests = duration / nbOfExecute;
		System.out.println("---------------------------------");
		System.out.println("Number of threads : " + threads);
		System.out.println("Number of requests : " + nbOfExecute);
		System.out.println("Requests are taking " + durationByRequests + "ms");
	}

	private static void doSearch(SimpleSearchView view, RMSchemasRecordsServices rm, String search) {
		SimpleSearchPresenter presenter = new SimpleSearchPresenter(view);

		presenter.forRequestParameters("r/" + search);
		presenter.getFacets();
		SearchResultVODataProvider dataProvider = presenter.getSearchResults();
		for (int j = 0; j < 10; j++) {
			SearchResultVO searchResultVO = dataProvider.listSearchResultVOs(j, 1)
					.get(0);
			// System.out.println(searchResultVO.getRecordVO().getTitle());
		}

	}
}