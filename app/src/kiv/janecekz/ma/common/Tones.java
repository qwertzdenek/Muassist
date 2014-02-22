package kiv.janecekz.ma.common;

public enum Tones {
    C(0), Cis(1), D(2), Dis(3), E(4), F(5), Fis(6),
    G(7), Gis(8), A(9), Ais(10), B(11);

    private int pos;
    
    public final static float[] freqCoefs = new float[] { 0.5946035575f,
        0.6299605249f, 0.6674199271f, 0.7071067812f, 0.7491535384f,
        0.793700526f, 0.8408964153f, 0.8908987181f, 0.9438743127f, 1f,
        1.0594630944f, 1.1224620483f };

    Tones(int pos) {
        this.pos = pos;
    }

    public int getTonePos() {
        return this.pos;
    }
}
