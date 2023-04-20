import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TreeMap;

public class MainForm {
    private JPanel mainPanel;
    private JButton button2;
    private JButton button1;
    private JTable table;
    private JButton button3;
    private JButton button4;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JButton button5;
    private JScrollPane tableScroll;
    private JTextArea textArea;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField7;
    private JTextField textField8;
    private JTextField textField9;
    private JButton button6;
    private JButton button7;
    private JButton button8;
    private JTextField textField10;

    private int columnCount = 4;
    private int rowCount = GetInfoSecurities.getInfo().size();
    private String[][] tableData = new String[rowCount][columnCount];

    public MainForm() {

        java.util.Date currentDate = new java.util.Date();
        Date formattedCurrentDate = new Date(currentDate.getTime());
        textField2.setText(formattedCurrentDate.toString());
        SimpleDateFormat logDateTimeFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText(textArea.getText().
                        concat(logDateTimeFormat.format(new java.util.Date()) +
                                " SYSTEM Начало обновления списка ценных бумаг\n"));
                GetInfoSecurities.updateInfo();
                rowCount = GetInfoSecurities.getInfo().size();
                String[][] tableDataOld = tableData.clone();
                tableData = new String[rowCount][columnCount];
                int counter = 0;
                for (String secId : GetInfoSecurities.getInfo().keySet()) {
                    for (int i = 0; i < tableDataOld.length; i++) {
                        if (secId.equals(tableDataOld[i][0])) {
                            tableData[counter][0] = secId;
                            tableData[counter][1] = tableDataOld[i][1];
                            tableData[counter][2] = tableDataOld[i][2];
                            tableData[counter][3] = tableDataOld[i][3];
                            counter++;
                            break;
                        }
                        if (!secId.equals(tableDataOld[i][0]) && i == tableDataOld.length - 1) {
                            tableData[counter][0] = secId;
                            counter++;
                        }
                    }
                }
                table.revalidate();
                table.repaint();
                textArea.setText(textArea.getText().
                        concat(logDateTimeFormat.format(new java.util.Date()) +
                                " SYSTEM Завершено обновление списка ценных бумаг\n"));
            }
        });
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText(textArea.getText().
                        concat(logDateTimeFormat.format(new java.util.Date()) +
                                " SYSTEM Начало обновления базы данных\n"));
                GetRemoteHistorySecurities.getHistory();
                textArea.setText(textArea.getText().
                        concat(logDateTimeFormat.format(new java.util.Date()) +
                                " SYSTEM Завершено обновление базы данных\n"));
            }
        });

        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText(textArea.getText().
                        concat(logDateTimeFormat.format(new java.util.Date()) +
                                " SYSTEM Начало получения дат последних торгов из базы данных\n"));
                GetLastDateAuctionLocalHistory lastDateAuctionLocalHistory = new GetLastDateAuctionLocalHistory();
                TreeMap<String, Date> lastDate = lastDateAuctionLocalHistory.get();
                for (int i = 0; i < rowCount; i++) {
                    if (lastDate.get(tableData[i][0]).equals(Date.valueOf("1970-01-01"))) {
                        tableData[i][1] = "Нет данных в БД";
                    } else {
                        tableData[i][1] = lastDate.get(tableData[i][0]).toString();
                    }
                }
                table.repaint();
                textArea.setText(textArea.getText().
                        concat(logDateTimeFormat.format(new java.util.Date()) +
                                " SYSTEM Завершено получение дат последних торгов из базы данных\n"));
            }
        });

        button4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText(textArea.getText().
                        concat(logDateTimeFormat.format(new java.util.Date()) +
                                " SYSTEM Начало удаления и загрузки базы данных\n"));
                File directory = new File("data/");
                String[] fileList = directory.list();
                for (int i = 0; i < fileList.length; i++) {
                    try {
                        Files.delete(Paths.get("data/" + fileList[i]));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                GetRemoteHistorySecurities.getHistory();
                textArea.setText(textArea.getText().
                        concat(logDateTimeFormat.format(new java.util.Date()) +
                                " SYSTEM Завершено удаление и загрузка базы данных\n"));
            }
        });

        button5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < rowCount; i++) {
                    if (tableData[i][0].equals(textField1.getText().trim().toUpperCase()) &&
                            !FormatInputData.formatInputDate(textField2.getText()).isBlank() &&
                            !FormatInputData.formatInputPrice(textField3.getText()).isBlank()) {
                        tableData[i][2] = FormatInputData.formatInputDate(textField2.getText());
                        tableData[i][3] = FormatInputData.formatInputPrice(textField3.getText());
                        textArea.setText(textArea.getText().
                                concat(logDateTimeFormat.format(new java.util.Date()) +
                                        " UPDATE Добавлены данные торгов для ценной бумаги: " +
                                        "SecID = " + tableData[i][0] + ", Дата = " + tableData[i][2] +
                                        ", Цена = " + tableData[i][3] + "\n"));
                        textField1.setText("");
                        if (!textField2.getText().equals(formattedCurrentDate.toString()))
                            textField2.setText(formattedCurrentDate.toString());
                        textField3.setText("");
                    }
                }
                table.repaint();
            }
        });

        button6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String secId = textField4.getText().trim().toUpperCase();
                String fromDate = FormatInputData.formatInputDate(textField5.getText());
                String tillDate = FormatInputData.formatInputDate(textField6.getText());
                int firstEMA = FormatInputData.formatInputEMA(textField7.getText());
                int secondEMA = FormatInputData.formatInputEMA(textField8.getText());
                double tax = FormatInputData.formatInputTax(textField9.getText());
                for (int i = 0; i < rowCount; i++) {
                    if (tableData[i][0].equals(secId) &&
                            !fromDate.isBlank() &&
                            !tillDate.isBlank() &&
                            firstEMA != -1 &&
                            secondEMA != -1 &&
                            tax != -1) {
                        CalculationStrategy calculationStrategy =
                                new CalculationStrategy(Date.valueOf(fromDate), Date.valueOf(tillDate), secId, firstEMA, secondEMA, tax);
                        if (tableData[i][2] != null && tableData[i][3] != null)
                        calculationStrategy.addData(tableData[i][2], tableData[i][3]);
                        ArrayList<String> result = calculationStrategy.getResult();
                        textArea.setText(textArea.getText().
                                concat(logDateTimeFormat.format(new java.util.Date()) +
                                        " CALCULATE Произведен расчет стратегии для ценной бумаги \"" +
                                        GetInfoSecurities.getInfo().get(secId).get(1) + "\":\n" +
                                        "SecID = " + secId + "\nНачальная дата = " + fromDate +
                                        ", Конечная дата = " + tillDate + "\nПервый EMA = " + firstEMA +
                                        ", Второй EMA = " + secondEMA + ", Налог = " + tax + "\n" +
                                        "РЕЗУЛЬТАТ:\n" + result.get(0) + " - Прибыль за указанный период\n" +
                                        result.get(1) + " - Прибыль за год\n"));
                        textField4.setText("");
                        textField5.setText("");
                        textField6.setText("");
                        textField7.setText("");
                        textField8.setText("");
                        textField9.setText("");
                    }
                }
            }
        });

        button7.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String secId = textField4.getText().trim().toUpperCase();
                int firstEMA = FormatInputData.formatInputEMA(textField7.getText());
                int secondEMA = FormatInputData.formatInputEMA(textField8.getText());
                for (int i = 0; i < rowCount; i++) {
                    if (tableData[i][0].equals(secId) &&
                            firstEMA != -1 &&
                            secondEMA != -1) {
                        CalculationEMACurrentDate calculationEMACurrentDate =
                                new CalculationEMACurrentDate(secId, firstEMA, secondEMA);

                        if (tableData[i][2] != null && tableData[i][3] != null)
                            calculationEMACurrentDate.addData(tableData[i][2], tableData[i][3]);
                        ArrayList<String> result = calculationEMACurrentDate.getEMAInfo();
                        textArea.setText(textArea.getText().
                                concat(logDateTimeFormat.format(new java.util.Date()) +
                                        " CALCULATE Произведен расчет EMA для ценной бумаги \"" +
                                        GetInfoSecurities.getInfo().get(secId).get(1) + "\":\n" +
                                        result.get(0) + result.get(1) + result.get(2) + result.get(3) +
                                                result.get(4) + result.get(5)));
                        textField4.setText("");
                        textField5.setText("");
                        textField6.setText("");
                        textField7.setText("");
                        textField8.setText("");
                        textField9.setText("");
                    }
                }
            }
        });

        button8.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String secId = textField10.getText().trim().toUpperCase();
                for (int i = 0; i < rowCount; i++) {
                    if (tableData[i][0].equals(secId)){
                        ParseLocalHistorySecurity parseHistory = new ParseLocalHistorySecurity(secId);
                        TreeMap<Date, Double> history = parseHistory.parse();
                        if (tableData[i][2] != null && tableData[i][3] != null){
                            history.put(Date.valueOf(tableData[i][2]), Double.valueOf(tableData[i][3]));
                        }
                        textField10.setText("");
                        mainPanel.setVisible(false);
                        JFrame frame = new JFrame();
                        frame.setSize(900, 600);
                        ForecastingForm fForm = new ForecastingForm(secId, textArea);
                        fForm.setMainPanel(mainPanel);
                        fForm.setFrame(frame);
                        fForm.setHistory(history);
                        frame.add(fForm.getFPanel());
                        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                        frame.setLocationRelativeTo(null);
                        frame.setVisible(true);
                    }
                }
            }
        });

        table.setModel(new AbstractTableModel() {

            @Override
            public int getRowCount() {
                return rowCount;
            }

            @Override
            public int getColumnCount() {
                return columnCount;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                for (int i = 0; i < rowCount; i++) {
                    tableData[i][0] = (String) GetInfoSecurities.getInfo().keySet().toArray()[i];
                }
                return tableData[rowIndex][columnIndex];
            }

            @Override
            public String getColumnName(int column) {
                switch (column) {
                    case 0:
                        return "<html><b><center>SecID</center></b></html>";
                    case 1:
                        return "<html><b><center>Дата последних<br>торгов в БД</center></b></html>";
                    case 2:
                        return "<html><b><center>Дата текущих<br>торгов</center></b></html>";
                    case 3:
                        return "<html><b><center>Цена</center></b></html>";
                }
                return "";
            }
        });
        table.setRowHeight(25);
        TableColumnModel tableColumnModel = table.getColumnModel();
        tableColumnModel.getColumn(1).setPreferredWidth(60);
        tableColumnModel.getColumn(2).setPreferredWidth(60);
        tableColumnModel.getColumn(3).setPreferredWidth(20);
        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(JLabel.CENTER);
        tableColumnModel.getColumn(0).setCellRenderer(defaultTableCellRenderer);
        tableColumnModel.getColumn(1).setCellRenderer(defaultTableCellRenderer);
        tableColumnModel.getColumn(2).setCellRenderer(defaultTableCellRenderer);
        tableColumnModel.getColumn(3).setCellRenderer(defaultTableCellRenderer);
        JTableHeader jTableHeader = table.getTableHeader();
        jTableHeader.setBackground(Color.ORANGE);
        jTableHeader.setReorderingAllowed(false);
        jTableHeader.setResizingAllowed(false);
        jTableHeader.setPreferredSize(new Dimension(50, 50));
        table.setColumnSelectionAllowed(true);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public String getLog() {
        return textArea.getText();
    }

}
