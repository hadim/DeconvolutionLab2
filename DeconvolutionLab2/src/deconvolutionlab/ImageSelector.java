package deconvolutionlab;

import imagej.IJImageSelector;

public class ImageSelector {

	private PlatformImageSelector pis;
	
	public ImageSelector(Platform platform) {
		
		if (platform == Platform.IMAGEJ)
			pis = new IJImageSelector();
	
		else if (platform == Platform.ICY)
			pis = new plugins.sage.deconvolutionlab.IcyImageSelector();
		else
			pis = new LabImageSelector();
	}
	
	public PlatformImageSelector getImageSelector() {
		return pis;
	}
}
