package seng201.team53.game.map;

import javafx.scene.shape.Polyline;
import javafx.util.Duration;
import seng201.team53.exceptions.TileNotFoundException;
import seng201.team53.items.towers.Tower;
import seng201.team53.service.PathFinderService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a map in the game. It stores information about the map grid, tiles, and pathfinding
 * A map consists of a 2D array of tiles, where each tile represents a specific location on the map and holds
 * information about its properties (buildable, path, and the tower if there is a tower placed on this tile)
 */
public class GameMap {
    public static final int TILE_HEIGHT = 40;
    public static final int TILE_WIDTH = 40;
    private final String name;
    private final Tile[][] tiles;
    private final Polyline polylinePath;
    private final int pathLength;
    private MapInteraction interaction = MapInteraction.NONE;
    /** A mapping of towers on this map, and the tile they are placed on */
    private final Map<Tower, Tile> towers = new HashMap<>();

    /**
     * Constructs a new Map with the specified properties
     * @param name The name of the map
     * @param tiles The matrix of map tiles
     * @param startX The paths starting grid x coordinate
     * @param startY The paths starting grid y coordinate
     * @param endX The paths ending grid x co-ordinate
     * @param endY The paths ending grid y co-ordinate
     */
    public GameMap(String name, Tile[][] tiles, int startX, int startY, int endX, int endY) {
        this.name = name;
        this.tiles = tiles;

        var pathFinderService = new PathFinderService();
        pathFinderService.findPath(tiles, startX, startY, endX, endY);
        this.polylinePath = pathFinderService.generatePathPolyline();
        this.pathLength = pathFinderService.calculatePathLength();
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
     * @return The polyline version of the complete path
     */
    public Polyline getPolylinePath() {
        return polylinePath;
    }

    public Duration calculatePathDuration(float velocity) {
        float duration = pathLength / velocity;
        return Duration.seconds(duration);
    }

    /**
     * @return The list of towers currently placed on the map
     */
    public Collection<Tower> getTowers() {
        return towers.keySet();
    }

    public void addTower(Tower tower, Tile tile) {
        towers.put(tower, tile);
        tile.setTower(tower);
    }

    public void removeTower(Tile tile) {
        Tower tower = tile.getTower();
        towers.remove(tower);
        tile.setTower(null);
    }

    public MapInteraction getInteraction() {
        return interaction;
    }

    public void setInteraction(MapInteraction interaction) {
        this.interaction = interaction;
    }
}