package com.constellio.app.api.benchmarks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

		//		try {
		//			handleGet(request, response);
		//		} catch (UnresolvableOptimisticLockingConflict | OptimisticLocking e) {
		//			//Normal scenario, this does not fail the benchmark
		//
		//		} catch (BenchmarkWebServiceConfigurationRuntimeException e) {
		//			LOGGER.error(e.getMessage());
		//			response.setStatusForAllCollections(HttpServletResponse.SC_BAD_REQUEST);
		//
		//		} catch (RuntimeException e) {
		//			e.printStackTrace();
		//			throw e;
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//			throw new RuntimeException(e);
		//		}

	}

}
