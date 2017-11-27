

public class TestRunDeconvolution {

	public static void main(String args[]) {
		net.imagej.Main.launch(args);

		String image = " -image file /home/hadim/Documents/Code/Postdoc/ij/testdata/deconvolution/Input.tif";
		String psf = " -psf file /home/hadim/Documents/Code/Postdoc/ij/testdata/deconvolution/PSF.tif";
		String algorithm = " -algorithm RLTV 10 0.1000";
		String parameters = "";
		parameters += " -fft JCuFFT";
		//parameters += " -fft Academic";
		new DeconvolutionLab2(image + psf + algorithm + parameters);
	}

}