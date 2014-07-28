package alston.minecraft;

import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Contains both the Item aspect of tools and the Spatials for a 3D
 * representation.
 *
 * @author Alston
 * @version RTM
 */
public class Tool extends Item {

    private float rockMultiplier, woodMultiplier, dirtMultiplier, damageMultiplier; //Amount of extra damage it deals

    /**
     * Creates a new tool with the specified characteristics. The default
     * maximum amount of Tools is 1.
     *
     * @param type The Integer representation of the Item.
     * @param rockMultiplier The multiplier of the damage this tool deals to
     * rocks
     * @param woodMultiplier The multiplier of the damage this tool deals to
     * wood
     * @param dirtMultiplier The multiplier of the damage this tool deals to
     * dirt
     * @param damageMultiplier The multiplier of the damage this tool deals to
     * characters
     * @param spatial A refrence to the prototype spatial
     * @param picture The picture for the GUI
     */
    public Tool(int type, float rockMultiplier, float woodMultiplier, float dirtMultiplier, float damageMultiplier, Spatial spatial, Picture picture) {
        this(type, rockMultiplier, woodMultiplier, dirtMultiplier, damageMultiplier, spatial, picture, 1);
    }

    /**
     * Creates a new tool with the specified characteristics, and maximum amount
     * per stack.
     *
     * @param type The Integer representation of the Item.
     * @param rockMultiplier The multiplier of the damage this tool deals to
     * rocks
     * @param woodMultiplier The multiplier of the damage this tool deals to
     * wood
     * @param dirtMultiplier The multiplier of the damage this tool deals to
     * dirt
     * @param damageMultiplier The multiplier of the damage this tool deals to
     * characters
     * @param spatial A refrence to the prototype spatial
     * @param picture The GUI reprensentation of the Item
     * @param maxAmount The maximum amount per stack
     */
    public Tool(int type, float rockMultiplier, float woodMultiplier, float dirtMultiplier, float damageMultiplier, Spatial spatial, Picture picture, int maxAmount) {
        super(type, spatial, picture, maxAmount);
        this.rockMultiplier = rockMultiplier;
        this.woodMultiplier = woodMultiplier;
        this.dirtMultiplier = dirtMultiplier;
        this.damageMultiplier = damageMultiplier;
    }

    /**
     * Only called when reading from an Object file
     *
     * @param input The input stream provided by the Object reader when the
     * method is called
     * @throws IOException Something went wrong when reading
     * @throws ClassNotFoundException This is the wrong class (usually problem
     * with serialVersionUUID)
     */
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject(); //Calls the default reader first
        //Restores the Spatial
        setSpatial(Item.ITEMS[getType()].getSpatial().clone(true));
    } //End of readObject

    /**
     * Does nothing.
     *
     * @param x Irrelevant
     * @param y Irrelevant
     * @param z Irrelevant
     * @param xModifier Irrelevant
     * @param yModifier Irrelevant
     * @param zModifier Irrelevant
     */
    @Override
    public void rightClick(int x, int y, int z, int xModifier, int yModifier, int zModifier) {
    }

    @Override
    public Tool clone() throws CloneNotSupportedException {
        Tool cloned = (Tool) super.clone();
        cloned.setSpatial(getSpatial().clone(true));
        return cloned;
    }

    public float getRockMultiplier() {
        return rockMultiplier;
    }

    public float getWoodMultiplier() {
        return woodMultiplier;
    }

    public float getDirtMultiplier() {
        return dirtMultiplier;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }
}
