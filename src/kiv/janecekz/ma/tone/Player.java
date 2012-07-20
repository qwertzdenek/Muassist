package kiv.janecekz.ma.tone;

public class Player extends Thread {
    private boolean loop;
    private boolean play = false;

    final static double PI2 = 2 * Math.PI;
    final static int SAMPLE_FREQ = 11025;

    static double piDivSampleFreq;

    double[] audioData;
    double angleMain = 0;
    double angleHarmonic = 0;
    int freq;
    boolean updatedFreq;
    int sampleLength;
    AudioDevice ad;

    public Player() {
        ad = new AudioDevice(SAMPLE_FREQ);
        piDivSampleFreq = PI2 / SAMPLE_FREQ;
    }

    public synchronized void setPlay() {
        this.play = !play;
        if (play)
            notify();
    }

    @Override
    public void run() {
        while (loop) {
            try {
                pauseLoop();
            } catch (InterruptedException e) {
                loop = false;
                break;
            }

            if (updatedFreq) {
                sampleLength = SAMPLE_FREQ / freq;
                double incMain = piDivSampleFreq * freq;
                double incHarm = piDivSampleFreq * 2 * freq;

                double[] samples = new double[sampleLength];
                for (int i = 0; i < samples.length; i++) {
                    samples[i] = (Math.sin(angleMain) + 0.5 * Math
                            .sin(angleHarmonic)) / 2;
                    angleMain += incMain;
                    angleHarmonic += incHarm;
                }

                audioData = new double[10 * samples.length];

                int index = 0;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < samples.length; j++) {
                        audioData[index++] = samples[j];
                    }
                }

                updatedFreq = false;
            }

            ad.writeSamples(audioData);
        }
    }

    private synchronized void pauseLoop() throws InterruptedException {
        if (!play) {
            wait();
        }
    }

    @Override
    public void interrupt() {
        loop = false;
        ad.cleanUp();
        super.interrupt();
    }

    @Override
    public synchronized void start() {
        loop = true;
        super.start();
    }

    public void setFreq(int freq) {
        this.freq = freq;
        updatedFreq = true;
    }
}
