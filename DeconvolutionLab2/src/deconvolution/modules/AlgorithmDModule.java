package deconvolution.modules;

import javax.swing.JSplitPane;

import bilib.component.HTMLPane;
import bilib.table.CustomizedTable;
import bilib.tools.NumFormat;
import deconvolution.Deconvolution;
import deconvolution.Features;
import deconvolution.algorithm.AbstractAlgorithm;
import deconvolution.algorithm.AbstractAlgorithmPanel;
import deconvolution.algorithm.Algorithm;
import deconvolution.algorithm.Controller;
import deconvolutionlab.monitor.Monitors;
import fft.AbstractFFT;
import fft.FFT;
import signal.ComplexSignal;
import signal.RealSignal;
import signal.SignalCollector;

public class AlgorithmDModule extends AbstractDModule implements Runnable {

	private CustomizedTable	table;
	private HTMLPane		doc;

	public AlgorithmDModule(Deconvolution deconvolution) {
		super(deconvolution);
		doc = new HTMLPane(100, 1000);
		table = new CustomizedTable(new String[] { "Features", "Values" }, false);
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, table.getPane(200, 200), doc.getPane());
	}

	public void update() {
		if (doc == null)
			return;
		if (table == null)
			return;
		
		table.removeRows();
		table.append(new String[] { "PSF", "Waiting for loading ..." });
		Thread thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
		split.setDividerLocation(300);
	}

	@Override
	public String getName() {
		return "Algorithm";
	}
	
	@Override
	public void run() {
		Features features = new Features();
		
		if (deconvolution.algo == null) {
			features.add("Algorithm", "No valid algorithm");
			return;
		}
		AbstractAlgorithm algo = deconvolution.algo;
		doc.clear();
		String name = algo.getShortnames()[0];
		AbstractAlgorithmPanel algoPanel = Algorithm.getPanel(name);
		if (algoPanel != null)
			doc.append(algoPanel.getDocumentation());
		
		if (deconvolution.image == null) {
			startAsynchronousTimer("Open image", 200);
			deconvolution.image = deconvolution.openImage();
			stopAsynchronousTimer();
		}
		
		if (deconvolution.image == null) {
			features.add("Image", "No valid input image");
			return;
		}
		if (deconvolution.pad == null) {
			features.add("Padding", "No valid padding");
			return;
		}
		if (deconvolution.apo == null) {
			features.add("Apodization", "No valid apodization");
			return;
		}
		
		if (deconvolution.psf == null) {
			startAsynchronousTimer("Open PSF", 200);
			deconvolution.psf = deconvolution.openPSF();
			stopAsynchronousTimer();
		}
		
		if (deconvolution.psf == null) {
			features.add("Image", "No valid PSF");
			return;
		}

		Controller controller = deconvolution.algo.getController();
		if (controller == null) {
			features.add("Controller", "No valid controller");
			return;
		}

		startAsynchronousTimer("Run FFT", 200);
		deconvolution.algo.setController(controller);
		AbstractFFT f = FFT.getFastestFFT().getDefaultFFT();
		double Q = Math.sqrt(2);
		if (deconvolution.image != null) {
			int mx = deconvolution.image.nx;
			int my = deconvolution.image.ny;
			int mz = deconvolution.image.nz;
			
			while (mx * my * mz > Math.pow(2, 15)) {
				mx = (int)(mx / Q);
				my = (int)(my / Q);
				mz = (int)(mz / Q);
			}
			double N = deconvolution.image.nx * deconvolution.image.ny * deconvolution.image.nz;
			double M = mx * my * mz;
			double ratio = 1;
			if (M != 0)
				ratio = (N * Math.log(N)) / (M * Math.log(M));
			
			double chrono = System.nanoTime(); 
			RealSignal x = new RealSignal("test", mx, my, mz);
			ComplexSignal c = new ComplexSignal("test", mx, my, mz);
			f.init(Monitors.createDefaultMonitor(), mx, my, mz);
			f.transform(x, c);
			SignalCollector.free(x);
			SignalCollector.free(c);
			
			chrono = (System.nanoTime() - chrono);
			features.add("Tested on", mx + "x" + my + "x" + mz);
			features.add("Estimated Time on small", NumFormat.time(chrono) );
			
			chrono = chrono * ratio * algo.getComplexityNumberofFFT();
			
			int n = algo.isIterative() ? controller.getIterationMax() : 1;
			features.add("Estimated Time", NumFormat.time(chrono) );
			features.add("Estimated Number of FFT / Transform", ""+algo.getComplexityNumberofFFT());
		}
		else 
			features.add("Estimated Time", "Error" );
		double mem = (algo.getMemoryFootprintRatio() * deconvolution.image.nx * deconvolution.image.ny * deconvolution.image.nz * 4);
		features.add("Estimated Memory", NumFormat.bytes(mem));
		features.add("Iterative", algo.isIterative()  ? "" + controller.getIterationMax() : "Direct");
		
		table.removeRows();
		for (String[] feature : features)
			table.append(feature);
		stopAsynchronousTimer();
	}

}