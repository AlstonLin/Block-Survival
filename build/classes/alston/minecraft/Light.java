package alston.minecraft;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Object representation of a Point-Source Light, which propergates from the
 * origin with a decay to the surrounding adjacent Blocks with a decay. This
 * Light uses a "careful" brute force approach for it's algorithm.
 *
 * @author Alston
 * @version RTM
 */
public class Light implements Serializable {

    /**
     * Public constant defining the ambient light value.
     */
    public static final float AMBIENT = 1f;
    /**
     * A list containing all lights in existance that are NOT from the sun.
     */
    public static final CopyOnWriteArrayList<Light> existingLights;
    private static Runnable updateSunlight; //Thread for updating lighting
    private static Future future; //So that only 1 can be updated at a time
    private static LinkedList<Block> blocksToUpdate; //Used for the cross-method light algortihmns
    //Private constant to represent the object when saving
    private static final long serialVersionUID = 3724124124342523449L;
    private float intensity; //The intensity of the light (Must be greater than AMBIENT)
    private float originalIntensity;
    private float decay; //The decay per Block 
    private Block origin; //Origin of the Light

    static { //Static initiation Block
        existingLights = new CopyOnWriteArrayList();
        blocksToUpdate = new LinkedList();
        setupRunnable();
    }

    /**
     * Creates a new light at the specified location and properties.
     *
     * @param origin The Block where the light is being originated from
     * @param intensity The intensity of the light above AMBIENT
     * @param decay The decay of lighting per Block that the Light travels
     */
    public Light(Block origin, float intensity, float decay) {
        this.origin = origin;
        this.decay = decay;
        this.intensity = intensity;
        originalIntensity = intensity;
    }

    /**
     * Pernamently (until it is explicitly removed) adds the light.
     *
     * @param light The light to be added
     */
    public static void addPernamentLight(Light light) {
        existingLights.add(light);
    }

    /**
     * Removes a pernament light.
     *
     * @param light The light to be removed
     */
    public static void removePernamentLight(Light light) {
        existingLights.remove(light);
    }

    /**
     * Sets up the runnable thread.
     */
    private static void setupRunnable() {
        updateSunlight = new Runnable() { //Runnable Thread that will update the sunlight
            public void run() {
                try {
                    recalculateAllLights();
                } catch (Exception e) {
                    Logger.getLogger(Light.class.getName()).log(Level.SEVERE, e.getMessage());
                }
            }
        };
    }

    /**
     * Starts a thread to update all lighting.
     */
    public static void updateLights() {
        if (future == null) {
            future = Main.executor.submit(updateSunlight);
        }
    }

    /**
     * Checks on the future for the sunlight thread.
     */
    public static void checkOnFuture() {
        if (future == null) { //Nothing is running
            return;
        }
        if (future.isDone()) { //Sets the future to null
            future = null;
        }
    }

    /**
     * Recalculates all the existingLights.
     */
    private static void recalculateAllLights() {
        //Removes all lights
        Iterator<Chunk> iterator = Main.currentGame.getLoadedChunks().iterator();
        while (iterator.hasNext()) {
            Block[][][] blocks = iterator.next().getBlocks();
            for (int i = 0; i < Main.MAX_BLOCKS; i++) {
                for (int j = 0; j < Main.MAX_BLOCKS_Y; j++) {
                    for (int k = 0; k < Main.MAX_BLOCKS; k++) {
                        if (blocks[i][j][k].getLightLevel() != Light.AMBIENT) {
                            blocks[i][j][k].setLightLevel(AMBIENT);
                            blocksToUpdate.add(blocks[i][j][k]);
                        }
                    }
                }
            }
        }
        //Recalculates Sunlights
        Iterator<Chunk> sunLightIterator = Main.currentGame.getLoadedChunks().iterator();
        while (sunLightIterator.hasNext()) {
            sunLightIterator.next().getSunlight().updateAllLights();
        }
        //Recalculates other lights
        Iterator<Light> lightIterator = existingLights.iterator();
        while (lightIterator.hasNext()) {
            lightIterator.next().updateLighting();
        }
        //Updates all the Blocks to change
        Iterator<Block> blockIterator = blocksToUpdate.iterator();
        while (blockIterator.hasNext()) {
            blockIterator.next().updateVertices();
        }
        blocksToUpdate.clear(); //resets it
    }

    /**
     * Proprogates lighting thru the Blocks and applies it's effects to the
     * Block's lightLevel value if it's high than it's current one. This is not
     * done in the method itself, but simply triggers a recursive light
     * propergating algrorithm starting at the light's origin.
     */
    public void updateLighting() {
        updateLighting(origin, intensity);
    }

    /**
     * Updates the Blocks that the light influences recursively on all sides
     * (part 1).
     *
     * @param block The Block where the Light will originate from
     * @param lightLevel The light level for the given Block
     */
    private void updateLighting(Block block, float lightLevel) {
        block.setLightLevel(lightLevel);
        blocksToUpdate.add(block);
        if (!(block.getType() == Item.AIR || block instanceof Torch)) { //Will only spread to other blocks if it's air or it's a Torch
            return;
        }
        //Sets the surrounding Block's lighting
        updateLighting(block, -1, 0, 0);
        updateLighting(block, 1, 0, 0);
        updateLighting(block, 0, -1, 0);
        updateLighting(block, 0, 1, 0);
        updateLighting(block, 0, 0, -1);
        updateLighting(block, 0, 0, 1);
    }

    /**
     * Updates the lighting at the Block at the given offsets from this Block
     * (2nd part of the recursive method).
     *
     * @param originalBlock The Block that will be the basis for the lcoations
     * @param xOffset Offset on the x axis
     * @param yOffset Offset on the y axis
     * @param zOffset Offset on the z axis
     *
     */
    private void updateLighting(Block originalBlock, int xOffset, int yOffset, int zOffset) {
        Block block = originalBlock.getParent().getBlock(originalBlock.getX() + xOffset, originalBlock.getY() + yOffset, originalBlock.getZ() + zOffset);
        if (block != null) { //Ensures there is a Block 
            if (Math.round((originalBlock.getLightLevel() - decay) * 100) > Math.round(block.getLightLevel() * 105)) { //It's turning brighter
                updateLighting(block, originalBlock.getLightLevel() - decay);
            }
        }
    }

    /**
     * Multiplies the intensity of the Light by the given factor. This will
     * change the intensity value and the multiple is only of the intensity
     * value.
     *
     * @param factor A positive floating point value that will be the multiple
     * of the intensity, with 1 being no change
     */
    public void factorInfluences(float factor) {
        intensity = originalIntensity * factor;
    }

    /**
     *
     * @return The Block at which the light originated from.
     */
    public Block getOrigin() {
        return origin;
    }

    /**
     *
     * @return The intensity of this Light
     */
    public float getIntensity() {
        return intensity;
    }
}
