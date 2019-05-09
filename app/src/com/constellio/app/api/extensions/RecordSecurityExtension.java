package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.RecordSecurityParam;

public interface RecordSecurityExtension {
	boolean isRecordAvalibleToAllUsers(RecordSecurityParam recordSecurityParam);
}
