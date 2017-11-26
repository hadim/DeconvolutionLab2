
import java.util.Arrays;

import org.jtransforms.fft.FloatFFT_1D;
import org.jtransforms.fft.FloatFFT_2D;

import jcuda.jcufft.JCufft;
import jcuda.jcufft.cufftHandle;
import jcuda.jcufft.cufftType;

public class JCudaTest {

	public static void main(String args[]) {
		System.out.println("Compare FFT with JTransform and JCuFFT in 1D\n");
		fft1();

		System.out.println("\n");

		System.out.println("Compare FFT with JTransform and JCuFFT in 2D\n");
		fft2();
	}

	public static void fft2() {

		int size = 2;
		float[] input = new float[] { (float) 5, (float) 15, (float) 6, (float) 3 };
		float[][] input2d = { { (float) 5, (float) 15 }, { (float) 6, (float) 3 } };

		// JTransform
		float[] outputJTransforms = new float[input.length * 2];
		System.arraycopy(input, 0, outputJTransforms, 0, input.length);

		FloatFFT_2D fft = new FloatFFT_2D(size, size);
		fft.complexForward(outputJTransforms);

		// JCuFFT
		float[] outputJCufft = new float[input.length * 2];
		System.arraycopy(input, 0, outputJCufft, 0, input.length);

		cufftHandle plan = new cufftHandle();
		int result = JCufft.cufftPlan2d(plan, size, size, cufftType.CUFFT_C2C);
		result = JCufft.cufftExecC2C(plan, outputJCufft, outputJCufft, JCufft.CUFFT_FORWARD);
		JCufft.cufftDestroy(plan);

		System.out.println("Input 1D");
		printArray(input);
		System.out.println("Input as 2D");
		printArray(input2d);
		System.out.println("FFT JTransform");
		printArray(outputJTransforms);
		System.out.println("FFT JCuFFT");
		printArray(outputJCufft);
	}

	public static void fft1() {

		float[] input = new float[] { (float) 5, (float) 15, (float) 6, (float) 3 };

		// JTransform
		float[] outputJTransforms = new float[input.length * 2];
		System.arraycopy(input, 0, outputJTransforms, 0, input.length);

		FloatFFT_1D fft = new FloatFFT_1D(input.length);
		fft.complexForward(outputJTransforms);

		// JCuFFT
		float[] outputJCufft = new float[input.length * 2];
		System.arraycopy(input, 0, outputJCufft, 0, input.length);

		cufftHandle plan = new cufftHandle();
		int result = JCufft.cufftPlan1d(plan, input.length, cufftType.CUFFT_C2C, 1);
		result = JCufft.cufftExecC2C(plan, outputJCufft, outputJCufft, JCufft.CUFFT_FORWARD);
		JCufft.cufftDestroy(plan);

		System.out.println("Input 1D");
		printArray(input);
		System.out.println("FFT JTransform");
		printArray(outputJTransforms);
		System.out.println("FFT JCuFFT");
		printArray(outputJCufft);
	}

	private static void printArray(float[] array) {
		System.out.println("----");
		for (int i = 0; i < array.length; i++) {
			System.out.println(array[i]);
		}
		System.out.println("----");
	}

	private static void printArray(float[][] array) {
		System.out.println("----");
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				System.out.print(array[i][j] + ", ");
			}
			System.out.println();
		}
		System.out.println("----");
	}

	public static void copy2DArray(float[][] src, float[][] dest) {
		for (int i = 0; i < src.length; i++) {
			System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
		}
	}

	public static void fill(float[][] src, int value) {
		for (int i = 0; i < src.length; i++) {
			Arrays.fill(src[i], value);
		}
	}
}