package com.constellio.app.modules.rm.reports.model.administration.plan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;

public class ConservationRulesReportModel {

	List<ConservationRulesReportModel_Rule> rules = new ArrayList<ConservationRulesReportModel_Rule>();

	Map<AdministrativeUnit, List<ConservationRulesReportModel_Rule>> adminUnitRulesMap = new HashMap();

	boolean byAdministrativeUnit;

	public void setByAdministrativeUnit(boolean byAdministrativeUnit) {
		this.byAdministrativeUnit = byAdministrativeUnit;
	}

	public boolean isByAdministrativeUnit() {
		return byAdministrativeUnit;
	}

	public List<ConservationRulesReportModel_Rule> getRules() {
		return rules;
	}

	public void setRules(List<ConservationRulesReportModel_Rule> rules) {
		this.rules = rules;
	}

	public void setRulesByAdministrativeUnit(
			Map<AdministrativeUnit, List<ConservationRulesReportModel_Rule>> rulesByAdministrativeUnit) {
		this.adminUnitRulesMap = rulesByAdministrativeUnit;
	}

	public Map<AdministrativeUnit, List<ConservationRulesReportModel_Rule>> getRulesByAdministrativeUnitMap() {
		return adminUnitRulesMap;
	}

	public String getTitle() {
		return "Liste des règles de conservation";
	}

	public static class ConservationRulesReportModel_Rule {

		String ruleNumber;

		String title;

		String description;

		Map<String, String> administrativeUnits;

		List<ConservationRulesReportModel_Copy> principalsCopies;

		ConservationRulesReportModel_Copy secondaryCopy;

		public String getRuleNumber() {
			return ruleNumber;
		}

		public void setRuleNumber(String ruleNumber) {
			this.ruleNumber = ruleNumber;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public Map<String, String> getAdministrativeUnits() {
			return administrativeUnits;
		}

		public void setPrincipalsHolders(Map<String, String> administrativeUnits) {
			this.administrativeUnits = administrativeUnits;
		}

		public List<ConservationRulesReportModel_Copy> getPrincipalsCopies() {
			return principalsCopies;
		}

		public void setPrincipalsCopies(List<ConservationRulesReportModel_Copy> principalsCopies) {
			this.principalsCopies = principalsCopies;
		}

		public ConservationRulesReportModel_Copy getSecondaryCopy() {
			return secondaryCopy;
		}

		public void setSecondaryCopy(ConservationRulesReportModel_Copy secondaryCopy) {
			this.secondaryCopy = secondaryCopy;
		}
	}

	public static class ConservationRulesReportModel_Copy {
		boolean principal;
		List<String> SupportTypes;
		String active;
		String inactive;
		String semiActive;
		String observations;

		public boolean isPrincipal() {
			return principal;
		}

		public void setPrincipal(boolean principal) {
			this.principal = principal;
		}

		public List<String> getSupportTypes() {
			return SupportTypes;
		}

		public void setSupportTypes(List<String> supportTypes) {
			SupportTypes = supportTypes;
		}

		public String getActive() {
			return active;
		}

		public void setActive(String active) {
			this.active = active;
		}

		public String getInactive() {
			return inactive;
		}

		public void setInactive(String inactive) {
			this.inactive = inactive;
		}

		public String getSemiActive() {
			return semiActive;
		}

		public void setSemiActive(String semiActive) {
			this.semiActive = semiActive;
		}

		public String getObservations() {
			return observations;
		}

		public void setObservations(String observations) {
			this.observations = observations;
		}
	}
}
