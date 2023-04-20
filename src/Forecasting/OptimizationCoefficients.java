package Forecasting;

import java.sql.Date;
import java.util.ArrayList;
import java.util.TreeMap;

class OptimizationCoefficients {
    private TreeMap<Date, Double> history;
    private ArrayList<Double> forecastErrorMinSimple;
    private ArrayList<Double> forecastErrorMinTrend;
    private ArrayList<Double> forecastErrorMinSeasonality;
    private Double optimizationCoefficientSimple;
    private Double[] optimizationCoefficientsTrend;
    private Double[] optimizationCoefficientsSeasonality;

    public OptimizationCoefficients(TreeMap<Date, Double> history) {
        this.history = history;
    }

    private void optimizationForSimpleHolt() {
        double epsilon = 1.0E-9;
        double coefficientK = 1;
        double alfaOptima;
        double alfa0 = 0.5;
        long startTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startTime > 5_000) {
                startTime = System.currentTimeMillis();
                coefficientK *= 10;
            }
            double sum0 = sumSquaredDeviationsSimple(alfa0);
            double sumTemp = sumSquaredDeviationsSimple(alfa0 + epsilon);
            double grad0 = (sumTemp - sum0) / epsilon;
            double alfa1 = alfa0 - coefficientK * grad0;
            if (alfa1 < 0 || alfa1 > 1) {
                coefficientK /= 10;
                continue;
            }
            double sum1 = sumSquaredDeviationsSimple(alfa1);
            if (sum1 < sum0) {
                if (Math.abs(alfa1 - alfa0) < epsilon) {
                    alfaOptima = alfa1;
                    sumSquaredDeviationsSimple(alfaOptima);
                    break;
                }
                alfa0 = alfa1;
            } else {
                if (coefficientK == 0.0) {
                    alfaOptima = alfa0;
                    sumSquaredDeviationsSimple(alfaOptima);
                    break;
                }
                coefficientK /= 10;
            }
        }
        optimizationCoefficientSimple = alfaOptima;
    }

    private void optimizationForTrendHolt() {
        double epsilon = 1.0E-9;
        Double[] coefficientK = new Double[2];
        coefficientK[0] = 1.0;
        coefficientK[1] = 1.0;
        double alfaOptima;
        double gammaOptima;
        double alfa0 = 0.5;
        double gamma0 = 0.5;
        long startTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startTime > 5_000) {
                startTime = System.currentTimeMillis();
                coefficientK[0] *= 10;
                coefficientK[1] *= 10;
            }
            double sum0 = sumSquaredDeviationsTrend(alfa0, gamma0);
            double sumTemp1 = sumSquaredDeviationsTrend(alfa0 + epsilon, gamma0);
            double sumTemp2 = sumSquaredDeviationsTrend(alfa0, gamma0 + epsilon);
            Double[] grad0 = new Double[2];
            grad0[0] = (sumTemp1 - sum0) / epsilon;
            grad0[1] = (sumTemp2 - sum0) / epsilon;
            double alfa1 = alfa0 - coefficientK[0] * grad0[0];
            double gamma1 = gamma0 - coefficientK[1] * grad0[1];
            if (alfa1 < 0 || alfa1 > 1){
                coefficientK[0] /= 10;
                continue;
            }
            if (gamma1 < 0 || gamma1 > 1){
                coefficientK[1] /= 10;
                continue;
            }
            double sum1 = sumSquaredDeviationsTrend(alfa1, gamma1);
            if (sum1 < sum0) {
                if (Math.abs(alfa1 - alfa0) < epsilon && Math.abs(gamma1 - gamma0) < epsilon) {
                    alfaOptima = alfa1;
                    gammaOptima = gamma1;
                    sumSquaredDeviationsTrend(alfaOptima, gammaOptima);
                    break;
                }
                alfa0 = alfa1;
                gamma0 = gamma1;
            } else {
                if (coefficientK[0] == 0.0 || coefficientK[1] == 0.0) {
                    alfaOptima = alfa0;
                    gammaOptima = gamma0;
                    sumSquaredDeviationsTrend(alfaOptima, gammaOptima);
                    break;
                }
                coefficientK[0] /= 10;
                coefficientK[1] /= 10;
            }
        }
        optimizationCoefficientsTrend = new Double[2];
        optimizationCoefficientsTrend[0] = alfaOptima;
        optimizationCoefficientsTrend[1] = gammaOptima;
    }

    private void optimizationForSeasonalityHolt(double level0, double trend0, TreeMap<Integer, Double> initialSeasonalFactor) {
        double epsilon = 1.0E-9;
        Double[] coefficientK = new Double[3];
        coefficientK[0] = 1.0;
        coefficientK[1] = 1.0;
        coefficientK[2] = 1.0;
        double alfaOptima;
        double gammaOptima;
        double deltaOptima;
        double alfa0 = 0.5;
        double gamma0 = 0.5;
        double delta0 = 0.5;
        long startTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startTime > 5_000) {
                startTime = System.currentTimeMillis();
                coefficientK[0] *= 10;
                coefficientK[1] *= 10;
                coefficientK[2] *= 10;
            }
            double sum0 = sumSquaredDeviationsSeasonality(alfa0, gamma0, delta0, level0, trend0, initialSeasonalFactor);
            double sumTemp1 = sumSquaredDeviationsSeasonality(alfa0 + epsilon, gamma0, delta0, level0, trend0, initialSeasonalFactor);
            double sumTemp2 = sumSquaredDeviationsSeasonality(alfa0, gamma0 + epsilon, delta0, level0, trend0, initialSeasonalFactor);
            double sumTemp3 = sumSquaredDeviationsSeasonality(alfa0, gamma0, delta0 + epsilon, level0, trend0, initialSeasonalFactor);
            Double[] grad0 = new Double[3];
            grad0[0] = (sumTemp1 - sum0) / epsilon;
            grad0[1] = (sumTemp2 - sum0) / epsilon;
            grad0[2] = (sumTemp3 - sum0) / epsilon;
            double alfa1 = alfa0 - coefficientK[0] * grad0[0];
            double gamma1 = gamma0 - coefficientK[1] * grad0[1];
            double delta1 = delta0 - coefficientK[2] * grad0[2];
            if (alfa1 < 0 || alfa1 > 1){
                coefficientK[0] /= 10;
                continue;
            }
            if (gamma1 < 0 || gamma1 > 1){
                coefficientK[1] /= 10;
                continue;
            }
            if (delta1 < 0 || delta1 > 1){
                coefficientK[2] /= 10;
                continue;
            }
            double sum1 = sumSquaredDeviationsSeasonality(alfa1, gamma1, delta1, level0, trend0, initialSeasonalFactor);
            if (sum1 < sum0) {
                if (Math.abs(alfa1 - alfa0) < epsilon && Math.abs(gamma1 - gamma0) < epsilon && Math.abs(delta1 - delta0) < epsilon) {
                    alfaOptima = alfa1;
                    gammaOptima = gamma1;
                    deltaOptima = delta1;
                    sumSquaredDeviationsSeasonality(alfaOptima, gammaOptima, deltaOptima, level0, trend0, initialSeasonalFactor);
                    break;
                }
                alfa0 = alfa1;
                gamma0 = gamma1;
                delta0 = delta1;
            } else {
                if (coefficientK[0] == 0.0 || coefficientK[1] == 0.0 || coefficientK[2] == 0.0) {
                    alfaOptima = alfa0;
                    gammaOptima = gamma0;
                    deltaOptima = delta0;
                    sumSquaredDeviationsSeasonality(alfaOptima, gammaOptima, deltaOptima, level0, trend0, initialSeasonalFactor);
                    break;
                }
                coefficientK[0] /= 10;
                coefficientK[1] /= 10;
                coefficientK[2] /= 10;
            }
        }
        optimizationCoefficientsSeasonality = new Double[3];
        optimizationCoefficientsSeasonality[0] = alfaOptima;
        optimizationCoefficientsSeasonality[1] = gammaOptima;
        optimizationCoefficientsSeasonality[2] = deltaOptima;
    }

    public ArrayList<Double> getForecastErrorMinTrend() {
        if (forecastErrorMinTrend == null) optimizationForTrendHolt();
        return forecastErrorMinTrend;
    }

    public ArrayList<Double> getForecastErrorMinSimple() {
        if (forecastErrorMinSimple == null) optimizationForSimpleHolt();
        return forecastErrorMinSimple;
    }

    public ArrayList<Double> getForecastErrorMinSeasonality() {
        return forecastErrorMinSeasonality;
    }

    public double getOptimizationCoefficientSimple() {
        if (optimizationCoefficientSimple == null) optimizationForSimpleHolt();
        return optimizationCoefficientSimple;
    }

    public Double[] getOptimizationCoefficientsTrend() {
        if (optimizationCoefficientsTrend == null) optimizationForTrendHolt();
        return optimizationCoefficientsTrend;
    }

    public Double[] getOptimizationCoefficientsSeasonality
            (double level0, double trend0, TreeMap<Integer, Double> initialSeasonalFactor) {
        if (optimizationCoefficientsSeasonality == null)
            optimizationForSeasonalityHolt(level0, trend0, initialSeasonalFactor);
        return optimizationCoefficientsSeasonality;
    }

    private double sumSquaredDeviationsSimple(double alfa) {
        ArrayList<Double> level = new ArrayList<>();
        level.add(history.get(history.firstKey()));
        ArrayList<Double> oneStepForecast = new ArrayList<>();
        oneStepForecast.add(0.0);
        ArrayList<Double> forecastError = new ArrayList<>();
        forecastError.add(0.0);
        double sum = 0;
        int backStep = 0;
        for (Date date : history.keySet()) {
            oneStepForecast.add(level.get(backStep));
            forecastError.add(history.get(date) - oneStepForecast.get(backStep + 1));
            level.add(level.get(backStep) + alfa * forecastError.get(backStep + 1));
            sum += Math.pow(forecastError.get(backStep + 1), 2);
            backStep++;
        }
        forecastErrorMinSimple = forecastError;
        return sum;
    }

    private double sumSquaredDeviationsTrend(double alfa, double gamma) {
        ArrayList<Double> level = new ArrayList<>();
        level.add(history.get(history.firstKey()));
        ArrayList<Double> trend = new ArrayList<>();
        trend.add(0.0);
        ArrayList<Double> oneStepForecast = new ArrayList<>();
        oneStepForecast.add(0.0);
        ArrayList<Double> forecastError = new ArrayList<>();
        forecastError.add(0.0);
        double sum = 0;
        int backStep = 0;
        for (Date date : history.keySet()) {
            oneStepForecast.add(level.get(backStep) + trend.get(backStep));
            forecastError.add(history.get(date) - oneStepForecast.get(backStep + 1));
            level.add(level.get(backStep) + trend.get(backStep) + alfa * forecastError.get(backStep + 1));
            trend.add(trend.get(backStep) + alfa * gamma * forecastError.get(backStep + 1));
            sum += Math.pow(forecastError.get(backStep + 1), 2);
            backStep++;
        }
        forecastErrorMinTrend = forecastError;
        return sum;
    }

    private double sumSquaredDeviationsSeasonality
            (double alfa, double gamma, double delta, double level0, double trend0, TreeMap<Integer, Double> initialSeasonalFactor) {
        ArrayList<Double> level = new ArrayList<>();
        level.add(level0);
        ArrayList<Double> trend = new ArrayList<>();
        trend.add(trend0);
        ArrayList<Double> seasonal = new ArrayList<>(initialSeasonalFactor.values());
        ArrayList<Double> oneStepForecast = new ArrayList<>();
        oneStepForecast.add(0.0);
        ArrayList<Double> forecastError = new ArrayList<>();
        forecastError.add(0.0);
        double sum = 0;
        int backStep = 0;
        for (Date date : history.keySet()) {
            oneStepForecast.add((level.get(backStep) + trend.get(backStep)) * seasonal.get(backStep));
            forecastError.add(history.get(date) - oneStepForecast.get(backStep + 1));
            level.add(level.get(backStep) + trend.get(backStep) + alfa * forecastError.get(backStep + 1) / seasonal.get(backStep));
            trend.add(trend.get(backStep) + alfa * gamma * forecastError.get(backStep + 1) / seasonal.get(backStep));
            seasonal.add(seasonal.get(backStep) + delta * (1 - alfa) * forecastError.get(backStep + 1) /
                    (level.get(backStep) + trend.get(backStep)));
            sum += Math.pow(forecastError.get(backStep + 1), 2);
            backStep++;
        }
        forecastErrorMinSeasonality = forecastError;
        return sum;
    }

}
