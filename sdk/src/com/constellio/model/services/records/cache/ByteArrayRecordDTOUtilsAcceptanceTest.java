package com.constellio.model.services.records.cache;

import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO.ByteArrayRecordDTOWithIntegerId;
import com.constellio.model.services.records.cache.ByteArrayRecordDTO.ByteArrayRecordDTOWithStringId;
import com.constellio.model.services.records.cache.CacheRecordDTOUtils.CacheRecordDTOBytesArray;
import com.constellio.model.services.records.cache.CacheRecordDTOUtils.CompactedInt;
import com.constellio.model.services.records.cache.CacheRecordDTOUtils.CompactedShort;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemaProvider;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import static com.constellio.data.dao.dto.records.RecordDTOMode.SUMMARY;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.INTEGER;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static com.constellio.model.services.records.cache.CacheRecordDTOUtils.convertDTOToByteArrays;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichAllowsZeSchemaType;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEssentialInSummary;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.mock;

//@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ByteArrayRecordDTOUtilsAcceptanceTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	TestsSchemasSetup.ZeSchemaMetadatas zeSchema = setup.new ZeSchemaMetadatas();
	TestsSchemasSetup.AnotherSchemaMetadatas anotherSchema = setup.new AnotherSchemaMetadatas();

	String mode;
	static String persited = "persisted";
	static String memory = "memory";


	User john;

	MetadataSchemasManager schemasManager;
	RecordServices recordServices;
	ReindexingServices reindexingServices;
	ContentManager contentManager;
	Random random = new Random();

	//	@Parameterized.Parameters(name = "{0}")
	//	public static Collection<Object[]> testCases() {
	//		return Arrays.asList(new Object[][]{{memory}, {persited}});
	//	}

	//	public ByteArrayRecordDTOUtilsAcceptanceTest(String mode) {
	//		this.mode = mode;
	//	}
	//
	//	@Before
	//	public void setUp() throws Exception {
	//		CacheRecordDTOUtils.SAVE_INT_DATE_METADATAS_IN_MEMORY = mode.equals(memory);
	//	}
	//
	//	@After
	//	public void tearDown() throws Exception {
	//		CacheRecordDTOUtils.SAVE_INT_DATE_METADATAS_IN_MEMORY = true;
	//	}

	private void init() {
		UserServices userServices = getModelLayerFactory().newUserServices();
		UserCredential adminCredential = userServices.createUserCredential("mrlabatt50", "John", "Labatt", "ti-joe@brewerie.com",
				new ArrayList<String>(), asList(zeCollection), UserCredentialStatus.ACTIVE).setSystemAdminEnabled();
		userServices.addUpdateUserCredential(adminCredential);
		userServices.addUserToCollection(adminCredential, zeCollection);
		john = userServices.getUserRecordInCollection("mrlabatt50", zeCollection);

		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		recordServices = getModelLayerFactory().newRecordServices();
		reindexingServices = getModelLayerFactory().newReindexingServices();
		contentManager = getModelLayerFactory().getContentManager();

	}

	@Test
	public void whenStoringSingleValueMetadatasInAByteArrayRecordDTOThenStoredAndRetrieved() throws Exception {
		defineSchemasManager().using(setup
				.withATitle(whichIsEssentialInSummary)
				.withABooleanMetadata(whichIsEssentialInSummary)
				.withAnIntegerMetadata(whichIsEssentialInSummary)
				.withANumberMetadata(whichIsEssentialInSummary)
				.withAnEnumMetadata(FolderStatus.class, whichIsEssentialInSummary)
				.withAParentReferenceFromZeSchemaToZeSchema(whichIsEssentialInSummary)
				.withAReferenceFromAnotherSchemaToZeSchema(whichIsEssentialInSummary)
				.withADateMetadata(whichIsEssentialInSummary)
				.withAContentMetadata(whichIsEssentialInSummary)
				.withALargeTextMetadata(whichIsEssentialInSummary)
				//				.withAStructureMetadata()
				.withADateTimeMetadata(whichIsEssentialInSummary));

		init();

		setup.modify((MetadataSchemaTypesAlteration) types -> {
			types.getSchema(anotherSchema.code())
					.create("booleanMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.BOOLEAN);
			types.getSchema(anotherSchema.code())
					.create("integerMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.INTEGER);
			types.getSchema(anotherSchema.code())
					.create("numberMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.NUMBER);
			types.getSchema(anotherSchema.code())
					.create("enumMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.ENUM)
					.defineAsEnum(CopyType.class);
			types.getSchema(anotherSchema.code())
					.create("dateMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.DATE);
			types.getSchema(anotherSchema.code())
					.create("dateTimeMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.DATE_TIME);
			types.getSchema(anotherSchema.code())
					.create("textMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.TEXT);
			types.getSchema(anotherSchema.code())
					.create("contentMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.CONTENT);
			types.getSchema(anotherSchema.code())
					.create("structureMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.STRUCTURE)
					.defineStructureFactory(CommentFactory.class);

			types.getSchema(zeSchema.code())
					.create("structMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.STRUCTURE)
					.defineStructureFactory(CommentFactory.class);
		});

		LocalDate date = new LocalDate();
		LocalDateTime dateTime = new LocalDateTime();

		File file = newTempFileWithContent("content.txt", "This is what a content file of type content contains. Content ?");
		ContentVersionDataSummary versionDataSummary = contentManager.upload(file);
		Content content = contentManager.createSystemContent(file.getName(), versionDataSummary);

		Comment comment = new Comment("Well hello there !", john, dateTime);
		String commentToString = new CommentFactory().toString(comment);

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());
		MetadataSchema zeSchemaType = schemaTypes.getSchema(zeSchema.code());

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(Schemas.TITLE, "Le village des Schtroumpfs")
				.set(zeSchema.booleanMetadata(), true)
				.set(zeSchema.integerMetadata(), 14)
				.set(zeSchema.numberMetadata(), -70.4d)
				.set(zeSchema.enumMetadata(), FolderStatus.ACTIVE)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), null)
				.set(zeSchema.dateMetadata(), date)
				.set(zeSchema.largeTextMetadata(), "Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
												   "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
												   "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
												   "nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in " +
												   "reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla " +
												   "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
												   "culpa qui officia deserunt mollit anim id est laborum.")
				.set(zeSchema.contentMetadata(), content)
				.set(zeSchemaType.getMetadata("structMetadata"), comment)
				.set(zeSchema.dateTimeMetadata(), dateTime);

		RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "recordMondial")
				.set(Schemas.TITLE, "")
				.set(zeSchema.integerMetadata(), 0)
				.set(zeSchema.booleanMetadata(), false)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record1.getId())
				.set(zeSchema.dateTimeMetadata(), dateTime.minusWeeks(13))
				.set(zeSchema.numberMetadata(), 0d)
				.set(zeSchema.dateMetadata(), date.plusMonths(2))
				.set(zeSchema.largeTextMetadata(), "")
				.set(zeSchemaType.getMetadata("structMetadata"), comment)
				.set(zeSchema.contentMetadata(), null)
				.set(zeSchema.enumMetadata(), FolderStatus.SEMI_ACTIVE);

		RecordImpl record3 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), "recordMondial")
				.set(zeSchema.booleanMetadata(), null)
				.set(zeSchema.integerMetadata(), -420)
				.set(zeSchema.dateMetadata(), date.minusYears(60))
				.set(zeSchema.numberMetadata(), 1337.12d)
				.set(zeSchema.enumMetadata(), null)
				.set(Schemas.TITLE, " Blue litte man ")
				.set(zeSchema.largeTextMetadata(), " ")
				.set(zeSchemaType.getMetadata("structMetadata"), null)
				.set(zeSchema.dateTimeMetadata(), dateTime.plusDays(300));

		RecordImpl record4 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "Gargamel")
				.set(zeSchema.dateMetadata(), null)
				.set(zeSchema.booleanMetadata(), true)
				.set(zeSchema.integerMetadata(), null)
				.set(zeSchema.numberMetadata(), null)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record3.getId())
				.set(Schemas.TITLE, "أريد أن أشرب الحليب")
				.set(zeSchemaType.getMetadata("structMetadata"), null)
				.set(zeSchema.enumMetadata(), FolderStatus.INACTIVE_DESTROYED)
				.set(zeSchema.largeTextMetadata(), null)
				.set(zeSchema.contentMetadata(), content)
				.set(zeSchema.dateTimeMetadata(), null);

		RecordImpl record5 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
				.set(Schemas.TITLE, "I'm blue da ba de da ba da")
				.set(anotherSchemaType.getMetadata("enumMetadata"), null)
				.set(anotherSchemaType.getMetadata("booleanMetadata"), true)
				.set(anotherSchemaType.getMetadata("dateMetadata"), date.plusYears(5).minusDays(2))
				.set(anotherSchemaType.getMetadata("numberMetadata"), -100.2d)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), record4.getId())
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), null)
				.set(anotherSchemaType.getMetadata("textMetadata"), " ")
				.set(anotherSchemaType.getMetadata("contentMetadata"), null)
				.set(anotherSchemaType.getMetadata("structureMetadata"), comment)
				.set(anotherSchemaType.getMetadata("integerMetadata"), null);

		RecordImpl record6 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Azrael")
				.set(Schemas.TITLE, " Nom d'un Schtroumpfs!")
				.set(anotherSchemaType.getMetadata("booleanMetadata"), null)
				.set(anotherSchemaType.getMetadata("numberMetadata"), null)
				.set(anotherSchemaType.getMetadata("enumMetadata"), CopyType.PRINCIPAL)
				.set(anotherSchemaType.getMetadata("dateMetadata"), date)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), null)
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), dateTime)
				.set(anotherSchemaType.getMetadata("textMetadata"), "")
				.set(anotherSchemaType.getMetadata("contentMetadata"), content)
				.set(anotherSchemaType.getMetadata("structureMetadata"), null)
				.set(anotherSchemaType.getMetadata("integerMetadata"), 0);

		RecordImpl record7 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "GrandSchtroumpfs")
				.set(Schemas.TITLE, "")
				.set(anotherSchemaType.getMetadata("integerMetadata"), -99)
				.set(anotherSchemaType.getMetadata("numberMetadata"), 0d)
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), dateTime.plusHours(600).minusWeeks(1))
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), "Gargamel")
				.set(anotherSchemaType.getMetadata("enumMetadata"), CopyType.SECONDARY)
				.set(anotherSchemaType.getMetadata("dateMetadata"), null)
				.set(anotherSchemaType.getMetadata("textMetadata"), "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
																	"nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in " +
																	"reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla " +
																	"pariatur.")
				.set(anotherSchemaType.getMetadata("contentMetadata"), content)
				.set(anotherSchemaType.getMetadata("structureMetadata"), null)
				.set(anotherSchemaType.getMetadata("booleanMetadata"), false);

		RecordImpl record8 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "SchtroumpfsGrognon")
				.set(anotherSchemaType.getMetadata("integerMetadata"), 1234)
				.set(anotherSchemaType.getMetadata("dateMetadata"), date.plusMonths(14))
				.set(anotherSchemaType.getMetadata("numberMetadata"), 10.88d)
				.set(anotherSchemaType.getMetadata("booleanMetadata"), true)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), "Gargamel")
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), dateTime.minusYears(21))
				.set(anotherSchemaType.getMetadata("enumMetadata"), CopyType.SECONDARY)
				.set(anotherSchemaType.getMetadata("textMetadata"), null)
				.set(anotherSchemaType.getMetadata("structureMetadata"), comment)
				.set(Schemas.TITLE, null);

		recordServices.execute(new Transaction(record1, record2, record3, record4, record5, record6, record7, record8));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());
		ByteArrayRecordDTO dto2 = create(getModelLayerFactory(), record2.getRecordDTO());
		ByteArrayRecordDTO dto3 = create(getModelLayerFactory(), record3.getRecordDTO());
		ByteArrayRecordDTO dto4 = create(getModelLayerFactory(), record4.getRecordDTO());
		ByteArrayRecordDTO dto5 = create(getModelLayerFactory(), record5.getRecordDTO());
		ByteArrayRecordDTO dto6 = create(getModelLayerFactory(), record6.getRecordDTO());
		ByteArrayRecordDTO dto7 = create(getModelLayerFactory(), record7.getRecordDTO());
		ByteArrayRecordDTO dto8 = create(getModelLayerFactory(), record8.getRecordDTO());

		assertThat(dto1.get(Schemas.TITLE.getDataStoreCode())).isEqualTo("Le village des Schtroumpfs");
		assertThat(dto1.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(true);
		assertThat(dto1.get(zeSchema.integerMetadata().getDataStoreCode())).isEqualTo(14);
		assertThat(dto1.get(zeSchema.numberMetadata().getDataStoreCode())).isEqualTo(-70.4d);
		assertThat(dto1.get(zeSchema.enumMetadata().getDataStoreCode())).isEqualTo(FolderStatus.ACTIVE.getCode());
		assertThat(dto1.get(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isEqualTo(null);
		assertThat(dto1.get(zeSchema.dateMetadata().getDataStoreCode())).isEqualTo(date);
		assertThat(dto1.get(zeSchema.largeTextMetadata().getDataStoreCode())).isEqualTo("Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
																						"sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
																						"Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
																						"nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in " +
																						"reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla " +
																						"pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
																						"culpa qui officia deserunt mollit anim id est laborum.");
		assertThat(dto1.get(zeSchema.dateTimeMetadata().getDataStoreCode())).isEqualTo(dateTime);
		assertThat(dto1.get(zeSchema.contentMetadata().getDataStoreCode()))
				.isEqualTo(record1.getRecordDTO().getFields().get(zeSchema.contentMetadata().getDataStoreCode()));
		assertThat(dto1.get(zeSchemaType.getMetadata("structMetadata").getDataStoreCode())).isEqualTo(commentToString);

		assertThat(dto2.get(Schemas.TITLE.getDataStoreCode())).isEqualTo(null);
		assertThat(dto2.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(false);
		assertThat(dto2.get(zeSchema.integerMetadata().getDataStoreCode())).isEqualTo(0);
		assertThat(dto2.get(zeSchema.numberMetadata().getDataStoreCode())).isEqualTo(0d);
		assertThat(dto2.get(zeSchema.enumMetadata().getDataStoreCode())).isEqualTo(FolderStatus.SEMI_ACTIVE.getCode());
		assertThat(dto2.get(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isEqualTo(record1.getId());
		assertThat(dto2.get(zeSchema.dateMetadata().getDataStoreCode())).isEqualTo(date.plusMonths(2));
		assertThat(dto2.get(zeSchema.dateTimeMetadata().getDataStoreCode())).isEqualTo(dateTime.minusWeeks(13));
		assertThat(dto2.get(zeSchema.largeTextMetadata().getDataStoreCode())).isEqualTo(null);
		assertThat(dto2.get(zeSchema.contentMetadata().getDataStoreCode())).isEqualTo(null);
		assertThat(dto2.get(zeSchemaType.getMetadata("structMetadata").getDataStoreCode())).isEqualTo(commentToString);

		assertThat(dto3.get(Schemas.TITLE.getDataStoreCode())).isEqualTo(" Blue litte man ");
		assertThat(dto3.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(null);
		assertThat(dto3.get(zeSchema.integerMetadata().getDataStoreCode())).isEqualTo(-420);
		assertThat(dto3.get(zeSchema.numberMetadata().getDataStoreCode())).isEqualTo(1337.12d);
		assertThat(dto3.get(zeSchema.enumMetadata().getDataStoreCode())).isEqualTo(null);
		assertThat(dto3.get(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isEqualTo("recordMondial");
		assertThat(dto3.get(zeSchema.dateMetadata().getDataStoreCode())).isEqualTo(date.minusYears(60));
		assertThat(dto3.get(zeSchema.dateTimeMetadata().getDataStoreCode())).isEqualTo(dateTime.plusDays(300));
		assertThat(dto3.get(zeSchema.largeTextMetadata().getDataStoreCode())).isEqualTo(" ");
		assertThat(dto3.get(zeSchema.contentMetadata().getDataStoreCode())).isEqualTo(null);
		assertThat(dto3.get(zeSchemaType.getMetadata("structMetadata").getDataStoreCode())).isEqualTo(null);

		assertThat(dto4.get(Schemas.TITLE.getDataStoreCode())).isEqualTo("أريد أن أشرب الحليب");
		assertThat(dto4.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(true);
		assertThat(dto4.get(zeSchema.integerMetadata().getDataStoreCode())).isEqualTo(null);
		assertThat(dto4.get(zeSchema.numberMetadata().getDataStoreCode())).isEqualTo(null);
		assertThat(dto4.get(zeSchema.enumMetadata().getDataStoreCode())).isEqualTo(FolderStatus.INACTIVE_DESTROYED.getCode());
		assertThat(dto4.get(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isEqualTo(record3.getId());
		assertThat(dto4.get(zeSchema.dateMetadata().getDataStoreCode())).isEqualTo(null);
		assertThat(dto4.get(zeSchema.dateTimeMetadata().getDataStoreCode())).isEqualTo(null);
		assertThat(dto4.get(zeSchema.largeTextMetadata().getDataStoreCode())).isEqualTo(null);
		assertThat(dto4.get(zeSchema.contentMetadata().getDataStoreCode()))
				.isEqualTo(record4.getRecordDTO().getFields().get(zeSchema.contentMetadata().getDataStoreCode()));
		assertThat(dto4.get(zeSchemaType.getMetadata("structMetadata").getDataStoreCode())).isEqualTo(null);

		assertThat(dto5.get(Schemas.TITLE.getDataStoreCode())).isEqualTo("I'm blue da ba de da ba da");
		assertThat(dto5.get(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isEqualTo(true);
		assertThat(dto5.get(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isEqualTo(null);
		assertThat(dto5.get(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isEqualTo(-100.2d);
		assertThat(dto5.get(anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode())).isEqualTo(null);
		assertThat(dto5.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo(record4.getId());
		assertThat(dto5.get(anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode())).isEqualTo(date.plusYears(5).minusDays(2));
		assertThat(dto5.get(anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode())).isEqualTo(null);
		assertThat(dto5.get(anotherSchemaType.getMetadata("textMetadata").getDataStoreCode())).isEqualTo(" ");
		assertThat(dto5.get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode())).isEqualTo(null);
		assertThat(dto5.get(anotherSchemaType.getMetadata("structureMetadata").getDataStoreCode())).isEqualTo(commentToString);

		assertThat(dto6.get(Schemas.TITLE.getDataStoreCode())).isEqualTo(" Nom d'un Schtroumpfs!");
		assertThat(dto6.get(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isEqualTo(null);
		assertThat(dto6.get(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isEqualTo(0);
		assertThat(dto6.get(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isEqualTo(null);
		assertThat(dto6.get(anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode())).isEqualTo(CopyType.PRINCIPAL.getCode());
		assertThat(dto6.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo(null);
		assertThat(dto6.get(anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode())).isEqualTo(date);
		assertThat(dto6.get(anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode())).isEqualTo(dateTime);
		assertThat(dto6.get(anotherSchemaType.getMetadata("textMetadata").getDataStoreCode())).isEqualTo(null);
		assertThat(dto6.get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode()))
				.isEqualTo(record6.getRecordDTO().getFields().get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode()));
		assertThat(dto6.get(anotherSchemaType.getMetadata("structureMetadata").getDataStoreCode())).isEqualTo(null);

		// if the title is null and the content is not null the title is set to the file name of content
		assertThat(dto7.get(Schemas.TITLE.getDataStoreCode())).isEqualTo(file.getName());
		assertThat(dto7.get(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isEqualTo(false);
		assertThat(dto7.get(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isEqualTo(-99);
		assertThat(dto7.get(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isEqualTo(0d);
		assertThat(dto7.get(anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode())).isEqualTo(CopyType.SECONDARY.getCode());
		assertThat(dto7.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo("Gargamel");
		assertThat(dto7.get(anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode())).isEqualTo(null);
		assertThat(dto7.get(anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode())).isEqualTo(dateTime.plusHours(600).minusWeeks(1));
		assertThat(dto7.get(anotherSchemaType.getMetadata("textMetadata").getDataStoreCode())).isEqualTo("Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
																										 "nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in " +
																										 "reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla " +
																										 "pariatur.");
		assertThat(dto7.get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode()))
				.isEqualTo(record7.getRecordDTO().getFields().get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode()));
		assertThat(dto7.get(anotherSchemaType.getMetadata("structureMetadata").getDataStoreCode())).isEqualTo(null);

		assertThat(dto8.get(Schemas.TITLE.getDataStoreCode())).isEqualTo(null);
		assertThat(dto8.get(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isEqualTo(true);
		assertThat(dto8.get(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isEqualTo(1234);
		assertThat(dto8.get(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isEqualTo(10.88d);
		assertThat(dto8.get(anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode())).isEqualTo(CopyType.SECONDARY.getCode());
		assertThat(dto8.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo("Gargamel");
		assertThat(dto8.get(anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode())).isEqualTo(date.plusMonths(14));
		assertThat(dto8.get(anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode())).isEqualTo(dateTime.minusYears(21));
		assertThat(dto8.get(anotherSchemaType.getMetadata("textMetadata").getDataStoreCode())).isEqualTo(null);
		assertThat(dto8.get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode())).isEqualTo(null);
		assertThat(dto8.get(anotherSchemaType.getMetadata("structureMetadata").getDataStoreCode())).isEqualTo(commentToString);
	}

	@Test
	public void whenStoringMultivalueMetadatasInAByteArrayRecordDTOThenStoredAndRetrieved() throws Exception {
		defineSchemasManager().using(setup
				.withABooleanMetadata(whichIsMultivalue, whichIsEssentialInSummary)
				.withAnIntegerMetadata(whichIsMultivalue, whichIsEssentialInSummary)
				.withANumberMetadata(whichIsMultivalue, whichIsEssentialInSummary)
				.withADateMetadata(whichIsMultivalue, whichIsEssentialInSummary)
				.withADateTimeMetadata(whichIsMultivalue, whichIsEssentialInSummary)
				.withAReferenceMetadata(whichAllowsZeSchemaType, whichIsMultivalue, whichIsEssentialInSummary)
				.withAnEnumMetadata(CopyType.class, whichIsMultivalue, whichIsEssentialInSummary)
				.withAMultivaluedLargeTextMetadata(whichIsEssentialInSummary)
				.withAContentMetadata(whichIsMultivalue, whichIsEssentialInSummary)
				.withAReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue, whichIsEssentialInSummary));

		init();

		setup.modify((MetadataSchemaTypesAlteration) types -> {
			types.getSchema(anotherSchema.code())
					.create("booleanMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.BOOLEAN)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("integerMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.INTEGER)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("numberMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.NUMBER)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("enumMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.ENUM)
					.defineAsEnum(CopyType.class)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("dateMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.DATE)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("dateTimeMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.DATE_TIME)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("textMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.TEXT)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("contentMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.CONTENT)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("structureMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.STRUCTURE)
					.defineStructureFactory(CommentFactory.class)
					.setMultivalue(true);

			types.getSchema(zeSchema.code())
					.create("structMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.STRUCTURE)
					.defineStructureFactory(CommentFactory.class)
					.setMultivalue(true);
		});

		LocalDate date = new LocalDate();
		LocalDateTime dateTime = new LocalDateTime();

		File file1 = newTempFileWithContent("content.txt", "This is what a content file of type content contains. Content ?");
		ContentVersionDataSummary versionDataSummary1 = contentManager.upload(file1);
		Content content = contentManager.createSystemContent(file1.getName(), versionDataSummary1);
		File file2 = newTempFileWithContent("no-content.txt", "Nothing to see here, move along");
		ContentVersionDataSummary versionDataSummary2 = contentManager.upload(file2);
		Content noContent = contentManager.createSystemContent(file2.getName(), versionDataSummary2);

		Comment comment1 = new Comment("Hello there !", john, dateTime);
		String comment1ToString = new CommentFactory().toString(comment1);
		Comment comment2 = new Comment("General Kenobi !", john, dateTime.plusDays(23));
		String comment2ToString = new CommentFactory().toString(comment2);

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());
		MetadataSchema zeSchemaType = schemaTypes.getSchema(zeSchema.code());

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.integerMetadata(), asList(0, 13, -40))
				.set(zeSchema.enumMetadata(), asList(CopyType.PRINCIPAL, CopyType.PRINCIPAL, CopyType.SECONDARY))
				.set(zeSchema.dateTimeMetadata(), asList(null, null))
				.set(zeSchema.numberMetadata(), asList(0d, -0d, 0.0d))
				.set(zeSchema.referenceMetadata(), null)
				.set(zeSchema.booleanMetadata(), null)
				.set(zeSchema.multivaluedLargeTextMetadata(), asList("Dance", "Play", "Pretend", "and I like to have fun, fun, fun"))
				.set(zeSchema.contentMetadata(), asList(content, noContent))
				.set(zeSchemaType.getMetadata("structMetadata"), asList(null, null))
				.set(zeSchema.dateMetadata(), asList(date.minusYears(22).plusMonths(2).plusDays(10), null));

		RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "StoneColdSteveAustin")
				.set(zeSchema.integerMetadata(), asList(619, 33, -244))
				.set(zeSchema.enumMetadata(), asList(CopyType.PRINCIPAL, CopyType.PRINCIPAL, CopyType.SECONDARY))
				.set(zeSchema.numberMetadata(), null)
				.set(zeSchema.referenceMetadata(), asList(null, record1.getId()))
				.set(zeSchema.dateMetadata(), asList(date, date.plusDays(3), date.plusDays(-100)))
				.set(zeSchema.dateTimeMetadata(), asList(null, dateTime.plusHours(5), dateTime.minusYears(33)))
				.set(zeSchemaType.getMetadata("structMetadata"), asList(comment2, comment1))
				.set(zeSchema.multivaluedLargeTextMetadata(), asList("", "Luigi", null, "Waluigi", "Wario"))
				.set(zeSchema.contentMetadata(), asList(noContent, content))
				.set(zeSchema.booleanMetadata(), asList(true, false, true));

		RecordImpl record3 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "Undertaker")
				.set(zeSchema.numberMetadata(), asList(420.69d, -40d, 0.0d))
				.set(zeSchema.integerMetadata(), asList(42, -13, 10))
				.set(zeSchema.enumMetadata(), asList(CopyType.PRINCIPAL, CopyType.PRINCIPAL, CopyType.SECONDARY))
				.set(zeSchema.dateMetadata(), null)
				.set(zeSchemaType.getMetadata("structMetadata"), asList(comment1, comment2))
				.set(zeSchema.referenceMetadata(), asList("StoneColdSteveAustin", record1.getId()))
				.set(zeSchema.booleanMetadata(), asList(false, null, true))
				.set(zeSchema.multivaluedLargeTextMetadata(), asList(null, null))
				.set(zeSchema.contentMetadata(), null)
				.set(zeSchema.dateTimeMetadata(), asList(dateTime, dateTime.plusHours(22), dateTime.minusYears(100)));

		RecordImpl record4 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.integerMetadata(), null)
				.set(zeSchema.numberMetadata(), asList(1.337d, -6.19d, 3.1416d))
				.set(zeSchema.booleanMetadata(), asList(null, null, null))
				.set(zeSchema.dateMetadata(), asList(null, null, null))
				.set(zeSchema.referenceMetadata(), asList("Undertaker", null))
				.set(zeSchema.enumMetadata(), asList(CopyType.PRINCIPAL, CopyType.PRINCIPAL, CopyType.SECONDARY))
				.set(zeSchema.multivaluedLargeTextMetadata(), asList(" ", "Kawabonga", null, "Yiihi"))
				.set(zeSchema.contentMetadata(), asList(content))
				.set(zeSchemaType.getMetadata("structMetadata"), asList(null, comment1))
				.set(zeSchema.dateTimeMetadata(), null);

		RecordImpl record5 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Tintin")
				.set(anotherSchemaType.getMetadata("dateMetadata"), asList(date.minusWeeks(7), date))
				.set(anotherSchemaType.getMetadata("numberMetadata"), asList(0d))
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), null)
				.set(anotherSchemaType.getMetadata("enumMetadata"), asList(CopyType.SECONDARY, CopyType.SECONDARY, CopyType.SECONDARY))
				.set(anotherSchemaType.getMetadata("booleanMetadata"), asList(true, false, true))
				.set(anotherSchemaType.getMetadata("integerMetadata"), null)
				.set(anotherSchemaType.getMetadata("textMetadata"), null)
				.set(anotherSchemaType.getMetadata("contentMetadata"), asList(content))
				.set(anotherSchemaType.getMetadata("structureMetadata"), null)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(null, null, null, null));

		RecordImpl record6 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Milou")
				.set(anotherSchemaType.getMetadata("booleanMetadata"), asList(null, true, false))
				.set(anotherSchemaType.getMetadata("numberMetadata"), asList(4600d))
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(record4.getId(), "Undertaker"))
				.set(anotherSchemaType.getMetadata("enumMetadata"), null)
				.set(anotherSchemaType.getMetadata("dateMetadata"), asList(null, null, null))
				.set(anotherSchemaType.getMetadata("structureMetadata"), asList(comment1, comment2))
				.set(anotherSchemaType.getMetadata("integerMetadata"), asList(1, 2, 3))
				.set(anotherSchemaType.getMetadata("textMetadata"), asList(null, null))
				.set(anotherSchemaType.getMetadata("contentMetadata"), asList(noContent))
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), asList(dateTime));

		RecordImpl record7 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "CapitaineHaddock")
				.set(anotherSchemaType.getMetadata("integerMetadata"), asList(0, -700))
				.set(anotherSchemaType.getMetadata("dateMetadata"), null)
				.set(anotherSchemaType.getMetadata("numberMetadata"), asList(456d, -123d))
				.set(anotherSchemaType.getMetadata("booleanMetadata"), asList(null, null))
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), asList(null, dateTime, dateTime.plusHours(4)))
				.set(anotherSchemaType.getMetadata("enumMetadata"), asList(CopyType.SECONDARY, CopyType.PRINCIPAL))
				.set(anotherSchemaType.getMetadata("textMetadata"), asList("A", "B", "C"))
				.set(anotherSchemaType.getMetadata("structureMetadata"), asList(comment2))
				.set(anotherSchemaType.getMetadata("contentMetadata"), asList(noContent, content))
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), null);

		RecordImpl record8 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "ProfesseurTournesol")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("StoneColdSteveAustin", record4.getId(), record1.getId(), "Undertaker"))
				.set(anotherSchemaType.getMetadata("integerMetadata"), asList(6701))
				.set(anotherSchemaType.getMetadata("booleanMetadata"), null)
				.set(anotherSchemaType.getMetadata("enumMetadata"), asList(CopyType.SECONDARY))
				.set(anotherSchemaType.getMetadata("dateMetadata"), asList(date, date.plusWeeks(52)))
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), asList(null, null, null))
				.set(anotherSchemaType.getMetadata("textMetadata"), asList("", " ", " Batman "))
				.set(anotherSchemaType.getMetadata("contentMetadata"), null)
				.set(anotherSchemaType.getMetadata("structureMetadata"), null)
				.set(anotherSchemaType.getMetadata("numberMetadata"), null);

		recordServices.execute(new Transaction(record1, record2, record3, record4, record5, record6, record7, record8));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());
		ByteArrayRecordDTO dto2 = create(getModelLayerFactory(), record2.getRecordDTO());
		ByteArrayRecordDTO dto3 = create(getModelLayerFactory(), record3.getRecordDTO());
		ByteArrayRecordDTO dto4 = create(getModelLayerFactory(), record4.getRecordDTO());
		ByteArrayRecordDTO dto5 = create(getModelLayerFactory(), record5.getRecordDTO());
		ByteArrayRecordDTO dto6 = create(getModelLayerFactory(), record6.getRecordDTO());
		ByteArrayRecordDTO dto7 = create(getModelLayerFactory(), record7.getRecordDTO());
		ByteArrayRecordDTO dto8 = create(getModelLayerFactory(), record8.getRecordDTO());

		assertThat(dto1.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(asList());
		assertThat(dto1.get(zeSchema.integerMetadata().getDataStoreCode())).isEqualTo(asList(0, 13, -40));
		assertThat(dto1.get(zeSchema.numberMetadata().getDataStoreCode())).isEqualTo(asList(0d, -0d, 0.0d));
		assertThat(dto1.get(zeSchema.dateMetadata().getDataStoreCode())).isEqualTo(asList(date.minusYears(22).plusMonths(2).plusDays(10), null));
		assertThat(dto1.get(zeSchema.dateTimeMetadata().getDataStoreCode())).isEqualTo(asList());
		assertThat(dto1.get(zeSchema.referenceMetadata().getDataStoreCode())).isEqualTo(asList());
		assertThat(dto1.get(zeSchema.enumMetadata().getDataStoreCode())).isEqualTo(asList(CopyType.PRINCIPAL.getCode(), CopyType.PRINCIPAL.getCode(), CopyType.SECONDARY.getCode()));
		assertThat(dto1.get(zeSchema.multivaluedLargeTextMetadata().getDataStoreCode())).isEqualTo(asList("Dance", "Play", "Pretend", "and I like to have fun, fun, fun"));
		assertThat(dto1.get(zeSchema.contentMetadata().getDataStoreCode()))
				.isEqualTo(record1.getRecordDTO().getFields().get(zeSchema.contentMetadata().getDataStoreCode()));
		assertThat(dto1.get(zeSchemaType.getMetadata("structMetadata").getDataStoreCode())).isEqualTo(asList());

		assertThat(dto2.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(asList(true, false, true));
		assertThat(dto2.get(zeSchema.integerMetadata().getDataStoreCode())).isEqualTo(asList(619, 33, -244));
		assertThat(dto2.get(zeSchema.numberMetadata().getDataStoreCode())).isEqualTo(asList());
		assertThat(dto2.get(zeSchema.dateMetadata().getDataStoreCode())).isEqualTo(asList(date, date.plusDays(3), date.plusDays(-100)));
		assertThat(dto2.get(zeSchema.dateTimeMetadata().getDataStoreCode())).isEqualTo(asList(null, dateTime.plusHours(5), dateTime.minusYears(33)));
		assertThat(dto2.get(zeSchema.referenceMetadata().getDataStoreCode())).isEqualTo(asList(null, record1.getId()));
		assertThat(dto2.get(zeSchema.enumMetadata().getDataStoreCode())).isEqualTo(asList(CopyType.PRINCIPAL.getCode(), CopyType.PRINCIPAL.getCode(), CopyType.SECONDARY.getCode()));
		assertThat(dto2.get(zeSchema.multivaluedLargeTextMetadata().getDataStoreCode())).isEqualTo(asList("Luigi", null, "Waluigi", "Wario"));
		assertThat(dto2.get(zeSchema.contentMetadata().getDataStoreCode()))
				.isEqualTo(record2.getRecordDTO().getFields().get(zeSchema.contentMetadata().getDataStoreCode()));
		assertThat(dto2.get(zeSchemaType.getMetadata("structMetadata").getDataStoreCode())).isEqualTo(asList(comment2ToString, comment1ToString));

		assertThat(dto3.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(asList(false, null, true));
		assertThat(dto3.get(zeSchema.integerMetadata().getDataStoreCode())).isEqualTo(asList(42, -13, 10));
		assertThat(dto3.get(zeSchema.numberMetadata().getDataStoreCode())).isEqualTo(asList(420.69d, -40d, 0.0d));
		assertThat(dto3.get(zeSchema.dateMetadata().getDataStoreCode())).isEqualTo(asList());
		assertThat(dto3.get(zeSchema.dateTimeMetadata().getDataStoreCode())).isEqualTo(asList(dateTime, dateTime.plusHours(22), dateTime.minusYears(100)));
		assertThat(dto3.get(zeSchema.referenceMetadata().getDataStoreCode())).isEqualTo(asList("StoneColdSteveAustin", record1.getId()));
		assertThat(dto3.get(zeSchema.enumMetadata().getDataStoreCode())).isEqualTo(asList(CopyType.PRINCIPAL.getCode(), CopyType.PRINCIPAL.getCode(), CopyType.SECONDARY.getCode()));
		assertThat(dto3.get(zeSchema.multivaluedLargeTextMetadata().getDataStoreCode())).isEqualTo(asList());
		assertThat(dto3.get(zeSchema.contentMetadata().getDataStoreCode())).isEqualTo(asList());
		assertThat(dto3.get(zeSchemaType.getMetadata("structMetadata").getDataStoreCode())).isEqualTo(asList(comment1ToString, comment2ToString));

		assertThat(dto4.get(zeSchema.booleanMetadata().getDataStoreCode())).isEqualTo(asList());
		assertThat(dto4.get(zeSchema.integerMetadata().getDataStoreCode())).isEqualTo(asList());
		assertThat(dto4.get(zeSchema.numberMetadata().getDataStoreCode())).isEqualTo(asList(1.337d, -6.19d, 3.1416d));
		assertThat(dto4.get(zeSchema.dateMetadata().getDataStoreCode())).isEqualTo(asList());
		assertThat(dto4.get(zeSchema.dateTimeMetadata().getDataStoreCode())).isEqualTo(asList());
		assertThat(dto4.get(zeSchema.referenceMetadata().getDataStoreCode())).isEqualTo(asList("Undertaker", null));
		assertThat(dto4.get(zeSchema.enumMetadata().getDataStoreCode())).isEqualTo(asList(CopyType.PRINCIPAL.getCode(), CopyType.PRINCIPAL.getCode(), CopyType.SECONDARY.getCode()));
		assertThat(dto4.get(zeSchema.multivaluedLargeTextMetadata().getDataStoreCode())).isEqualTo(asList(" ", "Kawabonga", null, "Yiihi"));
		assertThat(dto4.get(zeSchema.contentMetadata().getDataStoreCode()))
				.isEqualTo(record4.getRecordDTO().getFields().get(zeSchema.contentMetadata().getDataStoreCode()));
		assertThat(dto4.get(zeSchemaType.getMetadata("structMetadata").getDataStoreCode())).isEqualTo(asList(null, comment1ToString));

		assertThat(dto5.get(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isEqualTo(asList(true, false, true));
		assertThat(dto5.get(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isEqualTo(asList());
		assertThat(dto5.get(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isEqualTo(asList(0d));
		assertThat(dto5.get(anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode())).isEqualTo(asList(date.minusWeeks(7), date));
		assertThat(dto5.get(anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode())).isEqualTo(asList());
		assertThat(dto5.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo(asList(null, null, null, null));
		assertThat(dto5.get(anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode())).isEqualTo(asList(CopyType.SECONDARY.getCode(), CopyType.SECONDARY.getCode(), CopyType.SECONDARY.getCode()));
		assertThat(dto5.get(anotherSchemaType.getMetadata("textMetadata").getDataStoreCode())).isEqualTo(asList());
		assertThat(dto5.get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode()))
				.isEqualTo(record5.getRecordDTO().getFields().get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode()));
		assertThat(dto5.get(anotherSchemaType.getMetadata("structureMetadata").getDataStoreCode())).isEqualTo(asList());

		assertThat(dto6.get(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isEqualTo(asList(null, true, false));
		assertThat(dto6.get(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isEqualTo(asList(1, 2, 3));
		assertThat(dto6.get(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isEqualTo(asList(4600d));
		assertThat(dto6.get(anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode())).isEqualTo(asList());
		assertThat(dto6.get(anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode())).isEqualTo(asList(dateTime));
		assertThat(dto6.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo(asList(record4.getId(), "Undertaker"));
		assertThat(dto6.get(anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode())).isEqualTo(asList());
		assertThat(dto6.get(anotherSchemaType.getMetadata("textMetadata").getDataStoreCode())).isEqualTo(asList());
		assertThat(dto6.get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode()))
				.isEqualTo(record6.getRecordDTO().getFields().get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode()));
		assertThat(dto6.get(anotherSchemaType.getMetadata("structureMetadata").getDataStoreCode())).isEqualTo(asList(comment1ToString, comment2ToString));

		assertThat(dto7.get(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isEqualTo(asList());
		assertThat(dto7.get(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isEqualTo(asList(0, -700));
		assertThat(dto7.get(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isEqualTo(asList(456d, -123d));
		assertThat(dto7.get(anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode())).isEqualTo(asList());
		assertThat(dto7.get(anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode())).isEqualTo(asList(null, dateTime, dateTime.plusHours(4)));
		assertThat(dto7.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo(asList());
		assertThat(dto7.get(anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode())).isEqualTo(asList(CopyType.SECONDARY.getCode(), CopyType.PRINCIPAL.getCode()));
		assertThat(dto7.get(anotherSchemaType.getMetadata("textMetadata").getDataStoreCode())).isEqualTo(asList("A", "B", "C"));
		assertThat(dto7.get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode()))
				.isEqualTo(record7.getRecordDTO().getFields().get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode()));
		assertThat(dto7.get(anotherSchemaType.getMetadata("structureMetadata").getDataStoreCode())).isEqualTo(asList(comment2ToString));

		assertThat(dto8.get(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isEqualTo(asList());
		assertThat(dto8.get(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isEqualTo(asList(6701));
		assertThat(dto8.get(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isEqualTo(asList());
		assertThat(dto8.get(anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode())).isEqualTo(asList(date, date.plusWeeks(52)));
		assertThat(dto8.get(anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode())).isEqualTo(asList());
		assertThat(dto8.get(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isEqualTo(asList("StoneColdSteveAustin", record4.getId(), record1.getId(), "Undertaker"));
		assertThat(dto8.get(anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode())).isEqualTo(asList(CopyType.SECONDARY.getCode()));
		assertThat(dto8.get(anotherSchemaType.getMetadata("textMetadata").getDataStoreCode())).isEqualTo(asList(" ", " Batman "));
		assertThat(dto8.get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode())).isEqualTo(asList());
		assertThat(dto8.get(anotherSchemaType.getMetadata("structureMetadata").getDataStoreCode())).isEqualTo(asList());
	}

	@Test
	public void whenStoringMetadatasInAByteArrayRecordDTOThenVerifyingTheStoreMetadatas() throws Exception {
		defineSchemasManager().using(setup
				.withATitle(whichIsEssentialInSummary)
				.withABooleanMetadata(whichIsEssentialInSummary)
				.withAnIntegerMetadata(whichIsEssentialInSummary)
				.withANumberMetadata(whichIsEssentialInSummary)
				.withAnEnumMetadata(FolderStatus.class, whichIsEssentialInSummary)
				.withAParentReferenceFromZeSchemaToZeSchema(whichIsEssentialInSummary)
				.withAReferenceFromAnotherSchemaToZeSchema(whichIsEssentialInSummary)
				.withADateMetadata(whichIsEssentialInSummary)
				.withAContentMetadata(whichIsEssentialInSummary)
				.withALargeTextMetadata(whichIsEssentialInSummary)
				.withADateTimeMetadata(whichIsEssentialInSummary));

		init();

		setup.modify((MetadataSchemaTypesAlteration) types -> {
			types.getSchema(anotherSchema.code())
					.create("booleanMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.BOOLEAN);
			types.getSchema(anotherSchema.code())
					.create("integerMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.INTEGER);
			types.getSchema(anotherSchema.code())
					.create("numberMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.NUMBER);
			types.getSchema(anotherSchema.code())
					.create("enumMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.ENUM)
					.defineAsEnum(CopyType.class);
			types.getSchema(anotherSchema.code())
					.create("dateMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.DATE);
			types.getSchema(anotherSchema.code())
					.create("dateTimeMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.DATE_TIME);
			types.getSchema(anotherSchema.code())
					.create("textMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.TEXT);
			types.getSchema(anotherSchema.code())
					.create("contentMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.CONTENT);
			types.getSchema(anotherSchema.code())
					.create("structureMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.STRUCTURE)
					.defineStructureFactory(CommentFactory.class);

			types.getSchema(zeSchema.code())
					.create("structMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.STRUCTURE)
					.defineStructureFactory(CommentFactory.class);
		});

		LocalDate date = new LocalDate();
		LocalDateTime dateTime = new LocalDateTime();

		File file = newTempFileWithContent("content.txt", "This is what a content file of type content contains. Content ?");
		ContentVersionDataSummary versionDataSummary = contentManager.upload(file);
		Content content = contentManager.createSystemContent(file.getName(), versionDataSummary);

		Comment comment = new Comment("Well hello there !", john, dateTime);

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());
		MetadataSchema zeSchemaType = schemaTypes.getSchema(zeSchema.code());

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(Schemas.TITLE, "Le village des Schtroumpfs")
				.set(zeSchema.booleanMetadata(), true)
				.set(zeSchema.integerMetadata(), 14)
				.set(zeSchema.numberMetadata(), -70.4d)
				.set(zeSchema.enumMetadata(), FolderStatus.ACTIVE)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), null)
				.set(zeSchema.dateMetadata(), date)
				.set(zeSchema.largeTextMetadata(), "Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
												   "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
												   "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
												   "nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in " +
												   "reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla " +
												   "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
												   "culpa qui officia deserunt mollit anim id est laborum.")
				.set(zeSchema.contentMetadata(), content)
				.set(zeSchemaType.getMetadata("structMetadata"), comment)
				.set(zeSchema.dateTimeMetadata(), dateTime);

		RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "recordMondial")
				.set(Schemas.TITLE, "")
				.set(zeSchema.integerMetadata(), 0)
				.set(zeSchema.booleanMetadata(), false)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record1.getId())
				.set(zeSchema.dateTimeMetadata(), dateTime.minusWeeks(13))
				.set(zeSchema.numberMetadata(), 0d)
				.set(zeSchema.dateMetadata(), date.plusMonths(2))
				.set(zeSchemaType.getMetadata("structMetadata"), comment)
				.set(zeSchema.largeTextMetadata(), "")
				.set(zeSchema.contentMetadata(), null)
				.set(zeSchema.enumMetadata(), FolderStatus.SEMI_ACTIVE);

		RecordImpl record3 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), "recordMondial")
				.set(zeSchema.booleanMetadata(), null)
				.set(zeSchemaType.getMetadata("structMetadata"), null)
				.set(zeSchema.integerMetadata(), -420)
				.set(zeSchema.dateMetadata(), date.minusYears(60))
				.set(zeSchema.numberMetadata(), 1337.12d)
				.set(zeSchema.enumMetadata(), null)
				.set(Schemas.TITLE, " Blue litte man ")
				.set(zeSchema.largeTextMetadata(), " ")
				.set(zeSchema.dateTimeMetadata(), dateTime.plusDays(300));

		RecordImpl record4 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "Gargamel")
				.set(zeSchema.dateMetadata(), null)
				.set(zeSchemaType.getMetadata("structMetadata"), null)
				.set(zeSchema.booleanMetadata(), true)
				.set(zeSchema.integerMetadata(), null)
				.set(zeSchema.numberMetadata(), null)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record3.getId())
				.set(Schemas.TITLE, "أريد أن أشرب الحليب")
				.set(zeSchema.enumMetadata(), FolderStatus.INACTIVE_DESTROYED)
				.set(zeSchema.largeTextMetadata(), null)
				.set(zeSchema.contentMetadata(), content)
				.set(zeSchema.dateTimeMetadata(), null);

		RecordImpl record5 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
				.set(Schemas.TITLE, "I'm blue da ba de da ba da")
				.set(anotherSchemaType.getMetadata("enumMetadata"), null)
				.set(anotherSchemaType.getMetadata("booleanMetadata"), true)
				.set(anotherSchemaType.getMetadata("dateMetadata"), date.plusYears(5).minusDays(2))
				.set(anotherSchemaType.getMetadata("numberMetadata"), -100.2d)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), record4.getId())
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), null)
				.set(anotherSchemaType.getMetadata("textMetadata"), " ")
				.set(anotherSchemaType.getMetadata("contentMetadata"), null)
				.set(anotherSchemaType.getMetadata("structureMetadata"), comment)
				.set(anotherSchemaType.getMetadata("integerMetadata"), null);

		RecordImpl record6 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Azrael")
				.set(Schemas.TITLE, " Nom d'un Schtroumpfs!")
				.set(anotherSchemaType.getMetadata("booleanMetadata"), null)
				.set(anotherSchemaType.getMetadata("numberMetadata"), null)
				.set(anotherSchemaType.getMetadata("enumMetadata"), CopyType.PRINCIPAL)
				.set(anotherSchemaType.getMetadata("dateMetadata"), date)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), null)
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), dateTime)
				.set(anotherSchemaType.getMetadata("textMetadata"), "")
				.set(anotherSchemaType.getMetadata("structureMetadata"), null)
				.set(anotherSchemaType.getMetadata("contentMetadata"), content)
				.set(anotherSchemaType.getMetadata("integerMetadata"), 0);

		RecordImpl record7 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "GrandSchtroumpfs")
				.set(Schemas.TITLE, "")
				.set(anotherSchemaType.getMetadata("integerMetadata"), -99)
				.set(anotherSchemaType.getMetadata("numberMetadata"), 0d)
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), dateTime.plusHours(600).minusWeeks(1))
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), "Gargamel")
				.set(anotherSchemaType.getMetadata("enumMetadata"), CopyType.SECONDARY)
				.set(anotherSchemaType.getMetadata("dateMetadata"), null)
				.set(anotherSchemaType.getMetadata("structureMetadata"), null)
				.set(anotherSchemaType.getMetadata("textMetadata"), "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
																	"nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in " +
																	"reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla " +
																	"pariatur.")
				.set(anotherSchemaType.getMetadata("contentMetadata"), content)
				.set(anotherSchemaType.getMetadata("booleanMetadata"), false);

		RecordImpl record8 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "SchtroumpfsGrognon")
				.set(anotherSchemaType.getMetadata("integerMetadata"), 1234)
				.set(anotherSchemaType.getMetadata("dateMetadata"), date.plusMonths(14))
				.set(anotherSchemaType.getMetadata("numberMetadata"), 10.88d)
				.set(anotherSchemaType.getMetadata("booleanMetadata"), true)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), "Gargamel")
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), dateTime.minusYears(21))
				.set(anotherSchemaType.getMetadata("enumMetadata"), CopyType.SECONDARY)
				.set(anotherSchemaType.getMetadata("textMetadata"), null)
				.set(anotherSchemaType.getMetadata("structureMetadata"), comment)
				.set(Schemas.TITLE, null);

		recordServices.execute(new Transaction(record1, record2, record3, record4, record5, record6, record7, record8));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());
		ByteArrayRecordDTO dto2 = create(getModelLayerFactory(), record2.getRecordDTO());
		ByteArrayRecordDTO dto3 = create(getModelLayerFactory(), record3.getRecordDTO());
		ByteArrayRecordDTO dto4 = create(getModelLayerFactory(), record4.getRecordDTO());
		ByteArrayRecordDTO dto5 = create(getModelLayerFactory(), record5.getRecordDTO());
		ByteArrayRecordDTO dto6 = create(getModelLayerFactory(), record6.getRecordDTO());
		ByteArrayRecordDTO dto7 = create(getModelLayerFactory(), record7.getRecordDTO());
		ByteArrayRecordDTO dto8 = create(getModelLayerFactory(), record8.getRecordDTO());

		assertThat(dto1.keySet()).contains(Schemas.TITLE.getDataStoreCode(),
				zeSchema.contentMetadata().getDataStoreCode(),
				zeSchema.booleanMetadata().getDataStoreCode(),
				zeSchema.integerMetadata().getDataStoreCode(),
				zeSchema.numberMetadata().getDataStoreCode(),
				zeSchema.enumMetadata().getDataStoreCode(),
				zeSchema.dateMetadata().getDataStoreCode(),
				zeSchema.largeTextMetadata().getDataStoreCode(),
				zeSchemaType.get("structMetadata").getDataStoreCode(),
				zeSchema.dateTimeMetadata().getDataStoreCode());

		assertThat(dto2.keySet()).contains(zeSchema.booleanMetadata().getDataStoreCode(),
				zeSchema.numberMetadata().getDataStoreCode(),
				zeSchema.enumMetadata().getDataStoreCode(),
				zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode(),
				zeSchemaType.get("structMetadata").getDataStoreCode(),
				zeSchema.dateTimeMetadata().getDataStoreCode());

		assertThat(dto3.keySet()).contains(zeSchema.integerMetadata().getDataStoreCode(),
				zeSchema.numberMetadata().getDataStoreCode(),
				zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode(),
				zeSchema.dateMetadata().getDataStoreCode(),
				zeSchema.largeTextMetadata().getDataStoreCode(),
				zeSchema.dateTimeMetadata().getDataStoreCode());

		assertThat(dto4.keySet()).contains(Schemas.TITLE.getDataStoreCode(),
				zeSchema.contentMetadata().getDataStoreCode(),
				zeSchema.booleanMetadata().getDataStoreCode(),
				zeSchema.enumMetadata().getDataStoreCode(),
				zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode());

		assertThat(dto5.keySet()).contains(Schemas.TITLE.getDataStoreCode(),
				anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode(),
				anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode(),
				anotherSchemaType.getMetadata("textMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("structureMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode());

		assertThat(dto6.keySet()).contains(Schemas.TITLE.getDataStoreCode(),
				anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode());

		assertThat(dto7.keySet()).contains(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode(),
				anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode(),
				anotherSchemaType.getMetadata("textMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode());

		assertThat(dto8.keySet()).contains(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode(),
				anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode(),
				anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("structureMetadata").getDataStoreCode(),
				anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode());
	}

	@Test
	public void whenStoringMetadatasInAByteArrayRecordDTOThenVerifyingTheStoredValues() throws Exception {
		defineSchemasManager().using(setup
				.withABooleanMetadata(whichIsMultivalue, whichIsEssentialInSummary)
				.withAnIntegerMetadata(whichIsMultivalue, whichIsEssentialInSummary)
				.withANumberMetadata(whichIsMultivalue, whichIsEssentialInSummary)
				.withADateMetadata(whichIsMultivalue, whichIsEssentialInSummary)
				.withADateTimeMetadata(whichIsMultivalue, whichIsEssentialInSummary)
				.withAReferenceMetadata(whichAllowsZeSchemaType, whichIsMultivalue, whichIsEssentialInSummary)
				.withAnEnumMetadata(CopyType.class, whichIsMultivalue, whichIsEssentialInSummary)
				.withAMultivaluedLargeTextMetadata(whichIsEssentialInSummary)
				.withAContentMetadata(whichIsMultivalue, whichIsEssentialInSummary)
				.withAReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue, whichIsEssentialInSummary));

		init();

		setup.modify((MetadataSchemaTypesAlteration) types -> {
			types.getSchema(anotherSchema.code())
					.create("booleanMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.BOOLEAN)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("integerMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.INTEGER)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("numberMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.NUMBER)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("enumMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.ENUM)
					.defineAsEnum(CopyType.class)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("dateMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.DATE)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("dateTimeMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.DATE_TIME)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("textMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.TEXT)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("contentMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.CONTENT)
					.setMultivalue(true);
			types.getSchema(anotherSchema.code())
					.create("structureMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.STRUCTURE)
					.defineStructureFactory(CommentFactory.class)
					.setMultivalue(true);

			types.getSchema(zeSchema.code())
					.create("structMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.STRUCTURE)
					.defineStructureFactory(CommentFactory.class)
					.setMultivalue(true);
		});

		LocalDate date = new LocalDate();
		LocalDateTime dateTime = new LocalDateTime();

		File file1 = newTempFileWithContent("content.txt", "This is what a content file of type content contains. Content ?");
		ContentVersionDataSummary versionDataSummary1 = contentManager.upload(file1);
		Content content = contentManager.createSystemContent(file1.getName(), versionDataSummary1);
		File file2 = newTempFileWithContent("no-content.txt", "Nothing to see here, move along");
		ContentVersionDataSummary versionDataSummary2 = contentManager.upload(file2);
		Content noContent = contentManager.createSystemContent(file2.getName(), versionDataSummary2);

		Comment comment1 = new Comment("Hello there !", john, dateTime);
		String comment1ToString = new CommentFactory().toString(comment1);
		Comment comment2 = new Comment("General Kenobi !", john, dateTime.plusDays(23));
		String comment2ToString = new CommentFactory().toString(comment2);

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());
		MetadataSchema zeSchemaType = schemaTypes.getSchema(zeSchema.code());

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.integerMetadata(), asList(0, 13, -40))
				.set(zeSchema.enumMetadata(), asList(CopyType.PRINCIPAL, CopyType.PRINCIPAL, CopyType.SECONDARY))
				.set(zeSchema.dateTimeMetadata(), asList(null, null))
				.set(zeSchema.numberMetadata(), asList(0d, -0d, 0.0d))
				.set(zeSchema.referenceMetadata(), null)
				.set(zeSchema.booleanMetadata(), null)
				.set(zeSchema.multivaluedLargeTextMetadata(), asList("Dance", "Play", "Pretend", "and I like to have fun, fun, fun"))
				.set(zeSchema.contentMetadata(), asList(content, noContent))
				.set(zeSchemaType.getMetadata("structMetadata"), asList(null, null))
				.set(zeSchema.dateMetadata(), asList(date.minusYears(22).plusMonths(2).plusDays(10), null));

		RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "StoneColdSteveAustin")
				.set(Schemas.TITLE, " Macho Man Randy Savage ")
				.set(zeSchema.integerMetadata(), asList(619, 33, -244))
				.set(zeSchema.enumMetadata(), asList(CopyType.PRINCIPAL, CopyType.PRINCIPAL, CopyType.SECONDARY))
				.set(zeSchema.numberMetadata(), null)
				.set(zeSchemaType.getMetadata("structMetadata"), asList(comment2, comment1))
				.set(zeSchema.referenceMetadata(), asList(null, record1.getId()))
				.set(zeSchema.dateMetadata(), asList(date, date.plusDays(3), date.plusDays(-100)))
				.set(zeSchema.dateTimeMetadata(), asList(null, dateTime.plusHours(5), dateTime.minusYears(33)))
				.set(zeSchema.multivaluedLargeTextMetadata(), asList("", "Luigi", null, "أريد أن أشرب الحليب", "Waluigi", "Wario"))
				.set(zeSchema.contentMetadata(), asList(noContent, content))
				.set(zeSchema.booleanMetadata(), asList(true, false, true));

		RecordImpl record3 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "Undertaker")
				.set(Schemas.TITLE, "The cream of the crop")
				.set(zeSchema.numberMetadata(), asList(420.69d, -40d, 0.0d))
				.set(zeSchema.integerMetadata(), asList(42, -13, 10))
				.set(zeSchema.enumMetadata(), asList(CopyType.PRINCIPAL, CopyType.PRINCIPAL, CopyType.SECONDARY))
				.set(zeSchemaType.getMetadata("structMetadata"), asList(comment1, comment2))
				.set(zeSchema.dateMetadata(), null)
				.set(zeSchema.referenceMetadata(), asList("StoneColdSteveAustin", record1.getId()))
				.set(zeSchema.booleanMetadata(), asList(false, null, true))
				.set(zeSchema.multivaluedLargeTextMetadata(), asList(null, null))
				.set(zeSchema.contentMetadata(), null)
				.set(zeSchema.dateTimeMetadata(), asList(dateTime, dateTime.plusHours(22), dateTime.minusYears(100)));

		RecordImpl record4 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(Schemas.TITLE, " ")
				.set(zeSchema.integerMetadata(), null)
				.set(zeSchema.numberMetadata(), asList(1.337d, -6.19d, 3.1416d))
				.set(zeSchema.booleanMetadata(), asList(null, null, null))
				.set(zeSchema.dateMetadata(), asList(null, null, null))
				.set(zeSchema.referenceMetadata(), asList("Undertaker", null))
				.set(zeSchema.enumMetadata(), asList(CopyType.PRINCIPAL, CopyType.PRINCIPAL, CopyType.SECONDARY))
				.set(zeSchema.multivaluedLargeTextMetadata(), asList(" ", "Kawabonga", null, "Yiihi"))
				.set(zeSchema.contentMetadata(), asList(content))
				.set(zeSchemaType.getMetadata("structMetadata"), asList(null, comment1))
				.set(zeSchema.dateTimeMetadata(), null);

		RecordImpl record5 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Tintin")
				.set(Schemas.TITLE, "JELLO")
				.set(anotherSchemaType.getMetadata("dateMetadata"), asList(date.minusWeeks(7), date))
				.set(anotherSchemaType.getMetadata("numberMetadata"), asList(0d))
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), null)
				.set(anotherSchemaType.getMetadata("enumMetadata"), asList(CopyType.SECONDARY, CopyType.SECONDARY, CopyType.SECONDARY))
				.set(anotherSchemaType.getMetadata("booleanMetadata"), asList(true, false, true))
				.set(anotherSchemaType.getMetadata("integerMetadata"), null)
				.set(anotherSchemaType.getMetadata("structureMetadata"), asList(comment1, comment2))
				.set(anotherSchemaType.getMetadata("textMetadata"), null)
				.set(anotherSchemaType.getMetadata("contentMetadata"), asList(content))
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(null, null, null, null));

		RecordImpl record6 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Milou")
				.set(Schemas.TITLE, " Bee yourself zzzZZZ ")
				.set(anotherSchemaType.getMetadata("booleanMetadata"), asList(null, true, false))
				.set(anotherSchemaType.getMetadata("numberMetadata"), asList(4600d))
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(record4.getId(), "Undertaker"))
				.set(anotherSchemaType.getMetadata("enumMetadata"), null)
				.set(anotherSchemaType.getMetadata("dateMetadata"), asList(null, null, null))
				.set(anotherSchemaType.getMetadata("integerMetadata"), asList(1, 2, 3))
				.set(anotherSchemaType.getMetadata("textMetadata"), asList(null, null))
				.set(anotherSchemaType.getMetadata("structureMetadata"), asList(comment1, comment2))
				.set(anotherSchemaType.getMetadata("contentMetadata"), asList(noContent))
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), asList(dateTime));

		RecordImpl record7 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "CapitaineHaddock")
				.set(anotherSchemaType.getMetadata("integerMetadata"), asList(0, -700))
				.set(anotherSchemaType.getMetadata("dateMetadata"), null)
				.set(anotherSchemaType.getMetadata("numberMetadata"), asList(456d, -123d))
				.set(anotherSchemaType.getMetadata("booleanMetadata"), asList(null, null))
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), asList(null, dateTime, dateTime.plusHours(4)))
				.set(anotherSchemaType.getMetadata("structureMetadata"), asList(comment2))
				.set(anotherSchemaType.getMetadata("enumMetadata"), asList(CopyType.SECONDARY, CopyType.PRINCIPAL))
				.set(anotherSchemaType.getMetadata("textMetadata"), asList("A", "B", "C"))
				.set(anotherSchemaType.getMetadata("contentMetadata"), asList(noContent, content))
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), null);

		RecordImpl record8 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "ProfesseurTournesol")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("StoneColdSteveAustin", record4.getId(), record1.getId(), "Undertaker"))
				.set(anotherSchemaType.getMetadata("integerMetadata"), asList(6701))
				.set(anotherSchemaType.getMetadata("booleanMetadata"), null)
				.set(anotherSchemaType.getMetadata("enumMetadata"), asList(CopyType.SECONDARY))
				.set(anotherSchemaType.getMetadata("structureMetadata"), null)
				.set(anotherSchemaType.getMetadata("dateMetadata"), asList(date, date.plusWeeks(52)))
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), asList(null, null, null))
				.set(anotherSchemaType.getMetadata("textMetadata"), asList("", " ", " Batman "))
				.set(anotherSchemaType.getMetadata("contentMetadata"), null)
				.set(anotherSchemaType.getMetadata("numberMetadata"), null);

		recordServices.execute(new Transaction(record1, record2, record3, record4, record5, record6, record7, record8));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());
		ByteArrayRecordDTO dto2 = create(getModelLayerFactory(), record2.getRecordDTO());
		ByteArrayRecordDTO dto3 = create(getModelLayerFactory(), record3.getRecordDTO());
		ByteArrayRecordDTO dto4 = create(getModelLayerFactory(), record4.getRecordDTO());
		ByteArrayRecordDTO dto5 = create(getModelLayerFactory(), record5.getRecordDTO());
		ByteArrayRecordDTO dto6 = create(getModelLayerFactory(), record6.getRecordDTO());
		ByteArrayRecordDTO dto7 = create(getModelLayerFactory(), record7.getRecordDTO());
		ByteArrayRecordDTO dto8 = create(getModelLayerFactory(), record8.getRecordDTO());

		assertThat(dto1.values()).contains(asList(0, 13, -40),
				record1.getRecordDTO().getFields().get(zeSchema.contentMetadata().getDataStoreCode()),
				asList(0d, -0d, 0.0d),
				asList(date.minusYears(22).plusMonths(2).plusDays(10), null),
				asList("Dance", "Play", "Pretend", "and I like to have fun, fun, fun"),
				asList(CopyType.PRINCIPAL.getCode(), CopyType.PRINCIPAL.getCode(), CopyType.SECONDARY.getCode()));

		assertThat(dto2.values()).contains(asList(true, false, true),
				record2.getRecordDTO().getFields().get(zeSchema.contentMetadata().getDataStoreCode()),
				" Macho Man Randy Savage ",
				asList(619, 33, -244),
				asList(date, date.plusDays(3), date.plusDays(-100)),
				asList(null, dateTime.plusHours(5), dateTime.minusYears(33)),
				asList(null, record1.getId()),
				asList(comment2ToString, comment1ToString),
				asList("Luigi", null, "أريد أن أشرب الحليب", "Waluigi", "Wario"),
				asList(CopyType.PRINCIPAL.getCode(), CopyType.PRINCIPAL.getCode(), CopyType.SECONDARY.getCode()));

		assertThat(dto3.values()).contains(asList(false, null, true),
				"The cream of the crop",
				asList(42, -13, 10),
				asList(comment1ToString, comment2ToString),
				asList(420.69d, -40d, 0.0d),
				asList(dateTime, dateTime.plusHours(22), dateTime.minusYears(100)),
				asList("StoneColdSteveAustin", record1.getId()),
				asList(CopyType.PRINCIPAL.getCode(), CopyType.PRINCIPAL.getCode(), CopyType.SECONDARY.getCode()));

		assertThat(dto4.values()).contains(asList(1.337d, -6.19d, 3.1416d),
				record4.getRecordDTO().getFields().get(zeSchema.contentMetadata().getDataStoreCode()),
				" ",
				asList("Undertaker", null),
				asList(" ", "Kawabonga", null, "Yiihi"),
				asList(null, comment1ToString),
				asList(CopyType.PRINCIPAL.getCode(), CopyType.PRINCIPAL.getCode(), CopyType.SECONDARY.getCode()));

		assertThat(dto5.values()).contains(asList(true, false, true),
				record5.getRecordDTO().getFields().get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode()),
				"JELLO",
				asList(0d),
				asList(date.minusWeeks(7), date),
				asList(null, null, null, null),
				asList(CopyType.SECONDARY.getCode(), CopyType.SECONDARY.getCode(), CopyType.SECONDARY.getCode()));

		assertThat(dto6.values()).contains(asList(null, true, false),
				record6.getRecordDTO().getFields().get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode()),
				" Bee yourself zzzZZZ ",
				asList(1, 2, 3),
				asList(comment1ToString, comment2ToString),
				asList(4600d),
				asList(record4.getId(), "Undertaker"));

		assertThat(dto7.values()).contains(asList(0, -700),
				record7.getRecordDTO().getFields().get(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode()),
				asList(456d, -123d),
				asList(null, dateTime, dateTime.plusHours(4)),
				asList("A", "B", "C"),
				asList(comment2ToString),
				asList(CopyType.SECONDARY.getCode(), CopyType.PRINCIPAL.getCode()));

		assertThat(dto8.values()).contains(asList(6701),
				asList(date, date.plusWeeks(52)),
				asList("StoneColdSteveAustin", record4.getId(), record1.getId(), "Undertaker"),
				asList(" ", " Batman "),
				asList(CopyType.SECONDARY.getCode()));
	}


	@Test
	public void whenStoringSingleStringMetadataInAByteArrayRecordDTOThenVerifyingTheStoredValue() throws Exception {
		defineSchemasManager().using(setup.withAStringMetadata(whichIsEssentialInSummary));
		init();

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.stringMetadata(), "الشمس حاره");
		RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.stringMetadata(), " Macho Man Randy Savage ");

		recordServices.execute(new Transaction(record1, record2));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());
		ByteArrayRecordDTO dto2 = create(getModelLayerFactory(), record2.getRecordDTO());

		assertThat(dto1.values()).contains("الشمس حاره");
		assertThat(dto2.values()).contains(" Macho Man Randy Savage ");
	}

	@Test
	public void whenStoringLargeMultivalueStringMetadataInAByteArrayRecordDTOThenVerifyingTheStoredValue()
			throws Exception {
		defineSchemasManager().using(setup.withALargeTextMetadata(whichIsEssentialInSummary, whichIsMultivalue).withAStringMetadata(whichIsEssentialInSummary, whichIsMultivalue));
		init();

		String val1 = "123wo" + StringUtils.repeat("lolo", 1000) + "42";
		String val2 = "Na" + StringUtils.repeat("na", 2000) + "nan Batman!";
		String val3 = "un " + StringUtils.repeat("très ", 10000) + " long texte";
		String val4 = "un encore plus " + StringUtils.repeat("très ", 10000) + " long texte";

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.stringMetadata(), asList(val1, val2, "الشمس حاره"))
				.set(zeSchema.largeTextMetadata(), Arrays.asList(val4, val3));
		RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.stringMetadata(), asList(val2, "الشمس حاره", val1))
				.set(zeSchema.largeTextMetadata(), asList(val3, val4, "الشمس حاره"));

		recordServices.execute(new Transaction(record1, record2));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());
		ByteArrayRecordDTO dto2 = create(getModelLayerFactory(), record2.getRecordDTO());

		assertThat(dto1.get(zeSchema.stringMetadata().getDataStoreCode())).isEqualTo(asList(val1, val2, "الشمس حاره"));
		assertThat(dto1.get(zeSchema.largeTextMetadata().getDataStoreCode())).isEqualTo(asList(val4, val3));
		assertThat(dto2.get(zeSchema.stringMetadata().getDataStoreCode())).isEqualTo(asList(val2, "الشمس حاره", val1));
		assertThat(dto2.get(zeSchema.largeTextMetadata().getDataStoreCode())).isEqualTo(asList(val3, val4, "الشمس حاره"));
	}


	@Test
	public void whenStoringMultipleBooleanMetadatasThenAllWrittenAndReadWithoutProblems()
			throws Exception {

		int createdMetadatas = 2_500;
		int sizeOfMultivalues = 200;

		defineSchemasManager().using(setup.with((schemaTypes) -> {
			for (int i = 0; i < createdMetadatas; i++) {
				schemaTypes.getSchema("zeSchemaType_default").create("meta" + i).setType(BOOLEAN)
						.setEssentialInSummary(true).setMultivalue(i % 2 == 0);
			}
		}));

		init();

		List<Object> values = new ArrayList<>();
		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance());

		for (int i = 0; i < createdMetadatas; i++) {
			Object value;
			if (i % 2 == 0) {
				value = range(0, sizeOfMultivalues).mapToObj((j) -> random.nextBoolean()).collect(toList());
			} else {
				value = random.nextBoolean();
			}
			values.add(value);
			record1.set(zeSchema.metadata("meta" + i), value);
		}


		recordServices.execute(new Transaction(record1));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());

		for (int i = 0; i < createdMetadatas; i++) {
			assertThat(dto1.get(zeSchema.metadata("meta" + i).getDataStoreCode())).describedAs("comparing meta" + i).isEqualTo(values.get(i));
		}

	}


	@Test
	public void whenStoringMultipleIntMetadatasThenAllWrittenAndReadWithoutProblems()
			throws Exception {

		int createdMetadatas = 2_500;
		int sizeOfMultivalues = 50;

		defineSchemasManager().using(setup.with((schemaTypes) -> {
			for (int i = 0; i < createdMetadatas; i++) {
				schemaTypes.getSchema("zeSchemaType_default").create("meta" + i).setType(INTEGER)
						.setEssentialInSummary(true).setMultivalue(i % 2 == 0);
			}
		}));

		init();

		List<Object> values = new ArrayList<>();
		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance());

		for (int i = 0; i < createdMetadatas; i++) {
			Object value;
			if (i % 2 == 0) {
				value = range(0, sizeOfMultivalues).mapToObj((j) -> random.nextInt()).collect(toList());
			} else {
				value = random.nextInt();
			}
			values.add(value);
			record1.set(zeSchema.metadata("meta" + i), value);
		}


		recordServices.execute(new Transaction(record1));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());

		for (int i = 0; i < createdMetadatas; i++) {
			assertThat(dto1.get(zeSchema.metadata("meta" + i).getDataStoreCode())).describedAs("comparing meta" + i).isEqualTo(values.get(i));
		}

	}


	@Test
	public void whenStoringMultipleEnumMetadatasThenAllWrittenAndReadWithoutProblems()
			throws Exception {

		int createdMetadatas = 2_500;
		int sizeOfMultivalues = 50;

		defineSchemasManager().using(setup.with((schemaTypes) -> {
			for (int i = 0; i < createdMetadatas; i++) {
				schemaTypes.getSchema("zeSchemaType_default").create("meta" + i).defineAsEnum(CopyType.class)
						.setEssentialInSummary(true).setMultivalue(i % 2 == 0);
			}
		}));

		init();

		List<Object> values = new ArrayList<>();
		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance());

		for (int i = 0; i < createdMetadatas; i++) {
			Object value;
			if (i % 2 == 0) {
				value = range(0, sizeOfMultivalues).mapToObj((j) -> random.nextBoolean() ? CopyType.PRINCIPAL : CopyType.SECONDARY).collect(toList());
				record1.set(zeSchema.metadata("meta" + i), value);
				values.add(((List<CopyType>) value).stream().map(CopyType::getCode).collect(toList()));
			} else {
				value = random.nextBoolean() ? CopyType.PRINCIPAL : CopyType.SECONDARY;
				record1.set(zeSchema.metadata("meta" + i), value);
				values.add(((CopyType) value).getCode());
			}
		}


		recordServices.execute(new Transaction(record1));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());

		for (int i = 0; i < createdMetadatas; i++) {
			assertThat(dto1.get(zeSchema.metadata("meta" + i).getDataStoreCode())).describedAs("comparing meta" + i).isEqualTo(values.get(i));
		}

	}


	@Test
	public void whenStoringMultipleNumberMetadatasThenAllWrittenAndReadWithoutProblems()
			throws Exception {

		int createdMetadatas = 2_500;
		int sizeOfMultivalues = 25;

		defineSchemasManager().using(setup.with((schemaTypes) -> {
			for (int i = 0; i < createdMetadatas; i++) {
				schemaTypes.getSchema("zeSchemaType_default").create("meta" + i).setType(NUMBER)
						.setEssentialInSummary(true).setMultivalue(i % 2 == 0);
			}
		}));

		init();

		List<Object> values = new ArrayList<>();
		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance());

		for (int i = 0; i < createdMetadatas; i++) {
			Object value;
			if (i % 2 == 0) {
				value = range(0, sizeOfMultivalues).mapToObj((j) -> random.nextDouble()).collect(toList());
			} else {
				value = random.nextDouble();
			}
			values.add(value);
			record1.set(zeSchema.metadata("meta" + i), value);
		}


		recordServices.execute(new Transaction(record1));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());

		for (int i = 0; i < createdMetadatas; i++) {
			assertThat(dto1.get(zeSchema.metadata("meta" + i).getDataStoreCode())).describedAs("comparing meta" + i).isEqualTo(values.get(i));
		}

	}


	@Test
	public void whenStoringMultipleDateMetadatasThenAllWrittenAndReadWithoutProblems()
			throws Exception {

		int createdMetadatas = 2_500;
		int sizeOfMultivalues = 50;

		defineSchemasManager().using(setup.with((schemaTypes) -> {
			for (int i = 0; i < createdMetadatas; i++) {
				schemaTypes.getSchema("zeSchemaType_default").create("meta" + i).setType(MetadataValueType.DATE)
						.setEssentialInSummary(true).setMultivalue(i % 2 == 0);
			}
		}));

		init();

		List<Object> values = new ArrayList<>();
		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance());

		for (int i = 0; i < createdMetadatas; i++) {
			Object value;
			if (i % 2 == 0) {
				value = range(0, sizeOfMultivalues).mapToObj((j) -> new LocalDate().minusDays(random.nextInt(1000))).collect(toList());
			} else {
				value = new LocalDate().minusDays(random.nextInt(1000));
			}
			values.add(value);
			record1.set(zeSchema.metadata("meta" + i), value);
		}


		recordServices.execute(new Transaction(record1));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());

		for (int i = 0; i < createdMetadatas; i++) {
			assertThat(dto1.get(zeSchema.metadata("meta" + i).getDataStoreCode())).describedAs("comparing meta" + i).isEqualTo(values.get(i));
		}

	}


	@Test
	public void whenStoringMultipleDateTimeMetadatasThenAllWrittenAndReadWithoutProblems()
			throws Exception {

		int createdMetadatas = 2_500;
		int sizeOfMultivalues = 25;

		defineSchemasManager().using(setup.with((schemaTypes) -> {
			for (int i = 0; i < createdMetadatas; i++) {
				schemaTypes.getSchema("zeSchemaType_default").create("meta" + i).setType(MetadataValueType.DATE_TIME)
						.setEssentialInSummary(true).setMultivalue(i % 2 == 0);
			}
		}));

		init();

		List<Object> values = new ArrayList<>();
		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance());

		for (int i = 0; i < createdMetadatas; i++) {
			Object value;
			if (i % 2 == 0) {
				value = range(0, sizeOfMultivalues).mapToObj((j) -> new LocalDateTime().minusSeconds(random.nextInt(1000))).collect(toList());
			} else {
				value = new LocalDateTime().minusSeconds(random.nextInt(1000));
			}
			values.add(value);
			record1.set(zeSchema.metadata("meta" + i), value);
		}


		recordServices.execute(new Transaction(record1));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());

		for (int i = 0; i < createdMetadatas; i++) {
			assertThat(dto1.get(zeSchema.metadata("meta" + i).getDataStoreCode())).describedAs("comparing meta" + i).isEqualTo(values.get(i));
		}

	}


	@Test
	public void whenStoringMultipleStringMetadatasThenAllWrittenAndReadWithoutProblems()
			throws Exception {

		int createdMetadatas = 2_500;
		int sizeOfMultivalues = 5;

		defineSchemasManager().using(setup.with((schemaTypes) -> {
			for (int i = 0; i < createdMetadatas; i++) {
				schemaTypes.getSchema("zeSchemaType_default").create("meta" + i).setType(STRING)
						.setEssentialInSummary(true).setMultivalue(i % 2 == 0);
			}
		}));

		init();

		List<Object> values = new ArrayList<>();
		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance());

		for (int i = 0; i < createdMetadatas; i++) {
			Object value;
			if (i % 2 == 0) {
				value = range(0, sizeOfMultivalues).mapToObj((j) -> new LocalDateTime().minusSeconds(random.nextInt(1000)).toString()).collect(toList());
			} else {
				value = new LocalDateTime().minusSeconds(random.nextInt(1000)).toString();
			}
			values.add(value);
			record1.set(zeSchema.metadata("meta" + i), value);
		}


		recordServices.execute(new Transaction(record1));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());

		for (int i = 0; i < createdMetadatas; i++) {
			assertThat(dto1.get(zeSchema.metadata("meta" + i).getDataStoreCode())).describedAs("comparing meta" + i).isEqualTo(values.get(i));
		}

	}


	@Test
	public void whenStoringMultipleTextMetadatasThenAllWrittenAndReadWithoutProblems()
			throws Exception {

		int createdMetadatas = 2_500;
		int sizeOfMultivalues = 5;

		defineSchemasManager().using(setup.with((schemaTypes) -> {
			for (int i = 0; i < createdMetadatas; i++) {
				schemaTypes.getSchema("zeSchemaType_default").create("meta" + i).setType(TEXT)
						.setEssentialInSummary(true).setMultivalue(i % 2 == 0);
			}
		}));

		init();

		List<Object> values = new ArrayList<>();
		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance());

		for (int i = 0; i < createdMetadatas; i++) {
			Object value;
			if (i % 2 == 0) {
				value = range(0, sizeOfMultivalues).mapToObj((j) -> new LocalDateTime().minusSeconds(random.nextInt(1000)).toString()).collect(toList());
			} else {
				value = new LocalDateTime().minusSeconds(random.nextInt(1000)).toString();
			}
			values.add(value);
			record1.set(zeSchema.metadata("meta" + i), value);
		}


		recordServices.execute(new Transaction(record1));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());

		for (int i = 0; i < createdMetadatas; i++) {
			assertThat(dto1.get(zeSchema.metadata("meta" + i).getDataStoreCode())).describedAs("comparing meta" + i).isEqualTo(values.get(i));
		}

	}

	@Test
	public void whenStoringMultipleStringIdReferenceMetadatasThenAllWrittenAndReadWithoutProblems()
			throws Exception {

		int createdMetadatas = 2_500;
		int sizeOfMultivalues = 10;

		defineSchemasManager().using(setup.with((schemaTypes) -> {
			for (int i = 0; i < createdMetadatas; i++) {
				schemaTypes.getSchema("zeSchemaType_default").create("meta" + i).setType(MetadataValueType.REFERENCE)
						.defineReferencesTo(schemaTypes.getSchemaType("anotherSchemaType"))
						.setEssentialInSummary(true).setMultivalue(i % 2 == 0);
			}
		}));

		init();

		List<Object> values = new ArrayList<>();

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance());

		List<String> ids = new ArrayList<>();
		Transaction tx = new Transaction();
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance(), "r1")).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance(), "r2")).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance(), "r3")).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance(), "r4")).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance(), "r5")).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance(), "r6")).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance(), "r7")).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance(), "r8")).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance(), "r9")).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance(), "r10")).getId());


		for (int i = 0; i < createdMetadatas; i++) {
			Object value;
			if (i % 2 == 0) {
				value = range(0, sizeOfMultivalues).mapToObj((j) -> ids.get(random.nextInt(ids.size()))).collect(toList());
			} else {
				value = ids.get(random.nextInt(ids.size()));
			}
			values.add(value);
			record1.set(zeSchema.metadata("meta" + i), value);
		}


		tx.add(record1);


		recordServices.execute(tx);

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());

		for (int i = 0; i < createdMetadatas; i++) {
			assertThat(dto1.get(zeSchema.metadata("meta" + i).getDataStoreCode())).describedAs("comparing meta" + i).isEqualTo(values.get(i));
		}

	}


	@Test
	public void whenStoringMultipleIntIdReferenceMetadatasThenAllWrittenAndReadWithoutProblems()
			throws Exception {

		int createdMetadatas = 2_500;
		int sizeOfMultivalues = 50;

		defineSchemasManager().using(setup.with((schemaTypes) -> {
			for (int i = 0; i < createdMetadatas; i++) {
				schemaTypes.getSchema("zeSchemaType_default").create("meta" + i).setType(MetadataValueType.REFERENCE)
						.defineReferencesTo(schemaTypes.getSchemaType("anotherSchemaType"))
						.setEssentialInSummary(true).setMultivalue(i % 2 == 0);
			}
		}));

		init();

		List<Object> values = new ArrayList<>();

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance());

		List<String> ids = new ArrayList<>();
		Transaction tx = new Transaction();
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance())).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance())).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance())).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance())).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance())).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance())).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance())).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance())).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance())).getId());
		ids.add(tx.add(recordServices.newRecordWithSchema(anotherSchema.instance())).getId());


		for (int i = 0; i < createdMetadatas; i++) {
			Object value;
			if (i % 2 == 0) {
				value = range(0, sizeOfMultivalues).mapToObj((j) -> ids.get(random.nextInt(ids.size()))).collect(toList());
			} else {
				value = ids.get(random.nextInt(ids.size()));
			}
			values.add(value);
			record1.set(zeSchema.metadata("meta" + i), value);
		}


		tx.add(record1);


		recordServices.execute(tx);

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());

		for (int i = 0; i < createdMetadatas; i++) {
			assertThat(dto1.get(zeSchema.metadata("meta" + i).getDataStoreCode())).describedAs("comparing meta" + i).isEqualTo(values.get(i));
		}

	}

	@Test
	public void whenStoringMetadatasInAByteArrayRecordDTOThenVerifyingTheEntries() throws Exception {
		defineSchemasManager().using(setup
				.withABooleanMetadata(whichIsEssentialInSummary)
				.withAStringMetadata(whichIsEssentialInSummary)
				.withANumberMetadata(whichIsEssentialInSummary)
				.withATitle(whichIsEssentialInSummary)
				.withAContentMetadata(whichIsEssentialInSummary)
				.withAnIntegerMetadata(whichIsEssentialInSummary)
				.withAReferenceMetadata(whichAllowsZeSchemaType, whichIsMultivalue, whichIsEssentialInSummary)
				.withAReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue, whichIsEssentialInSummary)
				.withAParentReferenceFromZeSchemaToZeSchema(whichIsEssentialInSummary));

		init();

		setup.modify((MetadataSchemaTypesAlteration) types -> {
			types.getSchema(anotherSchema.code())
					.create("stringMetadata").setMultivalue(true).setEssentialInSummary(true)
					.setType(STRING);

			types.getSchema(anotherSchema.code())
					.create("booleanMetadata").setMultivalue(true).setEssentialInSummary(true)
					.setType(MetadataValueType.BOOLEAN);

			types.getSchema(anotherSchema.code())
					.create("textMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.TEXT);
		});

		File file = newTempFileWithContent("content.txt", "This is what a content file of type content contains. Content ?");
		ContentVersionDataSummary versionDataSummary = contentManager.upload(file);
		Content content = contentManager.createSystemContent(file.getName(), versionDataSummary);

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.booleanMetadata(), true)
				.set(zeSchema.stringMetadata(), "Howd'y Cowboy ?")
				.set(zeSchema.numberMetadata(), 777d)
				.set(zeSchema.integerMetadata(), 666)
				.set(zeSchema.referenceMetadata(), null)
				.set(zeSchema.contentMetadata(), content)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), null);

		RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "LuckyLuke")
				.set(zeSchema.booleanMetadata(), false)
				.set(zeSchema.stringMetadata(), "Le moment est venu de nous dire au revoir, mon vieux Jolly Jumper.")
				.set(zeSchema.numberMetadata(), 0d)
				.set(Schemas.TITLE, "Les aventures de Lucky Luke")
				.set(zeSchema.referenceMetadata(), null)
				.set(zeSchema.contentMetadata(), null)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record1.getId());

		RecordImpl record3 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "JollyJumper")
				.set(zeSchema.booleanMetadata(), null)
				.set(zeSchema.stringMetadata(), "Je n'aime pas le voir partir seul. Sans moi, il est démonté.")
				.set(zeSchema.numberMetadata(), -8d)
				.set(zeSchema.referenceMetadata(), asList("LuckyLuke", record1.getId()))
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), null);

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());

		RecordImpl record4 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Rantanplan")
				.set(anotherSchema.stringMetadata(), asList("Woof woof", "bark bark", "Grrrrr"))
				.set(anotherSchemaType.getMetadata("booleanMetadata"), asList(true, true, true))
				.set(anotherSchemaType.getMetadata("textMetadata"), "THIS IS A LOT OF TEXT. WOW INDEED IT IS. ")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("JollyJumper", "LuckyLuke"));

		RecordImpl record5 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "JoeDalton")
				.set(anotherSchema.stringMetadata(), asList("Damn you Lucky Luke", "Hands up!", "PEW PEW PEW"))
				.set(anotherSchemaType.getMetadata("booleanMetadata"), asList(null, null, false))
				.set(Schemas.TITLE, " Lucky Luke in downtown Matane ")
				.set(anotherSchemaType.getMetadata("textMetadata"), "")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(null, null));

		RecordImpl record6 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "JackDalton")
				.set(anotherSchema.stringMetadata(), asList("Hey Joe !", "Shut up it's your fault", ":'("))
				.set(anotherSchemaType.getMetadata("booleanMetadata"), null)
				.set(anotherSchemaType.getMetadata("textMetadata"), null)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList(null, "LuckyLuke"));

		recordServices.execute(new Transaction(record1, record2, record3, record4, record5, record6));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());
		ByteArrayRecordDTO dto2 = create(getModelLayerFactory(), record2.getRecordDTO());
		ByteArrayRecordDTO dto3 = create(getModelLayerFactory(), record3.getRecordDTO());
		ByteArrayRecordDTO dto4 = create(getModelLayerFactory(), record4.getRecordDTO());
		ByteArrayRecordDTO dto5 = create(getModelLayerFactory(), record5.getRecordDTO());
		ByteArrayRecordDTO dto6 = create(getModelLayerFactory(), record6.getRecordDTO());

		assertThat(toMap(dto1.entrySet())).contains(entry(zeSchema.booleanMetadata().getDataStoreCode(), true),
				entry(zeSchema.contentMetadata().getDataStoreCode(), record1.getRecordDTO().getFields().get(zeSchema.contentMetadata().getDataStoreCode())),
				entry(zeSchema.stringMetadata().getDataStoreCode(), "Howd'y Cowboy ?"),
				entry(zeSchema.numberMetadata().getDataStoreCode(), 777d),
				entry(zeSchema.integerMetadata().getDataStoreCode(), 666));

		assertThat(toMap(dto2.entrySet())).contains(entry(zeSchema.booleanMetadata().getDataStoreCode(), false),
				entry(zeSchema.stringMetadata().getDataStoreCode(), "Le moment est venu de nous dire au revoir, mon vieux Jolly Jumper."),
				entry(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode(), record1.getId()),
				entry(zeSchema.numberMetadata().getDataStoreCode(), 0d),
				entry(Schemas.TITLE.getDataStoreCode(), "Les aventures de Lucky Luke"));

		assertThat(toMap(dto3.entrySet())).contains(
				entry(zeSchema.stringMetadata().getDataStoreCode(), "Je n'aime pas le voir partir seul. Sans moi, il est démonté."),
				entry(zeSchema.referenceMetadata().getDataStoreCode(), asList("LuckyLuke", record1.getId())),
				entry(zeSchema.numberMetadata().getDataStoreCode(), -8d));

		assertThat(toMap(dto4.entrySet())).contains(entry(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode(), asList(true, true, true)),
				entry(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode(), asList("JollyJumper", "LuckyLuke")),
				entry(anotherSchema.stringMetadata().getDataStoreCode(), asList("Woof woof", "bark bark", "Grrrrr")),
				entry(anotherSchemaType.get("textMetadata").getDataStoreCode(), "THIS IS A LOT OF TEXT. WOW INDEED IT IS. "));

		assertThat(toMap(dto5.entrySet())).contains(entry(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode(), asList(null, null, false)),
				entry(anotherSchema.stringMetadata().getDataStoreCode(), asList("Damn you Lucky Luke", "Hands up!", "PEW PEW PEW")),
				entry(Schemas.TITLE.getDataStoreCode(), " Lucky Luke in downtown Matane "),
				entry(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode(), asList(null, null)));

		assertThat(toMap(dto6.entrySet())).contains(
				entry(anotherSchema.stringMetadata().getDataStoreCode(), asList("Hey Joe !", "Shut up it's your fault", ":'(")),
				entry(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode(), asList(null, "LuckyLuke")));
	}

	@Test
	public void whenStoringMetadatasInByteArrayRecordDTOThenVerifyingTheContain() throws Exception {
		defineSchemasManager().using(setup
				.withATitle(whichIsEssentialInSummary)
				.withABooleanMetadata(whichIsEssentialInSummary)
				.withAnIntegerMetadata(whichIsEssentialInSummary)
				.withANumberMetadata(whichIsEssentialInSummary)
				.withAnEnumMetadata(FolderStatus.class, whichIsEssentialInSummary)
				.withAParentReferenceFromZeSchemaToZeSchema(whichIsEssentialInSummary)
				.withAReferenceFromAnotherSchemaToZeSchema(whichIsEssentialInSummary)
				.withADateMetadata(whichIsEssentialInSummary)
				.withAContentMetadata(whichIsEssentialInSummary)
				.withALargeTextMetadata(whichIsEssentialInSummary)
				.withADateTimeMetadata(whichIsEssentialInSummary));

		init();

		setup.modify((MetadataSchemaTypesAlteration) types -> {
			types.getSchema(anotherSchema.code())
					.create("booleanMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.BOOLEAN);
			types.getSchema(anotherSchema.code())
					.create("integerMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.INTEGER);
			types.getSchema(anotherSchema.code())
					.create("numberMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.NUMBER);
			types.getSchema(anotherSchema.code())
					.create("enumMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.ENUM)
					.defineAsEnum(CopyType.class);
			types.getSchema(anotherSchema.code())
					.create("dateMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.DATE);
			types.getSchema(anotherSchema.code())
					.create("dateTimeMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.DATE_TIME);
			types.getSchema(anotherSchema.code())
					.create("textMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.TEXT);
			types.getSchema(anotherSchema.code())
					.create("contentMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.CONTENT);
			types.getSchema(anotherSchema.code())
					.create("structureMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.STRUCTURE)
					.defineStructureFactory(CommentFactory.class);

			types.getSchema(zeSchema.code())
					.create("structMetadata").setEssentialInSummary(true)
					.setType(MetadataValueType.STRUCTURE)
					.defineStructureFactory(CommentFactory.class);
		});

		LocalDate date = new LocalDate();
		LocalDateTime dateTime = new LocalDateTime();

		File file = newTempFileWithContent("content.txt", "This is what a content file of type content contains. Content ?");
		ContentVersionDataSummary versionDataSummary = contentManager.upload(file);
		Content content = contentManager.createSystemContent(file.getName(), versionDataSummary);

		Comment comment = new Comment("Well hello there !", john, dateTime);
		String commentToString = new CommentFactory().toString(comment);

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());
		MetadataSchema zeSchemaType = schemaTypes.getSchema(zeSchema.code());

		RecordImpl record1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(Schemas.TITLE, "Le village des Schtroumpfs")
				.set(zeSchema.booleanMetadata(), true)
				.set(zeSchema.integerMetadata(), 14)
				.set(zeSchema.numberMetadata(), -70.4d)
				.set(zeSchema.enumMetadata(), FolderStatus.ACTIVE)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), null)
				.set(zeSchema.dateMetadata(), date)
				.set(zeSchema.largeTextMetadata(), "Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
												   "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
												   "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
												   "nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in " +
												   "reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla " +
												   "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
												   "culpa qui officia deserunt mollit anim id est laborum.")
				.set(zeSchema.contentMetadata(), content)
				.set(zeSchemaType.getMetadata("structMetadata"), comment)
				.set(zeSchema.dateTimeMetadata(), dateTime);

		RecordImpl record2 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "recordMondial")
				.set(Schemas.TITLE, "")
				.set(zeSchema.integerMetadata(), 0)
				.set(zeSchema.booleanMetadata(), false)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record1.getId())
				.set(zeSchema.dateTimeMetadata(), dateTime.minusWeeks(13))
				.set(zeSchema.numberMetadata(), 0d)
				.set(zeSchema.dateMetadata(), date.plusMonths(2))
				.set(zeSchema.largeTextMetadata(), "")
				.set(zeSchema.contentMetadata(), null)
				.set(zeSchemaType.getMetadata("structMetadata"), comment)
				.set(zeSchema.enumMetadata(), FolderStatus.SEMI_ACTIVE);

		RecordImpl record3 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), "recordMondial")
				.set(zeSchema.booleanMetadata(), null)
				.set(zeSchema.integerMetadata(), -420)
				.set(zeSchema.dateMetadata(), date.minusYears(60))
				.set(zeSchemaType.getMetadata("structMetadata"), null)
				.set(zeSchema.numberMetadata(), 1337.12d)
				.set(zeSchema.enumMetadata(), null)
				.set(Schemas.TITLE, " Blue litte man ")
				.set(zeSchema.largeTextMetadata(), " ")
				.set(zeSchema.dateTimeMetadata(), dateTime.plusDays(300));

		RecordImpl record4 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance(), "Gargamel")
				.set(zeSchema.dateMetadata(), null)
				.set(zeSchema.booleanMetadata(), true)
				.set(zeSchemaType.getMetadata("structMetadata"), null)
				.set(zeSchema.integerMetadata(), null)
				.set(zeSchema.numberMetadata(), null)
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), record3.getId())
				.set(Schemas.TITLE, "أريد أن أشرب الحليب")
				.set(zeSchema.enumMetadata(), FolderStatus.INACTIVE_DESTROYED)
				.set(zeSchema.largeTextMetadata(), null)
				.set(zeSchema.contentMetadata(), content)
				.set(zeSchema.dateTimeMetadata(), null);

		RecordImpl record5 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
				.set(Schemas.TITLE, "I'm blue da ba de da ba da")
				.set(anotherSchemaType.getMetadata("enumMetadata"), null)
				.set(anotherSchemaType.getMetadata("booleanMetadata"), true)
				.set(anotherSchemaType.getMetadata("dateMetadata"), date.plusYears(5).minusDays(2))
				.set(anotherSchemaType.getMetadata("numberMetadata"), -100.2d)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), record4.getId())
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), null)
				.set(anotherSchemaType.getMetadata("textMetadata"), " ")
				.set(anotherSchemaType.getMetadata("contentMetadata"), null)
				.set(anotherSchemaType.getMetadata("structureMetadata"), comment)
				.set(anotherSchemaType.getMetadata("integerMetadata"), null);

		RecordImpl record6 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "Azrael")
				.set(Schemas.TITLE, " Nom d'un Schtroumpfs!")
				.set(anotherSchemaType.getMetadata("booleanMetadata"), null)
				.set(anotherSchemaType.getMetadata("structureMetadata"), null)
				.set(anotherSchemaType.getMetadata("numberMetadata"), null)
				.set(anotherSchemaType.getMetadata("enumMetadata"), CopyType.PRINCIPAL)
				.set(anotherSchemaType.getMetadata("dateMetadata"), date)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), null)
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), dateTime)
				.set(anotherSchemaType.getMetadata("textMetadata"), "")
				.set(anotherSchemaType.getMetadata("contentMetadata"), content)
				.set(anotherSchemaType.getMetadata("integerMetadata"), 0);

		RecordImpl record7 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "GrandSchtroumpfs")
				.set(Schemas.TITLE, "")
				.set(anotherSchemaType.getMetadata("integerMetadata"), -99)
				.set(anotherSchemaType.getMetadata("numberMetadata"), 0d)
				.set(anotherSchemaType.getMetadata("structureMetadata"), null)
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), dateTime.plusHours(600).minusWeeks(1))
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), "Gargamel")
				.set(anotherSchemaType.getMetadata("enumMetadata"), CopyType.SECONDARY)
				.set(anotherSchemaType.getMetadata("dateMetadata"), null)
				.set(anotherSchemaType.getMetadata("textMetadata"), "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris " +
																	"nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in " +
																	"reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla " +
																	"pariatur.")
				.set(anotherSchemaType.getMetadata("contentMetadata"), content)
				.set(anotherSchemaType.getMetadata("booleanMetadata"), false);

		RecordImpl record8 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance(), "SchtroumpfsGrognon")
				.set(anotherSchemaType.getMetadata("integerMetadata"), 1234)
				.set(anotherSchemaType.getMetadata("structureMetadata"), comment)
				.set(anotherSchemaType.getMetadata("dateMetadata"), date.plusMonths(14))
				.set(anotherSchemaType.getMetadata("numberMetadata"), 10.88d)
				.set(anotherSchemaType.getMetadata("booleanMetadata"), true)
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), "Gargamel")
				.set(anotherSchemaType.getMetadata("dateTimeMetadata"), dateTime.minusYears(21))
				.set(anotherSchemaType.getMetadata("enumMetadata"), CopyType.SECONDARY)
				.set(anotherSchemaType.getMetadata("textMetadata"), null)
				.set(Schemas.TITLE, null);

		recordServices.execute(new Transaction(record1, record2, record3, record4, record5, record6, record7, record8));

		ByteArrayRecordDTO dto1 = create(getModelLayerFactory(), record1.getRecordDTO());
		ByteArrayRecordDTO dto2 = create(getModelLayerFactory(), record2.getRecordDTO());
		ByteArrayRecordDTO dto3 = create(getModelLayerFactory(), record3.getRecordDTO());
		ByteArrayRecordDTO dto4 = create(getModelLayerFactory(), record4.getRecordDTO());
		ByteArrayRecordDTO dto5 = create(getModelLayerFactory(), record5.getRecordDTO());
		ByteArrayRecordDTO dto6 = create(getModelLayerFactory(), record6.getRecordDTO());
		ByteArrayRecordDTO dto7 = create(getModelLayerFactory(), record7.getRecordDTO());
		ByteArrayRecordDTO dto8 = create(getModelLayerFactory(), record8.getRecordDTO());

		assertThat(dto1.containsKey(zeSchema.booleanMetadata().getDataStoreCode())).isTrue();
		assertThat(dto1.containsKey(Schemas.TITLE.getDataStoreCode())).isTrue();
		assertThat(dto1.containsKey(zeSchema.numberMetadata().getDataStoreCode())).isTrue();
		assertThat(dto1.containsKey(zeSchema.integerMetadata().getDataStoreCode())).isTrue();
		assertThat(dto1.containsKey(zeSchema.enumMetadata().getDataStoreCode())).isTrue();
		assertThat(dto1.containsKey(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isFalse();
		assertThat(dto1.containsKey(zeSchema.largeTextMetadata().getDataStoreCode())).isTrue();
		assertThat(dto1.containsKey(zeSchema.dateMetadata().getDataStoreCode())).isTrue();
		assertThat(dto1.containsKey(zeSchema.contentMetadata().getDataStoreCode())).isTrue();
		assertThat(dto1.containsKey(zeSchemaType.getMetadata("structMetadata").getDataStoreCode())).isTrue();

		assertThat(dto2.containsKey(zeSchema.booleanMetadata().getDataStoreCode())).isTrue();
		assertThat(dto2.containsKey(zeSchema.integerMetadata().getDataStoreCode())).isTrue();
		assertThat(dto2.containsKey(Schemas.TITLE.getDataStoreCode())).isFalse();
		assertThat(dto2.containsKey(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isTrue();
		assertThat(dto2.containsKey(zeSchema.numberMetadata().getDataStoreCode())).isTrue();
		assertThat(dto2.containsKey(zeSchema.largeTextMetadata().getDataStoreCode())).isFalse();
		assertThat(dto2.containsKey(zeSchema.dateMetadata().getDataStoreCode())).isTrue();
		assertThat(dto2.containsKey(zeSchema.dateTimeMetadata().getDataStoreCode())).isTrue();
		assertThat(dto2.containsKey(zeSchema.contentMetadata().getDataStoreCode())).isFalse();
		assertThat(dto2.containsKey(zeSchemaType.getMetadata("structMetadata").getDataStoreCode())).isTrue();

		assertThat(dto3.containsKey(zeSchema.booleanMetadata().getDataStoreCode())).isFalse();
		assertThat(dto3.containsKey(zeSchema.numberMetadata().getDataStoreCode())).isTrue();
		assertThat(dto3.containsKey(zeSchema.integerMetadata().getDataStoreCode())).isTrue();
		assertThat(dto3.containsKey(zeSchema.dateMetadata().getDataStoreCode())).isTrue();
		assertThat(dto3.containsKey(zeSchema.dateTimeMetadata().getDataStoreCode())).isTrue();
		assertThat(dto3.containsKey(zeSchema.enumMetadata().getDataStoreCode())).isFalse();
		assertThat(dto3.containsKey(zeSchema.largeTextMetadata().getDataStoreCode())).isTrue();
		assertThat(dto3.containsKey(Schemas.TITLE.getDataStoreCode())).isTrue();
		assertThat(dto3.containsKey(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isTrue();
		assertThat(dto3.containsKey(zeSchema.contentMetadata().getDataStoreCode())).isFalse();
		assertThat(dto3.containsKey(zeSchemaType.getMetadata("structMetadata").getDataStoreCode())).isFalse();

		assertThat(dto4.containsKey(zeSchema.booleanMetadata().getDataStoreCode())).isTrue();
		assertThat(dto4.containsKey(zeSchema.numberMetadata().getDataStoreCode())).isFalse();
		assertThat(dto4.containsKey(zeSchema.integerMetadata().getDataStoreCode())).isFalse();
		assertThat(dto4.containsKey(zeSchema.dateMetadata().getDataStoreCode())).isFalse();
		assertThat(dto4.containsKey(zeSchema.dateTimeMetadata().getDataStoreCode())).isFalse();
		assertThat(dto4.containsKey(zeSchema.enumMetadata().getDataStoreCode())).isTrue();
		assertThat(dto4.containsKey(zeSchema.largeTextMetadata().getDataStoreCode())).isFalse();
		assertThat(dto4.containsKey(Schemas.TITLE.getDataStoreCode())).isTrue();
		assertThat(dto4.containsKey(zeSchema.parentReferenceFromZeSchemaToZeSchema().getDataStoreCode())).isTrue();
		assertThat(dto4.containsKey(zeSchema.contentMetadata().getDataStoreCode())).isTrue();
		assertThat(dto4.containsKey(zeSchemaType.getMetadata("structMetadata").getDataStoreCode())).isFalse();

		assertThat(dto5.containsKey(Schemas.TITLE.getDataStoreCode())).isTrue();
		assertThat(dto5.containsKey(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isTrue();
		assertThat(dto5.containsKey(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isTrue();
		assertThat(dto5.containsKey(anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode())).isFalse();
		assertThat(dto5.containsKey(anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode())).isTrue();
		assertThat(dto5.containsKey(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isTrue();
		assertThat(dto5.containsKey(anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode())).isFalse();
		assertThat(dto5.containsKey(anotherSchemaType.getMetadata("textMetadata").getDataStoreCode())).isTrue();
		assertThat(dto5.containsKey(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isFalse();
		assertThat(dto5.containsKey(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode())).isFalse();
		assertThat(dto5.containsKey(anotherSchemaType.getMetadata("structureMetadata").getDataStoreCode())).isTrue();

		assertThat(dto6.containsKey(Schemas.TITLE.getDataStoreCode())).isTrue();
		assertThat(dto6.containsKey(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isFalse();
		assertThat(dto6.containsKey(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isFalse();
		assertThat(dto6.containsKey(anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode())).isTrue();
		assertThat(dto6.containsKey(anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode())).isTrue();
		assertThat(dto6.containsKey(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isFalse();
		assertThat(dto6.containsKey(anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode())).isTrue();
		assertThat(dto6.containsKey(anotherSchemaType.getMetadata("textMetadata").getDataStoreCode())).isFalse();
		assertThat(dto6.containsKey(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isTrue();
		assertThat(dto6.containsKey(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode())).isTrue();
		assertThat(dto6.containsKey(anotherSchemaType.getMetadata("structureMetadata").getDataStoreCode())).isFalse();

		// if the title is null and the content is not null the title is set to the file name of content
		assertThat(dto7.containsKey(Schemas.TITLE.getDataStoreCode())).isTrue();
		assertThat(dto7.containsKey(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isTrue();
		assertThat(dto7.containsKey(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isTrue();
		assertThat(dto7.containsKey(anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode())).isTrue();
		assertThat(dto7.containsKey(anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode())).isFalse();
		assertThat(dto7.containsKey(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isTrue();
		assertThat(dto7.containsKey(anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode())).isTrue();
		assertThat(dto7.containsKey(anotherSchemaType.getMetadata("textMetadata").getDataStoreCode())).isTrue();
		assertThat(dto7.containsKey(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isTrue();
		assertThat(dto7.containsKey(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode())).isTrue();
		assertThat(dto7.containsKey(anotherSchemaType.getMetadata("structureMetadata").getDataStoreCode())).isFalse();

		assertThat(dto8.containsKey(Schemas.TITLE.getDataStoreCode())).isFalse();
		assertThat(dto8.containsKey(anotherSchema.referenceFromAnotherSchemaToZeSchema().getDataStoreCode())).isTrue();
		assertThat(dto8.containsKey(anotherSchemaType.getMetadata("booleanMetadata").getDataStoreCode())).isTrue();
		assertThat(dto8.containsKey(anotherSchemaType.getMetadata("enumMetadata").getDataStoreCode())).isTrue();
		assertThat(dto8.containsKey(anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode())).isTrue();
		assertThat(dto8.containsKey(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isTrue();
		assertThat(dto8.containsKey(anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode())).isTrue();
		assertThat(dto8.containsKey(anotherSchemaType.getMetadata("textMetadata").getDataStoreCode())).isFalse();
		assertThat(dto8.containsKey(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isTrue();
		assertThat(dto8.containsKey(anotherSchemaType.getMetadata("contentMetadata").getDataStoreCode())).isFalse();
		assertThat(dto8.containsKey(anotherSchemaType.getMetadata("structureMetadata").getDataStoreCode())).isTrue();
	}

	@Test
	public void whenStoringSingleValueIntegerMetadataInAByteArrayRecordDTOThenStoredAndRetrievedBetweenExtremes()
			throws Exception {
		defineSchemasManager().using(setup
				.withAnIntegerMetadata(whichIsEssentialInSummary));

		init();

		setup.modify((MetadataSchemaTypesAlteration) types -> types.getSchema(anotherSchema.code())
				.create("integerMetadata").setEssentialInSummary(true)
				.setType(MetadataValueType.INTEGER));

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());

		boolean firstPass = true;
		for (int i = Integer.MIN_VALUE; i <= Integer.MAX_VALUE; i += 5000000) {
			if (!firstPass && i < 0) {
				break;
			}

			if (i > 0) {
				firstPass = false;
			}

			RecordImpl recordInt1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
					.set(zeSchema.integerMetadata(), i);

			RecordImpl recordInt2 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
					.set(anotherSchemaType.getMetadata("integerMetadata"), i);

			recordServices.execute(new Transaction(recordInt1, recordInt2));

			ByteArrayRecordDTO dtoInt1 = create(getModelLayerFactory(), recordInt1.getRecordDTO());
			ByteArrayRecordDTO dtoInt2 = create(getModelLayerFactory(), recordInt2.getRecordDTO());

			assertThat(dtoInt1.get(zeSchema.integerMetadata().getDataStoreCode())).isEqualTo(i);
			assertThat(dtoInt2.get(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isEqualTo(i);
		}

		RecordImpl recordInt1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.integerMetadata(), Integer.MAX_VALUE);

		RecordImpl recordInt2 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
				.set(anotherSchemaType.getMetadata("integerMetadata"), Integer.MAX_VALUE);

		recordServices.execute(new Transaction(recordInt1, recordInt2));

		ByteArrayRecordDTO dtoInt1 = create(getModelLayerFactory(), recordInt1.getRecordDTO());
		ByteArrayRecordDTO dtoInt2 = create(getModelLayerFactory(), recordInt2.getRecordDTO());

		assertThat(dtoInt1.get(zeSchema.integerMetadata().getDataStoreCode())).isEqualTo(Integer.MAX_VALUE);
		assertThat(dtoInt2.get(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	public void whenStoringMultivalueIntegerMetadataInAByteArrayRecordDTOThenStoredAndRetrievedBetweenExtremes()
			throws Exception {
		defineSchemasManager().using(setup
				.withAnIntegerMetadata(whichIsMultivalue, whichIsEssentialInSummary));

		init();

		setup.modify((MetadataSchemaTypesAlteration) types -> types.getSchema(anotherSchema.code())
				.create("integerMetadata").setEssentialInSummary(true)
				.setType(MetadataValueType.INTEGER)
				.setMultivalue(true));

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());

		boolean firstPass = true;
		for (int i = Integer.MIN_VALUE; i <= Integer.MAX_VALUE; i += 20000000) {
			if (!firstPass && i < 0) {
				break;
			}

			if (i > 0) {
				firstPass = false;
			}

			RecordImpl recordInt1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
					.set(zeSchema.integerMetadata(), asList(i, i + 5000, i + 10000));

			RecordImpl recordInt2 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
					.set(anotherSchemaType.getMetadata("integerMetadata"), asList(i, i + 5000, i + 10000));

			recordServices.execute(new Transaction(recordInt1, recordInt2));

			ByteArrayRecordDTO dtoInt1 = create(getModelLayerFactory(), recordInt1.getRecordDTO());
			ByteArrayRecordDTO dtoInt2 = create(getModelLayerFactory(), recordInt2.getRecordDTO());

			assertThat(dtoInt1.get(zeSchema.integerMetadata().getDataStoreCode())).isEqualTo(asList(i, i + 5000, i + 10000));
			assertThat(dtoInt2.get(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isEqualTo(asList(i, i + 5000, i + 10000));
		}

		RecordImpl recordInt1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.integerMetadata(), asList(Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE));

		RecordImpl recordInt2 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
				.set(anotherSchemaType.getMetadata("integerMetadata"), asList(Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE));

		recordServices.execute(new Transaction(recordInt1, recordInt2));

		ByteArrayRecordDTO dtoInt1 = create(getModelLayerFactory(), recordInt1.getRecordDTO());
		ByteArrayRecordDTO dtoInt2 = create(getModelLayerFactory(), recordInt2.getRecordDTO());

		assertThat(dtoInt1.get(zeSchema.integerMetadata().getDataStoreCode())).isEqualTo(asList(Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
		assertThat(dtoInt2.get(anotherSchemaType.getMetadata("integerMetadata").getDataStoreCode())).isEqualTo(asList(Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
	}

	@Test
	public void whenStoringSingleValueNumberMetadataInAByteArrayRecordDTOThenStoredAndRetrievedBetweenExtremes()
			throws Exception {
		defineSchemasManager().using(setup
				.withANumberMetadata(whichIsEssentialInSummary));

		init();

		setup.modify((MetadataSchemaTypesAlteration) types -> types.getSchema(anotherSchema.code())
				.create("numberMetadata").setEssentialInSummary(true)
				.setType(MetadataValueType.NUMBER));

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());

		boolean firstPass = true;
		for (double i = Double.MIN_VALUE; i <= Double.MAX_VALUE; i += 1E1 * i) {
			if (!firstPass && i < 0) {
				break;
			}

			if (i > 0) {
				firstPass = false;
			}

			RecordImpl recordDouble1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
					.set(zeSchema.numberMetadata(), i);

			RecordImpl recordDouble2 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
					.set(anotherSchemaType.getMetadata("numberMetadata"), i);

			recordServices.execute(new Transaction(recordDouble1, recordDouble2));

			ByteArrayRecordDTO dtoDouble1 = create(getModelLayerFactory(), recordDouble1.getRecordDTO());
			ByteArrayRecordDTO dtoDouble2 = create(getModelLayerFactory(), recordDouble2.getRecordDTO());

			assertThat(dtoDouble1.get(zeSchema.numberMetadata().getDataStoreCode())).isEqualTo(i);
			assertThat(dtoDouble2.get(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isEqualTo(i);
		}

		RecordImpl recordDouble1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.numberMetadata(), Double.MAX_VALUE);

		RecordImpl recordDouble2 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
				.set(anotherSchemaType.getMetadata("numberMetadata"), Double.MAX_VALUE);

		recordServices.execute(new Transaction(recordDouble1, recordDouble2));

		ByteArrayRecordDTO dtoDouble1 = create(getModelLayerFactory(), recordDouble1.getRecordDTO());
		ByteArrayRecordDTO dtoDouble2 = create(getModelLayerFactory(), recordDouble2.getRecordDTO());

		assertThat(dtoDouble1.get(zeSchema.numberMetadata().getDataStoreCode())).isEqualTo(Double.MAX_VALUE);
		assertThat(dtoDouble2.get(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isEqualTo(Double.MAX_VALUE);
	}

	@Test
	public void whenStoringMultivalueNumberMetadataInAByteArrayRecordDTOThenStoredAndRetrievedBetweenExtremes()
			throws Exception {
		defineSchemasManager().using(setup
				.withANumberMetadata(whichIsMultivalue, whichIsEssentialInSummary));

		init();

		setup.modify((MetadataSchemaTypesAlteration) types -> types.getSchema(anotherSchema.code())
				.create("numberMetadata").setEssentialInSummary(true)
				.setType(MetadataValueType.NUMBER)
				.setMultivalue(true));

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());

		boolean firstPass = true;
		for (double i = Double.MIN_VALUE; i <= Double.MAX_VALUE; i += 1E1 * i) {
			if (!firstPass && i < 0) {
				break;
			}

			if (i > 0) {
				firstPass = false;
			}

			RecordImpl recordDouble1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
					.set(zeSchema.numberMetadata(), asList(i, i + 11231, i + 221333));

			RecordImpl recordDouble2 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
					.set(anotherSchemaType.getMetadata("numberMetadata"), asList(i, i + 11231, i + 221333));

			recordServices.execute(new Transaction(recordDouble1, recordDouble2));

			ByteArrayRecordDTO dtoDouble1 = create(getModelLayerFactory(), recordDouble1.getRecordDTO());
			ByteArrayRecordDTO dtoDouble2 = create(getModelLayerFactory(), recordDouble2.getRecordDTO());

			assertThat(dtoDouble1.get(zeSchema.numberMetadata().getDataStoreCode())).isEqualTo(asList(i, i + 11231, i + 221333));
			assertThat(dtoDouble2.get(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isEqualTo(asList(i, i + 11231, i + 221333));
		}

		RecordImpl recordDouble1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
				.set(zeSchema.numberMetadata(), asList(Double.MAX_VALUE - 2, Double.MAX_VALUE - 1, Double.MAX_VALUE));

		RecordImpl recordDouble2 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
				.set(anotherSchemaType.getMetadata("numberMetadata"), asList(Double.MAX_VALUE - 2, Double.MAX_VALUE - 1, Double.MAX_VALUE));

		recordServices.execute(new Transaction(recordDouble1, recordDouble2));

		ByteArrayRecordDTO dtoDouble1 = create(getModelLayerFactory(), recordDouble1.getRecordDTO());
		ByteArrayRecordDTO dtoDouble2 = create(getModelLayerFactory(), recordDouble2.getRecordDTO());

		assertThat(dtoDouble1.get(zeSchema.numberMetadata().getDataStoreCode())).isEqualTo(asList(Double.MAX_VALUE - 2, Double.MAX_VALUE - 1, Double.MAX_VALUE));
		assertThat(dtoDouble2.get(anotherSchemaType.getMetadata("numberMetadata").getDataStoreCode())).isEqualTo(asList(Double.MAX_VALUE - 2, Double.MAX_VALUE - 1, Double.MAX_VALUE));
	}

	@Test
	public void whenStoringSingleValueDateMetadataInAByteArrayRecordDTOThenStoredAndRetrievedLargeRange()
			throws Exception {
		cacheIntegrityCheckedAfterTest = false;
		defineSchemasManager().using(setup
				.withADateMetadata(whichIsEssentialInSummary));

		init();

		setup.modify((MetadataSchemaTypesAlteration) types -> types.getSchema(anotherSchema.code())
				.create("dateMetadata").setEssentialInSummary(true)
				.setType(MetadataValueType.DATE));

		LocalDate date = new LocalDate(-2000, 1, 1);

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());

		while (date.getYear() < 7000) {
			RecordImpl recordDate1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
					.set(zeSchema.dateMetadata(), date);

			RecordImpl recordDate2 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
					.set(anotherSchemaType.getMetadata("dateMetadata"), date);

			recordServices.execute(new Transaction(recordDate1, recordDate2));

			ByteArrayRecordDTO dtoDouble1 = create(getModelLayerFactory(), recordDate1.getRecordDTO());
			ByteArrayRecordDTO dtoDouble2 = create(getModelLayerFactory(), recordDate2.getRecordDTO());

			assertThat(dtoDouble1.get(zeSchema.dateMetadata().getDataStoreCode())).isEqualTo(date);
			assertThat(dtoDouble2.get(anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode())).isEqualTo(date);

			date = date.plusYears(10);
		}
	}

	@Test
	public void whenStoringMultivalueDateMetadataInAByteArrayRecordDTOThenStoredAndRetrievedLargeRange()
			throws Exception {
		defineSchemasManager().using(setup
				.withADateMetadata(whichIsMultivalue, whichIsEssentialInSummary));

		init();

		setup.modify((MetadataSchemaTypesAlteration) types -> types.getSchema(anotherSchema.code())
				.create("dateMetadata").setEssentialInSummary(true)
				.setType(MetadataValueType.DATE)
				.setMultivalue(true));

		LocalDate date = new LocalDate(-2000, 1, 1);

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());

		while (date.getYear() < 7000) {
			RecordImpl recordDate1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
					.set(zeSchema.dateMetadata(), asList(date.plusDays(1), date.plusDays(2), date.plusDays(3)));

			RecordImpl recordDate2 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
					.set(anotherSchemaType.getMetadata("dateMetadata"), asList(date.plusDays(1), date.plusDays(2), date.plusDays(3)));

			recordServices.execute(new Transaction(recordDate1, recordDate2));

			ByteArrayRecordDTO dtoDate1 = create(getModelLayerFactory(), recordDate1.getRecordDTO());
			ByteArrayRecordDTO dtoDate2 = create(getModelLayerFactory(), recordDate2.getRecordDTO());

			assertThat(dtoDate1.get(zeSchema.dateMetadata().getDataStoreCode())).isEqualTo(asList(date.plusDays(1), date.plusDays(2), date.plusDays(3)));
			assertThat(dtoDate2.get(anotherSchemaType.getMetadata("dateMetadata").getDataStoreCode())).isEqualTo(asList(date.plusDays(1), date.plusDays(2), date.plusDays(3)));

			date = date.plusYears(10);
		}
	}

	@Test
	public void whenStoringSinglevalueDateTimeMetadataInAByteArrayRecordDTOThenStoredAndRetrievedLargeRange()
			throws Exception {
		cacheIntegrityCheckedAfterTest = false;
		defineSchemasManager().using(setup
				.withADateTimeMetadata(whichIsEssentialInSummary));

		init();

		setup.modify((MetadataSchemaTypesAlteration) types -> types.getSchema(anotherSchema.code())
				.create("dateTimeMetadata").setEssentialInSummary(true)
				.setType(MetadataValueType.DATE_TIME));

		LocalDateTime dateTime = new LocalDateTime(-2000, 1, 1, 0, 0);

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());

		while (dateTime.getYear() < 7000) {
			RecordImpl recordDateTime1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
					.set(zeSchema.dateTimeMetadata(), dateTime);

			RecordImpl recordDateTime2 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
					.set(anotherSchemaType.getMetadata("dateTimeMetadata"), dateTime);

			recordServices.execute(new Transaction(recordDateTime1, recordDateTime2));

			ByteArrayRecordDTO dtoDateTime1 = create(getModelLayerFactory(), recordDateTime1.getRecordDTO());
			ByteArrayRecordDTO dtoDateTime2 = create(getModelLayerFactory(), recordDateTime2.getRecordDTO());

			assertThat(dtoDateTime1.get(zeSchema.dateTimeMetadata().getDataStoreCode())).isEqualTo(dateTime);
			assertThat(dtoDateTime2.get(anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode())).isEqualTo(dateTime);

			dateTime = dateTime.plusYears(10);
		}
	}

	@Test
	public void whenStoringMultivalueDateTimeMetadataInAByteArrayRecordDTOThenStoredAndRetrievedLargeRange()
			throws Exception {
		cacheIntegrityCheckedAfterTest = false;
		defineSchemasManager().using(setup
				.withADateTimeMetadata(whichIsMultivalue, whichIsEssentialInSummary));

		init();

		setup.modify((MetadataSchemaTypesAlteration) types -> types.getSchema(anotherSchema.code())
				.create("dateTimeMetadata").setEssentialInSummary(true)
				.setType(MetadataValueType.DATE_TIME)
				.setMultivalue(true));

		LocalDateTime dateTime = new LocalDateTime(-2000, 1, 1, 0, 0);

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(zeCollection);
		MetadataSchema anotherSchemaType = schemaTypes.getSchema(anotherSchema.code());

		while (dateTime.getYear() < 7000) {
			RecordImpl recordDateTime1 = (RecordImpl) recordServices.newRecordWithSchema(zeSchema.instance())
					.set(zeSchema.dateTimeMetadata(), asList(dateTime, dateTime.plusDays(1), dateTime.plusDays(2)));

			RecordImpl recordDateTime2 = (RecordImpl) recordServices.newRecordWithSchema(anotherSchema.instance())
					.set(anotherSchemaType.getMetadata("dateTimeMetadata"), asList(dateTime, dateTime.plusDays(1), dateTime.plusDays(2)));

			recordServices.execute(new Transaction(recordDateTime1, recordDateTime2));

			ByteArrayRecordDTO dtoDateTime1 = create(getModelLayerFactory(), recordDateTime1.getRecordDTO());
			ByteArrayRecordDTO dtoDateTime2 = create(getModelLayerFactory(), recordDateTime2.getRecordDTO());

			assertThat(dtoDateTime1.get(zeSchema.dateTimeMetadata().getDataStoreCode())).isEqualTo(asList(dateTime, dateTime.plusDays(1), dateTime.plusDays(2)));
			assertThat(dtoDateTime2.get(anotherSchemaType.getMetadata("dateTimeMetadata").getDataStoreCode())).isEqualTo(asList(dateTime, dateTime.plusDays(1), dateTime.plusDays(2)));

			dateTime = dateTime.plusYears(10);
		}
	}

	@Test
	public void whenStoringDataUsingCompactShortThenOK()
			throws Exception {
		Toggle.DEBUG_DTOS.disable();
		Metadata metadata = mock(Metadata.class);
		for (short v = Short.MIN_VALUE; v < Short.MAX_VALUE; v++) {
			DTOUtilsByteArrayDataOutputStream stream = new DTOUtilsByteArrayDataOutputStream(false, null);
			stream.writeLong(metadata, 0L);
			stream.writeCompactedShortFromByteArray_1_2_4(metadata, v);
			stream.writeLong(metadata, 12L);
			byte[] bytes = stream.toByteArray();
			CompactedShort value = CacheRecordDTOUtils.parseCompactedShortFromByteArray_1_2_4(bytes, 8);
			assertThat(value.value).isEqualTo(v);
			assertThat(value.length).isEqualTo(bytes.length - 16);
		}


	}

	@Test
	public void whenStoringDataUsingCompactIntThenOK()
			throws Exception {

		Toggle.DEBUG_DTOS.disable();
		Metadata metadata = mock(Metadata.class);
		for (int v = -1; v < 5_000_000; v++) {
			DTOUtilsByteArrayDataOutputStream stream = new DTOUtilsByteArrayDataOutputStream(false, null);
			stream.writeLong(metadata, 0L);
			stream.writeCompactedIntFromByteArray_2_4_8(metadata, v);
			stream.writeLong(metadata, 12L);
			byte[] bytes = stream.toByteArray();
			CompactedInt value = CacheRecordDTOUtils.parseCompactedIntFromByteArray_2_4_8(bytes, 8);
			assertThat(value.value).isEqualTo(v);
			assertThat(value.length).describedAs("" + v).isEqualTo(bytes.length - 16);
		}


	}

	private Map<String, Object> toMap(Set<Entry<String, Object>> dtoEntrySet) {
		Map<String, Object> mapFromSet = new HashMap<>();

		for (Entry<String, Object> entry : dtoEntrySet) {
			mapFromSet.put(entry.getKey(), entry.getValue());
		}

		return mapFromSet;
	}

	public static ByteArrayRecordDTO create(ModelLayerFactory modelLayerFactory, RecordDTO dto) {

		if (dto.getLoadingMode() == RecordDTOMode.CUSTOM) {
			throw new IllegalStateException("Cannot create ByteArrayRecordDTO from a customly loaded RecordDTO");
		}

		String collection = (String) dto.getFields().get("collection_s");
		String schemaCode = (String) dto.getFields().get("schema_s");
		short instanceId = modelLayerFactory.getInstanceId();
		MetadataSchemaType type = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchemaType(SchemaUtils.getSchemaTypeCode(schemaCode));

		MetadataSchemaProvider schemaProvider = modelLayerFactory.getMetadataSchemasManager();

		MetadataSchema schema = type.getSchema(schemaCode);
		CollectionInfo collectionInfo = schema.getCollectionInfo();

		//TODO Handle Holder
		CacheRecordDTOBytesArray bytesArray = convertDTOToByteArrays(dto, schema);

		int intId = RecordUtils.toIntKey(dto.getId());

		if (intId == RecordUtils.KEY_IS_NOT_AN_INT) {
			if (bytesArray.bytesToPersist != null && bytesArray.bytesToPersist.length > 0) {
				SummaryCacheSingletons.dataStore.get(instanceId).saveStringKey(dto.getId(), bytesArray.bytesToPersist);
			} else {
				//SummaryCacheSingletons.dataStore.removeStringKey(dto.getId());
			}
			return new ByteArrayRecordDTOWithStringId(dto.getId(), schemaProvider, dto.getVersion(), dto.getLoadingMode() == SUMMARY,
					instanceId, collectionInfo.getCode(), collectionInfo.getCollectionId(), type.getCode(), type.getId(),
					schema.getCode(), schema.getId(), bytesArray.bytesToKeepInMemory);
		} else {
			if (bytesArray.bytesToPersist != null && bytesArray.bytesToPersist.length > 0) {
				ByteArrayRecordDTOWithIntegerId byteArrayRecordDTO = new ByteArrayRecordDTOWithIntegerId(intId, schemaProvider, dto.getVersion(), dto.getLoadingMode() == SUMMARY,
						instanceId, collectionInfo.getCode(), collectionInfo.getCollectionId(),
						type.getCode(), type.getId(), schema.getCode(), schema.getId(), bytesArray.bytesToKeepInMemory);
				SummaryCacheSingletons.dataStore.get(instanceId).saveIntKeyPersistedAndMemoryData(intId, bytesArray.bytesToPersist, byteArrayRecordDTO);
			} else {
				//SummaryCacheSingletons.dataStore.removeIntKey(intId);
			}
			return new ByteArrayRecordDTOWithIntegerId(intId, schemaProvider, dto.getVersion(), dto.getLoadingMode() == SUMMARY,
					instanceId, collectionInfo.getCode(), collectionInfo.getCollectionId(),
					type.getCode(), type.getId(), schema.getCode(), schema.getId(), bytesArray.bytesToKeepInMemory);
		}

	}
}
