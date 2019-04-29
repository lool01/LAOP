package org.lrima.laop.simulation;

import javafx.stage.Stage;
import org.lrima.laop.core.LAOP;
import org.lrima.laop.network.LearningAlgorithm;
import org.lrima.laop.physic.CarControls;
import org.lrima.laop.settings.LockedSetting;
import org.lrima.laop.settings.Scope;
import org.lrima.laop.settings.Settings;
import org.lrima.laop.simulation.buffer.SimulationBuffer;
import org.lrima.laop.simulation.data.AlgorithmData;
import org.lrima.laop.utils.Actions.Action;
import org.lrima.laop.utils.Console;

import java.util.ArrayList;

public class LearningEngine implements Runnable{
    public static double DELTA_T = 0.05;
    private SimulationBuffer simulationBuffer;
    private Settings settings;

    private int batchCount;

    ArrayList<Action<LearningEngine>> onBatchStarted;
    ArrayList<Action<LearningEngine>> onEnd;

    private LearningAlgorithm learningAlgorithm;
    private Environnement environnement;
    private AlgorithmData learningData;
    private AlgorithmData trainedData;
    private Stage mainScene;

    private Thread currentThread;
    private final Object lock = new Object();
    private SimulationBuffer displayBuffer;

    private boolean alive;
    private boolean pause;
    private boolean paused = false;

    public LearningEngine(SimulationBuffer simulationBuffer, Settings settings) {
        this.simulationBuffer = simulationBuffer;
        displayBuffer = new SimulationBuffer();
        this.settings = settings;
        this.batchCount = 0;

        this.onBatchStarted = new ArrayList<>();
        this.onEnd = new ArrayList<>();
        this.learningData = new AlgorithmData();
        this.trainedData = new AlgorithmData();
    }

    public void start(){
        currentThread = new Thread(this);
        currentThread.start();
    }

    @Override
    public void run() {
        synchronized (lock){
            runn();
        }
    }

    public void runn(){
        this.environnement = generateEnvironnement();
        this.environnement.init(this);

        LearningAlgorithm[] trained = new LearningAlgorithm[this.settings.getLocalScopeKeys().size()];

        //train
        for (this.batchCount = 0; batchCount < this.settings.getLocalScopeKeys().size(); batchCount++) {
            alive = true;
            Console.info("Batch %s started", this.getBatchCount() + 1);
            this.onBatchStarted.forEach(b -> b.handle(this));

            learningAlgorithm = generateLearningAlgorithm();
            learningAlgorithm.train(environnement, this);


            trained[batchCount] = learningAlgorithm;


        }

        //TODO : faire le cas ou c'est pas un multi

        environnement = generateEnvironnement();
        environnement.init(this);

        if(environnement instanceof MultiAgentEnvironnement){
            int episode = 0;
            while(episode < 100){
                ArrayList<Agent> agents = ((MultiAgentEnvironnement) environnement).reset(trained.length);
                while (!environnement.isFinished()){
                    ArrayList<CarControls> carControls = new ArrayList<>();
                    for (int i = 0; i < trained.length; i++) {
                        carControls.add(trained[i].test(agents.get(i)));
                    }
                    agents = ((MultiAgentEnvironnement) environnement).step(carControls);

                    environnement.render();
                    System.out.println("haha");

                }
                for (int i = 0; i < trained.length; i++) {
                    trainedData.put(this.settings.getLocalScopeKeys().get(i), agents.get(i).reward);
                }
                episode++;
            }
        }
        else
            throw new RuntimeException("Do not support sigle environnemnt yet");

        onEnd.forEach((a) -> a.handle(this));
        learningData.toCsv("learning");
        trainedData.toCsv("training");
    }

    private Environnement generateEnvironnement() {
        Class<? extends Environnement> environnementClass = (Class<? extends Environnement>) settings.get(Settings.GLOBAL_SCOPE, LAOP.KEY_ENVIRONNEMENT_CLASS);

        try {
            return environnementClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        return new BetterEnvironnement();
    }

    public void evaluate(LearningAlgorithm learningAlgorithm){
        if(pause){
            synchronized (lock){
                try {
                    paused = true;
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        int MAX = 10;
        int episode = 0;
        Agent agent = this.environnement.reset();
        double sum = 0;
        while(episode < MAX){
            while(!this.environnement.isFinished()) {
                agent = this.environnement.step(learningAlgorithm.test(agent));
                environnement.render();
            }
            sum += agent.reward;
            episode++;
            agent = this.environnement.reset();
        }

        learningData.put(getCurrentScopeName(), sum/MAX);
    }


    public SimulationBuffer getBuffer() {
        return simulationBuffer;
    }

    LearningAlgorithm generateLearningAlgorithm() {
        Class<? extends LearningAlgorithm> learningClass = (Class<? extends LearningAlgorithm>) settings.get(this.getCurrentScopeName(), LAOP.KEY_LEARNING_CLASS);

        try {
            return learningClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void pause(){
        this.pause = true;
    }

    public void resume(){
        if(paused){
            synchronized (lock){
                this.pause = false;
                lock.notifyAll();
            }
            paused = false;
        }
    }

    public void setOnBatchStarted(Action<LearningEngine> onBatchFinished) {
        this.onBatchStarted.add(onBatchFinished);
    }

    public void setOnEnd(Action<LearningEngine> onEnd) {
        this.onEnd.add(onEnd);
    }

    public int getBatchCount() {
        return batchCount;
    }

    /**
     * Used to parse the listeners to the algorithms that need it (need key listeners)
     * @param mainScene the main JAVAFX scene
     */
    public void setMainScene(Stage mainScene) {
        this.mainScene = mainScene;
    }

    public LockedSetting getSettings() {
        return this.settings.lock(this.getCurrentScopeName());
    }

    public Environnement getEnvironnement() {
        return this.environnement;
    }

    private String getCurrentScopeName() {
    	return this.settings.getLocalScopeKeys().get(this.batchCount);
    }

    public Stage getMainScene() {
        return mainScene;
    }

    public LearningAlgorithm getCurrentLearning() {
        return learningAlgorithm;
    }

    public SimulationBuffer getDisplayBuffer() {
        return displayBuffer;
    }

    public boolean whileButtonNotPressed() {
        return alive;
    }

    public void nextAlgorithm(){
        alive = false;
    }

    public AlgorithmData getTrainingData() {
        return trainedData;
    }

    public AlgorithmData getLearningData() {
        return learningData;
    }
}
