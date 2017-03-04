/*
 * DeconvolutionLab2
 * 
 * Conditions of use: You are free to use this software for research or
 * educational purposes. In addition, we expect you to include adequate
 * citations and acknowledgments whenever you present or publish results that
 * are based on it.
 * 
 * Reference: DeconvolutionLab2: An Open-Source Software for Deconvolution
 * Microscopy D. Sage, L. Donati, F. Soulez, D. Fortun, G. Schmit, A. Seitz,
 * R. Guiet, C. Vonesch, M Unser, Methods of Elsevier, 2017.
 */

/*
 * Copyright 2010-2017 Biomedical Imaging Group at the EPFL.
 * 
 * This file is part of DeconvolutionLab2 (DL2).
 * 
 * DL2 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DL2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * DL2. If not, see <http://www.gnu.org/licenses/>.
 */

package deconvolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import deconvolution.algorithm.AbstractAlgorithm;
import deconvolution.algorithm.Algorithm;
import deconvolution.algorithm.Controller;
import deconvolutionlab.Output;
import deconvolutionlab.OutputCollection;
import deconvolutionlab.Output.View;
import deconvolutionlab.modules.AbstractModule;
import deconvolutionlab.modules.CommandModule;
import deconvolutionlab.monitor.Verbose;
import fft.AbstractFFT;
import fft.AbstractFFTLibrary;
import fft.FFT;
import lab.tools.NumFormat;
import signal.Constraint;
import signal.Operations;
import signal.apodization.AbstractApodization;
import signal.apodization.Apodization;
import signal.apodization.UniformApodization;
import signal.padding.AbstractPadding;
import signal.padding.NoPadding;
import signal.padding.Padding;
import wavelets.Wavelets;

public class Command {

	public static String			keywords[]	= { "-image", "-psf", "-algorithm", "-path", "-disable", "-verbose", "-monitor", "-display", "-multithreading", "-system", "-stats", "-constraint", "-time", "-residu", "-reference", "-out", "-pad", "-apo", "-norm", "-fft", "-epsilon" };

	private static AbstractModule	modules[];
	private static CommandModule	command;

	public static void active(AbstractModule[] m, CommandModule c) {
		modules = m;
		command = c;
	}

	public static String command() {
		if (modules == null)
			return "";
		String cmd = "";

		for (AbstractModule m : modules)
			cmd += m.getCommand() + " ";

		if (command != null)
			command.setCommand(cmd);

		return cmd;
	}
	
	public static void decode(String command, Deconvolution deconvolution) {
		
		AbstractAlgorithm algo = Algorithm.getDefaultAlgorithm();
		boolean flagSystem = true;
		boolean flagDisplay = true;
		boolean flagMultithreading = true;
		int monitor = 3;
		int stats = 0;
		Verbose verbose = Verbose.Log;
		String path = System.getProperty("user.dir");
		Controller controller = new Controller();
		OutputCollection outs = new OutputCollection();
		Padding pad = new Padding();
		Apodization apo = new Apodization();
		double norm = 1.0;
		AbstractFFTLibrary fft = FFT.getFastestFFT();

		ArrayList<Token> tokens = parse(command);
		for (Token token : tokens) {
			if (token.keyword.equalsIgnoreCase("-algorithm"))
				algo = Command.decodeAlgorithm(token, controller);

			if (token.keyword.equalsIgnoreCase("-path") && !token.parameters.equalsIgnoreCase("current"))
				path = token.parameters;

			if (token.keyword.equalsIgnoreCase("-monitor"))
				monitor = decodeMonitor(token);
			if (token.keyword.equalsIgnoreCase("-stats"))
				stats = decodeStats(token);
			if (token.keyword.equalsIgnoreCase("-system")) 
				flagSystem = decodeSystem(token);

			if (token.keyword.equalsIgnoreCase("-display")) 						
				flagDisplay = decodeDisplay(token);
			
			if (token.keyword.equalsIgnoreCase("-multithreading")) 						
				flagMultithreading = decodeMultithreading(token);

			if (token.keyword.equalsIgnoreCase("-verbose"))
				verbose = Verbose.getByName(token.parameters);

			if (token.keyword.equalsIgnoreCase("-fft"))
				fft = FFT.getLibraryByName(token.parameters);

			if (token.keyword.equalsIgnoreCase("-pad"))
				pad = decodePadding(token);
			
			if (token.keyword.equalsIgnoreCase("-apo"))
				apo =  decodeApodization(token);
			
			if (token.keyword.equalsIgnoreCase("-norm"))
				norm = decodeNormalization(token);
			
			if (token.keyword.equalsIgnoreCase("-constraint"))
				decodeController(token, controller);
			
			if (token.keyword.equalsIgnoreCase("-time"))
				decodeController(token, controller);
			
			if (token.keyword.equalsIgnoreCase("-residu"))
				decodeController(token, controller);
			
			if (token.keyword.equalsIgnoreCase("-reference"))
				decodeController(token, controller);

			if (token.keyword.equalsIgnoreCase("-epsilon"))
				Operations.epsilon = NumFormat.parseNumber(token.parameters, 1e-6);

			if (token.keyword.equals("-out")) {
				Output out = decodeOut(token);
				if (out != null)
					outs.add(out);
			}
		}
		
		deconvolution.setAlgorithm(algo, controller);
		deconvolution.setPath(path);
		deconvolution.setNormalization(norm);
		deconvolution.setPadding(pad);
		deconvolution.setApodization(apo);
		deconvolution.setOutput(outs);
		deconvolution.setVerbose(verbose);
		deconvolution.setFFT(fft);
		deconvolution.setMonitor(monitor);
		deconvolution.setStats(stats);
		deconvolution.setFlags(flagDisplay, flagMultithreading, flagSystem);
	}
	
	/**
	 * This methods first segments the command line, then create all the tokens
	 * of the command line
	 * 
	 * @param command
	 *            Command line
	 * @return the list of tokens extracted from the command line
	 */
	public static ArrayList<Token> parse(String command) {

		ArrayList<CommandSegment> segments = new ArrayList<CommandSegment>();
		for (String keyword : keywords)
			segments.addAll(findSegment(command, keyword));
		Collections.sort(segments);

		ArrayList<Token> tokens = new ArrayList<Token>();
		for (int i = 0; i < segments.size(); i++) {
			String keyword = segments.get(i).keyword;
			int begin = segments.get(i).index + keyword.length() + 1;
			int end = (i < segments.size() - 1 ? segments.get(i + 1).index : command.length());
			Token token = new Token(keyword, command, begin, end);
			tokens.add(token);
		}
		return tokens;
	}

	public static Token extract(String command, String keyword) {
		ArrayList<Token> tokens = parse(command);
		for (Token token : tokens)
			if (token.keyword.equalsIgnoreCase(keyword))
				return token;
		return (Token) null;
	}

	public static double[] parseNumeric(String line) {
		ArrayList<String> num = new ArrayList<String>();
		Pattern p = Pattern.compile("[-+]?[0-9]+[.]?[0-9]*([eE][-+]?[0-9]+)?");
		Matcher m = p.matcher(line);
		while (m.find()) {
			num.add(m.group());
		}
		double number[] = new double[num.size()];
		for (int i = 0; i < num.size(); i++)
			number[i] = Double.parseDouble(num.get(i));
		return number;
	}

	public static ArrayList<CommandSegment> findSegment(String command, String keyword) {
		ArrayList<CommandSegment> segments = new ArrayList<CommandSegment>();
		String regex = "(?<!\\w)" + keyword + "(?!\\w)";
		if (command == null)
			return segments;
		Matcher matcher = Pattern.compile(regex).matcher(command);
		while (matcher.find()) {
			segments.add(new CommandSegment(keyword, matcher.start()));
		}
		return segments;
	}

	public static String extractOptions(String command) {
		ArrayList<CommandSegment> segments = new ArrayList<CommandSegment>();
		for (String keyword : keywords)
			segments.addAll(findSegment(command, keyword));
		Collections.sort(segments);

		String options = "";
		for (int i = 0; i < segments.size(); i++) {
			String keyword = segments.get(i).keyword;
			int begin = segments.get(i).index + keyword.length() + 1;
			int end = (i < segments.size() - 1 ? segments.get(i + 1).index : command.length());
			if (keyword != "-image" && keyword != "-psf" && keyword != "-algorithm")
				options += keyword + " " + command.substring(begin, end);
		}
		return options;
	}

	public static AbstractAlgorithm decodeAlgorithm(Token token, Controller controller) {

		String option = token.option;

		AbstractAlgorithm algo = Algorithm.createAlgorithm(option);
		algo.setShortname(option);
		double params[] = parseNumeric(token.parameters);
		if (params != null) {
			algo.setParameters(params);
			if (algo.isIterative() && params.length >= 1) 
				controller.setIterationMax((int)params[0]);
		}

		if (algo.isWaveletsBased()) {
			for (String wavelet : Wavelets.getWaveletsAsArray()) {
				int pos = token.parameters.toLowerCase().indexOf(wavelet.toLowerCase());
				if (pos >= 0)
					algo.setWavelets(wavelet);
			}
		}
		return algo;
	}

	public static Output decodeOut(Token token) {
		int freq = 0;
		String line = token.parameters;
		String parts[] = token.parameters.split(" ");
		for (int i = 0; i < Math.min(2, parts.length); i++) {
			if (parts[i].startsWith("@"))
				freq = (int) NumFormat.parseNumber(parts[i], 0);
		}

		String p = token.parameters.toLowerCase();
		Output out = null;
		if (p.startsWith("stack"))
			out = new Output(View.STACK, freq, line.substring("stack".length(), line.length()));
		if (p.startsWith("series"))
			out = new Output(View.SERIES, freq, line.substring("series".length(), line.length()));
		if (p.startsWith("mip"))
			out = new Output(View.MIP, freq, line.substring("mip".length(), line.length()));
		if (p.startsWith("ortho"))
			out = new Output(View.ORTHO, freq, line.substring("ortho".length(), line.length()));
		if (p.startsWith("figure"))
			out = new Output(View.FIGURE, freq, line.substring("figure".length(), line.length()));
		if (p.startsWith("planar"))
			out = new Output(View.PLANAR, freq, line.substring("planar".length(), line.length()));

		return out;
	}

	public static void decodeController(Token token, Controller controller) {
		String line = token.parameters;
		if (token.parameters.startsWith("@")) {
			String parts[] = token.parameters.split(" ");
			if (parts.length >= 1) {
				line = token.parameters.substring(parts[0].length(), token.parameters.length()).trim();
			}
		}

		if (token.keyword.equals("-constraint"))
			controller.setConstraint(Constraint.getByName(line.trim()));
		else if (token.keyword.equals("-residu"))
			controller.setResiduStop(NumFormat.parseNumber(line, -1));
		else if (token.keyword.equals("-reference"))
			controller.setReference(line);
		else if (token.keyword.equals("-time"))
			controller.setTimeStop(NumFormat.parseNumber(line, Double.MAX_VALUE));

	}

	public static double decodeNormalization(Token token) {
		if (token.parameters.toLowerCase().endsWith("no"))
			return 0;
		else
			return NumFormat.parseNumber(token.parameters, 1);
	}

	public static int decodeMonitor(Token token) {
		String parts[] = token.parameters.toLowerCase().split(" ");
		int m = 0;
		for(String p : parts) {
			if (p.startsWith("no"))
				return 0;
			if (p.equals("false"))
				return 0;
			if (p.equals("0"))
				return 0;
			if (p.equals("1"))
				return 1;
			if (p.equals("2"))
				return 2;
			if (p.equals("3"))
				return 3;
			if (p.equals("console"))
				m += 1;
			if (p.equals("table"))
				m += 2;
		}
		return m;
	}
	
	public static int decodeStats(Token token) {
		String parts[] = token.parameters.toLowerCase().split(" ");
		int m = 0;
		for(String p : parts) {
			if (p.startsWith("no"))
				return 0;
			if (p.equals("false"))
				return 0;
			if (p.equals("0"))
				return 0;
			if (p.equals("1"))
				return 1;
			if (p.equals("2"))
				return 2;
			if (p.equals("3"))
				return 3;
			if (p.equals("show"))
				m += 1;
			if (p.equals("save"))
				m += 2;
		}
		return m;
	}

	public static boolean decodeSystem(Token token) {
		String p = token.parameters.toLowerCase();
		if (p.startsWith("no"))
			return false;
		if (p.equals("0"))
			return false;
		if (p.equals("false"))
			return false;
		return true;
	}

	public static boolean decodeDisplay(Token token) {
		String p = token.parameters.toLowerCase();
		if (p.startsWith("no"))
			return false;
		if (p.equals("0"))
			return false;
		if (p.equals("false"))
			return false;
		return true;
	}

	public static boolean decodeMultithreading(Token token) {
		String p = token.parameters.toLowerCase();
		if (p.startsWith("no"))
			return false;
		if (p.equals("0"))
			return false;
		if (p.equals("false"))
			return false;
		if (p.startsWith("dis"))
			return false;
		return true;
	}

	public static Padding decodePadding(Token token) {
		AbstractPadding padXY = new NoPadding();
		AbstractPadding padZ = new NoPadding();
		int extXY = 0;
		int extZ = 0;

		String param = token.parameters.trim();
		String[] parts = param.split(" ");
		if (parts.length > 0)
			padXY = Padding.getByShortname(parts[0].trim());
		if (parts.length > 1)
			padZ = Padding.getByShortname(parts[1].trim());
		double[] ext = NumFormat.parseNumbers(param);
		if (ext.length > 0)
			extXY = (int) Math.round(ext[0]);
		if (ext.length > 1)
			extZ = (int) Math.round(ext[1]);

		return new Padding(padXY, padXY, padZ, extXY, extXY, extZ);
	}

	public static Apodization decodeApodization(Token token) {
		AbstractApodization apoXY = new UniformApodization();
		AbstractApodization apoZ = new UniformApodization();
		String[] parts = token.parameters.trim().split(" ");
		if (parts.length >= 1)
			apoXY = Apodization.getByShortname(parts[0].trim());
		if (parts.length >= 2)
			apoZ = Apodization.getByShortname(parts[1].trim());
		return new Apodization(apoXY, apoXY, apoZ);
	}
	
	public static String getPath() {
		command();
		ArrayList<Token> tokens = parse(command.getCommand());
		String path = System.getProperty("user.dir");
		
		for (Token token : tokens)
			if (token.keyword.equalsIgnoreCase("-path") && !token.parameters.equalsIgnoreCase("current"))
				path = token.parameters;
		return path;
	}



}
