package com.constellio.app.entities.system;

import com.constellio.app.api.admin.services.SystemAnalysisUtils;
import com.constellio.model.entities.EnumWithSmallCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class SystemMemory {

	MemoryDetails totalSystemMemory;
	MemoryDetails constellioAllocatedMemory;
	MemoryDetails solrAllocatedMemory;
	MemoryDetails unallocatedMemory;

	private SystemMemory(MemoryDetails totalSystemMemory, MemoryDetails constellioAllocatedMemory, MemoryDetails solrAllocatedMemory) {
		this.totalSystemMemory = totalSystemMemory;
		this.constellioAllocatedMemory = constellioAllocatedMemory;
		this.solrAllocatedMemory = solrAllocatedMemory;
		this.unallocatedMemory = calculateNonAllocatedMemory();
	}

	public static SystemMemory fetchSystemMemoryInfo() {
		return new SystemMemory(SystemAnalysisUtils.getTotalSystemMemory(),
				SystemAnalysisUtils.getAllocatedMemoryForConstellio(),
				SystemAnalysisUtils.getAllocatedMemoryForSolr());
	}

	private MemoryDetails calculateNonAllocatedMemory() {
		if (totalSystemMemory.toNumberOfBytes() != null && constellioAllocatedMemory.toNumberOfBytes() != null && solrAllocatedMemory.toNumberOfBytes() != null) {
			return new MemoryDetails(totalSystemMemory.toNumberOfBytes() - constellioAllocatedMemory.toNumberOfBytes() - solrAllocatedMemory.toNumberOfBytes(), MemoryUnit.B);
		} else {
			return MemoryDetails.buildUnavailableInformations();
		}
	}

	public MemoryDetails getTotalSystemMemory() {
		return totalSystemMemory;
	}

	public MemoryDetails getConstellioAllocatedMemory() {
		return constellioAllocatedMemory;
	}

	public MemoryDetails getSolrAllocatedMemory() {
		return solrAllocatedMemory;
	}

	public Double getPercentageOfAllocatedMemory() {
		if (totalSystemMemory.toNumberOfBytes() != null && constellioAllocatedMemory.toNumberOfBytes() != null && solrAllocatedMemory.toNumberOfBytes() != null) {
			return roundToTwoDecimals((constellioAllocatedMemory.toNumberOfBytes() + solrAllocatedMemory.toNumberOfBytes()) / totalSystemMemory.toNumberOfBytes());
		} else {
			return null;
		}
	}

	public MemoryDetails getUnallocatedMemory() {
		return unallocatedMemory;
	}

	private static double roundToTwoDecimals(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

	public static class MemoryDetails {
		Double amount;
		MemoryUnit unit;

		public MemoryDetails(Double amount, MemoryUnit unit) {
			this.amount = amount;
			this.unit = unit;
		}

		static public MemoryDetails build(String amountAsString, String defaultUnit) {
			try {
				if(amountAsString == null) {
					return buildUnavailableInformations();
				}
				String regex = "((?<=[a-zA-Z])(?=[0-9]))|((?<=[0-9])(?=[a-zA-Z]))";
				List<String> numericalAndAlphabeticBits = Arrays.asList(amountAsString.trim().replace(" ", "").split(regex));
				if(numericalAndAlphabeticBits.size() == 1 && StringUtils.isNumeric(numericalAndAlphabeticBits.get(0)) && defaultUnit != null) {
					return new MemoryDetails(Double.parseDouble(numericalAndAlphabeticBits.get(0)), MemoryUnit.getCorrespondingMemoryUnit(defaultUnit));
				} else if(numericalAndAlphabeticBits.size() == 2 && StringUtils.isNumeric(numericalAndAlphabeticBits.get(0)) &&
						  StringUtils.isAlpha(numericalAndAlphabeticBits.get(1))){
					return new MemoryDetails(Double.parseDouble(numericalAndAlphabeticBits.get(0)), MemoryUnit.getCorrespondingMemoryUnit(numericalAndAlphabeticBits.get(1)));
				} else {
					return buildUnrecognizableMemoryAmount(amountAsString);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return buildUnrecognizableMemoryAmount(amountAsString);
			}
		}

		public Double getAmount() {
			return amount;
		}

		public MemoryUnit getUnit() {
			return unit;
		}

		public Double toNumberOfBytes() {
			return convertToUnit(MemoryUnit.B);
		}

		public Double toNumberOfKiloBytes() {
			return convertToUnit(MemoryUnit.KB);
		}

		public Double toNumberOfMegaBytes() {
			return convertToUnit(MemoryUnit.MB);
		}

		public Double toNumberOfGigaBytes() {
			return convertToUnit(MemoryUnit.GB);
		}

		public Double convertToUnit(MemoryUnit unitToConvertTo) {
			if(amount == null || unit == null) {
				return null;
			}
			return (amount*unit.getAmountOfByte())/unitToConvertTo.getAmountOfByte();
		}

		public boolean isGreaterThan(MemoryDetails comparatedMemoryDetails) {
			if(this == null || this.toNumberOfBytes() == null) {
				return false;
			} else if (comparatedMemoryDetails == null || comparatedMemoryDetails.toNumberOfBytes() == null) {
				return true;
			}
			return Double.compare(this.toNumberOfBytes(), comparatedMemoryDetails.toNumberOfBytes()) > 0;
		}

		public boolean isLessThan(MemoryDetails comparatedMemoryDetails) {
			if(this == null || this.toNumberOfBytes() == null) {
				return true;
			} else if (comparatedMemoryDetails == null || comparatedMemoryDetails.toNumberOfBytes() == null) {
				return false;
			}
			return Double.compare(this.toNumberOfBytes(), comparatedMemoryDetails.toNumberOfBytes()) < 0;
		}

		public boolean isEqualTo(MemoryDetails comparatedMemoryDetails) {
			if(this == null && comparatedMemoryDetails == null) {
				return true;
			} else if(this != null && comparatedMemoryDetails != null) {
				return Double.compare(this.toNumberOfBytes(), comparatedMemoryDetails.toNumberOfBytes()) == 0;
			}
			return false;
		}

		@Override
		public String toString() {
			if(amount == null|| unit == null) {
				return $("UpdateManagerViewImpl.statut");
			}
			return amount + " " + unit.getCode();
		}

		public static MemoryDetails buildUnrecognizableMemoryAmount(final String unrecognizableAmount) {
			return new MemoryDetails(null, null) {
				@Override
				public String toString() {
					return unrecognizableAmount;
				}
			};
		}

		public static MemoryDetails buildUnavailableInformations() {
			return new MemoryDetails(null, null);
		}
	}

	public enum MemoryUnit implements EnumWithSmallCode {
		B(1, "B"), KB(1024, "KB"), MB(1024*1024, "MB"), GB(1024*1024*1024, "GB");

		private String code;
		private int amountOfByte;

		MemoryUnit(int amountOfByte, String code) {
			this.amountOfByte = amountOfByte;
			this.code = code;
		}

		@Override
		public String getCode() {
			return null;
		}

		public int getAmountOfByte() {
			return amountOfByte;
		}

		public static MemoryUnit getCorrespondingMemoryUnit(String unit) {
			if(StringUtils.isBlank(unit)) {
				return null;
			}
			switch (unit.toLowerCase()) {
				case "b":
					return B;
				case "k":
				case "kb":
					return KB;
				case "m":
				case "mb":
					return MB;
				case "g":
				case "gb":
					return GB;
				default:
					return null;
			}
		}
	}

	public static void main(String[] args) {
		SystemMemory systemInfo = new SystemMemory(MemoryDetails.build("15728640", "k"), MemoryDetails.build("5120mb", null), MemoryDetails.build("5 G", null));
		System.out.println(systemInfo.getPercentageOfAllocatedMemory() * 100 + " %");
		System.out.println(systemInfo.getTotalSystemMemory().toNumberOfGigaBytes() + " GB");
		System.out.println(systemInfo.getConstellioAllocatedMemory().toNumberOfGigaBytes() + " GB");
		System.out.println(systemInfo.getSolrAllocatedMemory().toNumberOfGigaBytes() + " GB");
	}
}
