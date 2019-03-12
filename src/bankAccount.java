//Created: 07/11/2018 by Henry Lewis
//Last Modified: 29/11/2018 by Henry Lewis

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class bankAccount implements Serializable {
     private static final long serialVersionUID = 6529685098267757690L;

     private double balance;
     private String cardNumber; // Redundant, decoration variables
     private String cardType;

     public bankAccount(int startAmount, String[] details) {
         // Bank class constructor
        this.balance = startAmount;
        this.cardNumber = details[0];
        this.cardType = details[1];
    }

    public bankAccount(ArrayList<String> details) {
         // Alternative constructor using arraylist
        this.balance = Integer.parseInt(details.get(0));
        this.cardNumber = details.get(1);
        this.cardType = details.get(2);
    }


    //method setCardNumber + method setCardType = setCardDetails
    //the method for JUnit Test
    public String setCardNumber(String cardNumber){
        this.cardNumber=cardNumber;
        return this.cardNumber;
    }

    //the method for JUnit Test
    public String setCardType(String cardType){
        this.cardType=cardType;
        return this.cardType;
    }


    public double addAmount(double addAmount) {
        // increment value of bank-account
        this.balance += addAmount;
        return balance;
    }

    public double removeAmount(double subAmount) {
         // decrement value of bank-account
        this.balance -= subAmount;
        return balance;
    }

    public double returnAmount() {
         // returns value of bank-account
        return balance;
    }

    public String returnAmountString(){
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        if(balance == 0){
            return "$0";
        }
        else{
            return "$" + nf.format(balance);
        }
    }

    public String getLastFour(){
         return this.cardNumber.substring(12);
    }
    public String getType(){
         return this.cardType;
    }

    public void setCardDetails(String cardNumber, String cardType){
         this.cardNumber = cardNumber;
         this.cardType = cardType;
    }

    public ArrayList returnDetails() {
         // return details in arraylist for easier file writing/ reading

        ArrayList<String> details = new ArrayList<>();
        details.add(String.valueOf(balance));
        details.add(cardNumber);
        details.add(cardType);
        return details;
    }

    @Override
    public String toString() {
        // Enable bank-account to return a String containing all information
        if (cardNumber.length() > 0) {
            String newNumber = "**** **** **** " + cardNumber.substring(12, 16);
            return ("<html><center>Bank-Account Summary: <br>£" + user.round(this.balance, 2) + " available. <br>Card Number: " + newNumber + " <br>Card Type: " + cardType + "</center></html>");
        } else {
            return ("<html><center><h2>No Bank Account</h2></center></html>");
        }
    }
}
