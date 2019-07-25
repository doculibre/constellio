package com.constellio.app.modules.restapi.folder.dto;

import com.constellio.app.modules.restapi.resource.dto.ResourceTypeDto;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonRootName("FolderType")
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FolderTypeDto extends ResourceTypeDto {

	@Builder
	public FolderTypeDto(String id, String code, String title) {
		super(id, code, title);
	}

}
