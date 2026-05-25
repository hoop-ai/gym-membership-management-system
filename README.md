# Gym Membership Management System

> Demonstrating Builder + Observer in pure Java.

## What this is

A small university project for **SEN3006 — Software Architecture**. It
manages a tiny gym (members, fitness classes, enrolments) and uses that
domain to demonstrate two classic design patterns: **Builder** and
**Observer**. The code is pure Java with **zero external dependencies** —
if you have a JDK, you can run it.

## The patterns

**Builder** is for putting together objects that have a lot of optional
parts. Our `FitnessClass` has ten fields, and most of them are optional
(day of week, room, equipment, difficulty, and so on). Instead of a
ten-argument constructor, you set the fields by name and call `build()`.
The resulting object is immutable.

**Observer** lets several components react to the same event without
the source needing to know who they are. When the gym adds a member,
enrols someone in a class, or removes a class, it publishes one event —
and three observers react: one prints to the console, one writes to
`audit.log`, one stores it in memory for later. Adding a fourth would
not change the gym at all.

For full detail, see the
[project report](docs/report/report.md) and the
[slide deck](docs/presentation/PRESENTATION.pdf).

## How to get it

```bash
git clone https://github.com/hoop-ai/gym-membership-management-system.git
cd gym-membership-management-system
```

## How to run it

On Windows:

```bash
run.bat
```

On macOS or Linux:

```bash
./run.sh
```

If neither script works, you can run the bundled jar directly:

```bash
java -jar gym.jar
```

Java 8 or newer is required.

## What you'll see

The demo runs in under a second and prints seven labelled sections:

1. **Setting up the gym** — creates the gym and attaches three observers.
2. **Adding fitness classes** — three classes built with `FitnessClass.Builder`.
3. **Adding members** — three members added, each fires an event.
4. **Enrolling members in classes** — observers react to every enrolment.
5. **Drops and removals** — a member drops a class, then is removed.
6. **Error handling demo** — three intentional errors are caught and
   explained in plain English, no stack traces.
7. **Final journal dump** — the in-memory observer prints everything it captured.

The run also creates an `audit.log` file in the working directory — written
by the `AuditFileObserver` while the demo runs.

## Project structure

```
.
├── README.md
├── run.bat / run.sh                  <- one-command demo launchers
├── gym.jar                           <- prebuilt jar
├── src/main/java/                    <- 15 Java source files
│   ├── Member.java
│   ├── FitnessClass.java             (Builder)
│   ├── Difficulty.java
│   ├── GymEvent.java + 6 event classes
│   ├── GymEventObserver.java + 3 observer classes
│   ├── Gym.java                      (Subject)
│   └── Main.java                     (scripted demo)
└── docs/
    ├── report/report.md
    ├── presentation/PRESENTATION.pdf
    └── uml/
```

## Documentation

- Full report: [docs/report/report.md](docs/report/report.md)
- Slide deck: [docs/presentation/PRESENTATION.pdf](docs/presentation/PRESENTATION.pdf)
- Class diagram: [docs/uml/class/class-diagram.md](docs/uml/class/class-diagram.md)
- Sequence diagram: [docs/uml/sequence/sequence-diagram.md](docs/uml/sequence/sequence-diagram.md)

## Course info

- **Course:** SEN3006 — Software Architecture
- **Project:** Gym Membership Management System (Builder + Observer)
- **Deadline:** June 5, 2026
- **Author:** Elif

## License

Academic project produced for SEN3006. Not licensed for production use.
