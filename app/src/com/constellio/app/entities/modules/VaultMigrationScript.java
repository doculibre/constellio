package com.constellio.app.entities.modules;

import static com.constellio.app.utils.ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.contents.ContentImpl;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class VaultMigrationScript {

	private static final int LENGTH_OF_HASH = 28;
	private static final int LENGTH_OF_PARSED_HASH = 36;
	private static final int LENGTH_OF_PREVIEW_HASH = 36;
	//	static String currentCollection;
	static AppLayerFactory appLayerFactory;
	static ModelLayerFactory modelLayerFactory;
	static SearchServices searchServices;
	static RecordServices recordServices;

	private static void startBackend() {
		//Only enable this line to run in production
		appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();

		//Only enable this line to run on developer workstation
		//		appLayerFactory = SDKScriptUtils.startApplicationWithoutBackgroundProcessesAndAuthentication();

	}

	public static Set<String> improveHashCodes(AppLayerFactory appLayerFactory)
			throws Exception {
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		searchServices = modelLayerFactory.newSearchServices();
		recordServices = modelLayerFactory.newRecordServices();
		final Set<String> setOfHashCodes = new HashSet<>();
		List<String> collections = modelLayerFactory.getCollectionsListManager().getCollections();
		for (String collection : collections) {
			MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			List<MetadataSchemaType> metadataSchemaTypeList = metadataSchemaTypes.getSchemaTypes();
			for (final MetadataSchemaType metadataSchemaType : metadataSchemaTypeList) {
				for (Metadata metadata : metadataSchemaType.getAllMetadatas()) {
					if (MetadataValueType.CONTENT.equals(metadata.getType())) {
						final String metadataCode = metadata.getCode();
						ActionExecutorInBatch actionExecutorInBatch = new ActionExecutorInBatch(searchServices, "", 1000) {

							@Override
							public void doActionOnBatch(List<Record> records)
									throws Exception {
								Metadata contentMetadata = metadataSchemaType.getMetadata(metadataCode);
								Transaction transaction = new Transaction();
								for (Record record : records) {

									for (Content recordContent : record.<Content>getValues(contentMetadata)) {
										ContentImpl content = (ContentImpl) recordContent;
										content.changeHashCodesOfAllVersions();
										setOfHashCodes.addAll(content.getHashOfAllVersions());
									}

								}
								transaction.update(records);
								recordServices.execute(transaction);
							}
						};

						actionExecutorInBatch
								.execute(new LogicalSearchQuery(from(metadataSchemaType).where(metadata).isNotNull()));
					}
				}
			}
		}
		return setOfHashCodes;
	}

	public static void migrateVault(AppLayerFactory appLayerFactory)
			throws Exception {
		//      Longueur du hash : 28
		//      Longueur du parsedHash : 36
		Set<String> listOfHashCodes = improveHashCodes(appLayerFactory);
		DataLayerConfiguration dataLayerConfiguration = appLayerFactory.getModelLayerFactory().getDataLayerFactory()
				.getDataLayerConfiguration();
		File parentFile = dataLayerConfiguration.getContentDaoFileSystemFolder();

		java.util.Collection<File> leafFiles = FileUtils.listFiles(parentFile, null, true);
		Iterator<File> iterator = leafFiles.iterator();
		File fileToMove;
		while (iterator.hasNext()) {
			fileToMove = iterator.next();
			Boolean firstCondition =
					fileToMove.getName().length() < LENGTH_OF_HASH && !fileToMove.getName().contains("parsed") && !fileToMove
							.getName().contains("preview");
			Boolean secondCondition =
					fileToMove.getName().length() < LENGTH_OF_PARSED_HASH && fileToMove.getName().contains("parsed");
			Boolean thirdCondition =
					fileToMove.getName().length() < LENGTH_OF_PREVIEW_HASH && fileToMove.getName().contains("preview");
			if (firstCondition || secondCondition || thirdCondition) {
				moveFileToRightPath(fileToMove, parentFile, listOfHashCodes, true);
			} else {
				moveFileToRightPath(fileToMove, parentFile, listOfHashCodes, false);
			}
		}
	}

	public static String rename(Set<String> listOfHashCodes, String oldName) {
		String newName = "";
		boolean contain = false;
		String[] oldNameParts = oldName.split("_");
		for (String hashCode : listOfHashCodes) {
			for (int index = 0; index < oldNameParts.length; index++) {
				if (!hashCode.contains(oldNameParts[index])) {
					contain = false;
					break;
				} else {
					contain = true;
				}
			}
			if (contain == true) {
				return hashCode;
			}
		}
		return newName;
	}

	private static void moveFileToRightPath(File fileToMove, File parentFile, Set<String> listOfHashCodes, boolean changeName)
			throws IOException {
		File folderToRemove = fileToMove.getParentFile();
		String newName = "";
		while (!folderToRemove.getName().equals(parentFile.getName()) && changeName) {
			newName = folderToRemove.getName() + "_" + newName;
			folderToRemove = folderToRemove.getParentFile();
		}
		newName = newName + fileToMove.getName();

		if (newName.contains("+")) {
			newName = newName.replace("+", "-");
		}

		Boolean firstCondition = newName.length() < LENGTH_OF_HASH && !newName.contains("parsed") && !newName.contains("preview");
		Boolean secondCondition = newName.length() < LENGTH_OF_PARSED_HASH && newName.contains("parsed");
		Boolean thirdCondition = newName.length() < LENGTH_OF_PREVIEW_HASH && newName.contains("preview");
		if (firstCondition) {
			newName = rename(listOfHashCodes, newName);
		}
		if (secondCondition) {
			newName = rename(listOfHashCodes, newName.replace("__parsed", "")) + "__parsed";
		}
		if (thirdCondition) {
			newName = rename(listOfHashCodes, newName.replace(".preview", "")) + ".preview";
		}
		folderToRemove = fileToMove.getParentFile();

		String parentFilePath = parentFile.getAbsolutePath();
		File folder = new File(
				parentFilePath + "/" + StringUtils.substring(newName, 0, 1) + "/" + StringUtils.substring(newName, 0, 2) + "/"
						+ StringUtils.substring(newName, 0, 3));
		folder.mkdirs();
		fileToMove.renameTo(new File(folder.getAbsolutePath() + "/" + newName));
		removePathIfEmpty(folderToRemove);
	}

	private static void removePathIfEmpty(File file) {
		File immediateParent = file.getParentFile();
		if (file.listFiles().length == 0) {
			file.delete();
			removePathIfEmpty(immediateParent);
		}
	}

	/**
	 * Étapes :
	 * 1. On roule un check d’intégrité de la voute (nous dit le nombre de fichiers dans la voute et le nombre de fichiers manquants)
	 * 2. On arrête Constellio
	 * 3. On modifie manuellement les 2 configs du fichier conf/constellio.properties
	 * dao.contents.filesystem.separatormode=THREE_LEVELS_OF_ONE_DIGITS
	 * hashing.encoding=BASE64_URL_ENCODED
	 * 4. On roule le script, un Main sans paramètre (cd /opt/constellio/webapp-x.y.z/WEB-INF; sudo java -cp “lib/*:classes” com.constellio.app.entities.modules.VaultMigrationScript)
	 * 5. On repart
	 * 6. On roule un check d’intégrité de la voute, on compare avec celui du début, ça devrait être identique
	 *
	 *Note : la voute ne sera pas dans un état super clean : il reste des dossiers avec l'ancien pattern, mais c’est des dossiers
	 * vides ou des contenus non référencé dans solr. On fera un autre script pour ça, mais ça ne dérange pas pour le moment.
	 *
	 * @param argv
	 * @throws Exception
	 */
	public static void main(String argv[])
			throws Exception {
		startBackend();

		migrateVault(appLayerFactory);
	}

}
