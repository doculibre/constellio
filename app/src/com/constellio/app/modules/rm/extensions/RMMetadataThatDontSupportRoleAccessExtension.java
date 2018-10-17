package com.constellio.app.modules.rm.extensions;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.api.extensions.MetadataThatDontSupportRoleAccessExtension;
import com.constellio.app.api.extensions.MetadataThatDontSupportRoleAccessRetValue;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;

public class RMMetadataThatDontSupportRoleAccessExtension implements MetadataThatDontSupportRoleAccessExtension {
	public List<MetadataThatDontSupportRoleAccessRetValue> getMetadataThatDontSupportRoleAccess() {
		return Arrays.asList(new MetadataThatDontSupportRoleAccessRetValue(Document.SCHEMA_TYPE, null, Document.FOLDER),
				new MetadataThatDontSupportRoleAccessRetValue(Document.SCHEMA_TYPE, null, Document.APPLICABLE_COPY_RULES),
				new MetadataThatDontSupportRoleAccessRetValue(Folder.SCHEMA_TYPE, null, Folder.PARENT_FOLDER),
				new MetadataThatDontSupportRoleAccessRetValue(Folder.SCHEMA_TYPE, null, Folder.SUMMARY),
				new MetadataThatDontSupportRoleAccessRetValue(ContainerRecord.SCHEMA_TYPE, null, ContainerRecord.CAPACITY),
				new MetadataThatDontSupportRoleAccessRetValue(ContainerRecord.SCHEMA_TYPE, null, ContainerRecord.TYPE));
	}
}
