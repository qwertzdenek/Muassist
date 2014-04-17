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
	private final double[] freqs = { 0.0, 1.0 / 12.0, 2.0 / 12.0, 3.0 / 12.0,
			4.0 / 12.0, 5.0 / 12.0, 6.0 / 12.0, 7.0 / 12.0, 8.0 / 12.0,
			9.0 / 12.0, 10.0 / 12.0, 11.0 / 12.0, 1.0 };

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

	public Result findTone(double freq) {
		double min = Double.MAX_VALUE;
		Tones tone = Tones.A;

		double n = log2(freq / baseFreq);

		int iPart = (int) n; // integral part
		double fPart = n - iPart; // fractional part

		if (freq < baseFreq)
			fPart = 1 + fPart;
		
		double div = 0.0;
		int index = 0;
		for (int i = 0; i < freqs.length; i++) {
			div = fPart - freqs[i];

			if (Math.abs(div) < Math.abs(min)) {
				index = (i + 9) % 12;
				tone = Tones.values()[index];
				min = div;
			}
		}

		return new Result(tone, min * 24, freq);
	}

	private Double log2(double x) {
		return Math.log10(x) * invLog2;
	}

	public void changeRef(int newVal) {
		this.baseFreq = newVal;
	}
}
