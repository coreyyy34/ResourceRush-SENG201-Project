package seng201.team53.game.items.towers;

import static seng201.team53.game.GameEnvironment.getGameEnvironment;

import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.util.Duration;
import seng201.team53.game.GameDifficulty;
import seng201.team53.game.GameEnvironment;
import seng201.team53.game.items.Cart;
import seng201.team53.game.items.Item;
import seng201.team53.game.items.ResourceType;
import seng201.team53.game.items.upgrade.Upgradeable;
import seng201.team53.game.state.CartState;
import seng201.team53.game.state.GameState;

/**
 * Represents a tower in the game.
 * Each tower has a type, broken property, last generate property, generate timeline, reload speed modifier
 * and an XP level
 */
public class Tower implements Item, Upgradeable {
    private final TowerType type;
    private final BooleanProperty brokenProperty = new SimpleBooleanProperty(false);
    private final LongProperty lastGenerateTimeProperty = new SimpleLongProperty(System.currentTimeMillis());
    private Timeline generateTimeline;
    private double reloadSpeedModifier = 1;
    private IntegerProperty xpLevel = new SimpleIntegerProperty(1);
    private boolean inInventory = false;

    /**
     * Constructs a new tower with the given type. This method also creates a new Timeline with 2 keyframes. This
     * timeline is responsible for determining the next time a tower will generate. A listener is also added to listen
     * to the game state property. When the game state changes to active, the generate timeline will play, if the round
     * is paused then the generate timeline will be paused.
     * @param type The type of the tower
     */
    protected Tower(TowerType type) {
        this.type = type;

        createGenerateLoop();

        getGameEnvironment().getStateHandler().getGameStateProperty().addListener(($, oldState, newState) -> {
            switch (newState) {
                case ROUND_COMPLETE -> increaseLevel();
                default -> {}
            }
        });
    }

    /**
     * Handle creating the Timeline to repeatedly call generate(), and wait for however long this tower needs to reload. 
     * @return Whether the timeline was successfully created. If JavaFX is not running (for unit tests), this function will do nothing and return false.
     */
    private boolean createGenerateLoop() {
        // Unfortunately cannot use a Timeline without JavaFX running.
        if (!Platform.isFxApplicationThread())
            return false;

        generateTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, event -> generate()),
            new KeyFrame(Duration.millis(getReloadMS()))
        );
        generateTimeline.setCycleCount(Timeline.INDEFINITE);

        // Pause the tower generating in line with the game state
        getGameEnvironment().getStateHandler().getGameStateProperty().addListener(($, oldState, newState) -> {
            switch (newState) {
                case ROUND_ACTIVE -> generateTimeline.play();
                case ROUND_PAUSE -> generateTimeline.pause();
                default -> {}
            }
        });
        return true;
    }

    /**
     * Generates the resources for the carts in the game round.
     * If the tower can generate, it will loop all the carts in the game round and attempt to give it a resource
     * of the specified type. The last generate time is set to the current time, if it generates.
     */
    private void generate() {
        if (!canGenerate())
            return;

        ResourceType resourceType = this.getPurchasableType().getResourceType();
        GameEnvironment.getGameEnvironment().getRound().getCarts().stream()
                .filter(cart -> cart.getCartState() == CartState.TRAVERSING_PATH)
                .forEach(cart -> cart.addResource(resourceType));
        setLastGenerateTime(System.currentTimeMillis());
    }

    /**
     * Checks if the tower is ready to generate a resource. The tower can generate if it is not broken, is not in
     * the inventory, the game state is active and if any of the carts can accept the resource
     * @return true if the tower is ready to generate, false otherwise
     */
    public boolean canGenerate() {
        if (isBroken())
            return false;

        if (isInInventory())
            return false;

        GameState gameState = getGameEnvironment().getStateHandler().getGameStateProperty().getValue();
        if (gameState != GameState.ROUND_ACTIVE)
            return false;

        List<Cart> carts = GameEnvironment.getGameEnvironment().getRound().getCarts();
        ResourceType resourceType = getPurchasableType().getResourceType();
        return carts.stream().anyMatch(cart -> !cart.isFull()
            && cart.getResourceType() == resourceType
            && cart.getCartState() == CartState.TRAVERSING_PATH);
    }

    /**
     * Adds a reload speed modifier of 0.25. Everytime this method is called, it will generate resources 25% faster,
     * until the modifier is reset.
     * The updateGenerateDelay() function is called. This allows the timeline for the generation to be updated with new
     * reload speed.
     */
    public void addReloadSpeedModifier() {
        reloadSpeedModifier += 0.25;
        updateGenerateDelay();
    }

    /**
     * Takes away a reload speed modifier of 0.25. Everytime this method is called, it will generate resources 25%
     * slower, until the modifier is reset
     * The updateGenerateDelay() function is called. This allows the timeline for the generation to be updated with new
     * reload speed.
     */
    public void minusReloadModifier() {
        reloadSpeedModifier -= 0.25;
        updateGenerateDelay();
    }

    /**
     * Resets the reload speed modifier. The tower will generate at its original rate
     * The updateGenerateDelay() function is called. This allows the timeline for the generation to be updated with new
     * reload speed.
     */
    public void resetReloadSpeedModifier() {
        reloadSpeedModifier = 1;
        updateGenerateDelay();
    }

    /**
     * Updates the generate timeline with a new reload delay.
     */
    private void updateGenerateDelay() {
        generateTimeline.stop();
        Duration delay = Duration.millis(getReloadMS());
        generateTimeline.getKeyFrames().set(1, new KeyFrame(delay));
        generateTimeline.play();
    }

    /**
     * Calculates the reload speed in milliseconds. This calculation takes into account the difficulty reload speed
     * modifier as well as the individual tower reload modifier
     * @return The modified reload speed of this tower in milliseconds.
     */
    private long getReloadMS() {
        GameDifficulty difficulty = GameEnvironment.getGameEnvironment().getDifficulty();
        double reloadSpeed = type.getReloadSpeed().toMillis();
        reloadSpeed /= difficulty.getTowerReloadModifier();
        reloadSpeed /= reloadSpeedModifier;
        reloadSpeed /= 1 + (double)getXpLevel() / 10;
        return (long)reloadSpeed;
    }

    /**
     * Retrieves the towers purchasable type
     * @return The type of tower
     */
    public TowerType getPurchasableType() {
        return type;
    }

    /**
     * Retrieves the broken property.
     * This property is observable meaning it can be watched for changes
     * @return The observable broken property
     */
    public BooleanProperty getBrokenProperty() {
        return brokenProperty;
    }

    /**
     * Retrieves the broken boolean of the tower
     * @return true if the tower is broken, false otherwise
     */
    public boolean isBroken() {
        return brokenProperty.get();
    }

    /**
     * Sets the towers broken status
     * @param broken true if the tower is broken, false otherwise
     */
    public void setBroken(boolean broken) {
        brokenProperty.set(broken);
    }

    /**
     * Retrieves the last generate time property.
     * This property is observable, meaning it can be watched for changes
     * @return The last generate time property
     */
    public LongProperty getLastGenerateTimeProperty() {
        return lastGenerateTimeProperty;
    }

    /**
     * Sets the last generate time
     * @param time The last generate time
     */
    public void setLastGenerateTime(long time) {
        lastGenerateTimeProperty.set(time);
    }

    /**
     * Retrieves the current XP level
     * @return The XP level
     */
    public int getXpLevel() {
        return xpLevel.get();
    }


    /**
     * Retrieves the current XP level property.
     * This property is observable, meaning it can be watched for changes
     * @return The current XP level property
     */
    public IntegerProperty getXpLevelProperty() {
        return xpLevel;
    }

    /**
     * Increases the XP level of this tower by one.
     */
    public void increaseLevel() {
        xpLevel.set(getXpLevel() + 1);
    }

    /**
     * Set whether this tower is in inventory or not.
     * @param inInventory Whether this item is now in inventory.
     */
    public void setInInventory(boolean inInventory) {
        this.inInventory = inInventory;
    }

    /**
     * @return Whether this Tower is currently stored in inventory.
     */
    public boolean isInInventory() {
        return inInventory;
    }

    /**
     * This interface represents the types of towers
     */
    public interface Type {

        /**
         * Represents the lumber mill tower type. This tower generates the WOOD resource
         */
        TowerType LUMBER_MILL = new TowerType("Lumber Mill Tower",
            "A Lumber Mill produces wood",
            ResourceType.WOOD,
            100,
            Duration.seconds(1));

        /**
         * Represents the mine tower type. This tower generates the STONE resource
         */
        TowerType MINE = new TowerType("Mine Tower",
            "A Mine produces ores",
            ResourceType.STONE,
            120,
            Duration.seconds(1));

        /**
         * Represents the quarry tower type. This tower generates the ORE resource
         */
        TowerType QUARRY = new TowerType("Quarry Tower",
            "A Quarry produces stone",
            ResourceType.ORE,
            150,
            Duration.seconds(1));

        /**
         * Represents the windmill tower type. This tower generates the ENERGY resource
         */
        TowerType WINDMILL = new TowerType("Windmill Tower",
            "A windmill produces energy",
            ResourceType.ENERGY,
            200,
            Duration.seconds(1));
    }
}