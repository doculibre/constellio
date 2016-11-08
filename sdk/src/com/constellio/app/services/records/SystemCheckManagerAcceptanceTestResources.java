package com.constellio.app.services.records;

import static org.assertj.core.api.Assertions.tuple;

import org.assertj.core.groups.Tuple;

public class SystemCheckManagerAcceptanceTestResources {

	public static Tuple[] expectedErrorsWhenLogicallyDeletedCategoriesAndUnits = new Tuple[] {
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

	public static String[] expectedErrorsWhenLogicallyDeletedCategoriesAndUnitsErrorMessages = new String[] {
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
			"L'enregistrement de type Rubrique du plan categoryId_X100 - X100 ne devrait pas être supprimé logiquement" };

}
