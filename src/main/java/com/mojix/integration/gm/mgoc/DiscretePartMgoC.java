package com.mojix.integration.gm.mgoc;

import com.mojix.dao.CsvFilesReader;
import com.mojix.dao.CsvFilesReader.SampleDataFiles;
import com.mojix.dao.PoolConnection;
import com.mojix.integration.gm.IntegrationField;
import com.mojix.integration.gm.Item.Padding;
import com.mojix.integration.gm.PartImportMGOImpl;
import com.mojix.pojo.Part;
import com.mojix.pojo.PartSupplier;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.mojix.integration.gm.mgoc.DiscretePartMgoC.DiscreteFieldsMGOC.*;

public class DiscretePartMgoC extends PartImportMGOImpl {

	private static final int MAX_BUILD_SEQUENCES = 4;
	private static final int MAX_BUILD_SEQUENCE_SEED = 59;
	private static Logger logger = LoggerFactory.getLogger(DiscretePartMgoC.class);
	
	public enum DiscreteFieldsMGOC implements IntegrationField {
		RECORD_SEQUENCE		( 1,  6, Padding.ZEROS_LEFT), 
		PLANT_CODE			( 7,  8, Padding.BLANK_LEFT), 
		PART_NUMBER			( 9, 16, Padding.BLANK_LEFT),
		PART_ECL			(17, 19, Padding.BLANK_LEFT),
		PART_VERSION		(20, 22, Padding.BLANK_LEFT),
		PART_NAME       	(23, 37, Padding.BLANK_LEFT), 
		PRIMARY_SUPPLIER	(38, 38, Padding.BLANK_LEFT), 
		SUPPLIER_CODE		(39, 47, Padding.BLANK_LEFT), 
		SUPPLIER_NAME		(48, 82, Padding.BLANK_LEFT);

		private int beginIndex;
		private int endIndex;
		private Padding padding;

		DiscreteFieldsMGOC(int beginIndex, int endIndex, Padding padding) {
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.padding = padding;
		}

		
		@Override
		public Padding getPadding() {
			return this.padding;
		}

		@Override
		public String getLabelName() {
			return name();
		}


		@Override
		public int getBeginIndex() {
			return this.beginIndex;
		}


		@Override
		public int getEndIndex() {
			return this.endIndex;
		}
	}

	@Override
	public String getFileTypeIdentifier() {
		return DISCRETE_PART_FILE_TYPE_IDENTIFIER;
	}

	@Override
	public List<IntegrationField> getHeaders() {
		return Arrays.asList(DiscreteFieldsMGOC.values());
	}

	@Override
	public List<Map<IntegrationField, String>> buildLines(String plantCode) {
		Part part;
		List<PartSupplier> listSuppliers;
		CSVRecord partDetail;
		Map<IntegrationField, String> mapRecord;
		ArrayList<Part> partNumbersByPlant = new ArrayList<>();
		List<String> cacheOperations = this.initCacheOperations(plantCode);
		List<Map<IntegrationField, String>> listRecords = new ArrayList<>();

		if(cacheOperations.isEmpty()) {
			logger.warn("There are no operations to assign. Please verify the operation's billing code accomplish the requirements.");
			logger.warn("Data will not be generated for {} plant", plantCode);
		} else {
			this.getIntegrationParams().addPlantWithValidBillingCodes(plantCode, cacheOperations.size());
		}

		for(int i=0;i<this.getIntegrationParams().getPlantParams(plantCode).getDiscretePartRecords();i++) {
			listSuppliers = CsvFilesReader.getInstance().getRandomListSuppliers(SampleDataFiles.SUPPLIERS_CSV_FILE);
			partDetail = CsvFilesReader.getInstance().getRandomRecord(SampleDataFiles.PART_NAMES_CSV_FILE);
			part = this.generateNewPart(cacheOperations);
			
			partNumbersByPlant.add(part);
			for(PartSupplier partSupplier : listSuppliers) {
				mapRecord = new LinkedHashMap<>();
				mapRecord.put(RECORD_SEQUENCE, RECORD_SEQUENCE.getFormatedValue( super.getSequenceNumber() ));
				mapRecord.put(PLANT_CODE, PLANT_CODE.getFormatedValue(plantCode));
				mapRecord.put(PART_NUMBER, PART_NUMBER.getFormatedValue(part.getPartNumberBase()));
				mapRecord.put(PART_ECL, PART_ECL.getFormatedValue(part.getPartECL()));
				mapRecord.put(PART_VERSION, PART_VERSION.getFormatedValue(part.getPartVersion()));
				mapRecord.put(PART_NAME, PART_NAME.getFormatedValue(partDetail.get("name")));
				mapRecord.put(PRIMARY_SUPPLIER, PRIMARY_SUPPLIER.getFormatedValue( partSupplier.getPrimarySupplier() ));
				mapRecord.put(SUPPLIER_CODE, SUPPLIER_CODE.getFormatedValue(partSupplier.getCode()));
				mapRecord.put(SUPPLIER_NAME, SUPPLIER_NAME.getFormatedValue(partSupplier.getName()));

				listRecords.add(mapRecord);
			}
		}
		this.getIntegrationParams().addPartNumbersPerPlant(plantCode, partNumbersByPlant);

		return listRecords;
	}
	
	private Part generateNewPart(List<String> cacheOperations) {
		int billingCodeIndex;
		int totalBuildSequences;
		Integer partNumberBase;
		String strPartECL;
		String strPartVersion;
		List<String> billingCodes = new ArrayList<>();
		
		partNumberBase = this.getNextIntRandom(
				getMaxIntValueByLength(DiscreteFieldsMGOC.PART_NUMBER.getSize()),
				100
			);
		strPartECL = Integer.toString(this.getNextIntRandom( 10, 1 ));
		strPartVersion = Integer.toString(this.getNextIntRandom( 10, 1 ));

		strPartECL = StringUtils.leftPad(strPartECL, PART_ECL.getSize(), '0');
		strPartVersion = StringUtils.leftPad(strPartVersion, PART_VERSION.getSize(), '0');

		if(cacheOperations != null && !cacheOperations.isEmpty()) {
			totalBuildSequences = super.getNextIntRandom(MAX_BUILD_SEQUENCES, 2);
			billingCodeIndex = getNextIntRandom(cacheOperations.size() - 1, 0);

			for(int i=0;i<totalBuildSequences;i++) {
				billingCodes.add(cacheOperations.get(billingCodeIndex++));

				billingCodeIndex = billingCodeIndex % cacheOperations.size();
			}
		}
		
		return new Part(
				partNumberBase.toString(),
				strPartECL,
				strPartVersion,
				super.getNextIntRandom(MAX_BUILD_SEQUENCE_SEED, 1) * 100,
				billingCodes
		);
	}
	
	@Override
	public String getHeader() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(MGO_C_DATE_FORMAT);
		
		return String.format(
				"1DISCRETE PART HEADER          %s000001000000010",
				dateFormat.format(new Date())
			);
	}

	@Override
	public String getTrailer() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(MGO_C_DATE_FORMAT);
		
		return String.format(
				"1DISCRETE PART TRAILER         %s000001000000010",
				dateFormat.format(new Date())
			);
	}

	private List<String> initCacheOperations(String plantCode) {
		String billingCode = null;
		ResultSet resultSet = null;
		StringBuilder queryBuilder = new StringBuilder();
		List<String> cacheOperations = new ArrayList<String>();

		queryBuilder.append("select distinct g.billingcode ");
		queryBuilder.append("  from groupjdobase g ");
		queryBuilder.append("  join groupjdobase gg ");
		queryBuilder.append("    on g.field_02 = gg.jdoid ");
		queryBuilder.append(" where g.type0 = 5");
		queryBuilder.append("   and g.billingcode is not null");
		queryBuilder.append("   and g.archived = 0");
		queryBuilder.append("   and gg.CODE = ?");

		try {
			resultSet = PoolConnection.getInstance().execute(queryBuilder.toString(), plantCode);
			while(resultSet.next()) {
				billingCode = resultSet.getString(1).replaceAll("[ ]{2,}", " ").trim();
				if(StringUtils.isNotEmpty(billingCode) && isValidBillingCode(billingCode)) {
					cacheOperations.add(billingCode);
				}
			}
		} catch (SQLException e) {
			logger.error("Couldn't execute the query {}", queryBuilder.toString());
		} finally {
			PoolConnection.getInstance().close(resultSet);
		}

		logger.info("{} groups of type Operation where recovered from plant code {}", cacheOperations.size(), plantCode);

		return cacheOperations;
	}

	private boolean isValidBillingCode(String billinCode) {
		boolean isValid = true;
		int count = 0;
		String[] segments = billinCode.split(" ");
		PartGroupMgoC.PartDGOFieldsMGOC[] dgoFields = {
				PartGroupMgoC.PartDGOFieldsMGOC.DEPARTMENT,
				PartGroupMgoC.PartDGOFieldsMGOC.GROUP,
				PartGroupMgoC.PartDGOFieldsMGOC.OPERATION
		};

		if(segments.length > 3) {
			return false;
		}

		while(isValid && count < segments.length && count < dgoFields.length) {
			isValid = dgoFields[count].getSize() >= segments[count].length();
			count++;
		}

		return isValid;
	}

}
