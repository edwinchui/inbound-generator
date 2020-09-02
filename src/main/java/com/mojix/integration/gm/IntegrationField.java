package com.mojix.integration.gm;

import org.apache.commons.lang.StringUtils;

import com.mojix.integration.gm.Item.Padding;

public interface IntegrationField {
	
	default String getFormatedValue(String value) {
		String formattedData = value;
		
		if(value.length() > this.getSize()) {
			formattedData = value.substring(0, this.getSize());
		}
		
		switch (this.getPadding()) {
			case BLANK_LEFT:
				formattedData = StringUtils.leftPad(formattedData, this.getSize(), ' ');
				break;
			case BLANK_RIGHT:
				formattedData = StringUtils.rightPad(formattedData, this.getSize(), ' ');
				break;
			case ZEROS_LEFT:
				formattedData = StringUtils.leftPad(formattedData, this.getSize(), '0');
				break;
			default:
				;
		}
		
		return formattedData;
	}
	
	default int getSize() {
		return getEndIndex() - getBeginIndex() + 1;
	}
	
	int getBeginIndex();

	int getEndIndex();

	Padding getPadding();

	String getLabelName();
}