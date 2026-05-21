# Presentation Outline -- Gym Membership Management System

Slide-by-slide plan for the in-person presentation. Aim: 8-10 minutes
of talk + 2-3 minutes of live demo + 2-5 minutes of Q&A.

---

## Slide 1 -- Title

- **Title:** Gym Membership Management System -- Builder + Observer
- **Course:** SEN3006 Software Architecture
- **Subtitle:** Two design patterns applied to membership plans and
  user notifications.
- **Speaker note:** Open in one sentence. "I built a gym membership
  manager to make two design patterns visible."

---

## Slide 2 -- Why this project

- The brief asks for one Creational and one Behavioral pattern.
- A gym's software has two natural complexity sources that map
  cleanly:
  - Many configurable plan attributes (Creational -- Builder).
  - Notification fan-out across channels (Behavioral -- Observer).
- Real, working application rather than a toy demo.

---

## Slide 3 -- What the system does

- Three sample plans by default (Basic, Standard, Premium), plus
  unlimited custom ones via the Builder.
- Member lifecycle: PENDING -> ACTIVE -> EXPIRING -> EXPIRED, with
  FROZEN <-> ACTIVE and CANCELLED branches.
- Four event kinds: payment due, renewal reminder, class cancellation,
  promotion (targeted or broadcast).
- Three notification channels: email, SMS, push.
- Three entry points sharing one engine: scripted demo, console menu,
  Swing GUI.

---

## Slide 4 -- Builder

- Problem: an eight-parameter constructor is unreadable and impossible
  to extend.
- Solution: `MembershipPlan.Builder` -- nested static, fluent, runs
  validation centrally in `build()`.
- Product is immutable; the constructor is private.
- Code snippet:

  ```java
  MembershipPlan premium = new MembershipPlan.Builder("Premium Annual")
          .durationMonths(12)
          .monthlyFee(89.99)
          .accessTier(AccessTier.PREMIUM)
          .includesClass("Yoga")
          .guestPassesPerMonth(4)
          .personalTrainerIncluded(true)
          .build();
  ```

- **Speaker note:** Mention Bloch's "Effective Java" item: "Consider a
  builder when faced with many constructor parameters."

---

## Slide 5 -- Observer

- Problem: 4 events x 3 channels = 12 cells if done naively.
- Solution: `Gym` is the Subject; `MemberNotifier` is the Observer
  interface; three concrete implementations cover the channels.
- One `publishEvent(...)` call; the gym decides targeted vs broadcast
  internally.
- New channel = one new class. New event = one new subclass.
- **Speaker note:** This is the pattern Swing itself uses
  (`ActionListener`, `PropertyChangeListener`).

---

## Slide 6 -- Lifecycle (bonus pattern)

- `MembershipStatus` enum is a State machine.
- Six states, nine allowed transitions, two terminal states.
- Invalid transitions throw `IllegalArgumentException`.
- Show the state diagram on screen.

---

## Slide 7 -- SOLID quick-pass

- **S**RP -- one job per class.
- **O**CP -- new attribute / event / channel = new files only.
- **L**SP -- every notifier works through the interface.
- **I**SP -- minimal interfaces, type-specific methods on concrete classes.
- **D**IP -- Gym depends only on abstractions.

---

## Slide 8 -- Architecture in one picture

- Class diagram (Mermaid render).
- Highlight the three layers:
  - Domain (Member + Plan + Builder + enums).
  - Pattern (events + notifiers).
  - Coordination + entry-points (Gym + three drivers).
- Point out that arrows only ever go toward abstractions.

---

## Slide 9 -- Live demo (3-4 minutes)

1. **Run the test demo.** `java -cp bin Main`. Scroll past the
   `[PASS]` markers; pause at the Observer demo to show per-channel
   formatting.
2. **Open the GUI.** `java -jar GymManagerGUI.jar`. Load the
   *Observer demo* from the menu.
3. **Targeted event.** Select Alice, click *Payment due...*. Two
   lines appear in the dark log strip (email + push).
4. **Broadcast event.** Click *Promotion...*. Six lines appear --
   every notifier on every member fires.
5. **Invalid transition.** Pick a `PENDING` row, try to *Mark
   cooked*. The button is disabled, mirroring the engine's refusal.

---

## Slide 10 -- Extension story

- New attribute `lockerIncluded`:
  - Field + accessor on `MembershipPlan`.
  - Setter on `MembershipPlan.Builder`.
  - Existing callers unchanged.
- New channel `DiscordMemberNotifier`:
  - One new class implementing `MemberNotifier`.
  - One `attachNotifier(...)` call.
  - Engine and existing channels unchanged.
- New event kind `MemberBirthdayEvent`:
  - One new class extending `GymEvent`.
  - Optionally one new convenience method on `Gym`.

---

## Slide 11 -- What went well, what could be next

- Worked well:
  - Patterns map cleanly to the domain.
  - Test 5 makes Open/Closed visibly true at runtime.
  - The GUI's dark log strip makes Observer's fan-out impossible to
    miss.
- Could be next:
  - Persistence (Repository pattern).
  - Scheduled events (`MembershipScheduler`).
  - Internationalisation.

---

## Slide 12 -- Q&A

- Open the study-guide cheat-sheet on screen.
- Likely questions:
  - Why nested static Builder instead of a separate class?
  - Why is `Gym` the only publisher?
  - Why does each notifier keep a `sentLog`?
- All answered in `docs/design/study-guide.md`.

---

## Timing budget

| Slide | Time |
|-------|------|
| 1 -- Title | 0:30 |
| 2 -- Why | 0:45 |
| 3 -- What it does | 1:00 |
| 4 -- Builder | 1:15 |
| 5 -- Observer | 1:15 |
| 6 -- Lifecycle | 0:45 |
| 7 -- SOLID | 0:50 |
| 8 -- Architecture | 0:45 |
| 9 -- Live demo | 3:00 |
| 10 -- Extension | 0:45 |
| 11 -- Recap | 0:30 |
| 12 -- Q&A | 2-5:00 |
| **Total** | **~13 min + Q&A** |

Cut slide 7 (SOLID) and slide 11 (recap) if time pressure appears.
