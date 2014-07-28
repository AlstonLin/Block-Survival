package alston.minecraft;

import com.jme3.material.Material;
import com.jme3.scene.Spatial;
import java.io.Serializable;

/**
 * An Object of this class represents a torch: A Block that does not naturally
 * spawn (must be crafted). It takes up the same space as a Block, but appears
 * to be smaller. It also emits a PointLight that will illuminate the
 * surroundings.
 *
 * @author Alston
 * @version RTM
 */
public class Torch extends Block implements Serializable {

    //Private constant to represent the object when saving
    private static final long serialVersionUID = 1213424812949L;
    private Light light;

    /**
     * Creates a new initial Torch; only call this if the protoype has not been
     * created yet.
     *
     * @param material The material for the Torch
     * @param spaial The spatial for the Torch
     */
    public Torch(Material material, Spatial spatial) {
        super(material, Item.TORCH, Item.TORCH, Block.WOOD_BASED);
        setSpatial(spatial);
        setTransparent(true);
        setHealth(0.2f);
    }

    /**
     * Basicly deos the same function as clone() to the prototype; call this
     * ONLY if the prototype has been created.
     */
    public Torch() {
        super(Main.blockPrototypes[Item.TORCH].getMaterial(), Item.TORCH, Item.TORCH, Block.WOOD_BASED);
        setSpatial(Main.blockPrototypes[getType()].getSpatial().clone(true));
        setTransparent(true);
        setHealth(0.2f);
    }

    @Override
    public void setLocation(int x, int y, int z, Chunk parent) { //Adds a Light in addition to setting the location
        super.setLocation(x, y, z, parent);
        light = new Light(this, 4f, 0.3f);
        Light.addPernamentLight(light);
    }

    @Override
    public void changeToBlock(int type, boolean updatePhysics) {
        super.changeToBlock(type, updatePhysics);
        Light.removePernamentLight(light);
    }
}
