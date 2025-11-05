package generators;

import org.apache.commons.lang3.RandomStringUtils;

public class DataGenerator {
    private DataGenerator(){}
    public static String getUserName(){
        return RandomStringUtils.randomAlphabetic(10);
    }
    public static String getUserPassword(){
        return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(5).toLowerCase() +
                RandomStringUtils.randomNumeric(3) + "$";
    }

    public static String getName(){
        //два слова из букв и 1 пробел

        return RandomStringUtils.randomAlphabetic(5).toLowerCase() + " "
                + RandomStringUtils.randomAlphabetic(5).toUpperCase();
    }

}
