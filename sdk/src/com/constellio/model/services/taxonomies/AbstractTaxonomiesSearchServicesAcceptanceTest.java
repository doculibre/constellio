package com.constellio.model.services.taxonomies;

import com.constellio.data.extensions.AfterQueryParams;
import com.constellio.data.extensions.BigVaultServerExtension;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.assertj.core.api.Condition;

import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.model.services.taxonomies.TaxonomiesTestsUtils.ajustIfBetterThanExpected;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.assertj.core.api.Assertions.assertThat;

public class AbstractTaxonomiesSearchServicesAcceptanceTest extends ConstellioTest {

	private static final boolean VALIDATE_SOLR_QUERIES_COUNT = true;

	AtomicInteger queriesCount = new AtomicInteger();
	AtomicInteger facetsCount = new AtomicInteger();
	AtomicInteger returnedDocumentsCount = new AtomicInteger();

	protected void configureQueryCounter() {

		String className = getClass().getSimpleName();

		getDataLayerFactory().getExtensions().getSystemWideExtensions().bigVaultServerExtension
				.add(new BigVaultServerExtension() {
					@Override
					public void afterQuery(AfterQueryParams params) {

						String stacktrace = substringAfter(substringAfter(ExceptionUtils.getStackTrace(new Exception()), "\n"), "\n");

						if (stacktrace.contains(className)) {

							queriesCount.incrementAndGet();
							String[] facetQuery = params.getSolrParams().getParams("facet.query");
							if (facetQuery != null) {
								facetsCount.addAndGet(facetQuery.length);
							}

							returnedDocumentsCount.addAndGet(params.getReturnedResultsCount());

							System.out.println((params.getQueryName() == null ? "Unnamed query" : params.getQueryName()) + " 1-" + (facetQuery == null ? 0 : facetQuery.length) + "-" + params.getReturnedResultsCount()
											   + "\n" + stacktrace);
						}
					}
				});
	}


	protected abstract class LinkableTaxonomySearchResponseCaller {

		private LinkableTaxonomySearchResponse firstCallAnswer;

		private LinkableTaxonomySearchResponse secondCallAnswer;

		private String firstCallSolrQueries;

		private String secondCallSolrQueries;

		public LinkableTaxonomySearchResponse firstAnswer() {
			if (firstCallAnswer == null) {
				queriesCount.set(0);
				returnedDocumentsCount.set(0);
				facetsCount.set(0);
				firstCallAnswer = call();
				firstCallSolrQueries = queriesCount.get() + "-" + returnedDocumentsCount.get() + "-" + facetsCount.get();
				queriesCount.set(0);
				returnedDocumentsCount.set(0);
				facetsCount.set(0);
			}
			return firstCallAnswer;
		}

		public LinkableTaxonomySearchResponse secondAnswer() {
			firstAnswer();
			if (secondCallAnswer == null) {
				queriesCount.set(0);
				returnedDocumentsCount.set(0);
				facetsCount.set(0);
				secondCallAnswer = call();
				secondCallSolrQueries = queriesCount.get() + "-" + returnedDocumentsCount.get() + "-" + facetsCount.get();
				queriesCount.set(0);
				returnedDocumentsCount.set(0);
				facetsCount.set(0);
			}
			return secondCallAnswer;
		}

		protected abstract LinkableTaxonomySearchResponse call();

		public String firstAnswerSolrQueries() {
			firstAnswer();
			return firstCallSolrQueries;
		}

		public String secondAnswerSolrQueries() {
			secondAnswer();
			return secondCallSolrQueries;
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
				String current = value.firstAnswerSolrQueries();

				if (VALIDATE_SOLR_QUERIES_COUNT && !ajustIfBetterThanExpected(exception.getStackTrace(), current, expected)) {
					assertThat(current).describedAs("First call Queries count - Query resuts count - Facets count")
							.isEqualTo(expected);
				}
				queriesCount.set(0);
				facetsCount.set(0);
				returnedDocumentsCount.set(0);

				System.out.println("--------------------------------------------------------------------------------");

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
				String current = value.secondAnswerSolrQueries();

				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				if (VALIDATE_SOLR_QUERIES_COUNT && !ajustIfBetterThanExpected(exception.getStackTrace(), current, expected)) {
					assertThat(current).describedAs("First call Queries count - Query resuts count - Facets count")
							.isEqualTo(expected);
				}
				queriesCount.set(0);
				facetsCount.set(0);
				returnedDocumentsCount.set(0);
				System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");


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

	}

	protected void resetCounters(AtomicInteger queriesCount, AtomicInteger facetsCount,
								 AtomicInteger returnedDocumentsCount) {
		queriesCount.set(0);
		facetsCount.set(0);
		returnedDocumentsCount.set(0);
	}

	protected void resetCounters() {
		queriesCount.set(0);
		facetsCount.set(0);
		returnedDocumentsCount.set(0);
	}
}
