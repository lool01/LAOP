package org.lrima.laop.simulation;

import org.lrima.laop.network.carcontrollers.CarController;
import org.lrima.laop.physic.abstractObjects.AbstractCar;
import org.lrima.laop.simulation.map.AbstractMap;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface Environnement {
    <T extends CarController> ArrayList<T> evaluate(ArrayList<T> cars);
    void parallelEvaluation(Consumer<ArrayList<? extends CarController>> cars);
    boolean isFinished();

    void initialise(SimulationEngine simulationEngine);

    AbstractMap getMap();
    BiFunction<Environnement, AbstractCar, Double> getFitenessFunction();
}
