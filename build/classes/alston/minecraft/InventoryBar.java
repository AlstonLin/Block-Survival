package alston.minecraft;

import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Contains all the Pictures for the Inventory Bar GUI.
 *
 * @author Alston
 * @verion RTM
 */
public class InventoryBar implements Serializable {

    //Private constant used for Object serialization
    private static final long serialVersionUID = 3724124124224812838L;
    private transient Node node;
    private transient Picture selector; //Selector of Items

    /**
     * Creates a new Inventory Bar
     */
    public InventoryBar() {
        node = new Node("Inventory Bar");
        setupSelector();
        Main.getInstance().getGuiNode().attachChild(node);
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
        node = new Node("Inventory Bar");
        setupSelector();
        Main.getInstance().getGuiNode().attachChild(node);
    } //End of readObject

    /**
     * Initiates, then sets up the Picture for the inventory selector. Should
     * only be called during the start of the application.
     */
    private void setupSelector() {
        selector = new Picture("Selector");
        selector.setImage(Main.getInstance().getAssetManager(), "Interface/selector.png", true);
        selector.setHeight(Main.APP_SETTINGS.getWidth() / 11);
        selector.setWidth(Main.APP_SETTINGS.getWidth() / 11);
        selector.setPosition(Main.APP_SETTINGS.getWidth() / 11, 0);
        selector.setQueueBucket(RenderQueue.Bucket.Gui);
        selector.move(0, 0, -1); //Puts it ontop of other images
        node.attachChild(selector);
    }

    /**
     * Attaches the given Item to the given slot
     *
     * @param item The Item to be attached
     * @param slot Slot to be put in
     */
    public void attach(Item item) {
        item.getBarPicture().setPosition((item.getSlotX() + 1) * Main.APP_SETTINGS.getWidth() / 11, 0);
        item.getBarPicture().move(0, 0, -2); //Gives inventory precedence over bar
        item.getBarNumberText().setLocalTranslation(item.getBarPicture().getLocalTranslation()
                .add(0, Main.APP_SETTINGS.getHeight() / 30f, 1));
        node.attachChild(item.getBarPicture());
        node.attachChild(item.getBarNumberText());
    }

    /**
     * Detaches the given Item from the bar
     *
     * @param item Item to be detached
     */
    public void detach(Item item) {
        node.detachChild(item.getBarPicture());
        node.detachChild(item.getBarNumberText());
    }

    /**
     *
     * @return The Picture of the Selector
     */
    public Picture getSelector() {
        return selector;
    }
}
