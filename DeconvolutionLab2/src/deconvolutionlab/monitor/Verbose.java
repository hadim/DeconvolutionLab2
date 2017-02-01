package deconvolutionlab.monitor;

public enum Verbose {
	Mute, Quiet, Log, Prolix;
	
	public static Verbose getByName(String name) {
		String n = name.toLowerCase().trim();
		if (n.equals("log"))
			return Log;
		if (n.equals("prolix"))
			return Log;
		if (n.equals("quiet"))
			return Quiet;
		if (n.equals("mute"))
			return Mute;
		return Log;
	}
}
