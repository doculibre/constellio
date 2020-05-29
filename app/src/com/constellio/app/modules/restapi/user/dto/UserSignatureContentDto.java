package com.constellio.app.modules.restapi.user.dto;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;

@Data
@Builder
public class UserSignatureContentDto {
	private InputStream content;
	private String mimeType;
	private String filename;
}
