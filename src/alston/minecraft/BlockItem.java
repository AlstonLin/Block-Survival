package alston.minecraft;

import com.jme3.ui.Picture;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * An extension the abstract class Item, and is for any Items that will become a
 * block when right clicked. It defines the specific interactions that happens
 * when the user rigth clicks with this item
 *
 * @author Alston
 * @version RTM
 */
public class BlockItem extends Item {

    //Private constant to represent the object when saving
    private static final long serialVersionUID = 372413241342949L;
    //Fields
    private float timeToBreak;

    /**
     * Constructor for the blockItem, with the default 64 maxAmount per stack.
     * Calls it's superclass constructor then assigns the additional variable of
     * the time to break the block.
     *  
     * @param picture The icon for the GUI
     * @param type An integer representation of the item.
     * @param timeToBreak A float representing the time in seconds it would take
     * to break the block
     */
    public BlockItem(Picture picture, int type, float timeToBreak) {
        super(type, Main.blockGeometry.clone(true), picture); //Calls the constructor in Item first
        this.timeToBreak = timeToBreak;
    } //End of constructor

    /**
     * Constructor for the blockItem, with a predefined maxAmount. Calls it's
     * superclass constructor then assigns the additional variable of the time
     * to break the block.
     * 
     * @param picture The picture for the GUI
     * @param type An integer representation of the item.
     * @param timeToBreak A float representing the time in seconds it would take
     * to break the block
     * @param maxAmount The maximum amount of Items per stack
     */
    public BlockItem(Picture picture, int type, float timeToBreak, int maxAmount) {
        super(type, Main.blockGeometry.clone(true), picture, maxAmount); //Calls the constructor in Item first
        this.timeToBreak = timeToBreak;
    } //End of constructor

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
        setSpatial(Main.blockGeometry.clone(true));
    } //End of readObject

    @Override
    public void rightClick(int x, int y, int z, int xModifier, int yModifier, int zModifier) {
        if (Math.round(Main.currentGame.getPlayer().getControl().getPhysicsLocation().x) == x + xModifier && (int) Main.currentGame.getPlayer().getControl().getPhysicsLocation().y == y + yModifier
                && Math.round(Main.currentGame.getPlayer().getControl().getPhysicsLocation().z) == z + zModifier) { //Bug Fix (So player doesn't get stuck in a Block)
            return;
        }
        try {
            Main.currentGame.getChunk(Main.currentGame.getCurrentChunkX(), Main.currentGame.getCurrentChunkY())
                    .getBlock(x + xModifier - Main.currentGame.getCurrentChunkX() * Main.MAX_BLOCKS, y + yModifier,
                    z + zModifier - Main.currentGame.getCurrentChunkY() * Main.MAX_BLOCKS).changeToBlock(getType(), true);
            setAmount(getAmount() - 1); //Removes 1 from amount
            place.playInstance(); //Plays the sound
        } catch (NullPointerException e) { //Block being placed outside loaded chunk
        }
    }//End of rightClick

    /**
     *
     * @return A float of the seconds it takes the break the block
     */
    public float getTimeToBreak() {
        return timeToBreak;
    }
}