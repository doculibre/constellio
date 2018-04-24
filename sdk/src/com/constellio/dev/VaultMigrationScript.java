package com.constellio.dev;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.*;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.contents.ContentImpl;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.utils.ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class VaultMigrationScript {

	static String currentCollection;
	static AppLayerFactory appLayerFactory;
	static ModelLayerFactory modelLayerFactory;
	static SearchServices searchServices;
	static RecordServices recordServices;
	static RMSchemasRecordsServices rm;

	private static void startBackend() {
		//Only enable this line to run in production
		appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();

		//Only enable this line to run on developer workstation
//		appLayerFactory = SDKScriptUtils.startApplicationWithoutBackgroundProcessesAndAuthentication();

	}

	public static List<String> improveHashCodes(AppLayerFactory appLayerFactory, String collection) throws Exception {
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		searchServices = modelLayerFactory.newSearchServices();
		recordServices = modelLayerFactory.newRecordServices();
		final List<String> listOfHashCodes = new ArrayList<>();
		MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		List<MetadataSchemaType> metadataSchemaTypeList = metadataSchemaTypes.getSchemaTypes();
		for (final MetadataSchemaType metadataSchemaType : metadataSchemaTypeList) {
			for (Metadata metadata : metadataSchemaType.getAllMetadatas()) {
				if (MetadataValueType.CONTENT.equals(metadata.getType())) {
					final String metadataCode = metadata.getCode();
					ActionExecutorInBatch actionExecutorInBatch = new ActionExecutorInBatch(searchServices, "", 1000) {

						@Override
						public void doActionOnBatch(List<Record> records) throws Exception {
							Metadata contentMetadata = metadataSchemaType.getMetadata(metadataCode);
							Transaction transaction = new Transaction();
							for (Record record : records) {
								Content recordContent = record.get(contentMetadata);
								ContentImpl content = (ContentImpl) recordContent;
								content.changeHashCodesOfAllVersions();
								listOfHashCodes.addAll(content.getHashOfAllVersions());
							}
							transaction.update(records);
							recordServices.execute(transaction);
						}
					};

					actionExecutorInBatch.execute(new LogicalSearchQuery(from(metadataSchemaType).where(metadata).isNotNull()));
				}
			}
		}
		return listOfHashCodes;
	}

	public static void migrateVault(File parentFile,List<String> listOfHashCodes) throws IOException, RecordServicesException {
//      Longueur du hash : 28
//      Longueur du parsedHash : 36
		List<File> leafFiles = getFiles(parentFile);
		File fileToMove;
		for (int index = 0; index<leafFiles.size(); index++){
			fileToMove = leafFiles.get(index);
			if((fileToMove.getName().length() < 28 && !fileToMove.getName().contains("parsed")) ||
					(fileToMove.getName().length() < 36 && fileToMove.getName().contains("parsed"))){
				moveFileToRightPath(fileToMove, parentFile,listOfHashCodes, true);
			}else if(fileToMove.getName().contains("+")){
				moveFileToRightPath(fileToMove, parentFile,listOfHashCodes, false);
			}
		}
	}

	private static String rename(List<String> listOfHashCodes, String oldName){
		String newName = "";
		boolean contain = false;
		String [] oldNameParts =oldName.split("_");
		for (String hashCode : listOfHashCodes){
			for(int index=0; index<oldNameParts.length; index++){
				if (!hashCode.contains(oldNameParts[index])){
					contain = false;
					break;
				}else{
					contain = true;
				}
			}
			if (contain == true){
				return hashCode;
			}
		}
		return newName;
	}

	private static void moveFileToRightPath(File fileToMove, File parentFile,List<String> listOfHashCodes, boolean changeName) throws IOException {
		File folderToRemove = fileToMove.getParentFile();
		String newName = "";
		while(!folderToRemove.getName().equals(parentFile.getName()) && changeName){
			newName = folderToRemove.getName() + "_" + newName ;
			folderToRemove = folderToRemove.getParentFile();
		}
		newName = newName + fileToMove.getName();

		if(newName.contains("+")){
			newName = newName.replace("+","-");
		}

		if((newName.length() < 28 && !newName.contains("parsed"))){
			newName = rename(listOfHashCodes,newName);
		}else if(newName.length() < 36 && newName.contains("parsed")){
			newName = rename(listOfHashCodes,newName);
		}

		folderToRemove = fileToMove.getParentFile();

		String parentFilePath =  parentFile.getAbsolutePath();
		createFolder(parentFilePath ,"/" + StringUtils.substring(newName,0,1));
		createFolder(parentFilePath ,"/" + StringUtils.substring(newName,0,1) + "/" + StringUtils.substring(newName,0,2));
		File folder = createFolder(parentFilePath, "/" + StringUtils.substring(newName,0,1)+ "/" + StringUtils.substring(newName,0,2) + "/" + StringUtils.substring(newName,0,3));
		fileToMove.renameTo(new File(folder.getAbsolutePath() + "/" + newName ));
		removePathIfEmpty(folderToRemove);
	}

	private static File createFolder(String parentFilePath, String newPath){
		File newFolder = new File(parentFilePath+ newPath);
		newFolder.mkdir();
		return  newFolder;
	}

	private static void removePathIfEmpty(File file){
		File immediateParent = file.getParentFile();
		if(file.listFiles().length == 0){
			file.delete();
			removePathIfEmpty(immediateParent);
		}
	}

	private static List<File> getFiles(File file){
		File[] children = file.listFiles();
		List<File> listOfFiles = new ArrayList<>();
		for( int  index=0; index< children.length; index++){
			if(children[index].isFile()){
				listOfFiles.add(children[index]);
			}
			else{
				listOfFiles.addAll(getFiles(children[index]));
			}
		}
		return listOfFiles;
	}

	public static void main(String argv[])
			throws Exception {
//		RecordPopulateServices.LOG_CONTENT_MISSING = false;
		startBackend();

		improveHashCodes(appLayerFactory, currentCollection);
	}

}
