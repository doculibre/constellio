package com.constellio.app.utils.scripts;

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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Binary format :
 * int(size)bytes[](xml)int(size)bytes[](xml)....
 * <p>
 * The order of the fields in the XML can vary.
 */
public class SolrBinaryStreamBackup {

	private static final String[] COLLECTIONS = new String[]{"records", "events", "notifications"};
	private static final int THREADS = 1;
	private static final int BATCH_SIZE = 1000;

	public static void main(String[] argv)
			throws Exception {

		if (!validateArgs(argv)) {
			usage();
			System.exit(-1);
		}
		File localDir = new File(argv[1]);
		if (!localDir.exists()) {
			System.err.println("localDir: " + localDir + " does not exists");
			System.exit(-1);
		} else if (!localDir.isDirectory()) {
			System.err.println("localDir: " + localDir + " is not a directory");
			System.exit(-1);
		}

		if (argv[0].equals("--export")) {
			for (String collection : COLLECTIONS) {
				try (SolrClient solrClient = buildClient(argv[2], collection)) {
					File collectionOutputFile = new File(localDir, collection + ".output");
					if (collectionOutputFile.exists()) {
						System.err.println("export file: " + collectionOutputFile + " exits");
						System.exit(-1);
					}
					exportIndex(solrClient, collectionOutputFile);
				}
			}
		} else if (argv[0].equals("--import")) {
			importCollectionsIndex(argv[2], localDir);
		}
	}

	private static SolrClient buildClient(String solrParam, String collection) {
		if (solrParam.startsWith("http")) {
			if (!solrParam.endsWith("/solr/")) {
				throw new IllegalArgumentException("Invalid solrUrl");
			}
			return new HttpSolrClient.Builder().withBaseSolrUrl(solrParam + collection).build();
		} else {
			CloudSolrClient cloudSolrClient = new CloudSolrClient.Builder().withZkHost(solrParam).build();
			cloudSolrClient.setDefaultCollection(collection);
			return cloudSolrClient;
		}
	}

	private static void closeExecutor(ThreadPoolExecutor executor)
			throws InterruptedException {
		executor.shutdown();
		while (!executor.isTerminated()) {
			Thread.sleep(100);
		}

	}

	private static void exportIndex(SolrClient client, final File collectionOuputFile)
			throws IOException, SolrServerException {
		try (OutputStream fos = new FileOutputStream(collectionOuputFile);
			 BufferedOutputStream bos = new BufferedOutputStream(fos, 128 * 1024);
			 GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bos);
			 DataOutputStream dos = new DataOutputStream(gzipOutputStream)) {

			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("sort", "id asc");
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
				for (SolrInputDocument inputDocument : inputDocuments) {
					try {
						String xml = ClientUtils.toXML(inputDocument);
						byte[] xmlBytes = xml.getBytes("utf-8");
						dos.writeInt(xmlBytes.length);
						dos.write(xmlBytes);
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(-1);
					}
				}

				oldCursor = cursor;
				cursor = resp.getNextCursorMark();
				exported += inputDocuments.size();
				System.out.println(exported + "/" + resp.getResults().getNumFound());
			}
			dos.writeInt(-1);
		}
	}

	private static boolean isEmpty(SolrClient client)
			throws IOException, SolrServerException {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "*:*");
		params.set("rows", 0);

		QueryResponse resp = client.query(params);
		long numFound = resp.getResults().getNumFound();
		return numFound == 0;
	}

	public static void importCollectionsIndex(final String solrParam, final File localDir)
			throws IOException, SolrServerException, InterruptedException, XMLStreamException {
		for (String collection : COLLECTIONS) {
			try (SolrClient client = buildClient(solrParam, collection)) {
				if (isEmpty(client)) {
					File recordsFolder = new File(localDir, collection + ".output");
					importCollectionIndex(client, recordsFolder);
					client.commit();
				} else {
					throw new RuntimeException("Destination index is not empty");
				}
			}
		}
	}

	public static void importCollectionIndex(final SolrClient client, final File collectionOuputFile)
			throws IOException, SolrServerException, InterruptedException, XMLStreamException {
		System.out.println("Importing collection file : " + collectionOuputFile);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(THREADS, THREADS, 10, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(20), new ThreadPoolExecutor.CallerRunsPolicy());
		AtomicLong counter = new AtomicLong();
		XMLLoader loader = new XMLLoader();
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try (FileInputStream fis = new FileInputStream(collectionOuputFile);
			 BufferedInputStream bis = new BufferedInputStream(fis, 128 * 1024);
			 GZIPInputStream gzipInputStream = new GZIPInputStream(bis);
			 DataInputStream dis = new DataInputStream(gzipInputStream)) {
			List<SolrInputDocument> solrInputDocuments = new ArrayList<>();
			for (int xmlLength = dis.readInt(); xmlLength > 0; xmlLength = dis.readInt()) {
				byte[] xmlBytes = new byte[xmlLength];
				dis.readFully(xmlBytes);
				XMLStreamReader reader = inputFactory.createXMLStreamReader(new ByteArrayInputStream(xmlBytes));
				if (reader.hasNext()) {
					reader.next();
					SolrInputDocument solrInputDocument = loader.readDoc(reader);
					solrInputDocuments.add(solrInputDocument);
				}

				if (solrInputDocuments.size() % 1000 == 0) {
					final List<SolrInputDocument> buffer = new ArrayList<>(solrInputDocuments);
					solrInputDocuments.clear();
					addToSolr(client, buffer, counter, executor);
				}
			}

			if (!solrInputDocuments.isEmpty()) {
				addToSolr(client, solrInputDocuments, counter, executor);
			}
		}

		closeExecutor(executor);
		client.commit();
		System.out.println("Total imported : " + counter.get());

	}

	private static void addToSolr(final SolrClient client, final List<SolrInputDocument> buffer,
								  final AtomicLong counter, ThreadPoolExecutor executor) {
		Runnable writeDocs = new Runnable() {
			@Override
			public void run() {
				try {
					client.add(buffer);
					client.commit();
					System.out.println("Imported : " + counter.addAndGet(buffer.size()));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		};
		executor.submit(writeDocs);
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

	private static boolean validateArgs(String argv[]) {
		if (argv.length != 3) {
			return false;
		}
		if (!argv[0].equals("--import") && !argv[0].equals("--export")) {
			return false;
		}
		return true;
	}

	private static void usage() {
		System.out.println("Usage: SolrCloudBackup OPTIONS <zkAddress|solrUrl>");
		System.out.println("OPTIONS");
		System.out.println(" --import <localDir>");
		System.out.println("         Imports <localDir> in Solr, if empty");
		System.out.println(" --export <localDir>");
		System.out.println("         Exports Solr to <localDir>, if empty");
	}
}