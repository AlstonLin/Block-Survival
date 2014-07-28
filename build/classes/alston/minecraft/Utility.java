package alston.minecraft;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that contains utility functions that can be applicable in multiple
 * situations, or is to be used by multiple classes. All the methods in this
 * class are public and static and Objects are public and final. Note that these
 * methods are synchronized, and therefore thread-safe.
 *
 * @author Alston
 * @version RTM
 */
public class Utility {

    /**
     * The physics listener for all Items
     */
    private static final PhysicsCollisionListener physicsListener = new PhysicsCollisionListener() {
        public void collision(PhysicsCollisionEvent event) {
            Item collidedItem;
            if (event.getObjectA() == null || event.getObjectB() == null) { //90% of the time it's null
                return;
            }
            if (event.getObjectA() != Main.currentGame.getPlayer().getControl() && event.getObjectB() != Main.currentGame.getPlayer().getControl()) { //Not interested if player is not involved
                return;
            }            //Trys it to get an Item with Object A first
            collidedItem = (Item) Item.itemSpatialsMap.get(event.getObjectA());
            if (collidedItem == null) { //No result; tries with B
                collidedItem = (Item) Item.itemSpatialsMap.get(event.getObjectB());
                if (collidedItem == null) { //Tried with both and no Items were hit; returns
                    return;
                }
            }
            //At this point, the player collided with an Item
            collidedItem.pickUp();
            event.clean();
        }
    };
    /**
     * A Comparator that will sort/search a Matrix-ArrayList of Comparable
     * Objects.
     */
    public static final Comparator ARRAY_LIST_COMPARATOR = new Comparator() {
        @Override
        public int compare(Object object1, Object object2) { //Compares both arrays; sorts by size first, then by content
            ArrayList<ArrayList<Comparable>> array1 = (ArrayList<ArrayList<Comparable>>) object1;
            ArrayList<ArrayList<Comparable>> array2 = (ArrayList<ArrayList<Comparable>>) object2;
            assert array1.size() > 0 && array2.size() > 0 : "Empty ArrayList"; //Ensures that any bugs that happen can be easily resolved
            //First checks if the sizes are the same
            if (array1.size() < array2.size()) {
                return -1;
            } else if (array1.size() > array2.size()) {
                return 1;
            }
            if (array1.get(0).size() < array2.get(0).size()) {
                return -1;
            } else if (array1.get(0).size() > array2.get(0).size()) {
                return 1;
            }
            //Same sizes; checks for content
            for (int i = 0; i < array1.size(); i++) { //Goes thru every single index of both arrays and compare then
                for (int j = 0; j < array1.get(0).size(); j++) {
                    //If index by index they are not the same
                    if (array1.get(i).get(j).compareTo(array2.get(i).get(j)) > 0) {
                        return 1;
                    } else if (array1.get(i).get(j).compareTo(array2.get(i).get(j)) < 0) {
                        return -1;
                    }
                }
            }
            return 0;
        }
    };

    static {
        Main.bulletAppState.getPhysicsSpace().addCollisionListener(physicsListener); //Adds the Item's special physics listener
    }

    /**
     * Rounds a given float to 3 significant digits.
     *
     * @param unrounded The unrounded value of the float
     * @return The float rounded to 3 significant digits
     */
    public static synchronized float round(float unrounded) {
        BigDecimal decimal = new BigDecimal(unrounded);
        BigDecimal rounded = decimal.setScale(3, BigDecimal.ROUND_HALF_UP);
        return rounded.floatValue();

    }//End of round

    /**
     * Checks on the crafting space if it's currently a recipe for an Item. If
     * it is, places the new Item in the craftingResult.
     *
     * @param craftingSpace The SlotArea of any size with the ingredients to be
     * crafted
     * @param craftingResult The SlotArea of size 1x1 where the result will be
     * placed
     */
    public static synchronized void craft(SlotArea craftingSpace, SlotArea craftingResult) {
        Item craftResult; //The new Item as a result of the recipe from crafting
        if (craftingSpace == null || Main.currentGame == null) { //Prevents craftingSpace/currentGame self-refrencing while being constructed
            return;
        }
        if (craftingResult.getItems()[0][0].getParent() == craftingResult) { //Makes sure that it only removes it if it's the parent 
            craftingResult.getItems()[0][0].detachFromAll(); //Removes the current Item on the craftingResult
        }
        craftResult = Main.recipeMap.get(craftingSpace.getItems());
        if (craftResult == null) { //No recipe
            return;
        }
        try {
            craftingResult.place(craftResult.clone(), 0, 0);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sets the verticies of the given Spatial if it's a Geometry to the given
     * lightLevel.
     *
     * @param spatial The Spatial to set the vertice's light levels
     * @param lightLevel The light lebvel to set it at
     */
    public static synchronized void setLightLevel(Spatial spatial, float lightLevel) {
        lightLevel = lightLevel < Light.AMBIENT ? Light.AMBIENT : lightLevel; //Safely funcion of not going below AMBIENT
        if (spatial instanceof Geometry) { //Can't set lighting levels if it's not a Geometry
            final float newColors[] = new float[spatial.getVertexCount() * 4]; //Can be acessed froom inner classes
            for (int i = 0; i < newColors.length; i += 4) { //Goes thru each vertex
                newColors[i] = lightLevel;
                newColors[i + 1] = lightLevel;
                newColors[i + 2] = lightLevel;
                newColors[i + 3] = lightLevel;
            }
            ((Geometry) spatial).getMesh().setBuffer(VertexBuffer.Type.Color, 4, newColors);
        }
    }

    /**
     * Gets the Block from the given 3D co-ordinate space location.
     *
     * @param x The x component
     * @param y The y component
     * @param z The z component
     */
    public static synchronized Block getBlock(float x, float y, float z) {
        return Main.currentGame.getChunk(Main.currentGame.getCurrentChunkX(), Main.currentGame.getCurrentChunkY())
                .getBlock((int) x - Main.currentGame.getCurrentChunkX() * Main.MAX_BLOCKS,
                (int) y, (int) z - Main.currentGame
                .getCurrentChunkY() * Main.MAX_BLOCKS); //So the next part isn't really long
    }

    /**
     * Casts a ray to the guiNode to find the collided pictures and returns an
     * iterator for it.
     *
     * @param node The node where the pictures belong to
     * @return An iterator containing all the collided Pictures
     */
    public static synchronized Iterator<CollisionResult> getCollidedPictures(Node node) {
        CollisionResults collisionResults = new CollisionResults();
        Ray ray = new Ray(new Vector3f(Main.getInstance().getInputManager().getCursorPosition().x, Main.getInstance()
                .getInputManager().getCursorPosition().y, 1f),
                new Vector3f(0f, 0f, -1f)); //Ray starting from cursor position going into the screen
        node.collideWith(ray, collisionResults);
        return collisionResults.iterator();
    }

    /**
     * Gives the player a specific item (used for cheating/testing).
     *
     * @param player The player to give the Item to
     * @param type The item type
     * @param amount The amount of items
     */
    public static synchronized void give(Player player, int type, int amount) {
        try {
            Item item = Item.ITEMS[type].clone();
            item.setAmount(amount);
            player.getInventory().getInventorySpace().add(item);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
