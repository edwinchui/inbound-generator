package com.mojix.integration;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.mojix.generator.GenerateOption;
import com.mojix.generator.GeneratorFactory;
import com.mojix.readers.ExcelFileReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mojix.generator.GenerateOption.*;

public class BridgeFileGenerator {
	
	private static String PARAM_FILE_NAME = "parameters.properties";
	private static String HIKARI_FILE_NAME = "hikari.properties";
	private static String TEMP_PARAM_FILE_NAME = "parameters_template.properties";
	private static String TEMP_HIKARI_FILE_NAME = "hikari_template.properties";
	private static Logger logger = LoggerFactory.getLogger(BridgeFileGenerator.class);

	private void generateBridgeFiles() {
		List<IntegrationParams> params = readIntegrationParams();

		params.forEach(integrationParams -> {
			PartImportTypeFactory.getDiscretePartImport(integrationParams).generateFile();
			
			PartImportTypeFactory.getPartGroupImport(integrationParams).generateFile();

			PartImportTypeFactory.getPartBomImport(integrationParams).generateFile();
		});
	}
	
	private List<IntegrationParams> readIntegrationParams() {
		List<IntegrationParams> integrationsParams;
		final Properties params = readPropertiesParams();
		
		integrationsParams = Arrays.asList(
									params.getProperty("active.integrations").split(",")
								).stream()
								.map(adapterName -> new IntegrationParams(adapterName, params))
								.collect(Collectors.toList());
		logger.info("*** Recovered Param values: ***");
		logger.info(String.format(
				"activeIntegrations=%s",
				integrationsParams
					.stream()
					.map(integration -> integration.toString())
					.reduce(",\n", String::concat)
				));
		
		return integrationsParams;
	}

	public static Properties readPropertiesParams() {
		File currentFolder;
		final Properties params = new Properties();

		logger.info("Reading params from file {}", PARAM_FILE_NAME);

		try {
			currentFolder = new File(
					BridgeFileGenerator.class.getProtectionDomain().getCodeSource().getLocation().toURI()
			);

			params.load(new FileInputStream(currentFolder.getParent() + File.separator + PARAM_FILE_NAME));

			// params.load(ClassLoader.getSystemResourceAsStream(PARAM_FILE_NAME));
		} catch (FileNotFoundException e) {
			logger.error("File {} not found", PARAM_FILE_NAME, e);
			logger.info("The required files will be created...");

			System.exit(0);
		} catch (IOException e) {
			logger.error("Cannot read file properties");
			System.exit(3);
		} catch (URISyntaxException e) {
			logger.error("Error trying to get the current jar's folder");
			System.exit(4);
		}

		return params;
	}
	
	private void createRequiredFiles(String temporalFileName,
											String propertyFileName) {
		InputStream originFile;
		File copiedFile;
		String basePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			originFile = BridgeFileGenerator.class.getResourceAsStream(temporalFileName);
			basePath = new File(basePath + File.separator + propertyFileName).getParent();
			copiedFile = new File(basePath);

			FileUtils.copyToFile(originFile, copiedFile);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private static String getPlantCodeFromArgs(String[] args) {
		if(args.length == 4) {
			if(StringUtils.isAlpha(args[2])) {
				return args[2].trim().toUpperCase();
			} else if(StringUtils.isAlpha(args[3])) {
				return args[3].trim().toUpperCase();
			} else {
				logger.error("Cannot find a valid plant code.");
			}
		} else {
			logger.error("Invalid arguments");
		}

		System.exit(2);
		return null;
	}

	private static Integer getNumberOfRecords(String[] args) {
		if(args.length == 4) {
			try {
				if (StringUtils.isNumeric(args[2])) {
					return Integer.parseInt(args[2].trim());
				} else if (StringUtils.isNumeric(args[3])) {
					return Integer.parseInt(args[3].trim());
				}
			} catch(NumberFormatException e) {
				logger.error("Invalid number of records.", e);
			}
		} else {
			logger.error("Invalid arguments");
		}

		System.exit(2);
		return null;
	}
	
	public static void main(String[] args) {
		String plantCode;
		Integer numberOfRecords;
		GenerateOption generateOption;

		if (args != null && args.length == 2) {
			if (args[0].equalsIgnoreCase("-c") || args[0].equals("--convert")) {
				ExcelFileReader excelFileReader = new ExcelFileReader(args[1]);
				excelFileReader.convertSheetsToText();
			} else {
				logger.error("Invalid arguments.");
				System.exit(1);
			}
		} else if(args != null && args.length == 4 && Arrays.stream(args).anyMatch(arg -> arg.matches("-g|--generate"))) {
			generateOption = valueOf(args[1].trim());
			if (generateOption != null) {
				plantCode = getPlantCodeFromArgs(args);
				numberOfRecords = getNumberOfRecords(args);
				GeneratorFactory.getGeneratorFile(generateOption, plantCode, numberOfRecords).generateFile();
			} else {
				logger.error(
						"Invalid generate option. Available options: \n{}",
						StringUtils.join(Arrays.asList(values()), "\n")
				);
				System.exit(1);
			}
		} else {
			logger.info("No arguments found or invalid number of arguments. Proceeding to generate new files.");
			new BridgeFileGenerator().generateBridgeFiles();
		}
		
		System.exit(0);
	}
	
}
