package com.constellio.app.api.graphql.builder;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Builder
@Data
@EqualsAndHashCode
public class WiringInfo {
	private final String type;
	private final String name;
	private final String constellioName;
	private final String schemaType;
	private final boolean reference;
}
