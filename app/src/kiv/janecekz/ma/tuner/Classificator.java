/*
Musicians Assistant
    Copyright (C) 2014  Zdeněk Janeček <jan.zdenek@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package kiv.janecekz.ma.tuner;

import kiv.janecekz.ma.common.Tones;

public class Classificator {
	private final double invLog2 = 1 / Math.log10(2.0);
	private final double[] eq = { 0.0, 100.0, 200.0, 300.0, 400.0, 500.0,
			600.0, 700.0, 800.0, 900.0, 1000.0, 1100.0, 1200.0 };
	private final double[] just = { 0.0, 111.73, 203.91, 315.64, 386.31, 498.04,
			582.51, 701.96, 813.69, 884.36, 996.09, 1088.27, 1200.0 };

	private int baseFreq;

	public class Result {
		private Tones tone;
		private double error;
		private double freq;

		public Result(Tones tone, double error, double freq) {
			super();
			this.tone = tone;
			this.error = error;
			this.freq = freq;
		}

		public Tones getTone() {
			return tone;
		}

		public double getError() {
			return error;
		}

		public double getFreq() {
			return freq;
		}
	}

	public Classificator(int baseFreq) {
		this.baseFreq = baseFreq;
	}

	/**
	 * Classifies given frequency to the one tone.
	 * 
	 * @param freq
	 *            frequency for analysis
	 * @param temp
	 *            required temperament
	 * @return instance of the Result class. Tone, error and origin frequency.
	 */
	public Result findTone(double freq, int temp) {
		double min = Double.MAX_VALUE;
		Tones tone = Tones.A;
		double div = 0.0;
		int index = 0;

		double[] freqs = null;

		switch (temp) {
		case 0: // Equal temperament
			freqs = eq;
			break;
		case 1: // Just intonation
			freqs = just;
			break;
		default:
			break;
		}

		double n = log2(freq / baseFreq);

		int iPart = (int) n; // integral part
		double fPart = n - iPart; // fractional part

		if (freq < baseFreq)
			fPart = fPart + 1;

		fPart *= 1200;

		for (int i = 0; i < freqs.length; i++) {
			div = fPart - freqs[i];

			if (Math.abs(div) < Math.abs(min)) {
				// move reference tone from A to C
				index = (i + 9) % 12;
				tone = Tones.values()[index];
				min = div;
			}
		}

		// scale to (-1; 1)
		return new Result(tone, min / 50, freq);
	}

	/**
	 * Returns logarithm of base 2.
	 * 
	 * @param x
	 *            the value whose log has to be computed.
	 * @return real number
	 */
	private Double log2(double x) {
		return Math.log10(x) * invLog2;
	}

	/**
	 * Changes convert pitch
	 * 
	 * @param newVal
	 *            new value (390 to 460)
	 */
	public void changeRef(int newVal) {
		this.baseFreq = newVal;
	}
}
