package api.generators;

import java.util.Random;

public class RandomHeaderGenerator {
    public static String generateHeader(){
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int length = 10; // Длина будущей строки
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

       return sb.toString();
    }
}
