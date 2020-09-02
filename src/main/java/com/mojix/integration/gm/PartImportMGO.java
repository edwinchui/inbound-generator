package com.mojix.integration.gm;

import com.mojix.integration.IntegrationParams;

import java.util.List;

public interface PartImportMGO {

	String MGOA = "MGOA";
	String MGOC = "MGOC";
	String DISCRETE_PART_FILE_TYPE_IDENTIFIER = "DSPRT";
	String DGO_FILE_TYPE_IDENTIFIER = "DGO";
	String BOM_FILE_TYPE_IDENTIFIER = "SABOM";
	String GROUP_FILE_TYPE_IDENTIFIER = "PRTGP";

	String getFileTypeIdentifier();
	
	void addConfigurationParams(IntegrationParams integrationParams);
	
	void generateFile();

	List<IntegrationField> getHeaders();
	
	IntegrationParams getIntegrationParams();

	String getFileName();
}
