/**
 * Concrete observer that prints every event to standard output.
 *
 * <p>Used in the demo so the audience sees events as they happen.</p>
 */
public final class ConsoleObserver implements GymEventObserver {

    @Override
    public void onEvent(GymEvent event) {
        if (event == null) return;
        System.out.println(event.toString());
    }
}
