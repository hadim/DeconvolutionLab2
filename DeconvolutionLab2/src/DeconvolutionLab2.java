import java.io.File;

import deconvolution.Command;
import deconvolution.Deconvolution;
import deconvolutionlab.Config;
import deconvolutionlab.Constants;
import deconvolutionlab.Lab;
import deconvolutionlab.Platform;
import deconvolutionlab.dialog.LabDialog;
import deconvolutionlab.monitor.Monitors;

public class DeconvolutionLab2 {

	public static String ack = Constants.name + " " + Constants.version + " " + Constants.copyright;

	public static void main(String arg[]) {

		System.out.println(ack);
		Lab.getInstance(Platform.STANDALONE);

		if (arg.length == 0) {
			lab(arg);
			return;
		}

		String flag = arg[0].trim().toLowerCase();
		if (flag.equalsIgnoreCase("help")) {
			help();
			return;
		}

		if (flag.equalsIgnoreCase("lab")) {
			lab(arg);
		}

		if (flag.equalsIgnoreCase("fft")) {
			Lab.checkFFT(Monitors.createDefaultMonitor());
		}

		if (flag.equalsIgnoreCase("run")) {
			String cmd = "";
			for (int i = 1; i < arg.length; i++)
				cmd += arg[i] + " ";
			new Deconvolution(cmd).deconvolve(true);
		}

		if (flag.equalsIgnoreCase("launch")) {
			String cmd = "";
			for (int i = 1; i < arg.length; i++)
				cmd += arg[i] + " ";
			new Deconvolution(cmd).launch("", true);
		}
	}

	private static void lab(String arg[]) {
		String config = System.getProperty("user.dir") + File.separator + "DeconvolutionLab2.config";
		if (arg.length >= 2) {
			String filename = arg[1].trim();
			File file = new File(filename);
			if (file.exists())
				if (file.isFile())
					if (file.canRead())
						config = filename;
		}
		Config.getInstance(config);
		LabDialog dialog = new LabDialog();
		dialog.setVisible(true);
	}

	private static void help() {
		System.out.println("More info:" + Constants.url);
		System.out.println("Syntax:");
		System.out.println("java -jar DeconvolutionLab_2.jar lab [config.txt]");
		System.out.println("java -jar DeconvolutionLab_2.jar run {command} ...");
		System.out.println("java -jar DeconvolutionLab_2.jar {command} ...");
		System.out.println("java -jar DeconvolutionLab_2.jar fft");
		System.out.println("java -jar DeconvolutionLab_2.jar info");
		System.out.println("java -jar DeconvolutionLab_2.jar help");
		System.out.println("{command} is the full command line for running a deconvolution");
		System.out.print("Keywords of {command}: ");
		for (String keyword : Command.keywords)
			System.out.print(keyword + " ");

	}

	public DeconvolutionLab2(String cmd) {
		System.out.println("cmd: " + cmd);
		deconvolutionlab.Lab.getInstance(Platform.STANDALONE);
		String config = System.getProperty("user.dir") + File.separator + "DeconvolutionLab2.config";
		Config.getInstance(config);
		new Deconvolution(cmd).deconvolve(false);
	}
}
