package alston.minecraft;

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used to represent the smallest unit of 3D mesurement in the Game, the
 * equivilent of a voxel (volume-pixel), each mesuring exactly 1m*1m*1m.
 * Includes the visual representation of every Block by containing all the
 * appropriate JME Objects, and handles all interactions between them, including
 * optimization.
 *
 * @author Alston
 * @version RTM
 */
public class Block implements Serializable {

    //Public static constant to define types of bases
    public static final int AIR_BASED = 0;
    public static final int ROCK_BASED = 1;
    public static final int DIRT_BASED = 2;
    public static final int WOOD_BASED = 3;
    //Private constant to represent the object when saving
    private static final long serialVersionUID = 3724124124812949L;
    //Fields (transient = will not be written in file)
    private transient Spatial spatial;
    private transient Material material;
    private Chunk parent; //The chunk the block belongs in
    private int type; //Default type is air
    private int dropType; //What this will drop when broken
    private int base; //Default base is air
    private int x, y, z; //Coordinates of the block in the chunks
    private float lightLevel; //The amount of light (0.3f is ambient)
    private float actualLightLevel; //The actual light lebvel of it's spatial
    private float health; //How much it's damaged (only gets damages when the user tries to break it)
    private boolean hidden;
    private boolean transparent;

    { //Instance variable initiation block; all constructors will have this
        spatial = Main.blockGeometry.deepClone();
        ((Geometry) spatial).getMesh().updateCounts(); //Fixes bug where verticies counta re inaccurate
        hidden = true; //Default boolean values for air block
    }

    /**
     * Constuctor of Block; creates an Air Block (this constructor should be
     * used only to create chunks). Instaniates all the necessary
     * Objects/varibles as well as assign it from the given parameters.
     *
     * @param x X coordinate of the Block
     * @param y Y coordinate of the Block
     * @param z Z coordinate of the Block
     * @param parent The Chunk that the Block belongs to
     */
    public Block(int x, int y, int z, Chunk parent) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.parent = parent;
        transparent = true;
        spatial.setLocalTranslation(x + parent.getX() * Main.MAX_BLOCKS, y, z + parent.getY() * Main.MAX_BLOCKS);
        setLightLevel(Light.AMBIENT); //Sets it to ambient by default
        updateVertices();
    } //End of constructor

    /**
     * Creates a prototype of a plain Block (non-subtype), with the drop type
     * being the same as the Block type.
     *
     * @param material The material for the Spatial
     * @param type Type of Block
     * @param base The base of the Block
     */
    public Block(Material material, int type, int base) {
        this.material = material;
        this.type = type;
        this.base = base;
        dropType = type;
        transparent = false;
    }

    /**
     * Creates a prototype of a plain Block (non-subtype), with a specified drop
     * type.
     *
     * @param material The material for the Spatial
     * @param type Type of Block
     * @param dropType What the Block will drop when broken; or ITEM.NAN for
     * nothing
     * @param base The base of the Block
     */
    public Block(Material material, int type, int dropType, int base) {
        this.material = material;
        this.type = type;
        this.dropType = dropType;
        this.base = base;
    }

    /**
     * Changes the Block's type to something else. For efficency purposes, It
     * simply changes the relevant values, and only replaces with an entirely
     * new Block when determines necessary by the Factory.
     *
     * @param type The new type of the block
     * @param updatePhysics If the physics should be updated afterwards (Should
     * be false only when generating terrain to optimize time)
     */
    public void changeToBlock(int type, boolean updatePhysics) {
        Block newBlock = BlockFactory.getInstance().makeBlock(type);
        if (!Block.class.getName().equals(this.getClass().getName())) { //Was originally not a simple block
            //It's a Torch turning into air; must replace with a normal air block
            newBlock = new Block(Main.blockPrototypes[Item.AIR].getMaterial(), Item.AIR, Item.NAN, Block.AIR_BASED);
            try {
                (Item.ITEMS[dropType].clone()).drop(this);
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(Block.class.getName()).log(Level.SEVERE, null, ex);
            }
            replaceWith(newBlock);
            newBlock.hide();
            newBlock.deoptimize();
            Light.updateLights();
            return;
        }
        if (newBlock != null) { //It is not a plain Block; cannot avoid freeing memory and mallocing
            replaceWith(newBlock);
            newBlock.optimize(); //Checks if the new Block can be optimized
            Light.updateLights();
            return;
        }

        if (type == Item.AIR) { //If it's a solid block transforming to an air block (Block being removed)
            hide();
            deoptimize();
            //Drops a clone of the prototype Item
            try {
                (Item.ITEMS[dropType].clone()).drop(this);
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(Block.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IndexOutOfBoundsException ex) { //Nothing explicitly dropped
            } catch (NullPointerException e){
            }
            this.type = type;
            transparent = true;
        } else { //If it's an air block transforming to a solid block (Block being placed)
            copyFields(type);
            if (updatePhysics) { //Leaves the block hidden for efficiency if applicable
                show();
                optimize();
            }
        }
        if (updatePhysics) {
            parent.updateCollisionShape(); //Updates the chunk's physics body
            Light.updateLights();
        }
    } //End of setMaterial

    /**
     * Copies the fields of given type to this' fields.
     *
     * @param type
     */
    private void copyFields(int type) {
        updateHealth(type);
        dropType = Main.blockPrototypes[type].dropType;
        this.transparent = Main.blockPrototypes[type].transparent;
        this.type = type;
        base = Main.blockPrototypes[type].base;
    }

    /**
     * Checks if a Block must be shown as a result of a Block becomming air.
     */
    private void deoptimize() {
        if (transparent) { //Does not do anything if it's transparent
            return;
        }
        //Checks if any adjacent blocks were hidden, and shows them if they are
        checkBlockForShowing(0, 0, -1);
        checkBlockForShowing(0, 0, 1);
        checkBlockForShowing(0, -1, 0);
        checkBlockForShowing(0, 1, 0);
        checkBlockForShowing(-1, 0, 0);
        checkBlockForShowing(1, 0, 0);
    }

    /**
     * Checks if the Block can be hidden and therefore optimize the memory and
     * lower the polygon count to be rendered.
     */
    private void optimize() {
        if (transparent) { //Does not hide if it's transparent
            return;
        }
        //Checks the block on each side of the current block to see if it can be hidden and optimized 
        checkBlockForHiding(0, 0, -1);
        checkBlockForHiding(0, 0, 1);
        checkBlockForHiding(0, -1, 0);
        checkBlockForHiding(0, 1, 0);
        checkBlockForHiding(-1, 0, 0);
        checkBlockForHiding(1, 0, 0);
    }

    /**
     * Checks if the Block specified by the change in coordinates (ex. yModifier
     * = 1 is above the Block) can be hidden for optimization as a result of a
     * Block being placed adjacent to it, and hides it if it does.
     *
     * @param xModifier The change in coordinates in the X axis
     * @param yModifier The change in coordinates in the Y axis
     * @param zModifier The change in coordinates in the Z axis
     */
    private void checkBlockForHiding(int xModifier, int yModifier, int zModifier) {
        Block block = parent.getBlock(x + xModifier, y + yModifier, z + zModifier);
        if (block == null) { //If it called a chunks that is not loaded
            return;
        }
        if (isCovered(block) && !block.hidden) {
            block.hide();
        }
    } //End of checkBlockForHiding

    /**
     * Checks if the Block specified by the change in coordinates (ex. yModifier
     * = 1 is above the Block) needs to be shown as a result of a Block being
     * removed, and shows it if it does.
     *
     * @param xModifier
     * @param yModifier
     * @param zModifier
     */
    private void checkBlockForShowing(int xModifier, int yModifier, int zModifier) {
        Block block = parent.getBlock(x + xModifier, y + yModifier, z + zModifier);
        if (block == null) { //If it called a chunks that is not loaded
            return;
        }
        if (block.hidden && block.type != 0) {
            block.show();
            if (block.parent != parent) { //Updates physics for the other chunk also
                block.parent.updateCollisionShape();
            }
        }
    } //End of checkBlockForShowing

    /**
     * Checks this block for surrounding Air Blocks. If one is found, the Block
     * will show.
     */
    public void checkBlockForShowing() {
        if (type != 0) { //Ensures that this is not an Air Block
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    for (int k = -1; k <= 1; k++) {
                        try {
                            if (parent.getBlock(x + i, y + j, z + k).getType() == 0) { //There is an adjacent air Block
                                show();
                            }
                        } catch (NullPointerException e) { //Block does not exist
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if all sides are covered (No adjacent air blocks).
     *
     * @param block The block that is being checked
     * @return A boolean representation of whether or not the block is hidden
     */
    private boolean isCovered(Block block) {
        try { //All surrounded in try-catch in case it calls a unloaded/non-existant chunks, it'll just ignore and continue
            if (block.parent.getBlock(block.x, block.y, block.z - 1).transparent) { //Z-1 
                return false;
            }
        } catch (NullPointerException e) {
        }
        try {
            if (block.parent.getBlock(block.x, block.y, block.z + 1).transparent) { //Z+1
                return false;
            }
        } catch (NullPointerException e) {
        }
        try {
            if (block.parent.getBlock(block.x, block.y - 1, block.z).transparent) { //Y-1 (Down)
                return false;
            }
        } catch (NullPointerException e) {
        }
        try {
            if (block.parent.getBlock(block.x, block.y + 1, block.z).transparent) { //Y+1 (Up)
                return false;
            }
        } catch (NullPointerException e) {
        }
        try {
            if (block.parent.getBlock(block.x - 1, block.y, block.z).transparent) { //X-1
                return false;
            }
        } catch (NullPointerException e) {
        }
        try {
            if (block.parent.getBlock(block.x + 1, block.y, block.z).transparent) { //X+1
                return false;
            }
        } catch (NullPointerException e) {
        }
        return true; //If it reaches here, it means there is no adjacent airblocks and it must be covered
    } //End of isCovered

    /**
     * Adds the block to the Chunk's node and marks it as not hidden.
     */
    protected void show() {
        hidden = false;
        //Restores the Block's appearence
        material = Main.blockPrototypes[type].material;
        spatial.setMaterial(material);
        parent.getNode().attachChild(spatial); //Adds to chunk's node
    } //End of showBlock

    /**
     * Removes the block from the Chunk's node and marks it as hidden.
     */
    private void hide() {
        hidden = true;
        parent.getNode().detachChild(spatial); //Removes from chunk
        material = null;
    } //End of hideBlock

    /**
     * Replaces this Block with a new Block.
     *
     * @param newBlock The Block that will replace this Block.
     */
    private void replaceWith(Block newBlock) {
        parent.getNode().detachChild(spatial);
        parent.getBlocks()[x][y][z] = newBlock;
        newBlock.setLocation(x, y, z, parent);
        newBlock.show();
        parent.updateCollisionShape(); //Updates the physics
    }

    /**
     * Restores the Block's memory intensive Objects (Spatial and Material);
     * called to de-compress when LOADING A CHUNK.
     */
    public void restore() {
        //Restores the Block's spatial (ALL Blocks must have one, even if not used due to bugs in JME library)
        spatial = Main.blockPrototypes[getType()].getSpatial().deepClone();
        ((Geometry) spatial).getMesh().updateCounts();
        spatial.setLocalTranslation(x + parent.getX() * Main.MAX_BLOCKS, y, z + parent.getY() * Main.MAX_BLOCKS);
        setLightLevel(lightLevel); //Restores the light's level
        Utility.setLightLevel(spatial, lightLevel);
        if (!hidden) { //Only restores if it's not hidden 
            material = Main.blockPrototypes[type].material;
            spatial.setMaterial(material);
            show();
        }
    }

    /**
     * Sets up the Block's spatial and material from loading from a file
     *
     * @param parent A refrence to the Chunk that this belongs to (Bug
     * workaround)
     */
    public void setupFromFile(Chunk parent) {
        this.parent = parent;
        spatial = Main.blockPrototypes[type].getSpatial().deepClone();
        ((Geometry) spatial).getMesh().updateCounts();
        spatial.setLocalTranslation(x + parent.getX() * Main.MAX_BLOCKS, y, z + parent.getY() * Main.MAX_BLOCKS);
        if (!hidden) {
            material = Main.blockPrototypes[type].material;
            spatial.setMaterial(material);
            parent.getNode().attachChild(spatial);
        }
        setLightLevel(lightLevel);
        Utility.setLightLevel(spatial, lightLevel);
        updateVertices();
    }

    /**
     * Updates the Block's health as a result from change of type.
     *
     * @param type The Block type this is changing to
     */
    private void updateHealth(int type) {
        if (Item.ITEMS[type] instanceof BlockItem) { //Ensures the new block is a solid and breakable block
            health = ((BlockItem) Item.ITEMS[type]).getTimeToBreak();
        } else if (Item.ITEMS[type] instanceof TorchItem) { //Special case for torches
            health = 0.3f;
        } else { //Anything else
            health = 999999999; //Virtually unbreakable
        }
    }

    /**
     * Sets the memory intensive variables to a null refrence so the Objects are
     * eligible for Garbage Collection and free up RAM.
     */
    public void compress() {
        spatial = null;
        material = null;
    }

    /**
     *
     * @return The health of the block
     */
    public float getHealth() {
        return health;
    }

    /**
     *
     * @return The Block's Spatial
     */
    public Spatial getSpatial() {
        return spatial;
    }

    /**
     *
     * @return The Material/appearence of this Block's spatial
     */
    public Material getMaterial() {
        return material;
    }

    /**
     *
     * @return An Integer representation of the Block.
     */
    public int getType() {
        return type;
    }

    /**
     *
     * @return The base of the Item
     */
    public int getBase() {
        return base;
    }

    /**
     *
     * @return The x component of the location vector
     */
    public int getX() {
        return x;
    }

    /**
     *
     * @return The light intensity of the Block's vertices
     */
    public float getLightLevel() {
        return lightLevel;
    }

    /**
     *
     * @return The y component of the location vector
     */
    public int getY() {
        return y;
    }

    /**
     *
     * @return The z component of the location vector
     */
    public int getZ() {
        return z;
    }

    /**
     *
     * @return The Chunk that this Block belongs to
     */
    public Chunk getParent() {
        return parent;
    }

    /**
     *
     * @return The actual light level of the spatial itself
     */
    public float getActualLightLevel() {
        return actualLightLevel;
    }

    /**
     *
     * @return If the Block is currently not rendered
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Note: In order for the effects to be applied, call updateVertices()
     *
     * @param lightLevel A positive floating point value, with the higher being
     * brighter
     */
    public final void setLightLevel(float lightLevel) {
        this.lightLevel = lightLevel;
    }

    /**
     * Update the vertex colors of the Mesh of the Spatial and updates the
     * memoization.
     */
    public final void updateVertices() {
        Utility.setLightLevel(spatial, lightLevel);
        actualLightLevel = lightLevel;
    }

    /**
     *
     * @param health The new health of the block.
     */
    public void setHealth(float health) {
        this.health = health;
    }

    /**
     * Resets the health of the Block to the protoype's.
     */
    public void resetHealth() {
        Item item = Item.ITEMS[type];
        if (item instanceof BlockItem) { //This is a BlockItem
            health = ((BlockItem) item).getTimeToBreak();
            return;
        }
        if (item instanceof TorchItem) { //A Torch
            health = 0.2f;
        }
    }

    /**
     * Used for non-Cube Blocks only
     *
     * @param spatial The new Geometry of this Block
     */
    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
    }

    /**
     *
     * @param transparent If the Block is transparent
     */
    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    /**
     * Puts the block in the specified location within the specified chunk.
     *
     * @param x The new X coordinate
     * @param y The new Y coordinate
     * @param z The new Z coordinate
     * @param parent The Chunk that the Block will go in
     */
    public void setLocation(int x, int y, int z, Chunk parent) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.parent = parent;
        spatial.setLocalTranslation(x + parent.getX() * Main.MAX_BLOCKS, y, z + parent.getY() * Main.MAX_BLOCKS);
    }
}
