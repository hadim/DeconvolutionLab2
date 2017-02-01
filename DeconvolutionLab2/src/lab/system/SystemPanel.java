package lab.system;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lab.component.CustomizedTable;
import lab.component.SpinnerRangeInteger;
import lab.tools.NumFormat;

public class SystemPanel extends JPanel implements ChangeListener {

	private CustomizedTable	    table;
	private TimerTask	        updater	    = new Updater();
	private Timer	            timer;
	private SpinnerRangeInteger	spnRefresh;
	private int	                refreshTime	= 1000;

	public SystemPanel(int refreshTime) {
		super(new BorderLayout());
		this.refreshTime = refreshTime;
		spnRefresh = new SpinnerRangeInteger(refreshTime, 1, 360000, 100);
		table = new CustomizedTable(new String[] { "Manager", "Feature", "Value" }, false);
		table.update(getFeatures());
		table.setFillsViewportHeight(true);
		JPanel pn = new JPanel(new FlowLayout());
		pn.add(new JLabel("Refresh time"));
		pn.add(spnRefresh);
		pn.add(new JLabel("ms"));
		JScrollPane scroll = new JScrollPane(table);
		scroll.setVisible(true);
		add(pn, BorderLayout.NORTH);
		add(scroll, BorderLayout.CENTER);
		timer = new Timer();
		timer.schedule(updater, 1, refreshTime);
		spnRefresh.addChangeListener(this);
	}
	
	public static void show(int w, int h) {
		JFrame frame = new JFrame("System Info");
		frame.add(new SystemPanel(1000));
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == spnRefresh)
			restart(spnRefresh.get());
	}

	public void restart(int refreshTime) {
		this.refreshTime = refreshTime;
		if (updater != null) {
			updater.cancel();
			updater = null;
		}
		updater = new Updater();
		timer.schedule(updater, 0, refreshTime);
	}

	private class Updater extends TimerTask {
		@Override
		public void run() {
			if (table != null) {
				table.update(getFeatures());
			}
		}
	}

	public int getSystemTime() {
		return refreshTime;
	}

	public ArrayList<String[]> getFeatures() {

		ArrayList<String[]> features = new ArrayList<String[]>();
		String f = "";
		features.add(new String[] { "Properties", "OS", "" + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") });
		features.add(new String[] { "Properties", "Java Version", "" + System.getProperty("java.version") });
		f = "java.class.path";
		features.add(new String[] { "Properties", f, "" + System.getProperty(f) });
		f = "java.home";
		features.add(new String[] { "Properties", f, "" + System.getProperty(f) });
		f = "user.dir";
		features.add(new String[] { "Properties", f, "" + System.getProperty(f) });
		f = "user.home";
		features.add(new String[] { "Properties", f, "" + System.getProperty(f) });
		f = "user.name";
		features.add(new String[] { "Properties", f, "" + System.getProperty(f) });

		Runtime runt = Runtime.getRuntime();
		features.add(new String[] { "JVM", "Available processors", "" + runt.availableProcessors() });
		features.add(new String[] { "JVM", "Initial Memory (-Xms)", NumFormat.bytes(runt.freeMemory()) });
		features.add(new String[] { "JVM", "Maximum Memory (-Xmx)", NumFormat.bytes(runt.maxMemory()) });
		features.add(new String[] { "JVM", "Total Used Memory", NumFormat.bytes(runt.totalMemory()) });

		MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
		MemoryUsage heapU = mem.getHeapMemoryUsage();
		MemoryUsage nonhU = mem.getNonHeapMemoryUsage();

		features.add(new String[] { "Memory", "Heap Used", NumFormat.bytes(heapU.getUsed()) });
		features.add(new String[] { "Memory", "Heap Init", NumFormat.bytes(heapU.getInit()) });
		features.add(new String[] { "Memory", "Heap Max ", NumFormat.bytes(heapU.getMax()) });

		features.add(new String[] { "Memory", "NonHeap Used", NumFormat.bytes(nonhU.getUsed()) });
		features.add(new String[] { "Memory", "NonHeap Init", NumFormat.bytes(nonhU.getInit()) });
		features.add(new String[] { "Memory", "NonHeap Max ", NumFormat.bytes(nonhU.getMax()) });

		RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
		features.add(new String[] { "Runtime", "Library Path", rt.getLibraryPath() });
		features.add(new String[] { "Runtime", "Uptime", "" + NumFormat.time(rt.getUptime()) });
		features.add(new String[] { "Runtime", "Name", rt.getName() });
		features.add(new String[] { "Runtime", "Vm Version", rt.getVmVersion() });
		for (String input : rt.getInputArguments())
			features.add(new String[] { "Runtime", "Input Arguments", input });

		File[] roots = File.listRoots();
		for (File root : roots) {
			features.add(new String[] { "FileSystem", "Root Path", root.getAbsolutePath() });
			features.add(new String[] { "FileSystem", "Total Space", NumFormat.bytes(root.getTotalSpace()) });
			features.add(new String[] { "FileSystem", "Usable Space", NumFormat.bytes(root.getUsableSpace()) });
		}

		ClassLoadingMXBean loader = ManagementFactory.getClassLoadingMXBean();

		features.add(new String[] { "ClassLoading", "Loaded Class", "" + loader.getLoadedClassCount() });

		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		for (Method method : os.getClass().getDeclaredMethods()) {
			method.setAccessible(true);
			if (method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers())) {
				try {
					String name = split(method.getName());
					features.add(new String[] { "OS", name, "" + method.invoke(os).toString() });
				}
				catch (Exception e) {
				}
			}
		}
		return features;
	}

	private static String split(String name) {
		String func = name.substring(3);
		return func.replaceAll("(\\p{Ll})(\\p{Lu})", "$1 $2");
	}

}
