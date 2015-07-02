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
package com.constellio.app.modules.rm.reports.model.administration.plan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_AdministrativeUnit;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel.ConservationRulesReportModel_Copy;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel.ConservationRulesReportModel_Rule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

public class ConservationRulesReportPresenter {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ConservationRulesReportPresenter.class);
	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private RMSchemasRecordsServices rm;
	private SearchServices searchServices;
	private boolean byAdministrativeUnit;
	private DecommissioningService decommissioningService;

	public ConservationRulesReportPresenter(String collection, ModelLayerFactory modelLayerFactory) {
		this(collection, modelLayerFactory, false);
	}

	public ConservationRulesReportPresenter(String collection, ModelLayerFactory modelLayerFactory,
			boolean byAdministrativeUnit) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		searchServices = modelLayerFactory.newSearchServices();
		decommissioningService = new DecommissioningService(collection, modelLayerFactory);
		rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.byAdministrativeUnit = byAdministrativeUnit;
	}

	public ConservationRulesReportModel build() {
		ConservationRulesReportModel model = new ConservationRulesReportModel();

		if (byAdministrativeUnit) {
			Map<AdministrativeUnit, List<ConservationRulesReportModel_Rule>> conservationRulesModelByAdministrativeUnit = new HashMap<>();
			Map<AdministrativeUnit, List<RetentionRule>> retentionRulesByAdministrativeUnit = getRetentionRulesByAdministrativeUnit();
			for (Entry<AdministrativeUnit, List<RetentionRule>> entry : retentionRulesByAdministrativeUnit.entrySet()) {
				List<ConservationRulesReportModel_Rule> reportModelRules = convertRulesToModelRules(entry.getValue());
				conservationRulesModelByAdministrativeUnit.put(entry.getKey(), reportModelRules);
			}
			model.setRulesByAdministrativeUnit(conservationRulesModelByAdministrativeUnit);
			model.setByAdministrativeUnit(true);
		} else {
			List<ConservationRulesReportModel_Rule> rules = getAndConvertRulesToModelRules();

			model.setRules(rules);
		}

		return model;
	}

	private List<ConservationRulesReportModel_Rule> getAndConvertRulesToModelRules() {
		List<ConservationRulesReportModel_Rule> conservationRulesReportModel_Rules = new ArrayList<>();
		List<RetentionRule> retentionRules = getRetentionRules();

		if (retentionRules != null) {
			conservationRulesReportModel_Rules = convertRulesToModelRules(retentionRules);
		}

		return conservationRulesReportModel_Rules;
	}

	private List<ConservationRulesReportModel_Rule> convertRulesToModelRules(List<RetentionRule> retentionRules) {
		List<ConservationRulesReportModel_Rule> conservationRulesReportModel_Rules = new ArrayList<>();
		for (RetentionRule retentionRule : retentionRules) {
			if (retentionRule != null) {
				try {
					ConservationRulesReportModel_Rule conservationRulesReportModel_Rule = new ConservationRulesReportModel_Rule();

					String code = StringUtils.defaultString(retentionRule.getCode());
					String title = StringUtils.defaultString(retentionRule.getTitle());
					String description = StringUtils.defaultString(retentionRule.getDescription());

					conservationRulesReportModel_Rule.setRuleNumber(code);
					conservationRulesReportModel_Rule.setTitle(title);
					conservationRulesReportModel_Rule.setDescription(description);

					conservationRulesReportModel_Rule.setPrincipalsHolders(getPrincipalsHolders(retentionRule));
					conservationRulesReportModel_Rule.setPrincipalsCopies(getPrincipalCopies(retentionRule));

					conservationRulesReportModel_Rule.setSecondaryCopy(getSecondaryCopy(retentionRule));

					conservationRulesReportModel_Rules.add(conservationRulesReportModel_Rule);
				} catch (Exception e) {
					LOGGER.info(e.getMessage());
				}
			}
		}
		return conservationRulesReportModel_Rules;
	}

	private List<RetentionRule> getRetentionRules() {
		MetadataSchemaType retentionRuleSchemaType = rm.retentionRuleSchemaType();

		LogicalSearchQuery allRetentionRules = new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(retentionRuleSchemaType).returnAll())
				.sortAsc(Schemas.CODE);

		List<RetentionRule> retentionRules = rm.wrapRetentionRules(searchServices
				.search(allRetentionRules));

		if (retentionRules == null) {
			retentionRules = new ArrayList<>();
		}

		return retentionRules;

	}

	private Map<AdministrativeUnit, List<RetentionRule>> getRetentionRulesByAdministrativeUnit() {

		MetadataSchemaType administrativeUnitSchemaType = rm.administrativeUnitSchemaType();
		Map<AdministrativeUnit, List<RetentionRule>> retentionRulesByAdministrativeUnit = new HashMap<>();

		MetadataSchemaType retentionRuleSchemaType = rm.retentionRuleSchemaType();

		LogicalSearchQuery alladministrativesUnits = new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(administrativeUnitSchemaType).returnAll())
				.sortAsc(Schemas.CODE);

		List<AdministrativeUnit> administrativeUnits = rm.wrapAdministrativeUnits(searchServices
				.search(alladministrativesUnits));

		if (administrativeUnits != null) {
			for (AdministrativeUnit administrativeUnit : administrativeUnits) {
				List<RetentionRule> newRetentionRules = new ArrayList<>();

				LogicalSearchQuery retentionRulesQuery = new LogicalSearchQuery()
						.setCondition(LogicalSearchQueryOperators.from(retentionRuleSchemaType)
								.where(rm.retentionRuleAdministrativeUnitsId())
								.isContaining(Arrays.asList(administrativeUnit.getId()))).sortAsc(Schemas.CODE);

				List<RetentionRule> retentionRules = rm.wrapRetentionRules(searchServices
						.search(retentionRulesQuery));

				for (RetentionRule retentionRule : retentionRules) {
					if (!retentionRule.isResponsibleAdministrativeUnits()) {
						newRetentionRules.add(retentionRule);
					}
				}
				if (!newRetentionRules.isEmpty()) {
					retentionRulesByAdministrativeUnit.put(administrativeUnit, newRetentionRules);
				}
			}
		}
		return retentionRulesByAdministrativeUnit;
	}

	private Map<String, String> getPrincipalsHolders(RetentionRule rule) {
		Map<String, String> principalsHolders = new HashMap<String, String>();

		Map<String, AdministrativeUnitReportModel_AdministrativeUnit> administrativeUnits = getAdministrativeUnitsMapModel();

		if (administrativeUnits != null) {
			if (rule != null) {
				List<String> administrativeUnitIds = rule.getAdministrativeUnits();
				if (administrativeUnitIds != null) {
					for (String administrativeUnitId : administrativeUnitIds) {
						if (administrativeUnitId != null && !administrativeUnitId.isEmpty()) {
							AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit = administrativeUnits
									.get(administrativeUnitId);
							if (administrativeUnit != null) {
								String code = StringUtils.defaultString(administrativeUnit.getCode());
								if (code != null && !code.isEmpty()) {
									String label = StringUtils.defaultString(administrativeUnit.getLabel());
									principalsHolders.put(code, label);
								}
							}
						}
					}
				}
			}
		}

		return principalsHolders;
	}

	private Map<String, AdministrativeUnitReportModel_AdministrativeUnit> getAdministrativeUnitsMapModel() {
		Map<String, AdministrativeUnitReportModel_AdministrativeUnit> mappedUnits = new HashMap<>();

		List<AdministrativeUnitReportModel_AdministrativeUnit> modelAdministrativeUnits = getModelAdministrativeUnits();

		if (modelAdministrativeUnits != null) {
			for (AdministrativeUnitReportModel_AdministrativeUnit administrativeUnit : modelAdministrativeUnits) {
				if (administrativeUnit != null) {
					String unitId = administrativeUnit.getUnitId();
					if (unitId != null && !unitId.isEmpty()) {
						mappedUnits.put(unitId, administrativeUnit);
						List<AdministrativeUnitReportModel_AdministrativeUnit> childAdministrativeUnits = administrativeUnit
								.getChildAdministrativeUnits();
						if (childAdministrativeUnits != null) {
							recursive(childAdministrativeUnits, mappedUnits);
						}
					}
				}
			}
		}

		return mappedUnits;
	}

	private List<AdministrativeUnitReportModel_AdministrativeUnit> getModelAdministrativeUnits() {

		AdministrativeUnitReportPresenter unitPresenter = new AdministrativeUnitReportPresenter(collection,
				modelLayerFactory);

		AdministrativeUnitReportModel administrativeUnitModel = unitPresenter.build();

		List<AdministrativeUnitReportModel_AdministrativeUnit> modelAdministrativeUnits = null;
		if (administrativeUnitModel != null) {
			modelAdministrativeUnits = administrativeUnitModel.getAdministrativeUnits();
		}

		if (modelAdministrativeUnits == null) {
			modelAdministrativeUnits = new ArrayList<>();
		}
		return modelAdministrativeUnits;

	}

	private void recursive(List<AdministrativeUnitReportModel_AdministrativeUnit> adminUnits,
			Map<String, AdministrativeUnitReportModel_AdministrativeUnit> mappedUnits) {

		if (adminUnits != null) {
			for (AdministrativeUnitReportModel_AdministrativeUnit adminUnit : adminUnits) {
				if (adminUnit != null) {
					String unitId = adminUnit.getUnitId();
					if (unitId != null && !unitId.isEmpty()) {
						mappedUnits.put(unitId, adminUnit);
						List<AdministrativeUnitReportModel_AdministrativeUnit> childAdministrativeUnits = adminUnit
								.getChildAdministrativeUnits();
						if (childAdministrativeUnits != null) {
							recursive(childAdministrativeUnits, mappedUnits);
						}
					}
				}
			}
		}
	}

	private List<ConservationRulesReportModel_Copy> getPrincipalCopies(RetentionRule rule) {
		List<ConservationRulesReportModel_Copy> modelCopies = new ArrayList<>();

		if (rule != null) {
			List<CopyRetentionRule> copies = rule.getPrincipalCopies();

			Map<String, String> commentMap = buildCommentMap(rule);

			for (CopyRetentionRule copyRetentionRule : copies) {
				if (copyRetentionRule != null) {
					ConservationRulesReportModel_Copy conservationRulesReportModel_copy = new ConservationRulesReportModel_Copy();

					String observations = StringUtils.defaultString(buildObservations(copyRetentionRule, commentMap));
					conservationRulesReportModel_copy.setObservations(observations);
					//
					boolean principal = copyRetentionRule.getCopyType() == CopyType.PRINCIPAL ? true : false;
					conservationRulesReportModel_copy.setPrincipal(principal);

					//
					conservationRulesReportModel_copy.setSupportTypes(getMediumTypesCodesOf(copyRetentionRule));

					//
					RetentionPeriod activeRetentionPeriod = copyRetentionRule.getActiveRetentionPeriod();
					String activeRetentionPeriodValue = "";
					if (activeRetentionPeriod != null) {
						activeRetentionPeriodValue = StringUtils.defaultString(activeRetentionPeriod.toString());
					}
					conservationRulesReportModel_copy.setActive(activeRetentionPeriodValue);

					//
					RetentionPeriod semiActiveRetentionPeriod = copyRetentionRule.getSemiActiveRetentionPeriod();
					String semiActiveRetentionPeriodValue = "";
					if (semiActiveRetentionPeriod != null) {
						semiActiveRetentionPeriodValue = StringUtils
								.defaultString(semiActiveRetentionPeriod.toString());
					}
					conservationRulesReportModel_copy.setSemiActive(semiActiveRetentionPeriodValue);

					//
					DisposalType inactiveDisposalType = copyRetentionRule.getInactiveDisposalType();
					String inactiveDisposalTypeCode = "";
					if (inactiveDisposalType != null) {
						inactiveDisposalTypeCode = StringUtils.defaultString(inactiveDisposalType.getCode());
					}
					conservationRulesReportModel_copy.setInactive(inactiveDisposalTypeCode);

					modelCopies.add(conservationRulesReportModel_copy);
				}
			}
		}

		return modelCopies;
	}

	private List<String> getMediumTypesCodesOf(CopyRetentionRule copyRetentionRule) {
		List<String> mediumTypeIds = copyRetentionRule.getMediumTypeIds();
		if (mediumTypeIds == null) {
			mediumTypeIds = new ArrayList<>();
		}

		List<String> codes = new ArrayList<>();
		for (MediumType mediumType : rm.getMediumTypes(mediumTypeIds)) {
			codes.add(mediumType.getCode());
		}
		return codes;
	}

	private Map<String, String> buildCommentMap(RetentionRule rule) {
		Map<String, String> map = new HashMap<>();

		if (rule != null) {

			for (String copyRulesCommentLine : rule.getCopyRulesComment()) {
				String code = StringUtils.substringBefore(copyRulesCommentLine, ":");
				code = StringUtils.trim(code);
				String value = StringUtils.substringAfter(copyRulesCommentLine, ":");
				value = StringUtils.trim(value);

				if (!code.isEmpty()) {
					map.put(code, value);
				}
			}
		}

		return map;
	}

	private String buildObservations(CopyRetentionRule copyRetentionRule, Map<String, String> commentMap) {

		StringBuilder observationsBuilder = new StringBuilder();

		if (copyRetentionRule != null && commentMap != null) {
			appendObservation(observationsBuilder, copyRetentionRule.getContentTypesComment(), commentMap, "Supports");
			appendObservation(observationsBuilder, copyRetentionRule.getActiveRetentionComment(), commentMap, "Actif");
			appendObservation(observationsBuilder, copyRetentionRule.getSemiActiveRetentionComment(), commentMap,
					"Semi-actif");
			appendObservation(observationsBuilder, copyRetentionRule.getInactiveDisposalComment(), commentMap,
					"Inactif");
		}

		String observations = StringUtils.removeEnd(observationsBuilder.toString(), "\n");
		observations = StringUtils.defaultString(observations);
		return observations;
	}

	private void appendObservation(StringBuilder builder, String commentCode, Map<String, String> commentMap,
			String label) {
		String observation = "";
		if (StringUtils.isNotBlank(commentCode)) {
			if (commentMap != null) {
				String comment = commentMap.get(commentCode);
				if (StringUtils.isNotBlank(comment)) {
					observation += label + ": " + comment;
				}
			}
		}
		if (StringUtils.isNotBlank(observation)) {
			builder.append(observation + "\n");
		}
	}

	private ConservationRulesReportModel_Copy getSecondaryCopy(RetentionRule rule) {
		ConservationRulesReportModel_Copy conservationRulesReportModel_copy = new ConservationRulesReportModel_Copy();

		Map<String, String> commentMap = new HashMap<>();
		String observations = "";
		boolean principal = false;
		List<String> mediumTypeIds = new ArrayList<>();
		String activeRetentionPeriodValue = "";
		String semiActiveRetentionPeriodValue = "";
		String inactiveDisposalTypeCode = "";

		if (rule != null) {
			CopyRetentionRule copyRetentionRule = rule.getSecondaryCopy();

			commentMap = buildCommentMap(rule);

			observations = buildObservations(copyRetentionRule, commentMap);

			principal = copyRetentionRule.getCopyType() == CopyType.PRINCIPAL ? true : false;

			mediumTypeIds = getMediumTypesCodesOf(copyRetentionRule);

			RetentionPeriod activeRetentionPeriod = copyRetentionRule.getActiveRetentionPeriod();
			if (activeRetentionPeriod != null) {
				activeRetentionPeriodValue = StringUtils.defaultString(activeRetentionPeriod.toString());
			}

			RetentionPeriod semiActiveRetentionPeriod = copyRetentionRule.getSemiActiveRetentionPeriod();
			if (semiActiveRetentionPeriod != null) {
				semiActiveRetentionPeriodValue = StringUtils.defaultString(semiActiveRetentionPeriod.toString());
			}

			DisposalType inactiveDisposalType = copyRetentionRule.getInactiveDisposalType();
			if (inactiveDisposalType != null) {
				inactiveDisposalTypeCode = StringUtils.defaultString(inactiveDisposalType.getCode());
			}
		}

		conservationRulesReportModel_copy.setObservations(observations);
		conservationRulesReportModel_copy.setPrincipal(principal);
		conservationRulesReportModel_copy.setSupportTypes(mediumTypeIds);
		conservationRulesReportModel_copy.setActive(activeRetentionPeriodValue);
		conservationRulesReportModel_copy.setSemiActive(semiActiveRetentionPeriodValue);
		conservationRulesReportModel_copy.setInactive(inactiveDisposalTypeCode);

		return conservationRulesReportModel_copy;
	}

	public FoldersLocator getFoldersLocator() {
		return modelLayerFactory.getFoldersLocator();
	}
}