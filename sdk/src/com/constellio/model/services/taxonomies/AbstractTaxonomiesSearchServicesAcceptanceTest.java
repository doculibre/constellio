package com.constellio.model.services.taxonomies;

import com.constellio.data.extensions.AfterQueryParams;
import com.constellio.data.extensions.BigVaultServerExtension;
import com.constellio.data.utils.Pair;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.assertj.core.api.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.model.services.taxonomies.TaxonomiesTestsUtils.ajustIfBetterThanExpected;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.assertj.core.api.Assertions.assertThat;

public class AbstractTaxonomiesSearchServicesAcceptanceTest extends ConstellioTest {

	private static final boolean VALIDATE_SOLR_QUERIES_COUNT = true;

	private AtomicInteger queriesCount = new AtomicInteger();
	private AtomicInteger facetsCount = new AtomicInteger();
	private AtomicInteger returnedDocumentsCount = new AtomicInteger();
	private List<String> queryInfos = new ArrayList<>();

	protected void configureQueryCounter() {

		String className = getClass().getSimpleName();

		getDataLayerFactory().getExtensions().getSystemWideExtensions().bigVaultServerExtension
				.add(new BigVaultServerExtension() {
					@Override
					public void afterQuery(AfterQueryParams params) {

						String stacktrace = substringAfter(substringAfter(ExceptionUtils.getStackTrace(new Exception()), "\n"), "\n");

						if (stacktrace.contains(className)) {
							queriesCount.incrementAndGet();
							returnedDocumentsCount.addAndGet(params.getReturnedResultsCount());
							String[] facetQuery = params.getSolrParams().getParams("facet.query");
							if (facetQuery != null) {
								facetsCount.addAndGet(facetQuery.length);
							}

							String logMessage = "1-" + params.getReturnedResultsCount() + "-" + (facetQuery == null ? 0 : facetQuery.length) + " : " + (params.getQueryName() == null ? "Unnamed query" : params.getQueryName()) + "\n" + stacktrace;

							queryInfos.add(logMessage);
						}
					}
				});
	}


	protected abstract class LinkableTaxonomySearchResponseCaller {

		private LinkableTaxonomySearchResponse firstCallAnswer;

		private LinkableTaxonomySearchResponse secondCallAnswer;

		private String firstCallSolrQueries;

		private String secondCallSolrQueries;

		private List<String> firstCallQueries;

		private List<String> secondCallQueries;

		public LinkableTaxonomySearchResponse firstAnswer() {
			if (firstCallAnswer == null) {
				queriesCount.set(0);
				returnedDocumentsCount.set(0);
				facetsCount.set(0);
				queryInfos.clear();
				firstCallAnswer = call();
				firstCallQueries = new ArrayList<>(queryInfos);
				firstCallSolrQueries = queriesCount.get() + "-" + returnedDocumentsCount.get() + "-" + facetsCount.get();
				queriesCount.set(0);
				returnedDocumentsCount.set(0);
				facetsCount.set(0);
				queryInfos.clear();
			}
			return firstCallAnswer;
		}

		public LinkableTaxonomySearchResponse secondAnswer() {
			firstAnswer();
			if (secondCallAnswer == null) {
				queriesCount.set(0);
				returnedDocumentsCount.set(0);
				facetsCount.set(0);
				queryInfos.clear();
				secondCallAnswer = call();
				secondCallQueries = new ArrayList<>(queryInfos);
				secondCallSolrQueries = queriesCount.get() + "-" + returnedDocumentsCount.get() + "-" + facetsCount.get();
				queriesCount.set(0);
				returnedDocumentsCount.set(0);
				facetsCount.set(0);
				queryInfos.clear();
			}
			return secondCallAnswer;
		}

		protected abstract LinkableTaxonomySearchResponse call();

		public Pair<String, List<String>> firstAnswerSolrQueries() {
			firstAnswer();
			return new Pair(firstCallSolrQueries, firstCallQueries);
		}

		public Pair<String, List<String>> secondAnswerSolrQueries() {
			secondAnswer();
			return new Pair(secondCallSolrQueries, secondCallQueries);
		}

	}

	protected Condition<? super LinkableTaxonomySearchResponseCaller> solrQueryCounts(final int queries,
																					  final int queryResults,
																					  final int facets) {

		final Exception exception = new Exception();
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller value) {
				String expected = queries + "-" + queryResults + "-" + facets;
				Pair<String, List<String>> current = value.firstAnswerSolrQueries();

				if (VALIDATE_SOLR_QUERIES_COUNT && !ajustIfBetterThanExpected(exception.getStackTrace(), current.getKey(), expected)) {

					StringBuilder sb = new StringBuilder("First call Queries count - Query resuts count - Facets count");
					for (int i = 0; i < current.getValue().size(); i++) {
						String query = current.getValue().get(i);
						sb.append("\n\n\n\t- Query #" + (1 + i) + " : " + query);
					}

					assertThat(current.getKey()).describedAs(sb.toString()).isEqualTo(expected);
				}
				queriesCount.set(0);
				facetsCount.set(0);
				returnedDocumentsCount.set(0);
				queryInfos.clear();

				return true;
			}
		};
	}

	protected Condition<? super LinkableTaxonomySearchResponseCaller> secondCallQueryCounts(final int queries,
																							final int queryResults,
																							final int facets) {
		return secondSolrQueryCounts(queries, queryResults, facets);
	}

	protected Condition<? super LinkableTaxonomySearchResponseCaller> secondSolrQueryCounts(final int queries,
																							final int queryResults,
																							final int facets) {

		final Exception exception = new Exception();
		return new Condition<LinkableTaxonomySearchResponseCaller>() {
			@Override
			public boolean matches(LinkableTaxonomySearchResponseCaller value) {
				String expected = queries + "-" + queryResults + "-" + facets;
				Pair<String, List<String>> current = value.secondAnswerSolrQueries();

				if (VALIDATE_SOLR_QUERIES_COUNT && !ajustIfBetterThanExpected(exception.getStackTrace(), current.getKey(), expected)) {

					StringBuilder sb = new StringBuilder("Second call Queries count - Query resuts count - Facets count");
					for (int i = 0; i < current.getValue().size(); i++) {
						String query = current.getValue().get(i);
						sb.append("\n\n\n\t- Query #" + (1 + i) + " : " + query);
					}

					assertThat(current.getKey()).describedAs(sb.toString()).isEqualTo(expected);
				}
				queriesCount.set(0);
				facetsCount.set(0);
				returnedDocumentsCount.set(0);
				queryInfos.clear();


				return true;
			}
		};
	}


	@Override
	protected void givenConfig(SystemConfiguration config, Object value) {
		super.givenConfig(config, value);
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		queriesCount.set(0);
		facetsCount.set(0);
		returnedDocumentsCount.set(0);
		queryInfos.clear();

	}

	protected void resetCounters(AtomicInteger queriesCount, AtomicInteger facetsCount,
								 AtomicInteger returnedDocumentsCount) {
		queriesCount.set(0);
		facetsCount.set(0);
		returnedDocumentsCount.set(0);
		queryInfos.clear();
	}

	protected void resetCounters() {
		queriesCount.set(0);
		facetsCount.set(0);
		returnedDocumentsCount.set(0);
		queryInfos.clear();
	}
}
