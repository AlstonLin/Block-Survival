package alston.minecraft;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A Sunlight as defined by this class proporgates light from the top to bottom
 * of every X/Z components in the 3D Vector Space, and will put a Light on the
 * Block that it hits.
 *
 * @author Alston
 * @version 2013
 * @version RTM
 */
public class Sunlight implements Serializable {

    //Private constant to represent the object when saving
    private static final long serialVersionUID = 1232142023449L;
    private float intensity;
    private float originalIntensity;
    private CopyOnWriteArrayList<Block> origins; //Tracks the origins of the lights
    private LinkedList<Light> lights;
    private Chunk parent;

    /**
     * Creates a new Sunlight; Note that setupLighting() must be called at least
     * once on a full Chunk in order for the effects to apply.
     *
     * @param parent The parent that this Sunlight will light up
     * @param intensity The intensity of the lights
     */
    public Sunlight(Chunk parent, float intensity) {
        this.parent = parent;
        this.intensity = intensity;
        originalIntensity = intensity;
        origins = new CopyOnWriteArrayList();
        lights = new LinkedList();
    }

    /**
     * Called by the Object reader when the Object is read.
     *
     * @param input The ObjectInputStream provided by the Object reader
     * @throws IOException Something went wrong while reading
     * @throws ClassNotFoundException Could not find the class of something that
     * belonged to this (usually serialVersionUUID is wrong)
     */
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject(); //Calls default reader first 
    }

    /**
     * Adds a PointLight to every single x and z component at the surfaceHeight.
     */
    public final void setupLighting() {
        for (int i = 0; i < Main.MAX_BLOCKS; i++) { //X Component
            for (int j = 0; j < Main.MAX_BLOCKS; j++) { //Z Component
                calculateLightAndShadows(i, j);
            }
        }
    }

    /**
     * Updates all lights in sunlight.
     */
    public void updateAllLights() {
        Iterator<Light> lightsIterator;
        origins.clear(); //Resets the origins
        lights.clear();
        for (int i = 0; i < Main.MAX_BLOCKS; i++) {
            for (int j = 0; j < Main.MAX_BLOCKS; j++) {
                calculateLightAndShadows(i, j);
            }
        }
        lightsIterator = lights.iterator();
        while (lightsIterator.hasNext()) {
            lightsIterator.next().updateLighting();
        }
    }

    /**
     * Recalculates the lighting at the given spot to create shadows if needed.
     *
     * @param x The x component to recalculate
     * @param z The z component to recalculate
     */
    private void calculateLightAndShadows(int x, int z) throws ArrayIndexOutOfBoundsException {
        int surfaceHeight = Main.MAX_BLOCKS_Y - 1;
        Block block;
        while (parent.getBlock(x, surfaceHeight, z).getType() == 0) {
            //Checks all sides except for vertical
            lightUpAdjacentBlock(parent.getBlock(x, surfaceHeight, z), 1, 0);
            lightUpAdjacentBlock(parent.getBlock(x, surfaceHeight, z), -1, 0);
            lightUpAdjacentBlock(parent.getBlock(x, surfaceHeight, z), 0, 1);
            lightUpAdjacentBlock(parent.getBlock(x, surfaceHeight, z), 0, -1);
            surfaceHeight--;
        }
        block = parent.getBlock(x, surfaceHeight + 1, z);
        if (!origins.contains(block)) { //Only if it was not already lit up
            lights.add(new Light(block, intensity, 0.25f));
            origins.add(block);
        }
    }

    /**
     * Lights up the Block adjacent (at the given offsets) to the given Block if
     * it's a non-Air Block.= or is already an origin.
     *
     * @param block The Block the Block that will be lit up is adjacent to
     * @param xOffset The x component of the location
     * @param zOffset The z component of the location
     */
    private void lightUpAdjacentBlock(Block block, int xOffset, int zOffset) {
        Block adjacentBlock = block.getParent().getBlock(block.getX() + xOffset, block.getY(), block.getZ() + zOffset); //Gets the correct parent first
        if (adjacentBlock == null || adjacentBlock.getType() == Item.AIR || origins.contains(adjacentBlock)) { //Doesnt not exist/Air/Already an origin
            return;
        }
        if (!origins.contains(adjacentBlock)) {
            lights.add(new Light(adjacentBlock, intensity, 0.25f));
            origins.add(adjacentBlock);
        }
    }

    /**
     * Changes all the intensities of the influenced Blocks by the given factor
     * (does not change the original intensity value).
     *
     * @param factor The factor of the original intensity to change to.
     */
    public void factorIntensity(float factor) {
        intensity = factor * originalIntensity;
        Light.updateLights();
    }
}
