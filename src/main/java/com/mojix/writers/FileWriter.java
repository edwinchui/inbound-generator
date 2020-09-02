package com.mojix.writers;

import com.mojix.integration.gm.IntegrationField;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileWriter {

    String FILE_ENCODING = "UTF-8";

    void initTargetFile() throws IOException;

    void writeRecords(List<Map<IntegrationField, String>> listRecords);

    void finishWriteFile() throws IOException;

    String getFileName();

    default String getPathToFile() {
        return getBasePath() + File.separator + getFileName();
    }

    default String getBasePath() {
        String basePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        basePath = (new File(basePath)).getParent();

        return basePath;
    }
}
