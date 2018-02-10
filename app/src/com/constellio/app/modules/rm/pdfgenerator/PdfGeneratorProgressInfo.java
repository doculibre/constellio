package com.constellio.app.modules.rm.pdfgenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfGeneratorProgressInfo {
	
    private String globalMessage;
    private Map<String, FileStatus> messages = new HashMap<>();
    private boolean started;
    private boolean ended;

    public void notifyFileProcessingMessage(String fileName, String message) {
        FileStatus fs = new FileStatus(fileName);
        fs.setMessage(message);

        messages.put(fileName, fs);
    }

    public void notifyGlobalProcessingMessage(String message) {
        globalMessage = message;
    }

    public void notifyStartProcessing() {
        started = true;
    }

    public void notifyEndProcessing() {
        ended = true;
    }

    public String getGlobalMessage() {
        return globalMessage;
    }

    public List<FileStatus> getMessages() {
        return new ArrayList<>(messages.values());
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isEnded() {
        return ended;
    }
}
