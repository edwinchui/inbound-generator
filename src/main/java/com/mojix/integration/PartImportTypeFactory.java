package com.mojix.integration;

import com.mojix.integration.gm.PartImportMGO;
import com.mojix.integration.gm.mgoa.DiscretePartMgoA;
import com.mojix.integration.gm.mgoa.PartBomMgoA;
import com.mojix.integration.gm.mgoa.PartGroupMgoA;
import com.mojix.integration.gm.mgoc.DiscretePartMgoC;
import com.mojix.integration.gm.mgoc.PartBomMgoC;
import com.mojix.integration.gm.mgoc.PartGroupMgoC;

import java.util.Properties;

import static com.mojix.integration.gm.PartImportMGO.*;
import static com.mojix.integration.gm.PartImportMGO.BOM_FILE_TYPE_IDENTIFIER;

public class PartImportTypeFactory {
	
	public static final String ADAPTER_TYPE_MGOA = "MGO-A";
	public static final String ADAPTER_TYPE_MGOC = "MGO-C";
	
	private PartImportTypeFactory() {
		super();
	}

	public static PartImportMGO getPartImportBySheetName(String sheetName,
														 Properties paramProperties) {
		if(sheetName.contains(MGOA)) {
			if(sheetName.contains(DISCRETE_PART_FILE_TYPE_IDENTIFIER)) {
				return PartImportTypeFactory.getDiscretePartImport(new IntegrationParams(ADAPTER_TYPE_MGOA, paramProperties));
			} else if(sheetName.contains(GROUP_FILE_TYPE_IDENTIFIER)) {
				return PartImportTypeFactory.getPartGroupImport(new IntegrationParams(ADAPTER_TYPE_MGOA, paramProperties));
			} else if(sheetName.contains(BOM_FILE_TYPE_IDENTIFIER)) {
				return PartImportTypeFactory.getPartBomImport(new IntegrationParams(ADAPTER_TYPE_MGOA, paramProperties));
			}
		} else if(sheetName.contains(MGOC)) {
			if(sheetName.contains(DISCRETE_PART_FILE_TYPE_IDENTIFIER)) {
				return PartImportTypeFactory.getDiscretePartImport(new IntegrationParams(ADAPTER_TYPE_MGOC, paramProperties));
			} else if(sheetName.contains(DGO_FILE_TYPE_IDENTIFIER)) {
				return PartImportTypeFactory.getPartGroupImport(new IntegrationParams(ADAPTER_TYPE_MGOC, paramProperties));
			} else if(sheetName.contains(BOM_FILE_TYPE_IDENTIFIER)) {
				return PartImportTypeFactory.getPartBomImport(new IntegrationParams(ADAPTER_TYPE_MGOC, paramProperties));
			}
		}

		return null;
	}

	public static PartImportMGO getDiscretePartImport(IntegrationParams integrationParams) {
		PartImportMGO discretePartImportMGO;
		
		if(integrationParams.getAdapterName().equals(ADAPTER_TYPE_MGOA)) {
			discretePartImportMGO = new DiscretePartMgoA();
		} else {
			discretePartImportMGO = new DiscretePartMgoC();
		}
		discretePartImportMGO.addConfigurationParams(integrationParams);
		
		return discretePartImportMGO;
	}
	
	public static PartImportMGO getPartBomImport(IntegrationParams integrationParams) {
		PartImportMGO partBomImportMGO;
		
		if(integrationParams.getAdapterName().equals(ADAPTER_TYPE_MGOA)) {
			partBomImportMGO = new PartBomMgoA();
		} else {
			partBomImportMGO = new PartBomMgoC();
		}
		partBomImportMGO.addConfigurationParams(integrationParams);
		
		return partBomImportMGO;
	}
	
	public static PartImportMGO getPartGroupImport(IntegrationParams integrationParams) {
		PartImportMGO partGroupimportMGO;
		
		if(integrationParams.getAdapterName().equals(ADAPTER_TYPE_MGOA)) {
			partGroupimportMGO = new PartGroupMgoA();
		} else {
			partGroupimportMGO = new PartGroupMgoC();
		}
		partGroupimportMGO.addConfigurationParams(integrationParams);
		
		return partGroupimportMGO;
	}
	
}
