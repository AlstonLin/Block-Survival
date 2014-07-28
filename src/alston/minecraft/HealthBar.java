package alston.minecraft;

import com.jme3.audio.AudioNode;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.ui.Picture;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Represents the visual health bar and contains all interactions with the
 * Player's health.
 *
 * @author Alston
 * @version RTM
 */
public class HealthBar implements Serializable {

    //Private constant used for Object serialization
    private static final long serialVersionUID = 1342242256235812838L;
    private static final transient AudioNode hurt; //Sound of player getting hurt
    private static final Picture healthPicPrototype; //Static health pic prototype
    private transient Picture[] healthPics;
    private int health;

    static {
        healthPicPrototype = new Picture("Health Pic");
        healthPicPrototype.setWidth(Main.APP_SETTINGS.getWidth() / 25);
        healthPicPrototype.setHeight(Main.APP_SETTINGS.getWidth() / 25);
        healthPicPrototype.setQueueBucket(RenderQueue.Bucket.Gui);
        healthPicPrototype.setImage(Main.getInstance().getAssetManager(), "Interface/health.png", true);
        hurt = new AudioNode(Main.getInstance().getAssetManager(), "Sounds/hurt.wav", false);
    }

    /**
     * Creates a new health bar with full (10) health.
     */
    public HealthBar() {
        health = 10;
        setupPics();
    }

    /**
     * Called by the Object reader when the object is read.
     *
     * @param input The ObjectInputStream provided by the Object reader
     * @throws IOException Something went wrong while reading
     * @throws ClassNotFoundException Could not find the class of something that
     * belonged to this (usually serialVersionUUID is wrong)
     */
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject(); //Calls default reader first
        setupPics();
        subtractHealth(0); //Resets the health
    } //End of readObject

    /**
     * Sets up the healthPics.
     */
    private void setupPics() {
        healthPics = new Picture[10];
        //Fill the healthPics and put them in the approriate spaces
        for (int i = 0; i < 10; i++) {
            healthPics[i] = (Picture) healthPicPrototype.deepClone();
            healthPics[i].setPosition(Main.APP_SETTINGS.getWidth() / 4 + (i * Main.APP_SETTINGS.getWidth() / 23), Main.APP_SETTINGS.getHeight() / 6);
            Main.getInstance().getGuiNode().attachChild(healthPics[i]);
            healthPics[i].move(0, 0, -2); //Behind the windows
        }
    }

    /**
     * Checks for death then substracts health from the Player if he is not
     * dead.
     *
     * @param amount
     */
    public void subtractHealth(int amount) {
        health -= amount;
        if (amount != 0) { //Not just deserializaing
            hurt.playInstance();
        }
        if (health <= 0) {
            killWorld();
        } else {
            //Hides all the images
            for (int i = 0; i < 10; i++) {
                Main.getInstance().getGuiNode().detachChild(healthPics[i]);
            }
            //Puts all necessary health pics on the screen
            for (int i = 0; i < health; i++) {
                Main.getInstance().getGuiNode().attachChild(healthPics[i]);
            }
        }
    }

    /**
     * Ends the game without saving.
     */
    private void killWorld() {
        Picture gameOver = new Picture("Game Over");
        gameOver.setWidth(Main.APP_SETTINGS.getWidth() / 2);
        gameOver.setHeight(Main.APP_SETTINGS.getWidth() / 2);
        gameOver.setQueueBucket(RenderQueue.Bucket.Gui);
        gameOver.setImage(Main.getInstance().getAssetManager(), "Interface/gameOver.png", true);
        gameOver.setPosition(Main.APP_SETTINGS.getWidth() / 4, 0);
        //Shows the message
        Main.getInstance().getGuiNode().detachAllChildren();
        Main.getInstance().getGuiNode().attachChild(gameOver);
        Main.gameOver = true;
    }

    /**
     *
     * @return Player's health
     */
    public int getHealth() {
        return health;
    }
}
