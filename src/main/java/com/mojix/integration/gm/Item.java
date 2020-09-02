package com.mojix.integration.gm;

import org.apache.commons.lang.StringUtils;

public final class Item {
	public enum Padding { BLANK_LEFT, BLANK_RIGHT, ZEROS_LEFT };
	
	private int beginIndex;
	private int endIndex;
	private Item next;
	private Padding padding;
	
	public Item(int beginIndex, int endIndex, Item next) {
		super();
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
		this.next = next;
		this.padding = Padding.BLANK_RIGHT;
	}

	public Item(int beginIndex, int endIndex, Item next, Padding padding) {
		super();
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
		this.next = next;
		this.padding = padding;
	}
	
	public String getItemValue(String data) {
		String formattedData;
		
		if(data.length() > this.getLenght()) {
			formattedData = data.substring(0, this.getLenght() - 1);
		} else {
			formattedData = data;
		}
		
		switch(this.padding) {
			case BLANK_LEFT:
				formattedData = StringUtils.leftPad(formattedData, this.getLenght(), ' ');
				break;
			case BLANK_RIGHT:
				formattedData = StringUtils.rightPad(formattedData, this.getLenght(), ' ');
				break;
			case ZEROS_LEFT:
				formattedData = StringUtils.leftPad(formattedData, this.getLenght(), '0');
				break;
			default:
				;
		}
		
		if(this.next != null && this.endIndex != this.next.beginIndex) {
			formattedData += StringUtils.leftPad("", this.next.beginIndex - this.endIndex, ' ');;
		}
		
		return formattedData;
	}
	
	public int getLenght() {
		return this.endIndex - this.beginIndex;
	}
	
}
