package com.mojix.integration.gm;

import com.mojix.integration.IntegrationParams;
import com.mojix.integration.PlantParams;
import com.mojix.writers.CSVFileWriter;
import com.mojix.writers.ExcelFileWriter;
import com.mojix.writers.FileWriter;
import com.mojix.writers.TextFileWriter;
import com.mojix.pojo.Part;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public abstract class PartImportMGOImpl implements PartImportMGO {
	
	protected static final String MGO_A_DATE_FORMAT = "yyyy-MM-dd-HH:mm:ss:SSSSSS";
	protected static final String MGO_C_DATE_FORMAT = "yyyyMMddHHmmss";
	protected static final String INTERNAL_PART_NUMBER_SEPARATOR = "@";
	private static Logger logger = LoggerFactory.getLogger(PartImportMGOImpl.class);
	
	private long sequenceCount;
	protected Long timeStamp;
	private Random random;
	private String basePath;
	
	private IntegrationParams integrationParams;
	
	public PartImportMGOImpl() {
		this.timeStamp = Calendar.getInstance().getTimeInMillis();
		this.sequenceCount = 100;
		this.random = new Random(this.timeStamp);
		
		this.basePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		this.basePath = (new File(this.basePath)).getParent();
	}
	
	public void addConfigurationParams(IntegrationParams integrationParams) {
		this.integrationParams = integrationParams;
	}

	protected String getSequenceNumber() {
		return String.valueOf(this.sequenceCount++);
	}
	
	protected Object getNextIntRandom(List values) {
		return values.get(getNextIntRandom(values.size() - 1, 0));
	}
	
	protected int getNextIntRandom(Item item, int minValue) {
		int maxValue = this.getMaxIntValueByLength(item.getLenght());
		
		return getNextIntRandom(maxValue, minValue);
	}
	
	protected int getNextIntRandom(int maxValue, int minValue) {
		return Math.abs(this.random.nextInt((maxValue - minValue) + 1) + minValue);
	}
	
	protected BigDecimal getNextDoubleRandom(Item item, int precision) {
		BigDecimal maxValue = getMaxDoubleValueByLength(item.getLenght(), precision);
		
		return getNextDoubleRandom(maxValue, precision);
	}
	
	protected BigDecimal getNextDoubleRandom(BigDecimal maxValue, int precision) {
		maxValue = maxValue.multiply(new BigDecimal(this.random.nextDouble()));
		
		maxValue = maxValue.setScale(precision, RoundingMode.CEILING);
		
		
		return maxValue;
	}
	
	protected int getMaxIntValueByLength(int length) {
		try {
			return Integer.parseInt(StringUtils.leftPad("", length, "9"));
		} catch(NumberFormatException e) {
			return Integer.MAX_VALUE;
		}
	}
	
	protected BigDecimal getMaxDoubleValueByLength(int length, int precision) {
		BigDecimal result = new BigDecimal(StringUtils.leftPad("", length - precision - 1, "9"));
		
		result = result.add(new BigDecimal("0." + StringUtils.leftPad("", precision, "9")));
		
		return result;
	}
	
	protected String getRandomArrayValue(String[] possibleValues) {
		int index = this.getNextIntRandom(possibleValues.length, 0);
		
		return possibleValues[index];
		
	}

	public String[] separateDgoInSegments(String billingCode) {
		String[] segments = {"", "", ""};
		String[] values = billingCode.split(" ");

		if(values != null) {
			if(values.length > 0) {
				segments[0] = values[0];
			}
			if(values.length > 1) {
				segments[1] = values[1];
			}
			if(values.length > 2) {
				segments[2] = values[2];
			}
		}

		return segments;
	}

	@Override
	public void generateFile() {
		List<FileWriter> outputTargets = this.setOutputTargets();

		try {
			for(FileWriter fileWriter : outputTargets) {
				fileWriter.initTargetFile();

				for(PlantParams plant : this.integrationParams.getPlantParams()) {
					fileWriter.writeRecords(buildLines(plant.getCode()));
				}

				fileWriter.finishWriteFile();
			}
		} catch (IOException e) {
			logger.error("An error occurred when generating ", e);
		}
	}

	private List<FileWriter> setOutputTargets() {
		List<FileWriter> outputTargets = new ArrayList<>();

		if(this.integrationParams.isOutputFormatCsv()) {
			outputTargets.add(new CSVFileWriter(this));
		}

		if(this.integrationParams.isOutputFormatText()) {
			outputTargets.add( new TextFileWriter(this) );
		}

		if(this.integrationParams.isOutputFormatExcel()) {
			outputTargets.add( new ExcelFileWriter(this) );
		}

		return outputTargets;
	}

	public IntegrationParams getIntegrationParams() {
		return this.integrationParams;
	}
	
	public List<Part> getPartNumbersPerPlant(String plantCode) {
		return this.integrationParams.getPartNumbersPerPlant().get(plantCode);
	}
	
	protected Map<Part, List<Part>> getParentChildrenRelation(String plantCode) {
		List<Part> parentParts;
		List<Part> childrenParts;
		BigDecimal totalParentParts;
		Integer partChildrenRelationQty;
		List<Part> partNumbers = this.getIntegrationParams().getPartNumbersPerPlant().get(plantCode);
		HashMap<Part, List<Part>> partBomRelations = new HashMap<>();
		
		partChildrenRelationQty = this.getIntegrationParams().getParentChildrenRelationQty();
		totalParentParts = new BigDecimal(this.getIntegrationParams().getPlantParams(plantCode).getBomPartRecords())
				.divide(new BigDecimal(partChildrenRelationQty), 0,  RoundingMode.UP);
		
		logger.info("** Processing BOM Parts for plant code {}", plantCode);
		logger.info("Total Parent Parts: {}", totalParentParts);
		logger.info("Total Children Parts per Parent Part: " + partChildrenRelationQty);
		
		parentParts = partNumbers.subList(0, totalParentParts.intValue());
		childrenParts = partNumbers.subList(
					totalParentParts.intValue() + 1,
					partNumbers.size()
				);
		
		this.getIntegrationParams().addPartNumbersPerPlant(plantCode, childrenParts);
		
		for(Part parentPart : parentParts) {
			if(partChildrenRelationQty >= childrenParts.size()) {
				break;
			}
			
			partBomRelations.put(
					parentPart,
					childrenParts.subList(0, partChildrenRelationQty)
					);
			
			childrenParts = childrenParts.subList(
					partChildrenRelationQty + 1,
					childrenParts.size()
					);
		};

		return partBomRelations;
	}

	@Override
	public String getFileName() {
		StringBuilder builder = new StringBuilder();

		builder.append(this.getIntegrationParams().getPrefixFileNamePattern());
		builder.append(this.getFileTypeIdentifier());

		return builder.toString();
	}

	public abstract List<Map<IntegrationField, String>> buildLines(String plantCode);
	
	public abstract String getHeader();
	
	public abstract String getTrailer();
}
