package com.constellio.app.modules.rm.model.calculators;

import static org.joda.time.Days.daysBetween;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.model.entities.calculators.CalculatorLogger;

public class CalculatorUtils {

	public static LocalDate toNextEndOfYearDateIfNotAlready(LocalDate date, String yearEndStr, int requiredDaysBeforeYearEnd) {
		if (isEndOfYear(date, yearEndStr)) {
			return date;
		} else {
			return toNextEndOfYearDate(date, yearEndStr, requiredDaysBeforeYearEnd, false);
		}
	}

	public static LocalDate toEndOfYear(int year, String yearEndStr) {
		int indexOfSep = yearEndStr.indexOf("/");
		int yearEndMonth = Integer.parseInt(yearEndStr.substring(0, indexOfSep));
		int yearEndDay = Integer.parseInt(yearEndStr.substring(indexOfSep + 1));
		return new LocalDate(year, yearEndMonth, yearEndDay);
	}

	static LocalDate toNextEndOfYearDate(LocalDate date, String yearEndStr, int requiredDaysBeforeYearEnd,
			boolean addDayIfEndOfYear) {
		if (date == null) {
			return null;
		}
		LocalDate yearEndDate;

		yearEndDate = toEndOfYear(date.getYear(), yearEndStr);

		if (yearEndDate.isBefore(date)) {
			yearEndDate = yearEndDate.plusYears(1);
		}

		int daysBetweenDateAndYearEndDate = daysBetween(date, yearEndDate).getDays();

		if (!addDayIfEndOfYear && daysBetweenDateAndYearEndDate == 0) {
			//Nothing

		} else if (daysBetweenDateAndYearEndDate < requiredDaysBeforeYearEnd) {
			yearEndDate = yearEndDate.plusYears(1);
		}
		return yearEndDate;
	}

	public static LocalDate getSmallestDate(List<LocalDate> dates) {

		LocalDate smallest = null;
		for (LocalDate date : dates) {
			if (smallest == null || (date != null && date.isBefore(smallest))) {
				smallest = date;
			}
		}

		return smallest;
	}

	public static boolean isEndOfYear(LocalDate date, String yearEndStr) {
		if (date == null) {
			return false;
		}
		int indexOfSep = yearEndStr.indexOf("/");
		int yearEndMonth = Integer.parseInt(yearEndStr.substring(0, indexOfSep));
		int yearEndDay = Integer.parseInt(yearEndStr.substring(indexOfSep + 1));
		return date.getDayOfMonth() == yearEndDay && date.getMonthOfYear() == yearEndMonth;
	}

	public static LocalDate calculateExpectedTransferDate(CopyRetentionRule copyRule, LocalDate adjustedDecommissioningDate,
			int defaultNumberOfYearWhenVariableDelay, CalculatorLogger logger) {

		if (adjustedDecommissioningDate == null) {
			if (logger.isTroubleshooting()) {
				logger.log("Aucune date ne peut être utilisée pour le calcul de la date de transfert prévue");
			}
			return null;
		}

		if (copyRule.getSemiActiveRetentionPeriod().isZero()) {
			if (logger.isTroubleshooting()) {
				logger.log("La délai semi-actif étant de 0, la date de transfert prévue n'est pas calculée");
			}
			return null;
		} else if (copyRule.getActiveRetentionPeriod().isVariablePeriod()) {

			int numberOfYearWhenVariableDelay = defaultNumberOfYearWhenVariableDelay;

			if (copyRule.getOpenActiveRetentionPeriod() != null && copyRule.getOpenActiveRetentionPeriod() >= 0) {
				if (defaultNumberOfYearWhenVariableDelay == -1) {
					if (logger.isTroubleshooting()) {
						logger.log("La configuration précise qu'il ne faut pas calculer la date de transfert lorsque le délai"
								+ " est ouvert, mais le délai précise qu'il faut ajouter "
								+ copyRule.getOpenActiveRetentionPeriod() + " année(s), alors c'est ce qui est fait");
					}
					numberOfYearWhenVariableDelay = copyRule.getOpenActiveRetentionPeriod();

				} else {

					numberOfYearWhenVariableDelay =
							defaultNumberOfYearWhenVariableDelay + copyRule.getOpenActiveRetentionPeriod();

					if (logger.isTroubleshooting()) {
						logger.log(numberOfYearWhenVariableDelay + " année(s) sont ajoutées (" +
								defaultNumberOfYearWhenVariableDelay + " à cause de la configuration par défaut, " +
								copyRule.getOpenActiveRetentionPeriod() + " à cause de la configuration du délai");
					}
				}
			}

			if (numberOfYearWhenVariableDelay == -1) {
				if (logger.isTroubleshooting()) {
					logger.log(
							"Le calcul de la date de transfert est désactivé, car le délai est ouvert (et la configuration précise que la date ne doit pas être calculée)");
				}
				return null;
			} else {
				LocalDate returnedDate = adjustedDecommissioningDate.plusYears(numberOfYearWhenVariableDelay);
				if (logger.isTroubleshooting()) {
					logger.log("C'est un délai ouvert, on ajoute " + numberOfYearWhenVariableDelay + " année(s) : "
							+ adjustedDecommissioningDate + " => " + returnedDate);
				}
				return adjustedDecommissioningDate.plusYears(numberOfYearWhenVariableDelay);
			}
		} else {
			int fixedPeriod = copyRule.getActiveRetentionPeriod().getFixedPeriod();
			LocalDate returnedDate = adjustedDecommissioningDate.plusYears(fixedPeriod);
			if (logger.isTroubleshooting()) {
				logger.log("C'est un délai fixe, on ajoute " + fixedPeriod + " année(s) : " + adjustedDecommissioningDate
						+ " => " + returnedDate);
			}
			return returnedDate;
		}

	}

	public static LocalDate calculateExpectedInactiveDate(CopyRetentionRule copyRule,
			LocalDate baseDate, int numberOfYearWhenVariableDelayPeriod, CalculatorLogger logger) {

		if (baseDate == null) {
			if (logger.isTroubleshooting()) {
				logger.log("Aucune date ne peut être utilisée pour le calcul de la date de versement/destruction prévue");
			}
			return null;
		} else if (copyRule.getSemiActiveRetentionPeriod().isVariablePeriod()) {
			if (numberOfYearWhenVariableDelayPeriod == -1) {
				if (logger.isTroubleshooting()) {
					logger.log("C'est un délai ouvert, et la configuration précise qu'il ne faut pas calculer la date "
							+ "de versement/destructio lorsque le  délai est ouvert");
				}
				return null;
			} else {
				LocalDate returnedDate = baseDate.plusYears(numberOfYearWhenVariableDelayPeriod);
				if (logger.isTroubleshooting()) {
					logger.log("C'est un délai ouvert et selon la config, on ajoute " + numberOfYearWhenVariableDelayPeriod
							+ " année(s) : " + baseDate + " => " + returnedDate);
				}

				return returnedDate;
			}
		} else if (copyRule.getSemiActiveRetentionPeriod().isZero()) {
			if (copyRule.getActiveRetentionPeriod().isVariablePeriod()) {
				if (logger.isTroubleshooting()) {
					logger.log("Le délai actif est ouvert et le délai semi-actif est à 0, on ajoute alors "
							+ numberOfYearWhenVariableDelayPeriod + " à la date " + baseDate);
				}

				return baseDate.plusYears(numberOfYearWhenVariableDelayPeriod);
			} else {
				if (logger.isTroubleshooting()) {
					logger.log("Le délai actif est fixe et le délai semi-actif est à 0, on ajoute alors " +
							copyRule.getActiveRetentionPeriod().getFixedPeriod() + " à la date " + baseDate);
				}

				return baseDate.plusYears(copyRule.getActiveRetentionPeriod().getFixedPeriod());
			}
		} else {
			int fixedPeriod = copyRule.getSemiActiveRetentionPeriod().getFixedPeriod();
			LocalDate returnedDate = baseDate.plusYears(fixedPeriod);
			if (logger.isTroubleshooting()) {
				logger.log("C'est un délai fixe, on ajoute " + fixedPeriod + " année(s) : " + baseDate
						+ " => " + returnedDate);
			}
			return returnedDate;
		}

	}
}
