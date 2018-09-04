package com.constellio.data.extensions.extensions.configManager;

import java.io.File;
import java.io.IOException;

public interface ExtensionConverter {
	File convert(File input) throws IOException;
}
