package com.constellio.model.services.taxonomies;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.utils.Provider;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AuthorizationDeleteRequest;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;

import java.io.File;
import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForGroups;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorizationDeleteRequest;
import static com.constellio.model.entities.security.global.AuthorizationModificationRequest.modifyAuthorizationOnRecord;

public class TaxonomiesTestsUtils {


	public static void createFoldersAndDocumentsWithNegativeAuths(final AdministrativeUnit administrativeUnit,
																  final Category category)
			throws Exception {

		final AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();

		Provider<String, Folder> folderBuilder = new Provider<String, Folder>() {
			@Override
			public Folder get(String id) {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(administrativeUnit.getCollection(), appLayerFactory);
				return rm.newFolderWithId(id).setTitle("Folder " + id).setCategoryEntered(category).setOpenDate(new LocalDate())
						.setAdministrativeUnitEntered(administrativeUnit).setRetentionRuleEntered(rm.getRetentionRule("ruleId_1"));
			}
		};

		Transaction tx = new Transaction();
		Folder folder1 = tx.add(folderBuilder.get("f1"));
		Folder folder2 = tx.add(folderBuilder.get("f2"));
		Folder folder3 = tx.add(folderBuilder.get("f3"));
		Folder folder4 = tx.add(folderBuilder.get("f4"));
		Folder folder5 = tx.add(folderBuilder.get("f5"));
		Folder folder6 = tx.add(folderBuilder.get("f6"));
		Folder folder7 = tx.add(folderBuilder.get("f7"));
		Folder folder8 = tx.add(folderBuilder.get("f8"));
		Folder folder9 = tx.add(folderBuilder.get("f9"));
		Folder folder10 = tx.add(folderBuilder.get("f10"));

		Document document1_1 = tx.add(createDocument(folder1, "d11"));
		Document document2_1 = tx.add(createDocument(folder2, "d21"));
		Document document3_1 = tx.add(createDocument(folder3, "d31"));
		Document document3_2 = tx.add(createDocument(folder3, "d32"));
		Document document4_1 = tx.add(createDocument(folder4, "d41"));
		Document document5_1 = tx.add(createDocument(folder5, "d51"));
		Document document6_1 = tx.add(createDocument(folder6, "d61"));
		Document document6_2 = tx.add(createDocument(folder6, "d62"));
		Document document7_1 = tx.add(createDocument(folder7, "d71"));
		Document document7_2 = tx.add(createDocument(folder7, "d72"));
		Document document8_1 = tx.add(createDocument(folder8, "d81"));
		Document document9_1 = tx.add(createDocument(folder9, "d91"));
		Document document10_1 = tx.add(createDocument(folder10, "d101"));

		appLayerFactory.getModelLayerFactory().newRecordServices().execute(tx);

		UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		User alice = userServices.getUserInCollection("alice", "zeCollection");
		User bob = userServices.getUserInCollection("bob", "zeCollection");
		User charles = userServices.getUserInCollection("charles", "zeCollection");
		User gandalf = userServices.getUserInCollection("gandalf", "zeCollection");
		User chuck = userServices.getUserInCollection("chuck", "zeCollection");
		Group legends = userServices.getGroupInCollection("legends", "zeCollection");
		Group heroes = userServices.getGroupInCollection("heroes", "zeCollection");


		AuthorizationsServices authServices = appLayerFactory.getModelLayerFactory().newAuthorizationsServices();
		String auth1 = authServices.add(authorizationForUsers(alice, bob, charles, gandalf).givingReadWriteDeleteAccess().on(administrativeUnit));

		String folder1Auth1 = authServices.add(authorizationForGroups(heroes).givingNegativeReadAccess().on(folder1));
		authServices.execute(modifyAuthorizationOnRecord(folder1Auth1, document1_1).removingItOnRecord());

		String folder2Auth1 = authServices.add(authorizationForGroups(heroes).givingNegativeReadAccess().on(folder2));

		String folder3Auth1 = authServices.add(authorizationForGroups(heroes).givingNegativeReadAccess().on(folder3));
		String copyOfFolder3Auth1OnDocument3_1 = authServices.detach(document3_1.getWrappedRecord()).get(folder3Auth1);
		String folder3Auth2 = authServices.add(authorizationForUsers(chuck).givingNegativeReadAccess().on(document3_1));
		authServices.execute(AuthorizationDeleteRequest.authorizationDeleteRequest(copyOfFolder3Auth1OnDocument3_1, "zeCollection"));

		String folder4Auth1 = authServices.add(authorizationForGroups(heroes).givingNegativeReadAccess().on(folder4));
		String copyOfFolder4Auth1OnFolder4_1 = authServices.detach(document4_1.getWrappedRecord()).get(folder4Auth1);
		String folder4Auth2 = authServices.add(authorizationForGroups(legends).givingNegativeReadAccess().on(document4_1));
		authServices.execute(AuthorizationDeleteRequest.authorizationDeleteRequest(copyOfFolder4Auth1OnFolder4_1, "zeCollection"));

		String folder5Auth1 = authServices.add(authorizationForGroups(heroes).givingNegativeReadAccess().on(folder5));
		String folder5_1Auth1 = authServices.add(authorizationForGroups(heroes).givingReadAccess().on(document5_1));
		authServices.execute(modifyAuthorizationOnRecord(folder5Auth1, document5_1).removingItOnRecord());

		String folder6_1Auth1 = authServices.add(authorizationForGroups(legends).givingNegativeReadAccess().on(document6_1));
		String folder6_2Auth1 = authServices.add(authorizationForGroups(legends).givingReadAccess().on(document6_2));

		String folder7Auth1 = authServices.add(authorizationForGroups(legends).givingNegativeReadAccess().on(folder7));
		authServices.execute(modifyAuthorizationOnRecord(folder7Auth1, document7_2).removingItOnRecord());


		String folder8Auth1 = authServices.add(authorizationForGroups(legends).givingNegativeReadAccess().on(folder8));
		String folder8Auth2 = authServices.add(authorizationForGroups(heroes).givingNegativeReadAccess().on(document8_1));
		authServices.execute(modifyAuthorizationOnRecord(folder8Auth1, document8_1).removingItOnRecord());


		String folder9Auth1 = authServices.add(authorizationForGroups(legends, heroes).givingNegativeReadAccess().on(folder9));
		String folder9_1Auth1 = authServices.add(authorizationForGroups(legends, heroes).givingNegativeReadAccess().on(document9_1));
		authServices.execute(modifyAuthorizationOnRecord(folder9Auth1, document9_1).removingItOnRecord());


		String folder10Auth1 = authServices.add(authorizationForGroups(legends, heroes).givingNegativeReadAccess().on(folder10));
		String copyOfFolder10Auth1 = authServices.detach(document10_1.getWrappedRecord()).get(folder10Auth1);
		String folder10_1Auth1 = authServices.add(authorizationForGroups(legends, heroes).givingNegativeReadAccess().on(document10_1));
		authServices.execute(authorizationDeleteRequest(copyOfFolder10Auth1, "zeCollection"));
	}


	public static void createFoldersAndSubFoldersWithNegativeAuths(final AdministrativeUnit administrativeUnit,
																   final Category category)
			throws Exception {

		final AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();

		Provider<String, Folder> folderBuilder = new Provider<String, Folder>() {
			@Override
			public Folder get(String id) {
				RMSchemasRecordsServices rm = new RMSchemasRecordsServices(administrativeUnit.getCollection(), appLayerFactory);
				return rm.newFolderWithId(id).setTitle("Folder " + id).setCategoryEntered(category).setOpenDate(new LocalDate())
						.setAdministrativeUnitEntered(administrativeUnit).setRetentionRuleEntered(rm.getRetentionRule("ruleId_1"));
			}
		};

		Transaction tx = new Transaction();
		Folder folder1 = tx.add(folderBuilder.get("f1"));
		Folder folder2 = tx.add(folderBuilder.get("f2"));
		Folder folder3 = tx.add(folderBuilder.get("f3"));
		Folder folder4 = tx.add(folderBuilder.get("f4"));
		Folder folder5 = tx.add(folderBuilder.get("f5"));
		Folder folder6 = tx.add(folderBuilder.get("f6"));
		Folder folder7 = tx.add(folderBuilder.get("f7"));
		Folder folder8 = tx.add(folderBuilder.get("f8"));
		Folder folder9 = tx.add(folderBuilder.get("f9"));
		Folder folder10 = tx.add(folderBuilder.get("f10"));

		Folder folder1_1 = tx.add(createSubFolder(folder1, "f11"));
		Folder folder2_1 = tx.add(createSubFolder(folder2, "f21"));
		Folder folder3_1 = tx.add(createSubFolder(folder3, "f31"));
		Folder folder3_2 = tx.add(createSubFolder(folder3, "f32"));
		Folder folder4_1 = tx.add(createSubFolder(folder4, "f41"));
		Folder folder5_1 = tx.add(createSubFolder(folder5, "f51"));
		Folder folder6_1 = tx.add(createSubFolder(folder6, "f61"));
		Folder folder6_2 = tx.add(createSubFolder(folder6, "f62"));
		Folder folder7_1 = tx.add(createSubFolder(folder7, "f71"));
		Folder folder7_2 = tx.add(createSubFolder(folder7, "f72"));
		Folder folder8_1 = tx.add(createSubFolder(folder8, "f81"));
		Folder folder9_1 = tx.add(createSubFolder(folder9, "f91"));
		Folder folder10_1 = tx.add(createSubFolder(folder10, "f101"));

		appLayerFactory.getModelLayerFactory().newRecordServices().execute(tx);

		UserServices userServices = appLayerFactory.getModelLayerFactory().newUserServices();
		User alice = userServices.getUserInCollection("alice", "zeCollection");
		User bob = userServices.getUserInCollection("bob", "zeCollection");
		User charles = userServices.getUserInCollection("charles", "zeCollection");
		User gandalf = userServices.getUserInCollection("gandalf", "zeCollection");
		User chuck = userServices.getUserInCollection("chuck", "zeCollection");
		Group legends = userServices.getGroupInCollection("legends", "zeCollection");
		Group heroes = userServices.getGroupInCollection("heroes", "zeCollection");


		AuthorizationsServices authServices = appLayerFactory.getModelLayerFactory().newAuthorizationsServices();
		String auth1 = authServices.add(authorizationForUsers(alice, bob, charles, gandalf).givingReadWriteDeleteAccess().on(administrativeUnit));

		String folder1Auth1 = authServices.add(authorizationForGroups(heroes).givingNegativeReadAccess().on(folder1));
		authServices.execute(modifyAuthorizationOnRecord(folder1Auth1, folder1_1).removingItOnRecord());

		String folder2Auth1 = authServices.add(authorizationForGroups(heroes).givingNegativeReadAccess().on(folder2));

		String folder3Auth1 = authServices.add(authorizationForGroups(heroes).givingNegativeReadAccess().on(folder3));
		String copyOfFolder3Auth1OnFolder3_1 = authServices.detach(folder3_1.getWrappedRecord()).get(folder3Auth1);
		String folder3Auth2 = authServices.add(authorizationForUsers(chuck).givingNegativeReadAccess().on(folder3_1));
		authServices.execute(authorizationDeleteRequest(copyOfFolder3Auth1OnFolder3_1, "zeCollection"));
		//authServices.execute(modifyAuthorizationOnRecord(folder3Auth1, folder3_1).removingItOnRecord());

		String folder4Auth1 = authServices.add(authorizationForGroups(heroes).givingNegativeReadAccess().on(folder4));
		String copyOfFolder4Auth1OnFolder4_1 = authServices.detach(folder4_1.getWrappedRecord()).get(folder4Auth1);
		String folder4Auth2 = authServices.add(authorizationForGroups(legends).givingNegativeReadAccess().on(folder4_1));
		authServices.execute(authorizationDeleteRequest(copyOfFolder4Auth1OnFolder4_1, "zeCollection"));
		//authServices.execute(modifyAuthorizationOnRecord(folder4Auth1, folder4_1).removingItOnRecord());

		String folder5Auth1 = authServices.add(authorizationForGroups(heroes).givingNegativeReadAccess().on(folder5));
		String folder5_1Auth1 = authServices.add(authorizationForGroups(heroes).givingReadAccess().on(folder5_1));
		authServices.execute(modifyAuthorizationOnRecord(folder5Auth1, folder5_1).removingItOnRecord());

		String folder6_1Auth1 = authServices.add(authorizationForGroups(legends).givingNegativeReadAccess().on(folder6_1));
		String folder6_2Auth1 = authServices.add(authorizationForGroups(legends).givingReadAccess().on(folder6_2));

		String folder7Auth1 = authServices.add(authorizationForGroups(legends).givingNegativeReadAccess().on(folder7));
		authServices.execute(modifyAuthorizationOnRecord(folder7Auth1, folder7_2).removingItOnRecord());

		String folder8Auth1 = authServices.add(authorizationForGroups(legends).givingNegativeReadAccess().on(folder8));
		String folder8Auth2 = authServices.add(authorizationForGroups(heroes).givingNegativeReadAccess().on(folder8_1));
		authServices.execute(modifyAuthorizationOnRecord(folder8Auth1, folder8_1).removingItOnRecord());

		String folder9Auth1 = authServices.add(authorizationForGroups(legends, heroes).givingNegativeReadAccess().on(folder9));
		String folder9_1Auth1 = authServices.add(authorizationForGroups(legends, heroes).givingNegativeReadAccess().on(folder9_1));
		authServices.execute(modifyAuthorizationOnRecord(folder9Auth1, folder9_1).removingItOnRecord());

		String folder10Auth1 = authServices.add(authorizationForGroups(legends, heroes).givingNegativeReadAccess().on(folder10));
		String copyOfFolder10Auth1 = authServices.detach(folder10_1.getWrappedRecord()).get(folder10Auth1);
		String folder10_1Auth1 = authServices.add(authorizationForGroups(legends, heroes).givingNegativeReadAccess().on(folder10_1));
		authServices.execute(authorizationDeleteRequest(copyOfFolder10Auth1, "zeCollection"));

	}

	private static Folder createSubFolder(Folder parent, String id) {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(parent.getCollection(), appLayerFactory);
		return rm.newFolderWithId(id).setParentFolder(parent).setTitle("Folder " + id).setOpenDate(new LocalDate());
	}

	private static Document createDocument(Folder parent, String id) {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(parent.getCollection(), appLayerFactory);
		return rm.newDocumentWithId(id).setFolder(parent).setTitle("Document " + id);
	}


	private static String toCommaSeparatedArgs(String str) {
		String[] parts = str.split("-");
		return "(" + parts[0] + ", " + parts[1] + ", " + parts[2] + ")";
	}


	public static boolean ajustIfBetterThanExpected(StackTraceElement[] stackTraceElements, String current,
													String expected) {

		String filePath = "/Users/francisbaril/Constellio/IdeaProjects/constellio-dev-2019/constellio/sdk/src/com/constellio/model/services/taxonomies";

		boolean betterThanExpected = isBetterThanExpected(current, expected);
		if (betterThanExpected) {

			int lineNumber;
			String filename;
			for (StackTraceElement element : stackTraceElements) {

				if (element.getClassName().endsWith("AcceptTest")
					&& !(element.getMethodName().equals("solrQueryCounts")
						 || element.getMethodName().equals("secondSolrQueryCounts")
						 || element.getMethodName().equals("secondCallQueryCounts"))) {
					filename = element.getFileName();
					lineNumber = element.getLineNumber();

					File file = new File(filePath + "/" + filename);
					if (file.exists()) {
						System.out.println(filename + ":" + lineNumber + " is changed from " + expected + " to " + current);
						try {
							List<String> lines = FileUtils.readLines(file, "UTF-8");
							System.out.println(lines.size());
							String line = lines.get(lineNumber - 1);
							if (line.contains("solrQueryCounts") || line.contains("secondSolrQueryCounts") || line
									.contains("secondCallQueryCounts")) {
								String modifiedLine = line.replace(toCommaSeparatedArgs(expected), toCommaSeparatedArgs(current));
								lines.set(lineNumber - 1, modifiedLine);

								FileUtils.writeLines(file, "UTF-8", lines);
								System.out.println(lines.size());
								System.out.println(line + " > " + modifiedLine);
							}

						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
					break;
				}
			}
		}
		return betterThanExpected;
	}

	private static boolean isBetterThanExpected(String current, String expected) {
//		if (!current.equals(expected)) {
		//			int[] currentParts = toInts(current.split("-"));
		//			int[] expectedParts = toInts(expected.split("-"));
		//
		//			if (currentParts[0] <= expectedParts[0]
		//				&& currentParts[1] <= expectedParts[1]
		//				&& currentParts[2] <= expectedParts[2]) {
		//				return true;
		//			}
		//		}
		return false;
	}

	private static int[] toInts(String[] split) {

		int[] intParts = new int[split.length];

		for (int i = 0; i < split.length; i++) {
			intParts[i] = Integer.parseInt(split[i]);
		}

		return intParts;
	}

}
