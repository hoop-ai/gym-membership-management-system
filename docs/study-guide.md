# Study Guide — Day of the Presentation

A short, private cheat sheet for Elif. Read this the morning of the demo.
You do not need to memorize the code. You need to remember the demo, the
opening line, and the answers below.

---

## 1. The one command you run during the demo

```
run.bat
```

(On Mac or Linux: `./run.sh`.)

If `run.bat` doesn't work, type this instead:

```
java -jar gym.jar
```

That's it. The demo finishes in under a second.

---

## 2. Your opening line (memorize this)

> "We built a small gym management system in pure Java to demonstrate
> two design patterns: Builder, for constructing rich objects, and
> Observer, for notifying multiple parts of the system when something
> changes."

Say it slowly. Take a breath. Then run the demo.

---

## 3. The five moments to point at during the demo

These are the lines in `docs/sample-run.txt` you point at while talking.
The numbers are the literal line numbers in that file.

- **Lines 10–12 (Builder):**
  "These three fitness classes were each built using
  `FitnessClass.Builder`. Watch — every class has different fields, but
  we don't need a ten-argument constructor for each."

- **Lines 20–23 (Observer fan-out):**
  "Each enrolment fires one event. Notice: every line printed here was
  actually printed by *three* observers — console, audit file, in-memory
  journal. One publish call, three reactions. That's Observer."

- **Lines 29–32 (Error handling):**
  "Each error is caught and explained in plain English. No crashes,
  no stack traces. The gym validates inputs and throws clear messages."

- **Line 31 (Case-insensitivity):**
  "Notice — 'yoga flow' in lowercase still finds 'Yoga Flow'. That's
  intentional. Class-name lookups are case-insensitive."

- **Line 50 (Persistence):**
  "The `audit.log` file was written to disk during the run. That's our
  `AuditFileObserver` — it doesn't just print, it persists. You can open
  the file after the demo and see every event the gym ever published."

---

## 4. Five questions the professor might ask + verbatim answers

**Q: Why Builder and not Factory Method?**
A: "Factory Method produces variants of the same product. We have only
one kind of `FitnessClass` — we just need to configure its many optional
fields. Builder is the right fit for that. Factory Method would be the
answer if we had Yoga, Spin, and HIIT as separate subclasses with
different behavior."

**Q: Why an interface for the observer instead of an abstract class?**
A: "There's no shared state to inherit. Our three observers behave very
differently — one prints, one writes to a file, one stores in memory.
They only share the single `onEvent` method. An interface is the
smaller, lighter contract for that."

**Q: Why typed events instead of just passing strings?**
A: "Typed events let each observer format the message in its own voice.
The `Gym` never has to know how a message looks in the audit log versus
the console. That keeps presentation concerns out of the domain layer."

**Q: How would you add a new notification channel — say, email?**
A: "I'd write one new class that implements `GymEventObserver`, then
call `gym.addObserver(new EmailObserver())`. Zero edits to `Gym`. That's
the Open/Closed principle in action."

**Q: Why no GUI?**
A: "The course requirement was to demonstrate design patterns. A GUI
would add UI scaffolding that has nothing to do with Builder or
Observer — it would distract from the patterns. We kept the code
focused."

---

## 5. If something goes wrong during the demo

- **"My terminal froze."**
  Press Ctrl+C, then type `run.bat` again. It runs in under a second.

- **"Java isn't installed on this machine."**
  Use the backup screenshots — they're in `docs/sample-run.txt` and
  embedded in the slide deck. Walk through them as if they were live.

- **"I can't remember a slide."**
  Read the speaker notes verbatim. That's what they're for.

- **General:** "I forgot — it's not a big deal. The slides cover
  everything. Keep going."

---

## 6. The two patterns in one paragraph each (for memory)

**Builder.** Used to construct complex objects step by step. Lets you
set optional fields by name, validates the whole thing in one place,
and produces an immutable result. We used it for `FitnessClass` because
it has ten fields and most of them are optional — name and instructor
are required, the rest have sensible defaults.

**Observer.** Lets multiple components react to events without the
source knowing who's listening. We have three observers — one for the
console, one for an audit log file, one for in-memory storage. When
the `Gym` publishes an event, all three react. Adding a fourth doesn't
change the gym at all.

---

You've got this. Breathe. The slides are written, the demo runs in one
command, and every answer above is rehearsed in plain English. Walk in,
say the opening line, run `run.bat`, point at the five moments, take
questions.
