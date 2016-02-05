package com.constellio.data.utils.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.constellio.data.utils.ThreadList;

public class CopySolrIndexMain {

	private static final int BATCH_SIZE = 1000;
	private static final int NUMBER_OF_THREADS = 4;

	public static void main(String argv[])
			throws Exception {

		if (argv.length < 2) {
			System.out.println("Usage   : CopySolrIndexMain <input-solr-server> <output-solr-server>");
			System.out
					.println("Exemple : CopySolrIndexMain http://localhost:8983/solr/records http://localhost:8984/solr/records");
			System.exit(0);
		}

		//SolrClient inputClient = new HttpSolrClient("http://localhost:8985/solr/records");
		//SolrClient outputClient = new HttpSolrClient("http://localhost:8986/solr/records");
		SolrClient inputClient = new HttpSolrClient(argv[0]);
		SolrClient outputClient = new HttpSolrClient(argv[1]);

		inputClient.commit();
		outputClient.commit();
		LinkedBlockingQueue<ReindexSolrIndexesMainTask> queue = new LinkedBlockingQueue<>(NUMBER_OF_THREADS);
		startAddThreads(outputClient, queue);

		String lastId = null;
		if (argv.length == 3) {
			lastId = argv[2];
		}
		int size = getSize(inputClient, lastId);
		int current = 0;
		List<SolrDocument> documents;
		while (!(documents = nextDocuments(inputClient, lastId)).isEmpty()) {

			queue.put(new ReindexSolrIndexesMainTask(documents));

			current += BATCH_SIZE;
			String firstId = (String) documents.get(0).getFieldValue("id");
			lastId = (String) documents.get(documents.size() - 1).getFieldValue("id");
			System.out.println("Indexing " + current + "/" + size + " : " + firstId + "-" + lastId);

		}

		stopThreads(queue);
		outputClient.commit();
	}

	private static int getSize(SolrClient client, String lastId)
			throws Exception {

		ModifiableSolrParams params = new ModifiableSolrParams();
		if (lastId == null) {
			params.set("q", "*:*");
		} else {
			params.set("q", "id:{" + lastId + " TO *}");
		}
		params.set("rows", 0);
		return ((Long) client.query(params).getResults().getNumFound()).intValue();

	}

	private static List<SolrInputDocument> toInputDocuments(List<SolrDocument> documents) {
		List<SolrInputDocument> inputDocuments = new ArrayList<>();
		for (SolrDocument document : documents) {
			SolrInputDocument inputDocument = new SolrInputDocument();
			for (String fieldName : document.getFieldNames()) {
				if (!fieldName.equals("_version_")) {
					Object value = document.get(fieldName);
					inputDocument.addField(fieldName, value);
				}
			}
			inputDocuments.add(inputDocument);
		}

		return inputDocuments;
	}

	private static List<SolrDocument> nextDocuments(SolrClient client, String lastId)
			throws Exception {

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("sort", "id asc");
		if (lastId == null) {
			params.set("q", "*:*");
		} else {
			params.set("q", "id:{" + lastId + " TO *}");
		}
		params.set("rows", BATCH_SIZE);

		List<SolrDocument> documents = client.query(params).getResults();
		return documents;
	}

	private static void stopThreads(LinkedBlockingQueue<ReindexSolrIndexesMainTask> queue)
			throws Exception {
		for (int i = 0; i < NUMBER_OF_THREADS; i++) {
			queue.put(new ReindexSolrIndexesMainTask(null));
		}
	}

	private static void startAddThreads(final SolrClient client, final LinkedBlockingQueue<ReindexSolrIndexesMainTask> queue) {
		ThreadList<Thread> threadList = new ThreadList<>();

		for (int i = 0; i < NUMBER_OF_THREADS; i++) {
			final int threadId = i;
			threadList.addAndStart(new Thread() {
				@Override
				public void run() {
					while (true) {
						try {
							ReindexSolrIndexesMainTask nextTask = queue.take();
							if (nextTask.documentList != null) {
								List<SolrInputDocument> documents = toInputDocuments(nextTask.documentList);
								client.add(documents);

								if (threadId == 0) {
									client.commit(true, true, true);
								}

							} else {
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}

	private static class ReindexSolrIndexesMainTask {

		List<SolrDocument> documentList;

		public ReindexSolrIndexesMainTask(List<SolrDocument> documentList) {
			this.documentList = documentList;
		}
	}

}