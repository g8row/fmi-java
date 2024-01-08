package bg.sofia.uni.fmi.mjt.trading.stock;

import java.time.LocalDateTime;

abstract public class StockPurchaseBase implements StockPurchase{
    int quantity;
    LocalDateTime purchaseTimestamp;
    double purchasePricePerUnit;
    String stockTicker;

    public StockPurchaseBase(int quantity, LocalDateTime purchaseTimestamp, double purchasePricePerUnit, String stockTicker) {
        this.quantity = quantity;
        this.purchaseTimestamp = purchaseTimestamp;
        this.purchasePricePerUnit = purchasePricePerUnit;
        this.stockTicker = stockTicker;
    }


    @Override
    public int getQuantity() {
        return quantity;
    }

    @Override
    public LocalDateTime getPurchaseTimestamp() {
        return purchaseTimestamp;
    }

    @Override
    public double getPurchasePricePerUnit() {
        return purchasePricePerUnit;
    }

    @Override
    public double getTotalPurchasePrice() {
        return purchasePricePerUnit*quantity;
    }

    @Override
    public String getStockTicker() {
        return stockTicker;
    }
}
