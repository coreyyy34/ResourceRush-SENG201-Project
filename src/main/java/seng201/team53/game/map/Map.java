package seng201.team53.game.map;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polyline;
import javafx.util.Duration;
import seng201.team53.exceptions.TileNotFoundException;
import seng201.team53.items.towers.Tower;
import seng201.team53.service.PathFinderService;

/**
 * This class represents a map in the game. It stores information about the map grid, tiles, and pathfinding
 * A map consists of a 2D array of tiles, where each tile represents a specific location on the map and holds
 * information about its properties (buildable, path, and the tower if there is a tower placed on this tile)
 * The map also calculate paths for carts to navigate the map using a depth-first search algorithm
 */
public class Map {
    public static final int TILE_HEIGHT = 40;
    public static final int TILE_WIDTH = 40;
    private final String name;
    private final Tile[][] tiles;
    private final Stack<Point> path = new Stack<>();
    private final Polyline polylinePath;
    /** A mapping of towers on this map, and the tile they are placed on */
    private final HashMap<Tower, Tile> towers = new HashMap<>();
    private final int startX;
    private final int startY;
    private final int endX;
    private final int endY;
    private final GridPane gridPane;
    private final Pane overlay;
    private MapInteraction currentInteraction = MapInteraction.NONE;
    private Tower selectedTower;

    /**
     * Constructs a new Map with the specified properties
     * @param name The name of the map
     * @param tiles The matrix of map tiles
     * @param startX The paths starting grid x coordinate
     * @param startY The paths starting grid y coordinate
     * @param endX The paths ending grid x co-ordinate
     * @param endY The paths ending grid y co-ordinate
     */
    public Map(String name, Tile[][] tiles, int startX, int startY, int endX, int endY, GridPane gridPane, Pane overlay) {
        this.name = name;
        this.tiles = tiles;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.gridPane = gridPane;
        this.overlay = overlay;
        this.overlay.setOnMouseClicked(this::onMouseClicked);

        var pathFinderService = new PathFinderService();
        pathFinderService.findPath(tiles, startX, startY, endX, endY);
        polylinePath = pathFinderService.generatePathPolyline();
    }

    /**
     * @return The name of the map
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the tile object at the specified coordinates on the map grid
     * @param tileX The X coordinate of the tile on the grid (zero-based indexing)
     * @param tileY The Y coordinate of the tile on the grid (zero-based indexing)
     * @return The tile object at the given coordinates, or null if the coordinates are outside the map bounds
     * @throws TileNotFoundException If the requested tile coordinates are outside the valid range of the map
     */
    public Tile getTileAt(int tileX, int tileY) throws TileNotFoundException {
        if (tileX >= 0 && tileX < tiles.length && tileY >= 0 && tileY < tiles[tileX].length)
            return tiles[tileY][tileX];
        throw new TileNotFoundException("Tile does not exist at x=" + tileX + ", y=" + tileY);
    }

    /**
     * Retrieves the tile object at the specified coordinates on the map grid
     * @param screenX The X coordinate of the screen (in pixels)
     * @param screenY The Y coordinate of the screen (in pixels)
     * @return The tile object at the given coordinates, or null if the coordinates are outside the map bounds
     */
    public Tile getTileFromScreenPosition(int screenX, int screenY) throws TileNotFoundException {
        int tileX = Math.floorDiv(screenX, TILE_WIDTH);
        int tileY = Math.floorDiv(screenY, TILE_HEIGHT);
        return getTileAt(tileX, tileY);
    }

    /**
     * @return The list of points which makes up the complete path
     */
    public List<Point> getPath() {
        return path;
    }

    /**
     * @return The polyline version of the complete path
     */
    public Polyline getPolylinePath() {
        return polylinePath;
    }

    public Duration calculatePathDuration(float velocity) {
        var pathLength = path.size() + 2; // add 2 to take into account starting off screen and ending off screen
        float duration = pathLength / velocity;
        return Duration.seconds(duration);
    }

    /**
     * @return The list of towers currently placed on the map
     */
    public Collection<Tower> getTowers() {
        return towers.keySet();
    }

    /**
     * @return The current map interation
     */
    public MapInteraction getCurrentInteraction() {
        return currentInteraction;
    }

    /**
     * Sets the current map interaction state and handle grid display
     * @param interaction The new map interation state to set
     */
    public void setInteraction(MapInteraction interaction) {
        currentInteraction = interaction;
        gridPane.setGridLinesVisible(interaction != MapInteraction.NONE);
    }

    /**
     * Begins the process of placing a tower on the map. This method begins a
     * visual representation of the tower
     * that follows the mouse curser and sets the map interaction state
     * @param tower The tower to be placed
     */
    public boolean startPlacingTower(Tower tower, double mouseX, double mouseY) {


        this.setInteraction(MapInteraction.PLACE_TOWER);
        this.selectedTower = tower;
        ImageView towerImage = selectedTower.getImageView();
        // start it off screen
        towerImage.setX(mouseX - 20);
        towerImage.setY(mouseY - 20);
        overlay.getChildren().add(towerImage);
        overlay.setOnMouseMoved(event -> {
            towerImage.setX(event.getX() - 20);
            towerImage.setY(event.getY() - 20);
        });
        return true;
    }

    /**
     * Stops the process of placing a tower on the map. This method removes the visual representation of the tower
     * and resets the map interaction state
     */
    private void stopPlacingTower() {
        overlay.setOnMouseMoved(null);
        overlay.getChildren().remove(selectedTower.getImageView());
        selectedTower = null;
    }

    /**
     * Try place the tower onto the map on the given tile.
     * @param tower The tower to place
     * @param tile The tile to place the tower on
     * @return Whether the Tower was added to the map/placed on the tile. Will return false if the given tile is not buildable or part of the cart's
     *         path.
     */
    private boolean addTower(Tower tower, Tile tile) {

        // Tile is not buildable or already occupied
        if (!tile.isBuildable() || tile.getTower() != null)
            return false;

        var imageView = tower.getImageView();
        gridPane.add(imageView, tile.getX(), tile.getY());
        towers.put(selectedTower, tile);
        tile.setTower(selectedTower);
        return true;
    }

    private boolean removeTower(Tower tower) {
        // Remove the tower from the map
        gridPane.getChildren().remove(tower.getImageView());
        Tile tile = towers.get(tower);
        tile.setTower(null);
        towers.remove(tower);
        return true;
    }

    /**
     * Handle map interactions when the user clicks their mouse on the screen.
     */
    private void onMouseClicked(MouseEvent event) {
        // Only care about left clicking
        if (event.getButton() != MouseButton.PRIMARY)
            return;

        int mouseX = (int)Math.round(event.getSceneX());
        int mouseY = (int)Math.round(event.getSceneY());

        Tile tile;
        try {
            tile = this.getTileFromScreenPosition(mouseX, mouseY);
        } catch (TileNotFoundException e) {
            return;
        }

        Tower tileTower = tile.getTower();

        MapInteraction interaction = this.getCurrentInteraction();

        // If the user is not currently interacting with something, and selects a tower, then start moving the tower.
        if (interaction == MapInteraction.NONE && tileTower != null) {
            removeTower(tileTower);
            startPlacingTower(tileTower, event.getX(), event.getY());
            // MapInteraction is set to MOVE_TOWER from above call
            return;
        }

        // PLACE_TOWER
        if (interaction == MapInteraction.PLACE_TOWER) {
            boolean placed = addTower(selectedTower, tile);
            if (placed) {
                setInteraction(MapInteraction.NONE);
                stopPlacingTower();
            }
            return;
        }
    }


    public GridPane getGridPane() {
        return gridPane;
    }

    public Pane getOverlay() {
        return overlay;
    }
}
