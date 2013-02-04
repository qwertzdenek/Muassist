package kiv.janecekz.ma;

public interface Informable {
    /**
     * Executed when is available new message to write
     * 
     * @param msg Message to post.
     */
    public void onMessage(String msg);
}
