package alston.minecraft;

import com.jme3.scene.Node;
import java.io.Serializable;

/**
 * Abstract representation of a sqauare area in the GUI that contains slots for
 * Items and handles all interactions with them.
 *
 * @author Alston
 * @version RTM
 */
public abstract class SlotArea implements Serializable, Cloneable {

    //Private constant used for Object serialization
    private static final long serialVersionUID = 26241241248023420L;
    //Instance Variables
    private Item[][] items; //Items/Slots in the area
    private transient Node node; //The Node that will contain all the Pictures
    //private Node numbersNode; //The Node that will contain all the Numbers

    /**
     * Creates a new representation of a square are of GUI which contains slots
     * for Items.
     *
     * @param columns Number of columns in the area of slots
     * @param rows Number of rows in the area of slots
     * @param picturesNode The Node that will contain all the Pictures
     * @param numbersNode The Node that will contain all the Numbers
     */
    public SlotArea(int columns, int rows, Node node) {
        items = new Item[columns][rows];
        this.node = node;
        setup();
    }

    /**
     * Places the given Item in the given slot coordinates, whether or not it is
     * currently occupied. This method should ensure that both fields and
     * graphics are correctly changed.
     *
     * @param item The Item to be placed
     * @param x The x coordinates of the slot
     * @param y The y coordinates of the slot
     */
    public abstract void place(Item item, int x, int y);

    /**
     * Common operations called for place(); used instead of a super-call in the
     * case that a SlotArea would no need this.
     */
    public void defaultPlace(Item item, int x, int y) {
        item.setSlotPosition(x, y);
        getItems()[x][y] = item;
        item.setParent(this);
    }

    /**
     * Instantiates Items in every index of items and adds the Item's Picture to
     * the picturesNode.
     */
    public final void setup() {
        for (int j = 0; j < items[0].length; j++) {
            for (int i = 0; i < items.length; i++) {
                place(new EmptyItem(), i, j);
            }
        }
    }//End of setupInventory

    /**
     * Restores the SlotArea's Items and transient fields.
     *
     * @param picturesNode The Node that the Pictures will be contained in
     */
    public void restore(Node node) {
        this.node = node;
        for (int j = 0; j < items[0].length; j++) {
            for (int i = 0; i < items.length; i++) {
                place(items[i][j], i, j);
            }
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        SlotArea cloned = ((SlotArea) super.clone());
        cloned.items = items.clone();
        cloned.node = new Node();
        return cloned;
    }

    /**
     * Adds a given item to the slotArea. First searches for Items of same type
     * to stack with. If none is found, searches for an empty inventory slot. If
     * inventory is full, returns false
     *
     * @param item The Item to be added
     * @return True if Item was added, false if it could not
     */
    public boolean add(Item item) {
        for (int j = 0; j < 4; j++) { //Searches for other items of the same type
            for (int i = 0; i < 9; i++) {
                if (items[i][j].getType() == item.getType() && items[i][j].getAmount() < items[i][j].getMaxAmount()) { //Same type and not full; Adds to amount then discards Item
                    items[i][j].setAmount(items[i][j].getAmount() + 1);
                    return true;
                }
            }
        }
        for (int j = 0; j < 4; j++) { //Searches for an empty slot
            for (int i = 0; i < 9; i++) {
                if (items[i][j].getType() == 0) { //If there is no real item and item can be placed
                    items[i][j].detachFromAll();
                    place(item, i, j);
                    items[i][j] = item; //The slot in the inventory is the item; Old EmptyItem has gone to Garbage Collection
                    return true; //Item was added!
                }
            }
        }
        return false; //Inventory is full
    }//End of addItem

    /**
     * Removes the Item at the specified coordinates from the inventory, removes
     * all the refrences to it so it can be eligible for Garbage Collection,
     * then replaces it with a new EmptyItem.
     *
     * @param x The x coordinate of the Item
     * @param y The y coordinate of the Item
     */
    public void remove(int x, int y) {
        items[x][y].detachFromAll();
        items[x][y] = new EmptyItem();
        place(items[x][y], x, y);
        if (y == 0 && this == Main.currentGame.getPlayer().getInventory().getInventorySpace()) { //bottom row and inventory
            Main.currentGame.getInventoryBar().attach(items[x][y]);
        }
    }

    /**
     *
     * @return The Item contents of the slot area
     */
    public Item[][] getItems() {
        return items;
    }

    public Node getNode() {
        return node;
    }
}
