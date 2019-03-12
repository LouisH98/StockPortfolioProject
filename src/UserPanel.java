/* User Panel used for Log-in/out. Loads data from an external file and populates information in the app. */
//class by Louis H

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class UserPanel extends JPanel {
    gui window;
    private JPanel leftPanel;
    private JPanel topLeftPanel;
    private JPanel bottomLeftPanel;

    private JPanel rightPanel;


    //text fields.
    private JTextField loginUName;
    private JPasswordField loginPWord;

    public UserPanel(gui window) {
        setLayout(new BorderLayout());
        this.window = window;

        // Init sub-panels
        leftPanel = new JPanel();
        topLeftPanel = new JPanel();
        bottomLeftPanel = new JPanel();
        rightPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        topLeftPanel.setLayout(new GridBagLayout());
        bottomLeftPanel.setLayout(new GridBagLayout());
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        JScrollPane bottomScroll = new JScrollPane(rightPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        bottomScroll.getVerticalScrollBar().setUnitIncrement(10);


        //top left panel elements

        //constraints
        GridBagConstraints constraint = new GridBagConstraints();

        JLabel loginLabel = new JLabel("Log-in");
        loginLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0,0));
        loginLabel.setFont(loginLabel.getFont().deriveFont(18f));
        constraint.fill = GridBagConstraints.BOTH;
        constraint.weightx = 0.5;
        constraint.weighty = 0.5;
        constraint.ipadx = 1;
        constraint.ipady = 1;
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.CENTER;
        topLeftPanel.add(loginLabel, constraint);

        JLabel usernameLabel = new JLabel("Username:", SwingConstants.CENTER);
        usernameLabel.setFont(usernameLabel.getFont().deriveFont(12f));
        constraint.weightx = 1;
        constraint.weighty = 1;
        constraint.ipadx = 1;
        constraint.ipady = 1;
        constraint.gridx = 0;
        constraint.gridy = 1;
        topLeftPanel.add(usernameLabel, constraint);

        JLabel passwordLabel = new JLabel("Password:", SwingConstants.CENTER);
        passwordLabel.setFont(passwordLabel.getFont().deriveFont(12f));
        constraint.gridx = 0;
        constraint.gridy = 2;
        topLeftPanel.add(passwordLabel, constraint);


        loginUName = new JTextField(10);
        constraint.gridx = 1;
        constraint.gridy = 1;
        constraint.gridwidth = 4;
        constraint.anchor = GridBagConstraints.LINE_START;
        topLeftPanel.add(loginUName, constraint);

        loginPWord = new JPasswordField(10);
        constraint.gridx = 1;
        constraint.gridy = 2;
        constraint.gridwidth = 4;
        topLeftPanel.add(loginPWord, constraint);

        //keyListener for when enter is pressed - try log in
        loginPWord.addActionListener(new loginHandler());

        JButton loginButton = new JButton("Log in");
        constraint.gridx = 1;
        constraint.gridy = 3;
        constraint.gridwidth = 3;
        constraint.anchor = GridBagConstraints.CENTER;
        topLeftPanel.add(loginButton, constraint);



        //bottom left panel

        //constraints
        constraint = new GridBagConstraints();

        JLabel signupLabel = new JLabel("Create an account");
        signupLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0,0));
        signupLabel.setFont(signupLabel.getFont().deriveFont(18f));
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.weightx = 0.5;
        constraint.weighty = 0.5;
        constraint.ipadx = 0;
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.LINE_START;
        bottomLeftPanel.add(signupLabel, constraint);

        JLabel helpLabel = new JLabel("(with current stocks)");
        constraint.anchor = GridBagConstraints.CENTER;
        helpLabel.setFont(helpLabel.getFont().deriveFont(12f));
        constraint.weightx = 0.5;
        constraint.weighty = 0.5;
        constraint.ipadx = 0;
        constraint.gridx = 1;
        constraint.gridwidth = 3;
        constraint.gridy = 0;
        bottomLeftPanel.add(helpLabel, constraint);


        JLabel signupUNameLabel = new JLabel("Username:", SwingConstants.CENTER);
        signupUNameLabel.setFont(usernameLabel.getFont().deriveFont(12f));
        constraint.gridy = 1;
        constraint.gridx = 0;
        constraint.gridwidth = 1;
        bottomLeftPanel.add(signupUNameLabel, constraint);

        JTextField signupUNameField = new JTextField(16);
        constraint.gridx = 1;
        constraint.gridy = 1;
        constraint.gridwidth = 3;
        bottomLeftPanel.add(signupUNameField, constraint);



        JLabel signupPassLabel = new JLabel("Password:", SwingConstants.CENTER);
        signupPassLabel.setFont(passwordLabel.getFont().deriveFont(12f));
        constraint.gridx = 0;
        constraint.gridy = 2;
        constraint.gridwidth = 1;
        bottomLeftPanel.add(signupPassLabel, constraint);

        JPasswordField signupPassField = new JPasswordField(16);
        constraint.gridx = 1;
        constraint.gridy = 2;
        constraint.gridwidth = 3;
        bottomLeftPanel.add(signupPassField, constraint);


        JLabel cardLabel = new JLabel("Card Number:", SwingConstants.CENTER);
        cardLabel.setFont(passwordLabel.getFont().deriveFont(12f));
        constraint.gridx = 0;
        constraint.gridy = 3;
        constraint.gridwidth = 1;
        bottomLeftPanel.add(cardLabel, constraint);

        JTextField cardNumber = new JTextField(16);
        constraint.gridx = 1;
        constraint.gridy = 3;
        constraint.gridwidth = 3;
        bottomLeftPanel.add(cardNumber, constraint);

        JLabel typeLabel = new JLabel("Card Type:", SwingConstants.CENTER);
        typeLabel.setFont(passwordLabel.getFont().deriveFont(12f));
        constraint.gridx = 0;
        constraint.gridy = 4;
        constraint.gridwidth = 1;
        bottomLeftPanel.add(typeLabel, constraint);

        String[] cardTypes = {"VISA", "MASTERCARD", "ELECTRON", "AMEX"};
        JComboBox cardChooser = new JComboBox(cardTypes);
        constraint.gridx = 1;
        constraint.gridy = 4;
        constraint.gridwidth = 3;
        bottomLeftPanel.add(cardChooser, constraint);

        JButton signUpButton = new JButton("Sign up");
        constraint.gridx = 1;
        constraint.gridy = 5;
        constraint.gridwidth = 3;
        bottomLeftPanel.add(signUpButton, constraint);



        //right panel
        JLabel notLoggedIn = new JLabel("Not Currently Logged-in.");
        notLoggedIn.setAlignmentX(Component.CENTER_ALIGNMENT);
        notLoggedIn.setFont(notLoggedIn.getFont().deriveFont(20f));
        notLoggedIn.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));
        rightPanel.add(notLoggedIn);



        JSplitPane vertPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                topLeftPanel, bottomLeftPanel);
        vertPane.setEnabled(false);
        vertPane.setDividerLocation(0.3);
        vertPane.setDividerSize(0);
        leftPanel.add(vertPane);



        JSplitPane horizPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, bottomScroll);
        horizPane.setEnabled(false);
        horizPane.setDividerLocation(350);
        horizPane.setDividerSize(0);
        this.add(horizPane, BorderLayout.CENTER);


        //action listeners
        loginButton.addActionListener(new loginHandler());
        signUpButton.addActionListener(new signupHandler(signupUNameField, signupPassField, cardNumber, cardChooser));


    }


    public void updateUserInfo(){
        //clear the panel
        Component[] components = rightPanel.getComponents();
        for (Component comp : components){
            rightPanel.remove(comp);
        }

        this.repaint();
        this.revalidate();

        //gui user to get info from
        user currentUser = window.getGuiUser();

        String username;
        if (currentUser.name.equals("")) {
            username = "Please log-in/sign-up";
        } else {
            username = "Hello " + currentUser.name + "!";
        }

        JLabel userName = new JLabel(username, SwingConstants.CENTER);
        userName.setFont(userName.getFont().deriveFont(20f));
        userName.setAlignmentX(Component.CENTER_ALIGNMENT);
        userName.setBorder(BorderFactory.createEmptyBorder(10,0,20,0));
        rightPanel.add(userName);


        JLabel bankInfo = new JLabel(currentUser.bank.toString(), SwingConstants.CENTER);
        bankInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        bankInfo.setBorder(BorderFactory.createEmptyBorder(0,10,10,0));
        bankInfo.setFont(bankInfo.getFont().deriveFont(15f));
        rightPanel.add(bankInfo);



        //total user worth label
        JLabel worthLabel = new JLabel("Total Asset Worth: $" + user.round(currentUser.totalWorth(), 2), SwingConstants.CENTER);
        worthLabel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        worthLabel.setFont(worthLabel.getFont().deriveFont(20f));
        worthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(worthLabel);

        //title
        JLabel stocksTitle = new JLabel("Stocks:", SwingConstants.CENTER);
        stocksTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        stocksTitle.setFont(stocksTitle.getFont().deriveFont(20f));
        rightPanel.add(stocksTitle);

        //get each stock and add it's info to the panel
        for(String stock : currentUser.stockObjOwned.keySet()){
            //stock properties
            String stockSymbol = currentUser.stockObjOwned.get(stock).getName();
            String stockValue = Double.toString(currentUser.stockObjOwned.get(stock).getWorth());
            String stockName = currentUser.stockObjOwned.get(stock).getAttributes("companyName");
            String website = currentUser.stockObjOwned.get(stock).getAttributes("website");
            String companyCEO = currentUser.stockObjOwned.get(stock).getAttributes("ceo");
            float high = currentUser.stockObjOwned.get(stock).getToday().high;
            float low = currentUser.stockObjOwned.get(stock).getToday().low;
            int amount = currentUser.stocksIDOwned.get(stock);
            double stockWorth = currentUser.stockObjOwned.get(stock).getWorth() * amount;
            String labelText = "<html><center>"
                    + stockName + "(<font color=blue>"+ stockSymbol + "</font>)<br> CEO: <strong>" + companyCEO + "</strong>"
                    + "<br>Open: <strong>"+ stockValue+"</strong>&nbsp;&nbsp;"
                    + "High: <strong>"+high+"</strong>&nbsp;&nbsp;"
                    + "Low: <strong>"+low
                    + "<br><font color=green>Total Stock Value:</font> $<strong>"+user.round(stockWorth, 2)+"</strong>"
                    + " ("+amount+"x)</center></html>";

            JLabel stockLab = new JLabel(labelText, SwingConstants.CENTER);
            stockLab.setBorder(BorderFactory.createEmptyBorder(0,10,10,0));
            stockLab.setAlignmentX(Component.CENTER_ALIGNMENT);
            stockLab.setFont(stockLab.getFont().deriveFont(15f));

            stockLab.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    openURLInBrowser(website);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    stockLab.setForeground(Color.blue);
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    stockLab.setForeground(Color.black);
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });

            rightPanel.add(stockLab);
        }

        JButton saveInfoButton = new JButton("Save to PDF");
        saveInfoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(saveInfoButton);

        saveInfoButton.addActionListener(e -> {
            // Make call to export PDF function in user

            //get save location
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            FileFilter filter = new FileNameExtensionFilter("PDF File", "pdf");
            fileChooser.setFileFilter(filter);
            int userSaved = fileChooser.showSaveDialog(this);
            String saveDir = fileChooser.getSelectedFile().toString();

            if(userSaved == 0){
                window.getGuiUser().exportPDF(saveDir);
                System.out.println("PDF Saved. (UserPanel)");
            }

        });

        if(currentUser.stockObjOwned.size() == 0){
            JLabel noStockLab = new JLabel("No Stocks Owned");
            noStockLab.setBorder(BorderFactory.createEmptyBorder(0,10,10,0));
            noStockLab.setAlignmentX(Component.CENTER_ALIGNMENT);
            noStockLab.setFont(noStockLab.getFont().deriveFont(20f));
            rightPanel.add(noStockLab);
        }
    }

    /*
    Opens a browser window located to given URL - used for stock info
     */
    private void openURLInBrowser(String URL){
        try{
            Desktop.getDesktop().browse(new URI(URL));

        }
        catch (IOException | URISyntaxException e){
            System.out.println("Error in opening stock URL: " + e);
        }
    }

    /*
    Clears all text fields on the panel
     */
    protected void clearFields(JPanel panel) {
        Component[] elements = panel.getComponents();
        for (Component comp : elements) {
            if ((comp instanceof JTextField)) {
                ((JTextField) comp).setText("");
            }
        }
    }

    /*
    loginHandler class that looks for a file with the username that the user entered.
    If it is found and the password matches, then the users values are loaded.
     */
    class loginHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(loginUName.getText().trim().isEmpty() || loginPWord.getPassword().length == 0){
                JOptionPane.showMessageDialog(window, "Please make sure you have filled in all the fields", "Missing Field", JOptionPane.ERROR_MESSAGE);
            }
            else{
                String username = loginUName.getText().trim();
                String password = new String(loginPWord.getPassword()).trim();
                user newUser;
                GetUserData userLoader = new GetUserData();

                //search for files with called 'name.user'
                File userFile = new File(username+".user");
                if(userFile.exists()){
                    newUser = userLoader.load(username+".user");
                    //check password
                    if(newUser.password.equals(password)){
                        clearFields(topLeftPanel);
                        window.setGuiUser(newUser);
                        JOptionPane.showMessageDialog(window, "Welcome " + newUser.name + "! You have been logged in", "Logged In", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else{
                        JOptionPane.showMessageDialog(window, "Password Incorrect. Please try again.", "Incorrect Password",JOptionPane.ERROR_MESSAGE);
                    }
                }
                else{
                    JOptionPane.showMessageDialog(window, "User file could not be found. Please check spelling and capitals!", "User Not Found", JOptionPane.ERROR_MESSAGE);
                }

            }
        }
    }


    class signupHandler implements ActionListener{

        private final JTextField signupUNameField;
        private final JPasswordField signupPassField;
        private final JTextField cardNumber;
        private final JComboBox cardChooser;

        public signupHandler(JTextField signupUNameField, JPasswordField signupPassField, JTextField cardNumber, JComboBox cardChooser){
            this.signupUNameField = signupUNameField;
            this.signupPassField = signupPassField;
            this.cardNumber = cardNumber;
            this.cardChooser = cardChooser;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            //check fields to see if they're empty
            if(signupUNameField.getText().isEmpty() ||
                    signupPassField.getPassword().length == 0 ||
                    cardNumber.getText().isEmpty()){
                JOptionPane.showMessageDialog(window, "Please make sure you have filled in all the fields", "Missing Field", JOptionPane.ERROR_MESSAGE);
            }
            else if(cardNumber.getText().trim().length() != 16){
                JOptionPane.showMessageDialog(window, "Card number incorrect (needs to be 16 digits long)", "Incorrect Card Number", JOptionPane.ERROR_MESSAGE);
            }
            else{
                GetUserData saver = new GetUserData();
                String name = signupUNameField.getText();
                String password = new String(signupPassField.getPassword()).trim();
                int balance = 0;
                String[] stocks = {};
                int[] amounts = {};
                String[] bankDetails = {cardNumber.getText().trim(), (String)cardChooser.getSelectedItem()};

                user savedUser = new user(name, balance, stocks, amounts, bankDetails, password);
                saver.save(savedUser);

                JOptionPane.showMessageDialog(window, "Sign-up successful! You have been logged-in", "Sign-up Complete!", JOptionPane.INFORMATION_MESSAGE);
                window.setGuiUser(savedUser);
                //signup complete - clear fields
                clearFields(bottomLeftPanel);
                rightPanel.repaint();
                rightPanel.revalidate();
            }
        }
    }
}


