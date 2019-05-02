package com.constellio.app.modules.restapi.folder.dto;

import com.constellio.app.modules.restapi.resource.dto.AceDto;
import com.constellio.app.modules.restapi.resource.dto.ExtendedAttributeDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.joda.time.LocalDate;

import javax.validation.Valid;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Data
@Builder
@JsonRootName("Folder")
public class FolderDto {
	private String id;
	private String parentFolderId;
	private FolderTypeDto type;
	private String category;
	private String retentionRule;
	private String administrativeUnit;
	private String mainCopyRule;
	private String copyStatus;
	private List<String> mediumTypes;
	@JsonProperty(access = READ_ONLY)
	private String mediaType;
	private String container;
	private String title;
	private String description;
	private List<String> keywords;
	private LocalDate openingDate;
	private LocalDate closingDate;
	private LocalDate actualTransferDate;
	private LocalDate actualDepositDate;
	private LocalDate actualDestructionDate;
	@Valid @JsonProperty(access = READ_ONLY)
	private LocalDate expectedTransferDate;
	@Valid @JsonProperty(access = READ_ONLY)
	private LocalDate expectedDepositDate;
	@Valid @JsonProperty(access = READ_ONLY)
	private LocalDate expectedDestructionDate;
	@Valid
	private List<AceDto> directAces;
	@Valid @JsonProperty(access = READ_ONLY)
	private List<AceDto> inheritedAces;
	@Valid
	private List<ExtendedAttributeDto> extendedAttributes;
	@JsonIgnore @Getter(onMethod = @__(@JsonIgnore))
	private String eTag;

}
