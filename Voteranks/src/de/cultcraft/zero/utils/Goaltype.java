package de.cultcraft.zero.utils;

public enum Goaltype {

	BIGGER,SMALLER,SAME,MODULO;
	
	public String typeToString() {
		
		if(this.equals(Goaltype.BIGGER)) {
			return ">";
		}else if(this.equals(Goaltype.MODULO)) {
			return "%";
		}else if(this.equals(Goaltype.SAME)) {
			return "=";
		}else if(this.equals(Goaltype.SMALLER)) {
			return "<";
		}else {
			return "error";
		}		
	}
	
}
