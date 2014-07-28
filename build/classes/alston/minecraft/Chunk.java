package alston.minecraft;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.scene.Node;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds an array of Blocks and is and handles all initializations of Blocks and
 * find the Blocks they hold. A Chunk is maxBlocks ^2 * maxBlockY in volume. The
 * use of Chunks instead of a direct array of Blocks is to enable the ability to
 * generate a infinite (or almost infinite, being limited by memory) world. The
 * memory that a typical loaded Chunk consumes is approx. 100MB (varying
 * depending on the amount of Blocks that are hidden and have their memory
 * intensive data null-refrenced), while a non-loaded chunk would take
 * maxBlocksY(maxBlocksX^2)(64 bits [float] + 4 (32 bits) [int] + 2 bits
 * [boolean] + 32 to 64 bits, depending on processor [pointer/reference to
 * parent]).
 *
 * @author Alston
 * @version RTM
 */
public class Chunk implements Serializable {

    //Private constant used for Object serialization
    private static final long serialVersionUID = 3724124124812948L;
    //Fields (transient = will not serialize)
    private Sunlight sunlight; //The sunlight for this Chunk
    private boolean loaded; //If the chunk is currently rendered and in physics
    private Block[][][] blocks; //The blocks making up the chunk
    private int x, y; //Location within the world
    private transient Future updateCollisionFuture; //Keeps track of the thread when updating collision shape
    private transient Node node; //Node containing spatials of all the blocks
    private transient RigidBodyControl control; //The collision shape of all the blocks in the chunk for physics

    /**
     * Instantiates a new Array of Block, all the associated Objects to the
     * Chunk, and initiates the variables.
     *
     * @param x The x coordinate of the Chunk in the World
     * @param y The y coordinate of the Chunk in the World
     */
    public Chunk(int x, int y) {
        blocks = new Block[Main.MAX_BLOCKS][Main.MAX_BLOCKS_Y][Main.MAX_BLOCKS];
        node = new Node();
        control = new RigidBodyControl(0f);
        node.addControl(control);
        sunlight = new Sunlight(this, 3f);
        this.x = x;
        this.y = y;
        loaded = true;
        setupBlocks();
        sunlight.setupLighting();
    } //End of constructor

    /**
     * Called by the Object reader when the Object is read.
     *
     * @param input The ObjectInputStream provided by the Object reader
     * @throws IOException Something went wrong while reading
     * @throws ClassNotFoundException Could not find the class of something that
     * belonged to this (usually serialVersionUUID is wrong)
     */
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        if (loaded) { //Re-instantiates all memory-intensive Objects if the chunk is currently loaded
            node = new Node();
            Main.blockNode.attachChild(node);
            for (int i = 0; i < Main.MAX_BLOCKS; i++) { //Restores all blocks in the array
                for (int j = 0; j < Main.MAX_BLOCKS_Y; j++) {
                    for (int k = 0; k < Main.MAX_BLOCKS; k++) {
                        blocks[i][j][k].setupFromFile(this);
                    }
                }
            }
            control = new RigidBodyControl(CollisionShapeFactory.createMeshShape(node), 0f);
            addToPhysics();
        }
        //Reads each individual field

    } //End of readObject

    /**
     * Gets the Block in the specified coordinates (allows for negatives) from
     * the Chunk by using a divide and conquer search algorithm. If it is not
     * located inside the Chunk, it will attempt to access the Block from the
     * most appropriate Chunk and keep recusively (kinda) doing so until it
     * either finds the Chunk, or reaches an unloaded Chunk, and would then
     * return null.
     *
     * @param x X coordinate of the specified Block
     * @param y Y coordinate of the specified Block
     * @param z Z coordinate of the specified Block
     * @return The specified Block
     */
    public Block getBlock(int x, int y, int z) {
        try {
            if (x >= Main.MAX_BLOCKS) { //On a chunks + 1 in X axis
                if (z >= Main.MAX_BLOCKS) { //Top Right(X+1,Y+1)
                    return Main.currentGame.getChunk(this.x + 1, this.y + 1).getBlock(x - Main.MAX_BLOCKS, y, z - Main.MAX_BLOCKS);
                } else if (z < 0) { //Top Left(X+1,Y-1)
                    return Main.currentGame.getChunk(this.x + 1, this.y - 1).getBlock(x - Main.MAX_BLOCKS, y, z + Main.MAX_BLOCKS);
                } else { //Directly Right(X+1)
                    return Main.currentGame.getChunk(this.x + 1, this.y).getBlock(x - Main.MAX_BLOCKS, y, z);
                }
            } else if (x < 0) {  //On a chunks - 1 in X axis
                if (z >= Main.MAX_BLOCKS) { // Bottom Right(X-1,Y+1)
                    return Main.currentGame.getChunk(this.x - 1, this.y + 1).getBlock(x + Main.MAX_BLOCKS, y, z - Main.MAX_BLOCKS);
                } else if (z < 0) { //Bottom Left(X-1,Y-1)
                    return Main.currentGame.getChunk(this.x - 1, this.y - 1).getBlock(x + Main.MAX_BLOCKS, y, z + Main.MAX_BLOCKS);
                } else { //Directly Left(X-1)
                    return Main.currentGame.getChunk(this.x - 1, this.y).getBlock(x + Main.MAX_BLOCKS, y, z);
                }
            } else { //On the current X value on the axis
                if (z >= Main.MAX_BLOCKS) { // Top(Y+1)
                    return Main.currentGame.getChunk(this.x, this.y + 1).getBlock(x, y, z - Main.MAX_BLOCKS);
                } else if (z < 0) { //Bottom(Y-1)
                    return Main.currentGame.getChunk(this.x, this.y - 1).getBlock(x, y, z + Main.MAX_BLOCKS);
                } else { //Current Chunk (0)
                    return blocks[x][y][z];
                }
            }
        } catch (NullPointerException e) { //Tried to access a chunk that does not exist
            return null;
        } catch (ArrayIndexOutOfBoundsException e) { //Tried to access a block that does not exist
            return null;
        }
    }//End of getBlock

    /**
     * Initiates new blocks in every index in the chunk
     *
     */
    private void setupBlocks() {
        for (int i = 0; i < Main.MAX_BLOCKS; i++) { //Declares the block in every 
            for (int j = 0; j < Main.MAX_BLOCKS_Y; j++) {
                for (int k = 0; k < Main.MAX_BLOCKS; k++) {
                    blocks[i][j][k] = new Block(i, j, k, this);
                }
            }
        }
        generateTerrain();
    }//End of setupBlocks

    /**
     * Generates terrain on the Chunk. If called from the Main/Render thread
     * (Game is being created), it will do so directly. If it is being called
     * from a seperate thread, the method will enqueue render/physics Objects in
     * the Main thread to prevents problems due to the JMonkeyEngine lacking
     * synchronization.
     */
    private void generateTerrain() {
        generateBedrock();
        generateRocks();
        generateSurface();
        generateTrees();
        for (int i = 0; i < Main.MAX_BLOCKS; i++) { //Checks if any blocks needs to be shown
            for (int j = 0; j < Main.MAX_BLOCKS_Y; j++) {
                for (int k = 0; k < Main.MAX_BLOCKS; k++) {
                    blocks[i][j][k].checkBlockForShowing();
                }
            }
        }
        if (Thread.currentThread().equals(Main.mainThread)) { //If it's being used in the main thread
            control = new RigidBodyControl(CollisionShapeFactory.createMeshShape(node), 0f);
            Main.blockNode.attachChild(node);
            addToPhysics();
        } else { //If it's being used in a different thread 
            boolean controlCreated = false; //Keeps track on whether or not the control was actually created
            while (!controlCreated) {
                try {
                    control = new RigidBodyControl(CollisionShapeFactory
                            .createMeshShape((Node) Main.getInstance().enqueue(new Callable() { //Safely gets the node from main thread
                        public Object call() throws Exception {
                            return node;
                        }
                    }).get()), 0f); //Creates the control
                    controlCreated = true;
                } catch (Exception ex) { //Something went wrong and will try again
                    Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Main.getInstance().enqueue(new Callable() { //Callable task that will add the chunk to rendering and physics
                //Safely adds it to the render thread
                public Object call() throws Exception {
                    Main.blockNode.attachChild(node);
                    addToPhysics();
                    return null;
                }
            }); //Modifys the rendering and physics safely by enqueuing it in the main thread
        }
    } //End of generateTerrain

    /**
     * Creates a 1 layer thick bedrock layer at the bottom of the map.
     */
    private void generateBedrock() {
        for (int i = 0; i < Main.MAX_BLOCKS; i++) {
            for (int j = 0; j < Main.MAX_BLOCKS; j++) {
                blocks[i][0][j].changeToBlock(3, false);
            }
        }
    }

    /**
     * Generates the layer in between bedrock and the surface, comprising mainly
     * of stone, and occasionaly various ores.
     */
    private void generateRocks() {
        final float coalSpawnChance = 0.05f, ironSpawnChance = 0.01f, goldSpawnChance = 0.0075f, diamondSpawnChance = 0.001f; //Ore rarity
        for (int i = 0; i < Main.MAX_BLOCKS; i++) {
            for (int j = 1; j < 29; j++) {
                for (int k = 0; k < Main.MAX_BLOCKS; k++) {
                    double oreDecider = Math.random(); //Random number that decides if there should be ore, and what ore
                    if (oreDecider <= diamondSpawnChance) { //Diamond ore
                        blocks[i][j][k].changeToBlock(8, false);
                    } else if (oreDecider <= diamondSpawnChance + goldSpawnChance) { //Gold ore
                        blocks[i][j][k].changeToBlock(7, false);
                    } else if (oreDecider <= diamondSpawnChance + goldSpawnChance + ironSpawnChance) { //Iron ore
                        blocks[i][j][k].changeToBlock(6, false);
                    } else if (oreDecider <= diamondSpawnChance + goldSpawnChance + ironSpawnChance + coalSpawnChance) { //Coal ore
                        blocks[i][j][k].changeToBlock(5, false);
                    } else { //Stone
                        blocks[i][j][k].changeToBlock(4, false);
                    }
                }
            }
        }
    }

    /**
     * Generates the dirt and grass laver
     */
    private void generateSurface() {
        for (int i = 0; i < Main.MAX_BLOCKS; i++) { //Dirt
            for (int j = 29; j < 32; j++) {
                for (int k = 0; k < Main.MAX_BLOCKS; k++) {
                    blocks[i][j][k].changeToBlock(1, false);
                }
            }
        }
        for (int i = 0; i < Main.MAX_BLOCKS; i++) { //Grass
            for (int k = 0; k < Main.MAX_BLOCKS; k++) {
                blocks[i][32][k].changeToBlock(2, false);
            }
        }
    }

    /**
     * Generates trees in random places in the chunk.
     */
    private void generateTrees() {
        float collectivePossibility = 0f; //Builds up after a while of not spawning trees
        for (int i = 0; i < Main.MAX_BLOCKS; i++) {
            for (int j = 0; j < Main.MAX_BLOCKS; j++) {
                if (Math.random() * collectivePossibility < 1 && !isTreeNear(i, j) && i > 4 && i < Main.MAX_BLOCKS - 4
                        && j > 4 && j < Main.MAX_BLOCKS - 4) {//Ensures trees are not near and is not at the edge of the chunk
                    createTree(i, getSurfaceHeight(i, j) + 1, j, false);
                } else {
                    collectivePossibility += 0.3f; //More possibility next round
                }
            }
        }
    }

    /**
     * Creates a tree at the given coordinates of the bottom of the tree.
     *
     * @param x The x coordinate bottom of the tree
     * @param y The y coordinate bottom of the tree
     * @param z The z coordinate bottom of the tree
     * @param updatePhysics If physics should be updated after this (not
     * generated from chunk creation)
     */
    public void createTree(int x, int y, int z, boolean updatePhysics) {
        int treeHeight = (int) (Math.random() * 5) + 4; //Random tree height from 4-8 blocks high
        for (int i = 0; i <= treeHeight; i++) { //Creates trunk
            blocks[x][y + i][z].changeToBlock(9, false);
        }
        for (int i = -2; i <= 2; i++) { //Creates leaves (middle)
            for (int j = 2; j <= treeHeight; j++) {
                for (int k = -1; k <= 1; k++) {
                    if (!(i == 0 && k == 0)) { //So it doesnt replace the trunk
                        blocks[x + i][y + j][z + k].changeToBlock(10, false);
                    }
                }
            }
        }
        for (int i = -1; i <= 1; i++) { //Bottom leaves
            for (int j = 2; j <= treeHeight; j++) {
                blocks[x + i][y + j][z - 2].changeToBlock(10, false);
            }
        }
        for (int i = -1; i <= 1; i++) { //Top leaves
            for (int j = 2; j <= treeHeight; j++) {
                blocks[x + i][y + j][z + 2].changeToBlock(10, false);
            }
        }
        for (int i = -1; i <= 1; i++) { //1st layer of leaves above trunk
            for (int j = -1; j <= 1; j++) {
                blocks[x + i][y + treeHeight + 1][z + j].changeToBlock(10, false);
            }
        }
        blocks[x][y + treeHeight + 2][z].changeToBlock(10, updatePhysics); //Very top leaf, then updates physics in applicable

    }

    /**
     * Adds the Chunk to physics
     */
    private void addToPhysics() {
        Main.bulletAppState.getPhysicsSpace().add(control);
        node.addControl(control);
    }

    /**
     * Either submits a new thread that will concurrently create a new
     * CollisionShape to update the old one, or check on it to see if it's done.
     */
    public void updateCollisionShape() {
        if (updateCollisionFuture == null) { //If it is starting the thread
            updateCollisionFuture = Main.executor.submit(new Callable() { //Callable task that returns a collisionShape of the chunk
                public Object call() throws Exception {
                    return CollisionShapeFactory.createMeshShape((Node) Main.getInstance().enqueue(new Callable() { //Safely gets from main thread
                        public Object call() throws Exception {
                            return node;
                        }
                    }).get());
                }
            });
        } else { //If it is being checked on
            if (updateCollisionFuture.isDone()) { //When it's finished
                //Removes old control and all refrences to it
                Main.bulletAppState.getPhysicsSpace().remove(control);
                node.removeControl(control);
                //Replaces with the new control created from the thread
                try {
                    control = new RigidBodyControl((CollisionShape) updateCollisionFuture.get(), 0f);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Chunk.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(Chunk.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                Main.bulletAppState.getPhysicsSpace().add(control);
                node.addControl(control);
                updateCollisionFuture = null;
            } else if (updateCollisionFuture.isCancelled()) { //Canceled for some reason
                Logger.getLogger(Chunk.class
                        .getName()).log(Level.SEVERE, null, "updateCollisionShape was CANCELLED");
                updateCollisionFuture = null;
            }
        }
    } //End of updateCollisionShape

    /**
     * Restores the chunk's node and controls. To be called from a seperate
     * thread from the Main/Render thread.
     */
    public void restore() { //Restores the chunk's node and controls (called from a seperate thread)
        loaded = true;
        boolean controlCreated = false;
        node = new Node();
        for (int i = 0; i < Main.MAX_BLOCKS; i++) { //Restores all blocks in the array
            for (int j = 0; j < Main.MAX_BLOCKS_Y; j++) {
                for (int k = 0; k < Main.MAX_BLOCKS; k++) {
                    blocks[i][j][k].restore();
                }
            }
        }
        while (!controlCreated) { //A control must be created in order for this thread to continue
            try {
                control = new RigidBodyControl(CollisionShapeFactory
                        .createMeshShape((Node) Main.getInstance().enqueue(new Callable() { //Safely gets the node from main thread
                    public Object call() throws Exception {
                        return node;
                    }
                }).get()), 0f); //Restores the control for physics
                controlCreated = true;
            } catch (Exception ex) {
                Logger.getLogger(Chunk.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        Main.getInstance().enqueue(new Callable() { //Callable task that will add the chunk to rendering and physics
            //Safely adds it to the render thread
            public Object call() throws Exception {
                Main.blockNode.attachChild(node);
                Main.bulletAppState.getPhysicsSpace().add(control);
                node.addControl(control);
                return null;
            }
        });
        sunlight.factorIntensity((float) Math.sin((Main.currentGame.getTime() / 24f * Math.PI))); //Sets to the approriate sunlight
    }//End of restore

    /**
     * Destroys the block by removing all refrences (including to
     * Render/Physics) to any memory intensive Objects so they will be eligible
     * for Garbage Collection and frees up RAM. To be called concurrently of the
     * Main/Render thread.
     */
    public void destroy() { //Removes all refrences to the contents of this chunk (render and physics)
        Future future = Main.getInstance().enqueue(new Callable() { //Callable task that will remove chunk from rendering and physics
            public Object call() throws Exception {
                Main.blockNode.detachChild(node);
                Main.bulletAppState.getPhysicsSpace().remove(control);
                node = null;
                control = null;
                return null;
            }
        });
        while (!future.isDone()) { //Waits for the node and control to be removed from main thread until it continues (Bug Fix)
        }
        for (int i = 0; i < Main.MAX_BLOCKS; i++) { //Destroys all blocks in the array
            for (int j = 0; j < Main.MAX_BLOCKS_Y; j++) {
                for (int k = 0; k < Main.MAX_BLOCKS; k++) {
                    blocks[i][j][k].compress();
                }
            }
        }
        loaded = false;
    } //End of destroy

    /**
     *
     * @param x The x coordinate of intrest within the chunk
     * @param z The y cooridnate of intrest within the chunk
     * @return If there is a tree within a 7 m radius at the surface
     */
    private boolean isTreeNear(int x, int z) {
        for (int i = -7; i <= 7; i++) {
            for (int j = -7; j <= 7; j++) {
                try {
                    if (blocks[x + i][getSurfaceHeight(x, z)][z + j].getType() == 10) { //If there is a leaf block at a surface at the spot
                        return true;
                    }
                } catch (ArrayIndexOutOfBoundsException ex) { //Block deos not exist
                }
            }
        }
        return false; //Reaches here if a leaf block is not near
    }

    /**
     * Goes from the top of the map and continues to go down until it finds the
     * highest non-air Block in a specific spot, given the x and z coordinates.
     *
     * @param x The x coordinate of the spot
     * @param z The z coordinate of the splot
     * @return The y coordinate of the highest non-air Block
     */
    public int getSurfaceHeight(int x, int z) {
        int counter = Main.MAX_BLOCKS_Y - 1;
        while (blocks[x][counter][z].getType() == 0) {
            counter--;
        }
        return counter;
    }
    
    /**
     *
     * @return The Blocks that this Chunk contains.
     */
    public Block[][][] getBlocks() {
        return blocks;
    }

    /**
     *
     * @return The x coordinate of the Chunk within the World
     */
    public int getX() {
        return x;
    }

    /**
     *
     * @return The y coordinate of the Chunk within the world
     */
    public int getY() {
        return y;
    }

    /**
     *
     * @return The Sunlight of the Chunk
     */
    public Sunlight getSunlight() {
        return sunlight;
    }

    /**
     *
     * @return The futrue that keeps tarck of the updateCollision thread
     */
    public Future getUpdateCollisionFuture() {
        return updateCollisionFuture;
    }

    /**
     *
     * @return The chunk's node
     */
    public Node getNode() {
        return node;
    }

    /**
     *
     * @return Whether or not the chunk is currently rendered and in physics
     */
    public boolean isLoaded() {
        return loaded;
    }
}
