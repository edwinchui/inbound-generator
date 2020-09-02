package com.mojix.generator;

import com.mojix.integration.gm.PartImportMGO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneratorFactory {

    private static Logger logger = LoggerFactory.getLogger(GeneratorFactory.class);

    public static final PartImportMGO getGeneratorFile(GenerateOption generateOption,
                                                       String plantCode,
                                                       Integer numberOfRecords) {
        switch(generateOption) {
            case defectCategories:
                return new DefectCategoriesGenerator(plantCode, numberOfRecords);
            default:
                logger.error("Generate Option not supported.");
                System.exit(6);
        }

        return null;
    }
}
