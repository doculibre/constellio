package com.constellio.app.modules.restapi.apis.v1.folder.dto;

import com.constellio.app.modules.restapi.apis.v1.resource.dto.BaseReferenceDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@JsonRootName("AdministrativeUnit")
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AdministrativeUnitDto extends BaseReferenceDto {
	@JsonProperty(access = READ_ONLY)
	String code;

	@Builder
	public AdministrativeUnitDto(String id, String code, String title) {
		super(id, title);
		this.code = code;
	}
}
