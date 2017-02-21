package deconvolution;

import java.util.ArrayList;

public class Features extends ArrayList<String[]>{
	
	public Features() {
		super();
	}
	

	public void add(String feature, String value) {
		add(new String[] {feature, value});
	}

}
