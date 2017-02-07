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

import lab.tools.NumFormat;
import signal.Constraint;
import signal.apodization.AbstractApodization;
import signal.apodization.Apodization;
import signal.apodization.UniformApodization;
import signal.padding.AbstractPadding;
import signal.padding.NoPadding;
import signal.padding.Padding;
import wavelets.Wavelets;
import deconvolution.algorithm.AbstractAlgorithm;
import deconvolution.algorithm.Algorithm;
import deconvolution.algorithm.Controller;
import deconvolutionlab.Output;
import deconvolutionlab.Output.View;
import deconvolutionlab.modules.AbstractModule;
import deconvolutionlab.modules.CommandModule;
import deconvolutionlab.modules.LanguageModule;
import deconvolutionlab.monitor.ConsoleMonitor;
import deconvolutionlab.monitor.Monitors;
import deconvolutionlab.monitor.TableMonitor;

public class Command {

	public static String			keywords[]	= { "-image", "-psf", "-algorithm", "-path", "-disable", "-verbose", "-time", "-constraint", "-residu", "-reference", "-savestats", "-showstats", "-out", "-pad", "-apo", "-norm", "-fft" };

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

		ArrayList<Token> tokens = new ArrayList<Token>();
		for(int i=0; i<segments.size(); i++) {
			String keyword = segments.get(i).keyword;
			int begin = segments.get(i).index+keyword.length()+1;
			int end = (i<segments.size()-1 ? segments.get(i+1).index : command.length());
			Token token = new Token(keyword, command, begin, end);
			tokens.add(token);
		}

		/*
		for (int i = 0; i < segments.size(); i++) {
			CommandSegment segment = segments.get(i);
			String next = (i + 1 < segments.size() ? segments.get(i + 1).keyword : "");
			int end = (i + 1 < segments.size() ? segments.get(i + 1).index - next.length() : command.length());
			tokens.add(new Token(segment.keyword, command, segment.index, end));
		}
		*/
		
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
		Matcher matcher = Pattern.compile(regex).matcher(command);

		while (matcher.find()) {
			segments.add(new CommandSegment(keyword, matcher.start()));
			System.out.println(" " + "," + matcher.end() + " " + matcher.group());
		}

		Collections.sort(segments);
		return segments;
	}
	/*
	 * public static ArrayList<CommandSegment> findSegment(String command,
	 * String key) { int index = -1; ArrayList<CommandSegment> segments = new
	 * ArrayList<CommandSegment>(); do { index = command.indexOf(key, index +
	 * 1); if (index >= 0) { segments.add(new CommandSegment(key, index +
	 * key.length())); } } while (index >= 0); return segments; }
	 */

	public static AbstractAlgorithm decodeAlgorithm(Token token, Controller controller) {

		String option = token.option;

		AbstractAlgorithm algo = Algorithm.createAlgorithm(option);
		double params[] = parseNumeric(token.parameters);
		if (params != null) {
			algo.setParameters(params);
			if (algo.isIterative())
				controller.setIterationMax(algo.getController().getIterationMax());
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

		int freq = 1;
		String line = token.parameters;
		if (token.parameters.startsWith("@")) {
			String parts[] = token.parameters.split(" ");
			if (parts.length >= 1) {
				freq = (int) NumFormat.parseNumber(parts[0], 1);
				line = token.parameters.substring(parts[0].length(), token.parameters.length()).trim();
			}
		}

		if (token.keyword.equals("-constraint")) {
			controller.setConstraint(freq, Constraint.getByName(line.trim()));
		}

		else if (token.keyword.equals("-residu")) {
			double stop = NumFormat.parseNumber(line, -1);
			controller.setResiduStop(freq, stop);
		}

		else if (token.keyword.equals("-reference")) {
			controller.setReference(freq, line);
		}

		else if (token.keyword.equals("-savestats")) {
			controller.setSaveStats(freq, line);
		}

		else if (token.keyword.equals("-showstats")) {
			controller.setShowStats(freq, line);
		}

		else if (token.keyword.equals("-time")) {
			double stop = NumFormat.parseNumber(line, Double.MAX_VALUE);
			controller.setTimeStop(stop);
		}
	}

	public static double decodeNormalization(Token token) {
		if (token.parameters.toLowerCase().endsWith("no"))
			return 0;
		else
			return NumFormat.parseNumber(token.parameters, 1);
	}

	public static boolean decodeDisable(Token token, String word) {
		String p = token.parameters.toLowerCase();
		String parts[] = p.split(" ");
		for (String part : parts) {
			if (part.trim().equals(word))
				return false;
		}
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

}
