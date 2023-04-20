import java.util.ArrayList;
import java.util.TreeMap;
import java.sql.Date;

public class GetLastDateAuctionLocalHistory {
    private TreeMap<String, ArrayList<String>> securitiesInfo;

    public TreeMap<String, Date> get(){
        securitiesInfo = GetInfoSecurities.getInfo();
        TreeMap<String, Date> lastDateAuctionLocalHistory = new TreeMap<>();
        for (String secId : securitiesInfo.keySet()){
            TreeMap<Date, Double> localHistorySecurity = new ParseLocalHistorySecurity(secId).parse();
            if (localHistorySecurity.isEmpty()){
                lastDateAuctionLocalHistory.put(secId, Date.valueOf("1970-01-01"));
            } else {
                lastDateAuctionLocalHistory.put(secId, localHistorySecurity.lastKey());
            }
        }
        return lastDateAuctionLocalHistory;
    }
}
