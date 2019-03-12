import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.TimeUnit;


/*
StockBlock class - each JPanel is created from info on a stock, which is then added onto the main panel
 */
class StockBlock extends JPanel{
    private double prevPrice;
    private double currentPrice;
    private String stockName;

    private  JLabel stockNameLabel;
    private  JLabel stockPriceLabel;
    private  JLabel stockChangeLabel;


    public String getStockName(){return stockName;}

    public void setPrice(double price){

        this.prevPrice = currentPrice;
        this.currentPrice = price;

        double change = currentPrice - prevPrice;
        double percentChange = change / prevPrice * 100;

        DecimalFormat formatter = new DecimalFormat("#0.00");

        stockPriceLabel.setText(Double.toString(currentPrice));
        stockChangeLabel.setText(formatter.format(percentChange)+"%");

        if(currentPrice > prevPrice){
            stockPriceLabel.setForeground(new Color(0x1FB700));
        }
        else if (currentPrice < prevPrice){
            stockPriceLabel.setForeground(Color.red);
        }
        else{
            stockPriceLabel.setForeground(new Color(200, 116, 1));
        }
    }


    public StockBlock(stock stock){
        setLayout(new BorderLayout());
        this.prevPrice = stock.getWorth();
        this.currentPrice = stock.getWorth();
        this.stockName = stock.getName();

        setBorder(new EmptyBorder(30, 30, 30, 30));
        stockNameLabel = new JLabel(stock.getName(), SwingConstants.CENTER);
        stockNameLabel.setFont(stockNameLabel.getFont().deriveFont(30f));
        stockPriceLabel = new JLabel("£"+stock.getWorth(), SwingConstants.CENTER);
        stockPriceLabel.setFont(stockPriceLabel.getFont().deriveFont(30f));
        stockPriceLabel.setBorder(new EmptyBorder(20,0,20,0));
        stockChangeLabel = new JLabel("0", SwingConstants.CENTER);
        stockChangeLabel.setFont(stockChangeLabel.getFont().deriveFont(20f));
        add(stockNameLabel, BorderLayout.NORTH);
        add(stockPriceLabel, BorderLayout.CENTER);
        add(stockChangeLabel, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension arcs = new Dimension(5, 5);
        int width = getWidth();
        int height = getHeight();

        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(0xC1C1C1));
        g2d.fillRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);
    }
}
/*
Main panel that has all StockBlock panels on it.
 */
public class LivePricePanel extends JPanel {
    private user guiUser;
    private ArrayList<StockBlock> stockBlockList = new ArrayList<>();

    //how often the stocks refresh in milliseconds
    private int refreshRate;

    public LivePricePanel(user guiUser){
        setLayout(new FlowLayout(FlowLayout.CENTER, 30, 15));

        this.guiUser = guiUser;

        this.refreshRate = 5000;

        //function to create StockBlock panels for every stock owned.
        addStockBlocks();


        //Check prices every x seconds
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                setPrices();
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 1000, refreshRate);
    }

    private void addStockBlocks(){

        //for every stock, create a block with that stocks info
        for(String stock : guiUser.stockObjOwned.keySet()){
            StockBlock block = new StockBlock(guiUser.stockObjOwned.get(stock));
            add(block);
            stockBlockList.add(block);
        }

        Calendar now = Calendar.getInstance();
        Calendar openTime = Calendar.getInstance();
        openTime.set(Calendar.HOUR_OF_DAY, 14);
        openTime.set(Calendar.MINUTE, 30);
        openTime.set(Calendar.SECOND, 0);

        //if its closed today, add a day
        if(now.get(Calendar.HOUR_OF_DAY) > 21){
            openTime.add(Calendar.DATE, 1);
        }
        //If we are out of market hours, show a countdown until the market is open again.
        if(now.get(Calendar.HOUR_OF_DAY) > 21 || now.compareTo(openTime) < 0){
            JLabel tooLateLab = new JLabel("Stock market is now closed.", SwingConstants.CENTER);
            tooLateLab.setFont(tooLateLab.getFont().deriveFont(25f));
            //update the market opening time every second.
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    now.setTime(Calendar.getInstance().getTime());
                    long nowMillis = now.getTimeInMillis();
                    long openMillis = openTime.getTimeInMillis();

                    long difference =   Math.abs(nowMillis - openMillis);

                    String timeUntil = String.format("%dh %02dm %02ds",
                            TimeUnit.MILLISECONDS.toHours(difference),
                            (TimeUnit.MILLISECONDS.toMinutes(difference) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(difference))),
                            TimeUnit.MILLISECONDS.toSeconds(difference)
                                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(difference)));
                    tooLateLab.setText("<html>Stock market is now closed.<br>Reopening in: <strong>"
                            +timeUntil+"</strong></html>");

                    /*
                    When the market opens, get rid of the label and stop the timer
                     */
                    if(now.compareTo(openTime) >= 0){
                        remove(tooLateLab);
                        repaint();
                        revalidate();
                        this.cancel();
                    }
                }
            };
            Timer t = new Timer();
            t.scheduleAtFixedRate(task, 0, 500);
            add(tooLateLab);
        }

        if(guiUser.stockObjOwned.keySet().isEmpty()){
            JLabel noStockLab = new JLabel("Please buy a stock to have it displayed here", SwingConstants.CENTER);
            noStockLab.setFont(noStockLab.getFont().deriveFont(30f));
            noStockLab.setForeground(Color.red);
            noStockLab.setBorder(new EmptyBorder(65,0,0,0));
            add(noStockLab);
        }
        //if there are stock, show some info on how often stock are updated.
        else{
            JLabel infoLab = new JLabel("(refreshed every " + refreshRate/1000 + " seconds)", SwingConstants.CENTER);
            infoLab.setForeground(Color.gray);
            add(infoLab);
        }
    }

    /*
    This function is called every 5 seconds by the TimerTask timer
    It fetches the price in the background for each stock and then calls
     */
    public void setPrices(){

        for(StockBlock block : stockBlockList) {
            SwingWorker<Double, Double> getPriceInBackground = new SwingWorker<Double, Double>() {

                @Override
                protected Double doInBackground()   {
                    return guiUser.stockObjOwned.get(block.getStockName()).getWorth();
                }

                @Override
                protected void done() {
                    try {
                        block.setPrice(get());
                    } catch (Exception e) {
                        System.out.println("Problem setting price after fetching: ");
                    }
                }
            };
            getPriceInBackground.execute();
        }
    }



    /*
    Function called on stock purchase/removal
     */
    public void update(){
        /*
        Clear all components
         */
        Component[] elements = this.getComponents();
        for (Component comp : elements) {
            remove(comp);
        }
        stockBlockList = new ArrayList<>();

        //add them back again
        addStockBlocks();
    }
}
