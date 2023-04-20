import java.sql.Date;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeMap;

public class CalculationStrategy {

    private Date fromDate;
    private Date tillDate;
    private String secId;
    private int rateFirstEMA;
    private int rateSecondEMA;
    private double tax;
    private Date addDate;
    private Double addPrice;

    public CalculationStrategy(Date fromDate, Date tillDate, String secId, int rateFirstEMA, int rateSecondEMA, double tax) {
        this.fromDate = fromDate;
        this.tillDate = tillDate;
        this.secId = secId.toUpperCase(Locale.ROOT);
        if (rateFirstEMA > rateSecondEMA) {
            this.rateFirstEMA = rateSecondEMA;
            this.rateSecondEMA = rateFirstEMA;
        } else {
            this.rateFirstEMA = rateFirstEMA;
            this.rateSecondEMA = rateSecondEMA;
        }
        this.tax = tax / 100;
    }

    public ArrayList<String> getResult() {
        ArrayList<String> resultList = new ArrayList<>();
        if (!GetInfoSecurities.getInfo().containsKey(secId)){
            resultList.add("0.0%");
            resultList.add("0.0%");
            return resultList;
        }
        TreeMap<Date, Double> historySecurity = new ParseLocalHistorySecurity(secId).parse();
        if (addDate != null && addPrice != null){
            historySecurity.put(addDate, addPrice);
        }
        TreeMap<Date, Double> firstEMA = new CalculationEMA().getEMA(historySecurity, rateFirstEMA);
        TreeMap<Date, Double> secondEMA = new CalculationEMA().getEMA(historySecurity, rateSecondEMA);
        TreeMap<Date, ArrayList<Double>> strategy = new TreeMap<>();
        Date previousDate = historySecurity.firstKey();
        for (Date d : historySecurity.keySet()) {
            if (d.before(fromDate) || d.after(tillDate)) continue;
            if (firstEMA.get(d) == null || secondEMA.get(d) == null ||
                    firstEMA.get(previousDate) == null || secondEMA.get(previousDate) == null) {
                previousDate = d;
                continue;
            }
            if (secondEMA.get(d) > firstEMA.get(d) && secondEMA.get(previousDate) <= firstEMA.get(previousDate)) {
                ArrayList<Double> costAndAction = new ArrayList<>();
                costAndAction.add(historySecurity.get(d));
                costAndAction.add(0.0);
                strategy.put(d, costAndAction);
                previousDate = d;
            }
            if (secondEMA.get(d) < firstEMA.get(d) && secondEMA.get(previousDate) >= firstEMA.get(previousDate)) {
                ArrayList<Double> costAndAction = new ArrayList<>();
                costAndAction.add(historySecurity.get(d));
                costAndAction.add(1.0);
                strategy.put(d, costAndAction);
                previousDate = d;
            }
        }
        if (strategy.isEmpty()) {
            resultList.add("0.0%");
            resultList.add("0.0%");
            return resultList;
        }
        if (strategy.get(strategy.firstKey()).get(1).equals(0.0)) strategy.remove(strategy.firstKey());
        double money = 1;
        double priceBuy = 0;
        for (Date d : strategy.keySet()) {
            if (strategy.get(d).get(1).equals(1.0)) {
                priceBuy = strategy.get(d).get(0);
            } else {
                money = strategy.get(d).get(0) < priceBuy ?
                        money / priceBuy * strategy.get(d).get(0) :
                        money * ((1 - tax) * strategy.get(d).get(0) / priceBuy + tax);
            }
            if (strategy.get(d).get(1).equals(1.0) && strategy.lastKey().equals(d)) {
                money = strategy.get(d).get(0) < priceBuy ?
                        money / priceBuy * strategy.get(d).get(0) :
                        money * ((1 - tax) * strategy.get(d).get(0) / priceBuy + tax);
            }
        }
        resultList.add((Math.round((money - 1) * 100 * 100) / 100.0) + "%");
        long countDays = (tillDate.getTime() - fromDate.getTime()) / 86400000 + 1;
        resultList.add((Math.round((money - 1) * 100 * 100 * 365 / countDays) / 100.0) + "%");
        return resultList;
    }

    public void addData(String date, String price){
        addDate = Date.valueOf(date);
        addPrice = Double.parseDouble(price);
    }
}
