package com.example.dcwa.auxclasses;

import com.example.dcwa.mainfeatures.TouchAreaView.ColorPicker;

public class RGBColor {
	
	public int r, g, b;
	
	public RGBColor(ColorPicker color) {
		switch (color) {
		case blue:
			r = 0x99;
			g = 0xCC;
			b = 0xFF;
			break;
		case green:
			r = 0x99;
			g = 0xFF;
			b = 0x99;
			break;
		case grey1:
			r = 0xC0;
			g = 0xC0;
			b = 0xC0;
			break;
		case grey2:
			r = 0xA0;
			g = 0xA0;
			b = 0xA0;
			break;
		case orange:
			r = 0xFF; 
			g = 0xCC;
			b = 0x99;
			break;
		case pink:
			r = 0xFF;
			g = 0x99;
			b = 0xFF;
			break;
		case purple:
			r = 0xE5;
			g = 0xCC;
			b = 0xFF;
			break;
		case yellow:
			r = 0xFF;
			g = 0xFF;
			b = 0x99;
			break;
		default:
			break;
		}
	}
	
}
