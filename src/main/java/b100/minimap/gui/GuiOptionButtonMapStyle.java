package b100.minimap.gui;

import b100.minimap.config.IntegerOption;

public class GuiOptionButtonMapStyle extends GuiOptionButtonInteger {

	public GuiOptionButtonMapStyle(GuiScreen screen, IntegerOption option) {
		super(screen, option);
	}
	
	@Override
	public String getOptionValueString() {
		if(option.value == 0) return "Blue";
		if(option.value == 1) return "Red";
		if(option.value == 2) return "Green";
		if(option.value == 3) return "Black";
		if(option.value == 4) return "Rei";
		if(option.value == 5) return "Zan";
		if(option.value == 6) return "Custom";
		
		return super.getOptionValueString();
	}
	
	

}
