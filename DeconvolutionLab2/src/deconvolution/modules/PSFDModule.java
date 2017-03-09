package deconvolution.modules;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.JSplitPane;

import bilib.component.CustomizedTable;
import bilib.component.JPanelImage;
import bilib.tools.NumFormat;
import deconvolution.Deconvolution;
import deconvolution.Features;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import signal.Constraint;
import signal.RealSignal;
import signal.SignalCollector;

public class PSFDModule extends AbstractDModule implements Runnable {

	private JPanelImage		pnImage;
	private CustomizedTable	table;

	public PSFDModule(Deconvolution deconvolution) {
		super(deconvolution);
		pnImage = new JPanelImage();
		table = new CustomizedTable(new String[] { "Features", "Values" }, false);
		pnImage.setPreferredSize(new Dimension(300, 300));
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, table.getPane(300, 300), pnImage);
	}

	public void update() {
		split.setDividerLocation(300);
		if (pnImage == null)
			return;
		if (table == null)
			return;
		table.removeRows();
		table.append(new String[] { "PSF", "Waiting for loading ..." });
		Thread thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	@Override
	public String getName() {
		return "PSF";
	}

	@Override
	public void run() {
		Features features = new Features();

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
			features.add("PSF", "No valid PSF");
			return;
		}

		startAsynchronousTimer("Preprocessing PSF", 200);

		float stati[] = deconvolution.psf.getStats();
		int sizi = deconvolution.psf.nx * deconvolution.psf.ny * deconvolution.psf.nz;
		float totali = stati[0] * sizi;
		features.add("<html><b>Orignal PSF</b></html>", "");
		features.add("Size", deconvolution.psf.dimAsString() + " " + NumFormat.bytes(sizi * 4));
		features.add("Mean (stdev)", NumFormat.nice(stati[0]) + " (" + NumFormat.nice(stati[3]) + ")");
		features.add("Min ... Max", NumFormat.nice(stati[1]) + " ... " + NumFormat.nice(stati[2]));
		features.add("Energy (int)", NumFormat.nice(stati[5]) + " (" + NumFormat.nice(totali) + ")");
		table.removeRows();
		for (String[] feature : features)
			table.append(feature);

		RealSignal h = deconvolution.psf.changeSizeAs(deconvolution.image);
		h.normalize(deconvolution.norm);
		float stats[] = h.getStats();
		int sizs = h.nx * h.ny * h.nz;
		float totals = stats[0] * sizs;
		features.add("<html><b>Working PSF</b></html>", "");
		features.add("Size", h.dimAsString() + " " + NumFormat.bytes(sizs * 4));
		features.add("Mean (stdev)", NumFormat.nice(stats[0]) + " (" + NumFormat.nice(stats[3]) + ")");
		features.add("Min Max", NumFormat.nice(stats[1]) + " " + NumFormat.nice(stats[2]));
		features.add("Energy (int)", NumFormat.nice(stats[5]) + " (" + NumFormat.nice(totals) + ")");
		features.add("<html><b>Information</b></html>", "");
		features.add("Size Increase ", NumFormat.nice((double) (sizs - sizi) / sizi * 100.0));
		features.add("Energy Lost", NumFormat.nice((stats[5] - stati[5]) / stati[5] * 100));
		SignalCollector.free(h);
		table.removeRows();
		for (String[] feature : features)
			table.append(feature);
		
		pnImage.setImage(deconvolution.psf.preview());
		stopAsynchronousTimer();
	}

}