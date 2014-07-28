package alston.minecraft;

import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;

/**
 * Objects of this class represents Items that have no special features, and do
 * not become Blocks.
 *
 * @author Alston
 * @version RTM
 */
public class SimpleItem extends Item {

    //Private constant used for identification when saving
    private static final long serialVersionUID = 8472642812947L;

    /**
     * Creates a new SimpleItem with the given type and Spatial, and default
     * maxAmount value of 64.
     *
     * @param type An Integer representation of the Item
     * @param spatial The Spatial for the Item
     * @param picture The Picture icon for the GUI
     */
    public SimpleItem(int type, Spatial spatial, Picture picture) {
        super(type, spatial, picture);
    }

    @Override
    public void rightClick(int x, int y, int z, int xModifier, int yModifier, int zModifier) { //Does nothing
    }
}
