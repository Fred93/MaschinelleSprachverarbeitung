package java_DOM_parcer;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class Grafic implements Runnable{
	private HashMap<String, Integer> allTokensBody = new HashMap<>();
	
	public Grafic(HashMap<String, Integer> allTokensBody){
	this.allTokensBody=allTokensBody;
		
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	

	 JPanel chartPanel = createChartPanel();
	 JFrame f = new JFrame();
	 f.setLayout(new BorderLayout());
	 f.setSize(1000, 500);
	 f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	 //add(chartPanel, BorderLayout.CENTER);
	 f.add(chartPanel, BorderLayout.CENTER);
	 f.setVisible(true);	
 

      
	 //chartPanel.setLocationRelativeTo(null);
	 
	/* Frame wnd = new Frame("Example1401");

      wnd.setSize(400,300);
      wnd.setVisible(true);*/
	 
}


private JPanel createChartPanel() {

	    String chartTitle = "Distribution of Tokens";
	    String categoryAxisLabel = "Tokens";
	    String valueAxisLabel = "Frequency";
	 
	    DefaultCategoryDataset dataset = createDatat(allTokensBody);
	 
	    JFreeChart chart = ChartFactory.createLineChart(chartTitle,
	            categoryAxisLabel, valueAxisLabel, dataset);
	 //chart.getXYPlot().getRangeAxis().setTickLabelsVisible(false);
	    return new ChartPanel(chart);
	
}
private DefaultCategoryDataset createDatat(HashMap<String, Integer> allTokensBody ){
	 DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	 for(Entry<String, Integer> entry : allTokensBody.entrySet()) {
			
		   String key = entry.getKey();
		   int value = entry.getValue();
		   dataset.addValue(value, "Values", key);
	 
 }
	 return  dataset;
}
	
}
