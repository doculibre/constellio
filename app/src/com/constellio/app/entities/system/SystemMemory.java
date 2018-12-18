package com.constellio.app.entities.system;

import com.constellio.app.api.admin.services.SystemAnalysisUtils;
import com.constellio.model.entities.EnumWithSmallCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class SystemMemory {

	MemoryDetails totalSystemMemory;
	MemoryDetails constellioAllocatedMemory;
	MemoryDetails solrAllocatedMemory;

	private SystemMemory() {};

	private SystemMemory(MemoryDetails totalSystemMemory, MemoryDetails constellioAllocatedMemory, MemoryDetails solrAllocatedMemory) {
		this.totalSystemMemory = totalSystemMemory;
		this.constellioAllocatedMemory = constellioAllocatedMemory;
		this.solrAllocatedMemory = solrAllocatedMemory;
	}

	public static SystemMemory fetchSystemInfos() {
		return new SystemMemory(SystemAnalysisUtils.getTotalSystemMemory(),
				SystemAnalysisUtils.getAllocatedMemoryForConstellio(),
				SystemAnalysisUtils.getAllocatedMemoryForSolr());
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

	public static class MemoryDetails {
		Double amount;
		MemoryUnit unit;

		public MemoryDetails(int amount, MemoryUnit unit) {
			this.amount = Double.valueOf(amount);
			this.unit = unit;
		}

		static public MemoryDetails build(String amountAsString, String defaultUnit) {
			try {
				if(amountAsString == null) {
					return buildUnrecognizableMemoryAmount(amountAsString);
				}
				String regex = "((?<=[a-zA-Z])(?=[0-9]))|((?<=[0-9])(?=[a-zA-Z]))";
				List<String> numericalAndAlphabeticBits = Arrays.asList(amountAsString.trim().replace(" ", "").split(regex));
				if(numericalAndAlphabeticBits.size() == 1 && StringUtils.isNumeric(numericalAndAlphabeticBits.get(0)) && defaultUnit != null) {
					return new MemoryDetails(Integer.parseInt(numericalAndAlphabeticBits.get(0)), MemoryUnit.getCorrespondingMemoryUnit(defaultUnit));
				} else if(numericalAndAlphabeticBits.size() == 2 && StringUtils.isNumeric(numericalAndAlphabeticBits.get(0)) &&
						  StringUtils.isAlpha(numericalAndAlphabeticBits.get(1))){
					return new MemoryDetails(Integer.parseInt(numericalAndAlphabeticBits.get(0)), MemoryUnit.getCorrespondingMemoryUnit(numericalAndAlphabeticBits.get(1)));
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

		@Override
		public String toString() {
			return amount + " " + unit.getCode();
		}

		public static MemoryDetails buildUnrecognizableMemoryAmount(final String unrecognizableAmount) {
			return new MemoryDetails(-1, null) {
				@Override
				public String toString() {
					return unrecognizableAmount;
				}
			};
		}
	}

	private enum MemoryUnit implements EnumWithSmallCode {
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

	public Double getPercentageOfAllocatedMemory() {
		if (totalSystemMemory.toNumberOfBytes() != null && constellioAllocatedMemory.toNumberOfBytes() != null && solrAllocatedMemory.toNumberOfBytes() != null) {
			return roundToTwoDecimals((constellioAllocatedMemory.toNumberOfBytes() + solrAllocatedMemory.toNumberOfBytes()) / totalSystemMemory.toNumberOfBytes());
		} else {
			return null;
		}
	}

	private static double roundToTwoDecimals(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

	public static void main(String[] args) {
		SystemMemory systemInfo = new SystemMemory(MemoryDetails.build("15728640", "k"), MemoryDetails.build("5120mb", null), MemoryDetails.build("5 G", null));
		System.out.println(systemInfo.getPercentageOfAllocatedMemory() + " %");
		System.out.println(systemInfo.getTotalSystemMemory().toNumberOfGigaBytes() + " GB");
		System.out.println(systemInfo.getConstellioAllocatedMemory().toNumberOfGigaBytes() + " GB");
		System.out.println(systemInfo.getSolrAllocatedMemory().toNumberOfGigaBytes() + " GB");
	}
}
