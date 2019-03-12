//Created: 24/10/2018 by Henry Lewis
//Last Modified: 19/02/2019 by Henry Lewis

//---[ Class Imports ]---

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

class hButton extends JButton {
    // Set extended button class
    public hButton(String name) {
        // Constructor uses parsed name for button label, gives a standardised size
        this.setText(name);
        this.setPreferredSize(new Dimension(150,20)); // Can be overriden on case-by-case basis
    }
}

class hField extends JTextField {
    // Set extended text-field class
    private final String initialText;
    public hField(String text) {
        // Constructor uses parsed text to fill field (i.e. default text)
        this.setText(text);
        this.setForeground(Color.darkGray);
        this.initialText = text;
        this.setPreferredSize(new Dimension(50, 20)); // Can be overridden on case-by-case basis

        //For clearing behaviour
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(getText().equals(initialText)){
                    setText("");
                    setForeground(Color.black);
                }
                //select all if there is already text in there for easy clearing
                else if(!getText().equals("") || !getText().equals(initialText)){
                    selectAll();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(getText().equals("")){
                    setText(initialText);
                    setForeground(Color.darkGray);
                }
            }
        });
    }
}


class hLabel extends JLabel {
    // Set extended label class
    public hLabel(String text) {
        super(text);
    }
}

class hPanel extends JPanel {
    // Set extended panel class
    public hPanel(String id) {
        // Constructor uses parsed dimensions to build panel
        this.setName(id);
        this.setMinimumSize(new Dimension(200,100));
        this.setLayout(new BorderLayout());
    }
}

class sButton extends JButton {
    // Set extended button class for stock-panel elements
    public sButton(String id, String name) {
        this.setName(id);
        this.setText(name);
        this.setMinimumSize(new Dimension(150, 30));
    }

    /*
    Resize font if text is too big for label
     */
    @Override
    public void setText(String text) {
        //wrap in HTML
        String buttonText = "<html><center>"+ text + "</center></html>";
        super.setText(buttonText);
    }
}


class hTabbedPane extends JTabbedPane {
    // Set extended Tabbed pane class
    public hTabbedPane(String id) {
        // Constructor uses parsed dimensions to build panel
        this.setName(id);
        this.setTabPlacement(TOP);
        this.setMinimumSize(new Dimension(210,110));

    }
}

public class gui extends JFrame{
    //---[ Declare Frame Variables ]---
    private user guiUser;
    private GetUserData saver;

    //---[ Init news stuff ]---
    private NewsGetter newsGetter;
    private String[] newsArray;
    private String newsLink;
    private String title;
    private String newsContent;


    Map<Integer, String> stockPos = new LinkedHashMap<>(); // This holds stocks 1-5 and their stock-codes
    // Date information for changelog and calendar
    public int currentDay;
    public int currentMonth;
    public int currentYear;
    public int viewedDay;
    public int viewedMonth;
    public int viewedYear;
    private int stockBeingShown = 1;

    //---[ Declare Frame Elements ]---
    private hButton switch_time;
    private hButton addStock, removeStock; // Stock pane buttons
    private hButton addValue, subValue, calendarButton; // Banking pane buttons

    private hLabel news, newsTitle, newsContents, totalInvestmentValue, totalSum;
    private hLabel stockTitle, stockInfo, stockValue, stockNum; // Stock page text-labels\\
    private JLabel currDateLabel, bankValue;

    private hField stockNameField, stockAmountField, transactionField, nameField;
    private hPanel homePanel, stocksPanel, bankPanel, chartPanel, userPanelTab; // Tab panels

    private ChartPanel chart;
    private UserPanel userPanel;
    private LivePricePanel livePanel;
    private UserWorthChart worthPanel;
    private JPanel homeTop, homeBottom, stocksLeft, stocksRight, stocksRightTop, stocksRightBottom, stocksBottom, bankOne, bankTwo, bankThree, bankFour; // Panels that sit within tab-panels
    private sButton stockOne, stockTwo, stockThree, stockFour, stockFive; // Panels that hold stocks in the stock panel
    private hTabbedPane tabs;

    public gui() {
        //---[ Build New User Obj ]---
        String[] stocks = new String[0];
        String[] bankDetails = {"",""};
        int[] stockAmounts = new int[0];
        guiUser = new user("", 0, stocks, stockAmounts, bankDetails, ""); // Build a blank user profile

        //--[Used for loading user data]--
        saver = new GetUserData();

        //set the date
        setDate();

        //set minimum size
        setMinimumSize(new Dimension(900, 500));

        // Frame constructor
        tabs = new hTabbedPane("tabs");

            // Tab elements can be added here i.e. images (icons)

        //---[ Frame Panels ]---
        homePanel = new hPanel("home");
        tabs.addTab("Home", null, homePanel, "Home page, shows total investments"); // Null icon value as non used yet
        tabs.setMnemonicAt(0, KeyEvent.VK_1); // Switch to tab by pressing Alt + 1

        stocksPanel = new hPanel("stocks");
        tabs.addTab("Stocks", null, stocksPanel, "Stock information is displayed here");
        tabs.setMnemonicAt(1, KeyEvent.VK_2);

        bankPanel = new hPanel("bank");
        bankPanel.setLayout(new BoxLayout(bankPanel, BoxLayout.PAGE_AXIS));
        tabs.addTab("Bank Account", null, bankPanel, "View cash available");
        // for future ref, GridBag probably suitable for desired look
        tabs.setMnemonicAt(2, KeyEvent.VK_3);

        chartPanel = new hPanel("Charts");
        tabs.addTab("Charts Panel", null, chartPanel, "Show stock information in a chart");
        tabs.setMnemonicAt(3, KeyEvent.VK_4);

        livePanel = new LivePricePanel(guiUser);
        //projectionsPanel.setLayout() // Set layout manager for projectionsPanel
        tabs.addTab("Live Stock Prices", null, livePanel, "Get Live Prices");
        tabs.setMnemonicAt(4, KeyEvent.VK_5);

        userPanelTab = new hPanel("User Info");
        tabs.addTab("User Settings", null, userPanelTab, "Log in/out and see your information");
        tabs.setMnemonicAt(5, KeyEvent.VK_6);

        this.add(tabs); // Add completed tab pane to window frame.

        //---[ Add Panels to Frames ]---
        // Home Tab panels
        homeTop = new JPanel();
        homeTop.setLayout(new BorderLayout());
        homeBottom = new JPanel();
        homeBottom.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        homePanel.add(homeTop, BorderLayout.NORTH);
        homePanel.add(homeBottom, BorderLayout.SOUTH);

        // Stocks Tab panels
        stocksLeft = new JPanel();
        stocksLeft.setLayout(new GridLayout(5, 0, 10, 10));
        stocksLeft.setPreferredSize(new Dimension(200, 300));

        stocksRight = new JPanel(new BorderLayout());
        stocksRightTop = new JPanel();
        stocksRightTop.setLayout(new BoxLayout(stocksRightTop, BoxLayout.PAGE_AXIS));
        stocksRightTop.setPreferredSize(new Dimension(250, 300));
        stocksRightBottom = new JPanel(new BorderLayout());
        stocksRightBottom.setPreferredSize(new Dimension(250, 400));
        currDateLabel = new JLabel("Date selected:  " + viewedDateToString());
        currDateLabel.setFont(currDateLabel.getFont().deriveFont(20f));
        currDateLabel.setPreferredSize(new Dimension(250, 200));
        // Calendar button on stock page
        calendarButton = new hButton("Set Date");
        stocksRightBottom.add(currDateLabel, BorderLayout.CENTER);
        stocksRightBottom.add(calendarButton, BorderLayout.SOUTH);

        stocksBottom = new JPanel();
        stocksBottom.setLayout(new GridLayout(2, 2));

        stocksBottom.setPreferredSize(new Dimension(490, 50));
        stocksPanel.add(stocksBottom, BorderLayout.SOUTH);
        stocksPanel.add(stocksLeft, BorderLayout.WEST);

        stocksRight.add(stocksRightTop, BorderLayout.NORTH);
        stocksRight.add(stocksRightBottom, BorderLayout.EAST);

        stocksPanel.add(stocksRight, BorderLayout.EAST);

        // Chart Tab panels
        JTabbedPane chartTabs = new JTabbedPane(JTabbedPane.TOP);
        worthPanel = new UserWorthChart(guiUser);
        chart = new ChartPanel(this.guiUser);
        chartTabs.add("Stock Values Chart", chart);
        chartTabs.add("User Total Worth Chart", worthPanel);
        chartPanel.add(chartTabs);


        // Bank Tab panels
        bankOne = new JPanel();
        bankOne.setLayout(new GridBagLayout());
        GridBagConstraints bankLayoutConstraints = new GridBagConstraints();// GridBagLayout constraints
        bankLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        bankOne.setPreferredSize(new Dimension(500, 50));
        bankPanel.add(bankOne);
        bankTwo = new JPanel();
        bankTwo.setLayout(new GridLayout());
        bankPanel.add(bankTwo);
        bankThree = new JPanel();
        bankThree.setLayout(new BorderLayout());
        bankPanel.add(bankThree);
        bankFour = new JPanel();
        bankFour.setLayout(new FlowLayout());
        bankPanel.add(bankFour);

        // User-Info Tab panels
        userPanel = new UserPanel(this);
        userPanelTab.add(userPanel);

        //---[ Add buttons to panel / frame ]---
        /*switch_time = new hButton("Switch time");
        bankTwo.add(switch_time);*/


        // Stock Left-Panel buttons
        stockOne = new sButton("stock one", "No stock");
        stocksLeft.add(stockOne);
        stockTwo = new sButton("stock two", "No stock");
        stocksLeft.add(stockTwo);
        stockThree = new sButton("stock three", "No stock");
        stocksLeft.add(stockThree);
        stockFour = new sButton("stock four", "No stock");
        stocksLeft.add(stockFour);
        stockFive = new sButton("stock five", "No stock");
        stocksLeft.add(stockFive);

        // Stock Bottom-Panel buttons
        addStock = new hButton("Buy Stock");
        addStock.setPreferredSize(new Dimension(50, 25));
        stocksBottom.add(addStock);
        removeStock = new hButton("Sell Stock");
        removeStock.setPreferredSize(new Dimension(50, 25));
        stocksBottom.add(removeStock);

        //need to update the text

        // Banking Panel Buttons
        addValue = new hButton("Add");
        bankLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        bankLayoutConstraints.gridx = 0;
        bankLayoutConstraints.gridy = 2;
        bankLayoutConstraints.ipadx = 3;
        bankLayoutConstraints.ipady = 3;
        bankOne.add(addValue, bankLayoutConstraints);
        subValue = new hButton("Widthdraw");
        bankLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        bankLayoutConstraints.gridx = 1;
        bankLayoutConstraints.gridy = 2;
        bankLayoutConstraints.ipadx = 3;
        bankLayoutConstraints.ipady = 3;
        bankOne.add(subValue, bankLayoutConstraints);


        //---[ Labels ]---
        // Text labels for Home tab (and its panels)

        newsTitle = new hLabel("");
        newsContents = new hLabel("");
        newsGetter = new NewsGetter();
        setNews(newsGetter.getNews());
        homeTop.add(newsTitle, BorderLayout.NORTH);
        homeTop.add(newsContents, BorderLayout.CENTER);
        totalInvestmentValue = new hLabel("Please log in to save changes!");
        totalInvestmentValue.setFont(totalInvestmentValue.getFont().deriveFont(20f));
        totalInvestmentValue.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
        homeBottom.add(totalInvestmentValue);

        /*
        MouseListener to handle opening a webpage and font colour change
         */
        newsTitle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openLink();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                newsTitle.setText("<html><h1>News</h1><br><h2><u><font color=blue>"+title+"</font></u></h2></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                newsTitle.setText("<html><h1>News</h1><br><h2><u>"+title+"</u></h2></html>");
            }
        });


        JLabel newsInfoLabel = new JLabel(Integer.toString(newsGetter.getPos()+1));
        newsInfoLabel.setFont(newsInfoLabel.getFont().deriveFont(18f));
        //JButton to get previous news
        JButton prevStoryButton = new JButton();
        try{
            //arrow img by 'Lyolya' from flaticon.com
            Image img = ImageIO.read(getClass().getResource("left-arrow.png"));
            img = img.getScaledInstance(32,32, Image.SCALE_SMOOTH);
            prevStoryButton.setIcon(new ImageIcon(img));
        }
        catch (Exception e){
            prevStoryButton.setText("<--");
        }
        prevStoryButton.addActionListener(e->{
            newsGetter.getPreviousStory();
            try{
                setNews(newsGetter.getNews());
            }
            catch (NullPointerException err){
                String[] noNews = {"", "<html><h1>No more news to display...</h1></html>", "\uD83D\uDE13"};
                setNews(noNews);
            }
            newsInfoLabel.setText(Integer.toString(newsGetter.getPos()+1));
        });

        //JButton to get next news
        JButton nextStoryButton = new JButton();
        try{
            //arrow img by 'Lyolya' from flaticon.com
            Image img = ImageIO.read(getClass().getResource("right-arrow.png"));
            img = img.getScaledInstance(32,32, Image.SCALE_SMOOTH);
            nextStoryButton.setIcon(new ImageIcon(img));
        }
        catch (Exception e){
            nextStoryButton.setText("-->");
        }

        nextStoryButton.addActionListener(e->{
            newsGetter.getNextStory();
            try{
                setNews(newsGetter.getNews());
            }
            catch (NullPointerException err){
                String[] noNews = {"", "<html><h1>No more news to display...</h1></html>", "\uD83D\uDE13"};
                setNews(noNews);
            }
            newsInfoLabel.setText(Integer.toString(newsGetter.getPos()+1));
        });


        homeBottom.add(prevStoryButton);
        homeBottom.add(newsInfoLabel);
        homeBottom.add(nextStoryButton);


        // Text labels for Stocks tab
        stockTitle = new hLabel("<html>Click on a button to get information for a stock!<html>");
        stockTitle.setBorder(new EmptyBorder(20,0,0,0));
        stockTitle.setFont(stockTitle.getFont().deriveFont(20f));
        stocksRightTop.add(stockTitle);
        stockNum = new hLabel("");
        stockNum.setFont(stockNum.getFont().deriveFont(18f));
        stocksRightTop.add(stockNum);
        stockInfo = new hLabel("");
        stockInfo.setFont(stockInfo.getFont().deriveFont(18f));
        stocksRightTop.add(stockInfo);
        stockValue = new hLabel("");
        stockValue.setFont(stockValue.getFont().deriveFont(18f));
        stocksRightTop.add(stockValue);


        // Text labels for Banking tab
        bankValue = new JLabel(("Balance: " + guiUser.bank.returnAmountString()), SwingConstants.CENTER);
        bankValue.setFont(bankValue.getFont().deriveFont(20f));
        bankValue.setBorder(new EmptyBorder(0,0,40,0));
        bankLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        bankLayoutConstraints.gridx = 0;
        bankLayoutConstraints.gridy = 0;
        bankLayoutConstraints.gridwidth = 2;
        bankLayoutConstraints.ipadx = 3;
        bankLayoutConstraints.ipady = 3;
        bankOne.add(bankValue, bankLayoutConstraints);

        //---[ Text Fields ]---

        // Stock panel text fields
        stockNameField = new hField("Stock ID (e.g. AAPL)");

        stockNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(Character.isLetter(e.getKeyChar())){
                    stockNameField.setText(stockNameField.getText().toUpperCase());
                }
            }
        });


        stockNameField.setPreferredSize(new Dimension(300, 50));
        stocksBottom.add(stockNameField);
        stockAmountField = new hField("Stock Amount (e.g. 12)");
        stockAmountField.setPreferredSize(new Dimension(300, 50));
        stocksBottom.add(stockAmountField);



        // Bank panel text fields
        transactionField = new hField("Enter amount of $ here");
        bankLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        bankLayoutConstraints.gridx = 0;
        bankLayoutConstraints.gridy = 1;
        bankLayoutConstraints.gridwidth = 2;
        bankLayoutConstraints.ipadx = 3;
        bankLayoutConstraints.ipady = 3;
        bankOne.add(transactionField, bankLayoutConstraints);

        //---[ Button Action Listeners ]---
            /* Note: Use the lambda method for action listeners, its more efficient
             listeners so when stock button pressed, changes info displayed on right pane */



            // add / remove stock button action listeners
        addStock.addActionListener(e -> {
            try {
                double price = guiUser.addStock(stockNameField.getText(), Integer.parseInt(stockAmountField.getText()), true);
                updateStockPos();
                totalInvestmentValue.setText("Total Investment Value: $" + guiUser.totalWorth());
                saveChangesToUser();
                showMessageDialogue(String.format("Bought " + stockAmountField.getText() + " "
                        + stockNameField.getText()+ " stocks "+ "for $%.2f", price), "Bought Stocks in " + stockNameField.getText(), JOptionPane.INFORMATION_MESSAGE);
                setStockInfo(stockBeingShown);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding stock. Please check the code/quantity and try again", "Stock Not Added",JOptionPane.ERROR_MESSAGE);
                System.out.println("There was an error creating the new stock. (GUI)");
            }
        });

        removeStock.addActionListener(e -> {
            try{
                String stockName = stockNameField.getText();
                double[] soldInfo = guiUser.sellStock(stockNameField.getText(), Integer.parseInt(stockAmountField.getText()), true);
                double profit = soldInfo[1];
                double soldFor = soldInfo[0];
                chart.setStock(stockNameField.getText().toUpperCase(), false);
                totalInvestmentValue.setText("Total Investment Value: $" + guiUser.totalWorth());
                saveChangesToUser();
                //show message for how much it sold for
                String messageString = String.format("Sold " +  stockAmountField.getText() + " " +stockName + " stock/s for $%.2f ($%.2f profit)", soldFor, profit);
                showMessageDialogue(messageString, "Sold stock in " +stockName, JOptionPane.INFORMATION_MESSAGE);
                setStockInfo(stockBeingShown);
            } catch (NullPointerException | IllegalArgumentException ex){
                JOptionPane.showMessageDialog(this, "Error removing stock. Please check the code/quantity and try again", "Stock Not Removed",JOptionPane.ERROR_MESSAGE);
                System.out.println("There was an error removing the stock. (GUI)");
            }
        });

            // bank panel button action listeners
        addValue.addActionListener(e -> {
            try {
                guiUser.bank.addAmount(Double.parseDouble(transactionField.getText()));
                bankValue.setText("Balance: " + guiUser.bank.returnAmountString());
                totalInvestmentValue.setText("Total Investment Value: " +  guiUser.bank.returnAmountString());
                saveChangesToUser();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please check a number was entered", "Value not added",JOptionPane.ERROR_MESSAGE);
                System.out.println("Could not parse transactionField contents to int");
                ex.printStackTrace();
            }
        });

        subValue.addActionListener(e -> {
            try {
                guiUser.bank.removeAmount(Double.parseDouble(transactionField.getText()));
                bankValue.setText("Balance: " + guiUser.bank.returnAmountString());
                totalInvestmentValue.setText("Total Investment Value: " +  guiUser.bank.returnAmountString());
                saveChangesToUser();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please check a number was entered", "Value not subtracted",JOptionPane.ERROR_MESSAGE);
                System.out.println("Could not parse transactionField contents to int"); }
        });

            // stock info display button action listeners
        stockOne.addActionListener(e -> {
            setStockInfo(1);
        });

        stockTwo.addActionListener(e -> {
            setStockInfo(2);
        });

        stockThree.addActionListener(e -> {
            setStockInfo(3);
        });

        stockFour.addActionListener(e -> {
            setStockInfo(4);

        });

        stockFive.addActionListener(e -> {
            setStockInfo(5);
        });



        calendarButton.addActionListener(e -> {
            calendarWindow calendar = new calendarWindow();
            calendar.getDays(this);
            calendar.setVisible(true);
        });



        //---[ Declare Frame Information ]---
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(true);
        this.setPreferredSize(new Dimension(900, 500));
        this.setTitle("CE291 Project Application");
        this.pack();
        this.setLocationRelativeTo(null);
        /*
        additional frame size information goes here i.e. min/max dimensions
         */
    }

    private void setStockInfo(int stockNumber){
        stockBeingShown = stockNumber;
        try{
            stockTitle.setText(stockPos.get(stockNumber));
            stockNum.setText(guiUser.stocksIDOwned.get(stockPos.get(stockNumber)) + " stocks owned");
            day today = guiUser.stockObjOwned.get(stockPos.get(stockNumber)).getToday();
            stockInfo.setText("<html> High: " + today.high // Display information from today
                    + "<br>Low: " + today.low
                    + "<br>Close: "+ today.close
                    + "<br>Volume: " + today.volume
                    + "<br>Change: " + today.change + "</html>");
            // guiUser.stockObjOwned.get(stockPos.get(1)); <- this is a stock object reference
            stockValue.setText(Double.toString(guiUser.stockObjOwned.get(stockPos.get(stockNumber)).getWorth()));
            stockNameField.setText(guiUser.stockObjOwned.get(stockPos.get(stockNumber)).getName());
        } catch (Exception ex){
            stockTitle.setText("No stock in this position");
            stockNum.setText("");
            stockInfo.setText("");
            stockValue.setText("");
        }
    }

    private void setNews(String[] news){
        this.newsLink = news[0];
        this.title = news[1];
        this.newsContent = news[2];

        newsTitle.setText("<html><h1>News</h1><br><h2><u>"+this.title+"</u></h2></html>");
        newsTitle.setBorder(new EmptyBorder(0,10,0,0));
        newsContents.setText("<html>" + this.newsContent + "</html>");
        newsContents.setFont(newsContents.getFont().deriveFont(20f));
        newsContents.setBorder(new EmptyBorder(0,10,20,0));
        this.repaint();
        this.revalidate();
    }

    //method to open the news link
    private void openLink(){
        try{
            Desktop.getDesktop().browse(new URI(newsLink));
        }
        catch (URISyntaxException | IOException err){
            System.out.println("system does not support opening web browser");
        }
    }

    //method to show message dialogue
    public void showMessageDialogue(String content, String title, int type){
        JOptionPane.showMessageDialog(this, content, title, type);
    }


    public user getGuiUser(){
        return this.guiUser;
    }

    /*
    Function to load in user data from a user file
     */
    public void setGuiUser(user newUser){
        this.guiUser.buildFromFile(newUser);
        worthPanel.setLoggedIn();
        updateInformation();
    }
    /*
    Writes a new user file with updated changes
     */
    public void saveChangesToUser(){
        saver.save(guiUser);
        updateInformation();
    }

    /*
    Function updates the labels to show correct information
     */
    private void updateInformation(){
        updateStockPos();
        totalInvestmentValue.setText("Total Investment Value: $" + guiUser.totalWorth());
        bankValue.setText("Balance: " + guiUser.bank.returnAmountString());
        chart.updateGUI();
        try{
            userPanel.updateUserInfo();
        }
        catch (NullPointerException e){
            //change labels back to default
            setStockInfo(0);
        }

        //update charts
        chart.updateGUI();
        worthPanel.generateGraph();

        resetButtonText();
        updateStockPos();
        livePanel.update();
        this.repaint();
        this.revalidate();
    }

    private void resetButtonText() {
        stockOne.setText("No stock");
        stockTwo.setText("No stock");
        stockThree.setText("No stock");
        stockFour.setText("No stock");
        stockFive.setText("No stock");
    }

    public void updateStockPos() {
        Iterator it = guiUser.stocksIDOwned.entrySet().iterator();
        int counter = 1;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            stockPos.put(counter, (String) pair.getKey());
            counter++;
        }

        //---[ Update Stock-Tab Buttons ]---
        if (guiUser.stocksIDOwned.size() == 0) {
            resetButtonText();
        }
        if (guiUser.stocksIDOwned.size() >= 1) { stockOne.setText(guiUser.stockObjOwned.get(stockPos.get(1)).getAttributes("companyName")); } // if has one stock owned then this
        if (guiUser.stocksIDOwned.size() >= 2) { stockTwo.setText(guiUser.stockObjOwned.get(stockPos.get(2)).getAttributes("companyName")); } // if has two stocks owned, etc...
        if (guiUser.stocksIDOwned.size() >= 3) { stockThree.setText(guiUser.stockObjOwned.get(stockPos.get(3)).getAttributes("companyName")); }
        if (guiUser.stocksIDOwned.size() >= 4) { stockFour.setText(guiUser.stockObjOwned.get(stockPos.get(4)).getAttributes("companyName")); }
        if (guiUser.stocksIDOwned.size() >= 5) { stockFive.setText(guiUser.stockObjOwned.get(stockPos.get(5)).getAttributes("companyName")); }
    }

    public boolean updateViewedDate(int day, int month, int year) {
        if (day < 31 && day > 0) {
            viewedDay = day;
            if (month < 12 && month >= 0) {
                viewedMonth = month;
                if (year <= currentYear && year > currentYear - 6) {
                    viewedYear = year;
                    System.out.println("viewed date updated to: " + day + month + year);
                    currDateLabel.setText("Date selected: " + viewedDateToString());
                    guiUser.setDate(year, month, day); //set the user date so we can buy stocks on this date
                    updateInformation();
                    return true;
                } else {
                    System.out.println("Given year to update viewed date out of bounds.");
                }
            }
            else {
                System.out.println("Given month to update viewed date out of bounds.");
            }
        } else {
            System.out.println("Given day to update viewed date out of bounds.");
        }
        return false;
    }

    public String currentDateToString() {
        String date = currentDay + "-" + currentMonth + "-" + currentYear;
        return date;
    }

    public String viewedDateToString() {
        String date = viewedDay + "-" + (viewedMonth+1) + "-" + viewedYear;
        return date;
    }

    private void setDate() {
        Calendar cal = Calendar.getInstance();
        currentYear = cal.get(Calendar.YEAR);
        currentMonth = cal.get(Calendar.MONTH);
        currentDay = cal.get(Calendar.DAY_OF_MONTH);
        viewedYear = currentYear;
        viewedMonth = currentMonth;
        viewedDay = currentDay;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) { System.out.println("Look and Feel not set"); }
        gui mainFrame = new gui();
        mainFrame.setVisible(true);
    }
}
