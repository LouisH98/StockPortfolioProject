/*
ChartPanel class by Louis H

 */

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.XYStyler;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.ButtonGroup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/*
    ChartPanel class. This class extends from JPanel and creates
    a graph that shows the stocks closing price for the last 5 years to 1 week (user selectable)
    It also create checkboxes so that the user can customise which stocks are graphed.
    The user can save the graph to a file.
 */

public class ChartPanel extends JPanel {
    private XYChart stockChart;
    private user guiUser;
    private JPanel topPanel = new JPanel();
    private JPanel bottomPanel = new JPanel();
    private JPanel chartPanel;
    private List<JCheckBox> stockBox;
    private JButton saveButton;
    private JFileChooser fileChooser = new JFileChooser();

    //Map of the last 5 years of X/Y coords. To get 'NFLX' stock dates: dateMap.get("NFLX"); etc...
    private HashMap<String, ArrayList<Date>> dateMap = new HashMap<>();
    private HashMap<String, ArrayList<Float>> valueMap = new HashMap<>();

    //filtered HashMaps for dates.
    private HashMap<String, ArrayList<Date>> filteredDateMap = new HashMap<>(); //on the X-Axis
    private HashMap<String, ArrayList<Float>> filteredValueMap = new HashMap<>(); //on the Y-Axis

    /*
    Constructor. Creating initial panels and layout.
     */
    public ChartPanel(user guiUser) {
        this.guiUser = guiUser;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        stockBox = new ArrayList<>();
        stockChart = new XYChartBuilder().title("5 Year Stock Chart").xAxisTitle("Time (Years)").yAxisTitle("Price ($)").build();

        XYStyler chartStyle = stockChart.getStyler();
        chartStyle.setChartBackgroundColor(this.getBackground());
        chartStyle.setMarkerSize(0);
        chartStyle.setToolTipsEnabled(true);
        chartStyle.setToolTipType(Styler.ToolTipType.yLabels);
        chartStyle.setBaseFont(this.getFont());
        chartStyle.setDecimalPattern("$###.##");

        //RadioButton group.
        RadioHandler myHandler = new RadioHandler();
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton all = new JRadioButton("5 Year");
        all.setActionCommand("5y");
        all.addActionListener(myHandler);
        JRadioButton year = new JRadioButton("1 Year");
        year.setActionCommand("1y");
        year.addActionListener(myHandler);
        JRadioButton halfYear = new JRadioButton("6 Months");
        halfYear.addActionListener(myHandler);
        halfYear.setActionCommand("6m");
        JRadioButton month = new JRadioButton("1 Month");
        month.setActionCommand("1m");
        month.addActionListener(myHandler);
        JRadioButton week = new JRadioButton("1 Week");
        week.setActionCommand("1w");
        week.addActionListener(myHandler);
        buttonGroup.add(all);
        buttonGroup.add(year);
        buttonGroup.add(halfYear);
        buttonGroup.add(month);
        buttonGroup.add(week);
        all.setSelected(true);
        topPanel.add(all);
        topPanel.add(year);
        topPanel.add(halfYear);
        topPanel.add(month);
        topPanel.add(week);


        chartPanel = new XChartPanel<>(stockChart);
        this.add(topPanel, BorderLayout.NORTH);
        this.add(chartPanel, BorderLayout.NORTH);
        this.add(bottomPanel, BorderLayout.SOUTH);
        updateGUI();
    }


    /*
    This function is called when the date range is changed
     */
    private void updateChart() {
        for (String s : filteredDateMap.keySet()) {
            try {

                stockChart.updateXYSeries(s, filteredDateMap.get(s), filteredValueMap.get(s), null);

            } catch (IllegalArgumentException e) {
                //This error will be thrown if you try to change the date when none of the stock boxes are ticked
            }
        }
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    /*
    This function pulls stock data from the guiUser object and populates the maps with data.
    This is called from gui.java on stock addition and removal.
     */
    private void getChartInfo() {
        dateMap.clear();
        valueMap.clear();
        for (String s : guiUser.stockObjOwned.keySet()) {
            stock myStock = guiUser.stockObjOwned.get(s);
            ArrayList<StockInfo> info = myStock.getDatesAndValues();
            ArrayList<Date> dates = new ArrayList<>();
            ArrayList<Float> values = new ArrayList<>();
            for (StockInfo stockInfoObj : info) {
                dates.add(stockInfoObj.getDate());
                values.add(stockInfoObj.getValue());
            }
            dateMap.put(myStock.getName(), dates);
            valueMap.put(myStock.getName(), values);
        }
    }

    /*
    This function is to filter down the last 5 years of data into smaller amounts
     */
    private void setDateRange(Calendar radioDate) {
        //for every stock in the list - look at the date, and if its in the range, add it
        filteredValueMap.clear();
        filteredDateMap.clear();

        String[] stockNames = valueMap.keySet().toArray(new String[0]);

        /*
        for every stock
        look at their dates, and if the date is before the given date (selected by radio buttons)
        then add the date and values to the filtered maps - then update the chart

         */
        for (String name : stockNames) {
            ArrayList<Date> stockDates = dateMap.get(name);
            ArrayList<Float> stockValues = valueMap.get(name);

            ArrayList<Date> survivingDates = new ArrayList<>();
            ArrayList<Float> survivingValues = new ArrayList<>();

            Iterator<Date> dateItr = stockDates.iterator();
            Iterator<Float> valueItr = stockValues.iterator();
            while (dateItr.hasNext() && valueItr.hasNext()) {
                Calendar stockDate = Calendar.getInstance();
                stockDate.setTime(dateItr.next());
                Float stockValue = valueItr.next();
                if (radioDate.compareTo(stockDate) <= 0) {
                    survivingDates.add(stockDate.getTime());
                    survivingValues.add(stockValue);
                }
            }
            filteredDateMap.put(name, survivingDates);
            filteredValueMap.put(name, survivingValues);
        }
        updateChart();
    }

    /*
    This function is called when a checkbox is changed.
     */
    void setStock(String stock, boolean visible) {
        if (visible) {
            try {
                stockChart.addSeries(stock, filteredDateMap.get(stock), filteredValueMap.get(stock));
            } catch (IllegalArgumentException e) {
                System.out.println("Series already added to chart" + e);
            }
        } else {
            try {
                stockChart.removeSeries(stock);
            } catch (IllegalArgumentException e) {
                System.out.println("Trying to remove a stock from chart that doesn't exist.");
            }
        }
        updateChart();
    }

    /*
    Function to save the chart as a JPEG image in the user chosen location
     */
    private void saveChart() {
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        FileFilter filter = new FileNameExtensionFilter("JPEG File", "jpg");
        fileChooser.setFileFilter(filter);
        int userSaved = fileChooser.showSaveDialog(this);
        String saveDir = fileChooser.getSelectedFile() + ".png";

        //if the user has clicked save, then try to save, otherwise do nothing
        if (userSaved == 0) {
            try {
                BitmapEncoder.saveBitmapWithDPI(stockChart, saveDir, BitmapEncoder.BitmapFormat.PNG, 300);
            } catch (IOException e) {
                System.out.println("Failed to save file.");
            }
        }
    }

    /*
    Creates a checkbox for each stock.
    Calls getChartInfo to update internal stock map
     */
    public void updateGUI() {
        //clear the checkboxes and update with all stock owned
        bottomPanel.removeAll();
        stockBox.clear();

        Iterator<String> setItr = guiUser.stocksIDOwned.keySet().iterator();
        for (int i = 0; i < guiUser.stocksIDOwned.size(); i++) {
            JCheckBox stock = new JCheckBox(setItr.next().toUpperCase());
            stockBox.add(stock);
        }
        //add all the check boxes to the bottom panel
        for (JCheckBox box : stockBox) {
            box.addActionListener(new CheckBoxHandler());
            bottomPanel.add(box, BorderLayout.SOUTH);
        }

        //if there are no stocks, put some text at the bottom telling the user
        if (guiUser.stockObjOwned.size() == 0) {
            JLabel noStockLab = new JLabel("No stocks to graph.");
            noStockLab.setForeground(new Color(255, 50, 0));
            String currentFont = noStockLab.getFont().getName();
            noStockLab.setFont(new Font(currentFont, Font.BOLD, 15));
            bottomPanel.add(noStockLab);
        } else {
            saveButton = new JButton("Save Chart");
            saveButton.addActionListener(e -> saveChart());
            bottomPanel.add(saveButton, BorderLayout.EAST);
        }
        //now that the GUI is created, get the information for the chart
        getChartInfo();
        Calendar fiveYearsAgo = Calendar.getInstance();
        fiveYearsAgo.add(Calendar.YEAR, -5);
        setDateRange(fiveYearsAgo);
    }

    /*
    Checkbox handler.
    It gets the name of the checkbox and the (boolean) value and sends it to the setStock function
     */
    class CheckBoxHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBox cb = (JCheckBox) e.getSource();
            setStock(cb.getActionCommand(), cb.isSelected());
        }
    }

    /*
    Handler class for the Radio Buttons
     */
    class RadioHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Calendar wantedDate = Calendar.getInstance();
            switch (e.getActionCommand()) {
                case "5y":
                    stockChart.setTitle("5 Year Stock Chart");
                    stockChart.setXAxisTitle("Time (Years)");
                    wantedDate.add(Calendar.YEAR, -5);
                    break;
                case "1y":
                    stockChart.setTitle("1 Year Stock Chart");
                    stockChart.setXAxisTitle("Time (Months)");
                    wantedDate.add(Calendar.YEAR, -1);
                    break;
                case "6m":
                    stockChart.setTitle("6 Month Stock Chart");
                    stockChart.setXAxisTitle("Time (Months)");
                    wantedDate.add(Calendar.MONTH, -6);
                    break;
                case "1m":
                    stockChart.setTitle("1 Month Stock Chart");
                    stockChart.setXAxisTitle("Time (Weeks)");
                    wantedDate.add(Calendar.MONTH, -1);
                    break;
                case "1w":
                    stockChart.setTitle("1 Week Stock Chart");
                    stockChart.setXAxisTitle("Time (Days)");
                    wantedDate.add(Calendar.WEEK_OF_YEAR, -1);
                    break;
            }
            setDateRange(wantedDate);
        }
    }
}