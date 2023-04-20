import java.sql.Date;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeMap;

public class CalculationEMACurrentDate {

    private String secId;
    private int rateFirstEMA;
    private int rateSecondEMA;
    private Date addDate;
    private Double addPrice;

    public CalculationEMACurrentDate(String secId, int rateFirstEMA, int rateSecondEMA) {
        this.secId = secId.toUpperCase(Locale.ROOT);
        if (rateFirstEMA > rateSecondEMA) {
            this.rateFirstEMA = rateSecondEMA;
            this.rateSecondEMA = rateFirstEMA;
        } else {
            this.rateFirstEMA = rateFirstEMA;
            this.rateSecondEMA = rateSecondEMA;
        }
    }

    public ArrayList<String> getEMAInfo() {
        TreeMap<Date, Double> historySecurity = new ParseLocalHistorySecurity(secId).parse();
        if (addDate != null && addPrice != null) {
            historySecurity.put(addDate, addPrice);
        }
        TreeMap<Date, Double> firstEMA = new CalculationEMA().getEMA(historySecurity, rateFirstEMA);
        TreeMap<Date, Double> secondEMA = new CalculationEMA().getEMA(historySecurity, rateSecondEMA);
        ArrayList<String> result = new ArrayList<>();
        double alfaFirstEMA = 2.0 / (rateFirstEMA + 1);
        double alfaSecondEMA = 2.0 / (rateSecondEMA + 1);
        if (firstEMA.get(firstEMA.lastKey()) > secondEMA.get(secondEMA.lastKey())) {
            result.add(secId + "\n");
            result.add("Дата: " + firstEMA.lastKey() + "\n");
            result.add("EMA-" + rateFirstEMA + ": " + Math.round(firstEMA.get(firstEMA.lastKey()) * 100_000) / 100_000.0 + "\n");
            result.add("EMA-" + rateSecondEMA + ": " + Math.round(secondEMA.get(secondEMA.lastKey()) * 100_000) / 100_000.0 + "\n");
            result.add("Ценная бумага показывает РОСТ\n");
            double deltaPrice = historySecurity.get(historySecurity.lastKey()) -
                    ((1 - alfaSecondEMA) * secondEMA.get(secondEMA.lastKey()) -
                            (1 - alfaFirstEMA) * firstEMA.get(firstEMA.lastKey())) / (alfaFirstEMA - alfaSecondEMA);
            deltaPrice = Math.round(deltaPrice * 100_000) / 100_000.0;
            if (deltaPrice < 0) {
                result.add("Для смены тренда цена не должна быть выше " + (historySecurity.get(historySecurity.lastKey()) - deltaPrice) +
                        "\nпри текущей цене бумаги " + historySecurity.get(historySecurity.lastKey()) + "\n");
            } else {
                result.add("Для смены тренда цена бумаги должна уменьшиться на " + deltaPrice +
                        "\nпри текущей цене бумаги " + historySecurity.get(historySecurity.lastKey()) + "\n");
            }
            return result;
        }
        if (firstEMA.get(firstEMA.lastKey()) < secondEMA.get(secondEMA.lastKey())) {
            result.add(secId + "\n");
            result.add("Дата: " + firstEMA.lastKey() + "\n");
            result.add("EMA-" + rateFirstEMA + ": " + Math.round(firstEMA.get(firstEMA.lastKey()) * 100_000) / 100_000.0 + "\n");
            result.add("EMA-" + rateSecondEMA + ": " + Math.round(secondEMA.get(secondEMA.lastKey()) * 100_000) / 100_000.0 + "\n");
            result.add("Ценная бумага показывает ПАДЕНИЕ\n");
            double deltaPrice = ((1 - alfaSecondEMA) * secondEMA.get(secondEMA.lastKey()) -
                    (1 - alfaFirstEMA) * firstEMA.get(firstEMA.lastKey())) / (alfaFirstEMA - alfaSecondEMA) -
                    historySecurity.get(historySecurity.lastKey());
            deltaPrice = Math.round(deltaPrice * 100_000) / 100_000.0;
            if (deltaPrice < 0){
                result.add("Для смены тренда цена не должна быть ниже " + (historySecurity.get(historySecurity.lastKey()) + deltaPrice) +
                        "\nпри текущей цене бумаги " + historySecurity.get(historySecurity.lastKey()) + "\n");
            } else {
                result.add("Для смены тренда цена бумаги должна увеличиться на " + deltaPrice +
                        "\nпри текущей цене бумаги " + historySecurity.get(historySecurity.lastKey()) + "\n");
            }
            return result;
        }
        Date previousLastDate = (Date) firstEMA.keySet().toArray()[firstEMA.values().toArray().length - 2];
        if (firstEMA.get(previousLastDate) < secondEMA.get(previousLastDate)) {
            result.add(secId + "\n");
            result.add("Дата: " + firstEMA.lastKey() + "\n");
            result.add("EMA-" + rateFirstEMA + ": " + firstEMA.get(firstEMA.lastKey()) + "\n");
            result.add("EMA-" + rateSecondEMA + ": " + secondEMA.get(secondEMA.lastKey()) + "\n");
            result.add("Ценная бумага показывает РОСТ\n");
            result.add("В данный момент происходит смена тренда\n");
            return result;
        } else {
            result.add(secId + "\n");
            result.add("Дата: " + firstEMA.lastKey() + "\n");
            result.add("EMA-" + rateFirstEMA + ": " + firstEMA.get(firstEMA.lastKey()) + "\n");
            result.add("EMA-" + rateSecondEMA + ": " + secondEMA.get(secondEMA.lastKey()) + "\n");
            result.add("Ценная бумага показывает ПАДЕНИЕ\n");
            result.add("В данный момент происходит смена тренда\n");
            return result;
        }
    }

    public void addData(String date, String price) {
        addDate = Date.valueOf(date);
        addPrice = Double.parseDouble(price);
    }
}
