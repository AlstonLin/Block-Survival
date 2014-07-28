/*
 * Item representation of a torch
 */
package alston.minecraft;

import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;

/**
 * An Item that will have a special Spatial and physics Control (is NOT a
 * Block-shaped item).
 *
 * @author Alston
 * @version RTM
 */
public class TorchItem extends SimpleItem {

    /**
     * Creates a new TorchItem; this constructor should only be called once to
     * vcreate the prototype.
     *
     * @param spatial The spatial for the Torch
     * @param picture The icon for the GUI
     */
    public TorchItem(Spatial spatial, Picture picture) {
        super(Item.TORCH, spatial, picture);
    }

    @Override
    public void rightClick(int x, int y, int z, int xModifier, int yModifier, int zModifier) { //Places it as if it were a Block
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
    }
}
