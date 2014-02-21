package kiv.janecekz.ma.tuner;

import kiv.janecekz.ma.Tones;

public class Classificator {
	private final double baseInvLog12 = 1 / Math.log10(Math
			.pow(2.0, 1.0 / 12.0));
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

		double n = Math.abs(logb(freq / baseFreq) / 12);
		// TODO: check negative values

		int iPart = (int) n; // integral part
		double fPart = n - iPart; // fractional part

		double div = 0.0;
		int index = 0;
		for (int i = 0; i < freqs.length; i++) {
			div = freq >= baseFreq ? fPart - freqs[i] : -(fPart - freqs[i]);

			if (Math.abs(div) < Math.abs(min)) {
				index = freq >= baseFreq ? (9 + i) % 12 : (21 - i) % 12;
				tone = Tones.values()[index];
				min = div;
			}
		}

		return new Result(tone, min * 24, freq);
	}

	private Double logb(double x) {
		return Math.log10(x) * baseInvLog12;
	}
}
