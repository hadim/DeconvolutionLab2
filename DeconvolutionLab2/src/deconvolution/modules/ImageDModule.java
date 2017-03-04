package deconvolution.modules;

import java.awt.Dimension;

import javax.swing.JSplitPane;

import deconvolution.Deconvolution;
import deconvolution.Features;
import lab.component.CustomizedTable;
import lab.component.JPanelImage;
import lab.tools.NumFormat;
import signal.RealSignal;
import signal.SignalCollector;

public class ImageDModule extends AbstractDModule implements Runnable {

	private JPanelImage pnImage;
	private CustomizedTable table;
	
	public ImageDModule(Deconvolution deconvolution) {
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
		table.append(new String[] {"Image", "Waiting for loading ..."});
		Thread thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	@Override
	public String getName() {
		return "Image";
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

		startAsynchronousTimer("Preprocessing image", 200);

		float stati[] = deconvolution.image.getStats();
		int sizi = deconvolution.image.nx * deconvolution.image.ny * deconvolution.image.nz;
		float totali = stati[0] * sizi;
		features.add("<html><b>Orignal Image</b></html>", "");
		features.add("Size", deconvolution.image.dimAsString() + " " + NumFormat.bytes(sizi*4));
		features.add("Mean (stdev)", NumFormat.nice(stati[0])  + " (" + NumFormat.nice(stati[3]) + ")");
		features.add("Min ... Max", NumFormat.nice(stati[1]) + " ... " + NumFormat.nice(stati[2]));
		features.add("Energy (int)", NumFormat.nice(stati[5])  + " (" + NumFormat.nice(totali) + ")");
		table.removeRows();
		for (String[] feature : features)
			table.append(feature);
		
		RealSignal signal = deconvolution.pad.pad(deconvolution.monitors, deconvolution.image);
		deconvolution.apo.apodize(deconvolution.monitors, signal);
		float stats[] = signal.getStats();
		int sizs = signal.nx * signal.ny * signal.nz;
		float totals = stats[0] * sizs;

		features.add("<html><b>Working Image</b></html>", "");
		features.add("Size", signal.dimAsString() + " " + NumFormat.bytes(sizs*4));
		features.add("Mean (stdev)", NumFormat.nice(stats[0])  + " (" + NumFormat.nice(stats[3]) + ")");
		features.add("Min Max", NumFormat.nice(stats[1]) + " " + NumFormat.nice(stats[2]));
		features.add("Energy (int)", NumFormat.nice(stats[5])  + " (" + NumFormat.nice(totals) + ")");
		features.add("<html><b>Information</b></html>", "");
		features.add("Size Increase ", NumFormat.nice((double)(sizs-sizi)/sizi*100.0));
		features.add("Energy Lost", NumFormat.nice((stats[5]-stati[5])/stati[5]*100));
		SignalCollector.free(signal);
		table.removeRows();
		for (String[] feature : features)
			table.append(feature);
		
		pnImage.setImage(deconvolution.image.preview());
		stopAsynchronousTimer();

	}	
}