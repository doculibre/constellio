package com.constellio.app.modules.rm.extensions;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.api.extensions.MetadataThatDontSupportRoleAccessExtension;
import com.constellio.app.api.extensions.MetadataThatDontSupportRoleAccessRetParam;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;

public class RMMetadataThatDontSupportRoleAccessExtension implements MetadataThatDontSupportRoleAccessExtension {
	public List<MetadataThatDontSupportRoleAccessRetParam> getMetadataThatDontSupportRoleAccess() {
		return Arrays.asList(new MetadataThatDontSupportRoleAccessRetParam(Document.SCHEMA_TYPE, null, Document.FOLDER),
				new MetadataThatDontSupportRoleAccessRetParam(Document.SCHEMA_TYPE, null, Document.APPLICABLE_COPY_RULES),
				new MetadataThatDontSupportRoleAccessRetParam(Folder.SCHEMA_TYPE, null, Folder.PARENT_FOLDER),
				new MetadataThatDontSupportRoleAccessRetParam(Folder.SCHEMA_TYPE, null, Folder.SUMMARY));
	}
}
