package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.MetadataThatDontSupportRoleAccessExtension;
import com.constellio.app.api.extensions.MetadataThatDontSupportRoleAccessRetValue;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;

import java.util.Arrays;
import java.util.List;

public class RMMetadataThatDontSupportRoleAccessExtension implements MetadataThatDontSupportRoleAccessExtension {
	public List<MetadataThatDontSupportRoleAccessRetValue> getMetadataThatDontSupportRoleAccess() {
		return Arrays.asList(new MetadataThatDontSupportRoleAccessRetValue(Document.SCHEMA_TYPE, null, Document.FOLDER),
				new MetadataThatDontSupportRoleAccessRetValue(Folder.SCHEMA_TYPE, null, Folder.PARENT_FOLDER),
				new MetadataThatDontSupportRoleAccessRetValue(AdministrativeUnit.SCHEMA_TYPE, null, AdministrativeUnit.PARENT),
				new MetadataThatDontSupportRoleAccessRetValue(Category.SCHEMA_TYPE, null, Category.PARENT));
	}
}
