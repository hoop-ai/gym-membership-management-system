# Gym Membership Management System

## Design Patterns: Builder and Observer

**Course:** SEN3006 -- Software Architecture
**Project type:** Java Design Pattern Project
**Language:** Java 8 (pure standard library, zero external dependencies)
**Source files:** 19 (1 abstract event class, 4 concrete events, 1 notifier interface, 3 concrete notifiers, 2 enums, Member, MembershipPlan + nested Builder, Gym, 2 non-GUI entry points, 5 GUI classes)
**Deliverables:** Java source ZIP, compiled JAR, PDF report

---

## Table of Contents

1. [Introduction](#31-introduction)
2. [Problem Definition and System Requirements](#32-problem-definition-and-system-requirements)
3. [Design Pattern Explanation](#33-design-pattern-explanation)
4. [System Design and UML Diagrams](#34-system-design-and-uml-diagrams)
5. [Implementation and Code Explanation](#35-implementation-and-code-explanation)
6. [Testing and Demonstration](#36-testing-and-demonstration)
7. [Results and Evaluation](#37-results-and-evaluation)
8. [Conclusion](#38-conclusion)
9. [References](#39-references)

---

## 3.1 Introduction

### Background

A modern gym is, from a software point of view, the intersection of two
recurring complexity sources. The first is **product variability** -- a
gym offers many subtly different membership plans (monthly vs annual,
basic vs premium, with or without group classes, with or without freeze
allowances, with or without personal training), and these plans differ
along many dimensions rather than along a single "type" axis. The second
is **communication fan-out** -- a gym must reach members through
whichever channels they have opted into (email, SMS, push, perhaps
in-app notifications), and the same internal event may need to be
formatted very differently for each channel.

Treated naively, these complexities pile up fast. Plan construction
gravitates toward constructors with seven or eight positional
parameters, or toward a sprawling factory that knows the rules for every
variant. Notification logic gravitates toward `if (channel == "email")
... else if (channel == "sms") ...` chains buried inside the business
logic, and every new channel becomes a code-review-wide change.

Both problems have textbook design-pattern solutions: the **Builder**
pattern for constructing objects with many configurable attributes, and
the **Observer** pattern for fanning a single event out to a set of
interested subscribers. This project implements a complete gym
membership management application that applies both patterns to a
real, working system that demonstrates the patterns' value visibly --
in scripted tests, in a console driver, and in a Swing GUI that lights
up a live event log every time a notification is published.

### Motivation

In an introductory architecture course the goal is not to ship a real
SaaS product; it is to make pattern theory tangible. A gym membership
system is well-suited to that goal because **the value of each pattern
is immediately visible at the call site**. A `MembershipPlan.Builder`
chain reads like a configuration list. An `EmailMemberNotifier` attached
to a `Member` produces a printed message the very next time the gym
publishes an event for that member. The patterns are not buried in
infrastructure; they show up in the demo output and the GUI log.

### Objectives

1. **Demonstrate the Builder pattern** by constructing immutable
   {@link MembershipPlan} instances with many optional, validated
   attributes via a fluent inner class.
2. **Demonstrate the Observer pattern** by publishing typed
   {@link GymEvent} instances from a single `Gym` subject to a
   per-member set of `MemberNotifier` observers.
3. **Apply SOLID principles** throughout the design so the system is
   maintainable, extensible, and testable -- in particular,
   demonstrating Open/Closed live in the scripted test.
4. **Build a complete, working system** in pure Java with zero
   external dependencies, proving that good architecture does not
   require frameworks.

### Solution overview

The Gym Membership Management System has three architectural layers.
The **domain layer** holds `Member`, `MembershipPlan` (with its nested
`Builder`), `MembershipStatus`, and `AccessTier`. The **pattern layer**
holds the Observer fabric (`GymEvent` and its four subclasses,
`MemberNotifier` and its three concrete implementations) plus the
Builder's nested class. The **coordination + presentation layer** holds
the `Gym` class -- the single point of publication for every event --
and the three entry points: `Main` (scripted demo), `GymManagementApp`
(console menu), and `gui.GymManagerGUI` (Swing GUI). All three
front-ends share the same engine; the patterns sit in the engine, not
in the GUI.

---

## 3.2 Problem Definition and System Requirements

### Problem statement

Build a gym membership management application that lets staff
(1) define many distinct membership plans without proliferating
constructors or factories, (2) enrol members against those plans,
(3) move members through the natural membership lifecycle, and
(4) notify members about gym events on whichever channel each member
has subscribed to. The design must allow new plan attributes, new
notification channels, and new event kinds to be added without
modifying any existing class.

### Functional requirements

| ID  | Requirement | Demonstrated by |
|-----|-------------|-----------------|
| FR1 | The system must support membership plans with at least seven configurable attributes (name, duration, price, access tier, included classes, guest passes, freeze days, personal-training flag). | `MembershipPlan` + `MembershipPlan.Builder`. |
| FR2 | Plans must be immutable once constructed; mutation is not allowed. | `MembershipPlan` fields are `private final`. |
| FR3 | Members must be enrollable against a registered plan and identifiable by an auto-generated ID. | `Gym.enrolMember`, `AbstractRecipe`-style ID counter on `Member`. |
| FR4 | Each member must transition through a lifecycle with rules that prevent invalid transitions. | `MembershipStatus` enum + `Member.setStatus` validation. |
| FR5 | The system must publish at least four event kinds (payment due, renewal reminder, class cancellation, promotion). Both targeted and broadcast events must be supported. | `GymEvent` and the four concrete subclasses; `Gym.publishEvent`. |
| FR6 | Each member must be able to subscribe to one or more notification channels (email, SMS, push, ...). Adding a new channel must not require touching the gym or other channels. | `MemberNotifier` interface; three concrete classes; `Member.attachNotifier`. |
| FR7 | The system must offer at least three entry points: a scripted test demo, a console menu, and a GUI. | `Main`, `GymManagementApp`, `gui.GymManagerGUI`. |

### Non-functional requirements

| ID   | Requirement | Approach |
|------|-------------|----------|
| NFR1 | Zero external dependencies. | Only `java.util.*`, `java.time.*`, `javax.swing.*`, `java.awt.*`. |
| NFR2 | Build with a single `javac` command on Java 8+. | Default package; no Maven, Gradle, or build descriptor. |
| NFR3 | Add a new plan attribute, a new notifier channel, or a new event kind without modifying any existing class. | All extension points are abstract; `Gym.publishEvent` dispatches by polymorphism. |
| NFR4 | Errors are surfaced with actionable messages, not stack traces. | Validation in builders, constructors, and setters throws `IllegalArgumentException`. |
| NFR5 | The system must be testable without a test framework. | `Main.java` contains six self-checking sections with `[PASS]` markers. |
| NFR6 | The GUI must be readable on Windows, macOS, and Linux. | Custom table-header renderer and Metal-styled accent buttons override platform L&F where it would otherwise hide background colours. |

### Why architecture matters

Without the patterns described in section 3.3, a fourth membership
plan attribute or a fourth notification channel would touch every
caller, every conditional, and every test. With the patterns in place
these changes are local: a new `MembershipPlan.Builder` setter or a new
class implementing `MemberNotifier`. NFR3 is the explicit form of this
guarantee, and Test 5 in `Main.java` proves it at runtime by attaching
a brand-new anonymous notifier without recompiling the engine.

---

## 3.3 Design Pattern Explanation

### 3.3.1 Builder Pattern (Creational)

#### Definition

The Builder pattern, as introduced by Gamma et al. (1994), "separates
the construction of a complex object from its representation so that
the same construction process can create different representations".
In its modern Java form (popularised by Bloch's *Effective Java*) the
emphasis shifts slightly: the Builder is a fluent inner class that
collects parameters one at a time and then validates them centrally in
a final `build()` call that returns an immutable Product.

#### When and why it is used

Use the Builder pattern when:

- A class has many constructor parameters (Bloch's heuristic: three or
  more, especially if several are optional).
- Several parameters are optional and the call site should not have to
  pass `null` or zero for them.
- The object must be immutable after construction.
- Validation rules cross multiple parameters and should run in a
  single, well-defined place.

`MembershipPlan` ticks every box: eight attributes, several optional,
required immutability, cross-field validation (`durationMonths` must be
positive, `monthlyFee` must be non-negative, `accessTier` must be set,
etc.).

#### Advantages

- **Readable call sites.** Configuration looks vertical, not positional.
- **Immutable product.** Once built, plans cannot drift; references can
  be shared freely.
- **Centralised validation.** Every plan that exists has been validated
  by the same routine.
- **Open/Closed for new attributes.** Adding a ninth attribute means
  one new field on `MembershipPlan`, one new method on `Builder`, and
  no changes to the call sites that do not need the new attribute.

#### Why suitable for this project

The assignment requires demonstrable extensibility for plan attributes.
Builder makes that property structural rather than aspirational:
extending the plan with a new field changes only the plan and the
builder, never any caller. The pattern also makes the code at the
demo's call site readable enough to put on a presentation slide.

#### Real-world example

`java.lang.StringBuilder` is the JDK's canonical Builder. More
contemporary examples include `HttpRequest.Builder` (JDK 11+),
`Stream.Builder` (JDK 8+), and almost every fluent API in Spring's
configuration layer.

### 3.3.2 Observer Pattern (Behavioral)

#### Definition

The Observer pattern "defines a one-to-many dependency between objects
so that when one object changes state, all its dependents are notified
and updated automatically" (Gamma et al., 1994). A Subject maintains a
list of Observers and exposes a method (`attach`, `subscribe`,
`addObserver`, ...) to register new ones; when the Subject's state
changes, it walks the list and calls each Observer's `update` method.

#### When and why it is used

Use the Observer pattern when:

- An abstraction has two aspects, one dependent on the other.
- A change to one object requires changing others, and you do not know
  in advance how many objects need to change.
- An object should be able to notify other objects without making
  assumptions about who those objects are.

The gym fits exactly: a single internal change (e.g., recognising that
a payment is due) needs to reach an unknown set of subscribed channels
for the affected member. The gym should not have to encode the cross
product of (event kind) x (channel) anywhere.

#### Advantages

- **Loose coupling.** The Subject knows the Observer interface only.
- **Runtime subscription.** Observers attach and detach dynamically.
- **Broadcast vs targeted, uniformly.** Both cases use the same
  publish call; the Subject decides who is in scope.
- **Open/Closed.** A new channel is a new Observer class. The Subject
  is untouched.

#### Why suitable for this project

The four event kinds and three channels in this system produce a 12-cell
matrix in a naive design. Observer collapses that matrix into 4 + 3
classes, with the `Gym` class as the single point of dispatch. The
GUI's notification log strip makes the pattern visible -- every event
emitted by the engine becomes a printed line in the dark log at the
bottom of the window.

#### Real-world example

`java.util.Observer` / `java.util.Observable` were the JDK's textbook
implementations (deprecated in JDK 9 in favour of more modern
abstractions). Swing's `ActionListener`, `PropertyChangeListener`, and
`TableModelListener` are all Observer implementations. Reactive
streams (`Flow.Subscriber` in JDK 9+) are a generalised Observer.

---

## 3.4 System Design and UML Diagrams

The system is organised in three logical layers:

1. **Domain layer** -- `Member`, `MembershipPlan` (with nested `Builder`),
   `MembershipStatus` enum, `AccessTier` enum.
2. **Pattern layer** -- `GymEvent` abstract class and four concrete events
   (`PaymentDueEvent`, `RenewalReminderEvent`, `ClassCancelledEvent`,
   `PromotionEvent`); `MemberNotifier` interface and three concrete
   implementations (`EmailMemberNotifier`, `SmsMemberNotifier`,
   `PushMemberNotifier`).
3. **Coordination + presentation layer** -- `Gym` (the single coordinator),
   `Main` (scripted demo), `GymManagementApp` (console menu), and the
   five-file Swing GUI under `gui/`.

### Class diagram

See [docs/uml/class/class-diagram.md](../uml/class/class-diagram.md)
(Mermaid render) and
[docs/uml/class/gym-management-class.puml](../uml/class/gym-management-class.puml)
(PlantUML source).

### Sequence diagram

The two principal flows -- building a plan via the Builder and
publishing an event to a member's attached notifiers -- are diagrammed
in [docs/uml/sequence/sequence-diagram.md](../uml/sequence/sequence-diagram.md).

### State diagram

The membership lifecycle is rendered in
[docs/uml/activity/state-diagram.md](../uml/activity/state-diagram.md).

### Activity diagram

The end-to-end flow of a member from enrolment to expiration appears in
[docs/uml/activity/activity-diagram.md](../uml/activity/activity-diagram.md).

### Use-case diagram

Every operation a gym staffer can perform is enumerated in
[docs/uml/usecase/usecase-diagram.md](../uml/usecase/usecase-diagram.md).

### Component and deployment diagrams

[docs/uml/class/component-diagram.md](../uml/class/component-diagram.md)
shows the three logical layers.
[docs/uml/class/deployment-diagram.md](../uml/class/deployment-diagram.md)
shows that the entire system runs in a single JVM with three optional
entry points.

---

## 3.5 Implementation and Code Explanation

### The Product hierarchy (Builder side)

`MembershipPlan` is a final class with eight private-final fields and
no setters -- once built, a plan is immutable. The nested
`MembershipPlan.Builder` is a static inner class that holds a working
copy of every attribute, exposes one fluent setter per attribute, and
validates the entire set inside `build()`. The constructor is private;
only the Builder can construct plans.

The decision to nest the Builder inside `MembershipPlan` is deliberate.
It keeps the Product and the Builder in one file, avoids exposing the
private constructor, and signals the relationship in the type name
(`MembershipPlan.Builder`). The pattern reads identically to
`StringBuilder` / `String`, `HttpRequest.Builder` / `HttpRequest`, and
the many other examples in the JDK.

### Member and the lifecycle

`Member` holds the universal member data (id, name, email, phone, join
timestamp, current plan, status, renewal date) plus a list of attached
`MemberNotifier` instances. `setStatus(...)` consults
`MembershipStatus.canTransitionTo(...)` and throws
`IllegalArgumentException` if the transition is not allowed. Side
effect: when moving back to `ACTIVE` from `EXPIRING` (a renewal) or
from `FROZEN` (a resume), the renewal date is pushed forward by the
plan duration.

### The event hierarchy (Observer side)

`GymEvent` is an abstract base class with three universal fields
(`timestamp`, `targetMember`, `message`). Four concrete subclasses add
the type-specific data they need: `PaymentDueEvent` (date + amount),
`RenewalReminderEvent` (renewal date), `ClassCancelledEvent` (class
name + class date, broadcastable), `PromotionEvent` (discount percent,
always a broadcast).

`isBroadcast()` returns `true` when `targetMember` is null. The `Gym`
uses this flag to choose between "iterate the target's notifiers" and
"iterate every member's notifiers".

### The Observer hierarchy

`MemberNotifier` is a three-method interface: `getMember()`,
`getChannel()`, and `onEvent(GymEvent)`. Each concrete implementation
wraps one member and one channel. `onEvent` is responsible for
ignoring events that target a different member -- broadcasts are
delivered to every attached notifier.

Each concrete notifier also keeps an internal `sentLog` of every
formatted message it has emitted. The log is what the test demo and the
GUI read to prove that the Observer pattern actually delivered the
event -- nothing is faked, every line in the log corresponds to a real
call to `onEvent`.

### The coordinator

`Gym` is the single class that ties everything together. It owns the
plan catalogue (built outside via the Builder), the member list, and
the chronological event journal. The two key methods are
`registerPlan(MembershipPlan)` (Builder consumer) and
`publishEvent(GymEvent)` (Observer subject). Both methods touch only
abstractions -- `Gym` never references `EmailMemberNotifier` or
`PaymentDueEvent` directly.

### The three entry points

- `Main.java` runs six labelled test sections that exercise both
  patterns, the lifecycle, and six edge cases, printing `[PASS]` for
  each successful check.
- `GymManagementApp.java` offers a console menu so a non-graphical
  demo can drive every public API call.
- `gui.GymManagerGUI` opens a Swing window. The member table on the
  left is bound to `Gym.getAllMembers()`. The enrolment form on the
  right pulls plan names from `Gym.getAllPlans()`. The lifecycle and
  notification buttons at the bottom call the matching
  `Gym.changeMemberStatus` and `Gym.publish*` methods. The dark log
  strip at the very bottom records every event the gym publishes,
  rendered identically to the messages each notifier produces.

---

## 3.6 Testing and Demonstration

### Test methodology

The project uses a lightweight, framework-free testing approach.
`Main.java` contains six self-checking sections, each of which prints a
clearly-labelled `[PASS]` line on success and a `FAIL` line on failure.
This style is appropriate for a classroom project where the goal is a
visible, presentable demonstration rather than an industrial test
pipeline.

| Test | Verifies |
|------|----------|
| 1 -- Builder demo | Three distinct plans built via fluent chains; each is immutable; the basic plan uses defaults, the premium plan exercises every optional attribute. |
| 2 -- Observer demo | A targeted event reaches only the affected member's notifiers; broadcast events reach every attached notifier across the gym; channel-specific formatting is applied. Assertions on the per-notifier `sentLog` sizes confirm correct delivery counts. |
| 3 -- Lifecycle demo | Every allowed `MembershipStatus` transition succeeds; every disallowed transition throws `IllegalArgumentException`; terminal states (`EXPIRED`, `CANCELLED`) reject every onward transition. |
| 4 -- Integration demo | A full workflow -- register plans (Builder), enrol members, attach notifiers, walk the lifecycle, publish targeted and broadcast events, summarise. |
| 5 -- SOLID demo | A brand-new `MemberNotifier` (defined inline as an anonymous class) is attached to a member at runtime, and the next published event reaches it. Open/Closed and Dependency Inversion are exercised live. |
| 6 -- Edge cases | Six guarded failure modes: builder rejects blank name, builder rejects zero duration, gym rejects unknown plan name, gym rejects unknown member ID, notifier ignores events for other members, detach stops further delivery. |

### How to run

```sh
javac -d bin src/main/java/*.java src/main/java/gui/*.java
java -cp bin Main
```

The program ends with `ALL TESTS PASSED`. Detailed test documentation
is in
[docs/design/test-documentation.md](../design/test-documentation.md).

### GUI demonstration

The Swing GUI offers a visual companion to the scripted tests. The
**Demos** menu loads the same data sets the test sections build, the
lifecycle buttons at the bottom drive `Member.setStatus(...)`, and the
notification buttons publish events through `Gym.publishEvent(...)`.
Every event the gym emits is appended to the dark log strip in real
time, with channel-specific formatting visible side by side.

---

## 3.7 Results and Evaluation

### Quantitative results

| Metric | Value |
|--------|-------|
| Source files | 19 (`.java`) |
| Production code lines | ~1,800 |
| External dependencies | 0 |
| Build commands required | 1 (`javac`) |
| Automated test sections | 6 |
| Edge cases covered | 6 |
| Membership-plan attributes | 8 (extensible) |
| Notification channels | 3 (extensible) |
| Event kinds | 4 (extensible) |
| Lifecycle states | 6 |
| Allowed transitions | 9 (out of 30 possible state pairs) |

### Pattern verification

Both target patterns are demonstrably implemented:

- **Builder.** Outside `MembershipPlan.Builder` itself, no other class
  invokes the private constructor of `MembershipPlan`. A textual search
  for `new MembershipPlan(` returns one hit and it lives inside the
  Builder. Adding a hypothetical ninth attribute (e.g., "guest passes
  per quarter") is a one-class change to `MembershipPlan` plus one new
  setter on `Builder` -- no caller breaks.
- **Observer.** `Gym.publishEvent(...)` calls only `MemberNotifier.onEvent(...)`
  through the interface reference. The test demo installs a brand-new
  anonymous `MemberNotifier` at runtime, attaches it to a member, and
  the next published event reaches it without recompiling the engine.

### SOLID assessment

| Principle | How the system satisfies it |
|-----------|-----------------------------|
| **S**ingle Responsibility | Each class has one job: Builder constructs, Member holds state, MembershipStatus governs transitions, each event subclass carries one kind of payload, each notifier writes one channel, the Gym coordinates and publishes. |
| **O**pen/Closed | New plan attributes, new event kinds, new notification channels are all single-file additions. Test 5 demonstrates this live with an anonymous notifier installed at runtime. |
| **L**iskov Substitution | Every notifier works through the `MemberNotifier` reference; every event works through the `GymEvent` reference. The test demo substitutes them explicitly. |
| **I**nterface Segregation | `MemberNotifier` has three methods, all used by every implementation. `GymEvent` exposes only universal fields plus `getType()`. Type-specific accessors stay on concrete events. |
| **D**ependency Inversion | `Gym` references only abstractions (`MemberNotifier`, `GymEvent`, `Member`). No `new EmailMemberNotifier(...)` outside the demo and the GUI's wiring. |

### Limitations and future work

The system intentionally stays small to keep the focus on the two
patterns. Three obvious extensions would not change the architecture
but would broaden the demo:

1. **Persistence.** Today the gym lives only in memory. A
   `GymRepository` abstraction with JSON or SQLite backends would add
   a third pattern (Repository) without disturbing the existing
   classes.
2. **Scheduling.** A `MembershipScheduler` running on a timer could
   raise renewal-reminder events automatically based on each member's
   renewal date.
3. **Internationalisation.** Notification messages are stored as plain
   strings. A resource-bundle-based message catalogue would let the
   gym run in multiple languages.

---

## 3.8 Conclusion

This project delivers a complete, working Gym Membership Management
System that satisfies every functional and non-functional requirement
of the SEN3006 assignment. Two design patterns -- Builder and Observer
-- form the architectural spine, and a lightweight State machine adds a
third pattern incidentally inside `MembershipStatus`. The SOLID
principles are not retro-fitted or claimed; they are visible structural
properties of the code, each demonstrated by at least one test section.

Three entry points (scripted demo, console menu, Swing GUI) prove that
the same engine supports very different presentation layers without
modification. The Observer pattern is especially striking in the GUI:
publishing an event from a button click immediately appears in the
notification log strip at the bottom of the window, formatted
differently for each channel attached to the affected member. Adding a
brand-new notifier at runtime in Test 5 requires zero changes to any
existing class. That visible extensibility is the project's main
pedagogical message: good architecture is not decoration, it is the
property of a system that says yes to the next reasonable change.

---

## 3.9 References

1. Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994).
   *Design Patterns: Elements of Reusable Object-Oriented Software*.
   Addison-Wesley. (The original "Gang of Four" book; defines the
   Builder and Observer patterns used here.)
2. Bloch, J. (2018). *Effective Java*, 3rd ed. Addison-Wesley.
   (Items 2, 17, 20, 34: builders for many parameters, minimising
   mutability, interface design, enum types -- all applied in this
   project.)
3. Martin, R. C. (2002). *Agile Software Development, Principles,
   Patterns, and Practices*. Prentice Hall. (Introduced the SOLID
   acronym and the Open/Closed motivation used in section 3.3.)
4. Freeman, E., Robson, E., Bates, B., & Sierra, K. (2004).
   *Head First Design Patterns*. O'Reilly. (Accessible treatment of
   the Builder and Observer patterns with similar Java examples.)
5. Oracle Corporation. (n.d.). *The Java Tutorials -- Collections,
   Enums, Generics, Swing*. https://docs.oracle.com/javase/tutorial/
   (Reference for the standard-library features used: collections,
   `LocalDate`, enum methods, Swing widgets.)
