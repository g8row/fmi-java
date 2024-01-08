package bg.sofia.uni.fmi.mjt.trading;


import bg.sofia.uni.fmi.mjt.trading.price.PriceChartAPI;
import bg.sofia.uni.fmi.mjt.trading.stock.AmazonStockPurchase;
import bg.sofia.uni.fmi.mjt.trading.stock.GoogleStockPurchase;
import bg.sofia.uni.fmi.mjt.trading.stock.MicrosoftStockPurchase;
import bg.sofia.uni.fmi.mjt.trading.stock.StockPurchase;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.SortedMap;

public class Portfolio implements PortfolioAPI{
    String owner;
    PriceChartAPI priceChartAPI;
    StockPurchase[] stockPurchases;
    double budget;
    int maxSize;


    public Portfolio(String owner, PriceChartAPI priceChartAPI, double budget, int maxSize) {
        this.owner = owner;
        this.priceChartAPI = priceChartAPI;
        this.budget = budget;
        this.maxSize = maxSize;
    }

    public Portfolio(String owner, PriceChartAPI priceChartAPI, StockPurchase[] stockPurchases, double budget, int maxSize) {
        this.owner = owner;
        this.priceChartAPI = priceChartAPI;
        this.stockPurchases = stockPurchases;
        this.budget = budget;
        this.maxSize = maxSize;
    }

    @Override
    public StockPurchase buyStock(String stockTicker, int quantity) {
        if(stockPurchases.length >= maxSize){
            return null;
        }
        if (quantity < 1) {
            return null;
        }
        StockPurchase purchase = null;
        if(budget < priceChartAPI.getCurrentPrice(stockTicker)*quantity) {
            return null;
        }
        if(stockTicker == null){
            return null;
        }
        switch (stockTicker) {
            case "MSFT" -> purchase = new MicrosoftStockPurchase(quantity, LocalDateTime.now(), priceChartAPI.getCurrentPrice(stockTicker));
            case "AMZ" -> purchase = new AmazonStockPurchase(quantity, LocalDateTime.now(), priceChartAPI.getCurrentPrice(stockTicker));
            case "GOOG" -> purchase = new GoogleStockPurchase(quantity, LocalDateTime.now(), priceChartAPI.getCurrentPrice(stockTicker));
            default -> {
                return null;
            }
        }

        budget -= priceChartAPI.getCurrentPrice(stockTicker)*quantity;

        priceChartAPI.changeStockPrice(stockTicker,5);

        StockPurchase[] arr = new StockPurchase[stockPurchases.length+1];
        for(int i=0;i<stockPurchases.length;i++){
            arr[i] = stockPurchases[i];
        }
        arr[stockPurchases.length] = purchase;
        stockPurchases = arr;
        return purchase;
    }

    @Override
    public StockPurchase[] getAllPurchases() {
        return stockPurchases;
    }

    @Override
    public StockPurchase[] getAllPurchases(LocalDateTime startTimestamp, LocalDateTime endTimestamp) {
        int count = 0;
        for(StockPurchase purchase:stockPurchases){
            if(purchase.getPurchaseTimestamp().isAfter(startTimestamp) && purchase.getPurchaseTimestamp().isBefore(endTimestamp)){
                count++;
            }
        }
        StockPurchase[] arr = new StockPurchase[count];
        count=0;
        for(StockPurchase purchase:stockPurchases){
            if(purchase.getPurchaseTimestamp().isAfter(startTimestamp) && purchase.getPurchaseTimestamp().isBefore(endTimestamp)){
                arr[count] = purchase;
                count++;
            }
        }
        return arr;
    }

    @Override
    public double getNetWorth() {
        double net = 0;
        for (StockPurchase purchase:stockPurchases){
            net += purchase.getQuantity()*priceChartAPI.getCurrentPrice(purchase.getStockTicker());
        }
        return net;
    }

    @Override
    public double getRemainingBudget() {
        return Math.round(budget*100)/100.0;
    }

    @Override
    public String getOwner() {
        return owner;
    }
}
