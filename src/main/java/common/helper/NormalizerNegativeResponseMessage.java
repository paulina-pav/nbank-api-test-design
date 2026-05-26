package common.helper;

public class NormalizerNegativeResponseMessage {
    public static String normalizeMessage(String message){
        StringBuilder sb = new StringBuilder(message);

        int startChars = 12; // сколько символов удалить с начала
        int endChars = 2;   // сколько символов удалить с конца

        sb.delete(0, startChars); // удалит первые
        sb.delete(sb.length() - endChars, sb.length()); // удалит последние


        return sb.toString();
    }
}
