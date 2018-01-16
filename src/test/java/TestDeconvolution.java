import ij.IJ;
import net.imagej.ImageJ;

public class TestDeconvolution {

	public static void main(String args[]) {

		final ImageJ ij = new ImageJ();
		ij.launch(args);

		IJ.runPlugIn("DeconvolutionLab2_Run", "");
	}

}