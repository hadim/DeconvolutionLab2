import net.imagej.ImageJ;

public class TestRunDeconvolution {

	public static void main(String args[]) {

		final ImageJ ij = new ImageJ();
		ij.launch(args);

		String image = " -image file /home/hadim/Documents/Code/Postdoc/ij/testdata/deconvolution/Input.tif";
		String psf = " -psf /home/hadim/Documents/Code/Postdoc/ij/testdata/deconvolution/PSF.tif";
		String algorithm = " -algorithm RLTV 10 0.1000";
		String parameters = "";
		parameters += " -fft JCuFFT (GPU support)";
		// parameters += " -fft Academic";
		new DeconvolutionLab2(image + psf + algorithm + parameters);

	}

}