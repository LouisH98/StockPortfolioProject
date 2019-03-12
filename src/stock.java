//Created: 29/10/2018 by Henry Lewis
//Last Modified: 27/11/2018 by Henry Lewis

//---[ Class Imports ]---

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//class for transferring information to the chart class
 class StockInfo {
    private Date date;
    private float value;

    public StockInfo(Date date, float value) {
        this.date = date;
        this.value = value;
    }

    Date getDate() {
        return this.date;
    }

    float getValue() {
        return this.value;
    }
}

public class stock implements Serializable{
    //---[ Data for the Stock ]---
    public String label;
    transient public ArrayList<day> days = new ArrayList<>(); // List will hold the day objects
    private HashMap<String, String> attributes = new HashMap<>(); //holds info about the stock such as 'companyName', 'website'
    public Queue<Double> initialValues; //value of the stocks when bought - when a stock is bought the price is pushed on to the queue
    private double mostRecentPrice;
    private long lastTime;

    //for serialization
    private static final long serialVersionUID = 6529685098267757690L;

    // Class Constructor
    public stock(String prefix) throws IOException {
        lastTime = 0;
        mostRecentPrice = 0;


        //LinkedList implementation of a queue
        initialValues = new LinkedList<>();
        try {
            populate(prefix);
            this.label = prefix;
            loadInformation();
            System.out.println("Stock " + prefix + " populated");
        } catch (IOException e) {
            System.out.println("IO Error - could not retrieve file. (Stock)");
            throw new IOException();
        }
    }


    // class constructor for JUnit test
    public stock(){

    }


    /*
    Called when adding a stock, adds a price to the Queue to then be taken off later when selling stock to calculate profit.
     */
    public void addValueToQueue(Double value){
        initialValues.add(value);
    }

    /*
    Called when selling a stock, used to calculate profit.
     */
    public double getAndRemoveValue(){
        double value = 0;
        try{
            value = initialValues.remove();
        }
        catch (NoSuchElementException e){
            System.out.println("Problem removing price from Queue");
            e.printStackTrace();
        }
        return value;
    }

    public double getWorth() {
        //have a 5 second cache of the most recent price to stop API request for every stock
        if(System.currentTimeMillis()  >  lastTime + 5000){
            mostRecentPrice = getLivePrice();
            lastTime = System.currentTimeMillis();
            return mostRecentPrice;

        }
        else{
            return mostRecentPrice;
        }
    }

    public String getName() {
        return this.label.toUpperCase();
    }

    public day getToday() {
        day today = days.get(days.size() - 1); // Last item in array is most recent day
        return today;
    }

    //method for returning all dates and values in a format that the graph takes
    public ArrayList<StockInfo> getDatesAndValues() {
        ArrayList<StockInfo> dateList = new ArrayList<>();
        String pattern = "yyyy-MM-dd";

        //for every day in days, create a 'StockInfo' object with a Java Date object and the closing stock value
        for (day stockDay : days) {
            float value = stockDay.high;
            String strDate = stockDay.date;
            Date dateObj = new Date();

            try {
                DateFormat df = new SimpleDateFormat(pattern);
                dateObj = df.parse(strDate);
            } catch (ParseException e) {
                System.out.println("error parsing date into date object: " + e);
            }
            StockInfo info = new StockInfo(dateObj, value);
            dateList.add(info);
        }
        return dateList;
    }


    /*
    Used to get a number of attributes from the HashMap
    Attributes include (String):
        symbol : AAPL
        companyName : Apple Inc.
        website : http://www.apple.com
        description: Apple Inc is designs, manufactures and markets mobile communication etc...
     */
    public String getAttributes(String attribute){
        if(attributes.get(attribute) == null){
            return "Invalid Attribute";
        }
        else{
            return attributes.get(attribute);
        }
    }

    //returns stock closing price on specific day
    public double getPriceOnDay(int day, int month, int year, boolean alert){
        Calendar now = Calendar.getInstance(Locale.UK);
        Calendar givenDate = Calendar.getInstance(Locale.UK);
        givenDate.set(year, month, day);

        long smallestDist = Long.MAX_VALUE; //to keep track of closest day

        //if we are asking for today, return the live price.
        //check day, month and year is the same
        if(now.get(Calendar.DAY_OF_MONTH) == day && now.get(Calendar.MONTH) == month && now.get(Calendar.YEAR) == year){
            return getLivePrice();
        }
        else {
            day closestDay = days.get(0);
            for (int i = 0; i < days.size(); i++) {
                day currentDay = days.get(i);
                Calendar currentDayCalendar = Calendar.getInstance();
                currentDayCalendar.set(currentDay.year, currentDay.month-1, currentDay.day);
                //if dates are the same then closest day is this one
                long distance = Math.abs(givenDate.getTimeInMillis() - currentDayCalendar.getTimeInMillis());
                if (distance < smallestDist) {
                    smallestDist = distance;
                    closestDay = currentDay;
                }
            }
            int timeInDays = (int) Math.round(smallestDist / 8.64e+7);
            if (timeInDays > 0 && alert){
                JOptionPane.showMessageDialog(null, "Closest date found: " + closestDay.date + " (" + timeInDays + " day/s off)");
            }
            else{

            }
            return closestDay.high;
        }
    }


    /*
    Fetches stock info from the IEX API
     */
    private void loadInformation(){
        try{
            URL informationAddress = new URL("https://api.iextrading.com/1.0/stock/" + label + "/company");
            BufferedReader reader = new BufferedReader(new InputStreamReader(informationAddress.openStream()));
            String infoData = reader.readLine();

            //this regex matches all elements inside double quotes - e.g. string = "symbol":"AAPL", "name":"Apple Inc" will match into array {symbol, AAPL, name, Apple Inc}
            Matcher m = Pattern.compile("\"[^\"]+\"|(\\+)").matcher(infoData);
            List<String> attributes = new ArrayList<>();
            while(m.find()){
                attributes.add(m.group().replaceAll("\"", ""));
            }

            this.attributes.put("symbol", attributes.get(1));
            this.attributes.put("companyName", attributes.get(3));
            this.attributes.put("description", attributes.get(11));
            this.attributes.put("website", attributes.get(9));
            this.attributes.put("ceo", attributes.get(13));
        }
        catch (IOException e){
            System.out.println("Error with stock information URL: " + e);
        }
    }

    public void populate(String prefix) throws IOException {
        // Builds the 'day' ArrayList object using data from IEX
        this.days = new ArrayList<>();

        //---[ Import data from IEX ]---
        URL stockAddress = new URL("https://api.iextrading.com/1.0/stock/" + prefix + "/chart/5y");
        BufferedReader reader = new BufferedReader( new InputStreamReader(stockAddress.openStream()));
        String inputData = reader.readLine().replaceAll(Character.toString((char)34), ""); // String holds CSV values for all different days, also removes " character to reduce complexity

        if (inputData.equalsIgnoreCase("Unknown symbol")) {
            System.out.println("Invalid Stock Code.");
            return;
        }

        inputData = inputData.replace("[" , "");
        inputData = inputData.replace("]" , ""); // remove square brackets that surround the CSV
        String[] toRemove = {"date:", "open:", "high:", "low:", "close:", "volume:", "unadjustedVolume:", "change:", "changePercent:", "vwap:", "label:", "changeOverTime:"};
        for (int i = 0; i < toRemove.length; i++) {
            inputData = inputData.replaceAll(toRemove[i], "");
        }

        //---[ Create day objects ]---
        List<Integer> open = new ArrayList<>();
        List<Integer> close = new ArrayList<>();
        for(int i = 0; i < inputData.length(); i++) {
            // Loop through the input string (from IEX)
            if (inputData.charAt(i) == '{') {
                open.add(i); // i is position of opening bracket
            } else if (inputData.charAt(i) == '}') {
                close.add(i); // i is position of close bracket
            }
        }
        if (open.size() != close.size()) {
            System.out.println("Open and Close position array size mismatch");
        }
        for (int j = 0; j < open.size(); j++) {
            String temp = inputData.substring(open.get(j), close.get(j));
            this.days.add(new day(temp));
        }
    }

    /*
    This function fetches the current price for the stock.
     */
    protected Double getLivePrice(){
        double price = 0;
        try{
            URL informationAddress = new URL("https://api.iextrading.com/1.0/stock/" + this.label + "/price");
            BufferedReader reader = new BufferedReader(new InputStreamReader(informationAddress.openStream()));
            price = Double.parseDouble(reader.readLine());
        }
        catch (IOException e){
            System.out.println("Problem reading live stock price");
        }
        return price;
    }


    // Hold Information once gotten from Source
    public static void main(String[] args) {
        // Method should be blank except for testing stock class
    }
}


class day  {
    //---[ Data for each day of a stock ]---
    String label, date;
    int day, month, year;
    float open, high, low, close, change, changePercent, vwap;
    double changeOverTime;
    int volume, unadjustedVolume;

    day(String wholeDayData){
        // Class Constructor, pull data from CSV
        wholeDayData = wholeDayData.replace("{", ""); //remove braces around the string
        wholeDayData = wholeDayData.replace("}", "");

        String[] data = wholeDayData.split(",");
        if (data.length == 13) {
            // same value being comma separated ceases at oct 29 2018 for some reason
            data[10] += data[11];
            data[11] = data[12];  // Strings at 10 + 11 are part of the same value, but get separated because date contains a comma
            data[12] = null;
        }
        //for (int i = 0; i < data.length; i++) System.out.println(data[i]);

        //---[ Assign Values to Class Variables ]---
        date = data[0];
        open = Float.parseFloat(data[1]);
        high = Float.parseFloat(data[2]);
        low = Float.parseFloat(data[3]);
        close = Float.parseFloat(data[4]);
        volume = Integer.parseInt(data[5]);
        unadjustedVolume = Integer.parseInt(data[6]);
        change = Float.parseFloat(data[7]);
        changePercent = Float.parseFloat(data[8]);
        vwap = Float.parseFloat(data[9]);
        label = data[10];
        changeOverTime = Double.parseDouble(data[11]);


        //make calendar object
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date parsedDate;
        try{
            parsedDate = sdf.parse(date);
        }
        catch (ParseException e){
            System.out.println("Failed to parse stock date");
            parsedDate = new Date();
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(parsedDate);
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH)+1; //to stop month being 0 based
        year = cal.get(Calendar.YEAR);
    }

    //constructor for JUnit test
    public day(){

    }
}