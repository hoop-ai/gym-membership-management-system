# Gym Membership Management System

## Design Patterns: Builder and Observer

**Course:** SEN3006 -- Software Architecture
**Project:** Gym Membership Management System (Java)
**Team:** Elif (single-author submission)
**Date:** 2026-05-25
**Language:** Java 8+ (standard library only, zero external dependencies)
**Source files:** 16 `.java` files in the default package
**Deliverables:** Java source archive, runnable JAR, this PDF report, slide deck

---

## Table of Contents

1. [Problem Definition](#1-problem-definition)
2. [Project Objectives](#2-project-objectives)
3. [Solution Approach](#3-solution-approach)
4. [Domain Model and Class Diagram](#4-domain-model-and-class-diagram)
5. [Builder Pattern](#5-builder-pattern)
6. [Observer Pattern](#6-observer-pattern)
7. [SOLID Principles](#7-solid-principles)
8. [Sample Run](#8-sample-run)
9. [Conclusions and Future Work](#9-conclusions-and-future-work)
10. [References](#10-references)

---

## 1. Problem Definition

A gym schedules fitness classes, registers members, and lets members enrol in classes. From a software design point of view, two recurring complexity sources sit underneath that simple description.

The first is **constructive complexity**. A fitness class is not just a name. Even a minimal model carries a day of the week, a start time, a duration, a capacity, a room, a difficulty, an equipment list, and a free-text description -- ten attributes in total, eight of which a typical caller does not want to specify by hand. A naive Java translation lands on either a ten-argument constructor (unreadable, position-dependent, easy to miscount) or a setter-based POJO (no immutability, no centralised validation, no way to guarantee a class is well-formed by the time it leaves construction). Both shapes are well-known anti-patterns; both deserve a better answer.

The second is **reactive complexity**. The gym needs to react when state changes. When a member is added, a console operator wants to see it; an auditor wants the change recorded on disk; an in-process journal wants the event for later inspection. Hard-coding three reactions into the gym's business logic couples the gym to every reaction, and adding a fourth reaction would mean editing the gym. The classical solution is to invert the flow: let the gym announce that something happened and let interested parties listen.

This project takes both problems seriously and applies the textbook design patterns that solve them: **Builder** for fitness-class construction and **Observer** for event publication. Both patterns are demonstrated through a single scripted demo that runs in under a second and produces deterministic, inspectable output.

---

## 2. Project Objectives

The project targets four concrete objectives.

1. **Demonstrate the Builder pattern** on a class with many optional, validated attributes. The chosen Product is `FitnessClass`, which has two required fields and eight optional fields with defaults.
2. **Demonstrate the Observer pattern** end-to-end. A single `Gym` subject publishes six kinds of events to three concrete observers (console, audit file, in-memory journal). Observers register at runtime and can be detached without touching the gym.
3. **Apply SOLID principles** so each class has one reason to change and so adding a new observer or a new event kind does not modify any existing class.
4. **Stay minimal.** Java 8 standard library only; one entry point (`Main.java`); one build command; one run command. No frameworks, no GUI, no persistence beyond a plain-text audit log.

The system is intentionally small. It is a teaching artefact -- the patterns are visible at the call site, not buried inside infrastructure.

---

## 3. Solution Approach

Two patterns from the Gang of Four catalogue (Gamma et al., 1994) were selected because the domain demanded them rather than the other way around.

**Builder (Creational)** was chosen for `FitnessClass` because the class has ten configurable attributes and the natural call site -- the scripted demo -- needs to construct three quite different classes ("Yoga Flow", "Spin Express", "HIIT 45") with overlapping defaults and per-instance overrides. Section 5 lays out the rationale in detail, including the explicit alternatives that were considered and rejected.

**Observer (Behavioral)** was chosen because three independent reactions (stdout, file, in-memory list) all need to react to the same internal events. The gym must remain unaware of how many reactions exist or what they do. Section 6 makes the case in detail and contrasts the chosen design with the alternatives that were considered.

Both patterns share a key trait: they make extension a **local** operation. Adding a tenth attribute to `FitnessClass` touches `FitnessClass` and its nested `Builder` only; adding a fourth observer is one new class and one `addObserver(...)` call at the demo's entry point. No existing class needs to be modified for either change. This is the Open/Closed Principle realised structurally rather than asserted in prose.

---

## 4. Domain Model and Class Diagram

The domain is intentionally small. The gym owns members, classes, and observers. Members enrol into classes. Every state change publishes an event.

| Entity         | Purpose                                                      |
|----------------|--------------------------------------------------------------|
| `Member`       | Immutable POJO with an auto-assigned id, name, and email     |
| `FitnessClass` | Immutable schedule entry constructed via its nested Builder  |
| `Gym`          | Aggregate root and single point of event publication         |
| `GymEvent`     | Abstract base; six concrete subclasses carry typed payloads  |
| `GymEventObserver` | Interface; three concrete observers ship with the system |

Five operations cover the entire public surface of `Gym`: add member, remove member, add class, remove class, and enrol/drop. Each operation that mutates state publishes exactly one event.

The full class diagram is rendered as Mermaid in [`docs/uml/class/class-diagram.md`](../uml/class/class-diagram.md), with the matching sequence diagram in [`docs/uml/sequence/sequence-diagram.md`](../uml/sequence/sequence-diagram.md). Three structural observations carry the architecture:

- The arrow from `FitnessClass.Builder` to `FitnessClass` is the Builder's build contract. No other class in the system invokes `FitnessClass`'s private constructor; only the Builder can.
- The arrow from `Gym` to `GymEventObserver` points at the **interface**, not at any concrete observer. The gym never imports `ConsoleObserver`, `AuditFileObserver`, or `InMemoryJournalObserver`. Dependency Inversion is a visible property of the diagram.
- The six concrete `GymEvent` subclasses share an abstract base with the three universal fields (`timestamp`, `type`, `message`). The gym treats every event as a `GymEvent`; the observer decides what (if anything) to do with the typed payload.

The source layout reflects the same shape:

| Layer        | Files                                                                                                                  |
|--------------|------------------------------------------------------------------------------------------------------------------------|
| Domain       | [`Member.java`](../../src/main/java/Member.java), [`FitnessClass.java`](../../src/main/java/FitnessClass.java), [`Difficulty.java`](../../src/main/java/Difficulty.java) |
| Pattern (Builder) | [`FitnessClass.java`](../../src/main/java/FitnessClass.java) (nested `Builder`)                                  |
| Pattern (Observer, events) | [`GymEvent.java`](../../src/main/java/GymEvent.java), [`MemberAddedEvent.java`](../../src/main/java/MemberAddedEvent.java), [`MemberRemovedEvent.java`](../../src/main/java/MemberRemovedEvent.java), [`ClassAddedEvent.java`](../../src/main/java/ClassAddedEvent.java), [`ClassRemovedEvent.java`](../../src/main/java/ClassRemovedEvent.java), [`MemberEnrolledInClassEvent.java`](../../src/main/java/MemberEnrolledInClassEvent.java), [`MemberDroppedFromClassEvent.java`](../../src/main/java/MemberDroppedFromClassEvent.java) |
| Pattern (Observer, observers) | [`GymEventObserver.java`](../../src/main/java/GymEventObserver.java), [`ConsoleObserver.java`](../../src/main/java/ConsoleObserver.java), [`AuditFileObserver.java`](../../src/main/java/AuditFileObserver.java), [`InMemoryJournalObserver.java`](../../src/main/java/InMemoryJournalObserver.java) |
| Coordinator  | [`Gym.java`](../../src/main/java/Gym.java)                                                                              |
| Entry point  | [`Main.java`](../../src/main/java/Main.java)                                                                            |

Sixteen Java files. No subpackages, no frameworks, no build tool descriptor.

---

## 5. Builder Pattern

### Rationale

`FitnessClass` (see [`FitnessClass.java`](../../src/main/java/FitnessClass.java)) has ten fields:

| Field            | Required? | Default                |
|------------------|-----------|------------------------|
| `name`           | yes       | --                     |
| `instructor`     | yes       | --                     |
| `dayOfWeek`      | no        | `MONDAY`               |
| `startTime`      | no        | `18:00`                |
| `durationMinutes`| no        | `60` (>= 15)           |
| `capacity`       | no        | `20` (>= 1)            |
| `room`           | no        | `"Main floor"`         |
| `difficulty`     | no        | `INTERMEDIATE`         |
| `equipment`      | no        | empty list             |
| `description`    | no        | `""`                   |

Two natural alternatives were considered and rejected.

**A ten-argument constructor was rejected** because the call site degenerates into a positional puzzle: `new FitnessClass("Yoga Flow", "Sarah Lin", MONDAY, LocalTime.of(18, 30), 60, 20, "Studio A", BEGINNER, equipmentList, "Slow-flow ...")`. Swapping any two same-typed arguments compiles silently. Telescoping overloads (one per common subset of optional fields) explode combinatorially.

**A setter-based POJO was rejected** because `FitnessClass` must be immutable. Once a class is added to the gym schedule and members are enrolled, changing its capacity or its start time underneath that enrolment list is a defect. Immutability also lets the gym share a single `FitnessClass` reference across structures without defensive copies.

The Builder pattern solves both: a fluent, named-setter chain assembles the configuration, then `build()` performs centralised validation and returns an immutable Product. The nested `Builder` is a static inner class of `FitnessClass`, mirroring the JDK idiom familiar from `HttpRequest.Builder` and `Stream.Builder`.

### Implementation

The Product is a `final` class with `private final` fields and a `private` constructor (see [`FitnessClass.java`](../../src/main/java/FitnessClass.java)). The nested Builder takes the two required fields in its constructor and exposes one fluent setter per optional field. Each setter validates its single argument and returns `this`. The `build()` method re-validates the combined state and is the only place that calls the private constructor.

A typical Builder call site from the demo ([`Main.java`](../../src/main/java/Main.java)):

```java
FitnessClass yoga = new FitnessClass.Builder("Yoga Flow", "Sarah Lin")
    .dayOfWeek(DayOfWeek.MONDAY)
    .startTime(LocalTime.of(18, 30))
    .capacity(20)
    .room("Studio A")
    .difficulty(Difficulty.BEGINNER)
    .addEquipment("Yoga mat")
    .build();
```

The call site reads like a configuration list, with one named setter per intentional override. Defaults for the eight optional fields kick in automatically; a caller that only wants the bare minimum writes `new FitnessClass.Builder("Yoga Flow", "Sarah Lin").build()` and gets a valid class.

Adding an eleventh attribute is a two-file diff (one new field on `FitnessClass`, one new setter on `Builder`) that does not touch any caller who does not need the new attribute. That is the Open/Closed Principle purchased structurally rather than promised in prose.

---

## 6. Observer Pattern

### Rationale

The gym needs to react to six state changes (member added, member removed, class added, class removed, member enrolled, member dropped). Three independent consumers want every reaction: a live console for the operator, a persistent audit log for the auditor, and an in-process journal for the final summary dump. The number of consumers is not fixed in principle -- a future REST endpoint, a metrics counter, or a unit test could plausibly want to listen as well.

Two natural alternatives were considered and rejected.

**Direct method calls were rejected** because they couple the gym to every consumer. `Gym.addMember(...)` would have to read something like `console.print(...); auditFile.write(...); journal.add(...);` -- three hard-coded calls. Adding a fourth consumer means editing the gym; removing the audit observer at runtime is impossible.

**An external pub/sub library was rejected** because it would introduce a dependency for a problem the standard library already solves cleanly. The project's non-functional requirement of zero external dependencies is not aesthetic; it makes the build a single `javac` command and the deliverable a single zip.

The Observer pattern solves the problem with three textbook pieces:

- A `GymEventObserver` interface ([`GymEventObserver.java`](../../src/main/java/GymEventObserver.java)) with one method, `onEvent(GymEvent)`.
- A `GymEvent` abstract base ([`GymEvent.java`](../../src/main/java/GymEvent.java)) and six concrete subclasses that carry typed payloads.
- A `Gym` subject ([`Gym.java`](../../src/main/java/Gym.java)) that holds a list of observers, exposes `addObserver(...)` / `removeObserver(...)`, and routes every state change through a single private `publish(GymEvent)` method.

### Implementation

Observers register at runtime and the gym is unaware of their concrete types. The single publication site is the private `publish(...)` method on `Gym`, which iterates the observer list and calls `onEvent(...)` on each. Every mutating public method on `Gym` ends with one call to `publish(...)`, and only `publish(...)` touches the observer list -- a single point of fan-out makes the dispatch flow trivial to audit.

The three concrete observers each carry one responsibility. `ConsoleObserver` ([`ConsoleObserver.java`](../../src/main/java/ConsoleObserver.java)) calls `System.out.println(event.toString())`. `AuditFileObserver` ([`AuditFileObserver.java`](../../src/main/java/AuditFileObserver.java)) appends the same string to `audit.log` and is contractually forbidden from throwing -- a failed disk write becomes a one-time stderr warning, never an exception that breaks the publisher. `InMemoryJournalObserver` ([`InMemoryJournalObserver.java`](../../src/main/java/InMemoryJournalObserver.java)) appends events to a list that the demo dumps at the end.

A typical attach-then-publish flow from the demo ([`Main.java`](../../src/main/java/Main.java)):

```java
Gym gym = new Gym("FitLife Centre");
gym.addObserver(new ConsoleObserver());
gym.addObserver(new AuditFileObserver("audit.log"));
gym.addObserver(new InMemoryJournalObserver());

gym.addMember("Sarah Connor", "sarah@fitlife.example");
// ConsoleObserver prints; AuditFileObserver appends; InMemoryJournalObserver records.
```

`Gym.addMember(...)` constructs a `MemberAddedEvent` and calls `publish(...)` once. The three observers each receive the event in the order they were registered. Adding a fourth observer at the call site -- for example, a unit-test-only sink -- requires zero changes to `Gym` and zero changes to any existing observer.

The events themselves carry typed payloads: `MemberEnrolledInClassEvent` holds a `Member` reference plus a `FitnessClass` reference, with accessors for both. An observer interested in the typed payload can downcast, but the shipped observers all work with the abstract base's `toString()` -- which is sufficient for printing, persisting, and journalling.

---

## 7. SOLID Principles

The five principles are stated by reference to concrete classes in the source rather than as abstract claims.

**Single Responsibility.** Each class has one reason to change. `FitnessClass` ([`FitnessClass.java`](../../src/main/java/FitnessClass.java)) holds the data of one scheduled class and the enrolment list. `Gym` ([`Gym.java`](../../src/main/java/Gym.java)) coordinates and publishes; no other class publishes. `AuditFileObserver` ([`AuditFileObserver.java`](../../src/main/java/AuditFileObserver.java)) appends to one file. Splitting `Gym` into separate coordinator and publisher classes was considered and rejected -- the publication site is a single private method, three lines long, and pulling it into its own class would add ceremony without reducing the gym's surface.

**Open/Closed.** Adding a new event kind (for example, a class-rescheduled event) is a single new subclass of [`GymEvent.java`](../../src/main/java/GymEvent.java) plus one new line in the `Gym` method that triggers it. Adding a new observer (for example, a metrics sink that counts events per type) is one new class implementing `GymEventObserver` plus one `addObserver(...)` call in `Main`. Neither extension modifies any existing class. The pattern's openness is not a written claim; it is a property the diagram makes visible.

**Liskov Substitution.** Every observer is interchangeable from the gym's perspective because the gym only ever references the `GymEventObserver` interface. The three concrete observers obey the interface's contract -- in particular, `AuditFileObserver` honours the never-throw rule by swallowing `IOException`. Substituting any observer for any other (or attaching all three at once) produces a behaviourally consistent system.

**Interface Segregation.** `GymEventObserver` ([`GymEventObserver.java`](../../src/main/java/GymEventObserver.java)) has exactly one method. Observers are not forced to implement methods they do not need. A counter-design where the interface had separate `onMemberAdded`, `onMemberRemoved`, `onClassAdded`, etc. methods would force every observer to override every callback; the single-method design lets each observer ignore the typed payload it does not care about.

**Dependency Inversion.** `Gym` references only abstractions: `GymEventObserver` (interface) and `GymEvent` (abstract base). It never imports `ConsoleObserver`, `AuditFileObserver`, or any concrete event subclass at use sites -- it constructs concrete `GymEvent` subclasses only because the events are pure data carriers. The wiring from concrete observers to the gym lives in `Main` ([`Main.java`](../../src/main/java/Main.java)) and only in `Main`.

---

## 8. Sample Run

The demo is invoked with one command after the build step:

```sh
build.bat            REM compiles to bin/ and packages gym.jar
run.bat              REM java -jar gym.jar
```

`Main` resets `audit.log`, attaches the three observers, builds three fitness classes, registers three members, performs four enrolments and two removals, exercises three intentional errors (each caught and reported), and finishes by dumping the in-memory journal. Total runtime is well under one second; the program always exits with code 0.

The verbatim transcript captured from a real run follows:

```text
========================================================================
   GYM MEMBERSHIP MANAGEMENT SYSTEM — Builder + Observer Demo
========================================================================

--- 1. Setting up the gym ----------------------------------------------
Created gym: FitLife Centre
Attached 3 observers: Console, AuditFile (audit.log), InMemoryJournal

--- 2. Adding fitness classes (Builder pattern) ------------------------
[21:54:38] [CLASS_ADDED] Class added: 'Yoga Flow' with Sarah Lin
[21:54:38] [CLASS_ADDED] Class added: 'Spin Express' with Tom Reyes
[21:54:38] [CLASS_ADDED] Class added: 'HIIT 45' with Aisha Patel

--- 3. Adding members --------------------------------------------------
[21:54:38] [MEMBER_ADDED] Member added: Sarah Connor (id=1)
[21:54:38] [MEMBER_ADDED] Member added: Tom Hardy (id=2)
[21:54:38] [MEMBER_ADDED] Member added: Aisha Tyler (id=3)

--- 4. Enrolling members in classes (Observer pattern) -----------------
[21:54:38] [MEMBER_ENROLLED] Sarah Connor enrolled in 'Yoga Flow'
[21:54:38] [MEMBER_ENROLLED] Tom Hardy enrolled in 'Spin Express'
[21:54:38] [MEMBER_ENROLLED] Aisha Tyler enrolled in 'HIIT 45'
[21:54:38] [MEMBER_ENROLLED] Sarah Connor enrolled in 'HIIT 45'

--- 5. Drops and removals ----------------------------------------------
[21:54:38] [MEMBER_DROPPED] Tom Hardy dropped from 'Spin Express'
[21:54:38] [MEMBER_REMOVED] Member removed: Tom Hardy (id=2)

--- 6. Error handling demo (each error is caught, no crash) ------------
✓ Caught: No member found with ID 99. Known IDs: 1, 3.
✓ Caught: Cannot add class: 'yoga flow' already exists.
✓ Caught: Cannot enrol Sarah Connor in 'Yoga Flow': already enrolled.

--- 7. Final journal dump (InMemoryJournalObserver) --------------------
12 events captured:
  [21:54:38] [CLASS_ADDED     ] Class added: 'Yoga Flow' with Sarah Lin
  [21:54:38] [CLASS_ADDED     ] Class added: 'Spin Express' with Tom Reyes
  [21:54:38] [CLASS_ADDED     ] Class added: 'HIIT 45' with Aisha Patel
  [21:54:38] [MEMBER_ADDED    ] Member added: Sarah Connor (id=1)
  [21:54:38] [MEMBER_ADDED    ] Member added: Tom Hardy (id=2)
  [21:54:38] [MEMBER_ADDED    ] Member added: Aisha Tyler (id=3)
  [21:54:38] [MEMBER_ENROLLED ] Sarah Connor enrolled in 'Yoga Flow'
  [21:54:38] [MEMBER_ENROLLED ] Tom Hardy enrolled in 'Spin Express'
  [21:54:38] [MEMBER_ENROLLED ] Aisha Tyler enrolled in 'HIIT 45'
  [21:54:38] [MEMBER_ENROLLED ] Sarah Connor enrolled in 'HIIT 45'
  [21:54:38] [MEMBER_DROPPED  ] Tom Hardy dropped from 'Spin Express'
  [21:54:38] [MEMBER_REMOVED  ] Member removed: Tom Hardy (id=2)

--- Done ---------------------------------------------------------------
audit.log written with 12 lines.
```

Three observations on the transcript. First, every line in sections 2-5 is **printed by `ConsoleObserver`**, not by `Main` itself -- the demo never calls `System.out.println` for state changes. The console output is proof that the Observer fan-out reached at least one observer. Second, the `audit.log` file produced alongside the run contains the same twelve lines written by `AuditFileObserver`; the report's final line confirms the file count. Third, the journal dump in section 7 is produced by `InMemoryJournalObserver.getJournal()` and proves the third observer also received every event -- three independent observers, identical event count, no coordination between them. The Observer pattern delivered.

The error-handling section deserves a separate note. The three caught `IllegalArgumentException` messages are produced by `Gym.getMember(...)`, `Gym.addClass(...)`, and `Gym.enrolMemberInClass(...)` respectively. The messages name **what failed, why, and what would fix it** (the known-IDs hint in the first message is particularly useful in an interactive session). No stack traces appear anywhere; the demo cannot crash.

The single point of persistence in the system is `audit.log`. There is no database, no remote service, no scheduled task. That is intentional: the project's scope is the patterns, not the surrounding infrastructure.

---

## 9. Conclusions and Future Work

The project delivers a complete Gym Membership Management System in 16 Java files that demonstrates the Builder and Observer patterns in a setting where both are warranted, not contrived. The Builder constructs immutable `FitnessClass` instances from a fluent chain; the Observer fans every state change out to three independent reactions from a single publication site. The scripted demo runs in under a second, produces deterministic output, and exercises every code path including three guarded error cases.

Three lessons emerged from the work that outlast the assignment.

**Patterns earn their cost when the domain demands them.** A `FitnessClass` with only two attributes would not justify a Builder; a `Gym` with one hard-coded reaction would not justify an Observer. The patterns were chosen because the alternatives -- ten-argument constructor, direct method calls -- were demonstrably worse on this specific code, not because patterns are desirable in the abstract.

**Immutability buys correctness for free.** Once a `FitnessClass` leaves its Builder, it cannot change. The gym can share references freely, the in-memory journal can hold a `FitnessClass` reference for the rest of the program's life, and no defensive copy is needed anywhere. Removing the mutator surface removed an entire category of latent defect.

**Single points of publication are easier to audit.** Every event in the system flows through `Gym.publish(...)` -- one private method, three lines long. When something is wrong, there is one place to look. When something needs to change (for example, to add a per-event filter), one place to edit. That centralisation is the Observer pattern's structural gift.

The system is intentionally small and several extensions would be natural next steps.

1. **Persistence beyond `audit.log`.** A `Repository`-pattern abstraction over JSON or SQLite would let members and classes survive a JVM restart, without disturbing the engine. The repository would itself become a fourth observer (write-through) or a startup loader.
2. **Time-based events.** A scheduler that compares each class's `startTime` with the wall clock and publishes a `ClassStartingEvent` would extend the Observer fabric naturally. The scheduler is itself a thin observer of "tick" events.
3. **Capacity policy.** The `addMember` path on `FitnessClass` rejects enrolment when full. A waitlist policy -- queue the member, publish a `MemberPromotedFromWaitlistEvent` when someone drops -- would slot in cleanly because every step is already published.
4. **Configuration externalisation.** Class definitions could be loaded from a YAML or JSON file, with the Builder reading the file and producing immutable Products. The Builder pattern absorbs the new input source without changing its consumers.

None of these extensions would require modifying the Builder or the publication site. That is the architectural property the project was built to demonstrate.

---

## 10. References

1. Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley. (Original definitions of the Builder and Observer patterns used here.)
2. Bloch, J. (2018). *Effective Java*, 3rd ed. Addison-Wesley. (Item 2 on builders for classes with many parameters; Item 17 on minimising mutability; both directly informed `FitnessClass`.)
3. Martin, R. C. (2002). *Agile Software Development, Principles, Patterns, and Practices*. Prentice Hall. (Source for the SOLID acronym and the Open/Closed reasoning in Section 7.)
4. Oracle Corporation. (n.d.). *The Java Tutorials -- Collections, Time API, I/O*. https://docs.oracle.com/javase/tutorial/ (Reference for the standard-library features used: `java.util.*`, `java.time.*`, `java.io.*`.)
