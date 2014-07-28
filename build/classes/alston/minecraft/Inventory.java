package alston.minecraft;

import com.jme3.renderer.queue.RenderQueue;
import com.jme3.ui.Picture;

/**
 * Contains all the Items the player currently has in their inventory, and is
 * the handles all interactions between the UI and the inventory, including (but
 * not limited to) handing the visual functions, changing the Items in it, ect.
 *
 * @author Alston
 * @version RTM
 */
public class Inventory extends Window {

    //Private constant used for Object serialization
    private static final long serialVersionUID = 2624124124802948L;
    //Fields
    private SlotArea inventorySpace; //The space of inventory slots in the table
    private SlotArea craftingSpace; //The space for crafting in the table
    private SlotArea craftingResult; //The result of the crafting
    private int currentItem; //Which item the player has selected with the selector

    /**
     * Constructs a new Inventory by initializaing the array of Items (however,
     * NOT each individual Item; setup() should be called to do this for
     * efficiency and readibility purposes), and all the Nodes associated with
     * the Inventory.
     *
     * @param bar The InventoryBar for Items to be attached to
     *
     */
    public Inventory(InventoryBar bar) {
        super();
        final InventoryBar inventoryBar = bar; //Allows to be called from inner class
        setupBackground();
        inventorySpace = new SlotArea(9, 4, Window.getInventoryItemsNode()) {
            @Override
            public void place(Item item, int x, int y) {
                if (item.getSlotY() == 0 && y != 0) { //Removed from the bar
                    inventoryBar.detach(item);
                }
                defaultPlace(item, x, y);
                //Sets Picture's and numberText's position
                item.getPicture().setPosition(0.285f * Main.APP_SETTINGS.getWidth()
                        + x * (Main.APP_SETTINGS.getHeight() / 11.75f), 0.195f * Main.APP_SETTINGS.getHeight()
                        + y * Main.APP_SETTINGS.getHeight() / 11.40f); //Precisely places Picture
                if (item.getSlotY() == 0) { //Bottom row
                    item.getPicture().move(0, -Main.APP_SETTINGS.getHeight() / 36, 0); //Compensates for the spacing between 1st and 2nd row
                }
                if (item.getType() == 0) { //It's an EmptyItem
                    item.getPicture().move(0, 0, -3); //Hides the Picture behind everything; it's hidden but is still in collision detection
                }
                item.getNumberText().setLocalTranslation(item.getPicture().getLocalTranslation().add(0, Main.APP_SETTINGS.getHeight() / 30f, 0));
                getNode().attachChild(item.getPicture());
                getNode().attachChild(item.getNumberText());
                if (y == 0) { //Shows/Updates on bar if applicable
                    inventoryBar.attach(item);
                }
            }
        };
        craftingSpace = new SlotArea(2, 2, getNode()) {
            @Override
            public void place(Item item, int x, int y) {
                defaultPlace(item, x, y);
                item.getPicture().setPosition(0.5f * Main.APP_SETTINGS.getWidth() + x * 0.08f * Main.APP_SETTINGS.getHeight(),
                        0.66f * Main.APP_SETTINGS.getHeight() + y * 0.09f * Main.APP_SETTINGS.getHeight()); //Precisely places Picture
                item.getNumberText().setLocalTranslation(item.getPicture().getLocalTranslation().add(0, Main.APP_SETTINGS.getHeight() / 30f, 0));
                if (item.getType() == 0) { //It's an EmptyItem
                    item.getPicture().move(0, 0, -3); //Hides the Picture behind everything; it's hidden but is still in collision detection
                }
                //Attaches GUI elements
                getNode().attachChild(item.getPicture());
                getNode().attachChild(item.getNumberText());
                //Checks to see if there is any Item that can be made from the recipe
                if (getItems()[getItems().length - 1][getItems()[getItems().length - 1].length - 1] == null) { //Not setup yet
                    return;
                }
                Utility.craft(craftingSpace, craftingResult);
            }
        };
        craftingResult = new SlotArea(1, 1, getNode()) {
            @Override
            public void place(Item item, int x, int y) {
                //Sets the positions
                item.setAmount(item.getAmount()); //Fixes a bug where the amount does not show
                defaultPlace(item, x, y);
                item.getPicture().setPosition(0.65f * Main.APP_SETTINGS.getWidth(), 0.7f * Main.APP_SETTINGS.getHeight()); //Assumes only index is 0,0
                item.getNumberText().setLocalTranslation(item.getPicture().getLocalTranslation().add(0, Main.APP_SETTINGS.getHeight() / 30f, 0));
                if (item.getType() == 0) { //It's an EmptyItem
                    item.getPicture().move(0, 0, -3); //Hides the Picture behind everything; it's hidden but is still in collision detection
                }
                //Attaches GUI elements
                getNode().attachChild(item.getPicture());
                getNode().attachChild(item.getNumberText());
            }
        };
        setup(craftingSpace, craftingResult);
    }

    /**
     * Restores the inventory bar and menu to the inventoryNode.
     */
    public void restore() { 
        setupBackground();
        inventorySpace.restore(Window.getInventoryItemsNode());
        craftingResult.restore(getNode());
        craftingSpace.restore(getNode());
        for (int i = 0; i < 9; i++) { //Bar
            Main.currentGame.getInventoryBar().attach(inventorySpace.getItems()[i][0]);
        }
        //Fixes a Bug with the selector
        Main.currentGame.getInventoryBar().getSelector().setPosition((currentItem + 1) * Main.APP_SETTINGS.getWidth() / 11, 0);
    }

    /**
     * Sets up the background's Picture
     */
    private void setupBackground() {
        Picture background = new Picture("Inventory background");
        background.setWidth(5 * Main.APP_SETTINGS.getHeight() / 6);
        background.setHeight(5 * Main.APP_SETTINGS.getHeight() / 6);
        background.setQueueBucket(RenderQueue.Bucket.Gui);
        background.setImage(Main.getInstance().getAssetManager(), "Interface/inventory.png", true);
        background.setPosition((Main.APP_SETTINGS.getWidth() - 5 * Main.APP_SETTINGS.getHeight() / 6) / 2, Main.APP_SETTINGS.getHeight() / 8); //Middle of the screen
        background.move(0, 0, -1); //Behind all other images
        getNode().attachChild(background);
    }

    /**
     *
     * @return The index of the inventory of what the player has currently
     * selected
     */
    public int getCurrentItem() {
        return currentItem;
    }
    

    /**
     *
     * @return The SlotArea of only the inventory space
     */
    public SlotArea getInventorySpace() {
        return inventorySpace;
    }

    /**
     *
     * @return The SlotArea of the crating area in the inventory
     */
    public SlotArea getCraftingSpace() {
        return craftingSpace;
    }

    /**
     *
     * @param currentItem The new currently selected index of the inventory the
     * player has selected
     */
    public void setCurrentItem(int currentItem) {
        this.currentItem = currentItem;
    }
}
