/**
 * Published by {@link Gym} after a fitness class has been added to the
 * schedule.
 *
 * <p>Carries the {@link FitnessClass}. Observers typically log this for the
 * audit trail or the in-memory journal.</p>
 */
public final class ClassAddedEvent extends GymEvent {

    private final FitnessClass fitnessClass;

    public ClassAddedEvent(FitnessClass fitnessClass) {
        super("CLASS_ADDED", buildMessage(fitnessClass));
        this.fitnessClass = fitnessClass;
    }

    private static String buildMessage(FitnessClass c) {
        if (c == null) {
            throw new IllegalArgumentException(
                "Cannot create ClassAddedEvent: fitnessClass must not be null.");
        }
        return "Class added: '" + c.getName() + "' with " + c.getInstructor();
    }

    public FitnessClass getFitnessClass() { return fitnessClass; }
}
