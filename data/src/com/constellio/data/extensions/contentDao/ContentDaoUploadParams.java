package com.constellio.data.extensions.contentDao;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ContentDaoUploadParams {
	String hash;
	long length;
	boolean movedFromLocalFile;
}
