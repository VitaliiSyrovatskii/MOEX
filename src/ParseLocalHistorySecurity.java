import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.sql.Date;

public class ParseLocalHistorySecurity {

    private String secId;

    public ParseLocalHistorySecurity(String secId) {
        this.secId = secId;
    }

    public TreeMap<Date, Double> parse() {
        List<String> listData = new ArrayList<>();
        if (!Files.exists(Paths.get("data/" + secId + ".txt"))) return new TreeMap<>();
        try {
            listData = Files.readAllLines(Paths.get("data/" + secId + ".txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        TreeMap<Date, Double> clearHistorySecurity = new TreeMap<>();
        for (String line : listData){
            if (!line.contains("<row TRADEDATE")) continue;
            int startIndexTradedate = line.indexOf("TRADEDATE=\"") + 11;
            int finishIndexTradedate = line.indexOf("\"", startIndexTradedate);
            Date tradedate = Date.valueOf(line.substring(startIndexTradedate, finishIndexTradedate));
            if (clearHistorySecurity.containsKey(tradedate)) System.out.println("ALARM");
            int startIndexClose = line.indexOf("CLOSE=\"", finishIndexTradedate) + 7;
            int finishIndexClose = line.indexOf("\"", startIndexClose);
            String stringClose = line.substring(startIndexClose, finishIndexClose);
            if (stringClose.isBlank()) continue;
            Double close = Double.valueOf(stringClose);
            if (close.equals(0.0)) continue;
            clearHistorySecurity.put(tradedate, close);
        }
        return clearHistorySecurity;
    }
}