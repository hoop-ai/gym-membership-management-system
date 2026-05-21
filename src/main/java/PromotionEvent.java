/**
 * Event raised for a marketing promotion (a discount, a referral campaign, a
 * limited-time offer). Always a broadcast: the gym pushes the same message
 * to every active member at once.
 *
 * <h3>Design Pattern Role</h3>
 * <p>Concrete event data in the Observer pattern -- see {@link GymEvent}.</p>
 */
public class PromotionEvent extends GymEvent {

    /** Percentage discount being offered, in the 0--100 range. */
    private final double discountPercent;

    /**
     * @param discountPercent the discount percent (must be in 0..100)
     * @param message         a marketing message (must not be null or blank)
     */
    public PromotionEvent(double discountPercent, String message) {
        super(null, message);  // null target -> broadcast
        if (message.isBlank()) {
            throw new IllegalArgumentException("Promotion message must not be blank.");
        }
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException(
                    "Discount percent must be between 0 and 100, got: " + discountPercent);
        }
        this.discountPercent = discountPercent;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    @Override
    public String getType() {
        return "PROMOTION";
    }
}
