package alston.minecraft;

import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.scene.Spatial;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Represents the generalizaion of all the hostile NPCs.
 *
 * @author Alston
 * @version RTM
 */
public abstract class Mob implements Serializable {

    //Private constant used for Object serialization
    private static final long serialVersionUID = 98324124124812948L;
    /**
     * Maps all the Mob's spatials to themseleves for collision detection.
     */
    public static final HashMap<Spatial, Mob> spatialMap;
    private transient Spatial spatial;
    private transient CharacterControl control;
    private int health, attackDamage;
    private float attackSpeed;
    private float x, y, z; //Used onlyf ro serialization to keep track of location
    private long lastAttackedTime;

    static {
        spatialMap = new HashMap();
    }

    /**
     * Creates a new Mob; should be only called thru a super() call.
     *
     * @param spatial The spatial of the mob
     * @param health The health of the mob
     * @param attackDamage The damage per attack opf the mob
     * @param attackSpeed The number of attacks per second
     */
    public Mob(Spatial spatial, float x, float y, float z, int health, int attackDamage, float attackSpeed) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.spatial = spatial;
        this.health = health;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        spatialMap.put(spatial, this);
        setup();
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
        input.defaultReadObject();
        setupSpatial();
        setup();
        spatialMap.put(spatial, this); //Maps spatial to this Mob   
    }

    /**
     * Called by the Object reader when the Object is written.
     *
     * @param output The ObjectOnputStream provided by the Object writer
     * @throws IOException Something went wrong while writing
     */
    private void writeObject(ObjectOutputStream output) throws IOException { //When it is being written
        //Stores the location
        x = control.getPhysicsLocation().x;
        y = control.getPhysicsLocation().y;
        z = control.getPhysicsLocation().z;
        output.defaultWriteObject(); //Calls default writer
    }

    /**
     * Sets up the spatial and control.
     */
    private void setup() {
        spatial.setLocalTranslation(x, y, z);
        control = new CharacterControl(new CapsuleCollisionShape(0.45f, 0.6f), 0.6f);
        spatial.addControl(control);
        Main.blockNode.attachChild(spatial);
        Main.bulletAppState.getPhysicsSpace().add(control);
    }

    /**
     * Updates the Mob.
     */
    public abstract void update();

    /**
     * Sets up the spatial at de-serialization.
     */
    public abstract void setupSpatial();

    /**
     * Attacks the given player
     *
     * @param player The player thatw as attacked
     */
    public void attack(Player player) {
        if (System.nanoTime() > lastAttackedTime + 1 / attackSpeed * 1e9) { //Next attack is ready
            player.getHealthBar().subtractHealth(attackDamage);
            lastAttackedTime = System.nanoTime();
        }
    }

    /**
     * Checks if the given Player and this Mob is within 20 Blocks of each other.
     *
     * @param player The Player to check
     * @return If the player is near and in a direct line of sight
     */
    protected boolean isPlayerVisible(Player player) {
        try {
            if (getControl().getPhysicsLocation().distance(player.getControl().getPhysicsLocation()) < 30f) { //Direst line of sight and near
                return true;
            }
        } catch (NullPointerException e) { //No Collision
        }
        return false;
    }

    /**
     * Removes this Mob from rendering and physics.
     */
    public void despawn() {
        spatial.removeFromParent();
        Main.bulletAppState.getPhysicsSpace().remove(control);
        Main.currentGame.getMobs().remove(this);
        spatialMap.remove(spatial);
    }

    /**
     * Substracts the given health from this mob's health
     *
     * @param health Health to be removed
     */
    public void substractHealth(int health) {
        this.health -= health;
        if (this.health <= 0) { //Zombie dies
            despawn();
        }
    }

    /**
     *
     * @return The physics control for the Mob
     */
    public CharacterControl getControl() {
        return control;
    }

    /**
     *
     * @return The Spatial of the Mob
     */
    public Spatial getSpatial() {
        return spatial;
    }

    /**
     *
     * @param spatial The new spatial for the Mob
     */
    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
    }

    /**
     *
     * @param control The physics control Object for the Mob
     */
    public void setControl(CharacterControl control) {
        this.control = control;
    }
}
