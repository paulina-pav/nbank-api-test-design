package ui.alerts;

public class AlertsHelpMethods {

    private static String  getNumbersFromAccountNumber(String accountNumber) {
      return accountNumber.substring(3);
    }

    public static String formTransferSuccessfulAlert(Double sum, String recipientAccNumber) {
        return "✅ Successfully transferred " + "$" + sum.toString() +
                " to " + "account " + recipientAccNumber + "!";
    }
    public static String formDepositSuccessfulAlert(Double sum, String accountNumber) {
        return "✅ Successfully deposited $" + sum
                + " to account " + accountNumber + "!";
    }

    public static String formTransferAgainSuccessfulAlert(Double sum,
                                                          String senderAccNumber, String recipientAccNumber) {

        Integer amountInteger = sum.intValue();
        String accountNumberWithoutChars = AlertsHelpMethods.getNumbersFromAccountNumber(senderAccNumber);

        return "✅ Transfer of $" + amountInteger.toString()
                + " successful from Account " + accountNumberWithoutChars
                + " " + "to " + accountNumberWithoutChars;
    }

    public static String formTransferAgainDebetSuccessfulAlert(Double sum, String senderAccNumber) {
        Integer amountInteger = sum.intValue();

        String accountNumberWithoutChars = AlertsHelpMethods.getNumbersFromAccountNumber(senderAccNumber);

        return "✅ Transfer of $" + amountInteger.toString()
                + " successful from Account " + accountNumberWithoutChars
                + " " + "to " + accountNumberWithoutChars;
    }
}
