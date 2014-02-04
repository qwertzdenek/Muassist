package kiv.janecekz.ma.tuner;

import kiv.janecekz.ma.Tones;

public class Classificator {
    private final double baseInvLog12 = 1 / Math.log10(Math.pow(2f, 1f/12));
    private final double freqRange = 1f/24;
    private final double[] freqs = { 0, 1f/12, 2f/12, 3f/12, 4f/12, 5f/12,
            6f/12, 7f/12, 8f/12, 9f/12, 10f/12, 11f/12 };

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
        
        int iPart = (int) n; // integral part
        double fPart = n - iPart;; // fractional part
        
        double div = 0.0;
        for (int i = 0; i < freqs.length; i++) {
            div = fPart - freqs[i]; // TODO: fix problem with A (shown as Gis)
            
            if (Math.abs(div) < Math.abs(min)) {
                tone = Tones.values()[(i + 9) % 12];
                min = div;
            }
        }
        
        //Log.d(MainActivity.TAG,
        //String.format("freq=%f, n=%f, tone=%s, min=%f", freq, n, tone, min));
        
        return new Result(tone, min / freqRange, freq);
    }
    
    private Double logb(double x) {
        return Math.log10(x) * baseInvLog12;
    }
}
