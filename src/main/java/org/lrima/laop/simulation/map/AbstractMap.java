package org.lrima.laop.simulation.map;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
import org.lrima.laop.physic.staticobjects.FitnessWallObject;
import org.lrima.laop.physic.staticobjects.StaticLineObject;

/**
 * Object representing every kind of maps
 * @author Clement Bisaillon
 */
public abstract class AbstractMap {
	ArrayList<StaticLineObject> lines;
	Quadtree quadtree;

	/**
	 * Initiate a map by reseting its array of lines
	 */
	public AbstractMap() {
		this.lines = new ArrayList<>();
	}
	
	/**
	 * Converts the lines of the map to multiple areas containing the same type of lines
	 */
	public void bake(){
		if(lines.size() == 0){
		    quadtree = new Quadtree(0, 0, 0, 1, 1);
		    return;
        }

		float minx = lines.get(0).getX1();
		float miny = lines.get(0).getY1();
		float maxx = lines.get(0).getX1();
		float maxy = lines.get(0).getY1();

		for (StaticLineObject line : lines) {
			maxx = Math.max(maxx, line.getX1());
			maxx = Math.max(maxx, line.getX2());
			maxy = Math.max(maxy, line.getY1());
			maxy = Math.max(maxy, line.getY2());

			minx = Math.min(minx, line.getX1());
			minx = Math.min(minx, line.getX2());
			miny = Math.min(miny, line.getY1());
			miny = Math.min(miny, line.getY2());
		}

		quadtree = new Quadtree(0, minx, miny, maxx, maxy);
		for (StaticLineObject line : lines) {
			quadtree.insert(line);
		}
	}

	/**
	 * Tell the quadtree that a line collided with the map
	 * @param lineCollidable the line that collided with the map
	 */
	public void collide(LineCollidable lineCollidable){
		lineCollidable.bake();
		quadtree.collide(lineCollidable);
	}
	
	/**
     * @return the starting point of the cars
     */
	abstract public Point2D getStartPoint();
	
	/**
	 * Get the distance from a certain point and the start
	 * @param point the point
	 * @return the distance
	 */
	public double distanceFromStart(Point2D point) {
		return point.distance(getStartPoint());
	}

	/**
	 * Draw the map on the screen
	 * @param gc the graphical context to draw to
	 */
    public void draw(GraphicsContext gc){
		lines.forEach(line -> {
			line.draw(gc);
		});
	}
}
