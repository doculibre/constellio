package com.constellio.app.modules.restapi.user.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

import java.io.InputStream;

@JsonRootName("UserCredentialsContent")
@Data
@Builder
public class UserCredentialsContentDto {
	private InputStream content;
	private String mimeType;
	private String filename;
}
