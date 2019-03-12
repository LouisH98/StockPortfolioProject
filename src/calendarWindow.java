//Created: 10/02/2019 by Henry Lewis
//Last Modified: 19/02/2019 by Henry Lewis

//---[ Class Imports ]---
import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.HashMap;

//---[ Main Class ]---
public class calendarWindow extends JFrame {
    //---[ Calendar Variables ]---
    // Hold day being viewed/ selected on calendar, and current day so cannot exceed.
    private int day = 10, dayLimit = 30;
    private int month = 2, monthLimit = 12;
    private int year = 2018, yearLimit = 2019;
    private gui refGUI;
    HashMap<Integer, Integer> daysInMonth = new HashMap<>();

    //---[ Frame Elements ]---
    private JPanel dateSelector, buttonPanel;
    private JSpinner daySpinner, monthSpinner, yearSpinner;
    private JButton select, today;

    public calendarWindow() {
        //---[ Class Constructor ]---
        // Set date so calendar highlights currently viewed day
        // Set limits to the current day so cannot view a day in the future (no stock information)

        // Spinner Data
        SpinnerModel yearOptions = new SpinnerNumberModel(year, yearLimit - 5, yearLimit, 1);
        SpinnerModel monthOptions = new SpinnerNumberModel(month, 1, 12, 1);
        SpinnerModel dayOptions = new SpinnerNumberModel(day, 1, 31, 1);

        // Days in Month map -> used for input verification
        daysInMonth.put(1, 31);
        daysInMonth.put(2, 29);
        daysInMonth.put(3, 31);
        daysInMonth.put(4, 30);
        daysInMonth.put(5, 31);
        daysInMonth.put(6, 30);
        daysInMonth.put(7, 31);
        daysInMonth.put(8, 31);
        daysInMonth.put(9, 30);
        daysInMonth.put(10, 31);
        daysInMonth.put(11, 30);
        daysInMonth.put(12, 31);


        //---[ Frame Panels ]---
        dateSelector = new JPanel();
        dateSelector.setLayout(new BorderLayout());
        dateSelector.setPreferredSize(new Dimension(210, 50));
        this.add(dateSelector, BorderLayout.NORTH);
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.setPreferredSize(new Dimension(210, 50));
        this.add(buttonPanel, BorderLayout.SOUTH);

        //---[ Declare Frame Information ]---
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.setMinimumSize(new Dimension(210, 100));
        this.setTitle("Calendar");
        this.pack();
        this.setLocationRelativeTo(null);

        //---[ Spinner Boxes ]---
        daySpinner = new JSpinner(dayOptions);
        daySpinner.setPreferredSize(new Dimension(70, 30));
        dateSelector.add(daySpinner, BorderLayout.WEST);
        monthSpinner = new JSpinner(monthOptions);
        monthSpinner.setPreferredSize(new Dimension (70, 30));
        dateSelector.add(monthSpinner, BorderLayout.CENTER);
        yearSpinner = new JSpinner(yearOptions);
        yearSpinner.setPreferredSize(new Dimension(70, 30));
        dateSelector.add(yearSpinner, BorderLayout.EAST);

        //---[ Buttons ]---
        today = new JButton("Today");
        today.setPreferredSize(new Dimension(10,30));
        dateSelector.add(today, BorderLayout.SOUTH);
        select = new JButton();
        select.setText("Select");
        buttonPanel.add(select);
        /*
        additional frame size information goes here i.e. min/max dimensions
         */

        //---[ Action Listeners ]---
        // Button Listener
        select.addActionListener(e -> {
            // Set viewed day of the main window with whatever is being looked at.
            boolean dateAllowed = refGUI.updateViewedDate(day, month-1, year);
            if(dateAllowed){
                this.dispose();
            }
            else{
                JOptionPane.showMessageDialog(this, "Date not valid, please try again");
            }
        });

        today.addActionListener(e -> setToday());

        // Spinner Listener
        daySpinner.addChangeListener(e -> {
            day = (Integer) daySpinner.getValue();
        });

        monthSpinner.addChangeListener(e -> {
            month = (Integer) monthSpinner.getValue();
        });

        yearSpinner.addChangeListener(e -> {
            year = (Integer) yearSpinner.getValue();
        });
    }

    private void setToday(){
        Calendar today = Calendar.getInstance();
        daySpinner.setValue(today.get(Calendar.DAY_OF_MONTH));
        monthSpinner.setValue(today.get(Calendar.MONTH)+1);
        yearSpinner.setValue(today.get(Calendar.YEAR));
    }

    public void getDays(gui window) {
        // Set variables after calendar window constructed
        yearLimit = year = window.viewedYear;
        yearSpinner.setValue(year);
        monthLimit = month = window.viewedMonth;
        monthSpinner.setValue(month + 1);
        dayLimit = day = window.viewedDay;
        daySpinner.setValue(day);
        refGUI = window;
    }

    public static void main(String[] args) {
        // Test main for calendar window
        calendarWindow testCalendar = new calendarWindow();
        testCalendar.setVisible(true);
    }
}
