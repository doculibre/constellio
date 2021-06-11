package com.constellio.app.modules.restapi.apis.v1.resource.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AceListDto {
	private List<AceDto> directAces;
	private List<AceDto> inheritedAces;
}
