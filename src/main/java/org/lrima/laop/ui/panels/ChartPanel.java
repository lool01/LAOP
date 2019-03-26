package org.lrima.laop.ui.panels;

import org.lrima.laop.simulation.Simulation;
import org.lrima.laop.simulation.data.GenerationData;

import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Panel to show to progression of the current simulation.
 * This panel shows a chart displaying information about the average fitness
 * of the cars by the generation number
 * @author Clement Bisaillon
 */
public class ChartPanel extends HBox {
	private NumberAxis xAxis;
	private NumberAxis yAxis;
	private LineChart<Number, Number> chart;
	private int generationNumber;
	private double maxY;
	private double minY;
	
	private XYChart.Series<Number, Number> averageFitnessSerie;
	
	public ChartPanel(Simulation simulation) {
		this.generationNumber = 0;
		this.maxY = 0;
		this.minY = 0;
		
		this.setPadding(new Insets(10, 10, 10, 10));
		setStyle("-fx-background-color: rgb(255, 255, 255, 0.5)");
		
		this.setPrefHeight(200);
		HBox.setHgrow(this, Priority.ALWAYS);
		
		simulation.setOnGenerationFinish( (sim) -> {
			this.generationEnd(sim.getGenerationData());
		});
		simulation.setOnSimulationFinish((sim) -> {
			this.allGenerationEnd();
		});

		this.setupChart();
	}
	
	/**
	 * Creates the axis and configure the chart
	 */
	private void setupChart() {
		this.xAxis = new NumberAxis("Generation", 0, 0, 1);
		this.yAxis = new NumberAxis("Score", 0, 500, 100);
		this.chart = new LineChart<>(xAxis, yAxis);
		
//		this.chart.setTitle("Fitness score by generation");
//		this.chart.setCreateSymbols(false);
		this.chart.setLegendSide(Side.RIGHT);
		ChartPanel.setHgrow(chart, Priority.ALWAYS);

		//Average fitness serie
		this.averageFitnessSerie = new XYChart.Series<>();
		this.averageFitnessSerie.setName("Average fitness");
		this.chart.getData().add(this.averageFitnessSerie);
		
		this.getChildren().add(this.chart);
		
	}

	public void allGenerationEnd() {
		//Reset the series and the generation count
		this.generationNumber = 0;
		this.averageFitnessSerie.getData().clear();
		this.maxY = 0;
		this.minY = 0;
		
		this.yAxis.setLowerBound(this.minY);
		this.yAxis.setUpperBound(this.maxY);
		this.xAxis.setUpperBound(this.generationNumber);
	}

	public void generationEnd(GenerationData pastGeneration) {
		//Add new data to the series from the past generation
		double averageFitnessScore = pastGeneration.getAverageFitness();
		XYChart.Data<Number, Number> data = new XYChart.Data(this.generationNumber, averageFitnessScore);
		
		//Change the bounds of the chart
		if(averageFitnessScore > this.maxY) {
			this.maxY = averageFitnessScore;
		}
		else if(averageFitnessScore < this.minY) {
			this.minY = averageFitnessScore;
		}
		
		this.yAxis.setLowerBound(this.minY);
		this.yAxis.setUpperBound(this.maxY);
		this.xAxis.setUpperBound(this.generationNumber);
		
		//Add the data to the chart
		this.averageFitnessSerie.getData().add(data);
		
		this.generationNumber++;
	}
}