import java.time.DateTimeException;
import java.time.LocalDate;

public class FormatInputData {

    private static String regexDate = "[0-9]{2,4}[\\\\/,.-][0-9]{2}[\\\\/,.-][0-9]{2,4}";
    private static String regexDouble = "[0-9]+[.,]?[0-9]*";

    public static String formatInputPrice(String text){
        if (text.trim().matches(regexDouble)){
            String[] arrayPrice = text.trim().split("[,.]");
            if (arrayPrice.length == 2){
                return arrayPrice[0] + "." + arrayPrice[1];
            } else return arrayPrice[0];
        } else return new String();
    }

    public static String formatInputDate(String text){
        if (text.trim().matches(regexDate)){
            String[] arrayDate = text.trim().split("[\\/,.-]");
            if (Integer.parseInt(arrayDate[0]) <= 31 && Integer.parseInt(arrayDate[1]) <= 12){
                String outputDate = isExistDate(arrayDate[2], arrayDate[1], arrayDate[0]) ?
                        arrayDate[2] + "-" + arrayDate[1] + "-" + arrayDate[0] : new String();
                return outputDate;
            } else {
                String outputDate = isExistDate(arrayDate[0], arrayDate[1], arrayDate[2]) ?
                        arrayDate[0] + "-" + arrayDate[1] + "-" + arrayDate[2] : new String();
                return outputDate;
            }
        } else return new String();
    }

    private static boolean isExistDate(String year, String month, String day){
        boolean isExistDate = true;
        try {
            LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        } catch (DateTimeException e){
            isExistDate = false;
        }
        return isExistDate;
    }

    public static int formatInputEMA(String text){
        if (text.trim().matches("[0-9]+") && Integer.parseInt(text.trim()) > 0){
            return Integer.parseInt(text.trim());
        }
        return -1;
    }

    public static double formatInputTax(String text){
        if (text.trim().isBlank()) return 0;
        if (text.trim().matches(regexDouble)){
            String[] arrayTax = text.trim().split("[,.]");
            if (arrayTax.length == 2){
                if (arrayTax[0].equals("0")) return Double.parseDouble((arrayTax[0] + "." + arrayTax[1])) * 100;
                return Double.parseDouble((arrayTax[0] + "." + arrayTax[1]));
            } else return Double.parseDouble(arrayTax[0]);
        } else return -1;
    }
}
