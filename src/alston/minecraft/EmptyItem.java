package alston.minecraft;

import com.jme3.ui.Picture;

/**
 * Represents an empty/no Item or slot (Null Object Pattern). Created as a
 * class/member instead of an anonymous due to problems that arise when it come
 * to serialization.
 *
 * @author Alston
 * @version RTM
 */
public class EmptyItem extends Item {

    //Private constant used for Object serialization
    private static final long serialVersionUID = 3724124124802948L;

    /**
     * Constructor for the empty Item prototype. Simply calls the superclass
     * with the Picture parameter.
     *
     * @param picture The icon for the GUI
     */
    public EmptyItem(Picture picture) {
        super(0, null, picture);
    }

    /**
     * Constructor for the empty Item. Simply calls the superclass.
     *
     */
    public EmptyItem() {
        super(0, null, Item.ITEMS[Item.AIR].getPicture());
    }

    @Override
    public void rightClick(int x, int y, int z, int xModifier, int yModifier, int zModifier) {
    }
}
