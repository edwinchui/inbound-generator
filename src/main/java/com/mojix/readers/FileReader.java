package com.mojix.readers;

import java.io.File;

public interface FileReader {

    default String getPathToFile(String fileName) {
        return getBasePath() + File.separator + fileName;
    }

    default String getBasePath() {
        String basePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        basePath = (new File(basePath)).getParent();

        return basePath;
    }
}
