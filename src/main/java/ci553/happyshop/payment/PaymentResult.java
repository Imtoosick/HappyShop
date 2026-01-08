package ci553.happyshop.payment;

public class PaymentResult {
    private final boolean success;
    private final String message;
    private final PaymentMethod method;
    private final double amountPaid;
    private final double change;
    private final String cardLast4;

    private PaymentResult(boolean success, String message, PaymentMethod method,
                          double amountPaid, double change, String cardLast4) {
        this.success = success;
        this.message = message;
        this.method = method;
        this.amountPaid = amountPaid;
        this.change = change;
        this.cardLast4 = cardLast4;
    }

    public static PaymentResult okCash(double amountPaid, double change) {
        return new PaymentResult(true, "Payment accepted", PaymentMethod.CASH, amountPaid, change, null);
    }

    public static PaymentResult okCard(double amountPaid, String last4) {
        return new PaymentResult(true, "Payment approved", PaymentMethod.CARD, amountPaid, 0.0, last4);
    }

    public static PaymentResult fail(String message) {
        return new PaymentResult(false, message, null, 0.0, 0.0, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public PaymentMethod getMethod() { return method; }
    public double getAmountPaid() { return amountPaid; }
    public double getChange() { return change; }
    public String getCardLast4() { return cardLast4; }
}

