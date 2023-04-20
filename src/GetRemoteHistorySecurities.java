import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TreeMap;

public class GetRemoteHistorySecurities {

    private static TreeMap<String, ArrayList<String>> securitiesInfo;

    public static void getHistory() {
        securitiesInfo = GetInfoSecurities.getInfo();
        for (String secId : securitiesInfo.keySet()) {
            String engines = securitiesInfo.get(secId).get(0);
            if (engines.equals("currency") && securitiesInfo.get(secId).get(2).equals("Traded") && !isActualInfo(secId)) {
                try {
                    Files.writeString(Paths.get("data/" + secId + ".txt"), readCurrencies(secId));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (engines.equals("stock") && securitiesInfo.get(secId).get(2).equals("Traded") && !isActualInfo(secId)) {
                try {
                    Files.writeString(Paths.get("data/" + secId + ".txt"), readStocks(secId));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static StringBuilder readCurrencies(String secId) {
        int start = 0;
        StringBuilder collectResponse = new StringBuilder();
        while (true) {
            String data = MOEXConnection.getResponse("https://iss.moex.com/" +
                    "iss/history/engines/currency/markets/selt/boardgroups/" + securitiesInfo.get(secId).get(4) + "/securities/" +
                    secId + ".xml?start=" + start + "&history.columns=TRADEDATE,CLOSE,BOARDID&iss.meta=off");
            int startData = data.indexOf("<rows>") + 6;
            int finishData = data.indexOf("</rows>", startData);
            if (data.substring(startData, finishData).isBlank()) break;
            collectResponse.append(data);
            start += 100;
        }
        return collectResponse;
    }

    private static StringBuilder readStocks(String secId) {
        int start = 0;
        StringBuilder collectResponse = new StringBuilder();
        while (true) {
            String data = MOEXConnection.getResponse("https://iss.moex.com/" +
                    "iss/history/engines/stock/markets/shares/boardgroups/" + securitiesInfo.get(secId).get(4) + "/securities/" +
                    secId + ".xml?start=" + start + "&history.columns=TRADEDATE,CLOSE,BOARDID&iss.meta=off");
            int startData = data.indexOf("<rows>") + 6;
            int finishData = data.indexOf("</rows>", startData);
            if (data.substring(startData, finishData).isBlank()) break;
            collectResponse.append(data);
            start += 100;
        }
        return collectResponse;
    }

    private static boolean isActualInfo(String secId) {
        try {
            Files.createDirectories(Paths.get("data"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!Files.exists(Paths.get("data/" + secId + ".txt"))) {
            return false;
        }
        String data;
        if (securitiesInfo.get(secId).get(0).equals("stock")) {
            data = MOEXConnection.getResponse("https://iss.moex.com/" +
                    "iss/history/engines/stock/markets/shares/securities/" +
                    secId + ".xml?&history.columns=TRADEDATE,CLOSE,BOARDID&iss.meta=off");
        } else {
            data = MOEXConnection.getResponse("https://iss.moex.com/" +
                    "iss/history/engines/currency/markets/selt/securities/" +
                    secId + ".xml?&history.columns=TRADEDATE,CLOSE,BOARDID&iss.meta=off");
        }
        int startIndexTotal = data.indexOf("TOTAL=\"") + 7;
        int finishIndexTotal = data.indexOf("\"", startIndexTotal);
        String total = data.substring(startIndexTotal, finishIndexTotal).trim();
        int totalCount = Integer.parseInt(total) - 1;
        if (securitiesInfo.get(secId).get(0).equals("stock")) {
            data = MOEXConnection.getResponse("https://iss.moex.com/" +
                    "iss/history/engines/stock/markets/shares/securities/" +
                    secId + ".xml?&history.columns=TRADEDATE,CLOSE,BOARDID&iss.meta=off&start=" + totalCount);
        } else {
            data = MOEXConnection.getResponse("https://iss.moex.com/" +
                    "iss/history/engines/currency/markets/selt/securities/" +
                    secId + ".xml?&history.columns=TRADEDATE,CLOSE,BOARDID&iss.meta=off&start=" + totalCount);
        }
        int startIndexDate = data.indexOf("TRADEDATE=\"") + 11;
        int finishIndexDate = data.indexOf("\"", startIndexDate);
        String date = data.substring(startIndexDate, finishIndexDate);
        String readFile = new String();
        try {
            readFile = Files.readString(Paths.get("data/" + secId + ".txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!readFile.contains(date)) return false;
        return true;
    }

}
