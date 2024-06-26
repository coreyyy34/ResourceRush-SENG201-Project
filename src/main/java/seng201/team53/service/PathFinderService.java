package seng201.team53.service;

import javafx.scene.shape.Polyline;
import seng201.team53.game.map.Tile;

import java.awt.*;
import java.util.Stack;

import static seng201.team53.game.map.GameMap.TILE_HEIGHT;
import static seng201.team53.game.map.GameMap.TILE_WIDTH;

/**
 * This service finds and validates a path in the map given a matrix
 */
public class PathFinderService {
    /**
     * The x value for directions that a path can be connected to.
     * This takes into account the left and right values
     */
    public static final int[] X_DIRECTIONS = {-1, 0, 1, 0};

    /**
     * The y value for directions that a path can be connected to.
     * This takes into account the top and bottom values
     */
    public static final int[] Y_DIRECTIONS = {0, 1, 0, -1};
    private final Stack<Point> path = new Stack<>();
    private final Polyline polylinePath = new Polyline();

    /**
     * Generates a polyline path representing the movement path for carts on the map
     * This method calculates a series of points that define a path that carts will follow. The first and last point
     * are offset to allow the path to start and end off-screen
     * @return The generated polyline
     */
    public Polyline generatePathPolyline() {
        if (!polylinePath.getPoints().isEmpty())
            throw new IllegalStateException("Map already has a polyline path calculated.");
        Point firstPoint = path.get(0);
        polylinePath.getPoints().add((double)firstPoint.y * TILE_WIDTH - (TILE_WIDTH / 2));
        polylinePath.getPoints().add((double)firstPoint.x * TILE_HEIGHT + (TILE_HEIGHT / 2));
        for (Point point : path) {
            polylinePath.getPoints().add((double)point.y * TILE_WIDTH + (TILE_WIDTH / 2));
            polylinePath.getPoints().add((double)point.x * TILE_HEIGHT + (TILE_HEIGHT / 2));
        }
        Point lastPoint = path.get(path.size() - 1);
        polylinePath.getPoints().add((double)lastPoint.y * TILE_WIDTH + (TILE_WIDTH / 2));
        polylinePath.getPoints().add((double)lastPoint.x * TILE_HEIGHT + ((3 * TILE_HEIGHT) / 2));
        return polylinePath;
    }

    /**
     * Calculates the path length. The value 2 is added to the calculation to take into account
     * starting the path off-screen and ending off-screen
     * @return The calculated path length
     */
    public int calculatePathLength() {
        return path.size() + 2;
    }

    /**
     * Finds a path from the starting point (startX, startY) to the ending point (endX, endY) on the map
     * This method uses a depth-first search algorithm to explore possible paths on the map
     * It throws an exception if a path cannot be found or if a path has already been calculated for this map
     * @param tiles The 2D matrix of tiles
     * @param startX The X coordinate of the start point
     * @param startY The Y coordinate of the start point
     * @param endX The X coordinate of the end point
     * @param endY The Y coordinate of the end point
     */
    public void findPath(Tile[][] tiles, int startX, int startY, int endX, int endY) {
        int[][] discovered = new int[tiles.length][tiles[0].length];
        if (!depthFirstSearch(tiles, discovered, startX, startY, endX, endY))
            throw new RuntimeException("Invalid map, does not contain a path");
    }

    /**
     * Recursive helper method for the findPath algorithm that implements a depth-first search.
     * This method explores possible paths on the map by recursively checking neighboring tiles until it reaches
     * the end point or exhausts all valid options. It marks visited tiles and builds the path by pushing and popping
     * coordinates onto a stack
     * @param discovered A 2D array to keep track of visited tiles (0: unvisited, 2: visited)
     * @param x The current X coordinate on the map
     * @param y The current Y coordinate on the map
     * @param endX The X coordinate of the end point
     * @param endY The Y coordinate of the end point
     * @return True if a path is found, false otherwise
     */
    private boolean depthFirstSearch(Tile[][] tiles, int[][] discovered, int x, int y, int endX, int endY) {
        discovered[x][y] = 2;
        if (x == endX && y == endY) {
            path.push(new Point(x, y));
            return true;
        }
        for (int i = 0; i < X_DIRECTIONS.length; i++) {
            int newX = x + X_DIRECTIONS[i];
            int newY = y + Y_DIRECTIONS[i];
            if (!isValidTile(tiles, discovered, newX, newY))
                continue;
            path.push(new Point(x, y));
            if (depthFirstSearch(tiles, discovered, newX, newY, endX, endY))
                return true;
            path.pop();
        }
        return false;
    }

    /**
     * Checks whether a tile on the map is a valid candidate for the path finding algorithm.
     * A valid cell is within the map boundaries, is marked as a path tile, and has not been visited yet
     * @param discovered A 2D array to keep track of visited tiles (0: unvisited, 1: on stack, 2: visited)
     * @param x The X coordinate of the cell to check
     * @param y The Y coordinate of the cell to check
     * @return True if the tile is a valid candidate, false otherwise
     */
    private boolean isValidTile(Tile[][] tiles, int[][] discovered, int x, int y) {
        return (x >= 0
            && y >= 0
            && x < tiles.length
            && y < tiles[x].length
            && tiles[x][y].isPath()
            && discovered[x][y] == 0);
    }
}
