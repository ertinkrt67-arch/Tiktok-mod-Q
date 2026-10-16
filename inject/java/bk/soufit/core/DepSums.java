package bk.soufit.core;
public final class DepSums {
    private DepSums() {} public static final String[] REEL = {"🍒", "🍋", "🔔", "💎", "🍀", "7"};
    private static final String[] RANK = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    private static final String[] SUIT = {"♠", "♥", "♦", "♣"};
    public static double apply(double left, char operation, double right) {
        switch (operation) {
            case '+': return left + right;
            case '−': return left - right;
            case '×': return left * right;
            case '÷': return right == 0 ? Double.NaN: left / right;
            default: return right;
        }
    }

    public static String shown(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return "∞";
        if (value == Math.rint(value) && Math.abs(value) < 1e15) return Long.toString((long) value);
        String text = String.format(java.util.Locale.US, "%.6f", value);
        while (text.endsWith("0")) text = text.substring(0, text.length() - 1);
        return text;
    }

    public static String card(int card) {
        return RANK[card % 13] + SUIT[(card / 13) % 4];
    }

    public static boolean red(int number) {
        if (number <= 0 || number > 36) return false;
        boolean low = number <= 10 || (number >= 19 && number <= 28);
        return low == (number % 2 == 1);
    }
}
