package seng201.team53.game.map;

import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Polyline;
import seng201.team53.App;
import seng201.team53.items.towers.Tower;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Map {

    public static final int TILE_HEIGHT = 40;
    public static final int TILE_WIDTH = 40;
    private static final int[] X_DIRECTIONS = { -1, 0, 1, 0 };
    private static final int[] Y_DIRECTIONS = { 0, 1, 0, -1 };
    private final String name;
    private final Tile[][] tiles;
    private final Stack<Point> path = new Stack<>();
    private final Polyline polylinePath = new Polyline();
    private final List<Tower> towers = new ArrayList<>();
    private final int startX;
    private final int startY;
    private final int endX;
    private final int endY;

    private MapInteraction currentInteraction = MapInteraction.NONE;
    private Tower selectedTower;
    private ImageView selectedTowerImage;

    public Map(String name, Tile[][] tiles, int startX, int startY, int endX, int endY) {
        this.name = name;
        this.tiles = tiles;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        findPath();
        generatePathPolyline();
    }

    public String getName() {
        return name;
    }

    public Tile[][] getTiles() {
        return tiles;
    }
    public Tile getTileAt(int tileX, int tileY) {
        // because we are using a 2D array to store tiles, we have to get the row first (y value)
        // then get the column (x value) hence tiles[tileY][tileX]
        if (tileX >= 0 && tileX < tiles.length && tileY >= 0 && tileY < tiles[tileX].length)
            return tiles[tileY][tileX];
        throw new RuntimeException("Tile does not exist at x=" + tileX + ", y=" + tileY);
    }
    public Tile getTileFromScreenPosition(int screenX, int screenY) {
        // screen x consists of 20 columns of width 40px each
        // screen y consists of 20 rows of height 40px each
        int tileX = Math.floorDiv(screenX, TILE_WIDTH);
        int tileY = Math.floorDiv(screenY, TILE_HEIGHT);
        return getTileAt(tileX, tileY);
    }

    public List<Point> getPath() {
        return path;
    }

    public Polyline getPolylinePath() {
        return polylinePath;
    }

    public List<Tower> getTowers() {
        return towers;
    }

    public MapInteraction getCurrentInteraction() {
        return currentInteraction;
    }
    public void setInteraction(MapInteraction interaction) {
        currentInteraction = interaction;

        // When the user is interacting with the map, scale the tiles down slightly
        // (scuffed way to make a border/highlight on each tile)
        if (interaction != MapInteraction.NONE) {
            scaleTiles(0.95);
        } else {
            scaleTiles(1);
        }
    }

    public Tower getSelectedTower() {
        return selectedTower;
    }
    public void startPlacingTower(Tower tower) {
        this.setInteraction(MapInteraction.PLACE_TOWER);
        selectedTowerImage = tower.getImageView();
        AnchorPane pane = App.getApp().getGameEnvironment().getWindow().getController().test;
        pane.getChildren().add(selectedTowerImage);
        pane.setOnMouseMoved(event -> {
            selectedTowerImage.setX(event.getX() - 20);
            selectedTowerImage.setY(event.getY() - 20);
        });
        selectedTower = tower;
    }
    public void stopPlacingTower() {
        AnchorPane pane = App.getApp().getGameEnvironment().getWindow().getController().test;
        pane.setOnMouseMoved(null);
        pane.getChildren().remove(selectedTowerImage);
        this.selectedTower = null;
        selectedTowerImage = null;
    }
    public void placeTower(Tower tower, Tile tile) {
        var gameController = App.getApp().getGameEnvironment().getWindow().getController();
        var gridPane = gameController.getGridPane();

        ImageView imageView = tower.getImageView();
        Point point = tile.getPoint();
        gridPane.add(imageView, point.x, point.y);
        towers.add(tower);
        tile.setTower(tower);
        // add tower to both the tile and to the maps array list
        // makes it faster to loop through all towers
        // or to find a tower at a given x,y
    }

    private void generatePathPolyline() {
        if (!polylinePath.getPoints().isEmpty())
            throw new IllegalStateException("Map already has a polyline path calculated.");
        // start off screen
        var firstPoint = path.get(0);
        polylinePath.getPoints().add(firstPoint.y * 40 - 20.0);
        polylinePath.getPoints().add(firstPoint.x * 40 + 20.0);
        for (var point : path) {
            polylinePath.getPoints().add(point.y * 40 + 20.0);
            polylinePath.getPoints().add(point.x * 40 + 20.0);
        }
        // end off screen
        var lastPoint = path.get(path.size() - 1);
        polylinePath.getPoints().add(lastPoint.y * 40 + 20.0);
        polylinePath.getPoints().add(lastPoint.x * 40 + 60.0);
    }
    private void findPath() {
        if (!path.isEmpty())
            throw new IllegalStateException("Map already has a path calculated.");
        int[][] discovered = new int[tiles.length][tiles[0].length];
        if (!depthFirstSearch(discovered, startX, startY, endX, endY))
            throw new RuntimeException("Invalid map, does not contain a path");
    }
    private boolean depthFirstSearch(int[][] discovered, int x, int y, int endX, int endY) {
        discovered[x][y] = 2;
        if (x == endX && y == endY) {
            path.push(new Point(x, y));
            return true;
        }
        for (int i = 0; i < X_DIRECTIONS.length; i++) {
            int newX = x + X_DIRECTIONS[i];
            int newY = y + Y_DIRECTIONS[i];
            if (!isValidCell(discovered, newX, newY))
                continue;
            path.push(new Point(x, y));
            if (depthFirstSearch(discovered, newX, newY, endX, endY))
                return true;
            path.pop();
        }
        return false;
    }
    private boolean isValidCell(int[][] discovered, int x, int y) {
        return x >= 0 && y >= 0 && x < tiles.length && y < tiles[x].length && tiles[x][y].isPath()
                && discovered[x][y] == 0;
    }
    private void scaleTiles(double value) {
        for (Tile[] tileRow : tiles) {
            for (Tile tile : tileRow) {
                tile.getImageView().setScaleX(value);
                tile.getImageView().setScaleY(value);
            }
        }
    }
}