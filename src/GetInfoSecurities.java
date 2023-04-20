import java.util.ArrayList;
import java.util.TreeMap;

public class GetInfoSecurities {

    private static TreeMap<String, ArrayList<String>> securitiesInfo;

    public static TreeMap<String, ArrayList<String>> updateInfo(){
        securitiesInfo = new TreeMap<>();
        ArrayList<String> listStocks = ParseSecuritiesList.getStocks();
        ArrayList<String> listCurrencies = ParseSecuritiesList.getCurrencies();
        for (String stock : listStocks){
            String text = MOEXConnection.getResponse("https://iss.moex.com/iss/securities.xml?q=" + stock);
            int startIndex = text.indexOf(stock);
            if (startIndex < 0) continue;
            int startIndexName = text.indexOf("shortname=\"", startIndex) + 11;
            int finishIndexName = text.indexOf("\"", startIndexName);
            String name = text.substring(startIndexName, finishIndexName);
            int startIndexIsTraded = text.indexOf("is_traded=\"", finishIndexName) + 11;
            int finishIndexIsTraded = text.indexOf("\"", startIndexIsTraded);
            String isTraded = text.substring(startIndexIsTraded, finishIndexIsTraded).equals("1") ? "Traded" : "NOT Traded";
            int startIndexPrimaryBoardId = text.indexOf("primary_boardid=\"", finishIndexIsTraded) + 17;
            int finishIndexPrimaryBoardId = text.indexOf("\"", startIndexPrimaryBoardId);
            String primaryBoardId = text.substring(startIndexPrimaryBoardId,finishIndexPrimaryBoardId);
            ArrayList<String> list = new ArrayList<>();
            list.add("stock");
            list.add(name);
            list.add(isTraded);
            list.add(primaryBoardId);
            list.add(getBoardGroupId(primaryBoardId));
            securitiesInfo.put(stock, list);
        }
        for (String currency : listCurrencies){
            String text = MOEXConnection.getResponse("https://iss.moex.com/iss/securities.xml?q=" + currency);
            int startIndex = text.indexOf("<rows>");
            int startIndexSecId = text.indexOf("secid=\"", startIndex) + 7;
            if (startIndexSecId < 7) continue;
            int finishIndexSecId = text.indexOf("\"", startIndexSecId);
            String secId = text.substring(startIndexSecId, finishIndexSecId);
            int startIndexName = text.indexOf("shortname=\"", finishIndexSecId) + 11;
            int finishIndexName = text.indexOf("\"", startIndexName);
            String name = text.substring(startIndexName, finishIndexName);
            int startIndexIsTraded = text.indexOf("is_traded=\"", finishIndexName) + 11;
            int finishIndexIsTraded = text.indexOf("\"", startIndexIsTraded);
            String isTraded = text.substring(startIndexIsTraded, finishIndexIsTraded).equals("1") ? "Traded" : "NOT Traded";
            int startIndexPrimaryBoardId = text.indexOf("primary_boardid=\"", finishIndexIsTraded) + 17;
            int finishIndexPrimaryBoardId = text.indexOf("\"", startIndexPrimaryBoardId);
            String primaryBoardId = text.substring(startIndexPrimaryBoardId,finishIndexPrimaryBoardId);
            ArrayList<String> list = new ArrayList<>();
            list.add("currency");
            list.add(name);
            list.add(isTraded);
            list.add(primaryBoardId);
            list.add(getBoardGroupId(primaryBoardId));
            securitiesInfo.put(secId, list);
        }
        return securitiesInfo;
    }
    public static TreeMap<String, ArrayList<String>> getInfo(){
        if (securitiesInfo != null) return securitiesInfo;
        return updateInfo();
    }

    private static String getBoardGroupId(String primaryBoardId){
        String boardGroupId;
        String text = MOEXConnection.getResponse("https://iss.moex.com/iss/index.xml");
        for (String line : text.split("\n")){
            if (line.contains(primaryBoardId)){
                int startIndexBoardGroupId = line.indexOf("board_group_id=\"") + 16;
                int finishIndexBoardGroupId = line.indexOf("\"", startIndexBoardGroupId);
                boardGroupId = line.substring(startIndexBoardGroupId, finishIndexBoardGroupId);
                return boardGroupId;
            }
        }
        return new String();
    }
}
