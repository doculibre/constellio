package com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip;


public class CEICSIP {
	
//	private static MetsObjectsProvider readXLS(File inDir) throws Exception {
//		List<MetsObject> sipObjects = new ArrayList<MetsObject>();
//		File xlsFile = new File(inDir, "Métadonnées fiches Intelligid pour test paquets SIP.xls");
//		File bagDir = new File(inDir, "bag");
//		WorkbookSettings workbookSettings = new WorkbookSettings();
//		workbookSettings.setEncoding("cp1252");
//		Workbook workbook = Workbook.getWorkbook(xlsFile, workbookSettings);
//		for (Sheet sheet : workbook.getSheets()) {
//			String sheetName = sheet.getName();
//        	List<String> metadataIds = new ArrayList<String>();
//        	Map<String, String> metadataLabels = new LinkedHashMap<String, String>();
//        	for (int j = 1; j < sheet.getColumns(); j++) {
//        		// Nom métadonnée
//	            Cell cell = sheet.getCell(j, 1);
//	            String contents = cell.getContents().trim();
//	            metadataIds.add(contents);
//	            metadataLabels.put(contents, contents);
//        	}	
//        	// Ignorer les deux premières lignes et la première colonne
//	        for (int i = 2; i < sheet.getRows(); i++) {
//	        	Map<String, List<String>> metadataValues = new LinkedHashMap<String, List<String>>();
//	        	for (int j = 1; j < sheet.getColumns(); j++) {
//		        	String metadataId = metadataIds.get(j - 1);
//		            Cell cell = sheet.getCell(j, i);
//		            String contents = cell.getContents().trim();
//		            metadataValues.put(metadataId, Arrays.asList(contents));
//		        }
//	        	
//	        	MetsObject sipObject;
//	        	String id = sheet.getCell(1, i).getContents().trim();
//	        	if (StringUtils.isNotBlank(id)) {
//	            	MetsFolder parentFolder;
//		            if ("Document".equalsIgnoreCase(sheetName) || "Courriel".equalsIgnoreCase(sheetName)) {
//		            	String filename = metadataValues.get("Nom de fichier").get(0);
//		            	String folderIdAndTitle = metadataValues.get("Dossier parent").get(0);
//		            	String folderId = StringUtils.substringBefore(folderIdAndTitle, "-").trim();
//		            	String folderTitle = StringUtils.substringAfter(folderIdAndTitle, "-").trim();
//		            	
//		            	File file = new File(bagDir, filename);
//		            	if (!file.exists()) {
//		            		file = new File(bagDir, "potato.txt");
//		            	}
//		            	
//		            	String parentFolderIdAndTitle = getParentFolderIdAndTitle(workbook, folderIdAndTitle);
//		            	if (StringUtils.isNotBlank(parentFolderIdAndTitle)) {
//		            		String parentFolderId = StringUtils.substringBefore(parentFolderIdAndTitle, "-").trim();
//		            		String parentFolderTitle = StringUtils.substringAfter(parentFolderIdAndTitle, "-").trim();
//		            		parentFolder = new BaseMetsFolder(parentFolderId, new ArrayList<String>(), new LinkedHashMap<String, String>(), new LinkedHashMap<String, List<String>>(), parentFolderTitle, null, null);
//		            	} else {
//		            		parentFolder = null;
//		            	}
//		            	MetsFolder folder = new BaseMetsFolder(folderId, new ArrayList<String>(), new LinkedHashMap<String, String>(), new LinkedHashMap<String, List<String>>(), folderTitle, parentFolder, null);
//		            	sipObject = new BaseMetsDocument(id, filename, metadataIds, metadataLabels, metadataValues, file, folder);
//		            } else if ("Dossier".equalsIgnoreCase(sheetName) || "DossierSujet".equals(sheetName)) {
//		            	String title;
//		            	if ("Dossier".equalsIgnoreCase(sheetName)) {
//		            		title = metadataValues.get("Titre du dossier").get(0);
//		            		
//		            		String folderIdAndTitle = id + " - " + title;
//		            		String parentFolderIdAndTitle = getParentFolderIdAndTitle(workbook, folderIdAndTitle);
//			            	if (StringUtils.isNotBlank(parentFolderIdAndTitle)) {
//			            		String parentFolderId = StringUtils.substringBefore(parentFolderIdAndTitle, "-").trim();
//			            		String parentFolderTitle = StringUtils.substringAfter(parentFolderIdAndTitle, "-").trim();
//			            		parentFolder = new BaseMetsFolder(parentFolderId, new ArrayList<String>(), new LinkedHashMap<String, String>(), new LinkedHashMap<String, List<String>>(), parentFolderTitle, null, null);
//			            	} else {
//			            		parentFolder = null;
//			            	}
//		            	} else {
//		            		title = metadataValues.get("Nom, prénom").get(0);
//		            		parentFolder = null;
//		            	}
//		            	sipObject = new BaseMetsFolder(id, metadataIds, metadataLabels, metadataValues, title, parentFolder, null);
//		            } else {
//		            	throw new RuntimeException(sheetName);
//		            }
//		            sipObjects.add(sipObject);
//	        	}
//		    }
//		}
//	    return new BaseMetsObjectsProvider(sipObjects);
//	}
//	
//	private static String getParentFolderIdAndTitle(Workbook workbook, String folderIdAndTitle) {
//		String parentFolderIdAndTitle = null;
//		if (folderIdAndTitle != null) {
//			String folderId =  StringUtils.substringBefore(folderIdAndTitle, "-").trim();
//			Sheet dossierSheet = workbook.getSheet("Dossier");
//	        for (int i = 2; i < dossierSheet.getRows(); i++) {
//	        	String folderIdCell = dossierSheet.getCell(1, i).getContents();
//	        	if (folderId.equals(folderIdCell)) {
//		        	parentFolderIdAndTitle = dossierSheet.getCell(18, i).getContents();
//		        	break;
//	        	}
//	        }
//		}
//        return parentFolderIdAndTitle;
//	}
//
//	public static void main(String[] args) throws Exception {
//		File inDir = new File("in");
//		File outDir = new File("out");
//		
//		File metsPaquetInfoFile = new File(inDir, "mets-paquet-info.txt");
//		
//		File outFile = new File(outDir, "sip.zip");
//		if (outFile.exists()) {
//			outFile.delete();
//		}
//		
//		InputStream metsPaquetInfoIn = new FileInputStream(metsPaquetInfoFile);
//		List<String> packageInfoLines = IOUtils.readLines(metsPaquetInfoIn);
//		metsPaquetInfoIn.close();
//		
//		MetsObjectsProvider sipObjectsProvider = readXLS(inDir);
//
//		ConstellioSIP constellioSIP = new ConstellioSIP(sipObjectsProvider, packageInfoLines);
//		constellioSIP.build(new FileOutputStream(outFile));
//	}

}
