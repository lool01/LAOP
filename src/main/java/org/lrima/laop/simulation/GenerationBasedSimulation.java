package org.lrima.laop.simulation;

import org.lrima.laop.core.LAOP;
import org.lrima.laop.physic.PhysicEngine;
import org.lrima.laop.physic.abstractObjects.AbstractCar;
import org.lrima.laop.physic.concreteObjects.SimpleCar;
import org.lrima.laop.simulation.data.GenerationData;
import org.lrima.laop.simulation.sensors.ProximityLineSensor;
import org.lrima.laop.utils.Actions.Action;
import org.lrima.laop.utils.math.Vector2d;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * A simulation that can take the LearningAlgorithms that uses generation bases learning
 *
 * @author Léonard
 */
public class GenerationBasedSimulation extends Simulation{
    private int simulationCount;
    private int generationCount;

    ArrayList<Action<GenerationBasedSimulation>> onSimulationFinish;
    ArrayList<Action<GenerationBasedSimulation>> onGenerationFinish;

    private PhysicEngine physicEngine;

    private boolean autoRun;

    //temporary
    private final int NUMBER_OF_SENSORS = 2;
    //endtemporary

    /**
     * Creates a new Generation
     *
     * @param simulationEngine the simulation engine to refer to
     */
    public GenerationBasedSimulation(SimulationEngine simulationEngine){
        super(simulationEngine);

        this.onSimulationFinish = new ArrayList<>();
        this.onGenerationFinish = new ArrayList<>();
        autoRun = true;
    }

    /**
     * Starts the simulation
     *
     */
    @Override
    public void start() {
        this.nextGen();
    }

    /**
     * Confifure all the cars for the simulation
     * @return
     */
    private ArrayList<SimpleCar> configureCar(){
        //TODO ConfigureCars depending on settings and currentScope

        //Create the cars
        //Temporary
        ArrayList<SimpleCar> cars = new ArrayList<>();

        if(this.simulationEngine.getBuffer() != null) {
            for(int i = 0 ; i < 1 ; i++) {
                Point2D start = this.simulationEngine.getMap().getStartPoint();
                SimpleCar car = new SimpleCar(new Vector2d(start.getX(), start.getY()), this.simulationEngine.generateCurrentNetwork());

                double orientationIncrement = Math.PI / NUMBER_OF_SENSORS;
                //Create the sensors and assign them to the car
                for(int x = 0 ; x < this.NUMBER_OF_SENSORS ; x++) {
                    ProximityLineSensor sensor = new ProximityLineSensor(car, (x * orientationIncrement) + orientationIncrement/2);
                    car.addSensor(sensor);
                }

                cars.add(car);
            }
        }

        return cars;
    }

    /**
     * Precedes to next generation
     */
    public void nextGen(){
        simulateGeneration();
        incrementGeneration();
    }

    /**
     *
     * Increment the generation count and batch count. Calls the listener accordingly.
     */
    private void incrementGeneration(){
        //FIRE LISTENERS
        this.onGenerationFinish.forEach(simulationAction -> simulationAction.handle(this));
        generationCount ++;

        //CHECK IF GENERATION IS OUT OF BOUNDS
        if(generationCount > (int) this.simulationEngine.getSettings().get(LAOP.KEY_NUMBER_OF_GENERATIONS)){
            //FIRE LISTENERS
            this.onSimulationFinish.forEach(simulationAction -> simulationAction.handle(this));
            generationCount = 0;
            simulationCount++;

            //CHECK IF SIMULATION IS OUT OF BOUND
            if(simulationCount > (int) this.simulationEngine.getSettings().get(LAOP.KEY_NUMBER_OF_SIMULATION)){
                //FIRE END LISTENER
                this.end.forEach(e -> e.accept(this));
            }
        }
    }

    /**
     * Simulate a generation
     */
    private void simulateGeneration(){
        this.simulationEngine.getBuffer().clear();
        this.physicEngine = new PhysicEngine(this.simulationEngine.getBuffer(), this.simulationEngine.getMap());

        this.physicEngine.setWaitDeltaT(true);
        this.physicEngine.setFinishingConditions((list) -> {
            for (AbstractCar abstractCar : list) {
                if(!abstractCar.isDead()){
                    return false;
                }
            }
            return true;
        });
        this.physicEngine.getCars().addAll(configureCar());

        this.physicEngine.setOnPhysicEngineFinishOnce(engine -> {
            if(this.autoRun)
                this.nextGen();
        });
        this.physicEngine.start();
    }

    /**
     * Get the generation data of the current generation
     * Used to populate the chart panel
     * @author Clement Bisaillon
     * @return the generation data of the current generation
     */
    public GenerationData getGenerationData() {
        GenerationData data = new GenerationData(this.generationCount);

        return data;
    }

    public void setOnSimulationFinish(Action<GenerationBasedSimulation> onSimulationFinish) {
        this.onSimulationFinish.add(onSimulationFinish);
    }

    public void setOnGenerationFinish(Action<GenerationBasedSimulation> onGenerationFinish){
        this.onGenerationFinish.add(onGenerationFinish);
    }

    public int getGenerationCount() {
        return generationCount;
    }

    public int getSimulationCount() {
        return simulationCount;
    }

    //TODO
    /**
     *
     * @param autoRun false if the simulation should wait input before going to the next generation or going as soon as the generation is finished
     */
    public void setAutoRun(boolean autoRun) {
        this.autoRun = autoRun;
    }
}
