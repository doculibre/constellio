package com.constellio.app.modules.rm;

import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.logging.DecommissioningLoggingService;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.rm.model.enums.CopyType.PRINCIPAL;
import static com.constellio.app.modules.rm.model.enums.CopyType.SECONDARY;
import static com.constellio.app.modules.rm.model.enums.DecommissioningType.DEPOSIT;
import static com.constellio.app.modules.rm.model.enums.DecommissioningType.DESTRUCTION;
import static com.constellio.app.modules.rm.model.enums.DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static java.util.Arrays.asList;

public class DemoTestRecords {

	private int id = 4200;

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();

	private String nextId() {
		String idWithTooMuchZeros = "0000000" + (id++);
		return idWithTooMuchZeros.substring(idWithTooMuchZeros.length() - 10);
	}

	//1. Changer tous les ids hardcodés par des nextId();

	public final String categoryId_20 = nextId();
	public final String categoryId_21 = nextId();
	public final String categoryId_231 = nextId();
	public final String categoryId_232 = nextId();
	public final String categoryId_233 = nextId();
	public final String categoryId_234 = nextId();
	public final String categoryId_22 = nextId();
	public final String categoryId_23 = nextId();

	public final String categoryId_10 = nextId();
	public final String categoryId14 = nextId();
	public final String categoryId_1000 = nextId();
	public final String categoryId_1100 = nextId();
	public final String categoryId_2000 = nextId();
	public final String categoryId_2100 = nextId();
	public final String categoryId_2110 = nextId();
	public final String categoryId_2120 = nextId();
	public final String categoryId_2130 = nextId();
	public final String categoryId_3000 = nextId();
	public final String categoryId_3100 = nextId();
	public final String categoryId_3200 = nextId();
	public final String categoryId_4000 = nextId();
	public final String categoryId_4100 = nextId();
	public final String categoryId_4200 = nextId();
	public final String categoryId_13 = nextId();

	public final String unitId_10 = nextId();
	public final String unitId_10A = nextId();
	public final String unitId_10B = nextId();
	public final String unitId_10C = nextId();
	public final String unitId_10D = nextId();
	public final String unitId_10E = nextId();

	public final String subdivId_1 = nextId();
	public final String subdivId_2 = nextId();
	public final String subdivId_3 = nextId();

	public final String ruleId_1 = nextId();
	public final String ruleId_2 = nextId();
	public final String ruleId_3 = nextId();
	public final String ruleId_4 = nextId();

	public final String storageSpaceId_S01 = nextId();
	public final String storageSpaceId_S01_01 = nextId();
	public final String storageSpaceId_S01_02 = nextId();
	public final String storageSpaceId_S02 = nextId();
	public final String storageSpaceId_S02_01 = nextId();
	public final String storageSpaceId_S02_02 = nextId();

	public final String containerTypeId_boite22x22 = nextId();

	public final String containerId_bac19 = nextId();
	public final String containerId_bac18 = nextId();
	public final String containerId_bac17 = nextId();
	public final String containerId_bac16 = nextId();
	public final String containerId_bac15 = nextId();
	public final String containerId_bac14 = nextId();

	public final String containerId_bac13 = nextId();
	public final String containerId_bac12 = nextId();
	public final String containerId_bac11 = nextId();
	public final String containerId_bac10 = nextId();
	public final String containerId_bac09 = nextId();
	public final String containerId_bac08 = nextId();
	public final String containerId_bac07 = nextId();
	public final String containerId_bac06 = nextId();
	public final String containerId_bac05 = nextId();
	public final String containerId_bac04 = nextId();
	public final String containerId_bac03 = nextId();
	public final String containerId_bac02 = nextId();
	public final String containerId_bac01 = nextId();

	public String PA;
	public String MV;
	public String MD;
	public List<String> PA_MD;
	public final String folder_A01 = nextId();
	public final String folder_A02 = nextId();
	public final String folder_A03 = nextId();
	public final String folder_A04 = nextId();
	public final String folder_A05 = nextId();
	public final String folder_A06 = nextId();
	public final String folder_A07 = nextId();
	public final String folder_A08 = nextId();
	public final String folder_A09 = nextId();
	public final String folder_A10 = nextId();
	public final String folder_A11 = nextId();
	public final String folder_A12 = nextId();
	public final String folder_A13 = nextId();
	public final String folder_A14 = nextId();
	public final String folder_A15 = nextId();
	public final String folder_A16 = nextId();
	public final String folder_A17 = nextId();
	public final String folder_A18 = nextId();
	public final String folder_A19 = nextId();
	public final String folder_A20 = nextId();
	public final String folder_A21 = nextId();
	public final String folder_A22 = nextId();
	public final String folder_A23 = nextId();
	public final String folder_A24 = nextId();
	public final String folder_A25 = nextId();
	public final String folder_A26 = nextId();
	public final String folder_A27 = nextId();
	public final String folder_A42 = nextId();
	public final String folder_A43 = nextId();
	public final String folder_A44 = nextId();
	public final String folder_A45 = nextId();
	public final String folder_A46 = nextId();
	public final String folder_A47 = nextId();
	public final String folder_A48 = nextId();
	public final String folder_A49 = nextId();
	public final String folder_A50 = nextId();
	public final String folder_A51 = nextId();
	public final String folder_A52 = nextId();
	public final String folder_A53 = nextId();
	public final String folder_A54 = nextId();
	public final String folder_A55 = nextId();
	public final String folder_A56 = nextId();
	public final String folder_A57 = nextId();
	public final String folder_A58 = nextId();
	public final String folder_A59 = nextId();
	public final String folder_A79 = nextId();
	public final String folder_A80 = nextId();
	public final String folder_A81 = nextId();
	public final String folder_A82 = nextId();
	public final String folder_A83 = nextId();
	public final String folder_A84 = nextId();
	public final String folder_A85 = nextId();
	public final String folder_A86 = nextId();
	public final String folder_A87 = nextId();
	public final String folder_A88 = nextId();
	public final String folder_A89 = nextId();
	public final String folder_A90 = nextId();
	public final String folder_A91 = nextId();
	public final String folder_A92 = nextId();
	public final String folder_A93 = nextId();
	public final String folder_A94 = nextId();
	public final String folder_A95 = nextId();
	public final String folder_A96 = nextId();
	public final String folder_B01 = nextId();
	public final String folder_B02 = nextId();
	public final String folder_B03 = nextId();
	public final String folder_B04 = nextId();
	public final String folder_B05 = nextId();
	public final String folder_B06 = nextId();
	public final String folder_B07 = nextId();
	public final String folder_B08 = nextId();
	public final String folder_B09 = nextId();
	public final String folder_B30 = nextId();
	public final String folder_B31 = nextId();
	public final String folder_B32 = nextId();
	public final String folder_B33 = nextId();
	public final String folder_B34 = nextId();
	public final String folder_B35 = nextId();
	public final String folder_B50 = nextId();
	public final String folder_B51 = nextId();
	public final String folder_B52 = nextId();
	public final String folder_B53 = nextId();
	public final String folder_B54 = nextId();
	public final String folder_B55 = nextId();
	public final String folder_C01 = nextId();
	public final String folder_C02 = nextId();
	public final String folder_C03 = nextId();
	public final String folder_C04 = nextId();
	public final String folder_C05 = nextId();
	public final String folder_C06 = nextId();
	public final String folder_C07 = nextId();
	public final String folder_C08 = nextId();
	public final String folder_C09 = nextId();
	public final String folder_C30 = nextId();
	public final String folder_C31 = nextId();
	public final String folder_C32 = nextId();
	public final String folder_C33 = nextId();
	public final String folder_C34 = nextId();
	public final String folder_C35 = nextId();
	public final String folder_C50 = nextId();
	public final String folder_C51 = nextId();
	public final String folder_C52 = nextId();
	public final String folder_C53 = nextId();
	public final String folder_C54 = nextId();
	public final String folder_C55 = nextId();

	public final String list_01 = nextId();
	public final String list_02 = nextId();
	public final String list_03 = nextId();
	public final String list_04 = nextId();
	public final String list_05 = nextId();
	public final String list_06 = nextId();
	public final String list_07 = nextId();
	public final String list_08 = nextId();
	public final String list_09 = nextId();
	public final String list_10 = nextId();
	public final String list_11 = nextId();
	public final String list_12 = nextId();
	public final String list_13 = nextId();
	public final String list_14 = nextId();
	public final String list_15 = nextId();
	public final String list_16 = nextId();
	public final String list_17 = nextId();

	private String collection;

	private RMSchemasRecordsServices schemas;

	private String alice_notInCollection;

	private String admin_userIdWithAllAccess;

	private String bob_userInAC;

	private String charles_userInA;

	private String dakota_managerInA_userInB;

	private String edouard_managerInB_userInC;

	private String gandalf_managerInABC;

	private String chuckNorris;

	private Users users = new Users();

	private ModelLayerFactory modelLayerFactory;

	private AppLayerFactory appLayerFactory;

	private RecordServices recordServices;

	private LoggingServices loggingServices;

	private DecommissioningLoggingService decommissioningLoggingService;

	private SystemConfigurationsManager systemConfigurationsManager;
	private SearchServices searchServices;
	private ContentManager contentManager;

	public DemoTestRecords(String collection) {
		this.collection = collection;
	}

	public DemoTestRecords setup(AppLayerFactory appLayerFactory)
			throws RecordServicesException {
		this.appLayerFactory = appLayerFactory;
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();

		UserServices userServices = modelLayerFactory.newUserServices();
		users.setUp(userServices).withPasswords(modelLayerFactory.newAuthenticationService());

		this.modelLayerFactory = modelLayerFactory;
		schemas = new RMSchemasRecordsServices(collection, modelLayerFactory);

		recordServices = modelLayerFactory.newRecordServices();

		loggingServices = modelLayerFactory.newLoggingServices();

		contentManager = modelLayerFactory.getContentManager();

		decommissioningLoggingService = new DecommissioningLoggingService(modelLayerFactory);

		searchServices = modelLayerFactory.newSearchServices();

		PA = schemas.PA();
		MD = schemas.DM();
		MV = schemas.FI();

		String mainDataLanguage = modelLayerFactory.getConfiguration().getMainDataLanguage();

		if ("ar".equals(mainDataLanguage)) {
			PA = schemas.getMediumTypeByCode("ورقي").getId();
			MD = schemas.getMediumTypeByCode("القرص المغناطيسي").getId();
			MV = schemas.getMediumTypeByCode("فيلم").getId();
		}

		PA_MD = asList(PA, MD);

		systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		systemConfigurationsManager.setValue(RMConfigs.ENFORCE_CATEGORY_AND_RULE_RELATIONSHIP_IN_FOLDER, false);

		Transaction transaction = new Transaction();
		setupUsers(transaction, userServices);
		setupCategories(transaction);
		setupUniformSubdivisions(transaction);
		setupAdministrativeUnits(transaction);
		setupRetentionRules(transaction);

		recordServices.execute(transaction);
		setupAdministrativeUnitsAuthorizations();
		//setupAuthorizations(modelLayerFactory.newAuthorizationsServices(), modelLayerFactory.getRolesManager());
		waitForBatchProcesses(modelLayerFactory.getBatchProcessesManager());

		return this;
	}

	public DemoTestRecords alreadySettedUp(ModelLayerFactory modelLayerFactory) {
		UserServices userServices = modelLayerFactory.newUserServices();
		users.setUp(userServices);
		alice_notInCollection = users.aliceIn(collection).getId();
		admin_userIdWithAllAccess = users.adminIn(collection).getId();
		bob_userInAC = users.bobIn(collection).getId();
		charles_userInA = users.charlesIn(collection).getId();
		dakota_managerInA_userInB = users.dakotaIn(collection).getId();
		edouard_managerInB_userInC = users.edouardIn(collection).getId();
		gandalf_managerInABC = users.gandalfIn(collection).getId();
		chuckNorris = users.chuckNorrisIn(collection).getId();
		schemas = new RMSchemasRecordsServices(collection, appLayerFactory);
		recordServices = modelLayerFactory.newRecordServices();
		loggingServices = modelLayerFactory.newLoggingServices();
		decommissioningLoggingService = new DecommissioningLoggingService(modelLayerFactory);
		searchServices = modelLayerFactory.newSearchServices();
		contentManager = modelLayerFactory.getContentManager();
		systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();

		PA = schemas.PA();
		MD = schemas.DM();
		MV = schemas.FI();
		PA_MD = asList(PA, MD);

		return this;
	}

	private void waitForBatchProcesses(BatchProcessesManager batchProcessesManager) {
		for (BatchProcess batchProcess : batchProcessesManager.getAllNonFinishedBatchProcesses()) {
			batchProcessesManager.waitUntilFinished(batchProcess);
		}
	}

	private void setupAdministrativeUnitsAuthorizations() {

		List<String> userInUnit10A = asList(bob_userInAC, charles_userInA, admin_userIdWithAllAccess);
		List<String> managerInUnit10A = asList(dakota_managerInA_userInB, gandalf_managerInABC, admin_userIdWithAllAccess);

		List<String> userInUnit10B = asList(dakota_managerInA_userInB);
		List<String> managerInUnit10B = asList(edouard_managerInB_userInC, gandalf_managerInABC);

		List<String> userInUnit10C = asList(edouard_managerInB_userInC, bob_userInAC);
		List<String> managerInUnit10C = asList(gandalf_managerInABC);

		List<String> userInUnit10D = new ArrayList<>();
		List<String> managerInUnit10D = new ArrayList<>();

		List<String> userInUnit10E = new ArrayList<>();
		List<String> managerInUnit10E = new ArrayList<>();

		addUserAuthorization(unitId_10A, userInUnit10A);
		addManagerAuthorization(unitId_10A, managerInUnit10A);

		addUserAuthorization(unitId_10B, userInUnit10B);
		addManagerAuthorization(unitId_10B, managerInUnit10B);

		addUserAuthorization(unitId_10C, userInUnit10C);
		addManagerAuthorization(unitId_10C, managerInUnit10C);

		addUserAuthorization(unitId_10D, userInUnit10D);
		addManagerAuthorization(unitId_10D, managerInUnit10D);

		addUserAuthorization(unitId_10E, userInUnit10E);
		addManagerAuthorization(unitId_10E, managerInUnit10E);

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

	private void setupUsers(Transaction transaction, UserServices userServices) {

		userServices.execute(users.admin().getUsername(), (req) -> req.addCollection(collection));
		userServices.execute(users.bob().getUsername(), (req) -> req.addCollection(collection));
		userServices.execute(users.charles().getUsername(), (req) -> req.addCollection(collection));
		userServices.execute(users.dakotaLIndien().getUsername(), (req) -> req.addCollection(collection));
		userServices.execute(users.edouardLechat().getUsername(), (req) -> req.addCollection(collection));
		userServices.execute(users.gandalfLeblanc().getUsername(), (req) -> req.addCollection(collection));
		userServices.execute(users.chuckNorris().getUsername(), (req) -> req.addCollection(collection));

		alice_notInCollection = users.alice().getUsername();

		LocalDateTime now = new LocalDateTime();

		admin_userIdWithAllAccess = transaction.add(users.adminIn(collection)).setCollectionDeleteAccess(true)
				.setCollectionReadAccess(true).setCollectionWriteAccess(true).setUserRoles(asList(RMRoles.RGD))
				.setLastLogin(now).getId();

		bob_userInAC = transaction.add(users.bobIn(collection)).setUserRoles(asList(RMRoles.USER)).setLastLogin(now).getId();
		charles_userInA = transaction.add(users.charlesIn(collection)).setUserRoles(asList(RMRoles.USER)).setLastLogin(now)
				.getId();
		dakota_managerInA_userInB = transaction.add(users.dakotaLIndienIn(collection)).setUserRoles(asList(RMRoles.USER))
				.setLastLogin(now).getId();
		edouard_managerInB_userInC = transaction.add(users.edouardLechatIn(collection)).setUserRoles(asList(RMRoles.USER))
				.setLastLogin(now).getId();
		gandalf_managerInABC = transaction.add(users.gandalfLeblancIn(collection)).setUserRoles(asList(RMRoles.USER))
				.setLastLogin(now).getId();
		chuckNorris = transaction
				.add(users.chuckNorrisIn(collection).setUserRoles(asList(RMRoles.USER)).setCollectionAllAccess(true))
				.setLastLogin(now).getId();

	}

	private void setupCategories(Transaction transaction) {

		transaction.add(schemas.newCategoryWithId(categoryId_10).setCode("10")
				.setTitle("Gestion Interne")
				.setDescription("Documents relatifs à la gestion interne de l'entreprise"));
		//Ressources Humaines
		transaction.add(schemas.newCategoryWithId(categoryId14).setCode("14")
				.setTitle("Gestion des Ressources Humaines")
				.setDescription("Documents relatifs à la gestion interne des ressources humaines de l'entreprise")
				.setParent(categoryId_10).setRetentionRules(asList(ruleId_1)));

		transaction.add(schemas.newCategoryWithId(categoryId_1000).setCode("1000")
				.setTitle("Planification des Ressources Humaines")
				.setDescription("Documents relatifs à la planification des ressources humaines de l'entreprise")
				.setParent(categoryId14).setRetentionRules(asList(ruleId_1, ruleId_2)));

		transaction.add(schemas.newCategoryWithId(categoryId_1100).setCode("1100")
				.setTitle("Analyse des besoins des Ressources Humaines")
				.setDescription("Documents relatifs à l'analyse des besoins des ressources humaines")
				.setParent(categoryId_1000));

		transaction.add(schemas.newCategoryWithId(categoryId_2000).setCode("2000")
				.setTitle("Organisation des Ressources Humaines")
				.setDescription("Documents relatifs à l'organisation des ressources humaines")
				.setParent(categoryId14).setRetentionRules(asList(ruleId_4)));

		transaction.add(schemas.newCategoryWithId(categoryId_2100).setCode("2100")
				.setTitle("Embauche du Personnel")
				.setDescription("Documents relatifs à l'embauche du personnel")
				.setParent(categoryId_2000));

		transaction.add(schemas.newCategoryWithId(categoryId_2110).setCode("2110")
				.setTitle("Recrutement à l'interne")
				.setDescription("Documents relatifs au recrutement à l'interne")
				.setParent(categoryId_2100));

		transaction.add(schemas.newCategoryWithId(categoryId_2120).setCode("2120")
				.setTitle("Recrutement à l'externe")
				.setDescription("Documents relatifs au recrutement à l'externe")
				.setParent(categoryId_2100));

		transaction.add(schemas.newCategoryWithId(categoryId_2130).setCode("2130")
				.setTitle("Affichage de Postes")
				.setDescription("Documents relatifs à l'affichage de postes")
				.setParent(categoryId_2100));

		transaction.add(schemas.newCategoryWithId(categoryId_3000).setCode("3000")
				.setTitle("Administration des Ressources Humaines")
				.setDescription("Documents relatifs à l'administration des ressources humaines")
				.setParent(categoryId14));

		transaction.add(schemas.newCategoryWithId(categoryId_3100).setCode("3100")
				.setTitle("Dossiers du Personnel")
				.setDescription("Documents et dossiers du personnel")
				.setParent(categoryId_3000));

		transaction.add(schemas.newCategoryWithId(categoryId_3200).setCode("3200")
				.setTitle("Formation et Perfectionnement du Personnel")
				.setDescription("Documents relatifs à la formation et le perfectionnement du personnel")
				.setParent(categoryId_3000));

		transaction.add(schemas.newCategoryWithId(categoryId_4000).setCode("4000")
				.setTitle("Contrôle des Ressources Humaines")
				.setDescription("Documents relatifs au contrôle des ressources rumaines")
				.setParent(categoryId14));

		transaction.add(schemas.newCategoryWithId(categoryId_4100).setCode("4100")
				.setTitle("Évaluation des Ressources Humaines")
				.setDescription("Documents relatifs à l'évaluation des ressources humaines")
				.setParent(categoryId_4000));

		transaction.add(schemas.newCategoryWithId(categoryId_4200).setCode("4200")
				.setTitle("Mouvement du Personnel")
				.setDescription("Documents relatifs à la mouvement du personnel")
				.setParent(categoryId_4000));
		//Web
		transaction.add(schemas.newCategoryWithId(categoryId_13).setCode("13")
				.setTitle("Gestion du Site vitrine")
				.setDescription("Documents relatifs à la gestion du site vitrine")
				.setParent(categoryId_10).setRetentionRules(asList(ruleId_1, ruleId_2, ruleId_3, ruleId_4)));

		transaction.add(schemas.newCategoryWithId(categoryId_20).setCode("20")
				.setTitle("Gestion Externe")
				.setDescription("Documents relatifs à la gestion externe"));

		//Clients
		transaction.add(schemas.newCategoryWithId(categoryId_21).setCode("21")
				.setTitle("Documents concernant la gestion des fichiers Clients")
				.setDescription("Documents relatifs à la gestion des fichiers clients")
				.setParent(categoryId_20));

		//Fournisseurs
		transaction.add(schemas.newCategoryWithId(categoryId_22).setCode("22")
				.setTitle("Gestion des fichiers Fournisseurs")
				.setDescription("Documents relatifs à la gestion des fichiers fournisseurs")
				.setParent(categoryId_20));

		//Assurances
		transaction.add(schemas.newCategoryWithId(categoryId_23).setCode("23")
				.setTitle("Gestion des fichiers d'Assurance")
				.setDescription("Documents relatifs à la gestion des fichiers d'assurance des employés")
				.setParent(categoryId_20).setRetentionRules(asList(ruleId_1, ruleId_2, ruleId_3, ruleId_4)));

		transaction.add(schemas.newCategoryWithId(categoryId_231).setCode("231")
				.setTitle("Gestion des voitures de fonction")
				.setDescription("Documents relatifs à la gestion des voitures de fonction de l'entreprise")
				.setParent(categoryId_23).setRetentionRules(asList(ruleId_2)));

		transaction.add(schemas.newCategoryWithId(categoryId_232).setCode("232")
				.setTitle("Gestion des dossiers santé des salariés")
				.setDescription("Documents relatifs à la gestion des dossiers santé des salariés de l'entreprise")
				.setParent(categoryId_23));

		transaction.add(schemas.newCategoryWithId(categoryId_233).setCode("233")
				.setTitle("Rapports d'Accidents")
				.setTitle("Documents relatifs aux rapports d'Accidents")
				.setParent(categoryId_23).setRetentionRules(asList(ruleId_3)));

		transaction.add(schemas.newCategoryWithId(categoryId_234).setCode("234")
				.setTitle("Contrats")
				.setTitle("Documents relatifs aux contrats")
				.setParent(categoryId_23)
				.setRetentionRules(asList(ruleId_3)));

	}

	private void setupAdministrativeUnits(Transaction transaction) {
		transaction.add(schemas.newAdministrativeUnitWithId(unitId_10)).setCode("RH")
				.setTitle("Ressources humaines")
				.setDescription("Ressources humaines de l'entreprise")
				.setAdress("1265 Charest O, Suite 1040");

		transaction.add(schemas.newAdministrativeUnitWithId(unitId_10A)).setCode("A")
				.setTitle("Salle A - Planification des Ressources Humaines")
				.setDescription("Salle A - Planification des ressources humaines de l'entreprise")
				.setParent(unitId_10).setAdress("1265 Charest O, Suite 1040");

		transaction.add(schemas.newAdministrativeUnitWithId(unitId_10B)).setCode("B")
				.setTitle("Salle B - Organisation des Ressources Humaines")
				.setDescription("Salle B - Organisation des ressources humaines de l'entreprise")
				.setParent(unitId_10).setAdress("1265 Charest O, Suite 1040");

		transaction.add(schemas.newAdministrativeUnitWithId(unitId_10C)).setCode("C")
				.setTitle("Salle C - Administration des Ressources Humaines")
				.setDescription("Salle C - Administration des ressources humaines de l'entreprise")
				.setParent(unitId_10).setAdress("1265 Charest O, Suite 1040");

		transaction.add(schemas.newAdministrativeUnitWithId(unitId_10D)).setCode("D")
				.setTitle("Salle D - Contrôle des Ressources Humaines")
				.setDescription("Salle D - Contrôle des ressources humaines de l'entreprise")
				.setParent(unitId_10).setAdress("1265 Charest O, Suite 1040");

		transaction.add(schemas.newAdministrativeUnitWithId(unitId_10E)).setCode("E")
				.setTitle("Salle E - Dossiers Semi-Actifs")
				.setDescription("Salle E - Dossiers semi-actifs de l'entreprise")
				.setParent(unitId_10).setAdress("1265 Charest O, Suite 1040");
	}

	private void setupUniformSubdivisions(Transaction transaction) {
		transaction.add(schemas.newUniformSubdivisionWithId(subdivId_1).setCode("sub1").setTitle("Subdiv. 1")
				.setRetentionRules(asList(ruleId_2)));
		transaction.add(schemas.newUniformSubdivisionWithId(subdivId_2).setCode("sub2").setTitle("Subdiv. 2"));
		transaction.add(schemas.newUniformSubdivisionWithId(subdivId_3).setCode("sub3").setTitle("Subdiv. 3"));
	}

	private void setupRetentionRules(Transaction transaction) {
		CopyRetentionRule principal888_5_C = copyBuilder.newPrincipal(asList(PA, MD), "888-5-C");
		CopyRetentionRule secondary888_0_D = copyBuilder.newSecondary(asList(PA, MD), "888-0-D");
		transaction.add(schemas.newRetentionRuleWithId(ruleId_1)).setCode("1").setTitle("Rule #1")
				.setAdministrativeUnits(asList(unitId_10, unitId_10)).setApproved(true)
				.setCopyRetentionRules(asList(principal888_5_C, secondary888_0_D))
				.setKeywords(asList("Rule #1"))
				.setCorpus("Corpus  Rule 1")
				.setDescription("Description Rule 1")
				.setJuridicReference("Juridic reference Rule 1")
				.setGeneralComment("General Comment Rule 1")
				.setCopyRulesComment(asList("Copy rules comments"));

		CopyRetentionRule principal5_2_T = copyBuilder.newPrincipal(asList(PA, MD), "5-2-T");
		CopyRetentionRule secondary2_0_D = copyBuilder.newSecondary(asList(PA, MD), "2-0-D");
		transaction.add(schemas.newRetentionRuleWithId(ruleId_2)).setCode("2").setTitle("Rule #2")
				.setResponsibleAdministrativeUnits(true).setApproved(true)
				.setCopyRetentionRules(asList(principal5_2_T, secondary2_0_D));

		CopyRetentionRule principal999_4_T = copyBuilder.newPrincipal(asList(PA, MD), "999-4-T");
		CopyRetentionRule secondary1_0_D = copyBuilder.newSecondary(asList(PA, MD), "1-0-D");
		transaction.add(schemas.newRetentionRuleWithId(ruleId_3)).setCode("3").setTitle("Rule #3")
				.setResponsibleAdministrativeUnits(true).setApproved(true)
				.setCopyRetentionRules(asList(principal999_4_T, secondary1_0_D));

		CopyRetentionRule principal_PA_3_888_D = copyBuilder.newPrincipal(asList(PA), "3-888-D");
		CopyRetentionRule principal_MD_3_888_C = copyBuilder.newPrincipal(asList(MD), "3-888-C");
		transaction.add(schemas.newRetentionRuleWithId(ruleId_4)).setCode("4").setTitle("Rule #4")
				.setResponsibleAdministrativeUnits(true).setApproved(true)
				.setCopyRetentionRules(asList(principal_PA_3_888_D, principal_MD_3_888_C, secondary888_0_D));
	}

	public DemoTestRecords withFoldersAndContainersOfEveryStatus() {
		return withFoldersAndContainersOfEveryStatus(true);
	}

	public DemoTestRecords withFoldersAndContainersOfEveryStatus(boolean documents) {
		//Calculation of closing date is disabled because we want some folders without close date
		systemConfigurationsManager.setValue(RMConfigs.CALCULATED_CLOSING_DATE, false);
		systemConfigurationsManager.setValue(RMConfigs.YEAR_END_DATE, "10/31");

		Transaction transaction = new Transaction();
		setupStorageSpace(transaction);
		setupContainerTypes(transaction);
		setupContainers(transaction);
		setupFolders(transaction);
		if (documents) {
			setupDocuments(transaction);
		}
		setupLists(transaction);
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		return this;
	}

	private void setupDocuments(Transaction transaction) {

		transaction.add(newDocumentWithContent("cv-EmiliePoulain.odt").setFolder(folder_A01));
		transaction.add(newDocumentWithContent("guide-dev.pdf").setFolder(folder_A03));
		transaction.add(newDocumentWithContent("definition-comptable.pdf").setFolder(folder_A04));
		transaction.add(newDocumentWithContent("definition-commerciaux.pdf").setFolder(folder_A04));
		transaction.add(newDocumentWithContent("info-BastienAugerau.odt").setFolder(folder_A05));
		transaction.add(newDocumentWithContent("info-BastienBernadotte.odt").setFolder(folder_A06));
		transaction.add(newDocumentWithContent("info-BrittanyDaru.odt").setFolder(folder_A07));
		transaction.add(newDocumentWithContent("info-BorisGouvon.odt").setFolder(folder_A08));
		transaction.add(newDocumentWithContent("info-CarolineSuchet.odt").setFolder(folder_A10));
		transaction.add(newDocumentWithContent("info-DocuLibre.odt").setFolder(folder_A11));
		transaction.add(newDocumentWithContent("info-IBM.odt").setFolder(folder_A12));
		transaction.add(newDocumentWithContent("info-Google.odt").setFolder(folder_A13));
		transaction.add(newDocumentWithContent("info-Toyota.pdf").setFolder(folder_A14));
		transaction.add(newDocumentWithContent("assurance-EmiliePoulain.odt").setFolder(folder_A16));
		transaction.add(newDocumentWithContent("formations-internationale.odt").setFolder(folder_A18));

	}

	private Document newDocumentWithContent(String resource) {
		User user = users.adminIn(collection);
		ContentVersionDataSummary version = upload(resource);
		Content content = contentManager.createMajor(user, resource, version);

		return schemas.newDocument().setTitle(resource).setContent(content);
	}

	//
	//	public DemoTestRecords withEvents() {
	//		createRecordsEvents();
	//		createViewEvents();
	//		createDecommissioningEvents();
	//		createPermissionEvents();
	//		createBorrowAndReturnEvents();
	//		createLoginEvents();
	//		recordServices.flush();
	//		return this;
	//	}
	//
	//	private void createViewEvents() {
	//		User charles = users.charlesIn(collection);
	//		loggingServices.logRecordView(getFolder_A02().getWrappedRecord(), charles);
	//	}

	private void createLoginEvents() {
		User admin = users.adminIn(collection);
		loggingServices.login(admin);
		User charles = users.charlesIn(collection);
		loggingServices.login(charles);
		loggingServices.logout(charles);
	}

	//	private void createRecordsEvents() {
	//		Transaction transaction = new Transaction();
	//		User charles = users.charlesIn(collection);
	//		transaction
	//				.add(createEvent(charles.getBorrowerUsername(), EventType.CREATE_FOLDER, new LocalDateTime().minusDays(1), folder_A01));
	//		transaction.add(createEvent(charles.getBorrowerUsername(), EventType.CREATE_DOCUMENT, new LocalDateTime().minusDays(1), "11"));
	//		transaction
	//				.add(createEvent(charles.getBorrowerUsername(), EventType.MODIFY_DOCUMENT, new LocalDateTime().minusDays(1), folder_A03));
	//		transaction.add(createEvent(charles.getBorrowerUsername(), EventType.MODIFY_FOLDER, new LocalDateTime().minusDays(2), "13"));
	//		transaction
	//				.add(createEvent(charles.getBorrowerUsername(), EventType.DELETE_FOLDER, new LocalDateTime().minusDays(2), folder_A05));
	//		System.out.println("=====" + getBob_userInAC().getTitle());
	//		transaction.add(createEvent(charles.getBorrowerUsername(), EventType.CREATE_USER, new LocalDateTime().minusDays(2), bob_userInAC,
	//				getBob_userInAC().getTitle()));
	//		transaction.add(createEvent(charles.getBorrowerUsername(), EventType.MODIFY_USER, new LocalDateTime().minusDays(2), chuckNorris,
	//				getChuckNorris().getTitle()));
	//		try {
	//			recordServices.execute(transaction);
	//		} catch (RecordServicesException e) {
	//			throw new RuntimeException(e);
	//		}
	//	}

	private RecordWrapper createEvent(String username, String eventType, LocalDateTime eventDate, String recordId) {
		return createEvent(username, eventType, eventDate, recordId, null);
	}

	private RecordWrapper createEvent(String username, String eventType, LocalDateTime eventDate, String recordId,
									  String title) {
		return schemas.newEvent().setRecordId(recordId).setTitle(title).setUsername(username).setType(eventType)
				.setCreatedOn(eventDate);
	}

	//	private void createBorrowAndReturnEvents() {
	//		Folder folderA02 = getFolder_A02();
	//		Folder folderBorrowedByDakota = getFolder_A03();
	//		User bob = users.bobIn(collection);
	//		loggingServices.logBorrowRecord(folderA02.getWrappedRecord(), bob);
	//		loggingServices.logBorrowRecord(getContainerBac01().getWrappedRecord(), bob);
	//		loggingServices.logReturnRecord(folderA02.getWrappedRecord(), bob);
	//		User charles = users.charlesIn(collection);
	//		loggingServices.logBorrowRecord(folderBorrowedByDakota.getWrappedRecord(), charles);
	//	}

	private void createDecommissioningEvents() {
		DecommissioningList decommissioningList = schemas.newDecommissioningList();
		decommissioningList.setDecommissioningListType(DecommissioningListType.FOLDERS_TO_DEPOSIT);
		decommissioningList.setTitle("folder to deposit by bob");
		User bob = users.bobIn(collection);
		decommissioningLoggingService.logDecommissioning(decommissioningList, bob);

		DecommissioningList decommissioningList2 = schemas.newDecommissioningList();
		decommissioningList2.setDecommissioningListType(DecommissioningListType.FOLDERS_TO_DESTROY);
		decommissioningList2.setTitle("folder destroy by dakota");
		User dakota = users.dakotaLIndienIn(collection);
		decommissioningLoggingService.logDecommissioning(decommissioningList2, dakota);

		DecommissioningList decommissioningList3 = schemas.newDecommissioningList();
		decommissioningList3.setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER);
		decommissioningList3.setTitle("folder transfer by bob");
		decommissioningLoggingService.logDecommissioning(decommissioningList3, bob);
	}


	private void setupLists(Transaction transaction) {
		//		transaction.add(schemas.newDecommissioningListWithId(list_01)).setTitle("Listes avec plusieurs supports à détruire")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_DESTROY)
		//				.setContainerDetailsFor(containerId_bac18, containerId_bac19)
		//				.setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
		//				.setFolderDetailsForIds(asList(folder_A42, folder_A43, folder_A44, folder_A45, folder_A46, folder_A47));
		//
		//		transaction.add(schemas.newDecommissioningListWithId(list_02)).setTitle("Liste analogique à détruire")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_DESTROY)
		//				.setOriginArchivisticStatus(OriginStatus.SEMI_ACTIVE)
		//				.setFolderDetailsForIds(asList(folder_A54, folder_A55, folder_A56));
		//
		//		transaction.add(schemas.newDecommissioningListWithId(list_03)).setTitle("Liste hybride à fermer")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_CLOSE)
		//				.setOriginArchivisticStatus(OriginStatus.ACTIVE)
		//				.setFolderDetailsForIds(asList(folder_A01, folder_A02, folder_A03));
		//
		//		transaction.add(schemas.newDecommissioningListWithId(list_04)).setTitle("Liste analogique à transférer")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_TRANSFER)
		//				.setOriginArchivisticStatus(OriginStatus.ACTIVE)
		//				.setContainerDetailsFor(containerId_bac14, containerId_bac15)
		//				.setFolderDetailsForIds(asList(folder_A22, folder_A23, folder_A24));
		//
		//		transaction.add(schemas.newDecommissioningListWithId(list_05)).setTitle("Liste hybride à transférer")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_TRANSFER)
		//				.setOriginArchivisticStatus(OriginStatus.ACTIVE)
		//				.setFolderDetailsForIds(asList(folder_A19, folder_A20, folder_A21));
		//
		//		transaction.add(schemas.newDecommissioningListWithId(list_06)).setTitle("Liste électronique à transférer")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_TRANSFER)
		//				.setOriginArchivisticStatus(OriginStatus.ACTIVE)
		//				.setFolderDetailsForIds(asList(folder_A25, folder_A26, folder_A27));
		//
		//		transaction.add(schemas.newDecommissioningListWithId(list_07)).setTitle("Liste analogique à détruire")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_DESTROY)
		//				.setFolderDetailsForIds(asList(folder_A54, folder_A55, folder_A56));
		//
		//		transaction.add(schemas.newDecommissioningListWithId(list_08)).setTitle("Liste hybride à déposer")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_DEPOSIT)
		//				.setFolderDetailsForIds(folder_B30, folder_B33, folder_B35);
		//
		//		transaction.add(schemas.newDecommissioningListWithId(list_09)).setTitle("Liste électronique à déposer")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_DEPOSIT)
		//				.setFolderDetailsForIds(asList(folder_A57, folder_A58, folder_A59));
		//
		//		transaction.add(schemas.newDecommissioningListWithId(list_10)).setTitle("Liste avec plusieurs supports à déposer")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_DEPOSIT)
		//				.setFolderDetailsForIds(asList(folder_A42, folder_A43, folder_A44, folder_A48, folder_A49, folder_A50));
		//
		//		transaction.add(schemas.newDecommissioningListWithId(list_11)).setTitle("Liste de fermeture traîtée")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_CLOSE)
		//				.setProcessingUser(dakota_managerInA_userInB).setProcessingDate(date(2012, 5, 5))
		//				.setFolderDetailsForIds(asList(folder_A10, folder_A11, folder_A12, folder_A13, folder_A14, folder_A15));
		//
		//		transaction.add(schemas.newDecommissioningListWithId(list_12)).setTitle("Liste de transfert traîtée")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_TRANSFER)
		//				.setProcessingUser(dakota_managerInA_userInB).setProcessingDate(date(2012, 5, 5))
		//				.setContainerDetailsFor(containerId_bac10, containerId_bac11, containerId_bac12)
		//				.setFolderDetailsForIds(asList(folder_A45, folder_A46, folder_A47, folder_A48, folder_A49, folder_A50, folder_A51,
		//						folder_A52, folder_A53, folder_A54, folder_A55, folder_A56, folder_A57, folder_A58, folder_A59));
		//
		//		transaction.add(schemas.newDecommissioningListWithId(list_13)).setTitle("Liste de transfert uniforme traîtée")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_TRANSFER)
		//				.setProcessingUser(dakota_managerInA_userInB).setProcessingDate(date(2012, 5, 5))
		//				.setContainerDetailsFor(containerId_bac13)
		//				.setFolderDetailsForIds(asList(folder_A42, folder_A43, folder_A43));
		//
		//		transaction.add(schemas.newDecommissioningListWithId(list_14)).setTitle("Liste de dépôt traîtée")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_DEPOSIT)
		//				.setProcessingUser(dakota_managerInA_userInB).setProcessingDate(date(2012, 5, 5))
		//				.setContainerDetailsFor(containerId_bac05)
		//				.setFolderDetailsForIds(folder_A79, folder_A80, folder_A81, folder_A82, folder_A83, folder_A84, folder_A85,
		//						folder_A86, folder_A87, folder_A88, folder_A89, folder_A90, folder_A91, folder_A92, folder_A93);
		//
		//		transaction.add(schemas.newDecommissioningListWithId(list_15)).setTitle("Liste de dépôt uniforme traîtée")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_DEPOSIT)
		//				.setProcessingUser(dakota_managerInA_userInB).setProcessingDate(date(2012, 5, 5))
		//				.setContainerDetailsFor(containerId_bac04)
		//				.setFolderDetailsForIds(asList(folder_A94, folder_A95, folder_A96));
		//
		//		DecommissioningList zeList16 = schemas.newDecommissioningListWithId(list_16)
		//				.setTitle("Liste analogique à transférer en contenants")
		//				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(FOLDERS_TO_TRANSFER)
		//				.setContainerDetailsFor(containerId_bac14).setFolderDetailsForIds(asList(folder_A22, folder_A23, folder_A24));
		//		for (DecomListFolderDetail detail : zeList16.getFolderDetails()) {
		//			detail.setContainerRecordId(containerId_bac14);
		//		}
		//		transaction.add(zeList16);

		/*Document document_1 = newDocumentWithContent("cv-EmiliePoulain.odt").setFolder(folder_A54);
		Document document_2 = newDocumentWithContent("guide-dev.pdf").setFolder(folder_A42);
		transaction.add(document_1);
		transaction.add(document_2);
		transaction.add(schemas.newDecommissioningListWithId(list_17)).setTitle("Liste de documents à détruire")
				.setAdministrativeUnit(unitId_10A).setDecommissioningListType(DOCUMENTS_TO_DESTROY)
				.setDocuments(asList(document_1.getId(), document_2.getId()));*/
	}

	private void setupStorageSpace(Transaction transaction) {
		transaction.add(schemas.newStorageSpaceWithId(storageSpaceId_S01).setCode(storageSpaceId_S01).setTitle("Etagere 1"));
		transaction.add(schemas.newStorageSpaceWithId(storageSpaceId_S01_01).setCode(storageSpaceId_S01_01).setTitle("Tablette 1")
				.setParentStorageSpace(storageSpaceId_S01)).setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE);
		transaction.add(schemas.newStorageSpaceWithId(storageSpaceId_S01_02).setCode(storageSpaceId_S01_02).setTitle("Tablette 2")
				.setParentStorageSpace(storageSpaceId_S01)).setDecommissioningType(DEPOSIT);
		transaction.add(schemas.newStorageSpaceWithId(storageSpaceId_S02).setCode(storageSpaceId_S02).setTitle("Etagere 2"));
		transaction.add(schemas.newStorageSpaceWithId(storageSpaceId_S02_01).setCode(storageSpaceId_S02_01).setTitle("Tablette 1")
				.setParentStorageSpace(storageSpaceId_S02)).setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE);
		transaction.add(schemas.newStorageSpaceWithId(storageSpaceId_S02_02).setCode(storageSpaceId_S02_02).setTitle("Tablette 2")
				.setParentStorageSpace(storageSpaceId_S02)).setDecommissioningType(DEPOSIT);
	}

	private void setupContainerTypes(Transaction transaction) {
		transaction.add(schemas.newContainerRecordTypeWithId(containerTypeId_boite22x22)
				.setTitle("Boite 22X22").setCode("B22x22"));
	}

	private void setupContainers(Transaction transaction) {

		String noStorageSpace = null;

		transaction.add(schemas.newContainerRecordWithId(containerId_bac19)).setTemporaryIdentifier("10_A_12").setFull(false)
				.setAdministrativeUnit(unitId_10A).setDecommissioningType(DESTRUCTION)
				.setType(containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac18)).setTemporaryIdentifier("10_A_11").setFull(false)
				.setAdministrativeUnit(unitId_10A).setDecommissioningType(DESTRUCTION).setType(
				containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac17)).setTemporaryIdentifier("10_A_10").setFull(false)
				.setAdministrativeUnit(unitId_10A).setDecommissioningType(DEPOSIT).setType(
				containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac16)).setTemporaryIdentifier("10_A_09").setFull(false)
				.setAdministrativeUnit(unitId_10A).setDecommissioningType(DEPOSIT).setType(
				containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac15)).setTemporaryIdentifier("10_A_08").setFull(false)
				.setAdministrativeUnit(unitId_10A).setDecommissioningType(
				TRANSFERT_TO_SEMI_ACTIVE).setType(containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac14)).setTemporaryIdentifier("10_A_07").setFull(false)
				.setAdministrativeUnit(unitId_10A).setDecommissioningType(
				TRANSFERT_TO_SEMI_ACTIVE).setType(containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac13).setTemporaryIdentifier("10_A_06").setFull(false)
				.setStorageSpace(storageSpaceId_S01_01).setAdministrativeUnit(unitId_10A)
				.setRealTransferDate(date(2008, 10, 31))).setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(
				containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac12).setTemporaryIdentifier("10_A_05").setFull(false)
				.setStorageSpace(storageSpaceId_S01_01).setAdministrativeUnit(unitId_10A)
				.setRealTransferDate(date(2006, 10, 31))).setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(
				containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac11).setTemporaryIdentifier("10_A_04").setFull(false)
				.setStorageSpace(storageSpaceId_S01_01).setAdministrativeUnit(unitId_10A)
				.setRealTransferDate(date(2005, 10, 31))).setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(
				containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac10).setTemporaryIdentifier("10_A_03").setFull(true)
				.setStorageSpace(noStorageSpace).setAdministrativeUnit(unitId_10A)
				.setRealTransferDate(date(2007, 10, 31))).setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(
				containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac09).setTemporaryIdentifier("11_B_02").setFull(false)
				.setStorageSpace(storageSpaceId_S02_01).setAdministrativeUnit(unitId_10B)
				.setRealTransferDate(date(2006, 10, 31))).setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(
				containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac08).setTemporaryIdentifier("12_B_02").setFull(false)
				.setStorageSpace(storageSpaceId_S02_01).setAdministrativeUnit(unitId_10B)
				.setRealTransferDate(date(2007, 10, 31))).setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(
				containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac07).setTemporaryIdentifier("30_C_03").setFull(false)
				.setStorageSpace(storageSpaceId_S02_01).setAdministrativeUnit(unitId_10C)
				.setRealTransferDate(date(2007, 10, 31))).setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(
				containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac06).setTemporaryIdentifier("30_C_02").setFull(false)
				.setStorageSpace(noStorageSpace).setAdministrativeUnit(unitId_10C)
				.setRealTransferDate(date(2006, 10, 31))).setDecommissioningType(TRANSFERT_TO_SEMI_ACTIVE).setType(
				containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac05).setTemporaryIdentifier("10_A_02").setFull(true)
				.setStorageSpace(storageSpaceId_S01_02).setAdministrativeUnit(unitId_10A)
				.setRealTransferDate(date(2008, 10, 31)).setRealDepositDate(date(2012, 5, 15))).setDecommissioningType(
				DEPOSIT).setType(containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac04).setTemporaryIdentifier("10_A_01").setFull(false)
				.setStorageSpace(storageSpaceId_S01_02).setAdministrativeUnit(unitId_10A)
				.setRealTransferDate(date(2007, 10, 31)).setRealDepositDate(date(2010, 8, 17))).setDecommissioningType(
				DEPOSIT).setType(containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac03).setTemporaryIdentifier("11_B_01").setFull(false)
				.setStorageSpace(storageSpaceId_S02_02).setAdministrativeUnit(unitId_10B)
				.setRealTransferDate(date(2006, 10, 31)).setRealDepositDate(date(2009, 8, 17))).setDecommissioningType(
				DEPOSIT).setType(containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac02).setTemporaryIdentifier("12_B_01").setFull(false)
				.setStorageSpace(noStorageSpace).setAdministrativeUnit(unitId_10B)
				.setRealTransferDate(date(2007, 10, 31)).setRealDepositDate(date(2011, 2, 13))).setDecommissioningType(
				DEPOSIT).setType(containerTypeId_boite22x22);

		transaction.add(schemas.newContainerRecordWithId(containerId_bac01).setTemporaryIdentifier("30_C_01").setFull(true)
				.setStorageSpace(storageSpaceId_S02_02).setAdministrativeUnit(unitId_10C)
				.setRealTransferDate(date(2007, 10, 31)).setRealDepositDate(date(2011, 2, 13))).setDecommissioningType(
				DEPOSIT).setType(containerTypeId_boite22x22);
	}

	private void setupFolders(Transaction transaction) {
		transaction.add(schemas.newFolderWithId(folder_A01)).setTitle("Émilie Poulain").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant Mme Émilie Poulain")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4));

		transaction.add(schemas.newFolderWithId(folder_A02)).setTitle("Recrutement à l'externe - Tech. en Documentation")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_2110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Documents relatifs aux recrutements externes")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4));

		transaction.add(schemas.newFolderWithId(folder_A03)).setTitle("Recrutement à l'interne - Tech. en Documentation")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_2110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Documents relatifs aux promotions en interne")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5));

		transaction.add(schemas.newFolderWithId(folder_A04)).setTitle("Structure des Postes")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_1100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Schéma complet de la structure des postes, avec leur caractéristiques")
				.setOpenDate(date(2000, 10, 4));

		transaction.add(schemas.newFolderWithId(folder_A05)).setTitle("Bastien Augerau").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le client M. Bastien Augerau")
				.setOpenDate(date(2000, 11, 4));

		transaction.add(schemas.newFolderWithId(folder_A06)).setTitle("Bastien Bernadotte")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le client M. Bastien Bernadotte")
				.setOpenDate(date(2000, 11, 5));

		transaction.add(schemas.newFolderWithId(folder_A07)).setTitle("Brittany Daru").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le client Mme Brittany Daru")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4));

		transaction.add(schemas.newFolderWithId(folder_A08)).setTitle("Boris Gouvon").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le client M. Boris Gouvon")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4));

		transaction.add(schemas.newFolderWithId(folder_A09)).setTitle("Burt Marmont").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le client M. Burt Marmont")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5));

		transaction.add(schemas.newFolderWithId(folder_A10)).setTitle("Caroline Suchet").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le client Mme Caroline Suchet")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A11)).setTitle("DocuLibre").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_22).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le fournisseur DocuLibre")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A12)).setTitle("IBM").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_22).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le fournisseur IBM")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A13)).setTitle("Google").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_22).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le fournisseur Google")
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A14)).setTitle("Toyota 102-CQA").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_231).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant la voiture de fonction immatriculée 102-CQA")
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A15)).setTitle("Audi 734-FPL").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_231).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant la voiture immatriculée 734-FPL")
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A16)).setTitle("Assurance Santé Émilie Poulain")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_232).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant l'assurance santé de Mme Émilie Poulain")
				.setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A17)).setTitle("Documents PDF présents sur le site")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_13).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Sauvegarde de l'ensemble des fichiers présents sur le site côté client au format pdf")
				.setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A18)).setTitle("Formation à l'internationale")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_3200).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Formations possibles à l'international")
				.setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A19)).setTitle("Etude des sites concurrents")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_13).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA)
				.setDescription("Étude des sites concurrents")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A20)).setTitle("Propositions Commerciales")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_22).setRetentionRuleEntered(ruleId_3)
				.setDescription("Document vierge de devis")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A21)).setTitle("Accident du 20/02 - Émilie Poulain")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_233).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Accident du travail de Mme. Émilie Poulain en date du 20 février 2002")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A22)).setTitle("Contrat Auto Toyota 102-CQA")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_234).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Contrat avec la Banque Nationale du Québec")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4)).setCloseDateEntered(date(2002, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A23)).setTitle("Contrat Auto Audi 734-FPL")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_234).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Contrat avec la Banque Nationale du Québec")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 4)).setCloseDateEntered(date(2002, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A24)).setTitle("Contrat 1250 Charest Ouest, Suite 1040")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_234).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Contrat d'assurance des locaux avec AllianZ")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 5)).setCloseDateEntered(date(2003, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A25)).setTitle("Accident du 07/02 - Rousseau Amélie")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_233).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Accident du travail de Mme. Rousseau Amélie en date du 7 février 2002")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4)).setCloseDateEntered(date(2002, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A26)).setTitle("CGI").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_22).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Dossier concernant le fournisseur CGI")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 4)).setCloseDateEntered(date(2002, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A27)).setTitle("James Baxter").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Dossier concernant le client M. James Baxter")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 5)).setCloseDateEntered(date(2003, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_A42)).setTitle("Lisa Tyson").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA)
				.setDescription("Dossier concernant le client Mme Lisa Tyson")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2007, 10, 31)).setContainer(containerId_bac13);

		transaction.add(schemas.newFolderWithId(folder_A43)).setTitle("Gertrude Young").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le client Mme Gertrude Young")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2007, 10, 31)).setContainer(containerId_bac13);

		transaction.add(schemas.newFolderWithId(folder_A44)).setTitle("Scott Morris").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_2)
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31))
				.setDescription("Dossier concernant le client M. Scott Morris")
				.setActualTransferDate(date(2008, 10, 31)).setContainer(containerId_bac13);

		transaction.add(schemas.newFolderWithId(folder_A45)).setTitle("Linda Armstrong").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(MD)
				.setDescription("Dossier concernant le client Mme Linda Armstrong")
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2005, 10, 31)).setContainer(containerId_bac12);

		transaction.add(schemas.newFolderWithId(folder_A46)).setTitle("Jeffrey West").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA)
				.setDescription("Dossier concernant le client M. Jeffrey West")
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2005, 10, 31)).setContainer(containerId_bac12).setDescription("Babar");

		transaction.add(schemas.newFolderWithId(folder_A47)).setTitle("Letha Johnson").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_2)
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31))
				.setDescription("Dossier concernant le client Mme Letha Johnson")
				.setActualTransferDate(date(2006, 10, 31)).setContainer(containerId_bac12);

		transaction.add(schemas.newFolderWithId(folder_A48)).setTitle("Betty Howell").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(MD)
				.setDescription("Dossier concernant le client Mme Betty Howell")
				.setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setContainer(containerId_bac11);

		transaction.add(schemas.newFolderWithId(folder_A49)).setTitle("Caroline Lacroix").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setContainer(containerId_bac11)
				.setDescription("Dossier concernant le client Mme Caroline Lacroix");

		transaction.add(schemas.newFolderWithId(folder_A50)).setTitle("Edward Unrein").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le client M. Edward Unrein")
				.setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2005, 10, 31)).setContainer(containerId_bac11);

		transaction.add(schemas.newFolderWithId(folder_A51)).setTitle("Accident du 30/05 - Rousseau Amélie")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_233).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Accident du travail de Mme Rousseau Amélie en date du 30 Mai 2000")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setContainer(containerId_bac10);

		transaction.add(schemas.newFolderWithId(folder_A52)).setTitle("Toyota 480-SHI").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_231).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant la voiture de fonction immatriculée 480-SHI")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setContainer(containerId_bac10);

		transaction.add(schemas.newFolderWithId(folder_A53)).setTitle("Honda 462-OBR").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_231).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant la voiture de fonction immatriculée 462-OBR")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2005, 10, 31)).setContainer(containerId_bac10);

		transaction.add(schemas.newFolderWithId(folder_A54)).setTitle("Betty Hayes").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Documents officiels de Mme Betty Hayes")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setContainer(containerId_bac10);

		transaction.add(schemas.newFolderWithId(folder_A55)).setTitle("Filibert Valdez").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Documents officiels de M. Filibert Valdez")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setContainer(containerId_bac10);

		transaction.add(schemas.newFolderWithId(folder_A56)).setTitle("Anne Fernandez").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Documents officiels de Mme Anne Fernandez")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 5)).setCloseDateEntered(date(2003, 10, 31))
				.setActualTransferDate(date(2007, 10, 31)).setContainer(containerId_bac10);

		transaction.add(schemas.newFolderWithId(folder_A57)).setTitle("David Yates").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Documents officiels de M. David Yates")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setContainer(containerId_bac10);

		transaction.add(schemas.newFolderWithId(folder_A58)).setTitle("Henry Ford").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Documents officiels de M. Henry Ford")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setContainer(containerId_bac10);

		transaction.add(schemas.newFolderWithId(folder_A59)).setTitle("Roy Mathieu").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Documents officiels de M. Roy Mathieu")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 5)).setCloseDateEntered(date(2003, 10, 31))
				.setActualTransferDate(date(2007, 10, 31)).setContainer(containerId_bac10);

		transaction.add(schemas.newFolderWithId(folder_A79)).setTitle("Alexandra Zielinski")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Documents officiels de Mme Alexandra Zielinski")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2007, 10, 31)).setActualDepositDate(date(2011, 2, 13))
				.setContainer(containerId_bac05);

		transaction.add(schemas.newFolderWithId(folder_A80)).setTitle("Andrea Chavez").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Documents officiels de Mme Andrea Chavez")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2007, 10, 31)).setActualDestructionDate(date(2011, 2, 13));

		transaction.add(schemas.newFolderWithId(folder_A81)).setTitle("Cynthia Adams").setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Documents officiels de Mme Cynthia Adams")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2008, 10, 31)).setActualDepositDate(date(2012, 2, 13))
				.setContainer(containerId_bac05);

		transaction.add(schemas.newFolderWithId(folder_A82)).setTitle("Assurance Santé Filibert Valdez")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_232).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant l'assurance santé de M. Filibert Valdez")
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2005, 10, 31)).setActualDestructionDate(date(2007, 4, 14));

		transaction.add(schemas.newFolderWithId(folder_A83)).setTitle("Assurance Santé Anne Fernandez")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_232).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant l'assurance santé de Mme Anne Fernandez")
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2005, 10, 31)).setActualDestructionDate(date(2007, 4, 14));

		transaction.add(schemas.newFolderWithId(folder_A84)).setTitle("Assurance Santé David Yates")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_232).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant l'assurance santé de M. David Yates")
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setActualDestructionDate(date(2008, 4, 14));

		transaction.add(schemas.newFolderWithId(folder_A85)).setTitle("Assurance Santé Henry Ford")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_232).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant l'assurance santé de M. Henry Ford")
				.setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setActualDepositDate(date(2011, 5, 15))
				.setContainer(containerId_bac05);

		transaction.add(schemas.newFolderWithId(folder_A86)).setTitle("Assurance Santé Roy Mathieu")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_232).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant l'assurance santé de M. Roy Mathieu")
				.setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setActualDepositDate(date(2011, 5, 15))
				.setContainer(containerId_bac05);

		transaction.add(schemas.newFolderWithId(folder_A87)).setTitle("Assurance Santé Alexandra Zielinski")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_232).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant l'assurance santé de Mme Alexandra Zielinski")
				.setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2005, 10, 31)).setActualDepositDate(date(2012, 5, 15))
				.setContainer(containerId_bac05);

		transaction.add(schemas.newFolderWithId(folder_A88)).setTitle("Assurance Santé Andrea Chavez")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_232).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant l'assurance santé de Mme Andrea Chavez")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setActualDestructionDate(date(2011, 6, 16));

		transaction.add(schemas.newFolderWithId(folder_A89)).setTitle("Assurance Santé Cynthia Adams")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_232).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant l'assurance santé de Mme Cynthia Adams")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setActualDepositDate(date(2011, 6, 16))
				.setContainer(containerId_bac05);

		transaction.add(schemas.newFolderWithId(folder_A90)).setTitle("Assurance Santé Betty Hayes")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_232).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant l'assurance santé de Mme Betty Hayes")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 11, 5)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2005, 10, 31)).setActualDestructionDate(date(2012, 6, 16));

		transaction.add(schemas.newFolderWithId(folder_A91)).setTitle("Assurance Santé Olivier Dufault")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_232).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Dossier concernant l'assurance santé de M. Olivier Dufault")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setActualDestructionDate(date(2009, 7, 16));

		transaction.add(schemas.newFolderWithId(folder_A92)).setTitle("Documentation Comptable")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_1100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Documents d'information sur les pratiques du comptable")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setActualDestructionDate(date(2009, 7, 16));

		transaction.add(schemas.newFolderWithId(folder_A93)).setTitle("Documentation Directeur des Ressources Humaines")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_1100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Documents d'information sur les pratiques du DRH")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 5)).setCloseDateEntered(date(2003, 10, 31))
				.setActualTransferDate(date(2007, 10, 31)).setActualDestructionDate(date(2010, 7, 16));

		transaction.add(schemas.newFolderWithId(folder_A94)).setTitle("Documentation Secrétaire")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_1100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Documents d'information sur les pratiques du secrétaire")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setActualDepositDate(date(2009, 8, 17))
				.setContainer(containerId_bac04);

		transaction.add(schemas.newFolderWithId(folder_A95)).setTitle("Documentation Commercial-Marketing")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_1100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Documents d'information sur les pratiques des commerciaux")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setActualDepositDate(date(2009, 8, 17))
				.setContainer(containerId_bac04);

		transaction.add(schemas.newFolderWithId(folder_A96)).setTitle("Documentation Développeur")
				.setAdministrativeUnitEntered(unitId_10A)
				.setCategoryEntered(categoryId_1100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Documents d'information sur les pratiques du développeur")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 7, 5)).setCloseDateEntered(date(2003, 10, 31))
				.setActualTransferDate(date(2007, 10, 31)).setActualDepositDate(date(2010, 8, 17))
				.setContainer(containerId_bac04);

		transaction.add(schemas.newFolderWithId(folder_B01)).setTitle("Affichage des Postes")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_2130).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Hiérarchie des Postes internes et externes")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4));

		transaction.add(schemas.newFolderWithId(folder_B02)).setTitle("Programmes d'embauche")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_2100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Détails sur le programme d'embauche actuel")
				.setOpenDate(date(2000, 10, 4));

		transaction.add(schemas.newFolderWithId(folder_B03)).setTitle("Recrutement à l'externe - Aides")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_2120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Documents d'aide au recrutement externe")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4));

		transaction.add(schemas.newFolderWithId(folder_B04)).setTitle("Recrutement à l'interne - Aides")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_1000).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Documents d'aide au recrutement interne")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_B05)).setTitle("Recrutement à l'externe - Résultats")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_1000).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Convocations et Résultats d'entrevue des candidats à l'externe")
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_B06)).setTitle("Recrutement à l'interne - Résultats")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_2110).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Convocations et Résultats d'entrevue des candidats à l'interne")
				.setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_B07)).setTitle("Planning du Personnel")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_2000).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Planning prévisionnel de travail du Personnel")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_B08)).setTitle("Planning des Congés")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_2000).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Planning prévisionnel des congés")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4)).setCloseDateEntered(date(2002, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_B09)).setTitle("Fiche de Paie").setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_2000).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Documents vierge servant de modèle pour la paie")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4)).setCloseDateEntered(date(2002, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_B30)).setTitle("Formulaire de demande de promotion")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_2110).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Formulaire vierge de demande d'une promotion en interne")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2007, 10, 31)).setContainer(containerId_bac08);

		transaction.add(schemas.newFolderWithId(folder_B31)).setTitle("Formulaire de demande de mutation")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_4200).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Formulaire vierge de demande d'une mutation en interne")
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2005, 10, 31)).setContainer(containerId_bac09);

		transaction.add(schemas.newFolderWithId(folder_B32)).setTitle("Demandes Spontanées")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_2120).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Divers CV de demandes spontanées")
				.setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setContainer(containerId_bac08);

		transaction.add(schemas.newFolderWithId(folder_B33)).setTitle("Accueil de Stagiaires")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_1100).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Documents d'information sur l'accueil des stagiaires")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setContainer(containerId_bac09);

		transaction.add(schemas.newFolderWithId(folder_B34)).setTitle("Schémas et Maquettes")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_13).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Schémas et Maquettes du prochain site")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setContainer(containerId_bac08);

		transaction.add(schemas.newFolderWithId(folder_B35)).setTitle("Factures").setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_22).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Document vierge de Facture")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setContainer(containerId_bac09);

		transaction.add(schemas.newFolderWithId(folder_B50)).setTitle("Template").setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_13).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Documents concernant la charte graphique du site")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2007, 10, 31)).setActualDepositDate(date(2011, 2, 13))
				.setContainer(containerId_bac02);

		transaction.add(schemas.newFolderWithId(folder_B51)).setTitle("Grille de Tarifs").setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_20).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Grilles des tarifs actuels")
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2005, 10, 31)).setActualDestructionDate(date(2007, 4, 14));

		transaction.add(schemas.newFolderWithId(folder_B52)).setTitle("Base de Données").setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_13).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Sauvegarde de la base de données du site")
				.setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setActualDestructionDate(date(2006, 5, 15));

		transaction.add(schemas.newFolderWithId(folder_B53)).setTitle("Catalogue des Produits")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_20).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Catalogue de tous les produits actuels")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setActualDestructionDate(date(2011, 6, 16));

		transaction.add(schemas.newFolderWithId(folder_B54)).setTitle("Document de présentation de l'entreprise")
				.setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_20).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Présentation de l'entreprise à destination des clients et fournisseurs")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setActualDestructionDate(date(2009, 7, 16));

		transaction.add(schemas.newFolderWithId(folder_B55)).setTitle("Plan").setAdministrativeUnitEntered(unitId_10B)
				.setCategoryEntered(categoryId_20).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Plan google map pour accéder aux locaux 1250 Charest Ouest, Suite 1040")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setActualDepositDate(date(2009, 8, 17))
				.setContainer(containerId_bac03);

		transaction.add(schemas.newFolderWithId(folder_C01)).setTitle("Formation du Personnel")
				.setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_3200).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Calendriers, Formulaires d'inscription et Programmes de Formations du Personnel")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4));

		transaction.add(schemas.newFolderWithId(folder_C02)).setTitle("Rousseau Amélie").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Documents officiels de Mme Rousseau Amélie")
				.setOpenDate(date(2000, 10, 4));

		transaction.add(schemas.newFolderWithId(folder_C03)).setTitle("Accident du 10/05 - Filibert Valdez")
				.setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_233).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Accident du travail de M. Filibert Valdez en date du 10 Mai 1999")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4));

		transaction.add(schemas.newFolderWithId(folder_C04)).setTitle("Logo").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_20).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Logo de l'entreprise")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_C05)).setTitle("Sondage").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_4200).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Document vierge de sondage du bien-être des salariés")
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_C06)).setTitle("Charles Mozek").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_21).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le client M. Charles Mozek")
				.setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_C07)).setTitle("Contrat Banque Nationale de Québec")
				.setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_234).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Contrat entre l'entreprise et la Banque Nationale du Québec")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_C08)).setTitle("Documentation Assistance - Dépanage")
				.setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_1100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Documents d'information sur les pratiques de dépanage et d'assistance")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4)).setCloseDateEntered(date(2002, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_C09)).setTitle("Statistiques").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_20).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Statistiques divers sur les ventes, les tarifs, etc. de l'année passée")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4)).setCloseDateEntered(date(2002, 10, 31));

		transaction.add(schemas.newFolderWithId(folder_C30)).setTitle("Mentions Légales").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_20).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Documents d'information sur les mentions légales de l'entreprise")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2007, 10, 31)).setContainer(containerId_bac07);

		transaction.add(schemas.newFolderWithId(folder_C31)).setTitle("Note de Frais").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_2000).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Document vierge de note de frais")
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2005, 10, 31)).setContainer(containerId_bac07);

		transaction.add(schemas.newFolderWithId(folder_C32)).setTitle("Demande de matériel")
				.setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_2000).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Formulaire vierge de demande de matériel et fournitures")
				.setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setContainer(containerId_bac07);

		transaction.add(schemas.newFolderWithId(folder_C33)).setTitle("CV").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_2120).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Sauvegarde de CV")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setContainer(containerId_bac07);

		transaction.add(schemas.newFolderWithId(folder_C34)).setTitle("Scott Trucker").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Dossier concernant le client M. Scott Trucker")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setContainer(containerId_bac07);

		transaction.add(schemas.newFolderWithId(folder_C35)).setTitle("James Dawkins").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Dossier concernant le client M. James Dawkins")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setContainer(containerId_bac06);

		transaction.add(schemas.newFolderWithId(folder_C50)).setTitle("June Nocera").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le client Mme June Nocera")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2007, 10, 31)).setActualDepositDate(date(2011, 2, 13))
				.setContainer(containerId_bac01);

		transaction.add(schemas.newFolderWithId(folder_C51)).setTitle("Michèle Gallucci").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le client M. Michèle Gallucci")
				.setCopyStatusEntered(SECONDARY).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2005, 10, 31)).setActualDestructionDate(date(2007, 4, 14));

		transaction.add(schemas.newFolderWithId(folder_C52)).setTitle("Robert Garcia").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_1)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le client M. Robert Garcia")
				.setDescription("Patate").setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setActualDestructionDate(date(2006, 5, 15));

		transaction.add(schemas.newFolderWithId(folder_C53)).setTitle("Lee Taub").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_3)
				.setMediumTypes(PA, MD)
				.setDescription("Dossier concernant le client M. Lee Taub")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4)).setCloseDateEntered(date(2001, 10, 31))
				.setActualTransferDate(date(2004, 10, 31)).setActualDestructionDate(date(2011, 6, 16));

		transaction.add(schemas.newFolderWithId(folder_C54)).setTitle("Erwin Eckert").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(PA)
				.setDescription("Dossier concernant le client M. Erwin Eckert")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 5, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setActualDestructionDate(date(2009, 7, 16));

		transaction.add(schemas.newFolderWithId(folder_C55)).setTitle("Daniel Nelson").setAdministrativeUnitEntered(unitId_10C)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_4)
				.setMediumTypes(MD)
				.setDescription("Dossier concernant le client M. Daniel Nelson")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 6, 4)).setCloseDateEntered(date(2002, 10, 31))
				.setActualTransferDate(date(2006, 10, 31)).setActualDepositDate(date(2009, 8, 17))
				.setContainer(containerId_bac01);

		transaction.add(schemas.newFolderWithId(nextId())).setTitle("Évaluation des Ressources Humaines")
				.setAdministrativeUnitEntered(unitId_10D)
				.setCategoryEntered(categoryId_4100).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Document permettant l'évaluation du Personnel")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4));

		transaction.add(schemas.newFolderWithId(nextId())).setTitle("Mouvement du Personnel")
				.setAdministrativeUnitEntered(unitId_10D)
				.setCategoryEntered(categoryId_4200).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Documents relatifs aux affectations du Personnel")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4));

		transaction.add(schemas.newFolderWithId(nextId())).setTitle("Affichage des postes de tech. en documentation")
				.setAdministrativeUnitEntered(unitId_10E)
				.setCategoryEntered(categoryId_2130).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Documentation technique des postes")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4));

		transaction.add(schemas.newFolderWithId(nextId())).setTitle("Guides d'évaluations")
				.setAdministrativeUnitEntered(unitId_10E)
				.setCategoryEntered(categoryId_4100).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Anciennes versions des Guides d'évaluations")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4));

		transaction.add(schemas.newFolderWithId(nextId())).setTitle("Oliver Dufault").setAdministrativeUnitEntered(unitId_10E)
				.setCategoryEntered(categoryId_3100).setRetentionRuleEntered(ruleId_2)
				.setMediumTypes(PA, MD)
				.setDescription("Documents officiels de M. Dufault Olivier")
				.setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4));

	}

	private ContentVersionDataSummary upload(String resourceName) {
		InputStream inputStream = DemoTestRecords.class.getResourceAsStream("DemoTestRecords_" + resourceName);
		return contentManager.upload(inputStream);
	}

	private LocalDate date(int year, int month, int day) {
		return new LocalDate(year, month, day);
	}

	public String getCollection() {
		return collection;
	}

	public Users getUsers() {
		return users;
	}

}
