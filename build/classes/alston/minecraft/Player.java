package alston.minecraft;

import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This class contains the attributes of the representation of the user and
 * provides an easy container for Serialization.
 *
 * @author Alston
 * @version RTM
 */
public class Player implements Serializable {

    //Private constant to represent the object when saving
    private static final long serialVersionUID = 1224124124812949L;
    private Inventory inventory; //The player's inventory, which contains all his Items
    private float x, y, z; //Tracks the location upon Serialization
    private transient long lastAttackedTime;
    private float attackSpeed;
    private int attackDamage;
    private HealthBar healthBar;
    private transient CharacterControl control; //Physics for the user

    /**
     * Creates a new Player by initiating the Inventory, and control, then
     * adding it to the physics.
     *
     * @param bar The InventoryBar of the Game (Bug Fix)
     */
    public Player(InventoryBar bar) {
        inventory = new Inventory(bar);
        healthBar = new HealthBar();
        attackSpeed = 2f;
        attackDamage = 1;
        //Sets up the control
        control = new CharacterControl(new CapsuleCollisionShape(0.45f, 0.55f), 0.6f); //Creates a character control with a round rigid body
        control.setJumpSpeed(5f);
        control.setGravity(9.81f);
        Main.bulletAppState.getPhysicsSpace().add(control);
    }

    /**
     * Called by the Object reader when the Object is read.
     *
     * @param input The ObjectInputStream provided by the Object reader
     * @throws IOException Something went wrong while reading
     * @throws ClassNotFoundException Could not find the class of something that
     * belonged to this (usually serialVersionUUID is wrong)
     */
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException { //When it's being read from file
        input.defaultReadObject(); //Calls default reader first
        control = new CharacterControl(new CapsuleCollisionShape(0.45f, 0.6f), 0.6f); //Creates a character control with a round rigid body
        control.setJumpSpeed(6f);
        control.setGravity(10f);
        control.setPhysicsLocation(new Vector3f(x, y, z)); //Places the player's location at the variables
        Main.bulletAppState.getPhysicsSpace().add(control);
    }

    /**
     * Called by the Object reader when the Object is written.
     *
     * @param output The ObjectOnputStream provided by the Object writer
     * @throws IOException Something went wrong while writing
     */
    private void writeObject(ObjectOutputStream output) throws IOException { //When it is being written
        //Stores the lcoation
        x = control.getPhysicsLocation().x;
        y = control.getPhysicsLocation().y;
        z = control.getPhysicsLocation().z;
        output.defaultWriteObject(); //Calls default writer
    }

    /**
     * Attacks the given Mob if an attack is ready.
     *
     * @param mob The Mob to attack
     */
    public void attack(Mob mob) {
        if (System.nanoTime() > lastAttackedTime + (1e9 / attackSpeed) 
                && mob.getControl().getPhysicsLocation().distance(control.getPhysicsLocation()) < 1.5f) { //Attack is ready and mob is in range
            mob.substractHealth(attackDamage);
            lastAttackedTime = System.nanoTime();
        }
    }

    /**
     *
     * @return The inventory of the player
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     *
     * @return The physics control for the player
     */
    public CharacterControl getControl() {
        return control;
    }

    /**
     *
     * @return The health bar of the Player
     */
    public HealthBar getHealthBar() {
        return healthBar;
    }
}
