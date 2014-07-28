package alston.minecraft;

/**
 * A factory that's sole purpose is to create a Block, or a subtype of it given
 * a type. It uses the Singleton design pattern to prevent other instances of
 * being called.
 *
 * @author Alston
 * @version RTM
 */
public class BlockFactory {

    private static BlockFactory instance;

    /**
     * Does nothing; only exists to prevent other instances of this to be
     * created.
     */
    private BlockFactory() {
    }

    /**
     * Uses lazy initialization for BlockFactory, so that it will only have a
     * BlockFactory when it is needed.
     *
     * @return The singleton instance of the BlockFactory
     */
    public static BlockFactory getInstance() {
        if (instance == null) {
            instance = new BlockFactory();
        }
        return instance;
    }

    /**
     * Creates a Block of the specified type ONLY IF it is subtype of Block, for
     * efficiency purposes (prevents unneccesary mallocs and frees)
     *
     * @param type The type of Block to be created
     * @return Either a clone of a subtype of Block prototype, or null if it is
     * not a subtype.
     */
    public Block makeBlock(int type) {
        if (Main.blockPrototypes[type] instanceof Clickable) { //If it's Clickable, it has to be a subtype
            if (Main.blockPrototypes[type] instanceof CraftingTable) {
                return new CraftingTable(Main.blockPrototypes[type].getMaterial()); //No need to clonbe; only 1 type of workbench
            }
        }
        if (Main.blockPrototypes[type] instanceof Torch) { //It's a torch
            return new Torch();
        }
        return null; //Returns null if it is a plain Block
    }
}
