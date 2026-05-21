# Study Guide -- Gym Membership Management System

Preparation document for the in-person presentation. Every class is
explained in one paragraph, then the Q&A cheat-sheet answers the most
likely professor questions.

---

## Every class in one paragraph each

### `MembershipPlan` (the Builder product)

The immutable Product in the Builder pattern. Holds eight private-final
attributes -- name, duration in months, monthly fee, access tier,
included classes, guest passes per month, freeze-days allowance,
personal-trainer flag -- plus a derived `getTotalCost()` accessor. The
constructor is private; nothing outside the nested Builder can
instantiate a plan, so every plan that exists has been built and
validated by the same routine.

### `MembershipPlan.Builder` (the fluent builder)

The nested static Builder. Carries a working copy of every plan
attribute and exposes one chainable setter per attribute. The
constructor enforces the only truly mandatory field (the plan's
display name). `build()` runs every cross-field validation
(positive duration, non-negative fee, non-null tier, non-negative
counts) before returning the immutable Product. This is the textbook
Bloch-style Builder used throughout the JDK (`HttpRequest.Builder`,
`StringBuilder`, ...).

### `Member`

The carrier of state and of attached observers. Holds the universal
member data (id, name, email, phone, join timestamp, current plan,
status, renewal date) plus a list of attached `MemberNotifier`
instances. `setStatus(...)` validates the transition against
`MembershipStatus.canTransitionTo(...)` and pushes the renewal date
forward when a member moves out of `EXPIRING` or `FROZEN` back into
`ACTIVE`. The class is the single place where the lifecycle state
machine is enforced.

### `MembershipStatus` (enum)

A six-state lightweight State pattern: `PENDING -> ACTIVE ->
EXPIRING -> EXPIRED` (terminal), with `FROZEN <-> ACTIVE` and
`CANCELLED` (terminal) branches. Each constant overrides
`allowedTransitions()` to declare its allowed successors; the public
method `canTransitionTo(...)` consults that set so clients never need
to know the rules directly.

### `AccessTier` (enum)

A three-value ordered label: `BASIC`, `STANDARD`, `PREMIUM`. The enum
carries no per-constant code -- it exists so that plans, members, and
notifiers can refer to a typed tier rather than a stringly-typed value.

### `GymEvent` (the Observer event base)

Abstract base class for every event the `Gym` can publish. Holds the
three universal fields (timestamp, targetMember, message) and exposes
`isBroadcast()` plus the abstract `getType()` method that concrete
subclasses must implement. A `targetMember` of `null` marks a
broadcast.

### `PaymentDueEvent`, `RenewalReminderEvent`, `ClassCancelledEvent`, `PromotionEvent`

Four concrete events. The first two are always targeted; class
cancellations may be broadcast or targeted; promotions are always
broadcast. Each subclass adds the type-specific data it needs (due
date and amount, renewal date, class name and class date, discount
percent) and validates those fields in its constructor.

### `MemberNotifier` (Observer interface)

Three-method interface that defines the Observer side of the pattern:
`getMember()`, `getChannel()`, `onEvent(GymEvent)`. Implementations
must ignore events targeted at other members while still delivering
broadcasts.

### `EmailMemberNotifier`, `SmsMemberNotifier`, `PushMemberNotifier`

Three concrete Observers. Each wraps one member and one channel, and
each keeps an internal `sentLog` of every formatted message it has
emitted. The format differs by channel -- email is verbose, SMS is
truncated to 110 characters with an ellipsis, push uses a title +
body shape -- so the same event reaches three differently-formatted
recipients in a single `publishEvent` call.

### `Gym` (the Observer subject)

The single coordinator. Owns the plan catalogue (built outside via
the Builder), the member list, and the chronological event journal.
Its two key methods are `registerPlan(MembershipPlan)` (Builder
consumer) and `publishEvent(GymEvent)` (Observer subject). The latter
is the single point of dispatch -- members never call notifiers
directly.

### `Main`

The scripted demonstration. Six labelled test sections walk through
the Builder, the Observer fabric, the lifecycle state machine, an
integration scenario, the SOLID principles (including a live runtime
notifier swap), and six edge cases. Each section ends with one or
more `[PASS]` lines and the program closes with `ALL TESTS PASSED`.

### `GymManagementApp`

The interactive console driver. Eleven menu options cover every public
API call on `Gym`. Option 2 -- "Build a new plan" -- walks through
every Builder setter interactively, which makes the Builder pattern
visible in a CLI context.

### GUI classes (`gui.GymManagerGUI`, `MemberFormPanel`, `MemberTablePanel`, `MemberTableModel`, `DemoScenarios`)

A small Swing front-end. The member table on the left binds to
`Gym.getAllMembers()`; the enrolment form on the right pulls plan
names from `Gym.getAllPlans()`; the lifecycle and notification
buttons across the bottom drive `Member.setStatus(...)` and
`Gym.publish*(...)` respectively. The dark log strip at the very
bottom prints every event the gym emits, formatted exactly the way
each notifier formatted it -- so the professor literally sees the
Observer pattern firing.

---

## Q&A cheat-sheet

**Q. Why two patterns instead of one?**
A. The assignment requires one Creational and one Behavioral pattern.
Builder is Creational (constructing complex `MembershipPlan` objects),
and Observer is Behavioral (publishing events to a fan-out of
subscribed channels). The two map cleanly to the two complexity
sources in the gym domain -- plan variability and notification
fan-out.

**Q. Why a nested static `Builder` instead of a separate `MembershipPlanBuilder` class?**
A. Convention. Nesting keeps the Product and the Builder in one file,
makes the Product's private constructor accessible to the Builder
without exposing it elsewhere, and matches the JDK style
(`HttpRequest.Builder`, `Locale.Builder`, etc.). The relationship is
also self-documenting at the call site --
`new MembershipPlan.Builder(...)`.

**Q. Why is the `Gym` class the only publisher? Couldn't members publish their own events?**
A. Concentrating publication in one place lets cross-cutting policies
(rate-limiting, quiet hours, opt-outs, auditing) live in one method.
It also makes broadcast vs targeted dispatch uniform: the `Gym` decides
who is in scope before calling `onEvent` on each observer. Letting
members publish would scatter that logic.

**Q. Why does each `MemberNotifier` keep an internal `sentLog`?**
A. It is the cheapest way to make Observer's effect visible. The test
demo asserts on `sentLog.size()`; the GUI reads the gym's event
journal to print the log strip; users can verify the pattern fired by
looking at the printed messages.

**Q. Why allow `null` for `targetMember` in `GymEvent`?**
A. To distinguish broadcasts from targeted events. A `null` target
means "any attached notifier should hear it", which matches real-world
"new offer for everyone" or "class cancellation for everyone signed up".
The `Gym.publishEvent` method branches on `event.isBroadcast()` to
choose between targeted and broadcast dispatch.

**Q. How are invalid status transitions prevented?**
A. The `MembershipStatus` enum is itself a state machine: each
constant declares its allowed targets. `Member.setStatus(...)` calls
`canTransitionTo(...)` and throws `IllegalArgumentException` if the
target is not allowed. Terminal states (`EXPIRED`, `CANCELLED`)
return an empty allowed-set, so nothing can leave them.

**Q. What if I want a new notification channel?**
A. One new class implementing `MemberNotifier`. Then
`member.attachNotifier(new MyNewChannelNotifier(member))`. Test 5 in
`Main.java` does this with an anonymous class defined inline -- the
engine and the existing notifiers stay untouched.

**Q. What if I want a new event kind?**
A. One new class extending `GymEvent`. Optionally one new convenience
method on `Gym` to publish it. Every existing notifier handles the
new event correctly because they only depend on the abstract base
class.

**Q. Why no Maven or Gradle?**
A. The assignment explicitly mandates "pure Java with the standard
library". Maven and Gradle would add dependencies, configuration,
and build-tool noise without changing any architectural property of
the system. A single `javac` line and a single `java` line are
clearer for a demonstration of patterns.

**Q. Where is the Builder pattern actually visible at runtime?**
A. In `MembershipPlan.Builder.build()`. Set a breakpoint there and
watch the working state being validated and turned into the immutable
product. The call site -- e.g., `new MembershipPlan.Builder("Premium
Annual").durationMonths(12)...build()` -- shows the fluent assembly.

**Q. Where is the Observer pattern actually visible at runtime?**
A. In `Gym.publishEvent(...)`. The method dispatches to every
attached `MemberNotifier.onEvent(...)`, polymorphically. Set a
breakpoint there and watch the observer list being iterated.

**Q. Why are GUI classes in a `gui/` subfolder but with no `package gui;` declaration?**
A. Java 8 forbids importing default-package classes from a named
package. The GUI needs to reference engine types (`Gym`, `Member`,
`MembershipPlan`, ...) which live in the default package. Keeping the
GUI in the default package too lets it reference those types
directly. The `gui/` directory is purely organisational.

**Q. How is the project tested without JUnit?**
A. `Main.java` contains six self-checking sections that print
`[PASS]` on success. Edge cases are exercised in Test 6 and scored
out of six. Skipping JUnit avoids any external dependency while
still giving the professor a single deterministic output to verify.

---

## Live-demo checklist

1. **Show the file tree.** `tree -L 3` -- 19 source files split into
   product, pattern, coordination, and entry-point layers.
2. **Run the test demo.** `java -cp bin Main`. Walk past the
   `[PASS]` markers as they fly past. Stop at Test 2 to point out the
   per-channel formatting.
3. **Open the GUI.** `java -jar GymManagerGUI.jar`. Load the
   *Observer demo* from the menu. Walk through the table -- three
   members, three different channel mixes.
4. **Publish an event.** Select Alice, click *Payment due...*, accept
   the defaults. Two lines appear in the dark log strip (email + push).
5. **Publish a broadcast.** Click *Promotion...*, accept the defaults.
   Six lines appear in the log -- every notifier on every member.
6. **Try an invalid transition.** Pick a `PENDING` row, look at the
   lifecycle buttons: most are disabled. Click *Mark expiring* -- the
   engine refuses and the GUI shows the message in a dialog.
7. **Close with the file structure.** Re-emphasise that adding a new
   plan attribute, a new event kind, or a new channel is a single-file
   change -- the gym never changes. That is the project's main
   message.
