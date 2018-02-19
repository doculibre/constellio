package com.constellio.app.utils.scripts;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.handler.loader.XMLLoader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class SolrCloudBackup {

	private static final String[] COLLECTIONS = new String[] {"records", "events", "notifications"};
	private static final int BATCH_SIZE = 1000;
	private static final int THREADS = Runtime.getRuntime().availableProcessors() * 8;
	private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(THREADS, THREADS, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20), new ThreadPoolExecutor.CallerRunsPolicy());
	private static final AtomicLong COUNTER = new AtomicLong();

	public static void main(String[] argv)
			throws Exception {

		if (!validateArgs(argv)) {
			usage();
			return;
		}
		File localDir = new File(argv[1]);
		if (!localDir.exists()) {
			System.err.println("localDir: " + localDir + " does not exists");
		} else if (!localDir.isDirectory()) {
			System.err.println("localDir: " + localDir + " does not exists");
		}

		if (argv[0].equals("--export")) {
			CloudSolrClient client = new CloudSolrClient.Builder().withZkHost(argv[2]).build();
			for (String collection : COLLECTIONS) {
				client.setDefaultCollection(collection);
				File recordsFolder = new File(localDir, collection);
				FileUtils.forceMkdir(recordsFolder);
				exportIndex(client, recordsFolder);
			}
		} else if (argv[0].equals("--import")) {
			EXECUTOR.setCorePoolSize(2);
			EXECUTOR.setMaximumPoolSize(2);
			for (String collection : COLLECTIONS) {
				SolrClient client = new HttpSolrClient.Builder().withBaseSolrUrl(argv[2] + collection).build();
				if (isEmpty(client)) {
					File recordsFolder = new File(localDir, collection);
					importIndex(client, recordsFolder);
					client.commit();
				} else {
					throw new RuntimeException("Destination index is not empty");
				}
			}
		}

		EXECUTOR.shutdown();
		while (!EXECUTOR.isTerminated()) {
			Thread.sleep(100);
		}
	}

	private static void exportIndex(SolrClient client, final File localDir) throws IOException, SolrServerException, InterruptedException {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("sort", "_version_ asc, id asc");
		params.set("q", "*:*");
		params.set("rows", BATCH_SIZE);

		String cursor = CursorMarkParams.CURSOR_MARK_START;
		String oldCursor = null;

		long exported = 0;
		while (!cursor.equals(oldCursor)) {
			params.set(CursorMarkParams.CURSOR_MARK_PARAM, cursor);

			QueryResponse resp = client.query(params);
			List<SolrDocument> documents = resp.getResults();
			final List<SolrInputDocument> inputDocuments = toInputDocuments(documents);
			Runnable writeDocs = new Runnable() {
				@Override
				public void run() {
					for (SolrInputDocument inputDocument : inputDocuments) {
						try {
							String xml = ClientUtils.toXML(inputDocument);
							String id = (String) inputDocument.getField("id").getValue();
							String subFolderName = folderNameForId(id);

							File subFolder = new File(localDir, subFolderName);
							File docFile = new File(subFolder, id + ".xml");

							FileUtils.write(docFile, xml, StandardCharsets.UTF_8);
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(-1);
						}
					}
				}
			};
			EXECUTOR.submit(writeDocs);

			oldCursor = cursor;
			cursor = resp.getNextCursorMark();
			exported += inputDocuments.size();
			System.out.println(exported + "/" + resp.getResults().getNumFound());
		}
	}

	private static boolean isEmpty(SolrClient client)  throws IOException, SolrServerException {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "*:*");
		params.set("rows", 0);

		QueryResponse resp = client.query(params);
		long numFound = resp.getResults().getNumFound();
		return numFound == 0;
	}

	private static void importIndex(final SolrClient client, final File localDir) throws IOException, SolrServerException, InterruptedException, XMLStreamException {
		System.out.println("Importing folder : " + localDir);
		final List<File> inputFiles = new ArrayList<>();
		for (final File file : localDir.listFiles()) {
			if (file.isDirectory()) {
				importIndex(client, file);
			} else {
				inputFiles.add(file);
			}
		}
		if (!inputFiles.isEmpty()) {
			Runnable commitDocs = new Runnable() {
				@Override
				public void run() {
				try {
					List<SolrInputDocument> solrInputDocuments = new ArrayList<>();
					for (File inputFile : inputFiles) {
						XMLLoader loader = new XMLLoader();
						XMLInputFactory inputFactory = XMLInputFactory.newInstance();
						try (FileInputStream fis = new FileInputStream(inputFile)) {
							XMLStreamReader reader = inputFactory.createXMLStreamReader(fis);
							if (reader.hasNext()) {
								reader.next();
								SolrInputDocument solrInputDocument = loader.readDoc(reader);
								solrInputDocuments.add(solrInputDocument);
							}
						}
					}
					client.add(solrInputDocuments);
					COUNTER.addAndGet(solrInputDocuments.size());
					if (System.currentTimeMillis() % 10 == 0) {
						client.commit();
					}
					System.out.println("Added : " + solrInputDocuments.size() + "/" + COUNTER.get());
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
				}
			};
			EXECUTOR.submit(commitDocs);
		}
	}

	private static String folderNameForId(String id) {
		String subFolderName = StringUtils.reverse(id);
		subFolderName = StringUtils.substring(subFolderName, 0, 5);
		subFolderName = StringUtils.join(subFolderName.toCharArray(), File.separatorChar);

		return subFolderName;
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

	private static boolean validateArgs(String argv[]) throws Exception {
		if (argv.length != 3) {
			return false;
		}
		if (!argv[0].equals("--import") && !argv[0].equals("--export")) {
			return false;
		}
		return true;
	}

	private static void usage() {
		System.out.println("Usage: SolrCloudBackup OPTIONS <zkAddress>");
		System.out.println("OPTIONS");
		System.out.println(" --import <localDir>");
		System.out.println("         Imports <localDir> in Solr, if empty");
		System.out.println(" --export <localDir>");
		System.out.println("         Exports Solr to <localDir>, if empty");
	}
}