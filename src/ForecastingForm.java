import Forecasting.Forecasting;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ForecastingForm {
    private JPanel fPanel;
    private JButton button1;
    private JButton button2;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JPanel graphicsPanel;
    private JTextArea textArea;


    private JPanel mainPanel;
    private JFrame frame;
    private String secId;
    private JTextArea textAreaMain;
    private TreeMap<Date, Double> history;

    public ForecastingForm(String secId, JTextArea textAreaMain) {
        this.secId = secId;
        this.textAreaMain = textAreaMain;
        textArea.setText(textAreaMain.getText());
        textField1.setText(secId);
        SimpleDateFormat logDateTimeFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textAreaMain.setText(textArea.getText());
                frame.setVisible(false);
                mainPanel.setVisible(true);
            }
        });
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Graphics2D graphics2D = (Graphics2D) graphicsPanel.getGraphics();
                graphics2D.clearRect(0,0, graphicsPanel.getWidth(), graphicsPanel.getHeight());
                String secId = textField1.getText().trim().toUpperCase();
                String fromDate = FormatInputData.formatInputDate(textField2.getText());
                String tillDate = FormatInputData.formatInputDate(textField3.getText());
                if (GetInfoSecurities.getInfo().containsKey(secId) &&
                        !fromDate.isBlank() &&
                        !tillDate.isBlank()) {
                    TreeMap<Date, Double> newHistory = new TreeMap<>();
                    for (Date date : history.keySet()) {
                        if (date.before(Date.valueOf(fromDate)) || date.after(Date.valueOf(tillDate))) {
                            continue;
                        }
                        newHistory.put(date, history.get(date));
                    }
                    if (!newHistory.isEmpty()) {
                        Forecasting forecasting = new Forecasting(newHistory);
                        TreeMap<Integer, Double> simpleHolt = forecasting.simpleHolt();
                        TreeMap<Integer, Double> trendHolt = forecasting.trendHolt();
                        TreeMap<Integer, Double> seasonalityHolt = forecasting.seasonalityHolt();
                        //Установка значений краевых отступов
                        int indentXLeft = 80;
                        int indentXRight = 20;
                        int indentY = 50;
                        //Вычисление масштаба по x и по y
                        double xScale = (graphicsPanel.getWidth() - (indentXLeft + indentXRight)) /(double) simpleHolt.lastKey();
                        Double yMin = Double.POSITIVE_INFINITY;
                        Double yMax = Double.NEGATIVE_INFINITY;
                        for (int i : simpleHolt.keySet()) {
                            if (yMin > simpleHolt.get(i)) yMin = simpleHolt.get(i);
                            if (yMax < simpleHolt.get(i)) yMax = simpleHolt.get(i);
                        }
                        for (int i : trendHolt.keySet()) {
                            if (yMin > trendHolt.get(i)) yMin = trendHolt.get(i);
                            if (yMax < trendHolt.get(i)) yMax = trendHolt.get(i);
                        }
                        for (int i : seasonalityHolt.keySet()) {
                            if (yMin > seasonalityHolt.get(i)) yMin = seasonalityHolt.get(i);
                            if (yMax < seasonalityHolt.get(i)) yMax = seasonalityHolt.get(i);
                        }
                        if (yMin > newHistory.get(newHistory.lastKey())) yMin = newHistory.get(newHistory.lastKey());
                        if (yMax < newHistory.get(newHistory.lastKey())) yMax = newHistory.get(newHistory.lastKey());
                        double yScale = (graphicsPanel.getHeight() - indentY * 2 - 10) / (yMax - yMin);
                        //Создание массивов точек для построения графиков
                        int[] xSimpleHolt = new int[simpleHolt.size()];
                        for (int i : simpleHolt.keySet()) {
                            xSimpleHolt[i - 1] = (int) Math.round(indentXLeft + i * xScale);
                        }
                        int[] ySimpleHolt = new int[simpleHolt.size()];
                        for (int i : simpleHolt.keySet()) {
                            ySimpleHolt[i - 1] =
                                    (int) Math.round(graphicsPanel.getHeight() - (indentY + simpleHolt.get(i) * yScale - yScale * yMin));
                        }
                        int[] xTrendHolt = new int[trendHolt.size()];
                        for (int i : trendHolt.keySet()) {
                            xTrendHolt[i - 1] = (int) Math.round(indentXLeft + i * xScale);
                        }
                        int[] yTrendHolt = new int[trendHolt.size()];
                        for (int i : trendHolt.keySet()) {
                            yTrendHolt[i - 1] =
                                    (int) Math.round(graphicsPanel.getHeight() - (indentY + trendHolt.get(i) * yScale - yScale * yMin));
                        }
                        int[] xSeasonalityHolt = new int[seasonalityHolt.size()];
                        for (int i : seasonalityHolt.keySet()) {
                            xSeasonalityHolt[i - 1] = (int) Math.round(indentXLeft + i * xScale);
                        }
                        int[] ySeasonalityHolt = new int[seasonalityHolt.size()];
                        for (int i : seasonalityHolt.keySet()) {
                            ySeasonalityHolt[i - 1] =
                                    (int) Math.round(graphicsPanel.getHeight() - (indentY + seasonalityHolt.get(i) * yScale - yScale * yMin));
                        }
                        //Отрисовывание системы координат и меток шкал по x и по y
                        graphics2D.setColor(Color.BLACK);
                        graphics2D.drawLine(indentXLeft, graphicsPanel.getHeight() - indentY,
                                graphicsPanel.getWidth(), graphicsPanel.getHeight() - indentY);
                        graphics2D.drawLine(indentXLeft, graphicsPanel.getHeight() - indentY, indentXLeft, 0);
                        graphics2D.drawString(newHistory.lastKey().toString(), indentXLeft - 30, graphicsPanel.getHeight() - indentY + 20);
                        graphics2D.drawLine(indentXLeft, graphicsPanel.getHeight() - indentY, indentXLeft, graphicsPanel.getHeight() - indentY + 5);
                        graphics2D.drawString("Дни", indentXLeft + (graphicsPanel.getWidth() - indentXLeft - indentXRight) / 2 - 10, graphicsPanel.getHeight() - 20);
                        for (int i : simpleHolt.keySet()) {
                            if (i % 2 == 0) {
                                int x = (int) Math.round(indentXLeft + i * xScale);
                                graphics2D.drawLine(x, graphicsPanel.getHeight() - indentY,
                                        x, graphicsPanel.getHeight() - indentY + 5);
                                graphics2D.setColor(Color.LIGHT_GRAY);
                                graphics2D.drawLine(x, graphicsPanel.getHeight() - indentY, x, 0);
                                graphics2D.setColor(Color.BLACK);
                                graphics2D.drawString("+" + i, x - 10, graphicsPanel.getHeight() - indentY + 20);
                            }
                        }
                        double step = !yMax.equals(yMin) ? (yMax - yMin) / 6 : 1;
                        graphics2D.drawLine(indentXLeft, graphicsPanel.getHeight() - indentY, indentXLeft - 5, graphicsPanel.getHeight() - indentY);
                        for (double i = yMin; i < yMax + 0.000001; i += step) {
                            int y = (int) Math.round(graphicsPanel.getHeight() - (indentY + i * yScale - yScale * yMin));
                            if (i < 1) {
                                String label = Math.round(i * 1000000) / 1000000.0 + "";
                                graphics2D.drawString(label, 10, y + 5);
                            } else {
                                String label = Math.round(i * 1000) / 1000.0 + "";
                                graphics2D.drawString(label, 10, y + 5);
                            }
                            if (i == yMin) continue;
                            graphics2D.drawLine(indentXLeft, y, indentXLeft - 5, y);
                            graphics2D.setColor(Color.LIGHT_GRAY);
                            graphics2D.drawLine(indentXLeft, y, graphicsPanel.getWidth(), y);
                            graphics2D.setColor(Color.BLACK);
                        }
                        //Отрисовывание графиков
                        graphics2D.setColor(Color.RED);
                        int yLastPrice;
                        if (newHistory.size() == 1){
                            yLastPrice = (graphicsPanel.getHeight() - 2 * indentY) / 2;
                        } else {
                            yLastPrice = (int) Math.round(graphicsPanel.getHeight() -
                                    (indentY + newHistory.get(newHistory.lastKey()) * yScale - yScale * yMin));
                        }
                        graphics2D.drawLine(indentXLeft, yLastPrice, (int) (indentXLeft + xScale), yLastPrice);
                        String lastPrice = Math.round(newHistory.get(newHistory.lastKey()) * 1000) / 1000.0 +
                                " - цена на " + newHistory.lastKey();
                        graphics2D.drawString(lastPrice, (int) (indentXLeft + xScale), yLastPrice - 5);
                        graphics2D.setColor(Color.GREEN);
                        graphics2D.drawPolyline(xSimpleHolt, ySimpleHolt, simpleHolt.size());
                        if (!simpleHolt.isEmpty()){
                            graphics2D.drawString("simple",
                                    xSimpleHolt[simpleHolt.size() - 1] - 15, ySimpleHolt[simpleHolt.size() - 1] - 5);
                        }
                        graphics2D.setColor(Color.MAGENTA);
                        graphics2D.drawPolyline(xTrendHolt, yTrendHolt, trendHolt.size());
                        if (!trendHolt.isEmpty()){
                            graphics2D.drawString("trend",
                                    xTrendHolt[trendHolt.size() - 1] - 15, yTrendHolt[trendHolt.size() - 1] - 5);
                        }
                        graphics2D.setColor(Color.BLUE);
                        graphics2D.drawPolyline(xSeasonalityHolt, ySeasonalityHolt, seasonalityHolt.size());
                        if (!seasonalityHolt.isEmpty()){
                            graphics2D.drawString("seasonality",
                                    xSeasonalityHolt[seasonalityHolt.size() - 1] - 25, ySeasonalityHolt[seasonalityHolt.size() - 1] - 5);
                        }
                        textArea.setText(textArea.getText().
                                concat(logDateTimeFormat.format(new java.util.Date()) +
                                " FORECASTING Произведен расчет прогноза по ценной бумаге \"" +
                                        secId + "\".\nПри расчете брался период с " +
                                        newHistory.firstKey() + " по " + newHistory.lastKey() + "\n" +
                                        "Метод простого экспоненциального сглаживания показал результат: [" +
                                        Math.round(simpleHolt.get(simpleHolt.firstKey()) * 1000) / 1000.0 + "]\n" +
                                        "Метод с корректировкой тренда показал результат: " +
                                        trendHolt.values().stream().map(d -> Math.round(d * 1000) / 1000.0).toList() + "\n" +
                                        "Метод мультипликативного сглаживания показал результат: " +
                                        seasonalityHolt.values().stream().map(d -> Math.round(d * 1000) / 1000.0).toList() + "\n"));
                    }
                }
            }
        });
    }

    public JPanel getFPanel() {
        return fPanel;
    }

    public void setMainPanel(JPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public void setHistory(TreeMap<Date, Double> history) {
        this.history = history;
    }

}
