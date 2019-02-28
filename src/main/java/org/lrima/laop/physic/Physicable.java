package org.lrima.laop.physic;

import org.lrima.laop.graphics.AffineTransformation;
import org.lrima.laop.math.Vector3d;

import java.awt.*;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class that can be simulated physicaly
 * @author Clement Bisaillon
 */
public abstract class Physicable {
    protected Vector3d position;
    private Vector3d velocity;
    private double mass;
    protected ArrayList<Vector3d> forces;
    protected ArrayList<Vector3d> angularForces;
    protected AffineTransformation transformation;
    protected ArrayList<Physicable> subObjects;
    protected double rotation;

    //Used to disable collision detection right after the collision happened
    protected long stopCheckingCollisionAt;
    private final int MINIMUM_TIME_BEFORE_COLLISION_AGAIN = 100;

    private Physicable(){
        this.position = Vector3d.origin;
        this.velocity = Vector3d.origin;
        this.mass = 0;
        this.rotation = 0;
        this.forces = new ArrayList<>();
        this.subObjects = new ArrayList<>();
    }

    public Physicable(Vector3d position, double mass){
        this();
        this.position = position;
        this.mass = mass;
    }

    public Physicable(double mass){
        this(Vector3d.origin, mass);
    }

    /**
     * Add a new force to the object
     * @param force - The force to add
     */
    public void addForce(Vector3d force){
        this.forces.add(force);
    }

    /**
     * Add a new velocity to the object
     * @param velocity - the velocity to add
     */
    public void addVelocity(Vector3d velocity) {this.velocity.add(velocity);}

    /**
     * Set the position of the physicable object
     * @param position - the new position
     */
    public void setPosition(Vector3d position){
        this.position = position.clone();
    }

    /**
     * Get the actual position of the physicable object
     * @return the position of the object
     */
    public Vector3d getPosition(){
        return this.position;
    }

    /**
     * Get all the forces applied to the object
     * @return the forces applied to the object
     */
    public ArrayList<Vector3d> getForces(){
        return this.forces;
    }

    /**
     * Get the resulting force from all the forces on the object
     * @return the resulting force
     */
    public Vector3d getSumForces(){
        Vector3d sum = Vector3d.origin;

        for(Vector3d force : this.forces){
            sum = sum.add(force);
        }

        //Get the force of the children
        for(Physicable child : this.getSubObjects()){
            sum = sum.add(child.getSumForces());
        }

        return sum;
    }

    public Vector3d getSumAngularForces(){
        Vector3d sum = Vector3d.origin;

        for(Vector3d force : this.angularForces){
            sum = sum.add(force);
        }

        //Get the force of the children
        for(Physicable child : this.getSubObjects()){
            sum = sum.add(child.getSumAngularForces());
        }

        return sum;
    }


    /**
     * Calculates and set the position of the object and all its children depending on
     * the sum of the forces applied to them
     */
    protected void nextStep(){
        Vector3d sumOfForces = this.getSumForces();
        Vector3d acceleration = sumOfForces.multiply(1.0 / this.mass);
        this.velocity = this.velocity.add(acceleration.multiply(PhysicEngine.DELTA_T));
        this.position = this.position.add(this.velocity.multiply(PhysicEngine.DELTA_T));

        //Make all sub-objects move with the parent
        for(Physicable childObject : this.getSubObjects()) {
            childObject.setVelocity(this.velocity);
            childObject.nextStep();
        }

    }

    /**
     * Check if the object can collide. It is necessary to stop reacting to collisions for a small
     * amount of time after a collision occurred or else it would collide indefinitely.
     * @return true if the object can react to collisions, false otherwise
     */
    protected boolean canCollide(){
        return (System.currentTimeMillis() - this.stopCheckingCollisionAt > this.MINIMUM_TIME_BEFORE_COLLISION_AGAIN);
    }

    /**
     * Removes all the forces applied to this object
     */
    protected void resetForces(){
        this.forces = new ArrayList<>();
    }

    /**
     * Resets the velocity of the object
     */
    protected void resetVelocity() {this.velocity = Vector3d.origin;}

    /**
     * Get the area of the object to check for collisions
     * @return the area of the object
     */
    public Area getArea(){
        Area areaTotal = new Area();
        for(Physicable object : this.getAllObjects()){
            areaTotal.add(new Area(object.getShape()));
        }

        return areaTotal;
    }

    /**
     * Get the shape of the object
     * @return the shape of the object
     */
    public abstract Shape getShape();

    /**
     * Defines what happens when a collision occurs
     * @param object the object colliding with this object
     */
    public abstract void collideWith(Physicable object);

    /**
     * Add a physicable object to the array of child objects
     * @param object the object to add
     */
    protected void addSubObject(Physicable object){
        this.subObjects.add(object);
    }

    protected ArrayList<Physicable> getSubObjects(){
        return this.subObjects;
    }

    /**
     * Add a list of physicable objects to the array of child objects
     * @param objects the objects to add
     */
    protected void addSubObject(Physicable ... objects){
        this.subObjects.addAll(Arrays.asList(objects));
    }

    /**
     * Get this object with all its child physicable
     * @return all the objects making this physicable
     */
    public Physicable[] getAllObjects(){
        Physicable[] objects = new Physicable[this.subObjects.size() + 1]; //Plus one to count this object
        objects[0] = this;

        //Add all the sub objects
        for(int i = 0 ; i < this.subObjects.size() ; i++){
            objects[i + 1] = this.subObjects.get(i);
        }

        return objects;
    }

    public Vector3d getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3d velocity) {
        this.velocity = velocity;
    }

    /**
     * Set the rotation of the object and change the rotation of all the forces
     * @param rotation the rotation to apply on the object
     */
    public void rotate(double rotation) {
        this.rotation += rotation;
        //Rotate this object and each of its children
        for(Physicable object : this.getAllObjects()){
            for(Vector3d vector : object.getForces()){
                //Rotate the vector
                double newX = vector.getX() * Math.cos(rotation) - vector.getY() * Math.sin(rotation);
                double newY = vector.getX() * Math.sin(rotation) + vector.getY() * Math.cos(rotation);

                vector.setX(newX);
                vector.setY(newY);
            }
        }
    }

    public double getRotation(){
        return this.rotation;
    }

    public Vector3d getCenter(){
        return Vector3d.origin;
    }

    /**
     * Get the weight vector of this object based on the gravity of the physic engine
     * @return the weight of the object
     */
    public Vector3d getWeight(){
        return new Vector3d(0, 0, -(this.mass * PhysicEngine.GRAVITY));
    }
}
