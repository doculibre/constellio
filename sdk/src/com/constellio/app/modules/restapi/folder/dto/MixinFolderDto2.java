package com.constellio.app.modules.restapi.folder.dto;

import com.constellio.app.modules.restapi.resource.dto.AceDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.LocalDate;

import javax.validation.Valid;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_WRITE;

public class MixinFolderDto2 {
	@JsonProperty(access = READ_WRITE)
	private String mediaType;

	@Valid @JsonProperty(access = READ_WRITE)
	private LocalDate expectedTransferDate;
	@Valid @JsonProperty(access = READ_WRITE)
	private LocalDate expectedDepositDate;
	@Valid @JsonProperty(access = READ_WRITE)
	private LocalDate expectedDestructionDate;
	@Valid @JsonProperty(access = READ_WRITE)
	private List<AceDto> inheritedAces;
}
