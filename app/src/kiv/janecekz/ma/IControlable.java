package kiv.janecekz.ma;

/**
 * Utility interface for the {@code TouchControl} class.
 * 
 * @author Zdeněk Janeček
 * 
 */
public interface IControlable {
    /**
     * Called when is good to change some value. Called at the moment when is
     * not posible any other option, like double tap.
     * 
     * @param t
     *            TouchControl who called us.
     * @param val
     *            Delta value that means speed. Measured in the pixels per
     *            second.
     */
    public void onValueChange(TouchControl t, int val);

    /**
     * Called when is time to change run of your proces. Most interesting is is
     * {@code TouchControl.STATE_STOP} for you toggle function. Others are for
     * the animations.
     * 
     * @param t
     *            TouchControl who called us.
     * @param state
     *            Can get only values {@code TouchControl.STATE_BEGIN},
     *            {@code TouchControl.STATE_STOP}, and
     *            {@code TouchControl.STATE_OUT}.
     */
    public void onToggle(TouchControl t, int state);

    /**
     * Called everytime is recorded touch event. It is redirection of the
     * onTouch event. Use only for the touch cursor.
     * 
     * @param t
     *            TouchControl who called us.
     * @param x
     *            X coordinate.
     * @param y
     *            Y coordinate.
     */
    public void onPositionChange(TouchControl t, int x, int y);
}
