# Gym Membership Management System

A small Java application that demonstrates two classic object-oriented design
patterns — **Builder** and **Observer** — by managing gym memberships and
the notifications that go out to members. Built for the
**SEN3006 — Software Architecture** course.

The whole project is **pure Java with zero external libraries**. With a JDK
installed, you can compile and run everything from a terminal in under a
minute.

---

## Contents

- [What you need on your computer](#what-you-need-on-your-computer)
- [Quick start (easiest way to see the project)](#quick-start-easiest-way-to-see-the-project)
- [The three ways to run the project](#the-three-ways-to-run-the-project)
- [Project structure](#project-structure)
- [The two design patterns](#the-two-design-patterns)
- [Membership lifecycle (state machine)](#membership-lifecycle-state-machine)
- [Documentation index](#documentation-index)
- [Build everything from source](#build-everything-from-source)
- [Troubleshooting](#troubleshooting)

---

## What you need on your computer

| Requirement | Minimum version | How to check |
|---|---|---|
| **Java JDK** | 8 (Java 1.8) or newer | Open a terminal and run `java -version` |
| **Java compiler** | `javac` from the same JDK | Run `javac -version` |
| Operating system | Windows, macOS, or Linux | Any of the three works |
| Disk space | About 2 MB | The whole repository is small |

That's it. No build tools (Maven, Gradle), no package managers, no
internet connection, no accounts.

### Don't have a JDK yet?

Pick one of these free distributions, install it, and reopen your terminal:

- [Eclipse Temurin (recommended)](https://adoptium.net/temurin/releases/)
- [Microsoft Build of OpenJDK](https://learn.microsoft.com/en-us/java/openjdk/download)
- Whatever your operating-system package manager ships (e.g. `apt install
  default-jdk` on Ubuntu, `brew install openjdk` on macOS).

After installation, `java -version` should print something like
`openjdk version "21.0.x"`.

---

## Quick start (easiest way to see the project)

The repository ships a ready-to-run GUI as a `.jar` file:

```sh
java -jar GymManagerGUI.jar
```

On Windows you can also **double-click `run-gui.bat`** (macOS / Linux: run
`./run-gui.sh`). A window titled *Gym Membership Manager* opens.

Use the menu **Demos -> Load Observer demo** to populate the gym with three
members across three membership tiers, each pre-wired with a different mix
of notification channels (email, SMS, push). The dark log strip along the
bottom will fill with the events the `Gym` (Subject) publishes to the
attached notifiers (Observers).

---

## The three ways to run the project

| # | Mode | Command | What it shows |
|---|---|---|---|
| 1 | **Graphical UI** | `java -jar GymManagerGUI.jar` | Member table, plan catalogue, lifecycle buttons, live notification log |
| 2 | **Interactive console** | `java -cp bin GymManagementApp` | Menu-driven CLI: build plans, enrol members, attach notifiers, publish events |
| 3 | **Automated tests** | `java -cp bin Main` | Six-section scripted demo with `[PASS]` markers |

Mode 1 needs only the JDK and the bundled JAR. Modes 2 and 3 also need a
compiled `bin/` directory — see [Build everything from source](#build-everything-from-source).

---

## Project structure

```
.
├── README.md                       <- you are here
├── SUBMISSION_README.md            <- how to package the deliverable
├── GymManagerGUI.jar               <- runnable Swing GUI (Quick start)
├── run-gui.bat / run-gui.sh        <- double-click launchers for the JAR
├── build-jar.bat / build-jar.sh    <- rebuild the JAR from source
├── guide.md                        <- assignment description from the professor
├── src/
│   └── main/
│       └── java/                   <- 19 Java source files
│           ├── MembershipPlan.java         (Builder Product, with nested Builder)
│           ├── Member.java                 (Observer host)
│           ├── MembershipStatus.java       (Lifecycle state machine)
│           ├── AccessTier.java             (BASIC / STANDARD / PREMIUM enum)
│           ├── GymEvent.java               (Observer event base class)
│           ├── PaymentDueEvent.java
│           ├── RenewalReminderEvent.java
│           ├── ClassCancelledEvent.java
│           ├── PromotionEvent.java
│           ├── MemberNotifier.java         (Observer interface)
│           ├── EmailMemberNotifier.java
│           ├── SmsMemberNotifier.java
│           ├── PushMemberNotifier.java
│           ├── Gym.java                    (Subject; coordinator)
│           ├── Main.java                   (Automated test demo)
│           ├── GymManagementApp.java       (Interactive console app)
│           └── gui/                        (Swing GUI, 5 files)
│               ├── GymManagerGUI.java
│               ├── MemberFormPanel.java
│               ├── MemberTablePanel.java
│               ├── MemberTableModel.java
│               └── DemoScenarios.java
└── docs/
    ├── report/report.md            <- full 9-section project report
    ├── design/                     <- design spec, study guide, test docs,
    │                                  presentation outline
    └── uml/                        <- class, sequence, state, activity,
                                       component, deployment, use-case diagrams
```

There are no hidden helpers, no generated code, no transitive dependencies.

---

## The two design patterns

### 1. Builder (Creational)

**Problem.** A `MembershipPlan` has many configurable attributes: duration,
monthly fee, access tier, list of included classes, guest passes per month,
freeze-days allowance, whether personal training is included, etc. A
constructor with eight positional parameters is unreadable and impossible
to evolve — adding a ninth attribute breaks every caller.

**Solution.** `MembershipPlan.Builder` is a fluent inner class. Each
attribute has its own named setter; required validation runs centrally
inside `build()`; the resulting `MembershipPlan` is immutable.

```java
MembershipPlan premium = new MembershipPlan.Builder("Premium Annual")
        .durationMonths(12)
        .monthlyFee(89.99)
        .accessTier(AccessTier.PREMIUM)
        .includesClass("Yoga")
        .includesClass("Spinning")
        .includesClass("HIIT")
        .guestPassesPerMonth(4)
        .freezeDaysPerYear(60)
        .personalTrainerIncluded(true)
        .build();
```

### 2. Observer (Behavioral)

**Problem.** Members need to receive notifications through whatever channel
they have signed up for — email, SMS, push, or any future channel. The gym
should not have to know which member uses which channel, and adding a new
channel should not require editing any existing class.

**Solution.** The `Gym` is the **Subject** and publishes `GymEvent`
instances via a single `publishEvent(...)` method. `MemberNotifier` is the
**Observer** interface; concrete implementations (`EmailMemberNotifier`,
`SmsMemberNotifier`, `PushMemberNotifier`) attach to a `Member` and format
events for their channel.

```java
Member alice = gym.enrolMember("Alice", "alice@example.com", "", "Premium Annual");
alice.attachNotifier(new EmailMemberNotifier(alice));
alice.attachNotifier(new PushMemberNotifier(alice));
gym.publishPaymentDue(alice.getId(), LocalDate.now().plusDays(7), 89.99);
// -> both Alice's email and push notifiers fire
```

Adding a brand-new channel — Slack, Discord, in-app — is a single new
class implementing `MemberNotifier`. The `Gym`, the existing notifiers,
and every event class stay untouched.

---

## Membership lifecycle (state machine)

Each member moves through a small workflow with clearly-defined
transitions:

```
PENDING ----> ACTIVE ----> EXPIRING ----> EXPIRED   (terminal)
                |              |
                v              v
              FROZEN -----> ACTIVE
                |
                v
            CANCELLED      (terminal)
```

Invalid transitions (for example `PENDING -> EXPIRED`) throw an
`IllegalArgumentException`, so the data can never end up in an impossible
state.

---

## Documentation index

| Document | What's inside |
|---|---|
| [docs/report/report.md](docs/report/report.md) | Full 9-section project report |
| [docs/design/design-spec.md](docs/design/design-spec.md) | Class signatures, design rationale, SOLID mapping |
| [docs/design/test-documentation.md](docs/design/test-documentation.md) | What each test in `Main.java` proves and how to run it |
| [docs/design/study-guide.md](docs/design/study-guide.md) | Every class explained + Q&A cheat-sheet for the presentation |
| [docs/design/presentation-outline.md](docs/design/presentation-outline.md) | Slide-by-slide plan with speaker notes |
| [docs/uml/](docs/uml/) | Class, sequence, state, activity, component, deployment, use-case diagrams (PlantUML + Mermaid renders) |
| [guide.md](guide.md) | The original assignment description |

---

## Build everything from source

If you only have the source tree (no compiled `bin/` and no JAR), this is
the full rebuild:

### Step 1 -- Compile

From the project root:

```sh
javac -d bin src/main/java/*.java src/main/java/gui/*.java
```

Success looks like silence — no output, and a `bin/` directory appears
with `.class` files inside.

### Step 2 -- Run the automated tests

```sh
java -cp bin Main
```

You should see six test sections, each ending with `[PASS]`, and a final
`ALL TESTS PASSED` banner.

### Step 3 -- Run the interactive console app

```sh
java -cp bin GymManagementApp
```

Pick options from the menu to build plans (a step-by-step Builder demo),
enrol members, attach notifiers, change status, and publish events.

### Step 4 -- Rebuild the GUI JAR (optional)

- **Windows:** double-click `build-jar.bat` (or run it from a terminal).
- **macOS / Linux:** run `./build-jar.sh`.

Then launch with:

```sh
java -jar GymManagerGUI.jar
```

---

## Troubleshooting

**`java: command not found` / `javac: command not found`** — a JDK is not
installed, or it is installed but the executables are not on your `PATH`.
Reinstall using one of the links in
[What you need on your computer](#what-you-need-on-your-computer) and
reopen your terminal.

**`Error: Could not find or load main class Main`** — you ran the command
from the wrong directory, or you have not compiled yet. Make sure you are
in the project root (the folder containing `README.md`) and that a `bin/`
directory exists with `Main.class` inside.

**`Unsupported class file major version`** — the JAR was compiled with a
newer JDK than the one you are running. Either install a newer JDK or
recompile from source against your installed JDK using
`javac -d bin src/main/java/*.java src/main/java/gui/*.java`.

**The GUI window opens but looks blank** — drag a corner to resize the
window. Some window managers initialise the layout slightly differently on
the first paint.

**`error: unmappable character (0x...) for encoding ...` during compile** —
add `-encoding UTF-8` to the `javac` command:

```sh
javac -encoding UTF-8 -d bin src/main/java/*.java src/main/java/gui/*.java
```

---

## License and authorship

This is a student project submitted for SEN3006. All source code and
documentation in this repository are original work produced for that
course.
