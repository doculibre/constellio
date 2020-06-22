package com.constellio.app.modules.rm.reports.model.administration.plan;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel.ConservationRulesReportModel_Copy;
import com.constellio.app.modules.rm.reports.model.administration.plan.ConservationRulesReportModel.ConservationRulesReportModel_Rule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class ConservationRulesReportPresenter {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ConservationRulesReportPresenter.class);
	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private AppLayerFactory appLayerFactory;
	private RMSchemasRecordsServices rm;
	private SearchServices searchServices;
	private boolean byAdministrativeUnit;
	private DecommissioningService decommissioningService;
	private String administrativeUnitId;
	private List<String> rulesToIncludes = null;

	public ConservationRulesReportPresenter(String collection, AppLayerFactory appLayerFactory, Locale locale) {
		this(collection, appLayerFactory, false, null, locale, null);
	}

	public ConservationRulesReportPresenter(String collection, AppLayerFactory appLayerFactory,
											boolean byAdministrativeUnit, Locale locale) {
		this(collection, appLayerFactory, byAdministrativeUnit, null, locale, null);
	}

	public ConservationRulesReportPresenter(String collection, AppLayerFactory appLayerFactory,
											boolean byAdministrativeUnit, String administrativeUnitId, Locale locale,
											List<String> rulesToIncludes) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		searchServices = modelLayerFactory.newSearchServices();
		decommissioningService = new DecommissioningService(collection, appLayerFactory);
		rm = new RMSchemasRecordsServices(collection, appLayerFactory, locale);
		this.byAdministrativeUnit = byAdministrativeUnit;
		this.administrativeUnitId = administrativeUnitId;
		this.rulesToIncludes = rulesToIncludes;
	}

	public ConservationRulesReportModel build() {
		ConservationRulesReportModel model = new ConservationRulesReportModel();

		if (byAdministrativeUnit) {
			Map<AdministrativeUnit, List<ConservationRulesReportModel_Rule>> conservationRulesModelByAdministrativeUnit = new HashMap<>();
			Map<AdministrativeUnit, List<RetentionRule>> retentionRulesByAdministrativeUnit;
			if (StringUtils.isNotBlank(administrativeUnitId)) {
				retentionRulesByAdministrativeUnit = getRetentionRulesByAdministrativeUnit(administrativeUnitId);
			} else {
				retentionRulesByAdministrativeUnit = getRetentionRulesByAdministrativeUnit();
			}
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

	//
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
					String juridicReference = StringUtils.defaultString(retentionRule.getJuridicReference());

					conservationRulesReportModel_Rule.setRuleNumber(code);
					conservationRulesReportModel_Rule.setTitle(title);
					conservationRulesReportModel_Rule.setDescription(description);
					conservationRulesReportModel_Rule.setJuridicReference(juridicReference);

					conservationRulesReportModel_Rule.setPrincipalsHolders(getPrincipalsHolders(retentionRule));
					conservationRulesReportModel_Rule.setPrincipalsCopies(getPrincipalCopies(retentionRule));

					conservationRulesReportModel_Rule.setSecondaryCopy(getSecondaryCopy(retentionRule));

					conservationRulesReportModel_Rules.add(conservationRulesReportModel_Rule);
				} catch (Exception e) {
					LOGGER.error("Error while converting retention rule " + retentionRule.getCode(), e);
				}
			}
		}
		return conservationRulesReportModel_Rules;
	}

	private List<RetentionRule> getRetentionRules() {
		MetadataSchemaType retentionRuleSchemaType = rm.retentionRule.schemaType();

		LogicalSearchQuery allRetentionRules = new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(retentionRuleSchemaType).returnAll())
				.sortAsc(Schemas.CODE);

		if(rulesToIncludes != null && !rulesToIncludes.isEmpty()) {
			allRetentionRules.setCondition(allRetentionRules.getCondition().andWhere(Schemas.IDENTIFIER).isIn(rulesToIncludes));
		}

		List<RetentionRule> retentionRules = rm.wrapRetentionRules(searchServices
				.search(allRetentionRules));

		if (retentionRules == null) {
			retentionRules = new ArrayList<>();
		}

		return retentionRules;

	}

	private Map<AdministrativeUnit, List<RetentionRule>> getRetentionRulesByAdministrativeUnit(
			String administrativeUnitId) {

		Map<AdministrativeUnit, List<RetentionRule>> retentionRulesByAdministrativeUnit = new HashMap<>();
		MetadataSchemaType retentionRuleSchemaType = rm.retentionRule.schemaType();
		AdministrativeUnit administrativeUnit = rm.getAdministrativeUnit(administrativeUnitId);

		List<RetentionRule> newRetentionRules = getRetentionRulesByAdministrativeUnit(administrativeUnit,
				retentionRuleSchemaType);
		if (!newRetentionRules.isEmpty()) {
			retentionRulesByAdministrativeUnit.put(administrativeUnit, newRetentionRules);
		}
		return retentionRulesByAdministrativeUnit;
	}

	private List<RetentionRule> getRetentionRulesByAdministrativeUnit(AdministrativeUnit administrativeUnit,
																	  MetadataSchemaType retentionRuleSchemaType) {
		//List<RetentionRule> newRetentionRules = new ArrayList<>();

		LogicalSearchQuery retentionRulesQuery = new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(retentionRuleSchemaType)
						.where(rm.retentionRule.administrativeUnits())
						.isContaining(Arrays.asList(administrativeUnit.getId()))).sortAsc(Schemas.CODE);

		List<RetentionRule> retentionRules = rm.wrapRetentionRules(searchServices
				.search(retentionRulesQuery));

		/*for (RetentionRule retentionRule : retentionRules) {
			if (!retentionRule.isResponsibleAdministrativeUnits()) {
				newRetentionRules.add(retentionRule);
			}
		}*/
		return retentionRules;//newRetentionRules;
	}

	private Map<AdministrativeUnit, List<RetentionRule>> getRetentionRulesByAdministrativeUnit() {

		MetadataSchemaType administrativeUnitSchemaType = rm.administrativeUnit.schemaType();
		Map<AdministrativeUnit, List<RetentionRule>> retentionRulesByAdministrativeUnit = new HashMap<>();

		MetadataSchemaType retentionRuleSchemaType = rm.retentionRule.schemaType();

		LogicalSearchQuery alladministrativesUnits = new LogicalSearchQuery()
				.setCondition(LogicalSearchQueryOperators.from(administrativeUnitSchemaType).returnAll())
				.sortAsc(Schemas.CODE);

		List<AdministrativeUnit> administrativeUnits = rm.wrapAdministrativeUnits(searchServices
				.search(alladministrativesUnits));

		if (administrativeUnits != null) {
			for (AdministrativeUnit administrativeUnit : administrativeUnits) {
				List<RetentionRule> newRetentionRules = getRetentionRulesByAdministrativeUnit(administrativeUnit,
						retentionRuleSchemaType);
				if (!newRetentionRules.isEmpty()) {
					retentionRulesByAdministrativeUnit.put(administrativeUnit, newRetentionRules);
				}
			}
		}
		return retentionRulesByAdministrativeUnit;
	}

	private Map<String, String> getPrincipalsHolders(RetentionRule rule) {
		Map<String, String> principalsHolders = new HashMap<String, String>();

		if (rule != null) {
			List<String> administrativeUnitIds = rule.getAdministrativeUnits();
			if (administrativeUnitIds != null) {
				for (String administrativeUnitId : administrativeUnitIds) {
					if (administrativeUnitId != null && !administrativeUnitId.isEmpty()) {
						AdministrativeUnit administrativeUnit = rm.getAdministrativeUnit(administrativeUnitId);

						if (administrativeUnit != null) {
							String code = StringUtils.defaultString(administrativeUnit.getCode());
							if (code != null && !code.isEmpty()) {
								String label = StringUtils.defaultString(administrativeUnit.getTitle());
								principalsHolders.put(code, label);
							}
						}
					}
				}
			}

		}

		return principalsHolders;
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

		for (String mediumTypeId : mediumTypeIds) {
			try {
				codes.add(rm.getMediumType(mediumTypeId).getCode());
			} catch (Exception e) {
				if (rm.getMediumTypeByCode(mediumTypeId) != null) {
					codes.add(mediumTypeId);
				}
			}
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

			if(copyRetentionRule == null) {
				return null;
			}

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