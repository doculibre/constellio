package com.constellio.app.modules.rm.pdfgenerator;

import java.util.ArrayList;
import java.util.List;

public interface PdfGeneratorProgressInfo {
    public String getGlobalMessage();
    public List<FileStatus> getMessages();
    public boolean isStarted();
    public boolean isEnded();
}
