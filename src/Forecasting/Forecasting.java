package Forecasting;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

public class Forecasting {
    private double coefficientM;
    private double coefficientB;
    private TreeMap<Date, Double> history;
    private OptimizationCoefficients optimizationCoefficients;
    private int sizeSeasonality;
    private double firstAutocorrelation;

    public Forecasting(TreeMap<Date, Double> history) {
        this.history = history;
        this.optimizationCoefficients = new OptimizationCoefficients(history);
    }

    public TreeMap<Integer, Double> simpleHolt() {
        ArrayList<Double> level = new ArrayList<>();
        level.add(history.get(history.firstKey()));
        ArrayList<Double> oneStepForecast = new ArrayList<>();
        oneStepForecast.add(0.0);
        ArrayList<Double> forecastError = new ArrayList<>();
        forecastError.add(0.0);
        int backStep = 0;
        double alfa = optimizationCoefficients.getOptimizationCoefficientSimple();
        for (Date date : history.keySet()) {
            oneStepForecast.add(level.get(backStep));
            forecastError.add(history.get(date) - oneStepForecast.get(backStep + 1));
            level.add(level.get(backStep) + alfa * forecastError.get(backStep + 1));
            backStep++;
        }
        TreeMap<Integer, Double> resultSimpleHolt = new TreeMap<>();
        for (int i = 1; i <= 10; i++) {
            resultSimpleHolt.put(i, level.get(level.size() - 1));
        }
        return resultSimpleHolt;
    }

    public TreeMap<Integer, Double> trendHolt() {
        if (!isTrend()) {
            return new TreeMap<>();
        }
        double alfa = optimizationCoefficients.getOptimizationCoefficientsTrend()[0];
        double gamma = optimizationCoefficients.getOptimizationCoefficientsTrend()[1];
        ArrayList<Double> level = new ArrayList<>();
        level.add(history.get(history.firstKey()));
        ArrayList<Double> trend = new ArrayList<>();
        trend.add(0.0);
        ArrayList<Double> oneStepForecast = new ArrayList<>();
        oneStepForecast.add(0.0);
        ArrayList<Double> forecastError = new ArrayList<>();
        forecastError.add(0.0);
        int backStep = 0;
        for (Date date : history.keySet()) {
            oneStepForecast.add(level.get(backStep) + trend.get(backStep));
            forecastError.add(history.get(date) - oneStepForecast.get(backStep + 1));
            level.add(level.get(backStep) + trend.get(backStep) + alfa * forecastError.get(backStep + 1));
            trend.add(trend.get(backStep) + alfa * gamma * forecastError.get(backStep + 1));
            backStep++;
        }
        TreeMap<Integer, Double> resultTrendHolt = new TreeMap<>();
        for (int i = 1; i <= 10; i++) {
            resultTrendHolt.put(i, level.get(level.size() - 1) + trend.get(trend.size() - 1) * i);
        }
        return resultTrendHolt;
    }

    public TreeMap<Integer, Double> seasonalityHolt() {
        if (!isSeasonality()) {
            return new TreeMap<>();
        }
        TreeMap<Integer, Double> sequence = new TreeMap<>();
        int number = 1;
        for (Date date : history.keySet()) {
            sequence.put(number, history.get(date));
            number++;
        }
        TreeMap<Integer, Double> smoothed = new TreeMap<>();
        if (sizeSeasonality % 2 == 0) {
            for (int i : sequence.keySet()) {
                if (i == sequence.size() - sizeSeasonality + 1) break;
                double sum1 = 0;
                double sum2 = 0;
                for (int j = i; j <= i + sizeSeasonality - 1; j++) {
                    sum1 += sequence.get(j);
                }
                for (int j = i + 1; j <= i + sizeSeasonality; j++) {
                    sum2 += sequence.get(j);
                }
                smoothed.put(sizeSeasonality / 2 + i, (sum1 / sizeSeasonality + sum2 / sizeSeasonality) / 2);
            }
        } else {
            for (int i : sequence.keySet()) {
                if (i == sequence.size() - sizeSeasonality + 2) break;
                double sum = 0;
                for (int j = i; j <= i + sizeSeasonality - 1; j++) {
                    sum += sequence.get(j);
                }
                smoothed.put(sizeSeasonality / 2 + i, sum / sizeSeasonality);
            }
        }
        TreeMap<Integer, Double> seasonalFactor = new TreeMap<>();
        for (int i : smoothed.keySet()) {
            seasonalFactor.put(i, sequence.get(i) / smoothed.get(i));
        }
        TreeMap<Integer, Double> initialSeasonalFactor = new TreeMap<>();
        for (int i = 1; i <= sizeSeasonality; i++) {
            double sum = 0;
            int count = 0;
            for (int j = 0; j < seasonalFactor.size(); j++) {
                if (seasonalFactor.containsKey(j * sizeSeasonality + i)) {
                    sum += seasonalFactor.get(j * sizeSeasonality + i);
                    count++;
                }
            }
            initialSeasonalFactor.put(i, sum / count);
        }
        TreeMap<Integer, Double> initialSeasonalFactorFull = new TreeMap<>();
        number = 1;
        for (int i : sequence.keySet()){
            initialSeasonalFactorFull.put(i, initialSeasonalFactor.get(number));
            number++;
            if (number == sizeSeasonality + 1) number = 1;
        }
        TreeMap<Integer, Double> deseasonalizedSequence = new TreeMap<>();
        for (int i : sequence.keySet()){
            deseasonalizedSequence.put(i, sequence.get(i) / initialSeasonalFactorFull.get(i));
        }

        //Получаем уравнение линейной регрессии Y = coefficientM * X + coefficientB для десезонированных данных

        double sumX = 0;
        double sumY = 0;
        for (int i : deseasonalizedSequence.keySet()) {
            sumX += i;
            sumY += deseasonalizedSequence.get(i);
        }
        double averageX = sumX / deseasonalizedSequence.lastKey();
        double averageY = sumY / deseasonalizedSequence.lastKey();
        double tempVar1 = 0;
        for (int i : deseasonalizedSequence.keySet()) {
            tempVar1 += (i - averageX) * (deseasonalizedSequence.get(i) - averageY);
        }
        double tempVar2 = 0;
        for (int i : deseasonalizedSequence.keySet()) {
            tempVar2 += (i - averageX) * (i - averageX);
        }
        double coefficientMDes = tempVar1 / tempVar2;
        double coefficientBDes = averageY - coefficientMDes * averageX;
        Double[] optimCoefficients = optimizationCoefficients
                .getOptimizationCoefficientsSeasonality(coefficientBDes, coefficientMDes, initialSeasonalFactor);
        double alfa = optimCoefficients[0];
        double gamma = optimCoefficients[1];
        double delta = optimCoefficients[2];
        ArrayList<Double> forecastErrorCheck = optimizationCoefficients.getForecastErrorMinSeasonality();
        ArrayList<Double> autocorrelation = getAutocorrelation(forecastErrorCheck);
        autocorrelation.sort(Comparator.reverseOrder());
        if (firstAutocorrelation < autocorrelation.get(0)) return new TreeMap<>();
        ArrayList<Double> level = new ArrayList<>();
        level.add(coefficientBDes);
        ArrayList<Double> trend = new ArrayList<>();
        trend.add(coefficientMDes);
        ArrayList<Double> seasonal = new ArrayList<>(initialSeasonalFactor.values());
        ArrayList<Double> oneStepForecast = new ArrayList<>();
        oneStepForecast.add(0.0);
        ArrayList<Double> forecastError = new ArrayList<>();
        forecastError.add(0.0);
        int backStep = 0;
        for (Date date : history.keySet()) {
            oneStepForecast.add((level.get(backStep) + trend.get(backStep)) * seasonal.get(backStep));
            forecastError.add(history.get(date) - oneStepForecast.get(backStep + 1));
            level.add(level.get(backStep) + trend.get(backStep) + alfa * forecastError.get(backStep + 1) / seasonal.get(backStep));
            trend.add(trend.get(backStep) + alfa * gamma * forecastError.get(backStep + 1) / seasonal.get(backStep));
            seasonal.add(seasonal.get(backStep) + delta * (1 - alfa) * forecastError.get(backStep + 1) /
                    (level.get(backStep) + trend.get(backStep)));
            backStep++;
        }
        TreeMap<Integer, Double> resultSeasonalityHolt = new TreeMap<>();
        for (int i = 1; i <= 10; i++) {
            if (seasonal.size() - 1 < backStep) break;
            resultSeasonalityHolt.put(i, (level.get(level.size() - 1) + trend.get(trend.size() - 1) * i) * seasonal.get(backStep));
            backStep++;
        }
        return resultSeasonalityHolt;
    }

    private boolean isSeasonality() {
        ArrayList<Double> forecastError;
        if (isTrend()) {
            forecastError = optimizationCoefficients.getForecastErrorMinTrend();
        } else {
            forecastError = optimizationCoefficients.getForecastErrorMinSimple();
        }
        forecastError.remove(0);
        ArrayList<Double> autocorrelation = getAutocorrelation(forecastError);
        TreeMap<Integer, Double> seasonality = new TreeMap<>();
        for (int i = 0; i < autocorrelation.size(); i++) {
            seasonality.put(i + 1, autocorrelation.get(i));
        }
        TreeMap<Integer, Double> sortSeasonality = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if (seasonality.get(o1).equals(seasonality.get(o2)))
                    return o2.compareTo(o1);
                return seasonality.get(o2).compareTo(seasonality.get(o1));
            }
        });
        for (int i : seasonality.keySet()) {
            sortSeasonality.put(i, seasonality.get(i));
        }
        sizeSeasonality = sortSeasonality.firstKey();
        firstAutocorrelation = sortSeasonality.get(sortSeasonality.firstKey());
        if (sortSeasonality.get(sortSeasonality.firstKey()) >= 2.0 / history.size()) {
            return true;
        }
        return false;
    }

    private boolean isTrend() {

        //Получаем уравнение линейной регрессии Y = coefficientM * X + coefficientB

        TreeMap<Integer, Double> sequence = new TreeMap<>();
        int number = 1;
        for (Date date : history.keySet()) {
            sequence.put(number, history.get(date));
            number++;
        }
        double sumX = 0;
        double sumY = 0;
        for (int i : sequence.keySet()) {
            sumX += i;
            sumY += sequence.get(i);
        }
        double averageX = sumX / sequence.lastKey();
        double averageY = sumY / sequence.lastKey();
        double tempVar1 = 0;
        for (int i : sequence.keySet()) {
            tempVar1 += (i - averageX) * (sequence.get(i) - averageY);
        }
        double tempVar2 = 0;
        for (int i : sequence.keySet()) {
            tempVar2 += (i - averageX) * (i - averageX);
        }
        coefficientM = tempVar1 / tempVar2;
        coefficientB = averageY - coefficientM * averageX;

        //Вычисляем t-критерий Стьюдента

        double tempVar3 = 0;
        for (int i : sequence.keySet()) {
            tempVar3 += i * i;
        }
        double standardDeviationX = Math.sqrt(tempVar3 / sequence.lastKey() - averageX * averageX);
        double tempVar4 = 0;
        for (int i : sequence.keySet()) {
            tempVar4 += Math.pow(sequence.get(i) - (i * coefficientM + coefficientB), 2);
        }
        double standardRegressionError = Math.sqrt(tempVar4 / (sequence.lastKey() - 2));
        double standardDeviationM = standardRegressionError / (Math.sqrt(sequence.lastKey()) * standardDeviationX);
        double tTest = coefficientM / standardDeviationM;
        if (Math.abs(tTest) > 1.96) return true;
        return false;
    }
    private ArrayList<Double> getAutocorrelation(ArrayList<Double> forecastError){
        double tempVar1 = 0;
        for (int i = 0; i < forecastError.size(); i++) {
            tempVar1 += forecastError.get(i);
        }
        double tempVar2 = tempVar1 / forecastError.size();
        ArrayList<Double> error = new ArrayList<>();
        for (int i = 0; i < forecastError.size(); i++) {
            error.add(forecastError.get(i) - tempVar2);
        }
        double sumSquaredDeviationsMain = 0;
        for (int i = 0; i < error.size(); i++) {
            sumSquaredDeviationsMain += Math.pow(error.get(i), 2);
        }
        ArrayList<Double> autocorrelation = new ArrayList<>();
        for (int i = 0; i < error.size(); i++) {
            double tempVar3 = 0;
            ArrayList<Double> tempList = (ArrayList<Double>) error.clone();
            for (int j = 0; j <= i; j++) {
                tempList.add(j, 0.0);
            }
            for (int j = 0; j < error.size(); j++) {
                tempVar3 += error.get(j) * tempList.get(j);
            }
            autocorrelation.add(Math.abs(tempVar3 / sumSquaredDeviationsMain));
        }
        return autocorrelation;
    }
}
