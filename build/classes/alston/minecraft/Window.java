package alston.minecraft;

import com.jme3.collision.CollisionResult;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains all the basic functions of any pop-up window, including interactions
 * with the mouse such as Item manipulation.
 *
 * @author Alston
 * @version RTM
 */
public class Window implements Serializable {

    private static final long serialVersionUID = 37241241248252358L; //Private constant used for Object serialization
    private static final Node inventoryItemsNode; //There can only be one inventory
    private transient Node node; //The node for the Pictures in the Window
    private transient Item clickedItem; //The item that the user clicked; transient intentionally to prevent bugs
    private transient boolean itemsConsumed; //Tracks if the craftingIngredients were consumed yet
    private SlotArea craftingSpace;
    private SlotArea craftingResult;

    static { //Static initation Block
        inventoryItemsNode = new Node("Inventory Items Node");
        inventoryItemsNode.move(0, 0, 1);
    }

    /**
     * Creates a new Window; this should never be called directly, but through a
     * super() call from a class that extends this; Note: setup() MUST be called
     * before using the Window).
     */
    public Window() {
        node = new Node("Window Node");
        node.attachChild(inventoryItemsNode);
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
        //Restores Nodes
        node = new Node("Inventory Node");
        input.defaultReadObject(); //Calls default reader first 
        node.attachChild(inventoryItemsNode);
    }

    public void setup(SlotArea craftingSpace, SlotArea craftingResult) {
        this.craftingSpace = craftingSpace;
        this.craftingResult = craftingResult;
    }

    /**
     * Shows the inventory on the GUI.
     */
    public void show() {
        node.attachChild(Window.getInventoryItemsNode()); //Adds the Inventory to this Window
        Main.getInstance().getInputManager().setCursorVisible(true); //Hides cursor
        Main.getInstance().getGuiNode().attachChild(node);
        Main.getInstance().getFlyByCamera().setEnabled(false);
        Main.shownWindow = this; //Sets the shown window as this
    }

    /**
     * User left clicks when inventory is open.
     */
    public void leftClick() {
        clickedItem = getMousedItem();
        if (clickedItem == null || clickedItem.getType() == Item.AIR) { //No Item/EmptyItem
            clickedItem = null;
            return;
        }
        clickedItem.getPicture().move(0, 0, 10); //Places the Picture infront of all others
        Main.currentGame.getInventoryBar().detach(clickedItem); //Detaches from bar
        //Creates a new EmptyItem to take it's spot
        clickedItem.getParent().getItems()[clickedItem.getSlotX()][clickedItem.getSlotY()] = new EmptyItem();
        clickedItem.getParent().place(clickedItem.getParent().getItems()[clickedItem.getSlotX()][clickedItem.getSlotY()], clickedItem.getSlotX(), clickedItem.getSlotY());
        if (clickedItem.getParent() == craftingResult) {
            itemsConsumed = false;
        }
    }

    /**
     * Closes the inventory on the GUI.
     */
    public void close() {
        //Drops all the Items in the crafting space 
        for (int i = 0; i < craftingSpace.getItems().length; i++) {
            for (int j = 0; j < craftingSpace.getItems()[0].length; j++) {
                if (craftingSpace.getItems()[i][j].getType() != Item.AIR) { //Not Empty
                    while (craftingSpace.getItems()[i][j].getAmount() > 0
                            && craftingSpace.getItems()[i][j].getType() != Item.AIR) { //Drop until nothing is left
                        craftingSpace.getItems()[i][j].drop();
                    }
                    craftingSpace.remove(i, j);
                }
            }
        }
        //Removes from crafting result
        if (craftingResult.getItems()[0][0].getType() != 0) { //Not Empty
            while (craftingSpace.getItems()[0][0].getAmount() > 0
                    && craftingSpace.getItems()[0][0].getType() != Item.AIR) { //Drop until nothing is left
                craftingResult.getItems()[0][0].drop();
            }
            craftingResult.remove(0, 0);
        }
        Main.getInstance().getInputManager().setCursorVisible(false); //Shows cursor
        release(); //Releases the click to prevent bugs
        Main.getInstance().getGuiNode().detachChild(node);
        Main.getInstance().getFlyByCamera().setEnabled(true);
        clickedItem = null;
        Main.shownWindow = null; //Not the shown window anymore
    }

    /**
     * The user right clicks. Does nothing unless the user is currently
     * left-clicking an Item, in which case it would split the clicked Item into
     * 2 stacks, with one only containing 1 if they right clicked on an empty
     * slot, or adding to it if it's the same type. If the user attempts to
     * right click an occupied slot that the Item occupying it is not of same
     * type, does nothing.
     */
    public void rightClick() {
        if (clickedItem != null) { //User has clicked an Item; splits into 2 stack with one containing only 1
            Item collidedItem = getMousedItem();
            Item newItem = null;
            if (collidedItem == null) { //Collided with nothing
                return;
            }
            if (collidedItem.getParent() == craftingResult) { //Cannot place Items onto craftingResult
                return;
            }
            if (collidedItem.getType() == clickedItem.getType()) { //Same type; adds one from one stack to the other
                if (collidedItem.getAmount() >= collidedItem.getMaxAmount()) { //Stack is full
                    return;
                }
                collidedItem.setAmount(collidedItem.getAmount() + 1);
                clickedItem.setAmount(clickedItem.getAmount() - 1);
                if (clickedItem.getAmount() <= 0) { //Removes the Item no longer has any (Does Not Exist)
                    if (clickedItem.getParent() == craftingResult && !itemsConsumed) { //If it's swapping out of craftingResult
                        consumeIngredients();
                        itemsConsumed = true;
                        Utility.craft(craftingSpace, craftingResult); //Checks if there is enough to craft the same/another Item
                    }
                    clickedItem.detachFromAll();
                    clickedItem = null; //There is no longer any Item being clicked
                }
                return;
            }
            if (collidedItem.getType() != 0) { //Slot is not empty
                return;
            }

            if (clickedItem.getParent() == craftingResult && !itemsConsumed) { //If it's swapping out of craftingResult
                consumeIngredients();
                itemsConsumed = true;
                Utility.craft(craftingSpace, craftingResult); //Checks if there is enough to craft the same/another Item
            }

            //Splits 1 Item away from the stack
            try {
                newItem = clickedItem.clone(); //New stack of Items
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
            }
            newItem.setAmount(1); //Sets the amount to 1
            collidedItem.detachFromAll(); //Detaches EmptyItem from the Nodes; this Item will be discarded, along with fields, and sent to GC
            collidedItem.getParent().place(newItem, collidedItem.getSlotX(), collidedItem.getSlotY()); //Places the new stack
            clickedItem.setAmount(clickedItem.getAmount() - 1); //Substracts 1 Item from the clicked stack

            if (clickedItem.getAmount() <= 0) { //Removes the Item no longer has any (Does Not Exist)
                clickedItem.detachFromAll();
                if (clickedItem.getParent() == craftingResult) { //If it's swapping out of craftingResult
                    Utility.craft(craftingSpace, craftingResult); //Checks if there is enough to craft the same/another Item
                }
                clickedItem = null; //There is no longer any Item being clicked
            }
        }
    }

    /**
     * User released on the inventory menu.
     */
    public void release() {
        Item collidedItem = getMousedItem();
        if (clickedItem == null) { //Returns if there was never anything selected
            return;
        }
        clickedItem.getPicture().move(0, 0, -10); //Resets it
        if (collidedItem == null || collidedItem.getParent() == craftingResult) { //Collided with nothing/cannot place in craftingResult
            resetClickedItem();
            return;
        }
        if (clickedItem.getParent() == craftingResult) { //Special case when it's in the crafting result
            if (collidedItem.getType() != clickedItem.getType() && collidedItem.getType() != 0) { //Cant swap out the result; resets
                resetClickedItem();
                return;
            }
            if (collidedItem.getAmount() + clickedItem.getAmount() > collidedItem.getMaxAmount()) { //Returns if stack is full
                resetClickedItem();
                return;
            }
            if (!itemsConsumed) {
                consumeIngredients();
            }
        }
        if (clickedItem.getType() == collidedItem.getType()) { //Same type
            if (collidedItem.getAmount() + clickedItem.getAmount() <= clickedItem.getMaxAmount()) { //Enough to fit into a single stack
                collidedItem.setAmount(collidedItem.getAmount() + clickedItem.getAmount());
                clickedItem.detachFromAll(); //The clickedItem no longer exists
            } else { //Still 2 stacks; the collidedItem gets the maximum and the remainder remains in clickedItem
                clickedItem.setAmount(collidedItem.getAmount() + clickedItem.getAmount() - collidedItem.getMaxAmount());
                collidedItem.setAmount(collidedItem.getMaxAmount());
                clickedItem.getParent().place(clickedItem, clickedItem.getSlotX(), clickedItem.getSlotY()); //Resets to original slot  
            }
            if (clickedItem.getParent() == craftingResult) { //If it's swapping out of craftingResult
                Utility.craft(craftingSpace, craftingResult); //Checks if there is enough to craft the same/another Item
            }
            clickedItem = null;
            return;
        }
        //It is an empty slot/Item; Stores temporary values for swapping
        int tempX = clickedItem.getSlotX(), tempY = clickedItem.getSlotY();
        SlotArea tempParent = clickedItem.getParent();
        //Removes both from bar
        Main.currentGame.getInventoryBar().detach(clickedItem);
        Main.currentGame.getInventoryBar().detach(collidedItem);
        //Swaps the positions
        collidedItem.getParent().place(clickedItem, collidedItem.getSlotX(), collidedItem.getSlotY());
        if (collidedItem.getType() != Item.AIR) { //Prevents crafting bugs
            tempParent.place(collidedItem, tempX, tempY);
        } else { //Removes from everything if it;s an EmptyItem
            collidedItem.detachFromAll();
        }
        clickedItem = null; //Resets the clicked Item
        if (tempParent == craftingResult) { //If it's swapping out of craftingResult
            Utility.craft(craftingSpace, craftingResult); //Checks if there is enough to craft the same/another Item
        }
    }

    /**
     * Resets the clickedItem to it's original position.
     */
    private void resetClickedItem() {
        clickedItem.getParent().getItems()[clickedItem.getSlotX()][clickedItem.getSlotY()].detachFromAll(); //Detaches the EmptyItem occupying the area
        clickedItem.getParent().place(clickedItem, clickedItem.getSlotX(), clickedItem.getSlotY()); //Resets to original slot 
        clickedItem = null;
    }

    /**
     * Called when crafting; consumes 1 amount from all non-Empty Items on the
     * craftingSpace.
     */
    private void consumeIngredients() {
        //Removes 1 amount from all non-EmptyItems in the craftingSpace
        for (int i = 0; i < craftingSpace.getItems().length; i++) {
            for (int j = 0; j < craftingSpace.getItems()[0].length; j++) {
                if (craftingSpace.getItems()[i][j].getType() != 0) { //Non-Empty
                    craftingSpace.getItems()[i][j].setAmount(craftingSpace.getItems()[i][j].getAmount() - 1);
                }
            }
        }
    }

    /**
     * Gets the Item that the cursor is currently on.
     *
     * @return The Item that the cursor is hovering over, or null if it's not
     * over anything nothing.
     */
    private Item getMousedItem() {
        Item mousedItem;
        Iterator collidedPictures;
        collidedPictures = Utility.getCollidedPictures(node);
        try {
            while (collidedPictures.hasNext()) {
                mousedItem = (Item) Main.picturesMap.get((Picture) ((CollisionResult) collidedPictures.next()).getGeometry());
                if (mousedItem != null) { //Returns here only if it gets a non-null result
                    return mousedItem;
                }
            }
            return null; //No matches
        } catch (NullPointerException ex) { //Clicked on nothing
            return null;
        } catch (ClassCastException e) { //Clicked on Picture
            return null;
        }
    }

    /**
     *
     * @return The Node of the Pictures in this Window
     */
    public Node getNode() {
        return node;
    }

    /**
     *
     * @param clickedItem New clicked Item
     */
    public void setClickedItem(Item clickedItem) {
        this.clickedItem = clickedItem;
    }

    /**
     *
     * @return The current Item that is clicked
     */
    public Item getClickedItem() {
        return clickedItem;
    }

    /**
     *
     * @return The Node containing only the Items of the Inventory
     */
    public static Node getInventoryItemsNode() {
        return inventoryItemsNode;
    }
}
