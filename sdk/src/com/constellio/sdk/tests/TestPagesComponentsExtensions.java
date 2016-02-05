package com.constellio.sdk.tests;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.PagesComponentsExtensionParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.extensions.BigVaultServerExtension;
import com.constellio.data.utils.LoggerUtils;
import com.constellio.sdk.tests.SystemLoadSimulator.SystemLoadLevel;

public class TestPagesComponentsExtensions extends PagesComponentsExtension {

	private static Logger LOGGER = LoggerFactory.getLogger(TestPagesComponentsExtensions.class);

	AppLayerFactory appLayerFactory;

	BottomPanelBigVaultServerExtension bottomPanelBigVaultServerExtension;

	public TestPagesComponentsExtensions(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.bottomPanelBigVaultServerExtension = new BottomPanelBigVaultServerExtension();
		this.appLayerFactory.getModelLayerFactory().getDataLayerFactory().getExtensions()
				.getSystemWideExtensions().getBigVaultServerExtension().add(this.bottomPanelBigVaultServerExtension);
	}

	@Override
	public void decorateView(PagesComponentsExtensionParams params) {
		params.getFooter().addComponent(new SDKPanel());
	}

	public static class BottomPanelBigVaultServerExtension extends BigVaultServerExtension {

		AtomicInteger idQuery = new AtomicInteger();
		AtomicInteger idUpdate = new AtomicInteger();
		AtomicInteger queriesCount = new AtomicInteger();
		List<LoggedItem> queries = new ArrayList<>();
		List<LoggedItem> updates = new ArrayList<>();
		AtomicLong queryTimeCount = new AtomicLong();
		AtomicLong updateTimeCount = new AtomicLong();
		SystemLoadLevel loadLevel = SystemLoadLevel.OFF;

		@Override
		public void afterUpdate(BigVaultServerTransaction transaction, long qtime) {

			StringWriter sw = new StringWriter();
			new Throwable("").printStackTrace(new PrintWriter(sw));
			long id = idUpdate.incrementAndGet();

			updates.add(LoggedItem.create(id, qtime, transaction, cleanStackTrace(sw.toString())));
			updateTimeCount.addAndGet(qtime);
			SystemLoadSimulator.simulateUpdate(transaction, qtime, loadLevel);
		}

		@Override
		public void afterQuery(SolrParams solrParams, long qtime) {
			StringWriter sw = new StringWriter();
			new Throwable("").printStackTrace(new PrintWriter(sw));
			long id = idQuery.incrementAndGet();

			LoggedItem loggedItem = LoggedItem.create(id, qtime, solrParams, cleanStackTrace(sw.toString()));
			LoggedItem currentLoggedItemWithSameTitle = getLogItemWithTitle(loggedItem.getTitle());

			if (currentLoggedItemWithSameTitle == null) {
				queries.add(loggedItem);

			} else {
				LoggedItemCall call = loggedItem.getCalls().get(0);
				currentLoggedItemWithSameTitle.withNewCall(qtime, call.description, call.stack);
			}
			queriesCount.incrementAndGet();
			queryTimeCount.addAndGet(qtime);
			SystemLoadSimulator.simulateQuery(solrParams, qtime, loadLevel);
		}

		private LoggedItem getLogItemWithTitle(String title) {
			for (LoggedItem query : queries) {
				if (title.equals(query.getTitle())) {
					return query;
				}
			}
			return null;
		}

		private String cleanStackTrace(String string) {
			//			int secondLine = string.indexOf("\n");
			//			int thirdLine = string.indexOf("\n", secondLine + 1);
			//			int fourthLine = string.indexOf("\n", thirdLine + 1);
			//			return string.substring(fourthLine + 1);
			List<String> lines = new ArrayList<>(Arrays.asList(string.split("\n")));

			//Remove the first three lines
			lines.remove(0);
			lines.remove(0);
			lines.remove(0);

			while (!lines.get(lines.size() - 1).startsWith("\tat com.constellio")) {
				lines.remove(lines.size() - 1);
			}

			return StringUtils.join(lines, "\n");
		}

		public long getQueryCount() {
			return queriesCount.get();
		}

		public long getUpdateCount() {
			return updates.size();
		}

	}

	public static class LoggedItemCall implements Serializable {

		private int repeated;

		private long qtime;

		private String description;

		private String stack;

		public LoggedItemCall(long qtime, String description, String stack) {
			this.repeated = 1;
			this.qtime = qtime;
			this.description = description;
			this.stack = stack;
		}

		public int getRepeated() {
			return repeated;
		}

		public long getQtime() {
			return qtime;
		}

		public String getDescription() {
			return description;
		}

		public String getStack() {
			return stack;
		}

		public void repeated(long qtime) {
			this.qtime += qtime;
			this.repeated++;
		}
	}

	public static class LoggedItem implements Serializable {

		public static String TITLE = "title";
		public static String ID = "id";
		public static String QTIME = "qtime";
		public static String REPEATED = "repeated";
		public static String CALLS = "calls";

		private String title;

		private long id;

		private long qtime;

		private int repeated;

		private List<LoggedItemCall> calls = new ArrayList<>();

		private LoggedItem(long id, long qtime, String title, String description, String stack) {
			this.id = id;
			this.qtime = qtime;
			this.title = title;
			this.calls.add(new LoggedItemCall(qtime, description, stack));
			this.repeated = 1;
		}

		public String getTitle() {
			return title;
		}

		public long getId() {
			return id;
		}

		public List<LoggedItemCall> getCalls() {
			return Collections.unmodifiableList(calls);
		}

		public void withNewCall(long qtime, String description, String stack) {
			LoggedItemCall callWithSameStackAndDescription = getCallWith(stack, description);
			if (callWithSameStackAndDescription == null) {
				this.calls.add(new LoggedItemCall(qtime, description, stack));
			} else {
				callWithSameStackAndDescription.repeated(qtime);
			}
			this.qtime += qtime;
			this.repeated++;
		}

		private LoggedItemCall getCallWith(String stack, String description) {
			for (LoggedItemCall call : calls) {
				if (call.description.equals(description) && call.stack.equals(stack)) {
					return call;
				}
			}
			return null;
		}

		public long getQtime() {
			return qtime;
		}

		public static LoggedItem create(long id, long qtime, SolrParams solrParams, String stack) {
			String description = LoggerUtils.toParamsString(solrParams);
			String title = LoggerUtils.toParamsString(clean(solrParams));
			return new LoggedItem(id, qtime, title, description, stack);

		}

		private static SolrParams clean(SolrParams solrParams) {
			ModifiableSolrParams clone = new ModifiableSolrParams(solrParams);
			clone.remove("qt");
			clone.remove("shards.qt");
			if ("*:*".equals(solrParams.get("q"))) {
				clone.remove("q");
			}

			List<String> newFQs = new ArrayList<>();
			if (clone.getParams("fq") != null) {
				for (String fq : clone.getParams("fq")) {
					if (!fq.equals("-type_s:index") && !fq.startsWith("collection_s:")) {
						newFQs.add(fq);
					}
				}
				clone.set("fq", newFQs.toArray(new String[0]));
			}

			return clone;
		}

		public static LoggedItem create(long id, long qtime, BigVaultServerTransaction transaction, String stack) {
			int addUpdatedElementsCount = transaction.getNewDocuments().size() + transaction.getUpdatedDocuments().size();

			String title = "Transaction of " + addUpdatedElementsCount + " elements";
			String description = "";
			return new LoggedItem(id, qtime, title, description, stack);

		}

		public int getRepeated() {
			return repeated;
		}

		public void setRepeated(int repeated) {
			this.repeated = repeated;
		}
	}

}
