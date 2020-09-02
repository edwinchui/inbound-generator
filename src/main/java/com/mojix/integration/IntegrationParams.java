package com.mojix.integration;

import java.util.*;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojix.pojo.Part;

public class IntegrationParams {

	private final static String OUTPUT_CSV = "csv";
	private final static String OUTPUT_TEXT = "text";
	private final static String OUTPUT_EXCEL = "xls";

	private String adapterName;
	private String outputFormats = OUTPUT_TEXT;
	private String prefixFileNamePattern;
	private String generateForPlants;
	private List<PlantParams> plantParams;
	private Integer bomPartsDepthLevels;
	private Integer parentChildrenRelationQty;

	private Map<String, Integer> plantsWithValidBillinCodes = new HashMap<>();
	private Map<String, List<Part>> partNumbersPerPlant;
	
	private static Logger logger = LoggerFactory.getLogger(IntegrationParams.class);

	public IntegrationParams(String adapterName,
							 Properties params) {
		this.adapterName = adapterName;
		this.partNumbersPerPlant = new HashMap<>();
		this.plantParams = new ArrayList<>();
		String[] plantCodes; 
		int[] discretePartRecords;
		int[] partBomRecords;
		int[] partAssignmentRecords;
		
		plantCodes = params.getProperty(String.format(
				"plants.codes.%s", 
				adapterName
			)).split(",");
		
		discretePartRecords = getParamValueAsIntArray(
				params, 
				adapterName, 
				"discrete.parts.records."
		);
		
		partBomRecords = getParamValueAsIntArray(
				params, 
				adapterName, 
				"bom.parts.records."
		);
		partAssignmentRecords = getParamValueAsIntArray(
				params, 
				adapterName, 
				"part.assignments.records."
		);
		
		this.prefixFileNamePattern = params.getProperty(String.format(
				"prefix.file.name.pattern.%s",
				adapterName
			));
		
		this.bomPartsDepthLevels = Integer.valueOf(
				params.getProperty("bom.parts.levels")
			);
		this.parentChildrenRelationQty = Integer.valueOf(
				params.getProperty("parent.children.quantity.relation")
			);
		
		this.validateParams(plantCodes, discretePartRecords, partBomRecords, partAssignmentRecords);
		
		for(int i=0;i<plantCodes.length;i++) {
			this.plantParams.add(new PlantParams(
					plantCodes[i], 
					discretePartRecords[i], 
					partBomRecords[i], 
					partAssignmentRecords[i]
			));
		}

		Optional.ofNullable(params.getProperty("output.format"))
				.ifPresent(outputFormat -> this.outputFormats = outputFormat);

		Optional.ofNullable(params.getProperty("generate.plants"))
				.ifPresent(outputFormat -> this.generateForPlants = outputFormat);
	}
	
	private void validateParams(String[] plantCodes,
								int[] discretePartRecords,
								int[] partBomRecords,
								int[] partAssignmentRecords) {
		if(plantCodes.length > 0 &&
				plantCodes.length != discretePartRecords.length &&
				plantCodes.length != partBomRecords.length &&
				plantCodes.length != partAssignmentRecords.length) {
			logger.error(
					"Params {}, {} or {} length doesn't match the plant codes length that will be generated.\nPlease verify the parameters.",
					"discrete.parts.records",
					"bom.parts.records",
					"part.assignment.records"
			);
			
			System.exit(1);
		}
	}

	private int[] getParamValueAsIntArray(Properties params,
											String adapterName,
											String key) {
		String paramValue;
		
		paramValue = params.getProperty(key + adapterName);
		
		if(StringUtils.isEmpty(paramValue)) {
			logger.error(
					"parameter {}{} is missing.",
					key,
					adapterName
			);
			System.exit(1);
		}
		
		return Arrays.asList(paramValue.split(","))
				.stream()
				.mapToInt(value -> Integer.valueOf(value).intValue())
				.toArray();
	}

	public String getAdapterName() {
		return adapterName;
	}

	public void setAdapterName(String adapterName) {
		this.adapterName = adapterName;
	}

	public String getPrefixFileNamePattern() {
		return prefixFileNamePattern;
	}

	public void setPrefixFileNamePattern(String prefixFileNamePattern) {
		this.prefixFileNamePattern = prefixFileNamePattern;
	}

	public List<PlantParams> getPlantParams() {
		return plantParams;
	}

	public void setPlantParams(List<PlantParams> plantParams) {
		this.plantParams = plantParams;
	}

	public Integer getBomPartsDepthLevels() {
		return bomPartsDepthLevels;
	}

	public void setBomPartsDepthLevels(Integer bomPartsDepthLevels) {
		this.bomPartsDepthLevels = bomPartsDepthLevels;
	}

	public Map<String, Integer> getPlantsWithValidBillinCodes() {
		return plantsWithValidBillinCodes;
	}

	public void addPlantWithValidBillingCodes(String plantCode, Integer count) {
		if(count > 0) {
			this.plantsWithValidBillinCodes.put(plantCode, count);
		}
	}

	public Integer getTotalValidBillingCodes(String plantCode) {
		if(this.plantsWithValidBillinCodes.containsKey(plantCode)) {
			return this.plantsWithValidBillinCodes.get(plantCode);
		}

		return 0;
	}

	public Map<String, List<Part>> getPartNumbersPerPlant() {
		return partNumbersPerPlant;
	}

	public void setPartNumbersPerPlant(HashMap<String, List<Part>> partNumbersPerPlant) {
		this.partNumbersPerPlant = partNumbersPerPlant;
	}

	public void addPartNumbersPerPlant(String plantCode,
									   List<Part> partNumbersByPlant) {
		if(this.partNumbersPerPlant.containsKey(plantCode)) {
			this.partNumbersPerPlant.remove(plantCode);
		}
		
		this.partNumbersPerPlant.put(plantCode, partNumbersByPlant);
	}
	
	public Integer getParentChildrenRelationQty() {
		return parentChildrenRelationQty;
	}

	public void setParentChildrenRelationQty(Integer parentChildrenRelationQty) {
		this.parentChildrenRelationQty = parentChildrenRelationQty;
	}

	public boolean isOutputFormatCsv() {
		return outputFormats.contains(OUTPUT_CSV);
	}

	public boolean isOutputFormatText() {
		return this.outputFormats.contains(OUTPUT_TEXT);
	}

	public boolean isOutputFormatExcel() {
		return this.outputFormats.contains(OUTPUT_EXCEL);
	}
	
	public PlantParams getPlantParams(String plantCode) {
		return this.getPlantParams()
				.stream()
				.filter(plantParam -> plantParam.getCode().equals(plantCode))
				.findAny()
				.orElse(null);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Integration [\n");
		builder.append(String.format("\tadapterName=%s,\n", adapterName));
		builder.append(String.format("\tprefixFileNamePattern=%s,\n", prefixFileNamePattern));
		
		builder.append("\t--------------------------------------\n");
		for(PlantParams plantParam : this.plantParams) {
			builder.append(String.format("\tplantCodes=%s,\n", plantParam.getCode()));
			builder.append(String.format("\ttotalDiscretePartsPerPlant=%s,\n", plantParam.getDiscretePartRecords()));
			builder.append(String.format("\ttotalBomPartsPerPlant=%s,\n", plantParam.getBomPartRecords()));
			builder.append(String.format("\ttotalPartAssignmentsPerPlant=%s,\n", plantParam.getPartAssignmentRecords()));
			builder.append("\t--------------------------------------\n");
		}
		builder.append(String.format("\tbomPartsDepthLevels=%s,\n", bomPartsDepthLevels));
		builder.append(String.format("\tparentChildrenRelationQty=%s,\n", parentChildrenRelationQty));
		builder.append(String.format("\toutputFormats=%s,\n", outputFormats));
		builder.append("]");

		return builder.toString();
	}

	public String getGenerateForPlants() {
		return generateForPlants;
	}
}