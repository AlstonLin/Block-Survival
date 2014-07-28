package alston.minecraft;

import com.jme3.audio.AudioNode;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Iterator;

/**
 * A type of mob that is melee and will walk towards the Player if it is near
 * and attack him if he is near.
 *
 * @author Alston
 * @version RTM
 */
public class Zombie extends Mob implements Serializable {

    //Private constant used for Object serialization
    private static final long serialVersionUID = 3312234812948L;
    private static final Spatial zombieSpatialPrototype;
    private long lastSoundTime;
    private transient AudioNode sound; //Ambient sound
    private transient AudioNode hurt; //Sound it makes when it's hurt

    static {
        Material material = new Material(Main.getInstance().getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        material.setTexture("DiffuseMap", Main.getInstance().getAssetManager().loadTexture("Textures/zombie.png"));
        zombieSpatialPrototype = Main.getInstance().getAssetManager().loadModel("Models/Zombie.j3o");
        zombieSpatialPrototype.setMaterial(material);
    }

    /**
     * Creates a new Zombie at the given coordinates.
     *
     * @param x The x component of the location vector
     * @param y The y component of the location vector
     * @param z The z component of the location vector
     */
    public Zombie(float x, float y, float z){
        super(updateNodeVertices(zombieSpatialPrototype.deepClone()), x, y, z, 10, 1, 0.5f);
        setupSounds();
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
        setupSounds();
    }

    /**
     * Updates the spatial's vertices counts to correct inaccuracies in cloning.
     *
     * @param spatial The spatial to update
     * @return The updated spatial
     */
    private static Spatial updateNodeVertices(Spatial spatial) {
        if (spatial instanceof Node) {
            Node node = (Node) spatial;
            Iterator<Spatial> iterator = node.getChildren().iterator();
            node.detachAllChildren();
            while (iterator.hasNext()) {
                Geometry child = (Geometry) iterator.next();
                child.getMesh().updateCounts();
                node.attachChild(child);
            }
            return node;
        } else {
            Geometry geometry = (Geometry) spatial;
            geometry.getMesh().updateCounts();
            return geometry;
        }
    }

    /**
     * Checks if the player can be attacked and returns if they were attacked
     *
     * @param player
     * @return If the player was attacked
     */
    public boolean checkForAttack(Player player) {
        if (!(player.getControl().getPhysicsLocation().distance(getControl().getPhysicsLocation()) > 1.5f)) { //Close to player
            attack(player);
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        Player player = Main.currentGame.getPlayer();
        getControl().setWalkDirection(Vector3f.ZERO); //Resets the walk direction
        if (!checkForAttack(player) && isPlayerVisible(player)) { //Player not attacked and is visible/near
            walkTowardsPlayer(player);
        }
        updateSounds();
    }

    private void setupSounds() {
        sound = new AudioNode(Main.getInstance().getAssetManager(), "Sounds/zombie.wav", false);
        hurt = new AudioNode(Main.getInstance().getAssetManager(), "Sounds/zombieHurt.wav", false);
        sound.setPositional(true);
        sound.setVolume(0.3f);
        sound.setRefDistance(0.5f);
        sound.setMaxDistance(10f);
        hurt.setPositional(true);
    }

    /**
     * Moves the zombie towards the player and faces him.
     *
     * @param player The player to walk towards to
     */
    private void walkTowardsPlayer(Player player) {
        Vector3f directionToPlayer = getControl().getPhysicsLocation().subtract(player.getControl().getPhysicsLocation()); //Direction Vector
        directionToPlayer = new Vector3f(directionToPlayer.x, 0, directionToPlayer.z);
        getControl().setViewDirection(directionToPlayer.negate()); //Looks at the player
        getControl().setWalkDirection(directionToPlayer.negate().mult(0.02f / directionToPlayer.length())); //So it walks towards and not away and ensures const speed
    }

    /**
     * Updates the zombie's sounds.
     */
    private void updateSounds() {
        if (System.nanoTime() > lastSoundTime + (0.5 + Math.random()) * 20e9) { //Once every 5 seconds
            sound.play();
            lastSoundTime = System.nanoTime();
        }
    }

    @Override
    public void substractHealth(int health) {
        super.substractHealth(health);
        hurt.play();
    }

    @Override
    public void setupSpatial() {
        setSpatial(updateNodeVertices(zombieSpatialPrototype.deepClone()));
    }
}
