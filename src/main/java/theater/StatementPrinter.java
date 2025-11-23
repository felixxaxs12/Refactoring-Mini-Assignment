package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private Invoice invoice;
    private Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;
        final StringBuilder result = new StringBuilder("Statement for "
                + invoice.getCustomer()
                + System.lineSeparator());

        final NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);

        for (Performance performance : invoice.getPerformances()) {

            final int thisAmount = this.getAmount(performance);

            volumeCredits += this.getVolumesCredits(performance);
            // print line for this order
            result.append(String.format("  %s: %s (%s seats)%n",
                    this.getPlay(performance.getPlayID()).getName(),
                    format.format(thisAmount / Constants.PERCENT_FACTOR),
                    performance.getAudience()));
            totalAmount += thisAmount;
        }
        result.append(String.format("Amount owed is %s%n", format.format(totalAmount / Constants.PERCENT_FACTOR)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    /**
     * A helper method calculates the base amount for a given performance and play.
     * @param performance A performance
     * @return The base amount
     */
    public int getAmount(Performance performance) throws RuntimeException {
        int result = 0;
        switch (this.getPlay(performance.getPlayID()).getType()) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.HISTORY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s",
                        getPlay(performance.getPlayID()).getType()));
        }
        return result;
    }

    /**
     * A helper method to get Play.
     * @param playID The ID of the play
     * @return The play
     */
    public Play getPlay(String playID) {
        return plays.get(playID);
    }

    /**
     * A helper method that get volumes credits.
     * @param performance The performance object
     * @return The new credits earned
     */
    public int getVolumesCredits(Performance performance) {
        int result = 0;
        // add volume credits
        result += Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        // add extra credit for every five comedy attendees
        if ("comedy".equals(this.getPlay(performance.getPlayID()).getType())) {
            result += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;
    }
}
