/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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

import com.constellio.app.api.extensions.PagesComponentsExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.extensions.BigVaultServerExtension;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.LoggerUtils;
import com.constellio.sdk.tests.SystemLoadSimulator.SystemLoadLevel;
import com.vaadin.ui.Component;

public class TestPagesComponentsExtensions extends PagesComponentsExtensions {

	private static Logger LOGGER = LoggerFactory.getLogger(TestPagesComponentsExtensions.class);

	AppLayerFactory appLayerFactory;

	BottomPanelBigVaultServerExtension bottomPanelBigVaultServerExtension;

	public TestPagesComponentsExtensions(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.bottomPanelBigVaultServerExtension = new BottomPanelBigVaultServerExtension();
		this.appLayerFactory.getModelLayerFactory().getDataLayerFactory().getExtensions()
				.getSystemWideExtensions().bigVaultServerExtension.add(this.bottomPanelBigVaultServerExtension);
	}

	@Override
	public Factory<Component> getFooterComponentFactory() {
		return null;
	}

	@Override
	public Factory<Component> getLicenseComponentFactory() {
		return new LicenseComponentFactory();
	}

	private class LicenseComponentFactory implements Factory<Component> {

		@Override
		public Component get() {
			return new SDKPanel();
		}
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
			for (String fq : clone.getParams("fq")) {
				if (!fq.equals("-type_s:index") && !fq.startsWith("collection_s:")) {
					newFQs.add(fq);
				}
			}
			clone.set("fq", newFQs.toArray(new String[0]));

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
