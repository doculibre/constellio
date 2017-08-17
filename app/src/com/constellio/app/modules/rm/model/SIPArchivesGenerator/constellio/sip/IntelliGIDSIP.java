package com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip;

import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.filter.SIPFilter;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.data.intelligid.IntelliGIDSIPObjectsProvider;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.exceptions.SIPMaxReachedException;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;

public class IntelliGIDSIP {
	
	private static final String KEY_VALUE_SEP = "=";
	
	private static final String VALUES_SEP = ";";
	
	private static Map<String, List<String>> parse(String[] args) {
		Map<String, List<String>> argsMap = new LinkedHashMap<String, List<String>>();
		for (String arg : args) {
			String optionName = StringUtils.substringBefore(arg, KEY_VALUE_SEP);
			String argValue = StringUtils.substringAfter(arg, KEY_VALUE_SEP);
			List<String> optionValues = argsMap.get(optionName);
			if (optionValues == null) {
				optionValues = new ArrayList<String>();
				argsMap.put(optionName, optionValues);
			}
			if (argValue != null) {
				String[] splittedArgValue = StringUtils.split(argValue, VALUES_SEP);
				for (String optionValue : splittedArgValue) {
					optionValues.add(optionValue);
				}
			}
		}
		return argsMap;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getOption(String optionName, T defaultValue, Class<T> clazz, Map<String, List<String>> options) {
		T castedOptionValue;
		List<String> optionValues = options.get(optionName);
		if (optionValues == null) {
			optionValues = new ArrayList<String>();
		}
		if (Boolean.class.equals(clazz)) {
			if (optionValues.isEmpty()) {
				castedOptionValue = defaultValue;
			} else if (optionValues.size() == 1) {
				castedOptionValue = (T) new Boolean(optionValues.get(0));
			} else {
				throw new IllegalArgumentException(optionName + " has multiple values " + optionValues);
			}
		} else if (Integer.class.isAssignableFrom(clazz)) {
			if (optionValues.isEmpty()) {
				castedOptionValue = defaultValue;
			} else if (optionValues.size() == 1) {
				castedOptionValue = (T) new Integer(optionValues.get(0));
			} else {
				throw new IllegalArgumentException(optionName + " has multiple values " + optionValues);
			}
		} else if (Long.class.isAssignableFrom(clazz)) {
			if (optionValues.isEmpty()) {
				castedOptionValue = defaultValue;
			} else if (optionValues.size() == 1) {
				castedOptionValue = (T) new Long(optionValues.get(0));
			} else {
				throw new IllegalArgumentException(optionName + " has multiple values " + optionValues);
			}
		} else if (String.class.isAssignableFrom(clazz)) {
			if (optionValues.isEmpty()) {
				castedOptionValue = defaultValue;
			} else if (optionValues.size() == 1) {
				castedOptionValue = (T) optionValues.get(0);
			} else {
				throw new IllegalArgumentException(optionName + " has multiple values " + optionValues);
			}
		} else if (List.class.isAssignableFrom(clazz)) {
			if (optionValues.isEmpty()) {
				castedOptionValue = defaultValue;
			} else {
				castedOptionValue = (T) optionValues;
			}	
		} else {
			throw new IllegalArgumentException(optionName + " has an invalid type " + clazz.getName());
		}
		return castedOptionValue;
	}
	
	private static List<String> splitIds(String str) {
		List<String> ids = new ArrayList<>();
		ids.addAll(asList(StringUtils.split(str, ",")));
		return ids;
	}
	
	@SuppressWarnings("unchecked")
	public static void RegisterScript(AppLayerFactory factory, String collection) throws Exception{
//		StringBuffer usage = new StringBuffer("java " + IntelliGIDSIP.class.getName());
//		usage.append(" startIndex=[0] (Premier index)");
//		usage.append(" fichier=[/indexation/intelligid/tomcat/infos-sip-machin.zip] (Chemin du fichier SIP à générer)");
//		usage.append(" bagInfo=[/indexation/intelligid/tomcat/Baginfo.txt] (Chemin du fichier texte à intégrer au fichier Baginfo.txt)");
//		usage.append(" codeRubrique=[A123] (Code de la rubrique à utiliser pour générer le SIP)");
//		usage.append(" codeUniteAdministrative=[B456] (Code de l'unité administrative à utiliser pour générer le SIP)");
//		usage.append(" typeDocumentRequis=[true|false] (N'inclure que les documents qui ont un type?)");
//		usage.append(" idsFichesDossiersIncluses=[123456,123457]  (les identifiants des dossiers à inclure, séparés par des virgules)");
//		usage.append(" idsFichesDossiersExclues=[123456,123457]  (les identifiants des dossiers à exclure, séparés par des virgules)");
//		usage.append(" idsFichesDocumentsIncluses=[123456,123457]  (les identifiants des idsFichesDocumentsIncluses à inclure, séparés par des virgules)");
//		usage.append(" dossierExtraction=[/indexation/intelligid/tomcat/] (Chemin du dossier dans lequel déposer les SIPs)");
//		usage.append(" limiterTaille=[true|false] (Limiter la taille des SIP?)");
//		System.out.println(usage);

		//TODO CHECK OPTIONS
		Map<String, List<String>> options = parse(null);
		
		Integer startIndex = getOption("startIndex", 0, Integer.class, options);
	    String cheminFichier = getOption("fichier", null, String.class, options);
	    String bagInfo = getOption("bagInfo", null, String.class, options);
	    String codeRubrique = getOption("codeRubrique", null, String.class, options);
	    String codeUniteAdministrative = getOption("codeUniteAdministrative", null, String.class, options);
	    Boolean typeDocumentRequis = getOption("typeDocumentRequis", Boolean.TRUE, Boolean.class, options);
	    List<String> idsFichesDossiersIncluses = splitIds(getOption("idsFichesDossiersIncluses", "", String.class, options));
	    List<String> idsFichesDossiersExclues = splitIds(getOption("idsFichesDossiersExclues", "", String.class, options));
	    List<String> idsFichesDocumentsIncluses = splitIds(getOption("idsFichesDocumentsIncluses", "", String.class, options));
	    String dossierExtraction = getOption("dossierExtraction", null, String.class, options);
	    Boolean limiterTaille = getOption("limiterTaille", Boolean.TRUE, Boolean.class, options);
		
	    System.out.println(cheminFichier);
	    System.out.println(bagInfo);
	    System.out.println("startIndex: " + startIndex);
	    System.out.println("codeRubrique: " + codeRubrique);
	    System.out.println("codeUniteAdministrative: " + codeUniteAdministrative);
	    System.out.println("typeDocumentRequis: " + typeDocumentRequis);
	    System.out.println("idsFichesDossiersIncluses: " + idsFichesDossiersIncluses);
	    System.out.println("idsFichesDossiersExclues: " + idsFichesDossiersExclues);
	    System.out.println("idsFichesDocumentsIncluses: " + idsFichesDocumentsIncluses);
	    System.out.println("dossierExtraction: " + dossierExtraction);
	    System.out.println("limiterTaille: " + limiterTaille);

		File bagInfoFile = new File(bagInfo);
		InputStream bagInfoIn = new FileInputStream(bagInfoFile);
		List<String> packageInfoLines = IOUtils.readLines(bagInfoIn);
		bagInfoIn.close();
		
		if (dossierExtraction != null) {
			File outFolder = new File(dossierExtraction);
			//getFolder with category and parent is null;
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, factory);
			SearchServices searchServices = factory.getModelLayerFactory().newSearchServices();
			MetadataSchemaType folderSchemaType = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(Folder.SCHEMA_TYPE);
			LogicalSearchCondition condition = LogicalSearchQueryOperators.from(folderSchemaType).whereAllConditions(
					where(folderSchemaType.getMetadata(Folder.PARENT_FOLDER)).isNull(),
					where(folderSchemaType.getMetadata(Folder.CATEGORY_CODE)).isEqualTo(codeRubrique)
			);
			List<Folder> fichesDossiers = rm.wrapFolders(searchServices.search(new LogicalSearchQuery(condition)));
			
			System.out.println("Génération des SIP pour les dossiers de la rubrique " + codeRubrique + " : " + fichesDossiers.size());
			for (int i = 0; i < fichesDossiers.size(); i++) {
				Folder ficheDossierEnfant = fichesDossiers.get(i);
				System.out.println("Dossier " + ficheDossierEnfant.getId() + " (" + (i + 1) + " de " + fichesDossiers.size() + ")");
				
				String nomSipDossier = "sip-ceic-dossier-" + ficheDossierEnfant.getId() + ficheDossierEnfant.getTitle() + ".zip";
				File outFile = new File(outFolder, nomSipDossier);
				String outFilename = outFile.getName();
				if (!outFile.exists()) {
					int sipIndex = 1;
					while (true) {
						try {
							if (sipIndex > 1) {
								String newOutFilename = outFilename.replace(".zip", "-" + sipIndex + ".zip");
								outFile = new File(outFile.getParentFile(), newOutFilename);
							}

							//COPY THAT
							SIPFilter filter = new SIPFilter(collection, factory)
									.withStartIndex(startIndex)
									.withDocumentTypeRequired(typeDocumentRequis)
									.withExcludeFolderIds(idsFichesDossiersExclues)
									.withIncludeFolderIds(Collections.singletonList(ficheDossierEnfant.getId()))
									.withIncludeDocumentIds(idsFichesDocumentsIncluses);
							IntelliGIDSIPObjectsProvider metsObjectsProvider = new IntelliGIDSIPObjectsProvider(collection, factory, filter);
							if (!metsObjectsProvider.list().isEmpty()) {
								ConstellioSIP constellioSIP = new ConstellioSIP(metsObjectsProvider, packageInfoLines, limiterTaille);
								constellioSIP.build(outFile);
							}
							break;
						} catch (SIPMaxReachedException e) {
							sipIndex++;
							System.out.println(e.getMessage());
							startIndex = e.getLastDocumentIndex();
						}
					}
				}
			}
		} else {
			File outFile = new File(cheminFichier);
			if (outFile.exists()) {
				outFile.delete();
			}
			String outFilename = outFile.getName();
			
			int sipIndex = 1;
			while (true) {
				try {
					if (sipIndex > 1) {
						String newOutFilename = outFilename.replace(".zip", "-" + sipIndex + ".zip");
						outFile = new File(outFile.getParentFile(), newOutFilename);
					}
					SIPFilter filter = new SIPFilter(collection, factory)
							.withStartIndex(startIndex)
							.withRubriqueCode(codeRubrique)
							.withAdministrativeUnit(codeUniteAdministrative)
							.withDocumentTypeRequired(typeDocumentRequis)
							.withIncludeFolderIds(idsFichesDossiersIncluses)
							.withExcludeFolderIds(idsFichesDossiersExclues)
							.withIncludeDocumentIds(idsFichesDocumentsIncluses);
					IntelliGIDSIPObjectsProvider metsObjectsProvider = new IntelliGIDSIPObjectsProvider(collection, factory, filter);
					ConstellioSIP constellioSIP = new ConstellioSIP(metsObjectsProvider, packageInfoLines, limiterTaille);
					constellioSIP.build(outFile);
					break;
				} catch (SIPMaxReachedException e) {
					sipIndex++;
					System.out.println(e.getMessage());
					int lastDocumentIndex = e.getLastDocumentIndex();
					startIndex = lastDocumentIndex;
				}
			}
		}
		
		//FGDSpringUtils.getConversationManager().cancelConversation();
	}

}
