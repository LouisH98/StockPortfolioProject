import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.XYStyler;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/*
Panel to graph the users total worth over time
 */
public class UserWorthChart extends JPanel {
    private XYChart netWorthChart;
    private user guiUser;
    private JPanel centrePanel, chartPanel;
    public UserWorthChart(user guiUser){
        //set up panel
        setLayout(new BorderLayout());

        //init objects
        this.guiUser = guiUser;
        centrePanel = new JPanel(new BorderLayout());

        netWorthChart = new XYChartBuilder().title("Graph of total asset worth (including bank account)").xAxisTitle("Time").yAxisTitle("Value ($)").build();

        //chart style
        XYStyler chartStyle = netWorthChart.getStyler();
        chartStyle.setChartBackgroundColor(this.getBackground());
        chartStyle.setToolTipsEnabled(true);
        chartStyle.setToolTipType(Styler.ToolTipType.xAndYLabels);
        chartStyle.setBaseFont(getFont());
        chartStyle.setDecimalPattern("$####,###,###.#");
        chartStyle.setDatePattern("dd-MM-yyyy");
        chartStyle.setMarkerSize(0);
        chartStyle.setLegendVisible(false);


        add(new JLabel("Please log in to see net worth graph!", SwingConstants.CENTER));


    }

    public void setLoggedIn() {
        this.removeAll();
        this.add(centrePanel, BorderLayout.CENTER);
        chartPanel = new XChartPanel<>(netWorthChart);
        centrePanel.add(chartPanel);
    }

    public void generateGraph(){
        ArrayList<Date> dates = new ArrayList<>();
        ArrayList<Double> values = new ArrayList<>();

        //calculate total stock worth over the last 5 years for each week
        Calendar dateChanger = Calendar.getInstance();
        dateChanger.add(Calendar.YEAR, -5);
        while (dateChanger.compareTo(Calendar.getInstance()) < 0) {
            int currentDay = dateChanger.get(Calendar.DAY_OF_MONTH);
            int currentMonth = dateChanger.get(Calendar.MONTH);
            int currentYear = dateChanger.get(Calendar.YEAR);
            double currentDayTotalPrice = 0;

            //go through every stock and get price on this day, then add it to the total day price
            for(stock stock : guiUser.stockObjOwned.values()){
                double stockPriceOnDay = stock.getPriceOnDay(currentDay, currentMonth, currentYear, false);
                stockPriceOnDay *= guiUser.stocksIDOwned.get(stock.getName());
                currentDayTotalPrice += stockPriceOnDay;
            }

            //add user worth for the day
            currentDayTotalPrice += guiUser.bank.returnAmount();

            //add the day values to the arraylists
            values.add(currentDayTotalPrice);
            dates.add(dateChanger.getTime());

            //go forward a week
            dateChanger.add(Calendar.DAY_OF_MONTH, 7);
        }

        //lists are populated. update the graph now
        updateChart(dates, values);

    }

    private boolean chartMade = false;
    private void updateChart(ArrayList<Date> dates, ArrayList<Double> values){
        if(!chartMade){
            netWorthChart.addSeries("netWorth", dates, values, null);
            chartMade = true;
        } else {
            netWorthChart.updateXYSeries("netWorth", dates, values, null);
        }
    }
}
