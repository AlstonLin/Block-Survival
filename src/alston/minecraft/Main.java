/**
 * THE MAJORITY OF ASSETS (TEXTURES, IMAGES, SOUNDS, ECT.) USED AND GAME
 * CONCEPTS ARE PROVIDED BY THE ORIGINAL MINECRAFT GAME BY MOJANG. SOME ASSETS
 * (SUCH AS MODELS, SOME TEXTURES AND IMAGES) ARE ORIGINAL AND CREATED BY ALSTON
 * LIN. MUSIC IN THE TRAILER IS FROM THE ORIGINAL MINECRAFT TRAILER MADE BY
 * VAREIDE (http://www.youtube.com/watch?v=FaMTedT6P0I). THE CODE CONTAINED WAS
 * CREATED WITH THE CONSULTATION OF THE JMONKEYENGINE DOCUMENTATION
 * (http://jmonkeyengine.org/wiki/doku.php).
 *
 * -----------------------SYSTEM REQUIREMENTS---------------------------------
 * JAVA OBJECT HEAP SIZE IS 4G; MIN 5GB RAM IS RECOMMENDED, USING CONCURRENT
 * LOW-PAUSE GARBAGE COLLECTER WITH MOPDIFIED NEW TO OLD GENERATION SETTINGS TO
 * REDUCE FULL GC PAUSES; MIN IS REQUIRED DUAL CORE PROCESSORS DUE TO MODIFIED
 * GC (Will be unplayable without) OPTIMAL FOR i5(Desktop), i7QM (Laptop) TO
 * PREVENT THREAD-RELATED BUGS/WORK PERFECTLY, ALONG WITH ~GT630M GPU OR BETTER.
 * TURNING ON HIGH QUALITY IS NOT RECOMMENDED UNLESS YOUR GRAPHICS CARD IS AT
 * LEAST GTX630 (Desktop)/GT650M/660M (Laptop) OR EQUIVALENT.
 *
 * ----------------------------INSTRUCTIONS------------------------------------
 * -24 hours, 1 hour is 30 seconds (12 min per day)
 *
 * -Mobs spawn from 8PM - 4AM
 *
 * -Once you die, it close sthe game without saving (will have to continue from
 * last saved place).
 *
 * -WASD Controls to walk, space to jump, right click to place/use items, hold
 * left mouse button to destroy, Q to drop the currently selected item
 *
 * -Left click to attack a mob
 *
 * -ESC to save and exit or close any GUI windows that are open, I to open up a
 * GUI window for inventory (drag and drop to palce items)
 *
 * -123456789 (non-keypad) for item selection
 *
 * -To create a crafting table, open inventory and collect wood from trees.
 * After you have done that, palce it in the top right corner of your inventory
 * window. The result should be wooden planks. Drag and Drop the wooden planks
 * into your inventory and then fill the crafting area with them. The result
 * should be a crafting table. Drag and drop the crafting table to the bottom
 * row, then close your inventory with ESC. Then select it with the
 * corrosponding key (123456789) and and left click.
 *
 * -To create a Wooden Pickaxe, right click on a crafting table, and place 3
 * wooden planks at the top row, followed by 2 sticks under the middle
 * cobblestone. To make a stone, iron and diamond, do the same but replace the
 * wooden planks with stone, iron, and diamond respectively
 *
 * -Pickaxes allow the player to mine faster
 *
 * -To create a torch, open either the inventory or crafting window, and place a
 * coal on top of a stick.
 *
 * -Stick is 2 wooden planks on top of each other, coal and ores must be mined-
 * ------------------------------KNOWN BUGS-------------------------------------
 * Known Bugs originating from a bug relating to falling in the jBullet
 * (http://jmonkeyengine.org/forum/topic/inconsistency-in-physic-world/):
 * -Walking off a Block will cause the player to fall faster then intended -
 * -Removing the Block underneath a Item after ~2 seconds of it spawning will
 * cause the Item not to fall unless an Item is thrown at it -----------------
 *
 * ----------Known Bugs originating from other bugs in jBullet----------------
 * -The player's physics control (and therefore camera) will randomly "slip" and
 * move a few barely noticable milimeters.
 *
 * -------------------------Other Bugs------------------------------------------
 * -Full GC ("Stop the World") pauses when loading too many Chunks in a short
 * period of time (caused by the Old Generation not clearing fast enough; this
 * bug is unfixable (except for increasing memory heap space) due to Java's GC
 * assumptions of long-term Objects in memory (Old Generation) that will rarely,
 * if ever, be eligible for Garbage Collection.
 *
 * -May experience laggs in lighting and as a result wierd lighting on slow
 * computers.
 *
 * -May have zombies spawnings when they should not be spawning (concurrency
 * issues with the Render, Physics, and Lighting threads.
 */
package alston.minecraft;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main class and therefore entry point, and serves the purpose of
 * containing the majority of the static variables and Object prototypes
 * (Prototype design pattern) that are the same and will not change for all
 * instances of Game, as well as constants, useful refrences (such as to the
 * main thread), and all initiations for those variables/prototypes. It also act
 * as the main bridge between the program written to the JMonkey library, by
 * having the Objects react using implmentations of abstract methods from the
 * SimpleApplication class. Note that the Main class is final so it cannot be
 * subclassed, as it uses a Singleton design pattern and therefore has a private
 * constructor. A Singleton is used due to the fact that the other class must
 * access non-static fields/methods in it's superclass (SimpleApplication), and
 * therefore needs an instance of Main in order to do so. However there must
 * only be one instance of Main, and hence the Singleton.
 *
 * @author Alston Lin
 * @version RTM
 */
public final class Main extends SimpleApplication {

    //Game Setting Constants
    /**
     * Saves game to a file upon exit if true, pernamently discards game upon
     * exit if false.
     */
    public static final boolean SAVE_ENABLED;
    /**
     * Does not use a control for the player (and therefore physics is not used
     * for the camara in the scene) if set to true.
     */
    public static final boolean FLY_MODE;
    /**
     * Defines the number of Block per Chunk in the X and Z axis.
     */
    public static final int MAX_BLOCKS;
    /**
     * Defines the number of Block per Chunk in the Y axis.
     */
    public static final int MAX_BLOCKS_Y;
    /**
     * Contains important information of the application, such as screen
     * dimensions.
     */
    public static final AppSettings APP_SETTINGS;
    //Package protected variables - can only be acessed from this package
    /**
     * Keeps a refrence to the main thread so methods can handle acesses to
     * scene graph properly.
     */
    static Thread mainThread;
    /**
     * If the game is over, uses it to keep track so it can end the game 1 frame
     * (and render) after the player has died.
     */
    static boolean gameOver;
    /**
     * Physics listener for the game.
     */
    static final BulletAppState bulletAppState;
    /**
     * The Spatial that every Block is based off; not actually used per se (in
     * itself), but is used to create clones of for the Spatials actually used.
     */
    static Geometry blockGeometry;
    /**
     * Node containing Spatials of all the Blocks that are rendered and used for
     * physics (anything but air).
     */
    static final Node blockNode;
    /**
     * Used to execute Callable/Runnable tasks in seperate threads.
     */
    static final ScheduledThreadPoolExecutor executor;
    /**
     * The current Game that is being played.
     */
    static Game currentGame;
    /**
     * Prototypes of every Block type; prototypes are used (but not actually
     * cloned) because it keep tracks of subtypes as well.
     */
    static final Block[] blockPrototypes;
    /**
     * Hash Map to map a Picture on the GUI to the Item. Used in order for
     * simplicity for clicking and dragging in the GUI.
     */
    static final HashMap picturesMap;
    /**
     * Keeps tracks of all the Recipies for crafting. Uses the Item (Objects
     * that will be used as keys/results) and EmptyItem (Object that extends
     * Item, used for simplifying) as type parameters.
     */
    static final RecipeMap<Item> recipeMap;
    /**
     * The currently showed GUI window
     */
    static Window shownWindow;
    //Private static variables; there should only be one instance of these at all times
    private static Main instance; //The current instance of Main (and it's superclass SimpleApplication)
    private static int chunkXModifier, chunkYModifier; //Tracking variables for chunk creation thread; can only move in 1 axis at a time
    private static long crossedChunkTime; //Records the time when it was first recorded user crossed a chunk
    private static long lastMobTick; //Keeps track of the last time mobs was updated
    private static long lastTimeChange; //Last time the time was changed
    private static boolean forwards, backwards, left, right; //Movement booleans
    private static boolean stopEmittingParticles; //Signal from actionListener to analogListener to stop emitting particles
    private static boolean soundPlayingLastTick = false; //Tracks if the sound was playing at the last tick
    private static boolean endGameNextTick = false; //Ends the game at the tick if it's true
    private static boolean gameStarted = false; //If the 3D part of the game has started
    private static boolean startGame = false; //Signals the update loop to start the game 
    private static boolean startGameNextTick = false; //Allows for 1 frame to pass by beforehand
    private static boolean newGame; //If the player has picked a new game or not
    private static boolean highQualityOn; //Determines if high quality is on or not
    private static Block lastClickedBlock; //To keep track of what was the last block clicked (for Block health resets)
    private static Picture loading; //Loading message
    private static Picture titleScreen; //The background for the title screen
    private static Picture highQuality, lowQuality; //Pictures that much have refrences to
    private static AudioNode step; //Sound of the player walkings
    private static ParticleEmitter particleEmitter; //Particle effect
    private static Future future; //Keeps track of multithreading
    private static Runnable changeChunks = new Runnable() { //A runnable task that will destroy and load chunks based on pre-set fields
        public void run() { //Destroys and creates chunks to change which chunks are loaded in a seperate thread
            //Destroys the chunks
            if (chunkXModifier != 0) { //If it is moving on the X axis
                for (int i = -1; i <= 1; i++) { //Modifies the action 3 times for each block along the X axis
                    currentGame.getChunk(currentGame.getCurrentChunkX() - chunkXModifier,
                            currentGame.getCurrentChunkY() + i).destroy(); //Destroys the chunk that player is moving away from
                    if (currentGame.getChunk(currentGame.getCurrentChunkX() + 2 * chunkXModifier,
                            currentGame.getCurrentChunkY() + i) == null) { //If the player has never loaded this chunk before
                        Chunk chunk = new Chunk(currentGame.getCurrentChunkX() + 2 * chunkXModifier, currentGame.getCurrentChunkY() + i);
                        currentGame.add(chunk, chunk.getX(), chunk.getY());
                    } else { //Player has been to this chunk before
                        currentGame.getChunk(currentGame.getCurrentChunkX() + 2 * chunkXModifier,
                                currentGame.getCurrentChunkY() + i).restore();
                    }
                }
            } else { //Moving on the Y axis
                for (int i = -1; i <= 1; i++) { //Modifies the action 3 times for each block along the Y axis
                    currentGame.getChunk(currentGame.getCurrentChunkX() + i,
                            currentGame.getCurrentChunkY() - chunkYModifier).destroy(); //Destroys the chunk that player is moving away from
                    if (currentGame.getChunk(currentGame.getCurrentChunkX() + i,
                            currentGame.getCurrentChunkY() + 2 * chunkYModifier) == null) { //Creates a new Chunk if player has never been here
                        Chunk chunk = new Chunk(currentGame.getCurrentChunkX() + i,
                                currentGame.getCurrentChunkY() + 2 * chunkYModifier);
                        currentGame.add(chunk, chunk.getX(), chunk.getY());
                    } else { //Player has been to this chunk before
                        currentGame.getChunk(currentGame.getCurrentChunkX() + i,
                                currentGame.getCurrentChunkY() + 2 * chunkYModifier).restore();
                    }
                }
            }
        }
    };

    /**
     * An enumerator so that a switch statement will work with the Strings of
     * the Action.
     */
    private enum Action {

        LEFT, RIGHT, FORWARDS, BACKWARDS, JUMP, LEFT_CLICK, RIGHT_CLICK, INVENTORY, ESCAPE, DROP;
    }//End of Action
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            try {
                Main.Action action = Main.Action.valueOf(name); //Changes the name to an Action
                switch (action) { //Checks if the action is any non-number keytrigger
                    //When the user jumps
                    case LEFT:
                        left = isPressed;
                        return;
                    case RIGHT:
                        right = isPressed;
                        return;
                    case FORWARDS:
                        forwards = isPressed;
                        return;
                    case BACKWARDS:
                        backwards = isPressed;
                        return;
                    case JUMP:
                        if (!isPressed) {
                            currentGame.getPlayer().getControl().jump();
                        }
                        return;
                    case ESCAPE: {
                        if (!isPressed) { //Only reacts if it is released
                            return;
                        }
                        if (shownWindow != null) { //A window is open; closes it
                            shownWindow.close();
                            shownWindow = null;
                            return;
                        }
                        Picture saving = new Picture("Saving");
                        saving.setWidth(Main.APP_SETTINGS.getWidth() / 2);
                        saving.setHeight(Main.APP_SETTINGS.getWidth() / 2);
                        saving.setQueueBucket(RenderQueue.Bucket.Gui);
                        saving.setImage(Main.getInstance().getAssetManager(), "Interface/saving.png", true);
                        saving.setPosition(Main.APP_SETTINGS.getWidth() / 4, 0);
                        guiNode.attachChild(saving);
                        gameOver = true;
                        return;
                    }
                    case INVENTORY:
                        if (isPressed) { //Only when the key is pressed
                            if (shownWindow == null) { //If the not shown, it shows it
                                currentGame.getPlayer().getInventory().show();
                            } else if (shownWindow == currentGame.getPlayer().getInventory()) { //If inventory is shown, it hides it
                                currentGame.getPlayer().getInventory().close();
                            }
                        }
                        return;
                    case DROP:
                        if (isPressed) { //Only does any action when the key is pressed
                            currentGame.getPlayer().getInventory().getInventorySpace().getItems()[currentGame.getPlayer().getInventory().getCurrentItem()][0].drop();
                        }
                    case LEFT_CLICK:
                        if (shownWindow != null) { //A Window is open
                            if (isPressed) { //Clicked
                                shownWindow.leftClick();
                            } else { //Released
                                shownWindow.release();
                            }
                            return;
                        }
                        //No window is open if it reaches here
                        if (!isPressed) { //Resets the block health on release
                            particleEmitter.killAllParticles(); //Stops emitting particles
                            particleEmitter.setNumParticles(0);
                            stopEmittingParticles = true; //Signals analog listener that emitting particles has stopped
                            if (lastClickedBlock == null) { //Prevents NullPointerException
                                return;
                            }
                            lastClickedBlock.resetHealth();
                            lastClickedBlock = null;
                        } else {
                            stopEmittingParticles = false; //Signals the listener to emit paricles
                        }
                        return;
                    case RIGHT_CLICK: { //Places a block (In braces to reduce the scope of local variables)
                        if (shownWindow != null && isPressed) { //Window is open
                            shownWindow.rightClick();
                            return;
                        }
                        if (!isPressed) { //Returns if it's the key bring released
                            return;
                        }
                        //Variable declarations for collision variables
                        CollisionResults collisionResults = new CollisionResults();
                        Ray ray = new Ray(cam.getLocation(), cam.getDirection()); //Where the user is pointing to
                        blockNode.collideWith(ray, collisionResults); //Finds all the blocks that the user is pointing to
                        if (collisionResults.size() == 0) { //Returns if it did not collide with any blocks
                            return;
                        }
                        Geometry closestBlock = collisionResults.getClosestCollision().getGeometry();
                        Block block = Utility.getBlock(closestBlock.getLocalTranslation().x, closestBlock.getLocalTranslation().y,
                                closestBlock.getLocalTranslation().z);
                        if (block instanceof Clickable) { //Clickable Block
                            ((Clickable) block).click();
                            return;
                        }
                        if (collisionResults.getClosestCollision().getDistance() <= 6f) {
                            int xModifier = 0, yModifier = 0, zModifier = 0;
                            if (Utility.round(collisionResults.getClosestCollision().getContactPoint().getX()) == closestBlock
                                    .getLocalTranslation().getX() + 0.5) { //Right clicked on the +X side
                                xModifier = 1;
                                yModifier = 0;
                                zModifier = 0;
                            } else if (Utility.round(collisionResults.getClosestCollision().getContactPoint().getX()) == closestBlock
                                    .getLocalTranslation().getX() - 0.5) { //Right clicked on the -X side
                                xModifier = -1;
                                yModifier = 0;
                                zModifier = 0;
                            } else if (Utility.round(collisionResults.getClosestCollision().getContactPoint().getZ()) == closestBlock
                                    .getLocalTranslation().getZ() + 0.5) { //Right clicked on the +Z side
                                xModifier = 0;
                                yModifier = 0;
                                zModifier = 1;
                            } else if (Utility.round(collisionResults.getClosestCollision().getContactPoint().getZ()) == closestBlock
                                    .getLocalTranslation().getZ() - 0.5) { //Right clicked on the -Z side
                                xModifier = 0;
                                yModifier = 0;
                                zModifier = -1;
                            } else if (Utility.round(collisionResults.getClosestCollision().getContactPoint().getY()) == closestBlock
                                    .getLocalTranslation().getY() + 0.5) { //Right clicked above
                                xModifier = 0;
                                yModifier = 1;
                                zModifier = 0;
                            } else if (Utility.round(collisionResults.getClosestCollision().getContactPoint().getY()) == closestBlock
                                    .getLocalTranslation().getY() - 0.5) { //Right clicked below
                                xModifier = 0;
                                yModifier = -1;
                                zModifier = 0;
                            }
                            currentGame.getPlayer().getInventory().getInventorySpace().getItems()[currentGame.getPlayer().getInventory().getCurrentItem()][0]
                                    .rightClick((int) closestBlock.getLocalTranslation().getX(), (int) closestBlock.getLocalTranslation().getY(),
                                    (int) closestBlock.getLocalTranslation().getZ(), xModifier, yModifier, zModifier);
                        }
                    }
                }
            } catch (IllegalArgumentException e) { //If it's not defined in Action (and therefore a number)
                try {
                    int actionNum = Integer.parseInt(name);
                    if (actionNum == 0) { //0 does not corrospond to any inventory slot
                        return;
                    }
                    currentGame.getPlayer().getInventory().setCurrentItem(actionNum - 1); //Gives the illusion to the user that the keyboard numbers corrosponds to inventory slots
                    currentGame.getInventoryBar().getSelector().setPosition((currentGame.getPlayer().getInventory().getCurrentItem() + 1) * settings.getWidth() / 11, 0); //Changes which item is selectored
                } catch (NumberFormatException ex) { //If I lied and it wasn't a number for some reason
                }
            }
        }
    }; //End of actionListener
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            if (shownWindow != null) { //Window is open
                if (shownWindow.getClickedItem() != null) { //An item was clicked
                    shownWindow.getClickedItem().getPicture().setPosition(inputManager.getCursorPosition().x - settings.getHeight() / 25.5f,
                            inputManager.getCursorPosition().y - settings.getHeight() / 25.5f); //Puts the Picture at the same place as cursor
                    shownWindow.getClickedItem().getNumberText().setLocalTranslation(shownWindow.getClickedItem()
                            .getPicture().getLocalTranslation().add(0, Main.APP_SETTINGS.getHeight() / 30f, 0)); //Moves number text as well
                }
            } else { //No window is open
                //Variable declarations for collision variables
                CollisionResults collisionResults = new CollisionResults();
                Ray ray = new Ray(cam.getLocation(), cam.getDirection()); //Where the user's crosshairs is pointing
                blockNode.collideWith(ray, collisionResults); //Finds all the blocks that the user is pointing to
                if (collisionResults.size() > 0 && collisionResults.getClosestCollision().getDistance() <= 6f) { //If the user clicked on anything and if it's in range

                    Geometry closestGeom = collisionResults.getClosestCollision().getGeometry();//Just so the next line isn't unreadebly long
                    Mob potentialMob = Mob.spatialMap.get(closestGeom.getParent());
                    if (potentialMob != null) { //It was actually a Mob
                        currentGame.getPlayer().attack(potentialMob);
                        return;
                    }
                    Block block = currentGame.getChunk(currentGame.getCurrentChunkX(), currentGame.getCurrentChunkY()).getBlock((int) closestGeom
                            .getLocalTranslation().getX() - currentGame.getCurrentChunkX() * MAX_BLOCKS, (int) closestGeom.getLocalTranslation().getY(),
                            (int) closestGeom.getLocalTranslation().getZ() - currentGame.getCurrentChunkY() * MAX_BLOCKS); //For readability

                    //Checks if the Item is a tool, and multiplies appropriately
                    Item currentItem = currentGame.getPlayer().getInventory().getInventorySpace().getItems()[currentGame.getPlayer()
                            .getInventory().getCurrentItem()][0];

                    if (currentItem instanceof Tool) { //It's a tool
                        switch (block.getBase()) {
                            case Block.AIR_BASED:
                                return; //Can't break a Air Block
                            case Block.DIRT_BASED:
                                value *= ((Tool) currentItem).getDirtMultiplier();
                                break;
                            case Block.ROCK_BASED:
                                value *= ((Tool) currentItem).getRockMultiplier();
                                break;
                            case Block.WOOD_BASED:
                                value *= ((Tool) currentItem).getWoodMultiplier();
                                break;
                        }
                    }

                    if (lastClickedBlock == null) { //If the user didn't left click the previous tick or the previous block was destroyed
                        if (!stopEmittingParticles) { //Bug Fix (to ensure harmony between actionListener)
                            lastClickedBlock = block;
                            block.setHealth(block.getHealth() - value);
                            //Gets particles to appear while breaking
                            particleEmitter.setLocalTranslation(block.getSpatial().getLocalTranslation());
                            particleEmitter.emitAllParticles();
                            particleEmitter.setNumParticles(30);
                        }
                    } else if (lastClickedBlock == block) { //If the user clicked on the same block he did in the last tick
                        block.setHealth(block.getHealth() - value);
                    } else { //If it's a new block 
                        particleEmitter.setLocalTranslation(block.getSpatial().getLocalTranslation()); //Moves the particle emitter
                        lastClickedBlock.resetHealth();
                        block.setHealth(block.getHealth() - value);
                    }
                    if (block.getHealth() <= 0) { //If the block is destroyed
                        block.changeToBlock(0, true);
                        lastClickedBlock = null;
                        //Stops emitting particles
                        particleEmitter.killAllParticles();
                        particleEmitter.setNumParticles(0);
                    }
                }
            }
        }
    };
    private ActionListener preGameListener = new ActionListener() { //Listens before the game started
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("LEFT_CLICK") && isPressed) { //Left click and Pressed
                Iterator<CollisionResult> collidedPics = Utility.getCollidedPictures(guiNode);
                while (collidedPics.hasNext()) {
                    CollisionResult result = collidedPics.next();
                    if (result.getGeometry().getName().equals("New Game")) { //New Game Button has been clicked
                        setupLoadingMessage();
                        startGameNextTick = true;
                        newGame = true;
                    } else if (result.getGeometry().getName().equals("Load Game")) { //Load Game Button has been clicked
                        setupLoadingMessage();
                        startGameNextTick = true;
                        newGame = false;
                    } else if (result.getGeometry().getName().equals("High Quality")) { //Changing to low quality
                        highQualityOn = false;
                        //Changes the pictures
                        guiNode.detachChild(highQuality);
                        guiNode.attachChild(lowQuality);
                    } else if (result.getGeometry().getName().equals("Low Quality")) { //Changing to high quality
                        highQualityOn = true;
                        //Changes the pictures
                        guiNode.detachChild(lowQuality);
                        guiNode.attachChild(highQuality);
                    }
                }
            }
        }
    };

    static { //Static initialization block
        SAVE_ENABLED = true;
        FLY_MODE = false;
        MAX_BLOCKS = 32;
        MAX_BLOCKS_Y = 64;
        stopEmittingParticles = true;
        blockNode = new Node("Blocks");
        blockPrototypes = new Block[32];
        bulletAppState = new BulletAppState();
        executor = new ScheduledThreadPoolExecutor(8);
        picturesMap = new HashMap();
        particleEmitter = new ParticleEmitter("Breaking Effect", ParticleMesh.Type.Triangle, 50);
        recipeMap = new RecipeMap(EmptyItem.class);
        APP_SETTINGS = new AppSettings(true);
        lastTimeChange = System.nanoTime();
        loading = new Picture("Loading");
        titleScreen = new Picture("Title Screen");
    }

    /**
     * Only exists so that other classes cannot create instances of Main (simply
     * calls the superclass).
     */
    private Main() {
        super();
    }

    /**
     * This is the main method of the program, and therefore entry point.
     * Prepares the logger settings, as well as creating a new instance of the
     * Main class, which is nessecary to use the inherited non-static methods
     * from SimpleApplication. It also defines at what settings the
     * SimpleApplication will run at. Should not be explicitly called.
     *
     * @param args Useless/Not Used. Only a parameter as a result of convention.
     */
    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.SEVERE);//So the console wouldn't be spammed by useless messages from JME
        try {
            Logger.getLogger("").addHandler(new FileHandler("log.log"));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Defines window size
        APP_SETTINGS.put("Height", 720);
        APP_SETTINGS.put("Width", 1280);
        //Defines window title
        APP_SETTINGS.put("Title", "Block Survival");
        //APP_SETTINGS.setFrameRate(59); //Max 59FPS for stability
        instance = new Main(); //Starts a new instance of the app (JMonkey Window)
        instance.setSettings(APP_SETTINGS);
        instance.setShowSettings(false); //Disables popup screen
        instance.start(); //Starts the game
    }//End of Main

    /**
     * Called by the super class SimpleApplication upon creation to set up
     * variables and Objects, as well as calling initialization methods for the
     * title screen.
     */
    @Override
    public void simpleInitApp() {
        Picture newGameButton = new Picture("New Game");
        Picture loadGame = new Picture("Load Game");
        lowQuality = new Picture("Low Quality");
        highQuality = new Picture("High Quality");
        //Shows Title Screen
        titleScreen.setWidth(APP_SETTINGS.getWidth());
        titleScreen.setHeight(APP_SETTINGS.getHeight());
        titleScreen.setQueueBucket(RenderQueue.Bucket.Gui);
        titleScreen.setImage(assetManager, "Interface/titleScreen.png", true);
        guiNode.attachChild(titleScreen);
        //Shows newGameButton button
        newGameButton.setWidth(APP_SETTINGS.getWidth() / 3);
        newGameButton.setHeight(APP_SETTINGS.getHeight() / 5);
        newGameButton.setQueueBucket(RenderQueue.Bucket.Gui);
        newGameButton.setImage(assetManager, "Interface/newGame.png", true);
        newGameButton.setPosition(APP_SETTINGS.getWidth() / 8, APP_SETTINGS.getHeight() / 3);
        guiNode.attachChild(newGameButton);
        //Shows the loadGame button
        loadGame.setWidth(APP_SETTINGS.getWidth() / 3);
        loadGame.setHeight(APP_SETTINGS.getHeight() / 5);
        loadGame.setQueueBucket(RenderQueue.Bucket.Gui);
        loadGame.setImage(assetManager, "Interface/loadGame.png", true);
        loadGame.setPosition(APP_SETTINGS.getWidth() / 2, APP_SETTINGS.getHeight() / 3);
        guiNode.attachChild(loadGame);
        //Sets up high and low quality and shows the low quality by default
        lowQuality.setWidth(APP_SETTINGS.getHeight() / 10);
        lowQuality.setHeight(APP_SETTINGS.getHeight() / 10);
        lowQuality.setQueueBucket(RenderQueue.Bucket.Gui);
        lowQuality.setImage(assetManager, "Interface/lowQuality.png", true);
        lowQuality.setPosition(0, 9 * APP_SETTINGS.getHeight() / 10);
        guiNode.attachChild(lowQuality);
        highQuality.setWidth(APP_SETTINGS.getHeight() / 10);
        highQuality.setHeight(APP_SETTINGS.getHeight() / 10);
        highQuality.setQueueBucket(RenderQueue.Bucket.Gui);
        highQuality.setImage(assetManager, "Interface/highQuality.png", true);
        highQuality.setPosition(0, 9 * APP_SETTINGS.getHeight() / 10);
        //Sets up the mappings  
        inputManager.addMapping("LEFT_CLICK", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(preGameListener, "LEFT_CLICK");
        //Disables statistics
        setDisplayFps(false);
        setDisplayStatView(false);
        stateManager.attach(bulletAppState); //This prevents a NullPointer once the Utility class loads
    }// End of simpleInitApp 

    private void setupLoadingMessage() {
        loading.setWidth(Main.APP_SETTINGS.getWidth() / 2);
        loading.setHeight(Main.APP_SETTINGS.getWidth() / 2);
        loading.setQueueBucket(RenderQueue.Bucket.Gui);
        loading.setImage(Main.getInstance().getAssetManager(), "Interface/loading.png", true);
        loading.setPosition(Main.APP_SETTINGS.getWidth() / 4, 0);
        guiNode.detachAllChildren(); //Removes title screen
        guiNode.attachChild(titleScreen); //A nice background for the loading screen
        guiNode.attachChild(loading);
    }

    /**
     * Initialized all 3D Objects and sets up the Game based on the pre-set
     * constants.
     */
    private void startGame() {
        inputManager.removeListener(preGameListener);
        //Sets ups variables/Objects
        mainThread = Thread.currentThread();
        step = new AudioNode(assetManager, "Sounds/step.wav", false); //Step sound
        step.setLooping(true);
        step.setVolume(0.05f);
        blockGeometry = (Geometry) assetManager.loadModel("Models/Block.j3o"); //Casted to allow for vertices manipulation
        flyCam.setMoveSpeed(5f);
        cam.setFrustumPerspective(35, settings.getWidth() / settings.getHeight(), 0.2f, 90); //Adjusts frustrum near and far planes
        //Initalization methods
        setupCrossHairs();
        setupKeys();
        setupBlockPrototypes();
        setupLighting();
        setupEmitter();
        if (highQualityOn) {
            setupPostProcessor();
        }
        try {
            setupRecipes();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Either sets the current game as a newly created game, or a saved game
        if (newGame) {
            currentGame = new Game();
        } else { //Otherwise load the game from file
            load();
            currentGame.getPlayer().getInventory().restore(); //Sets up inventory once Game is initialized
            Item.restoreSpatials(); //Workaround for a bug between Deserialization and JME's rootNode
        }
        //Sky background
        viewPort.setBackgroundColor(ColorRGBA.Blue);
        //Adds everything to rootNode
        rootNode.attachChild(particleEmitter);
        rootNode.attachChild(blockNode);
        guiNode.detachChild(loading);
        guiNode.detachChild(titleScreen);
        inputManager.setCursorVisible(false);
        gameStarted = true;
    }

    /**
     * Called by the SimpleApplication super class for every frame. It is called
     * by the main/render Thread, so any changes in scene graph is Thread-safe.
     * The code contained in this method checks on other threads, and updates
     * the camera to the player's location.
     *
     * @param tpf The time interval between the current frame and the previous
     */
    @Override
    public void simpleUpdate(float tpf) {
        if (gameStarted) {
            checkOnChunkThreads();
            Light.checkOnFuture();
            if (!FLY_MODE) { //Only updates the player's and camera's location if flying is not enabled
                updatePlayer();
            }
            checkForChunkChange();
            if (System.nanoTime() > lastMobTick + 1e9) { //More than 1 second has past since last tick
                updateMobs();
                lastMobTick = System.nanoTime();
            }
            currentGame.checkOnChangingLights();
            checkForTimeChange();
            //Updates the sound listener lcoation
            listener.setLocation(cam.getLocation());
            listener.setRotation(cam.getRotation());
            if (endGameNextTick) {
                if (currentGame.getPlayer().getHealthBar().getHealth() <= 0) { //Player has died
                    endGame();
                } else { //Just saving
                    stop();
                }
            } else if (gameOver) {
                endGameNextTick = true;
            }
        } else {
            if (!inputManager.isCursorVisible()) { //Bug Fix at initialization
                inputManager.setCursorVisible(true);
            }
            if (startGame) { //Play has been clicked; game will now start
                startGame();
            } else if (startGameNextTick) { //Allows to render 1 frame before it starts to show loading message
                startGame = true;
            }
        }
    }//End of simpleUpdate  

    /**
     * Checks if the time should be changed.
     */
    private void checkForTimeChange() {
        if (System.nanoTime() > lastTimeChange + 30e9) { //1 hour every 30 seconds
            currentGame.setTime(currentGame.getTime() < 23 ? currentGame.getTime() + 1 : 0); //Increments or sets it to 0 at 23
            lastTimeChange = System.nanoTime();
            checkForMobSpawn();
        }
    }

    /**
     * The player has died and shows the Game Over message then pauses the
     * trhead so the player can read before closing the program.
     */
    private void endGame() {
        try {
            Thread.sleep(3000); //Waits 3 seconds for user to read message
        } catch (InterruptedException ex) {
            Logger.getLogger(HealthBar.class.getName()).log(Level.SEVERE, null, ex);
        }
        stop();
    }

    /**
     * Checks every Chunk to see if there are any threads that needs to be
     * checked on.
     */
    private void checkOnChunkThreads() {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Chunk chunk = currentGame.getChunk(currentGame.getCurrentChunkX() + i, currentGame.getCurrentChunkY() + j);
                try {
                    if (chunk.getUpdateCollisionFuture() != null) { //If there is a thread updating the chunk's shape
                        chunk.updateCollisionShape(); //Checks on the thread to see if the task is finished
                    }
                } catch (NullPointerException e) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    /**
     * Spawns mobs at the surface when the light of it is 2f or lower whenever
     * Chunks or time is changed.
     */
    private void checkForMobSpawn() {
        if (currentGame.getMobs().size() >= 3 || (currentGame.getTime() > 4 && currentGame.getTime() < 20)) { //Max 3 mobs on at a time and in the night
            return;
        }
        Iterator<Chunk> iterator = currentGame.getLoadedChunks().iterator();
        while (iterator.hasNext()) { //Iterates thru all chunks that are loaded
            Chunk chunk = iterator.next();
            for (int i = 0; i < MAX_BLOCKS; i++) { //All x and z coordinates
                for (int j = 0; j < MAX_BLOCKS; j++) {
                    Block surfaceBlock = chunk.getBlock(i, chunk.getSurfaceHeight(i, j), j);
                    if (surfaceBlock.getActualLightLevel() < 1.5f) { //Light is lower than 1.5f
                        if (chunk.getBlock(surfaceBlock.getX(), surfaceBlock.getY() + 1, surfaceBlock.getZ()).getType() == 0
                                && chunk.getBlock(surfaceBlock.getX(), surfaceBlock.getY() + 2, surfaceBlock.getZ()).getType() == 0) { //2 Air Blocks above
                            if (Math.random() < 0.0002) { //Mob spawn chance
                                currentGame.getMobs().add(new Zombie(surfaceBlock.getX() + chunk.getX() * MAX_BLOCKS,
                                        surfaceBlock.getY() + 1, surfaceBlock.getZ() + chunk.getY() * MAX_BLOCKS));
                            }
                        }
                    }
                    if (currentGame.getMobs().size() >= 3) { //Can't have more than 3 mobs
                        return;
                    }
                }
            }
        }
    }

    /**
     * Updates the player's control's location according to the values set by
     * the key listeners, then updates the camera location to the player's
     * control's location in the scene graph.
     */
    private void updatePlayer() {
        Vector3f direction = new Vector3f(0f, 0f, 0f); //Direction that the player will walk
        Vector3f camX = cam.getLeft().clone().multLocal(0.075f); //Left (+) and Right (-)
        Vector3f camZ = cam.getDirection().clone().multLocal(0.075f); //Forward (+) and Back (-)
        if (left) {
            direction.addLocal(camX);
        }
        if (right) {
            direction.addLocal(camX.negate()); //Negative value of the left direction
        }
        if (forwards) {
            direction.addLocal(camZ);
        }
        if (backwards) {
            direction.addLocal(camZ.negate()); //Negative value of forwards
        }
        currentGame.getPlayer().getControl().setWalkDirection(direction);
        if (!soundPlayingLastTick && !direction.equals(Vector3f.ZERO)) {
            step.play();
            soundPlayingLastTick = true;
        } else if (soundPlayingLastTick && direction.equals(Vector3f.ZERO)) {
            step.stop();
            soundPlayingLastTick = false;
        }
        cam.setLocation(currentGame.getPlayer().getControl().getPhysicsLocation().add(0, 0.75f, 0)); //Changes view location when player moves
    }

    /**
     * If the thread for changing Chunks is not running, it checks if chunks
     * need to be changed, otherwise it checks on the thread.
     */
    private void checkForChunkChange() {
        if (future == null) {
            if (!isOnCurrentChunk()) { //User is not on the chunk 
                if (crossedChunkTime == 0) { //Was on the chunk the previous tick
                    crossedChunkTime = System.currentTimeMillis(); //Records the time it was crossed
                } else if (System.currentTimeMillis() - crossedChunkTime > 500) { //If the player crossed over for 0.5 sec consecutively
                    //Determines the delta of the new chunk location
                    if (currentGame.getPlayer().getControl().getPhysicsLocation().x < currentGame.getCurrentChunkX() * MAX_BLOCKS) { //-1 in the X axis
                        chunkXModifier = -1;
                        chunkYModifier = 0;
                    } else if (currentGame.getPlayer().getControl().getPhysicsLocation().x >= (currentGame.getCurrentChunkX() + 1) * MAX_BLOCKS) { //+1 in the Y axis
                        chunkXModifier = 1;
                        chunkYModifier = 0;
                    } else if (currentGame.getPlayer().getControl().getPhysicsLocation().z < currentGame.getCurrentChunkY() * MAX_BLOCKS) { //-1 in Y axis
                        chunkXModifier = 0;
                        chunkYModifier = -1;
                    } else if (currentGame.getPlayer().getControl().getPhysicsLocation().z >= (currentGame.getCurrentChunkY() + 1) * MAX_BLOCKS) { //+1 in Y axis
                        chunkXModifier = 0;
                        chunkYModifier = 1;
                    }
                    changeChunks(); //Starts a new thread to generate/destroy the chunks
                }
            } else { //Went back to current chunk
                crossedChunkTime = 0; //Resets timer
            }
        } else {
            changeChunks(); //Checks on the progress 
        }
    }

    /**
     * Attaches crosshairs at the center of the screen to the guiNode. Should
     * only be called when the application starts.
     */
    private void setupCrossHairs() {
        BitmapText crosshairs;
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt"); //Loads font
        crosshairs = new BitmapText(guiFont, false);
        crosshairs.setSize(30);
        crosshairs.setText("+");
        crosshairs.setLocalTranslation(settings.getWidth() / 2, settings.getHeight() / 2, -2); //Puts crosshairs in exactly the middle of the screen
        guiNode.attachChild(crosshairs);
    }//End of addCrossHairs

    /**
     * Sets up the key bindings and attaches them to the appropriate listeners.
     * Should only be called when the application starts.
     */
    private void setupKeys() {
        //Removes ESC as the default close
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
        //Defines key mappings
        inputManager.addMapping("LEFT", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("RIGHT", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("FORWARDS", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("BACKWARDS", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("JUMP", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("RIGHT_CLICK", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("LEFT_CLICK", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("INVENTORY", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("DROP", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("0", new KeyTrigger(KeyInput.KEY_0));
        inputManager.addMapping("1", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("2", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addMapping("3", new KeyTrigger(KeyInput.KEY_3));
        inputManager.addMapping("4", new KeyTrigger(KeyInput.KEY_4));
        inputManager.addMapping("5", new KeyTrigger(KeyInput.KEY_5));
        inputManager.addMapping("6", new KeyTrigger(KeyInput.KEY_6));
        inputManager.addMapping("7", new KeyTrigger(KeyInput.KEY_7));
        inputManager.addMapping("8", new KeyTrigger(KeyInput.KEY_8));
        inputManager.addMapping("9", new KeyTrigger(KeyInput.KEY_9));
        inputManager.addMapping("ESCAPE", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(actionListener, "JUMP", "LEFT", "FORWARDS", "BACKWARDS", "RIGHT", "RIGHT_CLICK", "LEFT_CLICK",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "INVENTORY", "ESCAPE", "DROP"); //Adds all the bindings to the actionListener
        inputManager.addListener(analogListener, "LEFT_CLICK");
    }//End of setupKeys

    /**
     * Creates Arrays of Items to be Keys to be Mapped to a resulting Item for
     * crafting.
     */
    private void setupRecipes() throws CloneNotSupportedException {
        Item[][][] recipes = new Item[16][3][3]; //Temporarily stores the all the recipies
        Item[] results = new Item[16];
        for (int i = 0; i < recipes.length; i++) { //Makes all indexes EmptyItem by default
            for (int j = 0; j < recipes[0].length; j++) {
                for (int k = 0; k < recipes[0][0].length; k++) {
                    recipes[i][j][k] = new EmptyItem();
                }
            }
        }
        //FORMAT FOR RECIPIES: recipes[index][column][row] = ITEMS[Item.ItemType]
        //Wooden Plank 
        recipes[0][0][0] = Item.ITEMS[Item.WOOD];
        results[0] = Item.ITEMS[Item.WOODEN_PLANKS].clone();
        results[0].setAmount(4);
        //Crafting Table
        recipes[1][0][0] = Item.ITEMS[Item.WOODEN_PLANKS];
        recipes[1][0][1] = Item.ITEMS[Item.WOODEN_PLANKS];
        recipes[1][1][0] = Item.ITEMS[Item.WOODEN_PLANKS];
        recipes[1][1][1] = Item.ITEMS[Item.WOODEN_PLANKS];
        results[1] = Item.ITEMS[Item.WORKBENCH];
        //Furnace
        recipes[2][0][0] = Item.ITEMS[Item.COBBLESTONE];
        recipes[2][0][1] = Item.ITEMS[Item.COBBLESTONE];
        recipes[2][0][2] = Item.ITEMS[Item.COBBLESTONE];
        recipes[2][1][0] = Item.ITEMS[Item.COBBLESTONE];
        recipes[2][1][2] = Item.ITEMS[Item.COBBLESTONE];
        recipes[2][2][0] = Item.ITEMS[Item.COBBLESTONE];
        recipes[2][2][1] = Item.ITEMS[Item.COBBLESTONE];
        recipes[2][2][2] = Item.ITEMS[Item.COBBLESTONE];
        results[2] = Item.ITEMS[Item.FURNACE];
        //Stick
        recipes[3][0][0] = Item.ITEMS[Item.WOODEN_PLANKS];
        recipes[3][0][1] = Item.ITEMS[Item.WOODEN_PLANKS];
        results[3] = Item.ITEMS[Item.STICK].clone();
        results[3].setAmount(4);
        //Wood Pickaxe
        recipes[4][0][2] = Item.ITEMS[Item.WOODEN_PLANKS];
        recipes[4][1][2] = Item.ITEMS[Item.WOODEN_PLANKS];
        recipes[4][2][2] = Item.ITEMS[Item.WOODEN_PLANKS];
        recipes[4][1][1] = Item.ITEMS[Item.STICK];
        recipes[4][1][0] = Item.ITEMS[Item.STICK];
        results[4] = Item.ITEMS[Item.WOODEN_PICKAXE];
        //Stone Pickaxe
        recipes[5][0][2] = Item.ITEMS[Item.COBBLESTONE];
        recipes[5][1][2] = Item.ITEMS[Item.COBBLESTONE];
        recipes[5][2][2] = Item.ITEMS[Item.COBBLESTONE];
        recipes[5][1][1] = Item.ITEMS[Item.STICK];
        recipes[5][1][0] = Item.ITEMS[Item.STICK];
        results[5] = Item.ITEMS[Item.STONE_PICKAXE];
        //Torch
        recipes[6][0][1] = Item.ITEMS[Item.COAL];
        recipes[6][0][0] = Item.ITEMS[Item.STICK];
        results[6] = Item.ITEMS[Item.TORCH].clone();
        results[6].setAmount(4);
        //Maps the recipes to the results
        for (int i = 0; i < 7; i++) {
            recipeMap.put(recipes[i], results[i]);
        }
        recipeMap.sortKeys(); //Must be sorted to do a binary search
    }

    /**
     * Initiates all Block prototypes. Should only be called at the start of the
     * application.
     */
    private void setupBlockPrototypes() {
        //Initiates materials
        Material[] materials = new Material[32];
        for (int i = 0; i < materials.length; i++) {
            materials[i] = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            materials[i].setBoolean("UseVertexColor", true); //Allows for vertex manipulation
        }
        //Sets the materials' textures and sets them transparent is applicable
        materials[Item.AIR].setTexture("DiffuseMap", assetManager.loadTexture("Textures/air.png")); //This texture should never be actually used
        materials[Item.DIRT].setTexture("DiffuseMap", assetManager.loadTexture("Textures/dirt.png"));
        materials[Item.GRASS].setTexture("DiffuseMap", assetManager.loadTexture("Textures/grass.png"));
        materials[Item.BEDROCK].setTexture("DiffuseMap", assetManager.loadTexture("Textures/bedrock.png"));
        materials[Item.STONE].setTexture("DiffuseMap", assetManager.loadTexture("Textures/stone.png"));
        materials[Item.COAL_BLOCK].setTexture("DiffuseMap", assetManager.loadTexture("Textures/coalBlock.png"));
        materials[Item.IRON_BLOCK].setTexture("DiffuseMap", assetManager.loadTexture("Textures/ironBlock.png"));
        materials[Item.GOLD_BLOCK].setTexture("DiffuseMap", assetManager.loadTexture("Textures/goldBlock.png"));
        materials[Item.DIAMOND_BLOCK].setTexture("DiffuseMap", assetManager.loadTexture("Textures/diamondBlock.png"));
        materials[Item.WOOD].setTexture("DiffuseMap", assetManager.loadTexture("Textures/wood.png"));
        materials[Item.LEAVES].setTexture("DiffuseMap", assetManager.loadTexture("Textures/leaves.png"));
        materials[Item.WOOL].setTexture("DiffuseMap", assetManager.loadTexture("Textures/wool.png"));
        materials[Item.COBBLESTONE].setTexture("DiffuseMap", assetManager.loadTexture("Textures/cobblestone.png"));
        materials[Item.WOODEN_PLANKS].setTexture("DiffuseMap", assetManager.loadTexture("Textures/woodenPlanks.png"));
        materials[Item.WORKBENCH].setTexture("DiffuseMap", assetManager.loadTexture("Textures/workbench.png"));
        materials[Item.FURNACE].setTexture("DiffuseMap", assetManager.loadTexture("Textures/furnace.png"));
        materials[Item.TORCH].setTexture("DiffuseMap", assetManager.loadTexture("Textures/torch.png"));

        blockPrototypes[Item.AIR] = new Block(materials[Item.AIR], Item.AIR, Item.NAN, Block.AIR_BASED);
        blockPrototypes[Item.DIRT] = new Block(materials[Item.DIRT], Item.DIRT, Block.DIRT_BASED);
        blockPrototypes[Item.GRASS] = new Block(materials[Item.GRASS], Item.GRASS, Block.DIRT_BASED);
        blockPrototypes[Item.BEDROCK] = new Block(materials[Item.BEDROCK], Item.BEDROCK, Block.ROCK_BASED);
        blockPrototypes[Item.STONE] = new Block(materials[Item.STONE], Item.STONE, Item.COBBLESTONE, Block.ROCK_BASED);
        blockPrototypes[Item.COAL_BLOCK] = new Block(materials[Item.COAL_BLOCK], Item.COAL_BLOCK, Item.COAL, Block.ROCK_BASED);
        blockPrototypes[Item.IRON_BLOCK] = new Block(materials[Item.IRON_BLOCK], Item.IRON_BLOCK, Block.ROCK_BASED);
        blockPrototypes[Item.GOLD_BLOCK] = new Block(materials[Item.GOLD_BLOCK], Item.GOLD_BLOCK, Block.ROCK_BASED);
        blockPrototypes[Item.DIAMOND_BLOCK] = new Block(materials[Item.DIAMOND_BLOCK], Item.DIAMOND_BLOCK, Block.ROCK_BASED);
        blockPrototypes[Item.WOOD] = new Block(materials[Item.WOOD], Item.WOOD, Block.WOOD_BASED);
        blockPrototypes[Item.LEAVES] = new Block(materials[Item.LEAVES], Item.LEAVES, Item.NAN, Block.DIRT_BASED);
        blockPrototypes[Item.WOOL] = new Block(materials[Item.WOOL], Item.WOOL, Block.DIRT_BASED);
        blockPrototypes[Item.COBBLESTONE] = new Block(materials[Item.COBBLESTONE], Item.COBBLESTONE, Block.ROCK_BASED);
        blockPrototypes[Item.WOODEN_PLANKS] = new Block(materials[Item.WOODEN_PLANKS], Item.WOODEN_PLANKS, Block.WOOD_BASED);
        blockPrototypes[Item.WORKBENCH] = new CraftingTable(materials[Item.WORKBENCH]);
        blockPrototypes[Item.FURNACE] = new Block(materials[Item.FURNACE], Item.FURNACE, Block.ROCK_BASED);
        blockPrototypes[Item.TORCH] = new Torch(materials[Item.TORCH], Item.ITEMS[Item.TORCH].getSpatial());

    }//End of loadMaterials

    /**
     * Adjusts the settings for the particle emitter, responsible for special
     * effects. Should only be called during the start of the application.
     */
    private void setupEmitter() {
        //Sets up particle emitter
        Material particleMaterial = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        particleMaterial.setTexture("Texture", assetManager.loadTexture("Textures/particles.png"));
        particleEmitter.setMaterial(particleMaterial);
        particleEmitter.setLowLife(0.3f);
        particleEmitter.setHighLife(0.5f);
        particleEmitter.setStartSize(0.04f);
        particleEmitter.setEndSize(0.03f);
        particleEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 3f, 0));
        particleEmitter.getParticleInfluencer().setVelocityVariation(1);
        particleEmitter.setGravity(0, 5, 0);
        particleEmitter.setParticlesPerSec(60);
        particleEmitter.setImagesX(3);
        particleEmitter.setImagesY(3);
        particleEmitter.setSelectRandomImage(true);
        particleEmitter.setRotateSpeed(10);
        particleEmitter.setRandomAngle(true);
        particleEmitter.setStartColor(ColorRGBA.Brown);
        particleEmitter.setEndColor(ColorRGBA.Brown);
    }

    /**
     * Sets up the lighting of the World.
     */
    private void setupLighting() {
        AmbientLight ambientLight = new AmbientLight();
        //Lowers light
        ambientLight.setColor(ColorRGBA.White);
        rootNode.addLight(ambientLight);
    }

    /**
     * Sets up post-processor effects such as Ambient Occlusion and sunlight.
     */
    private void setupPostProcessor() {
        FilterPostProcessor filterProcessor = new FilterPostProcessor(assetManager);
        //Ambient Occlusion
        filterProcessor.addFilter(new SSAOFilter(0.5f, 3f, 2f, 0.3f));
        viewPort.addProcessor(filterProcessor);
    }

    /**
     * Starts a thread to change which chunks are loaded, or checks on the
     * progress if already done so. If it is being started, chunkXModifier and
     * chunkYModifier must be adjusted to define how the loaded chunks will
     * change.
     */
    private void changeChunks() {
        try {
            if (future == null) { //If the thread has not begun
                future = executor.submit(changeChunks); //Starts the thread running the runnable task
            } else { //Checks on the thread
                //Get the waylist when its done
                if (future.isDone()) { //If it finished loading the thread, adds chunk to the world
                    crossedChunkTime = 0; //Resets timer
                    //Adjusts the game variables to change the current middle chunk
                    currentGame.setCurrentChunkX(currentGame.getCurrentChunkX() + chunkXModifier);
                    currentGame.setCurrentChunkY(currentGame.getCurrentChunkY() + chunkYModifier);
                    future = null;
                    despawnMobs();
                    checkForMobSpawn();
                } else if (future.isCancelled()) { //Canceled for some reason
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, "changeChunks was CANCELLED");
                    future = null;
                }
            }
        } catch (Exception e) { //One of the dozens of exeption was thrown
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Updates every Mob on the map. TODO: CHANGE THIS INTO CUSTOM CONTROLS.
     */
    private void updateMobs() {
        Iterator<Mob> iterator = currentGame.getMobs().iterator();
        while (iterator.hasNext()) {
            iterator.next().update();
        }
    }

    /**
     * Checks to see if any Mobs need to be despawned.
     */
    private void despawnMobs() {
        Iterator<Mob> iterator = currentGame.getMobs().iterator();
        while (iterator.hasNext()) {
            Mob mob = iterator.next();
            if (mob.getControl().getPhysicsLocation().distance(currentGame.getPlayer().getControl().getPhysicsLocation()) > 45) {
                mob.despawn();
            }
        }
    }

    /**
     * Used to get the Singleton instance of Main (instanceof
     * SimpleApplication). Lazy initiation not used due to the fact that
     * instance would've already been initated before any classes could call
     * this (initated in main method).
     *
     * @return The current instance of Main
     */
    public static Main getInstance() {
        return instance;
    }

    /**
     * Called to read a file containing a Game object, and assign the Object
     * obtained to currentGame. Should only be called at the start of the
     * application.
     */
    private void load() {
        try {
            //Loads from a saved game
            ObjectInputStream input = new ObjectInputStream(new FileInputStream("World.sav"));
            currentGame = (Game) input.readObject();
            input.close();
            return;
        } //End of load
        catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Something went wrong; will not create a new Game
        currentGame = new Game();
    }//End of load

    /**
     * Called whenever the program is PROPERLY exited; saves the current Game to
     * a file if SAVE_ENABLED is true, or else it does nothing.
     */
    @Override
    public void stop() {
        listener.setVolume(0); //No annoying sounds
        if (SAVE_ENABLED && currentGame.getChunk(0, 0).getBlock(0, 0, 1) != null
                && currentGame.getPlayer().getHealthBar().getHealth() > 0) { //If saving is enabled and the Game loaded properly and player is not dead
            try { //Trys to save the game to a file
                ObjectOutputStream save = new ObjectOutputStream(new FileOutputStream("World.sav"));
                save.writeObject(currentGame);
                save.flush();
                save.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.ALL, null, ex);
            }
        }
        super.stop();
    }//End of stop

    /**
     * Called whenever the program is stopped, with the method of which it was
     * stopped irrelevant (will be called even if it the Main thread threw and
     * uncaught exception and crashed); stops all other Threads to prevent other
     * Threads of running after the program stops.
     */
    @Override
    public void destroy() {
        super.destroy();
        executor.shutdown(); //If the main thread is stopped, all other threads will stop as well
    }//End of destroy

    /**
     * Evaluates whether or not the player is currently on or near chunk
     * [CurrentChunkX, currentChunkY] in the world.
     *
     * @return A boolean representing if the player is on the current chunk
     */
    private static boolean isOnCurrentChunk() {
        if (currentGame.getPlayer().getControl().getPhysicsLocation().x >= currentGame.getCurrentChunkX() * MAX_BLOCKS - 10
                && currentGame.getPlayer().getControl().getPhysicsLocation().x < (currentGame.getCurrentChunkX() + 1) * MAX_BLOCKS + 10
                && currentGame.getPlayer().getControl().getPhysicsLocation().z >= currentGame.getCurrentChunkY() * MAX_BLOCKS - 10
                && currentGame.getPlayer().getControl().getPhysicsLocation().z < (currentGame.getCurrentChunkY() + 1) * MAX_BLOCKS + 10) {
            return true;
        }
        return false;
    }
}//End of Main Class

