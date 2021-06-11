package com.constellio.model.entities.records.wrappers.structure;

import com.constellio.model.entities.schemas.CombinedStructureFactory;
import com.constellio.model.entities.schemas.ModifiableStructure;
import org.apache.commons.exec.util.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ReportedMetadataFactory implements CombinedStructureFactory {
	private static final String ELEMENTS_SEPARATOR = "~#~";

	@Override
	public ModifiableStructure build(String string) {
		String[] tokens = StringUtils.split(string, ELEMENTS_SEPARATOR);

		String metadataCode = readString(tokens[0]);
		int xPosition = readInt(tokens[1]);
		int yPosition = readInt(tokens[2]);
		return new ReportedMetadata(metadataCode, xPosition, yPosition);
	}

	private int readInt(String token) {
		return Integer.valueOf(token);
	}

	@Override
	public String toString(ModifiableStructure structure) {
		ReportedMetadata reportedMetadata = (ReportedMetadata) structure;
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(reportedMetadata.getMetadataCode() + ELEMENTS_SEPARATOR);
		stringBuilder.append(reportedMetadata.getXPosition() + ELEMENTS_SEPARATOR);
		stringBuilder.append(reportedMetadata.getYPosition() + ELEMENTS_SEPARATOR);
		return stringBuilder.toString();
	}

	private String readString(String value) {
		if ("null".equals(value)) {
			return null;
		} else {
			return value;
		}
	}


	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

}
