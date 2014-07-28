package alston.minecraft;

import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import java.io.Serializable;

/**
 * A Clickable Block that can show a Window (Showable) to craft Items. It
 * handles all interactions with the majority of the GUI components of the
 * Block, while the Block class handles the majority of the 3D components.
 *
 * @author Alston
 * @version RTM
 */
public class CraftingTable extends Block implements Clickable, Showable, Serializable {

    //Private constant to represent the object when saving
    private static final long serialVersionUID = 3724124124124812949L;
    private static final Picture background; //static and final to improve effeciency
    private transient Node node; //Common Node for the GUI
    private SlotArea craftingSpace;
    private SlotArea craftingResult;
    private Window craftingWindow; //The GUI of this

    static {
        background = new Picture("Crafting Background");
        setupGui();
    }

    /**
     * Creates a new CraftingTable (Deos not use prototype clones).
     *
     * @param material The material for the mesh
     */
    public CraftingTable(Material material) {
        super(material, Item.WORKBENCH, Item.WORKBENCH, Block.WOOD_BASED);
        craftingWindow = new Window();
        node = craftingWindow.getNode();
        craftingResult = new SlotArea(1, 1, node) {
            @Override
            public void place(Item item, int x, int y) {
                item.setAmount(item.getAmount()); //Fixes a bug where the amount does not show
                defaultPlace(item, x, y);
                item.getPicture().setPosition(0.595f * Main.APP_SETTINGS.getWidth(), 0.7f * Main.APP_SETTINGS.getHeight()); //Assumes only index is 0,0
                item.getNumberText().setLocalTranslation(item.getPicture().getLocalTranslation().add(0, Main.APP_SETTINGS.getHeight() / 30f, 0));
                if (item.getType() == Item.AIR) { //It's an EmptyItem
                    item.getPicture().move(0, 0, -3); //Hides the Picture behind everything; it's hidden but is still in collision detection
                }
                //Attaches GUI elements
                node.attachChild(item.getPicture());
                node.attachChild(item.getNumberText());
            }
        };
        craftingSpace = new SlotArea(3, 3, node) {
            @Override
            public void place(Item item, int x, int y) {
                defaultPlace(item, x, y);
                item.getPicture().setPosition(0.345f * Main.APP_SETTINGS.getWidth() + x * 0.085f * Main.APP_SETTINGS.getHeight(),
                        0.615f * Main.APP_SETTINGS.getHeight() + y * 0.09f * Main.APP_SETTINGS.getHeight()); //Precisely places Picture
                item.getNumberText().setLocalTranslation(item.getPicture().getLocalTranslation().add(0, Main.APP_SETTINGS.getHeight() / 30f, 0));
                if (item.getType() == Item.AIR) { //It's an EmptyItem
                    item.getPicture().move(0, 0, -3); //Hides the Picture behind everything; it's hidden but is still in collision detection
                }
                //Attaches GUI elements
                node.attachChild(item.getPicture());
                node.attachChild(item.getNumberText());
                //Checks to see if there is any Item that can be made from the recipe
                if (getItems()[getItems().length - 1][getItems()[getItems().length - 1].length - 1] == null) { //Not setup yet
                    return;
                }
                Utility.craft(craftingSpace, craftingResult);
            }
        };
        setupGui();
        craftingWindow.setup(craftingSpace, craftingResult);
        setHealth(((BlockItem) Item.ITEMS[Item.WORKBENCH]).getTimeToBreak());
    }

    /**
     * Shows the Crafting Table GUI.
     */
    @Override
    public void click() {
        node.attachChild(background); //This is safe because only 1 window can be open
        craftingWindow.show();
    }

    /**
     * Hides the Crafting Table GUI.
     */
    @Override
    public void close() {
        Main.getInstance().getGuiNode().detachChild(node);
        Main.getInstance().getInputManager().setCursorVisible(false); //Hides cursor
        Main.getInstance().getFlyByCamera().setEnabled(true);
        Main.shownWindow = null;
    }

    /**
     * Sets up the display for the crafting table.
     */
    private static void setupGui() {
        //Background
        background.setWidth(5 * Main.APP_SETTINGS.getHeight() / 6);
        background.setHeight(5 * Main.APP_SETTINGS.getHeight() / 6);
        background.setQueueBucket(RenderQueue.Bucket.Gui);
        background.setImage(Main.getInstance().getAssetManager(), "Interface/workbench.png", true);
        background.setPosition((Main.APP_SETTINGS.getWidth() - 5 * Main.APP_SETTINGS.getHeight() / 6) / 2, Main.APP_SETTINGS.getHeight() / 8);
    }

    /**
     * Adds an additional level by restoring the other Objects as well.
     */
    @Override
    public void setupFromFile(Chunk parent) {
        super.setupFromFile(parent); //Sets up the Block first
        node = craftingWindow.getNode();
        setupGui();
        craftingResult.restore(node);
        craftingSpace.restore(node);
    }
}
