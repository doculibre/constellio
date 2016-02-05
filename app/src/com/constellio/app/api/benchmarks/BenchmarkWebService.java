package com.constellio.app.api.benchmarks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.HttpServletRequestAuthenticator;
import com.constellio.app.api.benchmarks.BenchmarkWebServiceConfigurationRuntimeException.BenchmarkWebServiceRuntimeException_BadParameter;
import com.constellio.app.api.benchmarks.BenchmarkWebServiceConfigurationRuntimeException.BenchmarkWebServiceRuntimeException_MissingParameter;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.pages.search.SimpleSearchPresenter;
import com.constellio.app.ui.pages.search.SimpleSearchView;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException.OptimisticLocking;
import com.constellio.model.services.records.RecordServicesException.UnresolvableOptimisticLockingConflict;

public class BenchmarkWebService extends HttpServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkWebService.class);

	public static final String SEARCH = "search";

	@Override
	public void init(ServletConfig config)
			throws ServletException {
		super.init(config);

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			handleGet(request, response);
		} catch (UnresolvableOptimisticLockingConflict | OptimisticLocking e) {
			//Normal scenario, this does not fail the benchmark

		} catch (BenchmarkWebServiceConfigurationRuntimeException e) {
			LOGGER.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	private void handleGet(HttpServletRequest request, HttpServletResponse response)
			throws UnresolvableOptimisticLockingConflict, OptimisticLocking {

		User user = new HttpServletRequestAuthenticator(modelLayerFactory()).authenticateSystemAdminInCollection(request);
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		} else {
			String action = request.getParameter("action");

			switch (action) {
			case SEARCH:
				handleSearch(request, user);
				break;

			}
		}
	}

	private void handleSearch(HttpServletRequest request, User user) {

		SimpleSearchView view = BenchmarkUtils.createDummySimpleSearchViewFor(user);
		SimpleSearchPresenter presenter = new SimpleSearchPresenter(view);

		String freeTextSearch = requiredParameter(request, "freeTextSearch");
		int qtyOfResults = intParameter(request, "qtyOfResults", 10);
		Map<String, String[]> extraSolrParams = allParametersExcept(request, "freeTextSearch", "qtyOfResults");

		presenter.forRequestParameters("r/" + freeTextSearch);
		presenter.setExtraSolrParams(extraSolrParams);
		presenter.getFacets();
		SearchResultVODataProvider dataProvider = presenter.getSearchResults();
		for (int j = 0; j < qtyOfResults && j < dataProvider.size(); j++) {
			dataProvider.listSearchResultVOs(j, 1).get(0);
		}

	}

	private Map<String, String[]> allParametersExcept(HttpServletRequest request, String... except) {
		Map<String, String[]> map = new HashMap<>(request.getParameterMap());
		for (String e : except) {
			map.remove(e);
		}
		map.remove(HttpServletRequestAuthenticator.USER_TOKEN);
		map.remove(HttpServletRequestAuthenticator.USER_SERVICE_KEY);
		map.remove(HttpServletRequestAuthenticator.COLLECTION);
		return map;
	}

	private String requiredParameter(HttpServletRequest request, String parameterName) {
		String value = request.getParameter(parameterName);
		if (value == null) {
			throw new BenchmarkWebServiceRuntimeException_MissingParameter(parameterName);
		}
		return value;
	}

	private String parameter(HttpServletRequest request, String parameterName) {
		return request.getParameter(parameterName);
	}

	private int intParameter(HttpServletRequest request, String parameterName, int defaultValue) {
		String value = request.getParameter(parameterName);
		if (value == null) {
			return defaultValue;
		} else {
			try {
				return Integer.valueOf(value);
			} catch (NumberFormatException e) {
				throw new BenchmarkWebServiceRuntimeException_BadParameter(parameterName, value);
			}
		}
	}

	private AppLayerFactory appLayerFactory() {
		return ConstellioFactories.getInstance().getAppLayerFactory();
	}

	private ModelLayerFactory modelLayerFactory() {
		return ConstellioFactories.getInstance().getModelLayerFactory();
	}

}
