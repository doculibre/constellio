/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm;

import static com.constellio.app.modules.rm.ConstellioRMModule.ID;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.model.enums.DecommissioningDateBasedOn;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.services.configs.SystemConfigurationsManager;

public class RMConfigs {

	public static enum DecommissioningPhase {
		NEVER, ON_DEPOSIT, ON_TRANSFER_OR_DEPOSIT
	}

	static List<SystemConfiguration> configurations = new ArrayList<>();

	//Retention calendar configs
	public static final SystemConfiguration CALCULATED_CLOSING_DATE,
			DECOMMISSIONING_DATE_BASED_ON,
			CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE,
			CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE,
			YEAR_END_DATE, REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_WEEK,
			CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLEPERIOD,
			CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE,
			COPY_RULE_TYPE_ALWAYS_MODIFIABLE,
			MINOR_VERSIONS_PURGED_ON,
			ALSO_PURGE_CURRENT_VERSION_IF_MINOR,
			PDFA_CREATED_ON;

	// Category configs
	public static final SystemConfiguration LINKABLE_CATEGORY_MUST_NOT_BE_ROOT, LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES;

	static {
		SystemConfigurationGroup decommissioning = new SystemConfigurationGroup(ID, "decommissioning");

		//Date fermeture calculée
		add(CALCULATED_CLOSING_DATE = decommissioning
				.createBooleanTrueByDefault("calculatedCloseDate"));

		//Nombre d'années avant la fermeture pour un délai à durée fixe (-1 si même nombre d'années que le délai actif)
		add(CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_FIXED_RULE = decommissioning
				.createInteger("calculatedCloseDateNumberOfYearWhenFixedRule")
				.withDefaultValue(-1));

		//Nombre d'années avant la fermeture pour un délai à durée ouverte (-1 si pas de calcul automatisé)
		add(CALCULATED_CLOSING_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE = decommissioning
				.createInteger("calculatedCloseDateNumberOfYearWhenVariableRule")
				.withDefaultValue(1));

		//Nombre d'années avant le transfert au semi-actif pour un délai à durée ouverte
		// (-1 si pas de calcul automatisé)
		add(CALCULATED_SEMIACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLEPERIOD = decommissioning
				.createInteger("calculatedSemiActiveDateNumberOfYearWhenOpenRule")
				.withDefaultValue(1));

		//Nombre d'années avant la disposition (versement/destruction) pour un délai semi-actif à durée ouverte
		// (-1 si pas de calcul automatisé)
		add(CALCULATED_INACTIVE_DATE_NUMBER_OF_YEAR_WHEN_VARIABLE_RULE = decommissioning
				.createInteger("calculatedInactiveDateNumberOfYearWhenOpenRule")
				.withDefaultValue(1));

		//Délais calculés à partir de la date d'ouverture (date de fermeture sinon)
		add(DECOMMISSIONING_DATE_BASED_ON = decommissioning
				.createEnum("decommissioningDateBasedOn", DecommissioningDateBasedOn.class)
				.withDefaultValue(DecommissioningDateBasedOn.CLOSE_DATE));

		//Date de fin d'année à utiliser (civique ou financière) pour le calcul des délais (MM/JJ)
		add(YEAR_END_DATE = decommissioning
				.createString("yearEndDate")
				.withDefaultValue("12/31"));

		//Nombre de jours devant précéder la date de fin d'année pour que celle-ci soit considérée dans le calcul des délais pour l'année en cours
		add(REQUIRED_DAYS_BEFORE_YEAR_END_FOR_NOT_ADDING_A_WEEK = decommissioning
				.createInteger("closeDateRequiredDaysBeforeYearEnd")
				.withDefaultValue(90));

		add(COPY_RULE_TYPE_ALWAYS_MODIFIABLE = decommissioning
				.createBooleanFalseByDefault("copyRuleTypeAlwaysModifiable"));

		add(MINOR_VERSIONS_PURGED_ON = decommissioning
				.createEnum("minorVersionsPurgedOn", DecommissioningPhase.class)
				.withDefaultValue(DecommissioningPhase.NEVER));

		// Only applies when MINOR_VERSIONS_PURGED_ON != NEVER
		add(ALSO_PURGE_CURRENT_VERSION_IF_MINOR = decommissioning.createBooleanFalseByDefault("alsoPurgeCurrentVersionIfMinor"));

		add(PDFA_CREATED_ON = decommissioning
				.createEnum("PDFACreatedOn", DecommissioningPhase.class)
				.withDefaultValue(DecommissioningPhase.NEVER));

		// Considérer ou non la position à la racine de la category dans le calculateur
		add(LINKABLE_CATEGORY_MUST_NOT_BE_ROOT = decommissioning.createBooleanTrueByDefault("linkableCategoryMustNotBeRoot"));

		// Considérer ou non le statut "approuvé" de la category dans le calculateur
		add(LINKABLE_CATEGORY_MUST_HAVE_APPROVED_RULES = decommissioning
				.createBooleanFalseByDefault("linkableCategoryMustHaveApprovedRules"));
	}

	static void add(SystemConfiguration configuration) {
		configurations.add(configuration);
	}

	SystemConfigurationsManager manager;

	public RMConfigs(SystemConfigurationsManager manager) {
		this.manager = manager;
	}

	public boolean isCopyRuleTypeAlwaysModifiable() {
		return manager.getValue(COPY_RULE_TYPE_ALWAYS_MODIFIABLE);
	}

	public boolean purgeMinorVersionsOnTransfer() {
		return manager.getValue(MINOR_VERSIONS_PURGED_ON) == DecommissioningPhase.ON_TRANSFER_OR_DEPOSIT;
	}

	public boolean purgeMinorVersionsOnDeposit() {
		return manager.getValue(MINOR_VERSIONS_PURGED_ON) == DecommissioningPhase.ON_DEPOSIT;
	}

	public boolean purgeCurrentVersionIfMinor() {
		return manager.getValue(ALSO_PURGE_CURRENT_VERSION_IF_MINOR);
	}

	public boolean createPDFaOnTransfer() {
		return manager.getValue(PDFA_CREATED_ON) == DecommissioningPhase.ON_TRANSFER_OR_DEPOSIT;
	}

	public boolean createPDFaOnDeposit() {
		return manager.getValue(PDFA_CREATED_ON) == DecommissioningPhase.ON_DEPOSIT;
	}
}
