package alston.minecraft;

/**
 * States that the Object that implements this can open a window
 * and must implement a close() method.
 *
 * @author Alston
 * @version RTM
 */
public interface Showable {

    /**
     * Closes the showable window
     */
    public void close();
}
