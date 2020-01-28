package com.constellio.sdk.tests;

import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.events.EventBusManager;
import com.constellio.data.events.SDKEventBusSendingDelayedService;
import com.constellio.data.events.SDKEventBusSendingService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.ManualDataEntry;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.setups.SchemaShortcuts;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.groups.Tuple;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval.PREFERRING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {

	public static final String chuckNorris = "Chuck Norris";
	private static Random random = new Random();
	private static int gen;
	public static Comparator<? super DecommissioningList> comparingRecordWrapperIds = new Comparator<DecommissioningList>() {
		@Override
		public int compare(DecommissioningList o1, DecommissioningList o2) {
			return o1.getId().endsWith(o2.getId()) ? 0 : 1;
		}
	};
	public static Comparator<Object> comparingListAnyOrder = new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
			List list1 = (List<String>) o1;
			List list2 = (List<String>) o2;
			boolean equal = CollectionUtils.isEqualCollection(list1, list2);
			return equal ? 0 : 1;
		}
	};

	private TestUtils() {
	}

	public static String frenchPangram() {
		return "ABCDEFGHIJKLMNOPQRST";
	}

	public static byte[] aByteArray() {
		return aByteArray(2);
	}

	public static byte[] aByteArray(int length) {
		byte[] bytes = new byte[length];
		for (int i = 0; i < length; i++) {
			if (gen >= Byte.MAX_VALUE) {
				gen = 0;
			}
			bytes[i] = Integer.valueOf(++gen).byteValue();
		}
		return bytes;
	}

	public static byte[] aRandomByteArray(int length) {
		byte[] bytes = new byte[length];
		random.nextBytes(bytes);
		return bytes;
	}

	public static LocalDateTime aDateTime() {
		return new LocalDateTime(2000 + random.nextInt(20), random.nextInt(13 - 1) + 1, random.nextInt(29 - 1) + 1,
				random.nextInt(24 - 1) + 1, random.nextInt(60 - 1) + 1);
	}

	public static LocalDate aDate() {
		return new LocalDate(2000 + random.nextInt(20), random.nextInt(13 - 1) + 1, random.nextInt(29 - 1) + 1);
	}

	public static File aFile() {
		return new File(aString());
	}

	public static File aFile(File parent) {
		return new File(parent, aString());
	}

	public static long aLong() {
		return anInteger();
	}

	public static int anInteger() {
		if (gen > 100000000) {
			gen = 0;
		}
		return ++gen;
	}

	public static String aString() {
		return "" + anInteger();
	}

	public static String[] aStringArray() {
		final int maxArraySize = 10;
		String[] theStringArray = new String[maxArraySize];

		for (int i = 0; i < random.nextInt(maxArraySize); i++) {
			theStringArray[i] = aString();
		}

		return theStringArray;
	}

	public static Set<Class<?>> getElementsClasses(Set<?> objects) {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		for (Object o : objects) {
			classes.add(o.getClass());
		}
		return classes;
	}

	public static boolean isIntegrationServer() {
		return new File(".").getAbsolutePath().contains("jenkins");
	}

	@SuppressWarnings("rawtypes")
	public static <T> Condition onlyElementsOfClass(final Class<?>... classes) {

		return new Condition() {

			@SuppressWarnings("unchecked")
			@Override
			public boolean matches(Object value) {
				List<Class<?>> valuesClasses = new ArrayList<>();
				Iterator<T> iterator = ((Iterable) value).iterator();
				while (iterator.hasNext()) {
					valuesClasses.add(iterator.next().getClass());
				}
				try {
					assertThat(valuesClasses).containsOnly(classes);
					return true;
				} catch (Exception e) {
					return false;
				}
			}

		};
	}

	public static void assertThatToEqualsAndToStringThrowNoException(Object o, Object o2) {
		assertThat(o).isEqualTo(o2);
		assertThat(o.toString()).isEqualTo(o2.toString());
		assertThat(o.hashCode()).isEqualTo(o2.hashCode());

		for (Method method : o.getClass().getMethods()) {
			boolean getter = method.getName().startsWith("get") || method.getName().startsWith("is");
			if (getter && method.getGenericParameterTypes().length == 0) {
				try {
					method.invoke(o);
				} catch (IllegalAccessException e) {

				} catch (InvocationTargetException e) {

				}
			}
		}

	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static Condition<? super Object> unmodifiableCollection() {
		return new Condition() {

			@Override
			public boolean matches(Object value) {
				Collection collection = (Collection) value;
				try {
					collection.clear();
					return false;
				} catch (Exception e) {
					return true;
				}
			}

		}.describedAs("unmodifiable");
	}

	public static List<String> ids(List<Record> records) {
		return new RecordUtils().toIdList(records);
	}

	public static String[] idsArray(Record... records) {
		return ids(asList(records)).toArray(new String[0]);
	}

	public static <K, V> Map<K, V> asMap(K key1, V value1) {
		Map<K, V> map = new HashMap<K, V>();
		map.put(key1, value1);
		return map;
	}

	public static <K, V> Map<K, V> asMap(K key1, V value1, K key2, V value2) {
		Map<K, V> map = new HashMap<K, V>();
		map.put(key1, value1);
		map.put(key2, value2);
		return map;
	}

	public static <K, V> Map<K, V> asMap(K key1, V value1, K key2, V value2, K key3, V value3) {
		Map<K, V> map = new HashMap<K, V>();
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		return map;
	}

	public static <K, V> Map<K, V> asMap(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
		Map<K, V> map = new HashMap<K, V>();
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		return map;
	}

	public static <K, V> Map<K, V> asMap(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5,
										 V value5) {
		Map<K, V> map = new HashMap<K, V>();
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		map.put(key5, value5);
		return map;
	}

	public static <K, V> Map<K, V> asMap(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5,
										 V value5, K key6, V value6) {
		Map<K, V> map = new HashMap<K, V>();
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		map.put(key4, value4);
		map.put(key5, value5);
		map.put(key6, value6);
		return map;
	}

	public static Map<String, Object> asStringObjectMap(String key1, Object value1) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(key1, value1);
		return map;
	}

	public static Map<String, Object> asStringObjectMap(String key1, Object value1, String key2, Object value2) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(key1, value1);
		map.put(key2, value2);
		return map;
	}

	public static Map<String, Object> asStringObjectMap(String key1, Object value1, String key2, Object value2,
														String key3,
														Object value3) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		return map;
	}

	private static short nextMetadataId = 1000;

	public static Metadata mockMetadata(String code) {
		if (nextMetadataId > 3000) {
			nextMetadataId = 1000;
		}
		String localCode = code.split("_")[2];
		final Metadata metadata = mock(Metadata.class, code);
		when(metadata.getCode()).thenReturn(code);
		when(metadata.getSchemaCode()).thenReturn(code.replace("_" + localCode, ""));
		when(metadata.getLocalCode()).thenReturn(localCode);
		when(metadata.getId()).thenReturn(nextMetadataId++);

		when(metadata.getInheritanceCode()).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation)
					throws Throwable {
				return metadata.computeInheritanceCode();
			}
		});

		when(metadata.isGlobal()).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation)
					throws Throwable {
				return metadata.computeIsGlobal();
			}
		});
		return metadata;
	}

	public static Metadata mockManualMultivalueMetadata(String code, MetadataValueType type) {
		Metadata metadata = mockManualMetadata(code, type);
		when(metadata.isMultivalue()).thenReturn(true);
		return metadata;

	}

	public static Metadata mockManualMetadata(String code, MetadataValueType type) {
		String localCode = code.split("_")[2];
		Metadata metadata = mock(Metadata.class, code);
		when(metadata.getCode()).thenReturn(code);
		when(metadata.getLocalCode()).thenReturn(localCode);
		when(metadata.getType()).thenReturn(type);
		if (type == MetadataValueType.CONTENT) {
			when(metadata.getStructureFactory()).thenReturn(new ContentFactory());
		}
		when(metadata.getDataEntry()).thenReturn(new ManualDataEntry());
		return metadata;
	}

	public static List<String> recordsIds(List<Record> records) {
		List<String> ids = new ArrayList<>();
		for (Record record : records) {
			ids.add(record.getId());
		}
		return ids;
	}

	public static List<String> usernamesOf(List<UserCredential> users) {
		List<String> ids = new ArrayList<>();
		for (UserCredential user : users) {
			ids.add(user.getUsername());
		}
		return ids;
	}

	@SafeVarargs
	public static <T> Set<T> asSet(T... elements) {
		return new HashSet<T>(asList(elements));
	}

	public static RecordDTO newRecordDTO(String id, SchemaShortcuts schema) {

		Map<String, Object> fields = new HashMap<>();
		fields.put("collection_s", schema.collection());
		fields.put("schema_s", schema.code());

		return new SolrRecordDTO(id, anInteger(), null, fields, RecordDTOMode.FULLY_LOADED);
	}

	public static Condition<? super List<String>> noDuplicates() {
		return new Condition<List<String>>() {
			@Override
			public boolean matches(List<String> list) {
				Set<String> set = new HashSet<>();

				for (String item : list) {
					if (set.contains(item)) {
						fail("Item '" + item + "' was found twice in list");
					} else {
						set.add(item);
					}
				}
				return true;
			}
		};
	}

	public static void printDocument(Document document) {
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		System.out.println(xmlOutput.outputString(document));
	}

	public static void print(RecordWrapper wrapper) {
		StringBuilder stringBuilder = new StringBuilder(
				"Record '" + wrapper.getId() + "' of schema '" + wrapper.getSchemaCode() + "'");

		final List<Metadata> metadatas = new ArrayList<>(wrapper.getSchema().getMetadatas());
		Collections.sort(metadatas, new Comparator<Metadata>() {
			@Override
			public int compare(Metadata o1, Metadata o2) {
				return o1.getLocalCode().compareTo(o2.getLocalCode());
			}
		});

		for (Metadata metadata : metadatas) {
			if (wrapper.hasValue(metadata.getLocalCode())) {
				stringBuilder.append(metadata.getLocalCode() + ": " + wrapper.get(metadata) + "\n");
			}
		}
		System.out.println(stringBuilder.toString());

	}

	public static void write(Document document, File file) {
		XMLOutputter xmlOutput = new XMLOutputter();
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);

			// display nice nice
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(document, fileWriter);

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(fileWriter);
		}

	}

	public static SolrInputDocument solrInputDocumentRemovingMetadatas(String id, List<Metadata> metadatas) {

		SolrInputDocument solrInputDocument = new SolrInputDocument();
		solrInputDocument.setField("id", id);
		for (Metadata metadata : metadatas) {

			Map<String, Object> map = new HashMap<>();
			map.put("set", null);

			solrInputDocument.setField(metadata.getDataStoreCode(), map);
		}

		return solrInputDocument;
	}

	public static SolrInputDocument solrInputDocumentRemovingMetadatas(String id, Metadata... metadatas) {
		return solrInputDocumentRemovingMetadatas(id, asList(metadatas));
	}

	public static Condition<? super File> zipFileWithSameContentThan(final File expectedZipFile) {
		return zipFileWithSameContentExceptingFiles(expectedZipFile);
	}

	public static ListAssert<String> assertFilesInZip(File file) {

		File tempFolder = null;
		try {

			tempFolder = File.createTempFile("temp", Long.toString(System.nanoTime()));
			tempFolder.delete();
			ZipService zipService = new ZipService(new IOServices(tempFolder));

			File unzipFolder = new File(tempFolder, "unzipFolder");
			unzipFolder.mkdirs();
			zipService.unzip(file, unzipFolder);

			List<String> containedFiles = new ArrayList<>();
			for (File aContainedFile : FileUtils.listFiles(unzipFolder, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
				String filePath = aContainedFile.getAbsolutePath().replace(unzipFolder.getAbsolutePath(), "").replace("\\", "/");
				containedFiles.add(filePath.substring(1));
			}

			Collections.sort(containedFiles);

			return assertThat(containedFiles);

		} catch (Exception e) {
			throw new RuntimeException(e);

		} finally {
			FileUtils.deleteQuietly(tempFolder);
		}
	}

	public static Condition<? super File> zipFileWithSameContentExceptingFiles(final File expectedZipFile,
																			   final String... exceptFiles) {
		return new Condition<File>() {
			@Override
			public boolean matches(File validatedZipFile) {
				File tempFolder = null;
				try {

					tempFolder = File.createTempFile("temp", Long.toString(System.nanoTime()));
					tempFolder.delete();

					ZipService zipService = new ZipService(new IOServices(tempFolder));

					File unzipFolder1 = new File(tempFolder, "unzip1");
					unzipFolder1.mkdirs();
					zipService.unzip(validatedZipFile, unzipFolder1);

					File unzipFolder2 = new File(tempFolder, "unzip2");
					unzipFolder2.mkdirs();
					zipService.unzip(expectedZipFile, unzipFolder2);

					compareFolder(unzipFolder1.getAbsolutePath(), unzipFolder1, unzipFolder2, asList(exceptFiles));

				} catch (Exception e) {
					throw new RuntimeException(e);

				} finally {
					FileUtils.deleteQuietly(tempFolder);
				}


				return true;
			}
		};
	}

	private static void compareFolder(String absolutePathToRemove, File validatedFolder, File expectedFolder,
									  List<String> exceptFiles) {

		List<String> filesInFolder1 = getFilesInFolder(validatedFolder, exceptFiles);
		List<String> filesInFolder2 = getFilesInFolder(expectedFolder, exceptFiles);

		String folderAbsolutePath = validatedFolder.getAbsolutePath().replace(absolutePathToRemove, "/");
		assertThat(filesInFolder1).describedAs("Folder '" + folderAbsolutePath + "'").isEqualTo(filesInFolder2);

		for (String file : filesInFolder1) {
			File file1 = new File(validatedFolder, file);
			File file2 = new File(expectedFolder, file);
			String fileAbsolutePath = folderAbsolutePath + file;
			//if (!file1.getName().contains("schemasDisplay.xml")) {

			String file1Content = null;
			try {
				file1Content = FileUtils.readFileToString(file1, "UTF-8");
			} catch (IOException e) {
				file1Content = "<FILE DOES NOT EXIST>";
			}
			String file2Content = null;
			try {
				file2Content = FileUtils.readFileToString(file2, "UTF-8");
			} catch (IOException e) {
				file2Content = "<FILE DOES NOT EXIST>";
			}


			if (!file1Content.equals(file2Content)) {
				assertThat("FICHIER OBTENU (ne pas tenir compte de cette ligne)" + " :\n"
						   + file1Content).describedAs("Actual content of file '" + fileAbsolutePath
													   + "' is not equal to the expected content")
						.isEqualTo("FICHIER DE RÉFÉRENCE (ne pas tenir compte de cette ligne) :\n"
								   + file2Content);
			}
			System.out.println(file1.getName() + " is OK");
			//}
		}

		List<String> foldersInFolder1 = getFoldersInFolder(validatedFolder);
		foldersInFolder1.remove("__MACOSX");
		List<String> foldersInFolder2 = getFoldersInFolder(expectedFolder);
		foldersInFolder2.remove("__MACOSX");
		assertThat(foldersInFolder1).describedAs("Folder '" + folderAbsolutePath + "'")
				.isEqualTo(foldersInFolder2);

		for (String file : foldersInFolder1) {
			File folder1 = new File(validatedFolder, file);
			File folder2 = new File(expectedFolder, file);

			compareFolder(absolutePathToRemove, folder1, folder2, exceptFiles);
		}
	}

	private static List<String> getFilesInFolder(File folder, List<String> exceptFiles) {
		String[] filenames = folder.list(onlyFile);
		if (filenames == null) {
			return new ArrayList<>();
		}
		List<String> files = new ArrayList<>(asList(filenames));
		files.remove(".DS_Store");
		Collections.sort(files);
		files.removeAll(exceptFiles);
		return files;
	}

	private static List<String> getFoldersInFolder(File folder) {
		String[] filenames = folder.list(onlyFolder);
		if (filenames == null) {
			return new ArrayList<>();
		}
		List<String> files = new ArrayList<>(asList(filenames));
		Collections.sort(files);
		return files;
	}

	private static FilenameFilter onlyFile = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return new File(dir, name).isFile();
		}
	};
	private static FilenameFilter onlyFolder = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return new File(dir, name).isDirectory();
		}
	};

	public static class MapBuilder<K, V> {

		Map<K, V> map = new HashMap<>();

		public static <K, V> MapBuilder<K, V> with(K key, V value) {
			return new MapBuilder<K, V>().andWith(key, value);
		}

		public MapBuilder<K, V> andWith(K key, V value) {
			map.put(key, value);
			return this;
		}

		public Map<K, V> build() {
			return map;
		}
	}

	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static RecordWrapperAssert assertThatRecord(RecordWrapper recordWrapper) {
		return new RecordWrapperAssert(recordWrapper);
	}

	public static RecordsAssert assertThatRecords(RecordWrapper... actual) {
		return assertThatRecords(asList(actual));
	}

	public static RecordsAssert assertThatRecords(List<?> actual) {
		return new RecordsAssert(actual);
	}

	public static RecordAssert assertThatRecord(Record actual) {
		return new RecordAssert(actual);
	}

	public static class RecordAssert extends ObjectAssert<Record> {

		protected RecordAssert(Record actual) {
			super(actual);
		}

		public RecordAssert hasMetadataValue(final Metadata metadata, final Object expectedValue) {
			assertThat(actual).describedAs("record").isNotNull();
			return (RecordAssert) super.has(new Condition<Record>() {
				@Override
				public boolean matches(Record value) {
					assertThat((Object) actual.get(metadata)).as((metadata.getCode())).isEqualTo(expectedValue);
					return true;
				}
			});
		}

		public RecordAssert hasNoMetadataValue(final Metadata metadata) {
			assertThat(actual).describedAs("record").isNotNull();
			return (RecordAssert) super.has(new Condition<Record>() {
				@Override
				public boolean matches(Record value) {

					if (metadata.isMultivalue()) {
						assertThat(actual.getList(metadata)).as((metadata.getCode())).isEmpty();
					} else {

						assertThat((Object) actual.get(metadata)).as((metadata.getCode())).isNull();
					}
					return true;
				}
			});
		}

		private Object getMetadataValue(Record record, String metadataLocalCode) {
			MetadataSchema schema = ConstellioFactories.getInstance().getModelLayerFactory()
					.getMetadataSchemasManager().getSchemaTypes(((Record) record).getCollection())
					.getSchema(((Record) record).getSchemaCode());
			Metadata metadata = schema.getMetadata(metadataLocalCode);
			if (metadata.isMultivalue()) {
				return record.getList(metadata);
			} else {
				return record.get(metadata);
			}
		}

		public ListAssert<Object> extracting(Metadata... metadatas) {
			String[] metadatasLocalCodes = new String[metadatas.length];
			for (int i = 0; i < metadatas.length; i++) {
				metadatasLocalCodes[i] = metadatas[i].getLocalCode();
			}
			return extracting(metadatasLocalCodes);
		}

		public ListAssert<Object> extracting(String... metadatas) {
			Object[] objects = new Object[metadatas.length];

			if (actual instanceof Record) {

				for (int i = 0; i < metadatas.length; i++) {
					String metadata = metadatas[i];
					String refMetadata = null;
					if (metadata.contains(".")) {
						refMetadata = org.apache.commons.lang3.StringUtils.substringAfter(metadata, ".");
						metadata = org.apache.commons.lang3.StringUtils.substringBefore(metadata, ".");
					}
					objects[i] = getMetadataValue(((Record) actual), metadata);

					if (refMetadata != null && objects[i] != null) {
						Record referencedRecord = ConstellioFactories.getInstance().getModelLayerFactory().newRecordServices()
								.getDocumentById((String) objects[i]);
						objects[i] = getMetadataValue(referencedRecord, refMetadata);
					}
				}
			} else if (actual instanceof RecordWrapper) {
				for (int i = 0; i < metadatas.length; i++) {
					String metadata = metadatas[i];
					String refMetadata = null;
					if (metadata.contains(".")) {
						refMetadata = org.apache.commons.lang3.StringUtils.substringAfter(metadata, ".");
						metadata = org.apache.commons.lang3.StringUtils.substringBefore(metadata, ".");
					}

					objects[i] = ((RecordWrapper) actual).get(metadata);

					if (refMetadata != null && objects[i] != null) {
						Record referencedRecord = ConstellioFactories.getInstance().getModelLayerFactory().newRecordServices()
								.getDocumentById((String) objects[i]);
						objects[i] = getMetadataValue(referencedRecord, refMetadata);
					}
				}
			} else {
				throw new RuntimeException("Unsupported object of class '" + actual.getClass());
			}
			return assertThat(Arrays.asList(objects));
		}

		public void exists() {
			RecordServices recordServices = ConstellioFactories.getInstance().getModelLayerFactory().newRecordServices();
			try {
				recordServices.getDocumentById(actual.getId());

			} catch (Exception e) {
				fail("Record '" + actual.getId() + "' is supposed to exist, but it does not");
			}
		}
	}

	public static class RecordsAssert extends ListAssert<Object> {

		protected RecordsAssert(List<?> actual) {
			super((List<Object>) actual);
		}

		public ListAssert<Tuple> extractingMetadatas(Metadata... metadatas) {
			List<Tuple> values = new ArrayList<>();

			for (Object record : actual) {
				Object[] objects = new Object[metadatas.length];
				for (int i = 0; i < metadatas.length; i++) {
					if (record instanceof Record) {
						objects[i] = ((Record) record).get(metadatas[i]);
					} else if (record instanceof RecordWrapper) {
						objects[i] = ((RecordWrapper) record).get(metadatas[i]);
					} else {
						throw new RuntimeException("Unsupported object of class '" + record.getClass());
					}
				}
				values.add(new Tuple(objects));
			}

			return assertThat(values);
		}

		private Object getMetadataValue(Record record, String metadataLocalCode) {
			MetadataSchema schema = ConstellioFactories.getInstance().getModelLayerFactory()
					.getMetadataSchemasManager().getSchemaTypes(((Record) record).getCollection())
					.getSchema(((Record) record).getSchemaCode());
			Metadata metadata = schema.getMetadata(metadataLocalCode);
			if (metadata.isMultivalue()) {
				return record.getList(metadata, locale, mode);
			} else {
				if (metadata.hasSameCode(Schemas.IDENTIFIER) || metadata.hasSameCode(Schemas.LEGACY_ID)) {
					return record.get(metadata, locale, PREFERRING);
				} else {
					return record.get(metadata, locale, mode);
				}
			}
		}

		public ListAssert<Tuple> extractingMetadatas(String... metadatas) {
			List<Tuple> values = getTuples(metadatas);

			return assertThat(values);
		}

		@NotNull
		protected List<Tuple> getTuples(String[] metadatas) {
			List<Tuple> values = new ArrayList<>();

			for (Object record : actual) {
				Object[] objects = new Object[metadatas.length];

				if (record instanceof Record) {

					for (int i = 0; i < metadatas.length; i++) {
						String metadata = metadatas[i];
						String refMetadata = null;
						if (metadata.contains(".")) {
							refMetadata = org.apache.commons.lang3.StringUtils.substringAfter(metadata, ".");
							metadata = org.apache.commons.lang3.StringUtils.substringBefore(metadata, ".");
						}
						objects[i] = getMetadataValue(((Record) record), metadata);

						if (refMetadata != null && objects[i] != null) {
							Record referencedRecord = ConstellioFactories.getInstance().getModelLayerFactory().newRecordServices()
									.getDocumentById((String) objects[i]);
							objects[i] = getMetadataValue(referencedRecord, refMetadata);
						}
					}
				} else if (record instanceof RecordWrapper) {
					for (int i = 0; i < metadatas.length; i++) {
						String metadata = metadatas[i];
						String refMetadata = null;
						if (metadata.contains(".")) {
							refMetadata = org.apache.commons.lang3.StringUtils.substringAfter(metadata, ".");
							metadata = org.apache.commons.lang3.StringUtils.substringBefore(metadata, ".");
						}

						objects[i] = ((RecordWrapper) record).get(metadata, locale, mode);

						if (refMetadata != null && objects[i] != null) {
							if (objects[i] instanceof String) {

								Record referencedRecord = ConstellioFactories.getInstance().getModelLayerFactory()
										.newRecordServices().getDocumentById((String) objects[i]);
								objects[i] = getMetadataValue(referencedRecord, refMetadata);
							} else if (objects[i] instanceof List) {
								List<Object> referencedMetas = new ArrayList<>();

								for (String id : (List<String>) objects[i]) {
									Record referencedRecord = ConstellioFactories.getInstance().getModelLayerFactory()
											.newRecordServices().getDocumentById(id);
									referencedMetas.add(getMetadataValue(referencedRecord, refMetadata));
								}

								objects[i] = referencedMetas;

							}
						}
					}
				} else {
					throw new RuntimeException("Unsupported object of class '" + record.getClass());
				}
				values.add(new Tuple(objects));
			}
			return values;
		}

		public <T> ListAssert<T> extractingMetadata(String metadata) {
			List<Tuple> tuples = getTuples(new String[]{metadata});

			List<T> untupledValues = new ArrayList<>();
			for (Tuple tuple : tuples) {
				untupledValues.add((T) tuple.toArray()[0]);
			}

			return assertThat(untupledValues);
		}

		LocalisedRecordMetadataRetrieval mode;
		Locale locale;

		public RecordsAssert preferring(Locale locale) {
			this.mode = PREFERRING;
			this.locale = locale;
			return this;
		}

		public RecordsAssert strictlyUsing(Locale locale) {
			this.mode = LocalisedRecordMetadataRetrieval.STRICT;
			this.locale = locale;
			return this;
		}
	}

	public static class RecordWrapperAssert extends ObjectAssert<RecordWrapper> {

		protected RecordWrapperAssert(RecordWrapper actual) {
			super(actual);
		}

		public RecordWrapperAssert hasMetadata(final Metadata metadata, final Object expectedValue) {
			//return hasMetadata(metadata.getLocalCode(), expectedValue);
			assertThat(actual).isNotNull();
			return (RecordWrapperAssert) super.has(new Condition<RecordWrapper>() {
				@Override
				public boolean matches(RecordWrapper value) {
					assertThat((Object) actual.getWrappedRecord().get(metadata)).as((metadata.getCode()))
							.isEqualTo(expectedValue);
					return true;
				}
			});
		}

		public RecordWrapperAssert hasMetadata(final String metadataLocalCode, final Object expectedValue) {
			return hasMetadata(actual.getSchema().getMetadata(metadataLocalCode), expectedValue);
			//			assertThat(actual).isNotNull();
			//			return (RecordWrapperAssert) super.has(new Condition<RecordWrapper>() {
			//				@Override
			//				public boolean matches(RecordWrapper value) {
			//					Metadata metadata = value.getSchema().getMetadata(metadataLocalCode);
			//					assertThat(actual.getWrappedRecord().get(metadata)).as((metadata.getCode())).isEqualTo(expectedValue);
			//					return true;
			//				}
			//			});
		}

		public void doesNotExist() {
			ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
			try {
				modelLayerFactory.newRecordServices().getDocumentById(actual.getId());
				fail("Record " + actual.getId() + "-" + actual.getTitle() + " does exist");
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {

			}
		}

		public void exist() {
			ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
			try {
				modelLayerFactory.newRecordServices().getDocumentById(actual.getId());
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				fail("Record " + actual.getId() + "-" + actual.getTitle() + " does not exist");
			}
		}

		private Object getMetadataValue(Record record, String metadataLocalCode) {
			MetadataSchema schema = ConstellioFactories.getInstance().getModelLayerFactory()
					.getMetadataSchemasManager().getSchemaTypes(((Record) record).getCollection())
					.getSchema(((Record) record).getSchemaCode());
			Metadata metadata = schema.getMetadata(metadataLocalCode);
			if (metadata.isMultivalue()) {
				return record.getList(metadata);
			} else {
				return record.get(metadata);
			}
		}

		public ListAssert<Object> extracting(String... metadatas) {
			Object[] objects = new Object[metadatas.length];

			if (actual instanceof Record) {

				for (int i = 0; i < metadatas.length; i++) {
					String metadata = metadatas[i];
					String refMetadata = null;
					if (metadata.contains(".")) {
						refMetadata = org.apache.commons.lang3.StringUtils.substringAfter(metadata, ".");
						metadata = org.apache.commons.lang3.StringUtils.substringBefore(metadata, ".");
					}
					objects[i] = getMetadataValue(((Record) actual), metadata);

					if (refMetadata != null && objects[i] != null) {
						Record referencedRecord = ConstellioFactories.getInstance().getModelLayerFactory().newRecordServices()
								.getDocumentById((String) objects[i]);
						objects[i] = getMetadataValue(referencedRecord, refMetadata);
					}
				}
			} else if (actual instanceof RecordWrapper) {
				for (int i = 0; i < metadatas.length; i++) {
					String metadata = metadatas[i];
					String refMetadata = null;
					if (metadata.contains(".")) {
						refMetadata = org.apache.commons.lang3.StringUtils.substringAfter(metadata, ".");
						metadata = org.apache.commons.lang3.StringUtils.substringBefore(metadata, ".");
					}

					objects[i] = ((RecordWrapper) actual).get(metadata);

					if (refMetadata != null && objects[i] != null) {
						if (objects[i] instanceof String) {
							Record referencedRecord = ConstellioFactories.getInstance().getModelLayerFactory().newRecordServices()
									.getDocumentById((String) objects[i]);
							objects[i] = getMetadataValue(referencedRecord, refMetadata);
						} else if (objects[i] instanceof List) {
							List<Object> values = new ArrayList<>();
							for (Object ref : (List) objects[i]) {
								Record referencedRecord = ConstellioFactories.getInstance().getModelLayerFactory()
										.newRecordServices()
										.getDocumentById((String) ref);
								values.add(getMetadataValue(referencedRecord, refMetadata));
							}
							objects[i] = values;

						} else {
							throw new RuntimeException("Invalid value : " + objects[i].getClass().getSimpleName());
						}
					}
				}
			} else {
				throw new RuntimeException("Unsupported object of class '" + actual.getClass());
			}
			return assertThat(Arrays.asList(objects));
		}

	}

	public static List<Tuple> extractingSimpleCodeAndParameters(ValidationRuntimeException e, String... parameters) {
		return extractingSimpleCodeAndParameters(e.getValidationErrors(), parameters);
	}

	public static List<Tuple> extractingSimpleCodeAndParameters(ValidationException e, String... parameters) {
		return extractingSimpleCodeAndParameters(e.getErrors(), parameters);
	}

	public static List<Tuple> extractingSimpleCodeAndParameters(
			com.constellio.model.frameworks.validation.ValidationException e,
			String... parameters) {
		return extractingSimpleCodeAndParameters(e.getValidationErrors(), parameters);
	}

	public static List<String> extractingSimpleCode(ValidationErrors errors) {

		List<String> codes = new ArrayList<>();
		for (ValidationError error : errors.getValidationErrors()) {
			codes.add(StringUtils.substringAfterLast(error.getCode(), "."));
		}

		return codes;
	}

	public static List<Tuple> extractingWarningsSimpleCodeAndParameters(
			com.constellio.model.frameworks.validation.ValidationException e, String... parameters) {
		return extractingWarningsSimpleCodeAndParameters(e.getValidationErrors(), parameters);
	}

	public static List<Tuple> extractingWarningsSimpleCodeAndParameters(
			com.constellio.model.frameworks.validation.ValidationRuntimeException e, String... parameters) {
		return extractingWarningsSimpleCodeAndParameters(e.getValidationErrors(), parameters);
	}

	public static List<Tuple> extractingWarningsSimpleCodeAndParameters(ValidationErrors errors, String... parameters) {

		List<Tuple> tuples = new ArrayList<>();
		for (ValidationError error : errors.getValidationWarnings()) {
			Tuple tuple = new Tuple(StringUtils.substringAfterLast(error.getCode(), "."));
			for (String parameter : parameters) {
				tuple.addData(error.getParameters().get(parameter));
			}
			tuples.add(tuple);
		}

		return tuples;
	}

	public static List<Tuple> extractingSimpleCodeAndParameters(ValidationErrors errors, String... parameters) {

		List<Tuple> tuples = new ArrayList<>();
		for (ValidationError error : errors.getValidationErrors()) {
			Tuple tuple = new Tuple(StringUtils.substringAfterLast(error.getCode(), "."));
			for (String parameter : parameters) {

				tuple.addData(error.getParameters().get(parameter));
			}
			tuples.add(tuple);
		}

		return tuples;
	}

	public static List<String> frenchMessages(ValidationRuntimeException e) {
		return frenchMessages(e.getValidationErrors());
	}

	public static List<String> frenchMessages(ValidationException e) {
		return frenchMessages(e.getErrors());

	}

	public static List<String> frenchMessages(com.constellio.model.frameworks.validation.ValidationException e) {
		return frenchMessages(e.getValidationErrors());

	}

	public static List<String> frenchMessages(List<ValidationError> errors) {
		List<String> messages = new ArrayList<>();

		Locale originalLocale = i18n.getLocale();
		i18n.setLocale(Locale.FRENCH);

		for (ValidationError error : errors) {
			messages.add($(error));
		}

		i18n.setLocale(originalLocale);

		return messages;
	}

	public static List<String> englishMessages(ValidationRuntimeException e) {
		return englishMessages(e.getValidationErrors());
	}

	public static List<String> englishMessages(ValidationException e) {
		return englishMessages(e.getErrors());

	}

	public static List<String> englishMessages(com.constellio.model.frameworks.validation.ValidationException e) {
		return englishMessages(e.getValidationErrors());

	}

	public static List<String> englishMessages(List<ValidationError> errors) {
		List<String> messages = new ArrayList<>();

		Locale originalLocale = i18n.getLocale();
		i18n.setLocale(Locale.ENGLISH);

		for (ValidationError error : errors) {
			messages.add($(error));
		}

		i18n.setLocale(originalLocale);

		return messages;
	}

	public static List<String> frenchMessages(ValidationErrors errors) {
		List<String> messages = new ArrayList<>();

		Locale originalLocale = i18n.getLocale();
		i18n.setLocale(Locale.FRENCH);

		for (ValidationError error : errors.getValidationErrors()) {
			messages.add($(error));
		}

		for (ValidationError error : errors.getValidationWarnings()) {
			messages.add($(error));
		}

		i18n.setLocale(originalLocale);

		return messages;
	}

	public static String frenchMessage(String key, Map<String, Object> args) {
		Locale originalLocale = i18n.getLocale();
		i18n.setLocale(Locale.FRENCH);
		String value = $(key, args);
		i18n.setLocale(originalLocale);
		return value;
	}

	public static String englishMessage(String key, Map<String, Object> args) {
		Locale originalLocale = i18n.getLocale();
		i18n.setLocale(Locale.ENGLISH);
		String value = $(key, args);
		i18n.setLocale(originalLocale);
		return value;
	}

	public static List<String> englishMessages(ValidationErrors errors) {
		List<String> messages = new ArrayList<>();

		Locale originalLocale = i18n.getLocale();
		i18n.setLocale(Locale.ENGLISH);

		for (ValidationError error : errors.getValidationErrors()) {
			messages.add($(error));
		}

		i18n.setLocale(originalLocale);

		return messages;
	}

	public static RecordsAssert assertThatAllRecordsOf(MetadataSchemaType type) {
		SearchServices searchServices = ConstellioFactories.getInstance().getModelLayerFactory().newSearchServices();
		return assertThatRecords(searchServices.search(query(from(type).returnAll())));
	}

	public static RecordsAssert assertThatAllRecordsOf(SchemaShortcuts schemaShortcuts) {
		SearchServices searchServices = ConstellioFactories.getInstance().getModelLayerFactory().newSearchServices();
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaShortcuts.code());
		MetadataSchemaType type = ConstellioFactories.getInstance().getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(schemaShortcuts.collection()).getSchemaType(schemaTypeCode);

		return assertThatRecords(searchServices.search(query(from(type).returnAll())));
	}

	public static void assumeWindows() {
		org.junit.Assume.assumeTrue(System.getProperty("os.name").startsWith("Windows"));
	}

	public static List<String> asOrderedList(String... values) {

		List<String> list = new ArrayList<>(asList(values));
		Collections.sort(list);

		return list;
	}

	public static List<Tuple> getNetworkLinksOf(String collection) {

		List<Tuple> tuples = new ArrayList();

		SchemasRecordsServices schemas = new SchemasRecordsServices(collection,
				ConstellioTest.getInstance().getModelLayerFactory());
		for (MetadataNetworkLink link : schemas.getTypes().getMetadataNetwork().getLinks()) {

			if (!link.getToMetadata().isGlobal()
				&& !link.getFromMetadata().isGlobal()
				&& !link.getFromMetadata().getCode().startsWith("savedSearch_")
				&& !link.getFromMetadata().getCode().startsWith("user_")
				&& !link.getFromMetadata().getCode().startsWith("userDocument_")
				&& !link.getFromMetadata().getCode().startsWith("user_")
				&& !link.getFromMetadata().getCode().startsWith("temporaryRecord_")) {
				Tuple tuple = new Tuple(link.getFromMetadata().getCode(), link.getToMetadata().getCode(), link.getLevel());
				tuples.add(tuple);
			}

		}

		return tuples;
	}

	public static void linkEventBus(ModelLayerFactory modelLayerFactory1, ModelLayerFactory modelLayerFactory2) {
		linkEventBus(modelLayerFactory1.getDataLayerFactory(), modelLayerFactory2.getDataLayerFactory(), 0);
	}

	public static void linkEventBus(ModelLayerFactory modelLayerFactory1, ModelLayerFactory modelLayerFactory2,
									int delayInSeconds) {
		linkEventBus(modelLayerFactory1.getDataLayerFactory(), modelLayerFactory2.getDataLayerFactory(), delayInSeconds);
	}

	public static SDKEventBusSendingService linkEventBus(DataLayerFactory dataLayerFactory1,
														 DataLayerFactory dataLayerFactory2) {
		return linkEventBus(dataLayerFactory1, dataLayerFactory2, 0);
	}

	public static SDKEventBusSendingService linkEventBus(DataLayerFactory dataLayerFactory1,
														 DataLayerFactory dataLayerFactory2,
														 int delayInSeconds) {
		EventBusManager eventBusManager1 = dataLayerFactory1.getEventBusManager();
		EventBusManager eventBusManager2 = dataLayerFactory2.getEventBusManager();
		assertThat(eventBusManager1).isNotSameAs(eventBusManager2);

		SDKEventBusSendingService sendingService1 = delayInSeconds > 0 ?
													new SDKEventBusSendingDelayedService(delayInSeconds) :
													new SDKEventBusSendingService();
		SDKEventBusSendingService sendingService2 = delayInSeconds > 0 ?
													new SDKEventBusSendingDelayedService(delayInSeconds) :
													new SDKEventBusSendingService();

		eventBusManager1.setEventBusSendingService(sendingService1);
		eventBusManager2.setEventBusSendingService(sendingService2);
		SDKEventBusSendingService.interconnect(sendingService1, sendingService2);

		return sendingService1;
	}

	public static <T> ListAssert<T> assertThatStream(Stream<T> stream) {
		return assertThat(stream.collect(toList()));
	}

	public static double calculateOpsPerSecondsOver(Runnable op, int msToBench) {
		long loopCount = 0;
		long start = new Date().getTime();
		long end = start;
		while ((start + msToBench > end)) {
			op.run();
			loopCount++;
			end = new Date().getTime();
		}

		long elapsedMs = end - start;
		return ((double) (1000 * loopCount)) / elapsedMs;
	}
}
