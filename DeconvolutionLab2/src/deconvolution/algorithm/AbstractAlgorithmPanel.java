package deconvolution.algorithm;

import javax.swing.JPanel;

public abstract class AbstractAlgorithmPanel {

	public abstract JPanel getPanelParameters();

	public abstract String getCommand();

	public abstract String getName();

	public abstract String getShortname();

	public abstract String getDocumentation();

	public boolean isNamed(String name) {
		if (name.equals(getShortname().toLowerCase()))
			return true;
		if (name.equals(getName().toLowerCase()))
			return true;
		return false;
	}

}
