/**
 * Published by {@link Gym} after a fitness class has been removed from the
 * schedule.
 *
 * <p>Carries the {@link FitnessClass} that was removed.</p>
 */
public final class ClassRemovedEvent extends GymEvent {

    private final FitnessClass fitnessClass;

    public ClassRemovedEvent(FitnessClass fitnessClass) {
        super("CLASS_REMOVED", buildMessage(fitnessClass));
        this.fitnessClass = fitnessClass;
    }

    private static String buildMessage(FitnessClass c) {
        if (c == null) {
            throw new IllegalArgumentException(
                "Cannot create ClassRemovedEvent: fitnessClass must not be null.");
        }
        return "Class removed: '" + c.getName() + "'";
    }

    public FitnessClass getFitnessClass() { return fitnessClass; }
}
