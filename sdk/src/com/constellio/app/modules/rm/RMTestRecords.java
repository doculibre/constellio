package com.constellio.app.modules.rm;

import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.services.logging.DecommissioningLoggingService;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static com.constellio.app.modules.rm.constants.RMRoles.MANAGER;
import static com.constellio.app.modules.rm.constants.RMRoles.RGD;
import static com.constellio.app.modules.rm.constants.RMRoles.USER;
import static com.constellio.app.modules.rm.model.enums.CopyType.PRINCIPAL;
import static com.constellio.app.modules.rm.model.enums.CopyType.SECONDARY;
import static com.constellio.app.modules.rm.model.enums.DecommissioningListType.DOCUMENTS_TO_DEPOSIT;
import static com.constellio.app.modules.rm.model.enums.DecommissioningListType.DOCUMENTS_TO_DESTROY;
import static com.constellio.app.modules.rm.model.enums.DecommissioningListType.DOCUMENTS_TO_TRANSFER;
import static com.constellio.app.modules.rm.model.enums.DecommissioningListType.FOLDERS_TO_CLOSE;
import static com.constellio.app.modules.rm.model.enums.DecommissioningListType.FOLDERS_TO_DEPOSIT;
import static com.constellio.app.modules.rm.model.enums.DecommissioningListType.FOLDERS_TO_DESTROY;
import static com.constellio.app.modules.rm.model.enums.DecommissioningListType.FOLDERS_TO_TRANSFER;
import static com.constellio.app.modules.rm.model.enums.DecommissioningType.DEPOSIT;
import static com.constellio.app.modules.rm.model.enums.DecommissioningType.DESTRUCTION;
import static com.constellio.app.modules.rm.model.enums.DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;

public class RMTestRecords {

	public CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	public String categoryId_Z = "categoryId_Z";
	public String categoryId_Z100 = "categoryId_Z100";
	public String categoryId_Z110 = "categoryId_Z110";
	public String categoryId_Z111 = "categoryId_Z111";
	public String categoryId_Z112 = "categoryId_Z112";
	public String categoryId_Z120 = "categoryId_Z120";
	public String categoryId_Z200 = "categoryId_Z200";
	public String categoryId_ZE42 = "categoryId_ZE42";
	public String categoryId_Z999 = "categoryId_Z999";

	public String categoryId_X = "categoryId_X";
	public String categoryId_X100 = "categoryId_X100";
	public String categoryId_X110 = "categoryId_X110";
	public String categoryId_X120 = "categoryId_X120";
	public String categoryId_X13 = "categoryId_X13";

	public String unitId_10 = "unitId_10";
	public String unitId_10a = "unitId_10a";
	public String unitId_11 = "unitId_11";
	public String unitId_11b = "unitId_11b";
	public String unitId_12 = "unitId_12";
	public String unitId_12b = "unitId_12b";
	public String unitId_12c = "unitId_12c";
	public String unitId_20 = "unitId_20";
	public String unitId_20d = "unitId_20d";
	public String unitId_20e = "unitId_20e";
	public String unitId_30 = "unitId_30";
	public String unitId_30c = "unitId_30c";

	public String subdivId_1 = "subdivId_1";
	public String subdivId_2 = "subdivId_2";
	public String subdivId_3 = "subdivId_3";

	public String documentTypeId_1 = "documentTypeId_1";
	public String documentTypeId_2 = "documentTypeId_2";
	public String documentTypeId_3 = "documentTypeId_3";
	public String documentTypeId_4 = "documentTypeId_4";
	public String documentTypeId_5 = "documentTypeId_5";
	public String documentTypeId_6 = "documentTypeId_6";
	public String documentTypeId_7 = "documentTypeId_7";
	public String documentTypeId_8 = "documentTypeId_8";
	public String documentTypeId_9 = "documentTypeId_9";
	public String documentTypeId_10 = "documentTypeId_10";

	public String ruleId_1 = "ruleId_1";
	public String ruleId_2 = "ruleId_2";
	public String ruleId_3 = "ruleId_3";
	public String ruleId_4 = "ruleId_4";
	public String ruleId_5 = "ruleId_5";
	public String ruleId_6 = "ruleId_6";
	public String principal42_5_CId;

	public String storageSpaceId_S01 = "S01";
	public String storageSpaceId_S01_01 = "S01-01";
	public String storageSpaceId_S01_02 = "S01-02";
	public String storageSpaceId_S02 = "S02";
	public String storageSpaceId_S02_01 = "S02-01";
	public String storageSpaceId_S02_02 = "S02-02";

	public String containerTypeId_boite22x22 = "boite22x22";

	public String containerId_bac19 = "bac19";
	public String containerId_bac18 = "bac18";
	public String containerId_bac17 = "bac17";
	public String containerId_bac16 = "bac16";
	public String containerId_bac15 = "bac15";
	public String containerId_bac14 = "bac14";

	public String containerId_bac13 = "bac13";
	public String containerId_bac12 = "bac12";
	public String containerId_bac11 = "bac11";
	public String containerId_bac10 = "bac10";
	public String containerId_bac09 = "bac09";
	public String containerId_bac08 = "bac08";
	public String containerId_bac07 = "bac07";
	public String containerId_bac06 = "bac06";
	public String containerId_bac05 = "bac05";
	public String containerId_bac04 = "bac04";
	public String containerId_bac03 = "bac03";
	public String containerId_bac02 = "bac02";
	public String containerId_bac01 = "bac01";

	public String PA;
	public String MV;
	public String MD;
	public List<String> PA_MD;
	public String folder_A01 = "A01";
	public String folder_A02 = "A02";
	public String folder_A03 = "A03";
	public String folder_A04 = "A04";
	public String folder_A05 = "A05";
	public String folder_A06 = "A06";
	public String folder_A07 = "A07";
	public String folder_A08 = "A08";
	public String folder_A09 = "A09";
	public String folder_A10 = "A10";
	public String folder_A11 = "A11";
	public String folder_A12 = "A12";
	public String folder_A13 = "A13";
	public String folder_A14 = "A14";
	public String folder_A15 = "A15";
	public String folder_A16 = "A16";
	public String folder_A17 = "A17";
	public String folder_A18 = "A18";
	public String folder_A19 = "A19";
	public String folder_A20 = "A20";
	public String folder_A21 = "A21";
	public String folder_A22 = "A22";
	public String folder_A23 = "A23";
	public String folder_A24 = "A24";
	public String folder_A25 = "A25";
	public String folder_A26 = "A26";
	public String folder_A27 = "A27";
	public String folder_A42 = "A42";
	public String folder_A43 = "A43";
	public String folder_A44 = "A44";
	public String folder_A45 = "A45";
	public String folder_A46 = "A46";
	public String folder_A47 = "A47";
	public String folder_A48 = "A48";
	public String folder_A49 = "A49";
	public String folder_A50 = "A50";
	public String folder_A51 = "A51";
	public String folder_A52 = "A52";
	public String folder_A53 = "A53";
	public String folder_A54 = "A54";
	public String folder_A55 = "A55";
	public String folder_A56 = "A56";
	public String folder_A57 = "A57";
	public String folder_A58 = "A58";
	public String folder_A59 = "A59";
	public String folder_A79 = "A79";
	public String folder_A80 = "A80";
	public String folder_A81 = "A81";
	public String folder_A82 = "A82";
	public String folder_A83 = "A83";
	public String folder_A84 = "A84";
	public String folder_A85 = "A85";
	public String folder_A86 = "A86";
	public String folder_A87 = "A87";
	public String folder_A88 = "A88";
	public String folder_A89 = "A89";
	public String folder_A90 = "A90";
	public String folder_A91 = "A91";
	public String folder_A92 = "A92";
	public String folder_A93 = "A93";
	public String folder_A94 = "A94";
	public String folder_A95 = "A95";
	public String folder_A96 = "A96";
	public String folder_B01 = "B01";
	public String folder_B02 = "B02";
	public String folder_B03 = "B03";
	public String folder_B04 = "B04";
	public String folder_B05 = "B05";
	public String folder_B06 = "B06";
	public String folder_B07 = "B07";
	public String folder_B08 = "B08";
	public String folder_B09 = "B09";
	public String folder_B30 = "B30";
	public String folder_B31 = "B31";
	public String folder_B32 = "B32";
	public String folder_B33 = "B33";
	public String folder_B34 = "B34";
	public String folder_B35 = "B35";
	public String folder_B50 = "B50";
	public String folder_B51 = "B51";
	public String folder_B52 = "B52";
	public String folder_B53 = "B53";
	public String folder_B54 = "B54";
	public String folder_B55 = "B55";
	public String folder_C01 = "C01";
	public String folder_C02 = "C02";
	public String folder_C03 = "C03";
	public String folder_C04 = "C04";
	public String folder_C05 = "C05";
	public String folder_C06 = "C06";
	public String folder_C07 = "C07";
	public String folder_C08 = "C08";
	public String folder_C09 = "C09";
	public String folder_C30 = "C30";
	public String folder_C31 = "C31";
	public String folder_C32 = "C32";
	public String folder_C33 = "C33";
	public String folder_C34 = "C34";
	public String folder_C35 = "C35";
	public String folder_C50 = "C50";
	public String folder_C51 = "C51";
	public String folder_C52 = "C52";
	public String folder_C53 = "C53";
	public String folder_C54 = "C54";
	public String folder_C55 = "C55";

	public String document_A19 = "docA19";
	public String document_A49 = "docA49";
	public String document_B30 = "docB30";
	public String document_B33 = "docB33";
	public String document_A79 = "docA79";

	public String folder_A01_documentWithSameCopy = "doc_A01_same_copy";
	public String folder_A01_documentWithDifferentCopy = "A02";
	public String folder_A02_documentWithSameCopy = "A03";
	public String folder_A02_documentWithDifferentCopy = "A04";
	public String folder_A03_documentWithDifferentCopy = "A05";
	public String folder_A03_documentWithSameCopy = "A06";
	//	public final String folder_A04_documentWithSameCopy = "A07";
	//	public final String folder_A04_documentWithSameCopy = "A08";
	//	public final String folder_A05_documentWithSameCopy = "A09";
	//	public final String folder_A05_documentWithSameCopy = "A10";

	public String list_01 = "list01";
	public String list_02 = "list02";
	public String list_03 = "list03";
	public String list_04 = "list04";
	public String list_05 = "list05";
	public String list_06 = "list06";
	public String list_07 = "list07";
	public String list_08 = "list08";
	public String list_09 = "list09";
	public String list_10 = "list10";
	public String list_11 = "list11";
	public String list_12 = "list12";
	public String list_13 = "list13";
	public String list_14 = "list14";
	public String list_15 = "list15";
	public String list_16 = "list16";
	public String list_17 = "list17";
	public String list_18 = "list18";
	public String list_19 = "list19";
	public String list_20 = "list20";
	public String list_21 = "list21";
	public String list_22 = "list22";
	public String list_23 = "list23";
	public String list_24 = "list24";
	public String list_25 = "list25";
	public String list_26 = "list26";

	public String list_30 = "list30";
	public String list_31 = "list31";
	public String list_32 = "list32";
	public String list_33 = "list33";
	public String list_34 = "list34";
	public String list_35 = "list35";
	public String list_36 = "list36";

	private String collection;
	private TasksSchemasRecordsServices tasks;
	private RMSchemasRecordsServices rm;
	private String alice_userWithNoWriteAccess;
	private String admin_userIdWithAllAccess;
	private String bob_userInAC;
	private String charles_userInA;
	private String dakota_managerInA_userInB;
	private String edouard_managerInB_userInC;
	private String gandalf_managerInABC;
	private String chuckNorris;
	private String robin;
	private String sasquatch;
	private Users users = new Users();
	private RecordServices recordServices;
	private LoggingServices loggingServices;
	private DecommissioningLoggingService decommissioningLoggingService;
	private SystemConfigurationsManager systemConfigurationsManager;
	private SearchServices searchServices;
	private RMEventsSearchServices rmEventsSearchServices;
	private ContentManager contentManager;
	private ModelLayerFactory modelLayerFactory;
	private AppLayerFactory appLayerFactory;

	private boolean developperFriendlyIds = true;

	public RMTestRecords(String collection) {
		this.collection = collection;
	}

	@Deprecated
	public RMTestRecords setDevelopperFriendlyIds(boolean developperFriendlyIds) {
		this.developperFriendlyIds = developperFriendlyIds;
		return this;
	}

	public RMTestRecords alreadySettedUp(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		users.setUp(userServices);

		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		tasks = new TasksSchemasRecordsServices(collection, appLayerFactory);
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		List<Record> userRecords = searchServices.search(new LogicalSearchQuery()
				.setCondition(from(rm.userSchemaType()).returnAll())
				.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(rm.userUsername())));

		for (User user : rm.wrapUsers(userRecords)) {
			if (user.getUsername().equals("admin")) {
				admin_userIdWithAllAccess = user.getId();

			} else if (user.getUsername().equals("chuck")) {
				chuckNorris = user.getId();

			} else if (user.getUsername().equals("alice")) {
				alice_userWithNoWriteAccess = user.getId();

			} else if (user.getUsername().equals("bob")) {
				bob_userInAC = user.getId();

			} else if (user.getUsername().equals("charles")) {
				charles_userInA = user.getId();

			} else if (user.getUsername().equals("dakota")) {
				dakota_managerInA_userInB = user.getId();

			} else if (user.getUsername().equals("edouard")) {
				edouard_managerInB_userInC = user.getId();

			} else if (user.getUsername().equals("gandalf")) {
				gandalf_managerInABC = user.getId();

			} else if (user.getUsername().equals("robin")) {
				robin = user.getId();

			} else if (user.getUsername().equals("sasquatch")) {
				sasquatch = user.getId();

			}
		}

		recordServices = modelLayerFactory.newRecordServices();
		loggingServices = modelLayerFactory.newLoggingServices();
		decommissioningLoggingService = new DecommissioningLoggingService(modelLayerFactory);

		searchServices = modelLayerFactory.newSearchServices();
		rmEventsSearchServices = new RMEventsSearchServices(modelLayerFactory, collection);

		contentManager = modelLayerFactory.getContentManager();
		systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();

		PA = rm.PA();
		MD = rm.DM();
		MV = rm.FI();
		PA_MD = asList(PA, MD);

		return this;
	}

	public RMTestRecords setup(AppLayerFactory appLayerFactory)
			throws RecordServicesException {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		users.setUp(userServices).withPasswords(modelLayerFactory.newAuthenticationService());
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		tasks = new TasksSchemasRecordsServices(collection, appLayerFactory);
		recordServices = modelLayerFactory.newRecordServices();
		loggingServices = modelLayerFactory.newLoggingServices();
		decommissioningLoggingService = new DecommissioningLoggingService(modelLayerFactory);
		searchServices = modelLayerFactory.newSearchServices();
		rmEventsSearchServices = new RMEventsSearchServices(modelLayerFactory, collection);
		contentManager = modelLayerFactory.getContentManager();
		systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();

		PA = rm.PA();
		MD = rm.DM();
		MV = rm.FI();
		PA_MD = asList(PA, MD);

		systemConfigurationsManager.setValue(RMConfigs.ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER, false);

		Transaction transaction = new Transaction();
		setupUsers(transaction, userServices);
		setupCategories(transaction);
		setupUniformSubdivisions(transaction);
		setupAdministrativeUnits(transaction);
		setupDocumentTypes(transaction);
		setupRetentionRules(transaction);
		setupTypes(transaction);
		recordServices.execute(transaction);

		setupAdministrativeUnitsAuthorizations();
		waitForBatchProcesses(modelLayerFactory.getBatchProcessesManager());

		return this;
	}

	private void setupAdministrativeUnitsAuthorizations() {

		List<String> userInUnit10 = asList(bob_userInAC, charles_userInA, admin_userIdWithAllAccess);
		List<String> managerInUnit10 = asList(dakota_managerInA_userInB, gandalf_managerInABC);

		List<String> userInUnit11 = asList(dakota_managerInA_userInB, admin_userIdWithAllAccess);
		List<String> managerInUnit11 = asList(edouard_managerInB_userInC, gandalf_managerInABC);

		List<String> userInUnit12 = asList(dakota_managerInA_userInB, admin_userIdWithAllAccess, edouard_managerInB_userInC,
				bob_userInAC, admin_userIdWithAllAccess);
		List<String> managerInUnit12 = asList(edouard_managerInB_userInC, gandalf_managerInABC);

		List<String> userInUnit20 = new ArrayList<>();
		List<String> managerInUnit20 = new ArrayList<>();

		List<String> userInUnit30 = asList(edouard_managerInB_userInC, bob_userInAC, admin_userIdWithAllAccess);
		List<String> managerInUnit30 = asList(gandalf_managerInABC);

		addUserAuthorization(unitId_10, userInUnit10);
		addManagerAuthorization(unitId_10, managerInUnit10);

		addUserAuthorization(unitId_11, userInUnit11);
		addManagerAuthorization(unitId_11, managerInUnit11);

		addUserAuthorization(unitId_12, userInUnit12);
		addManagerAuthorization(unitId_12, managerInUnit12);

		addUserAuthorization(unitId_20, userInUnit20);
		addManagerAuthorization(unitId_20, managerInUnit20);

		addUserAuthorization(unitId_30, userInUnit30);
		addManagerAuthorization(unitId_30, managerInUnit30);
	}

	private void addUserAuthorization(String target, List<String> principals) {
		addAuthorization(asList(RMRoles.USER), target, principals);
		addAuthorization(asList(Role.READ, Role.WRITE), target, principals);
	}

	private void addManagerAuthorization(String target, List<String> principals) {
		addAuthorization(asList(RMRoles.MANAGER), target, principals);
		addAuthorization(asList(Role.READ, Role.WRITE, Role.DELETE), target, principals);
	}

	private void addAuthorization(List<String> roles, String target, List<String> principals) {
		if (!principals.isEmpty()) {
			modelLayerFactory.newAuthorizationsServices()
					.add(authorizationInCollection(collection).forPrincipalsIds(principals).on(target).giving(roles));
		}
	}

	public RMTestRecords withFoldersAndContainersOfEveryStatus() {
		// Calculation of closing date is disabled because we want some folders
		// without close date
		systemConfigurationsManager.setValue(DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.ASYNC_PARSING_FOR_ALL_CONTENTS);
		systemConfigurationsManager.setValue(RMConfigs.CALCULATED_CLOSING_DATE, false);
		systemConfigurationsManager.setValue(RMConfigs.YEAR_END_DATE, "10/31");

		Transaction transaction = new Transaction();
		setupStorageSpace(transaction);
		setupContainerTypes(transaction);
		setupContainers(transaction);
		setupFolders(transaction);
		setupLists(transaction);
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		waitForBatchProcesses(modelLayerFactory.getBatchProcessesManager());
		return this;
	}

	public RMTestRecords withDocumentsHavingContent() {
		Transaction transaction = new Transaction();
		setupDocumentsWithContent(transaction);
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		return this;
	}

	public RMTestRecords withEvents() {
		createRecordsEvents();
		createUsersEvents();
		createGroupsEvents();
		createViewEvents();
		createDecommissioningEvents();
		createBorrowAndReturnEvents();
		createLoginEvents();
		recordServices.flush();
		return this;
	}

	private void setupTypes(Transaction transaction) {

		modelLayerFactory.getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {

				MetadataSchemaBuilder employeFolderSchema = types.getSchemaType("folder").createCustomSchema("employe");
				MetadataSchemaBuilder meetingFolderSchema = types.getSchemaType("folder").createCustomSchema("meetingFolder");

				MetadataSchemaBuilder formDocumentSchema = types.getSchemaType("document").createCustomSchema("form");
				MetadataSchemaBuilder reportDocumentSchema = types.getSchemaType("document").createCustomSchema("report");

				MetadataSchemaBuilder criticalTaskSchema = types.getSchemaType("userTask").createCustomSchema("criticalTask");
				MetadataSchemaBuilder communicationTaskSchema = types.getSchemaType("userTask")
						.createCustomSchema("communicationTask");

				employeFolderSchema.create("subType").setType(STRING).setDefaultValue("Dossier d'employé général");
				employeFolderSchema.create("hireDate").setType(MetadataValueType.DATE)
						.setDefaultValue(new LocalDate(2010, 12, 20));

				meetingFolderSchema.create("subType").setType(STRING).setDefaultValue("Meeting important");
				meetingFolderSchema.create("meetingDateTime").setType(MetadataValueType.DATE_TIME)
						.setDefaultValue(new LocalDateTime(2010, 12, 20, 1, 2, 3, 4));

				formDocumentSchema.create("subType").setType(STRING).setDefaultValue("Permit A-38 Form");
				formDocumentSchema.create("receivedDate").setType(MetadataValueType.DATE)
						.setDefaultValue(new LocalDate(2011, 12, 21));

				reportDocumentSchema.create("subType").setType(STRING).setDefaultValue("Financial report");
				reportDocumentSchema.create("receivedDate").setType(MetadataValueType.DATE)
						.setDefaultValue(new LocalDate(2012, 12, 22));

				criticalTaskSchema.create("subType").setType(STRING).setDefaultValue("Dû pour hier");

				communicationTaskSchema.create("subType").setType(STRING).setDefaultValue("Envoie d'un courriel");
				communicationTaskSchema.create("email").setType(STRING).setDefaultValue("dakota.indien@gmail.com");
				communicationTaskSchema.create("phone").setType(STRING);

			}
		});

		transaction.add(rm.newFolderType().setCode("employe").setTitle("Dossier employé").setLinkedSchema("folder_employe"));
		transaction.add(rm.newFolderType().setCode("meetingFolder").setTitle("Réunion employé")
				.setLinkedSchema("folder_meetingFolder"));
		transaction.add(rm.newFolderType().setCode("other").setTitle("Autre"));

		transaction.add(rm.newDocumentType().setCode("form").setTitle("Formulaire").setLinkedSchema("document_form"));
		transaction.add(rm.newDocumentType().setCode("report").setTitle("Rapport").setLinkedSchema("document_report"));
		transaction.add(rm.newDocumentType().setCode("other").setTitle("Autre"));

		transaction.add(tasks.newTaskType().setCode("criticalTask").setTitle("Tâche critique")
				.setLinkedSchema("userTask_criticalTask"));
		transaction.add(tasks.newTaskType().setCode("communicationTask").setTitle("Communication")
				.setLinkedSchema("userTask_communicationTask"));
		transaction.add(tasks.newTaskType().setCode("other").setTitle("Autre"));
	}

	private void setupDocumentTypes(Transaction transaction) {
		transaction.add(rm.newDocumentTypeWithId(documentTypeId_1).setCode("1").setTitle("Livre de recettes"));
		transaction.add(rm.newDocumentTypeWithId(documentTypeId_2).setCode("2").setTitle("Typologie"));
		transaction.add(rm.newDocumentTypeWithId(documentTypeId_3).setCode("3").setTitle("Petit guide"));
		transaction.add(rm.newDocumentTypeWithId(documentTypeId_4).setCode("4").setTitle("Histoire"));
		transaction.add(rm.newDocumentTypeWithId(documentTypeId_5).setCode("5").setTitle("Calendrier des réunions"));
		transaction.add(rm.newDocumentTypeWithId(documentTypeId_6).setCode("6").setTitle(
				"Dossier de réunion : avis de convocation, ordre du jour, procès-verbal, extraits de procès-verbaux, résolutions, documents déposés, correspondance"));
		transaction.add(rm.newDocumentTypeWithId(documentTypeId_7).setCode("7").setTitle("Notes de réunion"));
		transaction.add(rm.newDocumentTypeWithId(documentTypeId_8).setCode("8")
				.setTitle("Dossiers des administrateurs : affirmations solennelles, serments de discrétion"));
		transaction.add(rm.newDocumentTypeWithId(documentTypeId_9).setCode("9").setTitle("Contrat"));
		transaction.add(rm.newDocumentTypeWithId(documentTypeId_10).setCode("10").setTitle("Procès-verbal"));
	}

	private void waitForBatchProcesses(BatchProcessesManager batchProcessesManager) {
		for (BatchProcess batchProcess : batchProcessesManager.getAllNonFinishedBatchProcesses()) {
			batchProcessesManager.waitUntilFinished(batchProcess);
		}
	}

	private void setupUsers(Transaction transaction, UserServices userServices) {
		userServices.addUserToCollection(users.admin(), collection);
		userServices.addUserToCollection(users.alice(), collection);
		userServices.addUserToCollection(users.bob(), collection);
		userServices.addUserToCollection(users.charles(), collection);
		userServices.addUserToCollection(users.dakotaLIndien(), collection);
		userServices.addUserToCollection(users.edouardLechat(), collection);
		userServices.addUserToCollection(users.gandalfLeblanc(), collection);
		userServices.addUserToCollection(users.chuckNorris(), collection);

		admin_userIdWithAllAccess = transaction.add(users.adminIn(collection))
				.setCollectionAllAccess(true).setUserRoles(RGD).getId();
		alice_userWithNoWriteAccess = transaction.add(users.aliceIn(collection).setCollectionReadAccess(true)).getId();
		bob_userInAC = transaction.add(users.bobIn(collection).setUserRoles(USER)).getId();
		charles_userInA = transaction.add(users.charlesIn(collection).setUserRoles(USER)).getId();
		dakota_managerInA_userInB = transaction.add(users.dakotaLIndienIn(collection).setUserRoles(MANAGER)).getId();
		edouard_managerInB_userInC = transaction.add(users.edouardLechatIn(collection).setUserRoles(USER)).getId();
		gandalf_managerInABC = transaction.add(users.gandalfLeblancIn(collection).setUserRoles(MANAGER)).getId();
		chuckNorris = transaction.add(users.chuckNorrisIn(collection).setCollectionAllAccess(true).setUserRoles(RGD)).getId();

	}

	private void setupCategories(Transaction transaction) {

		categoryId_X = transaction.add(newCategory(categoryId_X).setCode("X")
				.setTitle("Xe category").setTitle(Locale.ENGLISH, "Xe category"))
				.setDescription("Ze ultimate category X")
				.setRetentionRules(asList(ruleId_1)).getId();

		categoryId_X100 = transaction.add(newCategory(categoryId_X100).setCode("X100").setTitle("X100")
				.setDescription("Ze category X100")
				.setParent(categoryId_X).setRetentionRules(asList(ruleId_1))).getId();

		categoryId_X110 = transaction.add(newCategory(categoryId_X110).setCode("X110").setTitle("X110")
				.setDescription("Ze category X110")
				.setParent(categoryId_X100).setRetentionRules(asList(ruleId_1, ruleId_2))).getId();

		categoryId_X120 = transaction.add(newCategory(categoryId_X120).setCode("X120").setTitle("X120")
				.setDescription("Ze category X120")
				.setParent(categoryId_X100).setRetentionRules(asList(ruleId_3, ruleId_4))).getId();

		categoryId_X13 = transaction.add(newCategory(categoryId_X13).setCode("X13").setTitle("Agent Secreet")
				.setTitle(Locale.ENGLISH, "Secret agent")
				.setDescription("218. Requiem pour un espion").setParent(categoryId_X)
				.setRetentionRules(asList(ruleId_1, ruleId_2, ruleId_3, ruleId_4))).getId();

		categoryId_Z = transaction.add(newCategory(categoryId_Z).setCode("Z").setTitle("Ze category")
				.setTitle(Locale.ENGLISH, "The category").setDescription("Ze ultimate category Z")).getId();

		categoryId_Z100 = transaction.add(newCategory(categoryId_Z100).setCode("Z100").setTitle("Z100")
				.setDescription("Ze category Z100")
				.setParent(categoryId_Z)).getId();

		categoryId_Z110 = transaction.add(newCategory(categoryId_Z110).setCode("Z110").setTitle("Z110")
				.setDescription("Ze category Z110")
				.setParent(categoryId_Z100)).setRetentionRules(asList(ruleId_2)).getId();

		categoryId_Z111 = transaction.add(newCategory(categoryId_Z111).setCode("Z111").setTitle("Z111")
				.setDescription("Ze category Z111")
				.setParent(categoryId_Z110)).getId();

		categoryId_Z112 = transaction.add(newCategory(categoryId_Z112).setCode("Z112").setTitle("Z112")
				.setDescription("Ze category Z112")
				.setParent(categoryId_Z110)).setRetentionRules(asList(ruleId_3)).getId();

		categoryId_Z120 = transaction.add(newCategory(categoryId_Z120).setCode("Z120").setTitle("Z120")
				.setDescription("Ze category Z120")
				.setParent(categoryId_Z100)).setRetentionRules(asList(ruleId_3)).getId();

		categoryId_Z200 = transaction.add(newCategory(categoryId_Z200).setCode("Z200").setTitle("Z200")
				.setDescription("Ze category Z200")
				.setParent(categoryId_Z)).getId();

		categoryId_ZE42 = transaction.add(newCategory(categoryId_ZE42).setCode("ZE42").setTitle("Ze 42")
				.setDescription("Ze category 42")
				.setParent(categoryId_Z).setRetentionRules(asList(ruleId_1, ruleId_2, ruleId_3, ruleId_4))).getId();

		categoryId_Z999 = transaction.add(newCategory(categoryId_Z999).setCode("Z999").setTitle("Z999")
				.setDescription("Ze category Z999")
				.setParent(categoryId_Z).setRetentionRules(asList(ruleId_5))).getId();

	}

	private Category newCategory(String id) {
		if (developperFriendlyIds) {
			return rm.newCategoryWithId(id);
		} else {
			return rm.newCategory();
		}
	}

	private void setupAdministrativeUnits(Transaction transaction) {
		unitId_10 = transaction.add(newAdministrativeUnitWithId(unitId_10)).setCode("10")
				.setTitle("Unité 10").setTitle(ENGLISH, "Unit 10")
				.setAdress("Unit 10 Adress").setDescription("Ze ultimate unit 10").getId();
		unitId_10a = transaction.add(newAdministrativeUnitWithId(unitId_10a)).setCode("10A")
				.setTitle("Unité 10-A").setTitle(ENGLISH, "Unit 10-A")
				.setParent(unitId_10).setDescription("Ze ultimate unit 10A").getId();

		unitId_11 = transaction.add(newAdministrativeUnitWithId(unitId_11)).setCode("11")
				.setTitle("Unité 11").setTitle(ENGLISH, "Unit 11")
				.setParent(unitId_10).setDescription("Ze ultimate unit 11").getId();
		unitId_11b = transaction.add(newAdministrativeUnitWithId(unitId_11b)).setCode("11B")
				.setTitle("Unité 11-B").setTitle(ENGLISH, "Unit 11-B")
				.setParent(unitId_11).setDescription("Ze ultimate unit 11B").getId();

		unitId_12 = transaction.add(newAdministrativeUnitWithId(unitId_12)).setCode("12")
				.setTitle("Unité 12").setTitle(ENGLISH, "Unit 12")
				.setParent(unitId_10).setDescription("Ze ultimate unit 12").getId();

		unitId_12b = transaction.add(newAdministrativeUnitWithId(unitId_12b)).setCode("12B")
				.setTitle("Unité 12-B").setTitle(ENGLISH, "Unit 12-B")
				.setParent(unitId_12).setDescription("Ze ultimate unit 12B").getId();
		unitId_12c = transaction.add(newAdministrativeUnitWithId(unitId_12c)).setCode("12C")
				.setTitle("Unité 12-C").setTitle(ENGLISH, "Unit 12-C")
				.setParent(unitId_12).setDescription("Ze ultimate unit 12C").getId();

		unitId_20 = transaction.add(newAdministrativeUnitWithId(unitId_20)).setCode("20")
				.setTitle("Unité 20").setTitle(ENGLISH, "Unit 20")
				.setDescription("Ze ultimate unit 20").getId();
		unitId_20d = transaction.add(newAdministrativeUnitWithId(unitId_20d)).setCode("20D")
				.setTitle("Unité 20-D").setTitle(ENGLISH, "Unit 20-D")
				.setParent(unitId_20).setDescription("Ze ultimate unit 20D").getId();
		unitId_20e = transaction.add(newAdministrativeUnitWithId(unitId_20e)).setCode("20E")
				.setTitle("Unité 20-E").setTitle(ENGLISH, "Unit 20-E")
				.setParent(unitId_20).setDescription("Ze ultimate unit 20E").getId();

		unitId_30 = transaction.add(newAdministrativeUnitWithId(unitId_30)).setCode("30")
				.setTitle("Unité 30").setTitle(ENGLISH, "Unit 30")
				.setDescription("Ze ultimate unit 30").getId();

		unitId_30c = transaction.add(newAdministrativeUnitWithId(unitId_30c)).setCode("30C")
				.setTitle("Unité 30-C").setTitle(ENGLISH, "Unit 30-C")
				.setParent(unitId_30).setDescription("Ze ultimate unit 30C").getId();
	}

	private AdministrativeUnit newAdministrativeUnitWithId(String id) {
		if (developperFriendlyIds) {
			return rm.newAdministrativeUnitWithId(id);
		} else {
			return rm.newAdministrativeUnit();
		}
	}

	private void setupUniformSubdivisions(Transaction transaction) {
		transaction.add(rm.newUniformSubdivisionWithId(subdivId_1).setCode("sub1").setTitle("Subdiv. 1")
				.setDescription("description1")
				.setRetentionRules(asList(ruleId_2)));
		transaction.add(rm.newUniformSubdivisionWithId(subdivId_2).setCode("sub2").setTitle("Subdiv. 2")
				.setDescription("description2"));
		transaction.add(rm.newUniformSubdivisionWithId(subdivId_3).setCode("sub3").setTitle("Subdiv. 3")
				.setDescription("description3"));
	}

	private void setupRetentionRules(Transaction transaction) {

		VariableRetentionPeriod period42 = rm.newVariableRetentionPeriod().setCode("42").setTitle("Ze 42")
				.setDescription("Ze ultimate 42");
		VariableRetentionPeriod period666 = rm.newVariableRetentionPeriod().setCode("666").setTitle("Ze 666")
				.setDescription("Ze ultimate 666");

		transaction.add(period42);
		transaction.add(period666);

		CopyRetentionRule principal42_5_C = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "42-5-C");
		principal42_5_CId = principal42_5_C.getId();
		principal42_5_C.setActiveRetentionPeriod(RetentionPeriod.variable(period42));
		principal42_5_C.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(5));
		principal42_5_C.setInactiveDisposalType(DisposalType.DEPOSIT);
		principal42_5_C.setContentTypesComment("R1");
		principal42_5_C.setActiveRetentionComment("R2");
		//principal42_5_C.setCode("42-5-C");
		CopyRetentionRule secondary888_0_D = copyBuilder.newSecondary(asList(rm.PA(), rm.DM()), "888-0-D");
		secondary888_0_D.setSemiActiveRetentionComment("R3");
		secondary888_0_D.setInactiveDisposalComment("R4");

		transaction.add(rm.newRetentionRuleWithId(ruleId_1)).setCode("1")
				.setTitle("Règle de conservation #1").setTitle(ENGLISH, "Retention rule #1")
				.setAdministrativeUnits(asList(unitId_10, unitId_20)).setApproved(true)
				.setCopyRetentionRules(asList(principal42_5_C, secondary888_0_D)).setKeywords(asList("Rule #1"))
				.setCorpus("Corpus Rule 1").setDescription("Description Rule 1")
				.setJuridicReference("Juridic reference Rule 1").setGeneralComment("General Comment Rule 1")
				.setCopyRulesComment(asList("R1:comment1", "R2:comment2", "R3:comment3", "R4:comment4"))
				.setDocumentTypesDetails(asList(
						new RetentionRuleDocumentType(documentTypeId_1),
						new RetentionRuleDocumentType(documentTypeId_2),
						new RetentionRuleDocumentType(documentTypeId_3)));

		CopyRetentionRule principal5_2_T = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "5-2-T");
		CopyRetentionRule secondary2_0_D = copyBuilder.newSecondary(asList(rm.PA(), rm.DM()), "2-0-D");
		transaction.add(rm.newRetentionRuleWithId(ruleId_2)).setCode("2")
				.setTitle("Règle de conservation #2").setTitle(ENGLISH, "Retention rule #2")
				.setResponsibleAdministrativeUnits(true).setApproved(true)
				.setCopyRetentionRules(asList(principal5_2_T, secondary2_0_D))
				.setDocumentTypesDetails(asList(
						new RetentionRuleDocumentType(documentTypeId_1, DisposalType.DESTRUCTION),
						new RetentionRuleDocumentType(documentTypeId_2, DisposalType.DEPOSIT),
						new RetentionRuleDocumentType(documentTypeId_3, DisposalType.DESTRUCTION),
						new RetentionRuleDocumentType(documentTypeId_4, DisposalType.DEPOSIT)));

		CopyRetentionRule principal999_4_T = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "999-4-T");
		principal999_4_T.setContentTypesComment("R1");
		CopyRetentionRule secondary1_0_D = copyBuilder.newSecondary(asList(rm.PA(), rm.DM()), "1-0-D");
		transaction.add(rm.newRetentionRuleWithId(ruleId_3)).setCode("3")
				.setTitle("Règle de conservation #3").setTitle(ENGLISH, "Retention rule #3")
				.setResponsibleAdministrativeUnits(true).setApproved(true)
				.setCopyRetentionRules(asList(principal999_4_T, secondary1_0_D))
				.setDocumentTypesDetails(asList(
						new RetentionRuleDocumentType(documentTypeId_1, DisposalType.DEPOSIT),
						new RetentionRuleDocumentType(documentTypeId_2, DisposalType.DEPOSIT)));

		CopyRetentionRule secondary666_0_D = copyBuilder.newSecondary(asList(rm.PA(), rm.DM()));
		secondary666_0_D.setActiveRetentionPeriod(RetentionPeriod.variable(period666));
		secondary666_0_D.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(0));
		secondary666_0_D.setInactiveDisposalType(DisposalType.DESTRUCTION);
		secondary666_0_D.setSemiActiveRetentionComment("R3");
		secondary666_0_D.setInactiveDisposalComment("R4");
		CopyRetentionRule principal_PA_3_888_D = copyBuilder.newPrincipal(asList(rm.PA()), "3-888-D");
		CopyRetentionRule principal_MD_3_888_C = copyBuilder.newPrincipal(asList(rm.DM()), "3-888-C");
		transaction.add(rm.newRetentionRuleWithId(ruleId_4)).setCode("4")
				.setTitle("Règle de conservation #4").setTitle(ENGLISH, "Retention rule #4")
				.setResponsibleAdministrativeUnits(true).setApproved(true)
				.setCopyRetentionRules(asList(principal_PA_3_888_D, principal_MD_3_888_C, secondary666_0_D))
				.setCopyRulesComment(Arrays.asList("R3:comment3")).setDocumentTypesDetails(asList(
				new RetentionRuleDocumentType(documentTypeId_1),
				new RetentionRuleDocumentType(documentTypeId_3),
				new RetentionRuleDocumentType(documentTypeId_4)));

		RetentionRule rule5 = rm.newRetentionRuleWithId(ruleId_5);
		rule5.setTitle("Conseil d’administration").setTitle(ENGLISH, "Board of directors");
		rule5.setCode("0122");
		rule5.setDocumentTypesDetails(asList(
				new RetentionRuleDocumentType(documentTypeId_5),
				new RetentionRuleDocumentType(documentTypeId_6),
				new RetentionRuleDocumentType(documentTypeId_7),
				new RetentionRuleDocumentType(documentTypeId_8)
		));
		rule5.setResponsibleAdministrativeUnits(true);

		CopyRetentionRule principal888_5_C_rule5 = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "888-5-C");
		principal888_5_C_rule5.setActiveRetentionComment("R1");
		principal888_5_C_rule5.setSemiActiveRetentionComment("R2");
		principal888_5_C_rule5.setInactiveDisposalComment("R3");
		CopyRetentionRule secondary888_0_D_rule5 = copyBuilder.newSecondary(asList(rm.PA(), rm.DM()), "888-0-D");
		secondary888_0_D_rule5.setActiveRetentionComment("R1");
		secondary888_0_D_rule5.setInactiveDisposalComment("R3");

		rule5.setCopyRetentionRules(asList(principal888_5_C_rule5, secondary888_0_D_rule5));

		String r1_rule5 = "R1:\nValeur administrative \n\n";

		String r2_rule5 = "R2:\nValeur administrative\n\nLes dossiers des réunions peuvent être consultés pour répondre à une demande d’information.\n\nValeur de preuve\n\n";

		String r3_rule5 = "R3:\nValeur historique\n\nLe dossier de réunion\n\n";

		rule5.setCopyRulesComment(asList(r1_rule5, r2_rule5, r3_rule5));

		transaction.add(rule5);
	}

	private void createGroupsEvents() {
		User charles = users.charlesIn(collection);
		Group group = (Group) rm.newGroup().setTitle("group1");
		loggingServices.addGroup(group.getWrappedRecord(), charles);
		Group group2 = (Group) rm.newGroup().setTitle("group2");
		loggingServices.removeGroup(group2.getWrappedRecord(), charles);
	}

	private void createUsersEvents() {
		User charles = users.charlesIn(collection);
		User bob = users.bobIn(collection);
		loggingServices.addUser(bob.getWrappedRecord(), charles);
		User chuck = users.chuckNorrisIn(collection);
		loggingServices.addUser(chuck.getWrappedRecord(), charles);
		User leblanc = users.gandalfLeblancIn(collection);
		loggingServices.addUser(leblanc.getWrappedRecord(), charles);
		recordServices.flush();
	}

	private void createViewEvents() {
		User charles = users.charlesIn(collection);
		loggingServices.logRecordView(getFolder_A02().getWrappedRecord(), charles);
	}

	private void createLoginEvents() {
		User admin = users.adminIn(collection);
		loggingServices.login(admin);
		User charles = users.charlesIn(collection);
		loggingServices.login(charles);
		loggingServices.logout(charles);
	}

	private void createRecordsEvents() {
		Transaction transaction = new Transaction();
		User charles = users.charlesIn(collection);
		transaction
				.add(createEvent(charles.getUsername(), EventType.CREATE_FOLDER, new LocalDateTime().minusDays(1), folder_A01));
		transaction.add(createEvent(charles.getUsername(), EventType.CREATE_DOCUMENT, new LocalDateTime().minusDays(1), "11"));
		transaction
				.add(createEvent(charles.getUsername(), EventType.MODIFY_DOCUMENT, new LocalDateTime().minusDays(1), folder_A03));
		transaction.add(createEvent(charles.getUsername(), EventType.MODIFY_FOLDER, new LocalDateTime().minusDays(2), "13"));
		transaction
				.add(createEvent(charles.getUsername(), EventType.DELETE_FOLDER, new LocalDateTime().minusDays(2), folder_A05));
		transaction.add(createEvent(charles.getUsername(), EventType.CREATE_USER, new LocalDateTime().minusDays(2), bob_userInAC,
				getBob_userInAC().getTitle()));
		transaction.add(createEvent(charles.getUsername(), EventType.MODIFY_USER, new LocalDateTime().minusDays(2), chuckNorris,
				getChuckNorris().getTitle()));
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private RecordWrapper createEvent(String username, String eventType, LocalDateTime eventDate, String recordId) {
		return createEvent(username, eventType, eventDate, recordId, null);
	}

	private RecordWrapper createEvent(String username, String eventType, LocalDateTime eventDate, String recordId,
									  String title) {
		return rm.newEvent().setRecordId(recordId).setTitle(title).setUsername(username).setType(eventType)
				.setCreatedOn(eventDate);
	}

	private void createBorrowAndReturnEvents() {
		Folder folderA02 = getFolder_A02();
		Folder folderBorrowedByDakota = getFolder_A03();
		User bob = users.bobIn(collection);
		loggingServices.borrowRecord(folderA02.getWrappedRecord(), bob);
		loggingServices.borrowRecord(getContainerBac01().getWrappedRecord(), bob);
		loggingServices.returnRecord(folderA02.getWrappedRecord(), bob);
		User charles = users.charlesIn(collection);
		loggingServices.borrowRecord(folderBorrowedByDakota.getWrappedRecord(), charles);
	}

	private void createDecommissioningEvents() {
		DecommissioningList decommissioningList = rm.newDecommissioningList();
		decommissioningList.setDecommissioningListType(DecommissioningListType.FOLDERS_TO_DEPOSIT);
		decommissioningList.setTitle("folder to deposit by bob");
		User bob = users.bobIn(collection);
		decommissioningLoggingService.logDecommissioning(decommissioningList, bob);

		DecommissioningList decommissioningList2 = rm.newDecommissioningList();
		decommissioningList2.setDecommissioningListType(DecommissioningListType.FOLDERS_TO_DESTROY);
		decommissioningList2.setTitle("folder destroy by dakota");
		User dakota = users.dakotaLIndienIn(collection);
		decommissioningLoggingService.logDecommissioning(decommissioningList2, dakota);

		DecommissioningList decommissioningList3 = rm.newDecommissioningList();
		decommissioningList3.setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER);
		decommissioningList3.setTitle("folder transfer by bob");
		decommissioningLoggingService.logDecommissioning(decommissioningList3, bob);
	}


	private void setupLists(Transaction transaction) {
		transaction.add(rm.newDecommissioningListWithId(list_01))
				.setTitle("Listes avec plusieurs supports à détruire").setAdministrativeUnit(unitId_10a)
				.setDecommissioningListType(FOLDERS_TO_DESTROY)
				.setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setContainerDetailsFor(containerId_bac18, containerId_bac19).setAlreadyIncludedFolderDetailsForIds(folder_A42, folder_A43, folder_A44, folder_A45, folder_A46, folder_A47);

		DecommissioningList zeList02 = rm.newDecommissioningListWithId(list_02).setTitle("Liste analogique à détruire")
				.setAdministrativeUnit(unitId_10a)
				.setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_DESTROY).setContainerDetailsFor(containerId_bac10)
				.setAlreadyIncludedFolderDetailsForIds(folder_A54, folder_A55, folder_A56);
		for (DecomListFolderDetail detail : zeList02.getFolderDetails()) {
			detail.setContainerRecordId(containerId_bac10);
		}
		zeList02.getContainerDetails().get(0).setFull(true);
		transaction.add(zeList02);

		transaction.add(rm.newDecommissioningListWithId(list_03)).setTitle("Liste hybride à fermer")
				.setAdministrativeUnit(unitId_10a)
				.setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_CLOSE).setAlreadyIncludedFolderDetailsForIds(folder_A01, folder_A02, folder_A03);

		transaction.add(rm.newDecommissioningListWithId(list_04)).setTitle("Liste analogique à transférer")
				.setAdministrativeUnit(unitId_10a)
				.setDecommissioningListType(FOLDERS_TO_TRANSFER).setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setContainerDetailsFor(containerId_bac14, containerId_bac15).setAlreadyIncludedFolderDetailsForIds(folder_A22, folder_A23, folder_A24);

		transaction.add(rm.newDecommissioningListWithId(list_05)).setTitle("Liste hybride à transférer")
				.setAdministrativeUnit(unitId_10a).setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_TRANSFER).setAlreadyIncludedFolderDetailsForIds(folder_A19, folder_A20, folder_A21);

		transaction.add(rm.newDecommissioningListWithId(list_06)).setTitle("Liste électronique à transférer")
				.setAdministrativeUnit(unitId_10a).setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_TRANSFER).setAlreadyIncludedFolderDetailsForIds(folder_A25, folder_A26, folder_A27);

		transaction.add(rm.newDecommissioningListWithId(list_07)).setTitle("Liste analogique à détruire 2")
				.setAdministrativeUnit(unitId_10a).setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_DESTROY).setAlreadyIncludedFolderDetailsForIds(folder_A54, folder_A55, folder_A56);

		//TODO Vérifier, était autrefois dans le filing space B
		DecommissioningList zeList08 = transaction.add(rm.newDecommissioningListWithId(list_08))
				.setTitle("Liste hybride à verser")
				.setAdministrativeUnit(unitId_20d).setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_DEPOSIT).setAlreadyIncludedFolderDetailsForIds(folder_B30, folder_B33, folder_B35)
				.setContainerDetailsFor(containerId_bac08, containerId_bac09);
		List<DecomListFolderDetail> details = zeList08.getFolderDetails();
		details.get(0).setContainerRecordId(containerId_bac08);
		details.get(1).setContainerRecordId(containerId_bac09);

		transaction.add(rm.newDecommissioningListWithId(list_09)).setTitle("Liste électronique à verser")
				.setAdministrativeUnit(unitId_10a).setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_DEPOSIT).setAlreadyIncludedFolderDetailsForIds(folder_A57, folder_A58, folder_A59);

		DecommissioningList zeList10 = transaction.add(rm.newDecommissioningListWithId(list_10))
				.setTitle("Liste avec plusieurs supports à verser").setAdministrativeUnit(unitId_10a)
				.setDecommissioningListType(FOLDERS_TO_DEPOSIT)
				.setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setContainerDetailsFor(containerId_bac11, containerId_bac13)
				.setAlreadyIncludedFolderDetailsForIds(folder_A42, folder_A43, folder_A44, folder_A48, folder_A49, folder_A50);
		details = zeList10.getFolderDetails();
		details.get(0).setContainerRecordId(containerId_bac13);
		details.get(1).setContainerRecordId(containerId_bac13);
		details.get(2).setContainerRecordId(containerId_bac13);
		details.get(4).setContainerRecordId(containerId_bac11);
		details.get(5).setContainerRecordId(containerId_bac11);

		transaction.add(rm.newDecommissioningListWithId(list_11)).setTitle("Liste de fermeture traîtée")
				.setAdministrativeUnit(unitId_10a).setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_CLOSE).setProcessingUser(dakota_managerInA_userInB)
				.setProcessingDate(date(2012, 5, 5)).setAlreadyIncludedFolderDetailsForIds(folder_A10, folder_A11,
				folder_A12, folder_A13, folder_A14, folder_A15);

		transaction.add(rm.newDecommissioningListWithId(list_12)).setTitle("Liste de transfert traîtée")
				.setAdministrativeUnit(unitId_10a)
				.setDecommissioningListType(FOLDERS_TO_TRANSFER).setProcessingUser(dakota_managerInA_userInB)
				.setProcessingDate(date(2012, 5, 5)).setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setContainerDetailsFor(containerId_bac10, containerId_bac11, containerId_bac12)
				.setAlreadyIncludedFolderDetailsForIds(folder_A45, folder_A46, folder_A47, folder_A48, folder_A49,
						folder_A50, folder_A51, folder_A52, folder_A53, folder_A54, folder_A55, folder_A56, folder_A57, folder_A58, folder_A59);

		transaction.add(rm.newDecommissioningListWithId(list_13)).setTitle("Liste de transfert uniforme traîtée")
				.setAdministrativeUnit(unitId_10a).setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_TRANSFER).setProcessingUser(dakota_managerInA_userInB)
				.setProcessingDate(date(2012, 5, 5)).setContainerDetailsFor(containerId_bac13)
				.setAlreadyIncludedFolderDetailsForIds(folder_A42, folder_A43, folder_A44);

		transaction.add(rm.newDecommissioningListWithId(list_14)).setTitle("Liste de versement traîtée")
				.setAdministrativeUnit(unitId_10a).setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_DEPOSIT).setProcessingUser(dakota_managerInA_userInB)
				.setProcessingDate(date(2012, 5, 5)).setContainerDetailsFor(containerId_bac05)
				.setAlreadyIncludedFolderDetailsForIds(folder_A79, folder_A80, folder_A81, folder_A82, folder_A83,
						folder_A84, folder_A85, folder_A86, folder_A87, folder_A88, folder_A89, folder_A90, folder_A91,
						folder_A92, folder_A93);

		transaction.add(rm.newDecommissioningListWithId(list_15)).setTitle("Liste de versement uniforme traîtée")
				.setAdministrativeUnit(unitId_10a).setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_DEPOSIT).setProcessingUser(dakota_managerInA_userInB)
				.setProcessingDate(date(2012, 5, 5)).setContainerDetailsFor(containerId_bac04)
				.setAlreadyIncludedFolderDetailsForIds(folder_A94, folder_A95, folder_A96);

		DecommissioningList zeList16 = transaction.add(rm.newDecommissioningListWithId(list_16)
				.setTitle("Liste analogique à transférer en contenants").setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setAdministrativeUnit(unitId_10a).setDecommissioningListType(FOLDERS_TO_TRANSFER)
				.setContainerDetailsFor(containerId_bac14).setAlreadyIncludedFolderDetailsForIds(folder_A22, folder_A23, folder_A24));
		for (DecomListFolderDetail detail : zeList16.getFolderDetails()) {
			detail.setContainerRecordId(containerId_bac14);
		}

		DecommissioningList zeList17 = transaction.add(rm.newDecommissioningListWithId(list_17))
				.setTitle("Liste à verser").setAdministrativeUnit(unitId_10a)
				.setDecommissioningListType(FOLDERS_TO_DEPOSIT)
				.setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setContainerDetailsFor(containerId_bac11)
				.setAlreadyIncludedFolderDetailsForIds(folder_A48, folder_A49, folder_A50);
		details = zeList17.getFolderDetails();
		details.get(1).setContainerRecordId(containerId_bac11);
		details.get(2).setContainerRecordId(containerId_bac11);

		//TODO Vérifier, était autrefois dans le filing space B
		DecommissioningList zeList18 = transaction.add(rm.newDecommissioningListWithId(list_18))
				.setTitle("Liste hybride à verser avec tri")
				.setAdministrativeUnit(unitId_20d).setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_DEPOSIT).setAlreadyIncludedFolderDetailsForIds(folder_B30, folder_B33)
				.setContainerDetailsFor(containerId_bac08, containerId_bac09);
		details = zeList18.getFolderDetails();
		details.get(0).setContainerRecordId(containerId_bac08);
		details.get(1).setContainerRecordId(containerId_bac09).setReversedSort(true);

		//TODO Vérifier, était autrefois dans le filing space B
		DecommissioningList zeList19 = transaction.add(rm.newDecommissioningListWithId(list_19))
				.setTitle("Liste hybride à détruire avec tri")
				.setAdministrativeUnit(unitId_20d).setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_DESTROY).setAlreadyIncludedFolderDetailsForIds(folder_B30, folder_B33)
				.setContainerDetailsFor(containerId_bac08, containerId_bac09);
		details = zeList19.getFolderDetails();
		details.get(0).setContainerRecordId(containerId_bac08);
		details.get(1).setContainerRecordId(containerId_bac09).setReversedSort(true);

		transaction.add(rm.newDecommissioningListWithId(list_20)).setTitle("Liste hybride à verser")
				.setAdministrativeUnit(unitId_10a).setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_DEPOSIT).setAlreadyIncludedFolderDetailsForIds(folder_A19, folder_A20, folder_A21);

		transaction.add(rm.newDecommissioningListWithId(list_21)).setTitle("Liste hybride à détruire")
				.setAdministrativeUnit(unitId_10a).setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_DESTROY).setAlreadyIncludedFolderDetailsForIds(folder_A19, folder_A20, folder_A21);

		transaction.add(rm.newDecommissioningListWithId(list_23)).setTitle("Liste à approuver")
				.setAdministrativeUnit(unitId_10a)
				.setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_CLOSE).setAlreadyIncludedFolderDetailsForIds(folder_A01, folder_A02, folder_A03)
				.setApprovalRequester(admin_userIdWithAllAccess).setApprovalRequestDate(new LocalDate());

		DecomListValidation validationDakota = new DecomListValidation(dakota_managerInA_userInB, new LocalDate().minusDays(1))
				.setValidationDate(new LocalDate());
		DecomListValidation validationBob = new DecomListValidation(bob_userInAC, new LocalDate().minusDays(1))
				.setValidationDate(new LocalDate());
		transaction.add(rm.newDecommissioningListWithId(list_24)).setTitle("Liste approuvée et validée")
				.setAdministrativeUnit(unitId_10a)
				.setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_CLOSE).setAlreadyIncludedFolderDetailsForIds(folder_A01, folder_A02, folder_A03)
				.setApprovalRequester(chuckNorris).setApprovalRequestDate(new LocalDate())
				.setApprovalUser(getAdmin()).setApprovalDate(new LocalDate())
				.setValidations(asList(validationDakota, validationBob));

		transaction.add(rm.newDecommissioningListWithId(list_25)).setTitle("Autre liste à approuver et valider")
				.setAdministrativeUnit(unitId_10a)
				.setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setDecommissioningListType(FOLDERS_TO_CLOSE).setAlreadyIncludedFolderDetailsForIds(folder_A01, folder_A02, folder_A03)
				.setApprovalRequester(chuckNorris).setApprovalRequestDate(new LocalDate())
				.addValidationRequest(dakota_managerInA_userInB, new LocalDate())
				.addValidationRequest(bob_userInAC, new LocalDate());
	}

	public RMTestRecords withDocumentDecommissioningLists() {

		//contrat
		CopyRetentionRule principal1_2_C = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "2-0-C")
				.setTypeId(documentTypeId_9);

		//proces-verbal
		CopyRetentionRule principal1_2_D = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "1-2-D")
				.setTypeId(documentTypeId_10);

		CopyRetentionRule principal5_5_C = copyBuilder.newPrincipal(asList(rm.PA(), rm.DM()), "5-5-C");
		CopyRetentionRule secondary5_5_D = copyBuilder.newSecondary(asList(rm.PA(), rm.DM()), "5-5-D");

		Transaction transaction = new Transaction();

		transaction.add(rm.newRetentionRuleWithId(ruleId_6)).setCode("6").setTitle("Rule #6")
				.setScope(RetentionRuleScope.DOCUMENTS)
				.setAdministrativeUnits(asList(unitId_10, unitId_20)).setApproved(true)
				.setDocumentCopyRetentionRules(asList(principal1_2_C, principal1_2_D))
				.setPrincipalDefaultDocumentCopyRetentionRule(principal5_5_C)
				.setSecondaryDefaultDocumentCopyRetentionRule(secondary5_5_D);

		Category categoryX = rm.getCategory(categoryId_X);
		List<String> retentionRules = new ArrayList<>(categoryX.getRententionRules());
		retentionRules.add(ruleId_6);
		transaction.add(categoryX.setRetentionRules(retentionRules));

		createDocumentsAndReturnThoseWhithDifferentCopyForEach(transaction, folder_A01, folder_A02, folder_A03, folder_A04,
				folder_A05, folder_A06, folder_A07, folder_A08, folder_A09, folder_A10, folder_A11, folder_A12,
				folder_A13, folder_A14, folder_A15, folder_A16, folder_A17, folder_A18, folder_A19, folder_A20,
				folder_A21, folder_A22, folder_A23, folder_A24, folder_A25, folder_A26, folder_A27);
		createDocumentsAndReturnThoseWhithDifferentCopyForEach(transaction, folder_A42, folder_A43, folder_A44,
				folder_A45, folder_A46, folder_A47, folder_A48, folder_A49, folder_A50, folder_A51, folder_A52,
				folder_A53, folder_A54, folder_A55, folder_A56, folder_A57, folder_A58, folder_A59);
		createDocumentsAndReturnThoseWhithDifferentCopyForEach(transaction, folder_A79, folder_A80, folder_A81, folder_A82, folder_A83,
				folder_A84, folder_A85, folder_A86, folder_A87, folder_A88, folder_A89, folder_A90, folder_A91,
				folder_A92, folder_A93, folder_A94, folder_A95, folder_A96);

		//transférée
		transaction.add(rm.newDecommissioningListWithId(list_30)).setTitle("Liste de transfert de documents traîtées")
				.setAdministrativeUnit(unitId_10a)
				.setDecommissioningListType(DOCUMENTS_TO_TRANSFER).setProcessingUser(dakota_managerInA_userInB)
				.setProcessingDate(date(2012, 5, 5)).setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setDocuments(documentsWhithDifferentCopyForEach(folder_A45, folder_A46, folder_A47, folder_A48, folder_A49));

		// à transférer
		transaction.add(rm.newDecommissioningListWithId(list_31))
				.setTitle("Liste de transfert de documents").setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setAdministrativeUnit(unitId_10a).setDecommissioningListType(DOCUMENTS_TO_TRANSFER)
				.setDocuments(documentsWhithDifferentCopyForEach(folder_A22, folder_A23, folder_A24));

		// ------------
		// versée
		transaction.add(rm.newDecommissioningListWithId(list_32)).setTitle("Liste de versement de documents traîtées")
				.setAdministrativeUnit(unitId_10a).setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setDecommissioningListType(DOCUMENTS_TO_DEPOSIT).setProcessingUser(dakota_managerInA_userInB)
				.setProcessingDate(date(2012, 5, 5))
				.setDocuments(documentsWhithDifferentCopyForEach(folder_A79, folder_A80, folder_A81, folder_A82, folder_A83,
						folder_A84, folder_A85, folder_A86, folder_A87, folder_A88, folder_A89, folder_A90, folder_A91,
						folder_A92, folder_A93));

		//semi-actif à verser
		transaction.add(rm.newDecommissioningListWithId(list_33))
				.setTitle("Liste de versement de documents semi-actifs").setAdministrativeUnit(unitId_10a)
				.setDecommissioningListType(DOCUMENTS_TO_DEPOSIT)
				.setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setContainerDetailsFor(containerId_bac11)
				.setDocuments(documentsWhithDifferentCopyForEach(folder_A48, folder_A49, folder_A50));

		// actif à verser
		transaction.add(rm.newDecommissioningListWithId(list_34)).setTitle("Liste de versement de documents semi-actifs")
				.setAdministrativeUnit(unitId_10a).setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setDecommissioningListType(DOCUMENTS_TO_DEPOSIT)
				.setDocuments(documentsWhithDifferentCopyForEach(folder_A19, folder_A20, folder_A21));

		// ------------
		// actif à détruire
		transaction.add(rm.newDecommissioningListWithId(list_35)).setTitle("Liste de destruction de documents actifs")
				.setAdministrativeUnit(unitId_10a).setOriginArchivisticStatus(OriginStatus.ACTIVE)
				.setDecommissioningListType(DOCUMENTS_TO_DESTROY)
				.setDocuments(documentsWhithDifferentCopyForEach(folder_A19, folder_A20, folder_A21));

		// semi-actif à détruire
		transaction.add(rm.newDecommissioningListWithId(list_36)).setTitle("Liste de destruction de documents semi-actifs")
				.setAdministrativeUnit(unitId_10a).setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
				.setDecommissioningListType(DOCUMENTS_TO_DESTROY)
				.setDocuments(documentsWhithDifferentCopyForEach(folder_A54, folder_A55, folder_A56));

		try {
 			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		return this;
	}

	private List<String> createDocumentsAndReturnThoseWhithDifferentCopyForEach(Transaction transaction,
																				String... folders) {

		ContentManager contentManager = modelLayerFactory.getContentManager();
		User user = users.adminIn(collection);

		ContentVersionDataSummary contractVersion = upload("contrat.docx");
		Content contractContent = contentManager.createMinor(user, "contrat.docx", contractVersion);
		ContentVersionDataSummary procesVersion = upload("proces.docx");
		Content procesContent = contentManager.createMinor(user, "proces.docx", procesVersion);

		List<String> returnedIds = new ArrayList<>();

		for (String folderId : folders) {
			Folder folder = rm.getFolder(folderId);

			String paperSameCopyId = folder + "_paperDocumentWithSameCopy";
			String paperContractDifferentCopyId = folder + "_paperContractWithDifferentCopy";
			String paperProcesDifferentCopyId = folder + "_paperProcesWithDifferentCopy";
			returnedIds.add(paperContractDifferentCopyId);
			returnedIds.add(paperProcesDifferentCopyId);
			transaction.add(rm.newDocumentWithId(paperContractDifferentCopyId).setFolder(folder).setType(documentTypeId_9)
					.setTitle(folder.getTitle() + " - Document contrat analogique avec un autre exemplaire"));
			transaction.add(rm.newDocumentWithId(paperProcesDifferentCopyId).setFolder(folder).setType(documentTypeId_10)
					.setTitle(folder.getTitle() + " - Document procès verbal analogique avec un autre exemplaire"));
			transaction.add(rm.newDocumentWithId(paperSameCopyId).setFolder(folder)
					.setTitle(folder.getTitle() + " - Document analogique avec le même exemplaire"));

			String numericContractDifferentCopyId = folder + "_numericContractWithDifferentCopy";
			String numericProcesDifferentCopyId = folder + "_numericProcesWithDifferentCopy";
			String numericSameCopyId = folder + "_numericDocumentWithSameCopy";
			returnedIds.add(numericContractDifferentCopyId);
			returnedIds.add(numericProcesDifferentCopyId);

			transaction.add(rm.newDocumentWithId(numericContractDifferentCopyId).setFolder(folder).setType(documentTypeId_9)
					.setContent(contractContent)
					.setTitle(folder.getTitle() + " - Document contrat numérique avec un autre exemplaire"));
			transaction.add(rm.newDocumentWithId(numericProcesDifferentCopyId).setFolder(folder).setType(documentTypeId_10)
					.setContent(procesContent)
					.setTitle(folder.getTitle() + " - Document procès verbal numérique avec un autre exemplaire"));
			transaction.add(rm.newDocumentWithId(numericSameCopyId).setFolder(folder)
					.setContent(procesContent).setTitle(folder.getTitle() + " - Document numérique avec le même exemplaire"));
		}

		return returnedIds;
	}

	private List<String> documentsWhithDifferentCopyForEach(String... folders) {

		List<String> returnedIds = new ArrayList<>();

		for (String folderId : folders) {
			Folder folder = rm.getFolder(folderId);

			String paperContractDifferentCopyId = folder + "_paperContractWithDifferentCopy";
			String paperProcesDifferentCopyId = folder + "_paperProcesWithDifferentCopy";
			returnedIds.add(paperContractDifferentCopyId);
			returnedIds.add(paperProcesDifferentCopyId);

			String numericContractDifferentCopyId = folder + "_numericContractWithDifferentCopy";
			String numericProcesDifferentCopyId = folder + "_numericProcesWithDifferentCopy";
			returnedIds.add(numericContractDifferentCopyId);
			returnedIds.add(numericProcesDifferentCopyId);
		}

		return returnedIds;
	}

	private void setupStorageSpace(Transaction transaction) {
		transaction.add(rm.newStorageSpaceWithId(storageSpaceId_S01).setCode(storageSpaceId_S01)
				.setTitle("Etagere 1"));
		transaction.add(
				rm.newStorageSpaceWithId(storageSpaceId_S01_01).setCode(storageSpaceId_S01_01)
						.setTitle("Tablette 1").setParentStorageSpace(storageSpaceId_S01)).setDecommissioningType(
				TRANSFERT_TO_SEMI_ACTIVE);
		transaction.add(
				rm.newStorageSpaceWithId(storageSpaceId_S01_02).setCode(storageSpaceId_S01_02)
						.setTitle("Tablette 2").setParentStorageSpace(storageSpaceId_S01)).setDecommissioningType(
				DEPOSIT);
		transaction.add(rm.newStorageSpaceWithId(storageSpaceId_S02).setCode(storageSpaceId_S02)
				.setTitle("Etagere 2"));
		transaction.add(
				rm.newStorageSpaceWithId(storageSpaceId_S02_01).setCode(storageSpaceId_S02_01)
						.setTitle("Tablette 1").setParentStorageSpace(storageSpaceId_S02)).setDecommissioningType(
				TRANSFERT_TO_SEMI_ACTIVE);
		transaction.add(rm.newStorageSpaceWithId(storageSpaceId_S02_02).setCode(storageSpaceId_S02_02).setTitle("Tablette 2")
				.setParentStorageSpace(storageSpaceId_S02)).setDecommissioningType(DEPOSIT);
	}

	private void setupContainerTypes(Transaction transaction) {
		transaction.add(rm.newContainerRecordTypeWithId(containerTypeId_boite22x22).setTitle("Boite 22X22")
				.setCode("B22x22"));
	}

	private void setupContainers(Transaction transaction) {

		String noStorageSpace = null;

		transaction.add(rm.newContainerRecordWithId(containerId_bac19)).setTemporaryIdentifier("10_A_12")
				.setFull(false).setAdministrativeUnit(unitId_10a)
				.setDecommissioningType(DESTRUCTION).setType(containerTypeId_boite22x22);

		transaction.add(rm.newContainerRecordWithId(containerId_bac18)).setTemporaryIdentifier("10_A_11")
				.setFull(false).setAdministrativeUnit(unitId_10a)
				.setDecommissioningType(DESTRUCTION).setType(containerTypeId_boite22x22);

		transaction.add(rm.newContainerRecordWithId(containerId_bac17)).setTemporaryIdentifier("10_A_10")
				.setFull(false).setAdministrativeUnit(unitId_10a)
				.setDecommissioningType(DEPOSIT).setType(containerTypeId_boite22x22);

		transaction.add(rm.newContainerRecordWithId(containerId_bac16)).setTemporaryIdentifier("10_A_09")
				.setFull(false).setAdministrativeUnit(unitId_10a)
				.setDecommissioningType(DEPOSIT).setType(containerTypeId_boite22x22);

		transaction.add(rm.newContainerRecordWithId(containerId_bac15)).setTemporaryIdentifier("10_A_08")
				.setFull(false).setAdministrativeUnit(unitId_10a)
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(containerTypeId_boite22x22);

		transaction.add(rm.newContainerRecordWithId(containerId_bac14)).setTemporaryIdentifier("10_A_07")
				.setFull(false).setAdministrativeUnit(unitId_10a)
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(containerTypeId_boite22x22);

		transaction
				.add(rm.newContainerRecordWithId(containerId_bac13).setTemporaryIdentifier("10_A_06")
						.setFull(false).setStorageSpace(storageSpaceId_S01_01).setAdministrativeUnit(unitId_10a)
						.setRealTransferDate(date(2008, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(containerTypeId_boite22x22);

		transaction
				.add(rm.newContainerRecordWithId(containerId_bac12).setTemporaryIdentifier("10_A_05")
						.setFull(false).setStorageSpace(storageSpaceId_S01_01).setAdministrativeUnit(unitId_10a)
						.setRealTransferDate(date(2006, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(containerTypeId_boite22x22);

		transaction
				.add(rm.newContainerRecordWithId(containerId_bac11).setTemporaryIdentifier("10_A_04")
						.setFull(false).setStorageSpace(storageSpaceId_S01_01).setAdministrativeUnit(unitId_10a)
						.setRealTransferDate(date(2005, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(containerTypeId_boite22x22);

		transaction
				.add(rm.newContainerRecordWithId(containerId_bac10).setTemporaryIdentifier("10_A_03")
						.setFull(true).setStorageSpace(noStorageSpace).setAdministrativeUnit(unitId_10a)
						.setRealTransferDate(date(2007, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(containerTypeId_boite22x22);

		transaction
				.add(rm.newContainerRecordWithId(containerId_bac09).setTemporaryIdentifier("11_B_02")
						.setFull(false).setStorageSpace(storageSpaceId_S02_01).setAdministrativeUnit(unitId_11b)
						.setRealTransferDate(date(2006, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(containerTypeId_boite22x22);

		transaction
				.add(rm.newContainerRecordWithId(containerId_bac08).setTemporaryIdentifier("12_B_02")
						.setFull(false).setStorageSpace(storageSpaceId_S02_01).setAdministrativeUnit(unitId_12b)
						.setRealTransferDate(date(2007, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(containerTypeId_boite22x22);

		transaction
				.add(rm.newContainerRecordWithId(containerId_bac07).setTemporaryIdentifier("30_C_03")
						.setFull(false).setStorageSpace(storageSpaceId_S02_01).setAdministrativeUnit(unitId_30c)
						.setRealTransferDate(date(2007, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(containerTypeId_boite22x22);

		transaction
				.add(rm.newContainerRecordWithId(containerId_bac06).setTemporaryIdentifier("30_C_02")
						.setFull(false).setStorageSpace(noStorageSpace).setAdministrativeUnit(unitId_30c)
						.setRealTransferDate(date(2006, 10, 31)))
				.setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(containerTypeId_boite22x22);

		transaction
				.add(rm.newContainerRecordWithId(containerId_bac05).setTemporaryIdentifier("10_A_02")
						.setFull(true).setStorageSpace(storageSpaceId_S01_02).setAdministrativeUnit(unitId_10a)
						.setRealTransferDate(date(2008, 10, 31))
						.setRealDepositDate(date(2012, 5, 15))).setDecommissioningType(DEPOSIT)
				.setType(containerTypeId_boite22x22);

		transaction
				.add(rm.newContainerRecordWithId(containerId_bac04).setTemporaryIdentifier("10_A_01")
						.setFull(false).setStorageSpace(storageSpaceId_S01_02).setAdministrativeUnit(unitId_10a)
						.setRealTransferDate(date(2007, 10, 31))
						.setRealDepositDate(date(2010, 8, 17))).setDecommissioningType(DEPOSIT)
				.setType(containerTypeId_boite22x22);

		transaction
				.add(rm.newContainerRecordWithId(containerId_bac03).setTemporaryIdentifier("11_B_01")
						.setFull(false).setStorageSpace(storageSpaceId_S02_02).setAdministrativeUnit(unitId_11b)
						.setRealTransferDate(date(2006, 10, 31))
						.setRealDepositDate(date(2009, 8, 17))).setDecommissioningType(DEPOSIT)
				.setType(containerTypeId_boite22x22);

		transaction
				.add(rm.newContainerRecordWithId(containerId_bac02).setTemporaryIdentifier("12_B_01")
						.setFull(false).setStorageSpace(noStorageSpace).setAdministrativeUnit(unitId_12b)
						.setRealTransferDate(date(2007, 10, 31))
						.setRealDepositDate(date(2011, 2, 13))).setDecommissioningType(DEPOSIT)
				.setType(containerTypeId_boite22x22);

		transaction
				.add(rm.newContainerRecordWithId(containerId_bac01).setTemporaryIdentifier("30_C_01")
						.setFull(true).setStorageSpace(storageSpaceId_S02_02).setAdministrativeUnit(unitId_30c)
						.setRealTransferDate(date(2007, 10, 31))
						.setRealDepositDate(date(2011, 2, 13))).setDecommissioningType(DEPOSIT)
				.setType(containerTypeId_boite22x22).setModifiedBy(bob_userInAC);
	}

	private void setupFolders(Transaction transaction) {

		List<Folder> folders = new ArrayList<>();

		Function<Folder, Folder> add = f -> {
			folders.add(f);
			return f;
		};


		folder_A01 = add.apply(newFolder(folder_A01).setTitle("Abeille").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))).getId();

		folder_A02 = add.apply(newFolder(folder_A02).setTitle("Aigle").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4))).getId();

		folder_A03 = add.apply(newFolder(folder_A03).setTitle("Alouette").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5))).getId();

		folder_A04 = add.apply(newFolder(folder_A04).setTitle("Baleine").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 10, 4)).setDescription("King Dedede")).getId();

		folder_A05 = add.apply(newFolder(folder_A05).setTitle("Belette").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 11, 4))).getId();

		folder_A06 = add.apply(newFolder(folder_A06).setTitle("Bison").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 11, 5))).getId();

		folder_A07 = add.apply(newFolder(folder_A07).setTitle("Bouc").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_Z112).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))).getId();

		folder_A08 = add.apply(newFolder(folder_A08).setTitle("Boeuf").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_Z112).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4))).getId();

		folder_A09 = add.apply(newFolder(folder_A09).setTitle("Buffle").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_Z112).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5))).getId();

		folder_A10 = add.apply(newFolder(folder_A10).setTitle("Canard").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_A11 = add.apply(newFolder(folder_A11).setTitle("Carpe").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4))
				.setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_A12 = add.apply(newFolder(folder_A12).setTitle("Castor").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5))
				.setCloseDateEntered(date(2002, 10, 31))).getId();

		folder_A13 = add.apply(newFolder(folder_A13).setTitle("Cerf").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_A14 = add.apply(newFolder(folder_A14).setTitle("Chacal").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 11, 4))
				.setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_A15 = add.apply(newFolder(folder_A15).setTitle("Chameau").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 11, 5))
				.setCloseDateEntered(date(2002, 10, 31))).getId();

		folder_A16 = add.apply(newFolder(folder_A16).setTitle("Chat").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_A17 = add.apply(newFolder(folder_A17).setTitle("Chauve-souris").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_A18 = add.apply(newFolder(folder_A18).setTitle("Cheval").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31))).getId();

		folder_A19 = add.apply(newFolder(folder_A19).setTitle("Chevreuil").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_A20 = add.apply(newFolder(folder_A20).setTitle("Chien").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_A21 = add.apply(newFolder(folder_A21).setTitle("Chimpanzé").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5))
				.setCloseDateEntered(date(2002, 10, 31))).getId();

		folder_A22 = add.apply(newFolder(folder_A22).setTitle("Chouette").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4))
				.setCloseDateEntered(date(2002, 10, 31))).getId();

		folder_A23 = add.apply(newFolder(folder_A23).setTitle("Cigale").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 4))
				.setCloseDateEntered(date(2002, 10, 31))).getId();

		folder_A24 = add.apply(newFolder(folder_A24).setTitle("Cochon").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 5))
				.setCloseDateEntered(date(2003, 10, 31))).getId();

		folder_A25 = add.apply(newFolder(folder_A25).setTitle("Coq").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4))
				.setCloseDateEntered(date(2002, 10, 31))).getId();

		folder_A26 = add.apply(newFolder(folder_A26).setTitle("Corbeau").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 4))
				.setCloseDateEntered(date(2002, 10, 31))).getId();

		folder_A27 = add.apply(newFolder(folder_A27).setTitle("Coyote").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 5))
				.setCloseDateEntered(date(2003, 10, 31))).getId();

		folder_A42 = add.apply(newFolder(folder_A42).setTitle("Crocodile").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2007, 10, 31))
				.setContainer(containerId_bac13)).getId();

		folder_A43 = add.apply(newFolder(folder_A43).setTitle("Dauphin").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2007, 10, 31))
				.setContainer(containerId_bac13)).getId();

		folder_A44 = add.apply(newFolder(folder_A44).setTitle("Dindon").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2008, 10, 31)).setContainer(containerId_bac13)).getId();

		folder_A45 = add.apply(newFolder(folder_A45).setTitle("Écureuil").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setContainer(containerId_bac12)).getId();

		folder_A46 = add.apply(newFolder(folder_A46).setTitle("Éléphant").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 11, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setContainer(containerId_bac12).setDescription("Babar")).getId();

		folder_A47 = add.apply(newFolder(folder_A47).setTitle("Girafe").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31))
				.setContainer(containerId_bac12)).getId();

		folder_A48 = add.apply(newFolder(folder_A48).setTitle("Gorille").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.DM()).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setDescription("Donkey Kong")).getId();

		folder_A49 = add.apply(newFolder(folder_A49).setTitle("Grenouille").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setContainer(containerId_bac11).setDescription("Greninja")).getId();

		folder_A50 = add.apply(newFolder(folder_A50).setTitle("Hamster").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2005, 10, 31)).setContainer(containerId_bac11)).getId();

		folder_A51 = add.apply(newFolder(folder_A51).setTitle("Hérisson").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2004, 10, 31))
				.setDescription("Sonic").setContainer(containerId_bac10)).getId();

		folder_A52 = add.apply(newFolder(folder_A52).setTitle("Hibou").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2004, 10, 31))
				.setContainer(containerId_bac10).setDescription("Gibou")).getId();

		folder_A53 = add.apply(newFolder(folder_A53).setTitle("Hippopotame").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2005, 10, 31))
				.setContainer(containerId_bac10)).getId();

		folder_A54 = add.apply(newFolder(folder_A54).setTitle("Jaguar").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setContainer(containerId_bac10)).getId();

		folder_A55 = add.apply(newFolder(folder_A55).setTitle("Kangourou").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setContainer(containerId_bac10)).getId();

		folder_A56 = add.apply(newFolder(folder_A56).setTitle("Léopard").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 5))
				.setCloseDateEntered(date(2003, 10, 31)).setActualTransferDate(date(2007, 10, 31))
				.setContainer(containerId_bac10)).getId();

		folder_A57 = add.apply(newFolder(folder_A57).setTitle("Lièvre").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setContainer(containerId_bac10).setDescription("Gèvre")).getId();

		folder_A58 = add.apply(newFolder(folder_A58).setTitle("Lion").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setContainer(containerId_bac10)).getId();

		folder_A59 = add.apply(newFolder(folder_A59).setTitle("Loup").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 5))
				.setCloseDateEntered(date(2003, 10, 31)).setActualTransferDate(date(2007, 10, 31))
				.setContainer(containerId_bac10)).getId();

		folder_A79 = add.apply(newFolder(folder_A79).setTitle("Lynx").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2007, 10, 31))
				.setActualDepositDate(date(2011, 2, 13)).setContainer(containerId_bac05)).getId();

		folder_A80 = add.apply(newFolder(folder_A80).setTitle("Marmotte").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2007, 10, 31))
				.setActualDestructionDate(date(2011, 2, 13))).getId();

		folder_A81 = add.apply(newFolder(folder_A81).setTitle("Moineau").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2008, 10, 31))
				.setActualDepositDate(date(2012, 2, 13)).setContainer(containerId_bac05)).getId();

		folder_A82 = add.apply(newFolder(folder_A82).setTitle("Mouton").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2005, 10, 31))
				.setActualDestructionDate(date(2007, 4, 14))).getId();

		folder_A83 = add.apply(newFolder(folder_A83).setTitle("Orignal").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 11, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualDestructionDate(date(2007, 4, 14))).getId();

		folder_A84 = add.apply(newFolder(folder_A84).setTitle("Ours").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 11, 5))
				.setCloseDateEntered(date(2002, 10, 31)).setActualDestructionDate(date(2008, 4, 14))).getId();

		folder_A85 = add.apply(newFolder(folder_A85).setTitle("Panda").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setActualDepositDate(date(2011, 5, 15))
				.setContainer(containerId_bac05)).getId();

		folder_A86 = add.apply(newFolder(folder_A86).setTitle("Perroquet").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setActualDepositDate(date(2011, 5, 15))
				.setContainer(containerId_bac05)).getId();

		folder_A87 = add.apply(newFolder(folder_A87).setTitle("Phoque").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2005, 10, 31)).setActualDepositDate(date(2012, 5, 15))
				.setContainer(containerId_bac05)).getId();

		folder_A88 = add.apply(newFolder(folder_A88).setTitle("Pigeon").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2004, 10, 31))
				.setActualDestructionDate(date(2011, 6, 16))).getId();

		folder_A89 = add.apply(newFolder(folder_A89).setTitle("Rossignol").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2004, 10, 31))
				.setActualDepositDate(date(2011, 6, 16)).setContainer(containerId_bac05)).getId();

		folder_A90 = add.apply(newFolder(folder_A90).setTitle("Sanglier").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2005, 10, 31))
				.setActualDestructionDate(date(2012, 6, 16))).getId();

		folder_A91 = add.apply(newFolder(folder_A91).setTitle("Serpent").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setActualDestructionDate(date(2009, 7, 16))).getId();

		folder_A92 = add.apply(newFolder(folder_A92).setTitle("Singe").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setActualDestructionDate(date(2009, 7, 16))).getId();

		folder_A93 = add.apply(newFolder(folder_A93).setTitle("Souris").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 5))
				.setCloseDateEntered(date(2003, 10, 31)).setActualTransferDate(date(2007, 10, 31))
				.setActualDestructionDate(date(2010, 7, 16))).getId();

		folder_A94 = add.apply(newFolder(folder_A94).setTitle("Taupe").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setActualDepositDate(date(2009, 8, 17)).setContainer(containerId_bac04)).getId();

		folder_A95 = add.apply(newFolder(folder_A95).setTitle("Tigre").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setActualDepositDate(date(2009, 8, 17)).setContainer(containerId_bac04)).getId();

		folder_A96 = add.apply(newFolder(folder_A96).setTitle("Zèbre").setAdministrativeUnitEntered(unitId_10a)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 5))
				.setCloseDateEntered(date(2003, 10, 31)).setActualTransferDate(date(2007, 10, 31))
				.setActualDepositDate(date(2010, 8, 17)).setContainer(containerId_bac04)).getId();

		folder_B01 = add.apply(newFolder(folder_B01).setTitle("Abricot").setAdministrativeUnitEntered(unitId_11b)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))).getId();

		folder_B02 = add.apply(newFolder(folder_B02).setTitle("Banane").setAdministrativeUnitEntered(unitId_12b)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 10, 4))).getId();

		folder_B03 = add.apply(newFolder(folder_B03).setTitle("Citron").setAdministrativeUnitEntered(unitId_11b)
				.setCategoryEntered(categoryId_Z112).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))).getId();

		folder_B04 = add.apply(newFolder(folder_B04).setTitle("Datte").setAdministrativeUnitEntered(unitId_12b)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_B05 = add.apply(newFolder(folder_B05).setTitle("Fraise").setAdministrativeUnitEntered(unitId_11b)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_B06 = add.apply(newFolder(folder_B06).setTitle("Framboise").setAdministrativeUnitEntered(unitId_12b)
				.setCategoryEntered(categoryId_X100).setRetentionRuleEntered(ruleId_1).setCopyStatusEntered(SECONDARY)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_B07 = add.apply(newFolder(folder_B07).setTitle("Kiwi").setAdministrativeUnitEntered(unitId_11b)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_B08 = add.apply(newFolder(folder_B08).setTitle("Mangue").setAdministrativeUnitEntered(unitId_12b)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4))
				.setCloseDateEntered(date(2002, 10, 31))).getId();

		folder_B09 = add.apply(newFolder(folder_B09).setTitle("Melon").setAdministrativeUnitEntered(unitId_11b)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4))
				.setCloseDateEntered(date(2002, 10, 31))).getId();

		folder_B30 = add.apply(newFolder(folder_B30).setTitle("Nectarine").setAdministrativeUnitEntered(unitId_12b)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2007, 10, 31))
				.setContainer(containerId_bac08)).getId();

		folder_B31 = add.apply(newFolder(folder_B31).setTitle("Orange").setAdministrativeUnitEntered(unitId_11b)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setContainer(containerId_bac09)).getId();

		folder_B32 = add.apply(newFolder(folder_B32).setTitle("Pêche").setAdministrativeUnitEntered(unitId_12b)
				.setCategoryEntered(categoryId_X100).setRetentionRuleEntered(ruleId_1).setCopyStatusEntered(SECONDARY)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setContainer(containerId_bac08)).getId();

		folder_B33 = add.apply(newFolder(folder_B33).setTitle("Poire").setAdministrativeUnitEntered(unitId_11b)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2004, 10, 31))
				.setContainer(containerId_bac09)).getId();

		folder_B34 = add.apply(newFolder(folder_B34).setTitle("Pomme").setAdministrativeUnitEntered(unitId_12b)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setContainer(containerId_bac08)).getId();

		folder_B35 = add.apply(newFolder(folder_B35).setTitle("Raison").setAdministrativeUnitEntered(unitId_11b)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))).getId();

		folder_B50 = add.apply(newFolder(folder_B50).setTitle("Tomate").setAdministrativeUnitEntered(unitId_12b)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2007, 10, 31))
				.setActualDepositDate(date(2011, 2, 13)).setContainer(containerId_bac02)).getId();

		folder_B51 = add.apply(newFolder(folder_B51).setTitle("Cerise").setAdministrativeUnitEntered(unitId_11b)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualDestructionDate(date(2007, 4, 14))).getId();

		folder_B52 = add.apply(newFolder(folder_B52).setTitle("Avocat").setAdministrativeUnitEntered(unitId_12b)
				.setCategoryEntered(categoryId_X100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setActualDestructionDate(date(2006, 5, 15))).getId();

		folder_B53 = add.apply(newFolder(folder_B53).setTitle("Ananas").setAdministrativeUnitEntered(unitId_11b)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2004, 10, 31))
				.setActualDestructionDate(date(2011, 6, 16))).getId();

		folder_B54 = add.apply(newFolder(folder_B54).setTitle("Mûre").setAdministrativeUnitEntered(unitId_12b)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setActualDestructionDate(date(2009, 7, 16))).getId();

		folder_B55 = add.apply(newFolder(folder_B55).setTitle("Prune").setAdministrativeUnitEntered(unitId_11b)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setActualDepositDate(date(2009, 8, 17)).setContainer(containerId_bac03)).getId();

		folder_C01 = add.apply(newFolder(folder_C01).setTitle("Asperge").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))).getId();

		folder_C02 = add.apply(newFolder(folder_C02).setTitle("Brocoli").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 10, 4))).getId();

		folder_C03 = add.apply(newFolder(folder_C03).setTitle("Carotte").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_Z112).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))).getId();

		folder_C04 = add.apply(newFolder(folder_C04).setTitle("Céleri").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_C05 = add.apply(newFolder(folder_C05).setTitle("Chou").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_C06 = add.apply(newFolder(folder_C06).setTitle("Chou-fleur").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_C07 = add.apply(newFolder(folder_C07).setTitle("Citrouille").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31))).getId();

		folder_C08 = add.apply(newFolder(folder_C08).setTitle("Concombre").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4))
				.setCloseDateEntered(date(2002, 10, 31))).getId();

		folder_C09 = add.apply(newFolder(folder_C09).setTitle("Courge").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4))
				.setCloseDateEntered(date(2002, 10, 31))).getId();

		folder_C30 = add.apply(newFolder(folder_C30).setTitle("Haricot").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2007, 10, 31))
				.setContainer(containerId_bac07)).getId();

		folder_C31 = add.apply(newFolder(folder_C31).setTitle("Laitue").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setContainer(containerId_bac07)).getId();

		folder_C32 = add.apply(newFolder(folder_C32).setTitle("Maïs").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(rm.PA(), rm.DM()).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setContainer(containerId_bac07)).getId();

		folder_C33 = add.apply(newFolder(folder_C33).setTitle("Navet").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2004, 10, 31))
				.setContainer(containerId_bac07)).getId();

		folder_C34 = add.apply(newFolder(folder_C34).setTitle("Oignon").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setContainer(containerId_bac07)).getId();

		folder_C35 = add.apply(newFolder(folder_C35).setTitle("Poireau").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setContainer(containerId_bac06)).getId();

		folder_C50 = add.apply(newFolder(folder_C50).setTitle("Pois").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2007, 10, 31))
				.setActualDepositDate(date(2011, 2, 13)).setContainer(containerId_bac01)).getId();

		folder_C51 = add.apply(newFolder(folder_C51).setTitle("Poivron").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualDestructionDate(date(2007, 4, 14))).getId();

		folder_C52 = add.apply(newFolder(folder_C52).setTitle("Pomme de terre")
				.setAdministrativeUnitEntered(unitId_30c).setCategoryEntered(categoryId_X100)
				.setRetentionRuleEntered(ruleId_1).setMediumTypes(rm.PA(), rm.DM()).setDescription("Patate")
				.setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setActualDestructionDate(date(2006, 5, 15))).getId();

		folder_C53 = add.apply(newFolder(folder_C53).setTitle("Radis").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_Z120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.setCloseDateEntered(date(2001, 10, 31)).setActualTransferDate(date(2004, 10, 31))
				.setActualDestructionDate(date(2011, 6, 16))).getId();

		folder_C54 = add.apply(newFolder(folder_C54).setTitle("Epinard").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.PA()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setActualDestructionDate(date(2009, 7, 16))).getId();

		folder_C55 = add.apply(newFolder(folder_C55).setTitle("Bette").setAdministrativeUnitEntered(unitId_30c)
				.setCategoryEntered(categoryId_X120).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4))
				.setCloseDateEntered(date(2002, 10, 31)).setActualTransferDate(date(2006, 10, 31))
				.setActualDepositDate(date(2009, 8, 17)).setContainer(containerId_bac01)).getId();

		Map<String, List<String>> ruleDocumentTypes = new HashMap<>();
		Map<String, String> documentTypesTitles = new HashMap<>();

		for (Folder folder : folders) {
			transaction.add(folder);
			if (folder.getMediumTypes().contains(PA)) {
				String retentionRule = folder.getRetentionRuleEntered();
				List<String> types = ruleDocumentTypes.get(retentionRule);
				if (types == null) {
					types = rm.getRetentionRule(retentionRule).getDocumentTypes();
					ruleDocumentTypes.put(retentionRule, types);
				}

				for (String type : types) {
					String title = documentTypesTitles.get(type);
					if (title == null) {
						title = rm.getDocumentType(type).getTitle();
						documentTypesTitles.put(type, title);
					}

					String documentTitle = folder.getTitle() + " - " + title;
					transaction.add(rm.newDocument().setTitle(documentTitle).setFolder(folder).setType(type));
				}
			}
		}
	}

	private Folder newFolder(String id) {
		if (developperFriendlyIds) {
			return rm.newFolderWithId(id);
		} else {
			return rm.newFolder();
		}
	}

	private void setupDocumentsWithContent(Transaction transaction) {
		transaction.add(newDocumentWithContent(document_A19, "Chevreuil.odt").setFolder(folder_A19));
		transaction.add(newDocumentWithContent(document_A49, "Grenouille.odt").setFolder(folder_A49));
		transaction.add(newDocumentWithContent(document_B30, "Nectarine.odt").setFolder(folder_B30));
		transaction.add(newDocumentWithContent(document_B33, "Poire.odt").setFolder(folder_B33));
		transaction.add(newDocumentWithContent(document_A79, "Lynx.odt").setFolder(folder_A79));
	}

	private Document newDocumentWithContent(String id, String resource) {
		User user = users.adminIn(collection);
		ContentVersionDataSummary version01 = upload("Minor_" + resource);
		Content content = contentManager.createMinor(user, resource, version01);
		ContentVersionDataSummary version10 = upload("Major_" + resource);
		content.updateContent(user, version10, true);
		return rm.newDocumentWithId(id).setTitle(resource).setContent(content).setType(documentTypeId_1);
	}

	public ContentVersionDataSummary upload(String resource) {
		InputStream inputStream = DemoTestRecords.class.getResourceAsStream("RMTestRecords_" + resource);
		return contentManager.upload(inputStream, new UploadOptions(resource)).getContentVersionDataSummary();
	}

	private LocalDate date(int year, int month, int day) {
		return new LocalDate(year, month, day);
	}

	public Category getCategory_Z() {
		return rm.getCategory(categoryId_Z);
	}

	public Category getCategory_Z100() {
		return rm.getCategory(categoryId_Z100);
	}

	public Category getCategory_Z110() {
		return rm.getCategory(categoryId_Z110);
	}

	public Category getCategory_Z111() {
		return rm.getCategory(categoryId_Z111);
	}

	public Category getCategory_Z112() {
		return rm.getCategory(categoryId_Z112);
	}

	public Category getCategory_Z120() {
		return rm.getCategory(categoryId_Z120);
	}

	public Category getCategory_Z200() {
		return rm.getCategory(categoryId_Z200);
	}

	public Category getCategory_ZE42() {
		return rm.getCategory(categoryId_ZE42);
	}

	public Category getCategory_Z999() {
		return rm.getCategory(categoryId_Z999);
	}

	public Category getCategory_X() {
		return rm.getCategory(categoryId_X);
	}

	public Category getCategory_X100() {
		return rm.getCategory(categoryId_X100);
	}

	public Category getCategory_X110() {
		return rm.getCategory(categoryId_X110);
	}

	public Category getCategory_X120() {
		return rm.getCategory(categoryId_X120);
	}

	public Category getCategory_X13() {
		return rm.getCategory(categoryId_X13);
	}

	public AdministrativeUnit getUnit10() {
		return rm.getAdministrativeUnit(unitId_10);
	}

	public AdministrativeUnit getUnit10a() {
		return rm.getAdministrativeUnit(unitId_10a);
	}

	public AdministrativeUnit getUnit11() {
		return rm.getAdministrativeUnit(unitId_11);
	}

	public AdministrativeUnit getUnit11b() {
		return rm.getAdministrativeUnit(unitId_11b);
	}

	public AdministrativeUnit getUnit12() {
		return rm.getAdministrativeUnit(unitId_12);
	}

	public AdministrativeUnit getUnit12b() {
		return rm.getAdministrativeUnit(unitId_12b);
	}

	public AdministrativeUnit getUnit12c() {
		return rm.getAdministrativeUnit(unitId_12c);
	}

	public AdministrativeUnit getUnit20() {
		return rm.getAdministrativeUnit(unitId_20);
	}

	public AdministrativeUnit getUnit20d() {
		return rm.getAdministrativeUnit(unitId_20d);
	}

	public AdministrativeUnit getUnit20e() {
		return rm.getAdministrativeUnit(unitId_20e);
	}

	public AdministrativeUnit getUnit30() {
		return rm.getAdministrativeUnit(unitId_30);
	}

	public AdministrativeUnit getUnit30c() {
		return rm.getAdministrativeUnit(unitId_30c);
	}

	public UniformSubdivision getUniformSubdivision1() {
		return rm.getUniformSubdivision(subdivId_1);
	}

	public UniformSubdivision getUniformSubdivision2() {
		return rm.getUniformSubdivision(subdivId_2);
	}

	public UniformSubdivision getUniformSubdivision3() {
		return rm.getUniformSubdivision(subdivId_3);
	}

	//	public FilingSpace getFilingA() {
	//		return rm.getFilingSpace(filingId_A);
	//	}
	//
	//	public FilingSpace getFilingB() {
	//		return rm.getFilingSpace(filingId_B);
	//	}
	//
	//	public FilingSpace getFilingC() {
	//		return rm.getFilingSpace(filingId_C);
	//	}
	//
	//	public FilingSpace getFilingD() {
	//		return rm.getFilingSpace(filingId_D);
	//	}
	//
	//	public FilingSpace getFilingE() {
	//		return rm.getFilingSpace(filingId_E);
	//	}

	public RetentionRule getRule1() {
		return rm.getRetentionRule(ruleId_1);
	}

	public RetentionRule getRule2() {
		return rm.getRetentionRule(ruleId_2);
	}

	public RetentionRule getRule3() {
		return rm.getRetentionRule(ruleId_3);
	}

	public RetentionRule getRule4() {
		return rm.getRetentionRule(ruleId_4);
	}

	public RetentionRule getRule5() {
		return rm.getRetentionRule(ruleId_5);
	}

	public String getCollection() {
		return collection;
	}

	public RMSchemasRecordsServices getSchemas() {
		return rm;
	}

	public User getAdmin() {
		return rm.getUser(admin_userIdWithAllAccess);
	}

	public User getAlice() {
		return rm.getUser(alice_userWithNoWriteAccess);
	}

	public User getBob_userInAC() {
		return rm.getUser(bob_userInAC);
	}

	public User getCharles_userInA() {
		return rm.getUser(charles_userInA);
	}

	public User getDakota_managerInA_userInB() {
		return rm.getUser(dakota_managerInA_userInB);
	}

	public User getEdouard_managerInB_userInC() {
		return rm.getUser(edouard_managerInB_userInC);
	}

	public User getGandalf_managerInABC() {
		return rm.getUser(gandalf_managerInABC);
	}

	public User getChuckNorris() {
		return rm.getUser(chuckNorris);
	}

	public StorageSpace getStorageSpaceS01() {
		return rm.getStorageSpace(storageSpaceId_S01);
	}

	public StorageSpace getStorageSpaceS01_01() {
		return rm.getStorageSpace(storageSpaceId_S01_01);
	}

	public StorageSpace getStorageSpaceS01_02() {
		return rm.getStorageSpace(storageSpaceId_S01_02);
	}

	public StorageSpace getStorageSpaceS02() {
		return rm.getStorageSpace(storageSpaceId_S02);
	}

	public StorageSpace getStorageSpaceS02_01() {
		return rm.getStorageSpace(storageSpaceId_S02_01);
	}

	public StorageSpace getStorageSpaceS02_02() {
		return rm.getStorageSpace(storageSpaceId_S02_02);
	}

	public ContainerRecord getContainerBac14() {
		return rm.getContainerRecord(containerId_bac14);
	}

	public ContainerRecord getContainerBac13() {
		return rm.getContainerRecord(containerId_bac13);
	}

	public ContainerRecord getContainerBac12() {
		return rm.getContainerRecord(containerId_bac12);
	}

	public ContainerRecord getContainerBac11() {
		return rm.getContainerRecord(containerId_bac11);
	}

	public ContainerRecord getContainerBac10() {
		return rm.getContainerRecord(containerId_bac10);
	}

	public ContainerRecord getContainerBac09() {
		return rm.getContainerRecord(containerId_bac09);
	}

	public ContainerRecord getContainerBac08() {
		return rm.getContainerRecord(containerId_bac08);
	}

	public ContainerRecord getContainerBac07() {
		return rm.getContainerRecord(containerId_bac07);
	}

	public ContainerRecord getContainerBac06() {
		return rm.getContainerRecord(containerId_bac06);
	}

	public ContainerRecord getContainerBac05() {
		return rm.getContainerRecord(containerId_bac05);
	}

	public ContainerRecord getContainerBac04() {
		return rm.getContainerRecord(containerId_bac04);
	}

	public ContainerRecord getContainerBac03() {
		return rm.getContainerRecord(containerId_bac03);
	}

	public ContainerRecord getContainerBac02() {
		return rm.getContainerRecord(containerId_bac02);
	}

	public ContainerRecord getContainerBac01() {
		return rm.getContainerRecord(containerId_bac01);
	}

	public DecommissioningList getList01() {
		return rm.getDecommissioningList(list_01);
	}

	public DecommissioningList getList02() {
		return rm.getDecommissioningList(list_02);
	}

	public DecommissioningList getList03() {
		return rm.getDecommissioningList(list_03);
	}

	public DecommissioningList getList04() {
		return rm.getDecommissioningList(list_04);
	}

	public DecommissioningList getList05() {
		return rm.getDecommissioningList(list_05);
	}

	public DecommissioningList getList06() {
		return rm.getDecommissioningList(list_06);
	}

	public DecommissioningList getList07() {
		return rm.getDecommissioningList(list_07);
	}

	public DecommissioningList getList08() {
		return rm.getDecommissioningList(list_08);
	}

	public DecommissioningList getList09() {
		return rm.getDecommissioningList(list_09);
	}

	public DecommissioningList getList10() {
		return rm.getDecommissioningList(list_10);
	}

	public DecommissioningList getList11() {
		return rm.getDecommissioningList(list_11);
	}

	public DecommissioningList getList12() {
		return rm.getDecommissioningList(list_12);
	}

	public DecommissioningList getList13() {
		return rm.getDecommissioningList(list_13);
	}

	public DecommissioningList getList14() {
		return rm.getDecommissioningList(list_14);
	}

	public DecommissioningList getList15() {
		return rm.getDecommissioningList(list_15);
	}

	public DecommissioningList getList16() {
		return rm.getDecommissioningList(list_16);
	}

	public DecommissioningList getList17() {
		return rm.getDecommissioningList(list_17);
	}

	public DecommissioningList getList18() {
		return rm.getDecommissioningList(list_18);
	}

	public DecommissioningList getList19() {
		return rm.getDecommissioningList(list_19);
	}

	public DecommissioningList getList20() {
		return rm.getDecommissioningList(list_20);
	}

	public DecommissioningList getList21() {
		return rm.getDecommissioningList(list_21);
	}

	public DecommissioningList getList23() {
		return rm.getDecommissioningList(list_23);
	}

	public DecommissioningList getList24() {
		return rm.getDecommissioningList(list_24);
	}

	public DecommissioningList getList25() {
		return rm.getDecommissioningList(list_25);
	}

	public DecommissioningList getList26() {
		return rm.getDecommissioningList(list_26);
	}

	public DecommissioningList getList30() {
		return rm.getDecommissioningList(list_30);
	}

	public DecommissioningList getList31() {
		return rm.getDecommissioningList(list_31);
	}

	public DecommissioningList getList32() {
		return rm.getDecommissioningList(list_32);
	}

	public DecommissioningList getList33() {
		return rm.getDecommissioningList(list_33);
	}

	public DecommissioningList getList34() {
		return rm.getDecommissioningList(list_34);
	}

	public DecommissioningList getList35() {
		return rm.getDecommissioningList(list_35);
	}

	public DecommissioningList getList36() {
		return rm.getDecommissioningList(list_36);
	}

	public VariableRetentionPeriod getVariableRetentionPeriod_42() {
		return rm.getVariableRetentionPeriodWithCode("42");
	}

	public VariableRetentionPeriod getVariableRetentionPeriod_666() {
		return rm.getVariableRetentionPeriodWithCode("666");
	}

	public Folder getFolder_A01() {
		return rm.getFolder(folder_A01);
	}

	public Folder getFolder_A02() {
		return rm.getFolder(folder_A02);
	}

	public Folder getFolder_A03() {
		return rm.getFolder(folder_A03);
	}

	public Folder getFolder_A04() {
		return rm.getFolder(folder_A04);
	}

	public Folder getFolder_A05() {
		return rm.getFolder(folder_A05);
	}

	public Folder getFolder_A06() {
		return rm.getFolder(folder_A06);
	}

	public Folder getFolder_A07() {
		return rm.getFolder(folder_A07);
	}

	public Folder getFolder_A08() {
		return rm.getFolder(folder_A08);
	}

	public Folder getFolder_A09() {
		return rm.getFolder(folder_A09);
	}

	public Folder getFolder_A10() {
		return rm.getFolder(folder_A10);
	}

	public Folder getFolder_A11() {
		return rm.getFolder(folder_A11);
	}

	public Folder getFolder_A12() {
		return rm.getFolder(folder_A12);
	}

	public Folder getFolder_A13() {
		return rm.getFolder(folder_A13);
	}

	public Folder getFolder_A14() {
		return rm.getFolder(folder_A14);
	}

	public Folder getFolder_A15() {
		return rm.getFolder(folder_A15);
	}

	public Folder getFolder_A16() {
		return rm.getFolder(folder_A16);
	}

	public Folder getFolder_A17() {
		return rm.getFolder(folder_A17);
	}

	public Folder getFolder_A18() {
		return rm.getFolder(folder_A18);
	}

	public Folder getFolder_A19() {
		return rm.getFolder(folder_A19);
	}

	public Folder getFolder_A20() {
		return rm.getFolder(folder_A20);
	}

	public Folder getFolder_A21() {
		return rm.getFolder(folder_A21);
	}

	public Folder getFolder_A22() {
		return rm.getFolder(folder_A22);
	}

	public Folder getFolder_A23() {
		return rm.getFolder(folder_A23);
	}

	public Folder getFolder_A24() {
		return rm.getFolder(folder_A24);
	}

	public Folder getFolder_A25() {
		return rm.getFolder(folder_A25);
	}

	public Folder getFolder_A26() {
		return rm.getFolder(folder_A26);
	}

	public Folder getFolder_A27() {
		return rm.getFolder(folder_A27);
	}

	public Folder getFolder_A42() {
		return rm.getFolder(folder_A42);
	}

	public Folder getFolder_A43() {
		return rm.getFolder(folder_A43);
	}

	public Folder getFolder_A44() {
		return rm.getFolder(folder_A44);
	}

	public Folder getFolder_A45() {
		return rm.getFolder(folder_A45);
	}

	public Folder getFolder_A46() {
		return rm.getFolder(folder_A46);
	}

	public Folder getFolder_A47() {
		return rm.getFolder(folder_A47);
	}

	public Folder getFolder_A48() {
		return rm.getFolder(folder_A48);
	}

	public Folder getFolder_A49() {
		return rm.getFolder(folder_A49);
	}

	public Folder getFolder_A50() {
		return rm.getFolder(folder_A50);
	}

	public Folder getFolder_A51() {
		return rm.getFolder(folder_A51);
	}

	public Folder getFolder_A52() {
		return rm.getFolder(folder_A52);
	}

	public Folder getFolder_A53() {
		return rm.getFolder(folder_A53);
	}

	public Folder getFolder_A54() {
		return rm.getFolder(folder_A54);
	}

	public Folder getFolder_A55() {
		return rm.getFolder(folder_A55);
	}

	public Folder getFolder_A56() {
		return rm.getFolder(folder_A56);
	}

	public Folder getFolder_A57() {
		return rm.getFolder(folder_A57);
	}

	public Folder getFolder_A58() {
		return rm.getFolder(folder_A58);
	}

	public Folder getFolder_A59() {
		return rm.getFolder(folder_A59);
	}

	public Folder getFolder_A79() {
		return rm.getFolder(folder_A79);
	}

	public Folder getFolder_A80() {
		return rm.getFolder(folder_A80);
	}

	public Folder getFolder_A81() {
		return rm.getFolder(folder_A81);
	}

	public Folder getFolder_A82() {
		return rm.getFolder(folder_A82);
	}

	public Folder getFolder_A83() {
		return rm.getFolder(folder_A83);
	}

	public Folder getFolder_A84() {
		return rm.getFolder(folder_A84);
	}

	public Folder getFolder_A85() {
		return rm.getFolder(folder_A85);
	}

	public Folder getFolder_A86() {
		return rm.getFolder(folder_A86);
	}

	public Folder getFolder_A87() {
		return rm.getFolder(folder_A87);
	}

	public Folder getFolder_A88() {
		return rm.getFolder(folder_A88);
	}

	public Folder getFolder_A89() {
		return rm.getFolder(folder_A89);
	}

	public Folder getFolder_A90() {
		return rm.getFolder(folder_A90);
	}

	public Folder getFolder_A91() {
		return rm.getFolder(folder_A91);
	}

	public Folder getFolder_A92() {
		return rm.getFolder(folder_A92);
	}

	public Folder getFolder_A93() {
		return rm.getFolder(folder_A93);
	}

	public Folder getFolder_A94() {
		return rm.getFolder(folder_A94);
	}

	public Folder getFolder_A95() {
		return rm.getFolder(folder_A95);
	}

	public Folder getFolder_A96() {
		return rm.getFolder(folder_A96);
	}

	public Folder getFolder_B01() {
		return rm.getFolder(folder_B01);
	}

	public Folder getFolder_B02() {
		return rm.getFolder(folder_B02);
	}

	public Folder getFolder_B03() {
		return rm.getFolder(folder_B03);
	}

	public Folder getFolder_B04() {
		return rm.getFolder(folder_B04);
	}

	public Folder getFolder_B05() {
		return rm.getFolder(folder_B05);
	}

	public Folder getFolder_B06() {
		return rm.getFolder(folder_B06);
	}

	public Folder getFolder_B07() {
		return rm.getFolder(folder_B07);
	}

	public Folder getFolder_B08() {
		return rm.getFolder(folder_B08);
	}

	public Folder getFolder_B09() {
		return rm.getFolder(folder_B09);
	}

	public Folder getFolder_B30() {
		return rm.getFolder(folder_B30);
	}

	public Folder getFolder_B31() {
		return rm.getFolder(folder_B31);
	}

	public Folder getFolder_B32() {
		return rm.getFolder(folder_B32);
	}

	public Folder getFolder_B33() {
		return rm.getFolder(folder_B33);
	}

	public Folder getFolder_B34() {
		return rm.getFolder(folder_B34);
	}

	public Folder getFolder_B35() {
		return rm.getFolder(folder_B35);
	}

	public Folder getFolder_B50() {
		return rm.getFolder(folder_B50);
	}

	public Folder getFolder_B51() {
		return rm.getFolder(folder_B51);
	}

	public Folder getFolder_B52() {
		return rm.getFolder(folder_B52);
	}

	public Folder getFolder_B53() {
		return rm.getFolder(folder_B53);
	}

	public Folder getFolder_B54() {
		return rm.getFolder(folder_B54);
	}

	public Folder getFolder_B55() {
		return rm.getFolder(folder_B55);
	}

	public Folder getFolder_C01() {
		return rm.getFolder(folder_C01);
	}

	public Folder getFolder_C02() {
		return rm.getFolder(folder_C02);
	}

	public Folder getFolder_C03() {
		return rm.getFolder(folder_C03);
	}

	public Folder getFolder_C04() {
		return rm.getFolder(folder_C04);
	}

	public Folder getFolder_C05() {
		return rm.getFolder(folder_C05);
	}

	public Folder getFolder_C06() {
		return rm.getFolder(folder_C06);
	}

	public Folder getFolder_C07() {
		return rm.getFolder(folder_C07);
	}

	public Folder getFolder_C08() {
		return rm.getFolder(folder_C08);
	}

	public Folder getFolder_C09() {
		return rm.getFolder(folder_C09);
	}

	public Folder getFolder_C30() {
		return rm.getFolder(folder_C30);
	}

	public Folder getFolder_C31() {
		return rm.getFolder(folder_C31);
	}

	public Folder getFolder_C32() {
		return rm.getFolder(folder_C32);
	}

	public Folder getFolder_C33() {
		return rm.getFolder(folder_C33);
	}

	public Folder getFolder_C34() {
		return rm.getFolder(folder_C34);
	}

	public Folder getFolder_C35() {
		return rm.getFolder(folder_C35);
	}

	public Folder getFolder_C50() {
		return rm.getFolder(folder_C50);
	}

	public Folder getFolder_C51() {
		return rm.getFolder(folder_C51);
	}

	public Folder getFolder_C52() {
		return rm.getFolder(folder_C52);
	}

	public Folder getFolder_C53() {
		return rm.getFolder(folder_C53);
	}

	public Folder getFolder_C54() {
		return rm.getFolder(folder_C54);
	}

	public Folder getFolder_C55() {
		return rm.getFolder(folder_C55);
	}

	public Document getDocumentWithContent_A19() {
		return rm.getDocument(document_A19);
	}

	public Document getDocumentWithContent_A49() {
		return rm.getDocument(document_A49);
	}

	public Document getDocumentWithContent_B30() {
		return rm.getDocument(document_B30);
	}

	public Document getDocumentWithContent_B33() {
		return rm.getDocument(document_B33);
	}

	public Document getDocumentWithContent_A79() {
		return rm.getDocument(document_A79);
	}

	public Users getUsers() {
		return users;
	}

	public Group getLegends() {
		return users.legendsIn(collection);
	}

	public Group getHeroes() {
		return users.heroesIn(collection);
	}

	public String[] folder_A(int from, int to) {

		List<String> ids = new ArrayList<>();
		for (int i = from; i <= to; i++) {

			String folderId;
			if (i < 10) {
				folderId = "A0" + i;
			} else {
				folderId = "A" + i;
			}

			ids.add(folderId);

		}

		return ids.toArray(new String[0]);
	}

	public String[] decommissionnableContractsInFolder_A(int from, int to) {

		List<String> ids = new ArrayList<>();
		for (int i = from; i <= to; i++) {

			String folderId;
			if (i < 10) {
				folderId = "A0" + i;
			} else {
				folderId = "A" + i;
			}

			ids.add(folderId + "_paperContractWithDifferentCopy");
			ids.add(folderId + "_numericContractWithDifferentCopy");

		}

		return ids.toArray(new String[0]);
	}

	public String[] decommissionnableProcesInFolder_A(int from, int to) {

		List<String> ids = new ArrayList<>();
		for (int i = from; i <= to; i++) {

			String folderId;
			if (i < 10) {
				folderId = "A0" + i;
			} else {
				folderId = "A" + i;
			}

			ids.add(folderId + "_paperProcesWithDifferentCopy");
			ids.add(folderId + "_numericProcesWithDifferentCopy");

		}

		return ids.toArray(new String[0]);
	}

	public List<String> folders(String idsStr) {
		List<String> ids = new ArrayList<>();
		for (String part : idsStr.split(",")) {
			part = part.trim();
			if (part.contains("-")) {
				String fromStr = part.split("-")[0];
				String toStr = part.split("-")[1];
				String filingSpace = fromStr.substring(0, 1);
				int from = Integer.valueOf(fromStr.substring(1));
				int to = Integer.valueOf(toStr.substring(1));
				for (int i = from; i <= to; i++) {
					if (i < 10) {
						ids.add(filingSpace + "0" + i);
					} else {
						ids.add(filingSpace + i);
					}
				}
			} else {
				ids.add(part);
			}
		}

		return ids;
	}

	public FolderType folderTypeEmploye() {
		return rm.getFolderTypeWithCode("employe");
	}

	public FolderType folderTypeMeeting() {
		return rm.getFolderTypeWithCode("meetingFolder");
	}

	public FolderType folderTypeOther() {
		return rm.getFolderTypeWithCode("other");
	}

	public DocumentType documentTypeForm() {
		return rm.getDocumentTypeByCode("form");
	}

	public DocumentType documentTypeReport() {
		return rm.getDocumentTypeByCode("report");
	}

	public DocumentType documentTypeOther() {
		return rm.getDocumentTypeByCode("other");
	}

	public TaskType taskTypeForm() {
		return tasks.getTaskTypeByCode("criticalTask");
	}

	public TaskType taskTypeReport() {
		return tasks.getTaskTypeByCode("communicationTask");
	}

	public TaskType taskTypeOther() {
		return tasks.getTaskTypeByCode("other");
	}

	public Folder newFolderWithValues() {
		return newFolderWithValuesAndId(null);
	}

	public Folder newFolderWithValuesAndId(String id) {
		Folder folder = newFolder(id);
		folder.setAdministrativeUnitEntered(unitId_11b);
		folder.setCategoryEntered(categoryId_X);
		folder.setRetentionRuleEntered(ruleId_2);
		folder.setCopyStatusEntered(CopyType.PRINCIPAL);
		folder.setTitle(id == null ? "A folder" : ("Folder " + id));
		folder.setOpenDate(LocalDate.now());
		return folder;
	}

	public Folder newChildFolderIn(Folder parent) {
		return newChildFolderWithIdIn(null, parent);
	}

	public Folder newChildFolderWithIdIn(String id, Folder parent) {
		Folder folder = newFolder(id);
		folder.setParentFolder(parent);
		folder.setTitle(id == null ? "A child folder" : ("child folder " + id));
		folder.setOpenDate(LocalDate.now());
		return folder;
	}

	public Document newDocumentIn(Folder parent) {
		return newDocumentWithIdIn(null, parent);
	}

	public Document newDocumentWithIdIn(String id, Folder parent) {
		Document document = rm.newDocumentWithId(id);
		document.setFolder(parent);
		document.setTitle(id == null ? "A document" : ("document " + id));
		return document;
	}
}
