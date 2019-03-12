// Class Created 23/11/18
// Last Modified 06/02/19 by Henry Lewis

//---[ Class Imports ]---

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

// PDF Jar Class --> Download: https://mvnrepository.com/artifact/com.itextpdf/itextpdf/5.5.13 (just grab Jar file since we don't use maven)


public class user implements java.io.Serializable{
    //to prevent serial number changing...
    private static final long serialVersionUID = 6529685098267757690L;
    // This class holds all useful user information
    String name; // Name of user
    String password;
    Map<String, Integer> stocksIDOwned = new LinkedHashMap<>(); // HashMap holds reference to stocks owned
    Map<String, stock> stockObjOwned = new LinkedHashMap<>();  // HashMap holds stocks owned - Key for both maps is same (stock Code)
    String[] stockCodes; // Hold character codes of the stocks (used as map key)
    bankAccount bank; // Holds bank-account information
    boolean built; // Tells the GUI whether or not to allow user to modify user information
    // any other user-info should be declared here
    Calendar currentStockDate;

    public user(String name, int bankVal, String[] stocks, int[] stockAmounts, String[] bankDetails, String password) {
        //---[ Class Constructor ]---
        // Set basic user values, i.e. user's name & bank account val
        this.name = name;
        this.bank = new bankAccount(bankVal, bankDetails);
        this.password = password;
        currentStockDate = Calendar.getInstance();
        //---[ Build Stock Information ]---
        this.stockCodes = stocks;
        // --> build the HashMaps
        if (stocks.length != stockAmounts.length) {
            throw new RuntimeException("Stocks given does not match the stock numbers given.");
        }
        for (int i = 0; i < stockCodes.length; i++) {
            try {
                addStock(stocks[i], stockAmounts[i], false); // add stocks parsed
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /*
    Function used when creating a user from a load file
     */
    public user buildFromFile(user loadedUser){


        name = loadedUser.name;
        bank = loadedUser.bank;
        password = loadedUser.password;
        stockCodes = loadedUser.stockCodes;
        stocksIDOwned = loadedUser.stocksIDOwned;
        stockObjOwned = loadedUser.stockObjOwned;


//        Thread t = new Thread(() -> { TODO Figure this out
//            SwingUtilities.invokeLater(() -> {
//                JOptionPane loadingPane = new JOptionPane("Loading values");
//                JDialog dialog = loadingPane.createDialog(null, "Hi, " + name + "!");
//                dialog.setVisible(true);
//                dialog.setModal(true);
//            });
//        });
//
//        t.start();

        for(stock stock: stockObjOwned.values()){
            try{
                stock.populate(stock.label);
            }
            catch (IOException e){
                System.out.println("Failed to recreate days");
                e.printStackTrace();
            }
        }
        return this;
    }

    public double[] sellStock(String stockPrefix, int stockAmount, boolean remove) {
        double totalProfit = 0;
        double totalSell = 0;
       if (stocksIDOwned.containsKey(stockPrefix)) {
           // if given stock prefix is actually a held stock

           //prevent the user from selling more than they have
           if(stockAmount > stocksIDOwned.get(stockPrefix)){
               throw new IllegalArgumentException();
           }

           if(remove){
               stock removedStock = stockObjOwned.get(stockPrefix);
               double totalBuy=0;
               double profit;

               stock thisStock = stockObjOwned.get(stockPrefix);
               double stockWorth = thisStock.getPriceOnDay(currentStockDate.get(Calendar.DAY_OF_MONTH),currentStockDate.get(Calendar.MONTH), currentStockDate.get(Calendar.YEAR), true);

               //get total price stocks being sold were bought for
               for(int i =0; i< stockAmount; i++){
                   totalBuy += removedStock.getAndRemoveValue();
               }

               //round for accuracy errors
               totalBuy = round(totalBuy, 5);

               //get total price they are selling for
               totalSell = stockAmount * stockWorth;

               profit = totalSell-totalBuy;

               //only need to add profit because sell amount already added
               bank.addAmount(totalSell);

               totalProfit = profit;
           }

           if (stocksIDOwned.get(stockPrefix) <= stockAmount) {
               // if the amount to remove is more than/equal to amount of stock held
               // completely remove the stock
               stocksIDOwned.remove(stockPrefix);
               stockObjOwned.remove(stockPrefix);
           } else {
               stocksIDOwned.put(stockPrefix, stocksIDOwned.get(stockPrefix) - stockAmount);
               // update prefix-pointer to new amount of stocks
           }
           if(remove && !this.name.equals("")) {
               try {
                   File file = new File("transaction-log.txt"); // Open file
                   boolean header = false;
                   if (!file.exists()) { // If file does not exist
                       file.createNewFile(); // Create file
                       header = true; // Indicates that header line is to be created
                   }
                   FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                   BufferedWriter bw = new BufferedWriter(fw);

                   if (header) {
                       String hLine = "name, bank balance, card type, stock, amount, price, total worth, transaction time";
                       bw.write(hLine);
                       bw.newLine();
                   }

                   // String to write to file
                   //String log = this.name + "," + this.bank.toString() + "," + stockPrefix + "," + stockAmount + java.time.LocalDateTime.now();
                   String log = this.name + "," +this.bank.returnAmount() + "," + this.bank.getType() + "," +stockPrefix + ",-" + stockAmount + "," + totalSell + "," + totalWorth() + currentStockDate.getTime();

                   // Writes to file and adds new line
                   bw.write(log);
                   bw.newLine();

                   // closes file
                   bw.close();
                   fw.close();

               } catch (Exception e) {
                   System.out.println("Exception: " + e + " occured");
               }
           }

       } else {
           System.out.println("Given stock found doesn't match held stock codes");
           throw new IllegalArgumentException();
       }
        return new double[]{totalSell, totalProfit};
    }



    public double addStock(String stockPrefix, int stockAmount, boolean buy) throws Exception {
        // Add a new stock to the HashMaps
        double priceBoughtFor = 0;
        try {
            if (stocksIDOwned.size() <= 5) { // if space for new stock available
                stock thisStock = new stock(stockPrefix); // new stock instance

                //make sure to transfer initial values Queue

                try{
                    thisStock.initialValues = stockObjOwned.get(stockPrefix).initialValues;
                } catch (NullPointerException e){
                    //stock doesn't exist
                }

                //add on top of any previous stock
                //get amount
                int currentAmount = 0;
                if(this.stocksIDOwned.get(stockPrefix) != null){
                    currentAmount += this.stocksIDOwned.get(stockPrefix);
                }


                stocksIDOwned.put(stockPrefix, stockAmount + currentAmount);
                stockObjOwned.put(stockPrefix, thisStock);

                //if we are buying the stock, take some money from the account
                //get the stock price on the selected day


                if(buy){
                    double stockWorth = thisStock.getPriceOnDay(currentStockDate.get(Calendar.DAY_OF_MONTH),currentStockDate.get(Calendar.MONTH), currentStockDate.get(Calendar.YEAR), true);
                    bank.removeAmount(stockWorth * stockAmount);

                    //adds the values for profit calculation
                    for(int i = 0; i < stockAmount; i++){
                        thisStock.addValueToQueue(stockWorth);
                    }
                    priceBoughtFor = stockWorth * stockAmount;
                }

                if (buy && !this.name.equals("")) {
                    try {
                        File file = new File("transaction-log.txt"); // Open file
                        boolean header = false;
                        if (!file.exists()) { // If file does not exist
                            file.createNewFile(); // Create file
                            header = true; // Indicates that header line is to be created
                        }
                        FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                        BufferedWriter bw = new BufferedWriter(fw);

                        if (header) {
                            String hLine = "name, bank balance, card type, stock, amount, price, total worth, transaction time";
                            bw.write(hLine);
                            bw.newLine();
                        }

                        // String to write to file , bank details are not yet present
                        //String log = this.name + "," + this.bank.toString() + "," + stockPrefix + "," + stockAmount + java.time.LocalDateTime.now();
                        String log = this.name + "," + this.bank.returnAmount() + "," + this.bank.getType() + "," +stockPrefix + ",-" + stockAmount + "," + Math.abs(priceBoughtFor) + "," + totalWorth() + "," + currentStockDate.getTime();

                        // Writes to file and adds new line
                        bw.write(log);
                        bw.newLine();

                        // closes file
                        bw.close();
                        fw.close();

                    } catch (Exception e) {
                        System.out.println("Exception: " + e + " occured");
                    }
                }
                System.out.println("New stock added");
            } else {
                System.out.println("No space available for new stock");
                throw new IndexOutOfBoundsException();
            }
        } catch(IOException ex) {
            System.out.println("Could not add stock to user. Invalid stock code. (User)");
            throw new IOException();
        }

        return priceBoughtFor;
    }

    //rounds a double to x dp
    protected static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void setDate(int year, int month, int day){
        currentStockDate.set(year, month, day);
    }

    public double totalWorth() {
        // Return the total worth of users stocks and bank value
        double worth = 0;
        worth = bank.returnAmount();
        Iterator it = stocksIDOwned.entrySet().iterator();
        while (it.hasNext()) { // Iterate through map
            Map.Entry pair = (Map.Entry)it.next(); // Temp. hold HashMap pair (i.e. hold a stock + no. stocks)
            stock thisStock = stockObjOwned.get(pair.getKey());
            double currentStockWorth = thisStock.getPriceOnDay(currentStockDate.get(Calendar.DAY_OF_MONTH), currentStockDate.get(Calendar.MONTH), currentStockDate.get(Calendar.YEAR), false);
            double totalStockWorth = currentStockWorth * (Integer)pair.getValue();
            worth += totalStockWorth;
            // get the worth of today's stock at open and multiply by number of stocks owned
        }
        DecimalFormat df = new DecimalFormat("#.00");
        return Double.parseDouble(df.format(worth));
    }

    public void exportPDF(String location) {
        // Method to write out user info to file
        try {
            // Open new file-stream to write out PDF
            Document pdfDoc = new Document();
            PdfWriter writer = PdfWriter.getInstance(pdfDoc, new FileOutputStream(location + ".pdf")); // Name PDF after user

            pdfDoc.open(); // Open PDF filestream
            writer.open();

            pdfDoc.addAuthor("CE291 Team26");
            pdfDoc.addTitle(name + "'s Stock Portfolio");
            pdfDoc.addSubject("A brief summary of " + name + "'s stock-portfolio based on the IEX Stock Exchange.");


            // Basic User Info
            Paragraph title = new Paragraph(new Phrase(10f, name + "'s portfolio.", FontFactory.getFont(FontFactory.COURIER, 20f)));
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(50);
            title.setSpacingBefore(30);
            pdfDoc.add(title);

            //add current date set
            Paragraph dateInfo = new Paragraph(new Phrase(10f, "Date set: " + currentStockDate.getTime().toString(), FontFactory.getFont(FontFactory.COURIER, 15f)));
            dateInfo.setSpacingAfter(20);
            dateInfo.setSpacingBefore(20);
            pdfDoc.add(dateInfo);

            //add bank info
            Paragraph bankInfo = new Paragraph(20);
            bankInfo.setFont(FontFactory.getFont(FontFactory.COURIER, 15f));
            bankInfo.add(new Chunk("Account balance: " + bank.returnAmountString()));
            bankInfo.add(Chunk.NEWLINE);
            bankInfo.add(new Chunk("Card number: **** **** **** " + bank.getLastFour()));
            bankInfo.add(Chunk.NEWLINE);
            bankInfo.add(new Chunk("Card type: " + bank.getType()));
            bankInfo.add(Chunk.NEWLINE);
            bankInfo.setSpacingAfter(50);

            pdfDoc.add(bankInfo);


            //add the stock info
            if (stocksIDOwned.size() < 1) {
                Paragraph stockPara = new Paragraph("No Stocks owned.");
                pdfDoc.add(stockPara);

            }

            PdfPTable table = new PdfPTable(4);
            table.setSpacingBefore(20);
            float[] colWidth = {2f, 2f, 2f, 2f};
            table.setWidths(colWidth);

            table.addCell(new PdfPCell(new Paragraph("Stock Name")));
            table.addCell(new PdfPCell(new Paragraph("Amount")));
            table.addCell(new PdfPCell(new Paragraph("Current Worth")));
            table.addCell(new PdfPCell(new Paragraph("Total Stock Worth")));
            double totalStockWorth = 0;
            for (stock stock : stockObjOwned.values()) {
                PdfPCell stockName = new PdfPCell(new Paragraph(stock.getName()));
                table.addCell(stockName);
                int stockAmount = stocksIDOwned.get(stock.getName());
                PdfPCell stockAmountCell = new PdfPCell(new Paragraph(Integer.toString(stockAmount)));
                table.addCell(stockAmountCell);
                double currentStockWorth = stock.getPriceOnDay(currentStockDate.get(Calendar.DAY_OF_MONTH), currentStockDate.get(Calendar.MONTH), currentStockDate.get(Calendar.YEAR), false);
                PdfPCell currentWorth = new PdfPCell(new Paragraph(String.format("$%.2f", currentStockWorth)));
                table.addCell(currentWorth);
                PdfPCell totalWorth = new PdfPCell(new Paragraph(String.format("$%.2f", currentStockWorth * stockAmount)));
                table.addCell(totalWorth);

                totalStockWorth += currentStockWorth * stockAmount;
            }
            pdfDoc.add(table);

            //add total stock worth
            Paragraph totalStockWorthPara = new Paragraph(new Phrase(0, String.format("Total stock worth: $%.2f", totalStockWorth), FontFactory.getFont(FontFactory.COURIER, 14f)));
            totalStockWorthPara.setAlignment(Paragraph.ALIGN_CENTER);
            totalStockWorthPara.setSpacingBefore(20);
            pdfDoc.add(totalStockWorthPara);

            //add total worth
            double totalWorthDouble = totalWorth();
            Paragraph totalWorth = new Paragraph(new Phrase(30f, "Total asset worth (inc. bank account): $" + totalWorthDouble, FontFactory.getFont(FontFactory.COURIER, 20f)));
            totalWorth.setAlignment(Paragraph.ALIGN_CENTER);
            totalWorth.setSpacingBefore(100);

            pdfDoc.add(totalWorth);

            pdfDoc.close();
            writer.close();

            System.out.println("PDF Built. (User)");
            JOptionPane.showMessageDialog(null, "PDF saved!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (FileNotFoundException | DocumentException ex) {
            // File errors from FileOutputStream and the PDF Library
            System.out.println("IO Error. Tried to output user info to PDF file. (User)");
            JOptionPane.showMessageDialog(null, "PDF not saved. Please select a different location/name and try again", "PDF Not Saved", JOptionPane.ERROR_MESSAGE);

        }
    }
}
