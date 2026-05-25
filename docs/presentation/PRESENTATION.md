---
marp: true
theme: default
paginate: true
size: 16:9
header: 'SEN3006 — Gym Membership Management System'
footer: 'Builder + Observer in Pure Java'
style: |
  section { font-size: 24px; }
  h1 { color: #0d7377; }
  h2 { color: #0d7377; }
  code { font-size: 0.85em; }
  pre { font-size: 0.7em; }
---

<!-- _class: lead -->

# Gym Membership Management System

### Two design patterns made visible in a working gym back-office

**Course:** SEN3006 — Software Architecture
**Presenter:** Elif
**Date:** June 2026

<!--
Good morning. My name is Elif and this is my project for SEN3006, Software Architecture. I built a small gym membership management system in pure Java, and the point of the project is to make two classic design patterns visible inside a real working program. Those patterns are Builder and Observer.

Everything you will see today is running code. I will walk you through the problem first, then the architecture, then the two patterns one at a time, and then I will run the program live so you can see the patterns producing real output. The whole demo takes less than a second to run. Let us start by looking at why a gym is a sensible place to study these patterns. That brings us to slide two, the problem domain.
-->

---

# The Problem Domain

A gym back-office has to keep track of **members**, **fitness classes**, and **who is enrolled in what**.

Two recurring engineering problems show up immediately:

- **Rich construction.** A fitness class has a name, an instructor, a day, a start time, a duration, a capacity, a room, a difficulty, an equipment list, a description. Ten fields. Most are optional. How do you construct that without a 10-argument constructor?
- **Change fan-out.** When a member enrols in a class, several listeners want to know — the console, an audit log, an in-memory journal. How does the gym tell them all without hard-coding each one?

The patterns we chose — **Builder** and **Observer** — were not picked off a list. The domain forced them.

<!--
A gym back-office sounds simple, but it has two engineering problems that come up again and again in real software. We picked this domain on purpose because both problems appear naturally without us having to force them.

The first problem is rich construction. A fitness class is not just a name. It has ten distinct fields, and most of them are optional. If we wrote a normal constructor for that, the call site would be unreadable. That is the kind of pain the Builder pattern is designed to remove.

The second problem is change fan-out. Whenever something happens in the gym — a member enrols, a class is added, someone is dropped — several different parts of the system want to know about it. We have a console printer, an audit log file, and an in-memory journal for the demo. We did not want the gym class to call all three of those directly. That kind of coupling breaks the moment we add a fourth listener. That is exactly the Observer pattern's job.

So the patterns came from the domain, not from a checklist. That brings us to what the system actually does, on slide three.
-->

---

# What the System Does

Five operations, exposed by the `Gym` class:

1. **Add member** — name and email; the gym assigns the ID.
2. **Remove member** — by ID; drops them from every class first.
3. **Add class** — built via `FitnessClass.Builder`; duplicate names rejected.
4. **Remove class** — by name, case-insensitive.
5. **Enrol** / **drop** — capacity-checked, duplicate-checked.

Three **observers** watch everything:

- `ConsoleObserver` — prints each event to the screen.
- `AuditFileObserver` — appends each event to `audit.log`.
- `InMemoryJournalObserver` — keeps a queryable list in RAM.

No GUI. No fake notifications. No fake users. **Just the patterns, working.**

<!--
The system is intentionally small. There are only five operations on the gym. We can add and remove members, add and remove classes, and enrol or drop a member from a class. That is the entire surface area. We kept it small because the point of the project is to demonstrate the patterns clearly, not to ship a gym product.

On the listener side, we have three observers attached. The Console observer prints every event to the terminal as it happens. The Audit File observer appends every event to a file called audit dot log, which means we get a persistent record on disk. And the In-Memory Journal observer keeps a list of every event in memory, which we will dump at the end of the demo to prove that all three observers saw exactly the same events.

There is no graphical interface, no fake notification system, no pretend users. Everything you see on the screen during the demo is the patterns doing their job. That brings us to the architecture diagram on slide four.
-->

---

# Architecture in One Diagram

The 16 source files fall into **three logical layers**:

**Domain layer**
`Member`  ·  `FitnessClass`  ·  `FitnessClass.Builder`  ·  `Difficulty`

**Pattern layer**
`GymEvent` *(abstract)* and 6 concrete events:
`MemberAddedEvent`, `MemberRemovedEvent`, `ClassAddedEvent`, `ClassRemovedEvent`, `MemberEnrolledInClassEvent`, `MemberDroppedFromClassEvent`
`GymEventObserver` *(interface)* and 3 implementations:
`ConsoleObserver`, `AuditFileObserver`, `InMemoryJournalObserver`

**Subject layer**
`Gym` — owns members, classes, observers; **the only class that publishes events.**

`Main.java` is the scripted demo. Zero external dependencies. Default Java package.

<!--
The whole project is sixteen Java files arranged in three logical layers. I am going to describe them with my voice so you can follow along even if the type on the slide is small.

The domain layer holds the things the gym is about. A Member is just an identity, a name, and an email. A Fitness Class is the immutable schedule object, and it has a nested Builder inside it which is the entire Builder pattern. The Difficulty enum is just three constants.

The pattern layer is where the Observer pattern lives. There is one abstract base class called Gym Event, and six concrete subclasses, one for each thing that can happen — member added, member removed, class added, class removed, member enrolled, member dropped. There is one observer interface called Gym Event Observer, and three concrete observers that implement it.

The subject layer is just one class, Gym. The Gym owns the members, the classes, and the list of observers, and it is the only class in the entire codebase that ever calls onEvent. That single point of publication is deliberate. It is the rule we will explain on the Observer slide. That brings us to Builder — slide five.
-->

---

# Why Builder?

**Builder** is a creational pattern that separates the *construction* of a complex object from its *representation*, so the same construction process can yield different results step by step.

**What it solves here.** `FitnessClass` has 10 fields. 8 are optional. The call site stays readable:

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

**What we rejected:**
- *10-argument constructor.* Unreadable at the call site — you cannot tell which `int` is capacity and which is duration.
- *Telescoping constructors.* Combinatorial explosion for every optional field.
- *Setter-based POJO.* Breaks immutability — once a class is on the schedule, its capacity should not silently change.

Code: **`src/main/java/FitnessClass.java`**

<!--
This is the most important pattern slide. Let me take it slowly.

The Builder pattern is a creational pattern. Its job is to separate the act of constructing a complex object from the object's final shape. The classic GoF definition says it lets us use the same construction process to produce different results. In practice it means we get a small, readable building block on the call site, and we get an immutable object out the other end.

In our project, the object that needs Builder is Fitness Class. A fitness class has ten fields. Two of them are required, the name and the instructor. The other eight — day of week, start time, duration, capacity, room, difficulty, equipment list, description — are all optional and they all have sensible defaults. The code snippet on the slide is exactly what we wrote in Main dot java. Notice we never had to write a ten-argument constructor.

We considered three alternatives and rejected all of them. A ten-argument constructor is unreadable: you cannot tell which integer is the capacity and which is the duration when you are reading the call site. Telescoping constructors — that is, writing one constructor per combination of optional fields — explodes combinatorially. And a plain setter-based POJO would mean that after a class is added to the schedule, anyone could silently mutate its capacity, which is exactly the kind of bug we want to make impossible.

The file to look at is FitnessClass dot java in source slash main slash java. That brings us to Observer — slide six.
-->

---

# Why Observer?

**Observer** is a behavioral pattern that defines a one-to-many dependency: when the subject changes, all registered observers are notified automatically.

**What it solves here.** Three log targets all want to know when a member enrols, a class is added, anything. Without Observer, `Gym` would call each one directly — adding a fourth means editing `Gym`, breaking the Open/Closed principle.

```java
// inside Gym — the ONLY place onEvent is called in the whole codebase
private void publish(GymEvent event) {
    for (GymEventObserver o : observers) {
        o.onEvent(event);
    }
}
```

**What we rejected:**
- *Direct method calls.* Tight coupling; cannot add a fourth observer without editing `Gym`.
- *Mediator pattern.* Centralises *control* — we want centralised *publication*. Different problem.
- *External pub/sub framework (Kafka, RxJava).* Extra dependency. Massive overkill for our scale.

Code: **`src/main/java/Gym.java`** (the `publish` method) and **`src/main/java/GymEventObserver.java`**

<!--
This is the second most important pattern slide.

The Observer pattern is a behavioral pattern. Its job is to set up a one-to-many relationship between one subject and any number of observers, so that whenever the subject's state changes, every registered observer is notified automatically without the subject having to know which observers exist.

In our project, the subject is the Gym class. The observers are the three log targets — Console, Audit File, and In-Memory Journal. Whenever a member enrols, the gym does not call those three observers by name. It calls one method called publish, and the publish method walks the list of registered observers and notifies each one. The code snippet on the slide is the whole publish method, and that is the only place in the entire codebase where onEvent is ever called.

We considered three alternatives. Direct method calls — gym calls console, gym calls audit, gym calls journal — would couple the gym to every observer individually, and we would have to edit the gym every time we wanted to add a fourth listener. That breaks the Open Closed principle on day one. The Mediator pattern is sometimes confused with Observer, but Mediator centralises control flow between many components. We do not want centralised control; we want centralised publication, which is exactly what Observer gives us. And an external pub-sub framework like Kafka or RxJava would solve our problem, but it would also add a heavy dependency for what is a tiny scale of events.

The files to look at are Gym dot java for the publish method, and Gym Event Observer dot java for the interface. That brings us to SOLID — slide seven.
-->

---

# SOLID — Quick Pass

- **S — Single Responsibility.** `Gym` coordinates. `FitnessClass.Builder` constructs. Each observer consumes. No class wears two hats.
- **O — Open/Closed.** Add a new event = one new class. Add a new observer = one new class. `Gym` is *not edited* either time.
- **L — Liskov Substitution.** Every `GymEvent` subclass is substitutable wherever `GymEvent` is expected. Every observer is substitutable wherever `GymEventObserver` is expected.
- **I — Interface Segregation.** `GymEventObserver` has one method, `onEvent(GymEvent)`. No observer is forced to implement anything it does not use.
- **D — Dependency Inversion.** `Gym` depends on the `GymEventObserver` *interface* and the abstract `GymEvent` — never on a concrete observer or concrete event subclass.

<!--
SOLID is a quick pass because we already covered most of it implicitly with the two patterns. Let me hit each letter briefly.

Single Responsibility means every class does one job. Gym coordinates. The Builder constructs. Each observer consumes. No class is wearing two hats.

Open Closed is the payoff of the Observer pattern. To add a new kind of event, we write one new class that extends Gym Event. To add a new observer, we write one new class that implements Gym Event Observer. In neither case do we edit Gym. That is open for extension, closed for modification.

Liskov Substitution: every event subclass can stand in for the abstract Gym Event, and every observer can stand in for the interface. Nothing breaks if you swap them.

Interface Segregation: our Gym Event Observer interface has exactly one method, onEvent. There is nothing for an observer to ignore or stub out. The interface is as small as possible.

Dependency Inversion: Gym never names a concrete observer class. It never names a concrete event class. It only knows the abstract Gym Event and the Gym Event Observer interface. The concrete observers are wired in from outside, in Main. That brings us to the live demo — slide eight.
-->

---

# Live Demo — copy these commands

```text
cd path\to\gym-management-system
run.bat
type audit.log
```

What to watch for as the output scrolls:

- Section 2 — three classes built via `FitnessClass.Builder`.
- Section 4 — each enrolment fires **one** event, three observers see it.
- Section 6 — three intentional errors, each caught with a plain-English message.
- Section 7 — the journal dumps every event captured in memory.
- The last line — `audit.log written with 12 lines.`

<!--
This is the demo slide. I am going to run the program live now. The commands on the slide are exactly what I will type, so if you want to follow along on your own machine, those three lines are everything you need.

I will run run dot bat first. Watch the output as it scrolls. In section two, you will see three classes being added — Yoga Flow, Spin Express, and HIIT 45. Each of those was built with FitnessClass dot Builder, and you can see the readable call site in the code. Notice we never wrote a ten-argument constructor.

In section four, each enrolment fires exactly one event. You will see it printed on the console, the audit dot log file captured it, and the In-Memory Journal recorded it. Three observers, one publish call. That is the Observer pattern doing its job.

In section six, I trigger three intentional errors — looking up a member ID that does not exist, adding a class with a duplicate name in lowercase, and double-enrolling someone. Each of those is caught with a clear plain-English message. Notice that the lowercase yoga flow still found the existing Yoga Flow class — that is the case-insensitive lookup.

In section seven, the In-Memory Journal dumps every event it captured. Twelve events. And the very last line tells us the audit dot log file on disk has twelve lines too. The two observers agree exactly. That brings us to the extension story on slide nine.
-->

---

# Extension Story

The Open/Closed payoff, measured in **lines of code**:

**Add a Slack notifier?**
- One new file: `SlackObserver.java` implementing `GymEventObserver`.
- One new line in `Main`: `gym.addObserver(new SlackObserver());`
- Edits to `Gym.java`: **zero.**

**Add a "class rescheduled" event?**
- One new file: `ClassRescheduledEvent.java` extending `GymEvent`.
- One new method on `Gym` that calls `publish(new ClassRescheduledEvent(...))`.
- Edits to existing observers: **zero** — they handle any `GymEvent` by contract.

That is what Open/Closed buys us. *New behavior is additive, not invasive.*

<!--
This slide is about what the patterns actually buy us in practice. I expressed it in terms of lines of code, because that is the cleanest way to show the Open Closed principle paying off.

Suppose the gym says, we now want to send a Slack message every time a member enrols. With the Observer pattern in place, the work is two things. We write one new file called Slack Observer that implements the Gym Event Observer interface, and we add one line in Main to register it. We make zero edits to the Gym class. Zero. The Gym does not know Slack exists, and it does not need to.

Suppose instead the gym says, we want to model when a class is rescheduled. Again the work is small. We write one new file called Class Rescheduled Event that extends Gym Event, and we add one new method on Gym that creates and publishes that event. We make zero edits to any existing observer, because every observer is contractually bound to handle any Gym Event subclass.

That is what Open Closed buys us in practice. New behavior is additive, not invasive. That brings us to the recap on slide ten.
-->

---

# Recap

- We modelled a small, honest gym domain — members, classes, enrolments.
- **Builder** handled the rich-construction problem — `FitnessClass` has 10 fields, 8 optional.
- **Observer** handled the fan-out problem — three observers learn about every event through one `publish` call.
- SOLID held: *adding a new event or a new observer never touches `Gym`.*
- Both patterns were chosen because the domain forced them — **not because the rubric required them.**

<!--
Quick recap. We modelled a small gym domain with members, classes, and enrolments. We picked two patterns to demonstrate. Builder solved the rich-construction problem for Fitness Class — ten fields, eight optional, a single readable call site, an immutable result. Observer solved the fan-out problem for change notifications — three observers all learn about every gym event through one publish call.

SOLID held throughout. The most visible payoff is Open Closed: adding a new event subclass or a new observer subclass never requires editing Gym. The Gym is closed for modification but open for extension.

And both patterns were chosen because the domain forced them, not because the assignment required them. That is the point of the project. The patterns were the right answer to the problems the domain raised, and we built the smallest amount of code that could demonstrate them honestly.

That brings us to questions and answers — slide eleven.
-->

---

# Q&A — Likely Questions

**Q: Why not Lombok's `@Builder` annotation?**
Zero external dependencies. The brief asks for hand-written Java patterns.

**Q: Why is `FitnessClass` immutable?**
A class on the schedule should not silently mutate after publication. Capacity, room, and time are facts, not variables.

**Q: Why an *interface* for `GymEventObserver`, not an abstract class?**
There is no shared state between observers — only shared behavior. The interface communicates that.

**Q: Why typed event classes instead of `notify(String message)`?**
Keeps presentation out of the domain. Each observer formats events in its own voice — the console with brackets, the audit file with newlines, the journal with no formatting at all.

**Thank you.**

<!--
Before we go to open questions, let me cover the four I expect to hear, because the answers are short.

Why not use Lombok at Builder annotation? Because the assignment is about us writing the pattern by hand. Lombok generates the Builder for us with one annotation, but then there is nothing for me to show or defend. Zero external dependencies was a constraint from the start, and it forced us to write the Builder ourselves.

Why is FitnessClass immutable? Because a class that is already on the schedule is a fact, not a variable. If a member has enrolled in Yoga Flow at six thirty on a Monday, we should not be able to mutate that to a different time silently. Mutation belongs to the enrolment list, which the Gym controls, not to the schedule itself.

Why an interface for the observer instead of an abstract class? Because there is no shared state between our three observers — they only share behavior, the onEvent method. When you have no shared state, an interface communicates that more honestly than an abstract base class.

Why typed event classes instead of a single notify-string method? Because each observer wants to format the event in its own voice. The console adds brackets and timestamps. The audit log adds newlines. The journal stores the event object raw. If we passed a pre-formatted string, all three observers would be locked into the same format. Typed events keep presentation out of the domain.

Thank you. I am happy to take questions.
-->
