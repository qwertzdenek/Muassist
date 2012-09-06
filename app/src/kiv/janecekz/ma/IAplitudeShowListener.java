package kiv.janecekz.ma;

import kiv.janecekz.ma.rec.ExtAudioRecorder;

public interface IAplitudeShowListener {
    /**
     * Executed when is available new amplitude value to draw.
     * 
     * @param ear
     *            Recorder class that sent a message.
     */
    public void onUpdate(ExtAudioRecorder ear);
}
