package com.constellio.app.services.records;

import org.assertj.core.groups.Tuple;

import static org.assertj.core.api.Assertions.tuple;

public class SystemCheckManagerAcceptanceTestResources {

	public static Tuple[] expectedErrorsWhenLogicallyDeletedCategoriesAndUnits = new Tuple[]{
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "administrativeUnit", "unitId_20d"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "category", "categoryId_X13"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "administrativeUnit", "unitId_20e"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "category", "categoryId_Z999"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "category", "categoryId_Z200"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "category", "categoryId_ZE42"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "category", "categoryId_Z110"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "category", "categoryId_Z111"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "category", "categoryId_Z120"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "category", "categoryId_Z112"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "administrativeUnit", "unitId_20"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "administrativeUnit", "unitId_12c"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "category", "categoryId_Z"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "category", "categoryId_Z100"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "category", "categoryId_X120"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "category", "categoryId_X100"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "category", "categoryId_X110"),
			tuple("SystemCheckResultsBuilder_logicallyDeletedRecord", "category", "categoryId_X")
	};

	public static String[] expectedErrorsWhenLogicallyDeletedCategoriesAndUnitsErrorMessages = new String[]{
			"L'enregistrement de type Rubrique du plan categoryId_Z111 - Z111 ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Unité administrative unitId_20e - Unité 20-E ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Rubrique du plan categoryId_Z110 - Z110 ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Rubrique du plan categoryId_Z - Ze category ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Rubrique du plan categoryId_Z200 - Z200 ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Rubrique du plan categoryId_ZE42 - Ze 42 ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Unité administrative unitId_12c - Unité 12-C ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Rubrique du plan categoryId_Z120 - Z120 ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Rubrique du plan categoryId_Z100 - Z100 ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Rubrique du plan categoryId_Z112 - Z112 ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Rubrique du plan categoryId_Z999 - Z999 ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Unité administrative unitId_20 - Unité 20 ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Rubrique du plan categoryId_X13 - Agent Secreet ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Unité administrative unitId_20d - Unité 20-D ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Rubrique du plan categoryId_X110 - X110 ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Rubrique du plan categoryId_X120 - X120 ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Rubrique du plan categoryId_X - Xe category ne devrait pas être supprimé logiquement",
			"L'enregistrement de type Rubrique du plan categoryId_X100 - X100 ne devrait pas être supprimé logiquement"};

	public static String expectedMessage1 = ""
											+ "Démarrage : 2014-12-12T00:00:00.000\n"
											+ "Statut : Terminé\n"
											+ "Enregistrements réparés : 0\n"
											+ "Références brisées : 2\n"
											+ "Références analysées : 3\n"
											+ "\n"
											+ "2 erreur(s) : \n"
											+ "\t- La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem1 fait référence à un enregistrement inexistant : bad\n"
											+ "\t- La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem2 fait référence à un enregistrement inexistant : notGood\n"
											+ "\n";

	public static String expectedMessage2 = ""
											+ "Démarrage : 2014-12-12T00:00:00.000\n"
											+ "Statut : Terminé\n"
											+ "Enregistrements réparés : 2\n"
											+ "Références brisées : 2\n"
											+ "Références analysées : 3\n"
											+ "\n"
											+ "2 erreur(s) : \n"
											+ "\t- La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem1 fait référence à un enregistrement inexistant : bad\n"
											+ "\t- La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem2 fait référence à un enregistrement inexistant : notGood\n"
											+ "\n";

	public static String expectedMessage3 = ""
											+ "Démarrage : 2014-12-12T00:00:00.000\n"
											+ "Statut : Terminé\n"
											+ "Enregistrements réparés : 0\n"
											+ "Références brisées : 2\n"
											+ "Références analysées : 7\n"
											+ "\n"
											+ "2 erreur(s) : \n"
											+ "\t- La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem1 fait référence à un enregistrement inexistant : recordC\n"
											+ "\t- La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem2 fait référence à un enregistrement inexistant : recordC\n"
											+ "\n";

	public static String expectedMessage4 = ""
											+ "Démarrage : 2014-12-12T00:00:00.000\n"
											+ "Statut : Terminé\n"
											+ "Enregistrements réparés : 2\n"
											+ "Références brisées : 2\n"
											+ "Références analysées : 7\n"
											+ "\n"
											+ "2 erreur(s) : \n"
											+ "\t- La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem1 fait référence à un enregistrement inexistant : recordC\n"
											+ "\t- La métadonnée anotherSchemaType_default_referenceFromAnotherSchemaToZeSchema de l'enregistrement recordWithProblem2 fait référence à un enregistrement inexistant : recordC\n"
											+ "\n";

	public static String expectedMessage5 = ""
											+ "Démarrage : 2014-12-12T00:00:00.000\n"
											+ "Statut : Terminé\n"
											+ "Enregistrements réparés : 0\n"
											+ "Références analysées : 89\n"
											+ "Unités administratives logiquement supprimées : 4\n"
											+ "Rubriques du plan logiquement supprimées : 14\n"
											+ "\n"
											+ "22 erreur(s) : \n"
											+ "\t- Dans le type de schéma de métadonnées document, le schéma code form ne débute pas par USR\n"
											+ "\t- Dans le type de schéma de métadonnées document, le schéma code report ne débute pas par USR\n"
											+ "\t- Dans le type de schéma de métadonnées folder, le schéma code employe ne débute pas par USR\n"
											+ "\t- Dans le type de schéma de métadonnées folder, le schéma code meetingFolder ne débute pas par USR\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_X - Xe category ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_X100 - X100 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_X110 - X110 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_X120 - X120 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_X13 - Agent Secreet ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z - Ze category ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z100 - Z100 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z110 - Z110 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z111 - Z111 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z112 - Z112 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z120 - Z120 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z200 - Z200 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z999 - Z999 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_ZE42 - Ze 42 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Unité administrative unitId_12c - Unité 12-C ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Unité administrative unitId_20 - Unité 20 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Unité administrative unitId_20d - Unité 20-D ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Unité administrative unitId_20e - Unité 20-E ne devrait pas être supprimé logiquement\n"
											+ "\n";

	public static String expectedMessage6 = ""
											+ "Démarrage : 2014-12-12T00:00:00.000\n"
											+ "Statut : Terminé\n"
											+ "Enregistrements réparés : 18\n"
											+ "Références analysées : 89\n"
											+ "Unités administratives logiquement supprimées : 4\n"
											+ "Rubriques du plan logiquement supprimées : 14\n"
											+ "\n"
											+ "Unités administratives physiquement supprimées : \n"
											+ "\t- 12C - Unité 12-C\n"
											+ "\t- 20D - Unité 20-D\n"
											+ "\t- 20E - Unité 20-E\n"
											+ "\n"
											+ "Unités administratives restaurées : \n"
											+ "\t- 20 - Unité 20\n"
											+ "\n"
											+ "Rubriques du plan physiquement supprimées : \n"
											+ "\t- X - Xe category\n"
											+ "\t- X100 - X100\n"
											+ "\t- X110 - X110\n"
											+ "\t- X120 - X120\n"
											+ "\t- X13 - Agent Secreet\n"
											+ "\t- Z110 - Z110\n"
											+ "\t- Z111 - Z111\n"
											+ "\t- Z112 - Z112\n"
											+ "\t- Z120 - Z120\n"
											+ "\t- Z200 - Z200\n"
											+ "\t- Z999 - Z999\n"
											+ "\t- ZE42 - Ze 42\n"
											+ "\n"
											+ "Rubriques du plan restaurées : \n"
											+ "\t- Z - Ze category\n"
											+ "\t- Z100 - Z100\n"
											+ "\n"
											+ "\n"
											+ "22 erreur(s) : \n"
											+ "\t- Dans le type de schéma de métadonnées document, le schéma code form ne débute pas par USR\n"
											+ "\t- Dans le type de schéma de métadonnées document, le schéma code report ne débute pas par USR\n"
											+ "\t- Dans le type de schéma de métadonnées folder, le schéma code employe ne débute pas par USR\n"
											+ "\t- Dans le type de schéma de métadonnées folder, le schéma code meetingFolder ne débute pas par USR\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_X - Xe category ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_X100 - X100 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_X110 - X110 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_X120 - X120 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_X13 - Agent Secreet ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z - Ze category ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z100 - Z100 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z110 - Z110 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z111 - Z111 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z112 - Z112 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z120 - Z120 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z200 - Z200 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_Z999 - Z999 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Rubrique du plan categoryId_ZE42 - Ze 42 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Unité administrative unitId_12c - Unité 12-C ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Unité administrative unitId_20 - Unité 20 ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Unité administrative unitId_20d - Unité 20-D ne devrait pas être supprimé logiquement\n"
											+ "\t- L'enregistrement de type Unité administrative unitId_20e - Unité 20-E ne devrait pas être supprimé logiquement\n"
											+ "\n";
}
