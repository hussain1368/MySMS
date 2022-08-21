package com.kabulbits.shoqa.gui;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.swing.JPanel;

import com.kabulbits.shoqa.db.ChartData;
import com.kabulbits.shoqa.db.Data;
import com.kabulbits.shoqa.util.Dic;

public class ChartsPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	private ChartData data;
	JFXPanel fxPanel;

	public ChartsPanel(){
		data = new ChartData();
		setLayout(new BorderLayout());
		fxPanel = new JFXPanel();
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				GUI();
			}
		});
		add(fxPanel, BorderLayout.CENTER);
	}
	
	private void GUI()
	{
		Accordion accordion = new Accordion();
		accordion.setStyle("-fx-font-family:arial;-fx-font-weight:bold;");
		accordion.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

		TitledPane pane = new TitledPane(Dic.w("educ_year_students_count"), new StudStat());
		accordion.getPanes().add(pane);
		accordion.setExpandedPane(pane);
		accordion.getPanes().add(new TitledPane(Dic.w("students_count_by_year"), new StudCount()));
		accordion.getPanes().add(new TitledPane(Dic.w("marks_statistics"), new MarksPane()));
		accordion.getPanes().add(new TitledPane(Dic.w("employees_statistics"), new EmpPane()));
		fxPanel.setScene(new Scene(accordion));
	}
	
	class StudStat extends HBox{
		
		public StudStat(){
			final CategoryAxis xAxis = new CategoryAxis();
			final NumberAxis yAxis = new NumberAxis();
			
			final BarChart<String,Number> bar = new BarChart<String,Number>(xAxis,yAxis);
			
			bar.setTitle(Dic.w("students_count_by_grade"));
			
			XYChart.Series<String,Number> series = new XYChart.Series<String, Number>();
			bar.getData().add(series);
			
			int count [] = data.studentsByGrade();
			int grade = 1;
			for(int num : count){
				if(num != 0){
					XYChart.Data<String,Number> data = new XYChart.Data<String,Number>(String.valueOf(grade), num);
					series.getData().add(data);
					Tooltip.install(data.getNode(), new Tooltip(String.valueOf(num)));
				}
				grade++;
			}
			bar.setLegendVisible(false);
			bar.setPrefWidth(800);
			
			int m = data.studentsByGender("m");
			int f = data.studentsByGender("f");
			
			PieChart.Data male = new PieChart.Data(Dic.w("male"), m);
			PieChart.Data female = new PieChart.Data(Dic.w("female"), f);
			
			final PieChart pie = new PieChart();
			pie.getData().addAll(male, female);
			pie.setLabelsVisible(false);
			
			Tooltip.install(male.getNode(), new Tooltip(String.valueOf(m)));
			Tooltip.install(female.getNode(), new Tooltip(String.valueOf(f)));
			this.getChildren().addAll(pie, bar);
		}
	}
	
	private class StudCount extends HBox{
		
		public StudCount(){
			
			int lower = Data.EDUC_YEAR - 4;
			int upper = Data.EDUC_YEAR;
			final NumberAxis xAxis = new NumberAxis(lower, upper, 1);
	        final NumberAxis yAxis = new NumberAxis();
	        
	        final LineChart<Number, Number> line = new LineChart<Number, Number>(xAxis,yAxis);
	                
	        XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
	        XYChart.Series<Number, Number> series2 = new XYChart.Series<Number, Number>();
	        XYChart.Series<Number, Number> series3 = new XYChart.Series<Number, Number>();
	        
	        line.getData().add(series1);
	        line.getData().add(series2);
	        line.getData().add(series3);
	        
	        series1.setName(Dic.w("students"));
	        series2.setName(Dic.w("new_entry"));
	        series3.setName(Dic.w("graduated"));
	        
	        int [][] count = data.studentsByYear();
	        
	        for(int i=0; i<5; i++)
	        {
	        	XYChart.Data<Number, Number> data1 = new XYChart.Data<Number, Number>(count[i][0], count[i][1]);
	        	XYChart.Data<Number, Number> data2 = new XYChart.Data<Number, Number>(count[i][0], count[i][2]);
	        	XYChart.Data<Number, Number> data3 = new XYChart.Data<Number, Number>(count[i][0], count[i][3]);
	        	
	        	series1.getData().add(data1);
	        	series2.getData().add(data2);
        		series3.getData().add(data3);
        		
        		Tooltip.install(data1.getNode(), new Tooltip(String.valueOf(count[i][1])));
        		Tooltip.install(data2.getNode(), new Tooltip(String.valueOf(count[i][2])));
        		Tooltip.install(data3.getNode(), new Tooltip(String.valueOf(count[i][3])));
	        }
	        line.setPrefWidth(900);
	        this.getChildren().add(line);
		}
	}
	
	private class MarksPane extends ScrollPane{
		
		private int EVENTUAL = 1;
		private int FAILED = 2;
		
		public MarksPane(){
			
			final CategoryAxis xAxis = new CategoryAxis();
	        final NumberAxis yAxis = new NumberAxis();
	        final BarChart<String,Number> bar = new BarChart<String,Number>(xAxis,yAxis);
	        
	        String levels [] = {"1 - 3", "4 - 6", "7 - 12"};
	        String names [] = {"failed", "eventual", "passed", "excused"};
	        
	        for(int i=0; i<4; i++){
	        	XYChart.Series<String,Number> s = new XYChart.Series<String, Number>();
	        	for(int j=0; j<3; j++){
	        		XYChart.Data<String, Number> d = new XYChart.Data<String, Number>(levels[j], 0);
	        		s.getData().add(d);
	        	}
	        	s.setName(Dic.w(names[i]));
	        	bar.getData().add(s);
	        }
			
			final int midAvgs [][] = { {0,10}, {10,30}, {30,40} };
			final int finAvgs [][] = { {0,60}, {60,80}, {80,100} };

			final PieChart pie = new PieChart();
			pie.setLabelsVisible(false);
			pie.setTitle(Dic.w("marks_average"));
			
			for(int i=0; i<3; i++){
				pie.getData().add(new PieChart.Data("0", 0));
			}
			
			HBox hbox = new HBox();
			hbox.getChildren().addAll(bar, pie);
			FlowPane flow = new FlowPane(15, 0);
			flow.setAlignment(Pos.BASELINE_CENTER);
			
			RadioButton midterm, finall;
			midterm = new RadioButton(Dic.w("half_mark"));
			finall = new RadioButton(Dic.w("final_mark"));
			midterm.setUserData(1);
			finall.setUserData(2);
			
			final ToggleGroup toggle = new ToggleGroup();
			midterm.setToggleGroup(toggle);
			finall.setToggleGroup(toggle);
			
			flow.getChildren().addAll(midterm, finall);
			VBox vbox = new VBox();
			vbox.getChildren().addAll(hbox, flow);
			vbox.setPrefSize(800, 250);
			this.setContent(vbox);
			
			toggle.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
				@Override
				public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) 
				{
					int season = (int) newValue.getUserData();
					int grades [][] = { {1, 3}, {4, 6}, {7, 12} };
					for(int i=0; i<3; i++)
					{
						int down = grades[i][0];
						int up = grades[i][1];
						int passed = data.passedCount(season, down, up);
						int excuseds = data.excusedCount(season, down, up);
						
						bar.getData().get(2).getData().get(i).setYValue(passed);
						bar.getData().get(3).getData().get(i).setYValue(excuseds);
						
						Tooltip.install(bar.getData().get(2).getData().get(i).getNode(), new Tooltip(String.valueOf(passed)));
						Tooltip.install(bar.getData().get(3).getData().get(i).getNode(), new Tooltip(String.valueOf(excuseds)));
					}
					if(season == 1)
					{
						for(int i=0; i<3; i++)
						{
							int down = grades[i][0];
							int up = grades[i][1];
							int fails = data.midtermFailCount(down, up);
							
							bar.getData().get(0).getData().get(i).setYValue(fails);
							bar.getData().get(1).getData().get(i).setYValue(0);
							
							Tooltip.install(bar.getData().get(0).getData().get(i).getNode(), new Tooltip(String.valueOf(fails)));
							Tooltip.install(bar.getData().get(1).getData().get(i).getNode(), new Tooltip(String.valueOf(0)));
						}
					}
					else if(season == 2)
					{
						for(int i=0; i<3; i++)
						{
							int fails = data.failOrEventualCount(grades[i][0], grades[i][1], FAILED);
							int eventuals = data.failOrEventualCount(grades[i][0], grades[i][1], EVENTUAL);
							
							bar.getData().get(0).getData().get(i).setYValue(fails);
							bar.getData().get(1).getData().get(i).setYValue(eventuals);
							
							Tooltip.install(bar.getData().get(0).getData().get(i).getNode(), new Tooltip(String.valueOf(fails)));
							Tooltip.install(bar.getData().get(1).getData().get(i).getNode(), new Tooltip(String.valueOf(eventuals)));
						}
					}
					int avgs [][] = season == 1 ? midAvgs : finAvgs;
					for(int i=0; i<3; i++)
					{
						int down = avgs[i][0];
						int up = avgs[i][1];
						int byAvg = data.countByAvg(season, down, up);
						String lab = String.format("%d - %d", down, up);
						
						pie.getData().get(i).setPieValue(byAvg);
						pie.getData().get(i).setName(lab);
						Tooltip.install(pie.getData().get(i).getNode(), new Tooltip(String.valueOf(byAvg)));
					}
				}
			});
		}
	}
	
	private class EmpPane extends HBox{
		
		public EmpPane(){
			
			Map<Integer, Integer> info = data.employeesByType();
			
			PieChart.Data d1 = new PieChart.Data(Dic.w("instructive"), info.get(1));
			PieChart.Data d2 = new PieChart.Data(Dic.w("administrative"), info.get(2));
			PieChart.Data d3 = new PieChart.Data(Dic.w("services"), info.get(3));

			final PieChart pie = new PieChart();
			pie.getData().addAll(d1, d2, d3);
			pie.setLabelsVisible(false);
			
			Tooltip.install(d1.getNode(), new Tooltip(String.valueOf(info.get(1))));
			Tooltip.install(d2.getNode(), new Tooltip(String.valueOf(info.get(2))));
			Tooltip.install(d3.getNode(), new Tooltip(String.valueOf(info.get(3))));
			
			final NumberAxis xAxis = new NumberAxis(0, 0, 1);
	        final NumberAxis yAxis = new NumberAxis();
	        
	        final LineChart<Number, Number> line = new LineChart<Number, Number>(xAxis,yAxis);
	        XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
	        line.getData().add(series);
	        
	        Map<Integer, Integer> count = data.employeesByYear();
	        Set<Integer> years = count.keySet();
	        
	        int min = Collections.min(years);
	        int max = Collections.max(years);
	        xAxis.setLowerBound(min);
	        xAxis.setUpperBound(max);
	        
	        for(Object key : years){
	        	XYChart.Data<Number, Number> node = new XYChart.Data<Number, Number>((int) key, count.get(key));
	        	series.getData().add(node);
	        	Tooltip.install(node.getNode(), new Tooltip(String.valueOf(count.get(key))));
	        }
	        line.setTitle(Dic.w("employees_by_employ_year"));
	        line.setLegendVisible(false);
	        line.setPrefWidth(800);
	        
	        this.getChildren().addAll(pie, line);
		}
	}
	@Override
	protected void finalize() throws Throwable{
		if(data != null){
			data.closeConn();
		}
		super.finalize();
	}
}






























