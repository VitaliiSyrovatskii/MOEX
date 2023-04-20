import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;

public class ParseSecuritiesList {
    private static String securitiesList;
    private static final String PATH_FILE = "info/SecuritiesList.txt";

    private static String readFile() {
        try {
            securitiesList = Files.readString(Paths.get(PATH_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return securitiesList;
    }

    public static ArrayList<String> getStocks() {
        readFile();
        int startStocks = securitiesList.indexOf("Stocks:") + 7;
        int finishStocks = securitiesList.indexOf("Currencies:");
        String stocks = securitiesList.substring(startStocks, finishStocks);
        ArrayList<String> listStocks = new ArrayList<>();
        int startIndex = 0;
        while (startIndex >= 0) {
            int finishIndex = stocks.indexOf("\n", startIndex + 1);
            if (finishIndex == -1) break;
            String stock = stocks.substring(startIndex, finishIndex).trim().toUpperCase(Locale.ROOT);
            if (!stock.isBlank()) listStocks.add(stock);
            startIndex = finishIndex;
        }
        return listStocks;
    }

    public static ArrayList<String> getCurrencies(){
        readFile();
        int startCurrencies = securitiesList.indexOf("Currencies:") + 11;
        String currencies = securitiesList.substring(startCurrencies).concat("\n");
        ArrayList<String> listCurrencies = new ArrayList<>();
        int startIndex = 0;
        while (startIndex >= 0){
            int finishIndex = currencies.indexOf("\n", startIndex + 1);
            if (finishIndex == -1) break;
            String currency = currencies.substring(startIndex, finishIndex).trim().toUpperCase(Locale.ROOT);
            if (!currency.isBlank()) listCurrencies.add(currency);
            startIndex = finishIndex;
        }
        return listCurrencies;
    }
}
