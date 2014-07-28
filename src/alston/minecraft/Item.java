package alston.minecraft;

import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A generalization of all the types of Items stacks, and holds all essential
 * variables and handles interactions that occurs between them.
 *
 * @author Alston
 * @version RTM
 */
public abstract class Item implements Cloneable, Serializable, Comparable {

    //Public static constants defining Items
    public static final int NAN = -1; //For Nothing
    public static final int AIR = 0;
    public static final int DIRT = 1;
    public static final int GRASS = 2;
    public static final int BEDROCK = 3;
    public static final int STONE = 4;
    public static final int COAL_BLOCK = 5;
    public static final int IRON_BLOCK = 6;
    public static final int GOLD_BLOCK = 7;
    public static final int DIAMOND_BLOCK = 8;
    public static final int WOOD = 9;
    public static final int LEAVES = 10;
    public static final int WOOL = 11;
    public static final int COBBLESTONE = 12;
    public static final int WOODEN_PLANKS = 13;
    public static final int WORKBENCH = 14;
    public static final int FURNACE = 15;
    public static final int TORCH = 16;
    public static final int WOODEN_PICKAXE = 17;
    public static final int STONE_PICKAXE = 18;
    public static final int IRON_PICKAXE = 19;
    public static final int GOLD_PICKAXE = 20;
    public static final int DIAMOND_PICKAXE = 21;
    public static final int WOODEN_SHOVEL = 22;
    public static final int STONE_SHOVEL = 23;
    public static final int IRON_PSHOVEL = 24;
    public static final int GOLD_SHOVEL = 25;
    public static final int DIAMOND_SHOVEL = 26;
    public static final int WOODEN_AXE = 27;
    public static final int STONE_AXE = 28;
    public static final int IRON_AXE = 29;
    public static final int GOLD_AXE = 30;
    public static final int DIAMOND_AXE = 31;
    public static final int WOODEN_SWORD = 32;
    public static final int STONE_SWORD = 33;
    public static final int IRON_SWORD = 34;
    public static final int GOLD_SWORD = 35;
    public static final int DIAMOND_SWORD = 36;
    public static final int STICK = 37;
    public static final int COAL = 38;
    /**
     * Prototype of every Item in the game. It is not used by the game per se,
     * but is used to be cloned, where the clones are the actual Items being
     * used.
     */
    public static final Item[] ITEMS;
    /**
     * Maps the Spatials to the Item for collision detection.
     */
    public static final HashMap itemSpatialsMap = new HashMap();
    protected static final AudioNode place; //Placing sound to be used by placeble items
    //Private constant used for identification when saving
    private static final long serialVersionUID = 3724124124812947L;
    private static final AudioNode pop; //Pop sound for picking up
    private static transient LinkedList itemsToLoad; //Fixes a bug upon Deserialization
    //Fields (transient = will not serialize)
    private transient Picture picture; //Picture when the player opens a Window with this contained
    private transient Picture barPicture; //Picture shown on the inventory bar at the bottom of the screen
    private transient BitmapText numberText; //The text that shows the amount of Items
    private transient BitmapText barNumberText; //The text that shows the amount of Items on the bar
    private transient Spatial spatial; //The spatial of the Item when it is dropped
    private transient RigidBodyControl control;
    private transient long droppedTime; //Delays between drop and pickups
    private int type; //Numerical representation of the Item
    private int amount; //Number of the Items the stack has
    private int maxAmount; //The maximum amount of the Item a stack of it can contain
    private int slotX, slotY; //The Item's slot in the inventory
    private SlotArea parent; //The area of slots that this Item is currently in
    private Block blockUnder; //The Block udnerneath if it's in the scene graph

    static {
        ITEMS = new Item[128];
        pop = new AudioNode(Main.getInstance().getAssetManager(), "Sounds/pop.wav", false);
        pop.setVolume(0.2f);
        place = new AudioNode(Main.getInstance().getAssetManager(), "Sounds/place.wav", false);
        place.setVolume(0.2f);
        setupItems();
        itemsToLoad = new LinkedList();
    }

    /**
     * Constructor for an Will never directly be called during an instantiation,
     * but will be called from the subclass' constructor. Default maxAmount is
     * 64, and amount is 1.
     *
     * @param type An integer representation of what item it is. Used to help
     * organize and keep track of Objects in the program.
     * @param spatial The spatial of the Item when thrown or equipped
     */
    public Item(int type, Spatial spatial, Picture picture) {
        this(type, spatial, picture, 64);
    }

    /**
     * Constructor for an Item, with a pre-defined maximum amount. Will never
     * directly be called during an instantiation, but will be called from the
     * subclass' constructor.
     *
     * @param type An integer representation of what item it is. Used to help
     * organize and keep track of Objects in the program.
     * @param maxAmount The maximum amount of Items that a stack of his can have
     */
    public Item(int type, Spatial spatial, Picture picture, int maxAmount) {
        this.type = type;
        this.maxAmount = maxAmount;
        this.spatial = spatial;
        this.picture = picture;
        amount = 1;
        setupPictures();
    }

    /**
     * Initiates the templates for all instances of Item, as well as their
     * associated Picture. Should only be called at the start of the
     * application.
     */
    private static void setupItems() {
        Spatial[] itemSpatials = new Spatial[8];
        Material[] itemMaterials = new Material[8];
        Picture[] itemPictures = new Picture[128];
        //Instanitiates and adjusts Pictures
        for (int i = 0; i < itemPictures.length; i++) {
            itemPictures[i] = new Picture("Item ID#" + i);
            itemPictures[i].setWidth(Main.APP_SETTINGS.getWidth() / 11);
            itemPictures[i].setHeight(Main.APP_SETTINGS.getWidth() / 11);
            itemPictures[i].setQueueBucket(Bucket.Gui);
        }
        //Sets the picture's images
        itemPictures[AIR].setImage(Main.getInstance().getAssetManager(), "Interface/emptyImage.png", true);
        itemPictures[DIRT].setImage(Main.getInstance().getAssetManager(), "Interface/dirtImage.png", true);
        itemPictures[GRASS].setImage(Main.getInstance().getAssetManager(), "Interface/grassImage.png", true);
        itemPictures[BEDROCK].setImage(Main.getInstance().getAssetManager(), "Interface/bedrockImage.png", true);
        itemPictures[STONE].setImage(Main.getInstance().getAssetManager(), "Interface/stoneImage.png", true);
        itemPictures[COAL_BLOCK].setImage(Main.getInstance().getAssetManager(), "Interface/coalBlockImage.png", true);
        itemPictures[IRON_BLOCK].setImage(Main.getInstance().getAssetManager(), "Interface/ironBlockImage.png", true);
        itemPictures[GOLD_BLOCK].setImage(Main.getInstance().getAssetManager(), "Interface/goldBlockImage.png", true);
        itemPictures[DIAMOND_BLOCK].setImage(Main.getInstance().getAssetManager(), "Interface/diamondBlockImage.png", true);
        itemPictures[WOOD].setImage(Main.getInstance().getAssetManager(), "Interface/woodImage.png", true);
        itemPictures[LEAVES].setImage(Main.getInstance().getAssetManager(), "Interface/leavesImage.png", true);
        itemPictures[WOOL].setImage(Main.getInstance().getAssetManager(), "Interface/woolImage.png", true);
        itemPictures[COBBLESTONE].setImage(Main.getInstance().getAssetManager(), "Interface/cobblestoneImage.png", true);
        itemPictures[WOODEN_PLANKS].setImage(Main.getInstance().getAssetManager(), "Interface/woodenPlanksImage.png", true);
        itemPictures[WORKBENCH].setImage(Main.getInstance().getAssetManager(), "Interface/workbenchImage.png", true);
        itemPictures[FURNACE].setImage(Main.getInstance().getAssetManager(), "Interface/furnaceImage.png", true);
        itemPictures[STICK].setImage(Main.getInstance().getAssetManager(), "Interface/stick.png", true);
        itemPictures[TORCH].setImage(Main.getInstance().getAssetManager(), "Interface/torch.png", true);
        itemPictures[WOODEN_PICKAXE].setImage(Main.getInstance().getAssetManager(), "Interface/woodPickaxe.png", true);
        itemPictures[STONE_PICKAXE].setImage(Main.getInstance().getAssetManager(), "Interface/stonePickaxe.png", true);
        itemPictures[IRON_PICKAXE].setImage(Main.getInstance().getAssetManager(), "Interface/ironPickaxe.png", true);
        itemPictures[GOLD_PICKAXE].setImage(Main.getInstance().getAssetManager(), "Interface/goldPickaxe.png", true);
        itemPictures[DIAMOND_PICKAXE].setImage(Main.getInstance().getAssetManager(), "Interface/diamondPickaxe.png", true);
        itemPictures[COAL].setImage(Main.getInstance().getAssetManager(), "Interface/coal.png", true);

        //Non-Block Spatials
        itemSpatials[0] = Main.getInstance().getAssetManager().loadModel("Models/Stick.j3o");
        itemSpatials[1] = Main.getInstance().getAssetManager().loadModel("Models/Pickaxe.j3o");
        itemSpatials[2] = itemSpatials[1].clone(true);
        itemSpatials[3] = itemSpatials[1].clone(true);
        itemSpatials[4] = itemSpatials[1].clone(true);
        itemSpatials[5] = itemSpatials[1].clone(true);
        itemSpatials[6] = Main.getInstance().getAssetManager().loadModel("Models/Coal.j3o");
        itemSpatials[7] = Main.getInstance().getAssetManager().loadModel("Models/Torch.j3o");

        for (int i = 0; i < itemMaterials.length; i++) { //Initiates Materials
            itemMaterials[i] = new Material(Main.getInstance().getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        }

        //Sets the Material's textures
        itemMaterials[0].setTexture("DiffuseMap", Main.getInstance().getAssetManager().loadTexture("Textures/stick.png"));
        itemMaterials[1].setTexture("DiffuseMap", Main.getInstance().getAssetManager().loadTexture("Textures/woodPickaxe.png"));
        itemMaterials[2].setTexture("DiffuseMap", Main.getInstance().getAssetManager().loadTexture("Textures/stonePickaxe.png"));
        itemMaterials[3].setTexture("DiffuseMap", Main.getInstance().getAssetManager().loadTexture("Textures/ironPickaxe.png"));
        itemMaterials[4].setTexture("DiffuseMap", Main.getInstance().getAssetManager().loadTexture("Textures/goldPickaxe.png"));
        itemMaterials[5].setTexture("DiffuseMap", Main.getInstance().getAssetManager().loadTexture("Textures/diamondPickaxe.png"));
        itemMaterials[6].setTexture("DiffuseMap", Main.getInstance().getAssetManager().loadTexture("Textures/coal.png"));
        itemMaterials[7].setTexture("DiffuseMap", Main.getInstance().getAssetManager().loadTexture("Textures/torch.png"));

        for (int i = 0; i < itemSpatials.length; i++) { //Attaches it to the Spatials
            itemSpatials[i].setMaterial(itemMaterials[i]);
        }

        //Initiates Items
        ITEMS[AIR] = new EmptyItem(itemPictures[AIR]);
        ITEMS[DIRT] = new BlockItem(itemPictures[DIRT], DIRT, 0.75f);
        ITEMS[GRASS] = new BlockItem(itemPictures[GRASS], GRASS, 0.75f);
        ITEMS[BEDROCK] = new BlockItem(itemPictures[BEDROCK], BEDROCK, Float.POSITIVE_INFINITY);
        ITEMS[STONE] = new BlockItem(itemPictures[STONE], STONE, 5f);
        ITEMS[COAL_BLOCK] = new BlockItem(itemPictures[COAL_BLOCK], COAL_BLOCK, 5f);
        ITEMS[IRON_BLOCK] = new BlockItem(itemPictures[IRON_BLOCK], IRON_BLOCK, 7.5f);
        ITEMS[GOLD_BLOCK] = new BlockItem(itemPictures[GOLD_BLOCK], GOLD_BLOCK, 7.5f);
        ITEMS[DIAMOND_BLOCK] = new BlockItem(itemPictures[DIAMOND_BLOCK], DIAMOND_BLOCK, 8.5f);
        ITEMS[WOOD] = new BlockItem(itemPictures[WOOD], WOOD, 1.5f);
        ITEMS[LEAVES] = new BlockItem(itemPictures[LEAVES], LEAVES, 0.5f);
        ITEMS[WOOL] = new BlockItem(itemPictures[WOOL], WOOL, 0.5f);
        ITEMS[COBBLESTONE] = new BlockItem(itemPictures[COBBLESTONE], COBBLESTONE, 5f);
        ITEMS[WOODEN_PLANKS] = new BlockItem(itemPictures[WOODEN_PLANKS], WOODEN_PLANKS, 2f);
        ITEMS[WORKBENCH] = new BlockItem(itemPictures[WORKBENCH], WORKBENCH, 2.5f, 1);
        ITEMS[FURNACE] = new BlockItem(itemPictures[FURNACE], FURNACE, 8.5f, 1);
        ITEMS[STICK] = new Tool(STICK, 1, 1, 1, 1.2f, itemSpatials[0], itemPictures[STICK], 64);
        ITEMS[TORCH] = new TorchItem(itemSpatials[7], itemPictures[TORCH]);
        ITEMS[WOODEN_PICKAXE] = new Tool(WOODEN_PICKAXE, 1.5f, 1, 1, 1.3f, itemSpatials[1], itemPictures[WOODEN_PICKAXE]);
        ITEMS[STONE_PICKAXE] = new Tool(STONE_PICKAXE, 3.5f, 1, 1, 1.5f, itemSpatials[2], itemPictures[STONE_PICKAXE]);
        ITEMS[IRON_PICKAXE] = new Tool(IRON_PICKAXE, 5f, 1, 1, 1.9f, itemSpatials[3], itemPictures[IRON_PICKAXE]);
        ITEMS[GOLD_PICKAXE] = new Tool(GOLD_PICKAXE, 7f, 1, 1, 2.5f, itemSpatials[4], itemPictures[GOLD_PICKAXE]);
        ITEMS[DIAMOND_PICKAXE] = new Tool(DIAMOND_PICKAXE, 10f, 1, 1, 3f, itemSpatials[5], itemPictures[DIAMOND_PICKAXE]);
        ITEMS[COAL] = new SimpleItem(COAL, itemSpatials[6], itemPictures[COAL]);

    }//End of setupItems

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
        try{
        spatial = ITEMS[type].getSpatial().clone(true);
        } catch (NullPointerException e){
        }
        if (blockUnder != null) { //If it is in the World and not in a Window
            itemsToLoad.add(this);
        }
        setupPictures();
        setAmount(amount); //Sets the number's bitmap's text
    } //End of readObject

    /**
     * Restores the Item in the scene graph.
     */
    private void restore() {
        drop(blockUnder);
    }
    
    /**
     * Restores all the Spatials of items in the scene graph.
     */
      public static void restoreSpatials(){
        Iterator<Item> iterator = itemsToLoad.iterator();
        while(iterator.hasNext()){
            Item item = iterator.next();
            item.drop(item.blockUnder);
        }
    }
    /**
     * Defines what happens when the player right clicks with this
     *
     * @param x The x coordinate of the block that was clicked on
     * @param y The y coordinate of the block that was clicked on
     * @param z The z coordinate of the block that was clicked on
     * @param xModifier The face in the X axis the block was clicked on
     * @param yModifier The face in the Y axis the block was clicked on
     * @param zModifier The face in the Z axis the block was clicked on
     */
    public abstract void rightClick(int x, int y, int z, int xModifier, int yModifier, int zModifier);

    /**
     * When the Item is dropped by the player.
     */
    public void drop() {
        if (type == AIR) { //Can't drop nothing
            return;
        }
        if (amount == 1) { //One left; removes Item and drops it
            parent.remove(slotX, slotY); //Removes from the parent
            parent = null;
            dropSpatial();
            return;
        }
        //More than one; Splits to 2 stacks and drops one
        setAmount(amount - 1);
        try {
            ITEMS[type].clone().dropSpatial();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Item.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * When the Item is dropped without force.
     *
     * @param block The Block that it was transformed from
     */
    public void drop(Block block) {
        blockUnder = block;
        Utility.setLightLevel(spatial, block.getLightLevel());
        spatial.scale(0.2f);
        spatial.setLocalTranslation(block.getX() + block.getParent().getX() * Main.MAX_BLOCKS, block.getY(), block.getZ() + block.getParent().getY() * Main.MAX_BLOCKS);
        setupControl();
        addToWorld();
    }

    /**
     * Transforms the Item from a SlotArea to the Spatial in the rootNode, then
     * applies force to it depending on where player is looking.
     */
    private void dropSpatial() {
        spatial.scale(0.2f);
        spatial.setLocalTranslation(Main.currentGame.getPlayer().getControl().getPhysicsLocation().addLocal(Main.getInstance().getCamera()
                .getDirection()).add(0, 1f, 0)); //So player doesn't pick it right back up
        setupControl();
        addToWorld();
        control.applyCentralForce(Main.getInstance().getCamera().getDirection().multLocal(5f)); //Initial "push"
        droppedTime = System.nanoTime(); //Tracks the time this was dropped 
    }

    /**
     * Sets up the Item's Spatial's physics control.
     */
    private void setupControl() {
        control = new RigidBodyControl(new BoxCollisionShape(new Vector3f(0.22f, 0.22f, 0.22f)), 0.05f); //Larger than spatial to prevent bugs
        spatial.addControl(control);
    }

    /**
     * Adds the Spatial to the rootNode and physics.
     */
    private void addToWorld() {
        if (this instanceof BlockItem) { //Only applies to BlockItems to save memory
            getSpatial().setMaterial(Main.blockPrototypes[getType()].getMaterial());
        }
        Main.getInstance().getRootNode().attachChild(spatial);
        Main.bulletAppState.getPhysicsSpace().add(control);
        itemSpatialsMap.put(control, this);
        try {
            Main.currentGame.getDroppedItems().add(this); //Keeps a refrence to this for Serialization
        } catch (NullPointerException ex) { //If it reaches here, it means that it is currently being De-Serialized
        }
    }

    /**
     * Picks up the Item and puts it in the inventory specified.
     */
    public void pickUp() {
        if (System.nanoTime() <= droppedTime + 1e9) { //Does not pick up if it's < 1sec from dropping
            return;
        }
        spatial.scale(5f);
        //Removes spatial and physics
        spatial.removeFromParent();
        Main.bulletAppState.getPhysicsSpace().remove(control);
        itemSpatialsMap.remove(control); //Prevents memory leak and lessens HashMap complexity
        Main.picturesMap.put(picture, this); //Remaps the picture
        Main.currentGame.getPlayer().getInventory().getInventorySpace().add(this);
        //Saves memory by null refrencing 
        control = null;
        pop.playInstance(); //Plays the pop sound
        Main.currentGame.getDroppedItems().remove(this); //Removes it from the list of dropped item
        blockUnder = null;
    }

    @Override
    public Item clone() throws CloneNotSupportedException, NullPointerException {
        Item cloned = (Item) super.clone();
        //Clones all non-primitive fields
        cloned.setupPictures();
        cloned.spatial = spatial.clone(true);
        return cloned;
    } //End of clone

    @Override
    public int compareTo(Object item) {
        return type - ((Item) item).type;
    }

    /**
     * Sets up the Item's Pictures, including the BitmapText representing the
     * amount.
     */
    private void setupPictures() {
        //Sets up the number's bitmap text image
        numberText = new BitmapText(Main.getInstance().getAssetManager().loadFont("Interface/Fonts/Default.fnt"), false);
        numberText.setSize(20);
        numberText.setQueueBucket(Bucket.Gui);
        barNumberText = new BitmapText(Main.getInstance().getAssetManager().loadFont("Interface/Fonts/Default.fnt"), false);
        barNumberText.setSize(20);
        barNumberText.setQueueBucket(Bucket.Gui);
        //Clones Pictures
        try {
            picture = (Picture) ITEMS[type].getBarPicture().clone();
        } catch (NullPointerException e) { //It's being setup
        }
        barPicture = (Picture) picture.clone();
        //Adjusts the picture's dimensions
        picture.setWidth(Main.APP_SETTINGS.getHeight() / 12.75f);
        picture.setHeight(Main.APP_SETTINGS.getHeight() / 12.75f);
        Main.picturesMap.put(picture, this); //Maps the Picture to the Item
    }

    /**
     * Detaches all the Item's graphics from their parents.
     */
    public void detachFromAll() {
        parent.getNode().detachChild(picture);
        parent.getNode().detachChild(numberText);
        Main.currentGame.getInventoryBar().detach(this);
        Main.picturesMap.remove(picture); //Removes from HashMap to prevent a memory leak
    }

    /**
     *
     * @return Picture of the Item's icon in the inventory menu
     */
    public Picture getPicture() {
        return picture;
    }

    /**
     *
     * @return The Picture representing the Item on the inventory bar
     */
    public Picture getBarPicture() {
        return barPicture;
    }

    /**
     *
     * @return The BitmapText of the amount number
     */
    public BitmapText getNumberText() {
        return numberText;
    }

    /**
     *
     * @return The BitmapText of the number on the Inventory bar
     */
    public BitmapText getBarNumberText() {
        return barNumberText;
    }

    /**
     *
     * @return An integer representation of the Item
     */
    public int getType() {
        return type;
    }

    /**
     *
     * @return The x coordinate of the Item within the slotArea
     */
    public int getSlotX() {
        return slotX;
    }

    /**
     *
     * @return The y coordinate of the Item within the slotArea
     */
    public int getSlotY() {
        return slotY;
    }

    /**
     *
     * @return The amount of Items in this stack of Items
     */
    public int getAmount() {
        return amount;
    }

    /**
     *
     * @return Maximum amount of Items that can be in one stack
     */
    public int getMaxAmount() {
        return maxAmount;
    }

    /**
     *
     * @return The current SlotArea that the Item is in; returns null if it is
     * not in any
     */
    public SlotArea getParent() {
        return parent;
    }

    /**
     *
     * @return The Spatial of the dropped Item
     */
    public Spatial getSpatial() {
        return spatial;
    }

    /**
     *
     * @return The physics control for this Item
     */
    public RigidBodyControl getControl() {
        return control;
    }

    /**
     * Sets the amount of Items the stack of it contains, then updates the
     * Item's BitmapText representation of the amount (number). If the amount is
     * 1, it will not show the amount.
     *
     * @param amount The new amount of Items
     */
    public void setAmount(int amount) {
        this.amount = amount;
        if (amount == 1) { //Does not show any text if it's only 1
            numberText.setText("");
            barNumberText.setText("");
        } else if (getAmount() <= 0) { //There is no more of this Item in the stack; removes all refrences to this Item to be eligible for GC
            if (parent.getItems()[slotX][slotY] == this) { //Only removes it if it's actually from that slot, and not simply the past parent
                parent.remove(slotX, slotY);
            }
        } else { //Updates the number's image if it's not 1
            numberText.setText(Integer.toString(amount));
            barNumberText.setText(Integer.toString(amount));
        }
    }

    /**
     *
     * @param barPicture The new picture representing the Item on the inventory
     * bar
     */
    public void setBarPicture(Picture barPicture) {
        this.barPicture = barPicture;
    }

    /**
     *
     * @param parent The new SlotArea that the Item is attached to
     */
    public void setParent(SlotArea parent) {
        this.parent = parent;
    }

    /**
     *
     * @param slotX The new x coordinate of the Item within the slotArea
     * @param slotY The new y coordinate of the Item within the slotArea
     */
    public void setSlotPosition(int slotX, int slotY) {
        this.slotX = slotX;
        this.slotY = slotY;
    }

    /**
     *
     * @param spatial The new Spatial of this Item
     */
    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
    }

    /**
     *
     * @param control The new physics control for the Item
     */
    public void setControl(RigidBodyControl control) {
        this.control = control;
    }
}
