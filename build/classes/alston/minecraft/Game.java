package alston.minecraft;

import com.jme3.math.Vector3f;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all game variables and handles initialization of a few of the
 * Objects. There should only be one instance of this running at all times, but
 * there can be multiple games held in Main.
 *
 * @author Alston
 * @version RTM
 */
public final class Game implements Serializable {

    //Private constant used for Object serialization
    private static final long serialVersionUID = 3724124124812838L;
    //Fields
    private HashMap<MapKey, Chunk> world; //Links to every chunk that is created; The Key Object is the MapKey inner class
    private ArrayList<Item> droppedItems; //List of items that was discarded and have no parent, this keeps a refrence on Serialization
    private Player player;
    private CopyOnWriteArrayList<Mob> mobs;
    private InventoryBar inventoryBar;
    private int time; //Represents the time in the game
    private int currentChunkX, currentChunkY; //Records the current chunk where the player is on
    private transient Future changeLightsFuture;
    private transient Runnable changeChunkLights;

    /**
     * Used as an Object that will be used as a Key for the HashMap to keep
     * track of which Chunk belongs to what coordinates.
     */
    class MapKey implements Serializable {

        private int x, y; //Coordinates

        /**
         * Constructs a new mapKey with the given Chunk coordinates to be used
         * for the HashMap.
         *
         * @param x The x coordinate of the Chunk
         * @param y The y coordinate of the Chunk
         */
        public MapKey(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            int hash = 1; //Initial hash value
            //Adds additional values to hash based on the int variables multiplied by prime numbers
            hash = hash + 7 * x;
            hash = hash + 17 * y;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) { //Returns false if the parameter is a null refrence
                return false;
            }
            if (getClass() != obj.getClass()) { //Ensures that the given Object is a MapKey
                return false;
            }
            final MapKey other = (MapKey) obj; //Casts MapKey to the Object
            //Ensures the parameters are equal
            if (this.x != other.x) {
                return false;
            }
            if (this.y != other.y) {
                return false;
            }
            return true;
        }
    }

    /**
     * Constructor for Game. Instantiates a new World, sets the player's
     * location based on the world, and initiates values for all variables.
     */
    public Game() {
        inventoryBar = new InventoryBar();
        player = new Player(inventoryBar); //Passes the refrence down so Item does not have to refrence this while constructing
        currentChunkX = 0;
        currentChunkY = 0;
        world = new HashMap();
        droppedItems = new ArrayList<Item>();
        mobs = new CopyOnWriteArrayList();
        //Creates chunks and adds to HashMap
        for (int i = -1; i <= 1; i++) { //X dimension of the chunks array
            for (int j = -1; j <= 1; j++) { //Y dimention of the chunks array
                Chunk chunk = new Chunk(i, j);
                world.put(new MapKey(i, j), chunk);
            }
        }
        setupRunnable();
        setTime(8); //Starts at sunrise 
        player.getControl().setPhysicsLocation(new Vector3f(0, ((Chunk) world.get(new MapKey(0, 0))).getSurfaceHeight(0, 0) + 2, 0));
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
        input.defaultReadObject();
        setupRunnable();
    }

    /**
     * Sets up the runnable thread.
     */
    private void setupRunnable() {
        changeChunkLights = new Runnable() { //Changes the lights of all the chunks
            public void run() {
                Iterator<Chunk> iterator = world.values().iterator();
                float factor = (float) Math.sin((time / 24f * Math.PI));
                while (iterator.hasNext()) {
                    Chunk chunk = iterator.next();
                    chunk.getSunlight().factorIntensity(factor);
                }
            }
        };
    }

    /**
     * Gets the Chunk with the specified coordinates by creating a new MapKey
     * with the parameters, and using it as a Key in the HashMap to get the
     * Chunk.
     *
     * @param x The x coordinate of the Chunk
     * @param y The y coordinate of the Chunk
     * @return The Chunk obtained from the HashMap with the specified
     * coordinates, or null if it does not exist
     */
    public Chunk getChunk(int x, int y) {
        return (Chunk) world.get(new MapKey(x, y));
    }

    /**
     * Maps the given Chunk to the specified coordinates by creating a new
     * MapKey with the parameters and using it as a Key to put the Chunk in the
     * HashMap. Note that there should only be one Chunk that corrosponds to any
     * single Key (and therefore 1 Chunk for each x and y coordinates
     * combination).
     *
     * @param chunk The Chunk to be mapped
     * @param x The x coordinate of the Chunk
     * @param y The y coodinate of the Chunk
     */
    public void add(Chunk chunk, int x, int y) {
        world.put(new MapKey(x, y), chunk);
        chunk.getSunlight().factorIntensity((float) Math.sin((time / 24f * Math.PI))); //Sets the appropriate light level
    }

    /**
     *
     * @return X coordinate of the Chunk the player is on
     */
    public int getCurrentChunkX() {
        return currentChunkX;
    }

    /**
     *
     * @return Y coordinate of the Chunk the player is on
     */
    public int getCurrentChunkY() {
        return currentChunkY;
    }

    /**
     * Gets all the currently loaded Chunks.
     *
     * @return A List containing all the existing Chunks.
     */
    public LinkedList getLoadedChunks() {
        Iterator<Chunk> iterator = world.values().iterator();
        LinkedList list = new LinkedList();
        while (iterator.hasNext()) {
            Chunk chunk = iterator.next();
            if (chunk.isLoaded()) {
                list.add(chunk);
            }
        }
        return list;
    }

    /**
     *
     * @return The ArrayList containing all Items dropped
     */
    public ArrayList getDroppedItems() {
        return droppedItems;
    }

    /**
     *
     * @return The Player that represents the user
     */
    public Player getPlayer() {
        return player;
    }

    /**
     *
     * @return The InventoryBar containing all the Bar Pictures
     */
    public InventoryBar getInventoryBar() {
        return inventoryBar;
    }

    /**
     *
     * @param currentChunkX The new X coordinate of the Chunk the player is on
     */
    public void setCurrentChunkX(int currentChunkX) {
        this.currentChunkX = currentChunkX;
    }

    /**
     *
     * @param currentChunkY The new Y coordinate of the Chunk the player is on
     */
    public void setCurrentChunkY(int currentChunkY) {
        this.currentChunkY = currentChunkY;
    }

    /**
     *
     * @return A List containing all the existing Mobs
     */
    public CopyOnWriteArrayList<Mob> getMobs() {
        return mobs;
    }

    /**
     *
     * @return The current game time between 0 and 23, with 0 as midnight and 12
     * as noon.
     */
    public int getTime() {
        return time;
    }

    /**
     *
     * @param time A integer between 0 and 23 that represents the game time,
     * with 0 as midnight, 6 as sunrise, 12 as noon and 18 as sunset
     */
    public void setTime(int time) {
        this.time = time;
        if (changeLightsFuture == null) {
            changeLightsFuture = Main.executor.submit(changeChunkLights);
        }
    }

    /**
     * Checks to see if there is a thread to change the lighting currently
     * running and updates it if it is.
     */
    public void checkOnChangingLights() {
        if (changeLightsFuture != null) { //Running
            if (changeLightsFuture.isDone()) {
                if (changeLightsFuture.isCancelled()) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, "changeChunkLights was CANCELLED");
                }
                changeLightsFuture = null;
            }
        }
    }
}
