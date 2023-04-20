import java.sql.Date;
import java.util.TreeMap;

public class CalculationEMA {

    double alfa;

    public CalculationEMA(){
    }

    public CalculationEMA(double alfa){
        this.alfa = alfa;
    }

    public TreeMap<Date, Double> getEMA(TreeMap<Date, Double> history, int rate){
        double sum = 0;
        for (int i = 0; i < rate; i++) {
            sum += (double) history.values().toArray()[i];
        }
        double SMA = sum / rate;
        TreeMap<Date, Double> EMA = new TreeMap<>();
        for (int i = 0; i < rate; i++){
            EMA.put((Date) history.keySet().toArray()[i], null);
        }
        EMA.put((Date) history.keySet().toArray()[rate], SMA);
        if (alfa == 0.0)
        alfa = 2.0 / (rate + 1);
        Date previousDate = (Date) history.keySet().toArray()[rate];
        for (int i = rate + 1; i < history.size(); i++) {
            double value = (double) history.values().toArray()[i] * alfa + (1 - alfa) * EMA.get(previousDate);
            previousDate = (Date) history.keySet().toArray()[i];
            EMA.put(previousDate, value);
        }
        return EMA;
    }
}
