package org.lrima.laop.simulation;

import javafx.scene.canvas.GraphicsContext;
import org.lrima.laop.physic.CarControls;
import org.lrima.laop.settings.LockedSetting;
import org.lrima.laop.ui.Drawable;

public interface Environnement extends Drawable {
//    <T extends CarController> ArrayList<T> evaluate(ArrayList<T> cars);
//    void parallelEvaluation(Consumer<ArrayList<? extends CarController>> cars);
    boolean isFinished();

    Agent reset();
    Agent step(CarControls carControls);

    void render();

    void init(SimulationEngine simulationEngine);

//    AbstractMap getMap();
//    BiFunction<Environnement, AbstractCar, Double> getFitenessFunction();
//
//    BatchData getBatchData();
//    void setFinished(boolean finished);
}
