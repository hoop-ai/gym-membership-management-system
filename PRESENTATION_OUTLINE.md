# Presentation Outline — Gym Membership Management System

**Course:** SEN3006 Software Architecture
**Project:** Gym Membership Management System (Elif)
**Patterns:** Builder (Creational) + Observer (Behavioral)
**Bonus pattern:** Lifecycle state machine via enum

Target: ~10 min talk + 3 min live demo + Q&A.
The professor already knows what each pattern *is* — we focus on **why we chose it here** and **what it bought us**.

---

## Table of contents

1. Title + one-line pitch
2. The domain — what problem are we solving
3. What the system does (feature summary)
4. Architecture in one diagram
5. **Why Builder?** (Creational rationale)
6. **Why Observer?** (Behavioral rationale)
7. Bonus: Lifecycle state machine
8. SOLID quick pass
9. **Live demo** (the centerpiece)
10. Extension story — adding a new notifier / new plan field
11. What went well / what we'd do next
12. Q&A

---

## Slide-by-slide talking points

### 1. Title
- "Gym Membership Management System — Builder + Observer in pure Java."
- Course, name, deadline. One sentence: "Two design patterns made visible in a working app."

### 2. The domain — why a gym
- A gym membership has two recurring problems:
  - Plans have **many optional configuration knobs** (duration, tier, freeze allowance, auto-renew, discount, trial days, group/PT credits). Constructors blow up. → Creational problem.
  - Members need to be **notified through different channels** (email/SMS/push) about different events (payment due, renewal reminder, class cancelled, promotions), and the notification list changes per member. → Behavioral problem.
- We chose the domain *because the patterns map naturally to it*, not the other way around.

### 3. What the system does
- Members, MembershipPlans, Gym (the aggregator/subject).
- Four event types: **PaymentDue**, **RenewalReminder**, **ClassCancelled**, **Promotion**.
- Three notification channels: **Email**, **SMS**, **Push** — each a separate Observer.
- Lifecycle states: `PENDING → ACTIVE → EXPIRING → EXPIRED` with `FROZEN ↔ ACTIVE` and `CANCELLED` branches.
- Three entry points share the same engine:
  - `Main` — scripted demo of all six sections.
  - `GymManagementApp` — interactive console menu.
  - `GymManagerGUI` (Swing) — graphical demo.

### 4. Architecture in one picture
- Show class diagram (Mermaid render from `docs/uml/`).
- Three layers:
  - **Domain layer** — `Member`, `MembershipPlan`, `MembershipPlan.Builder`, `Gym`, enums.
  - **Pattern layer** — `MemberNotifier` interface + 3 concrete; `GymEvent` abstract + 4 concrete.
  - **Entry-point layer** — `Main`, `GymManagementApp`, `GymManagerGUI`.

### 5. Why Builder? (the rationale slide — most important)
- **The pain Builder solves here:** `MembershipPlan` has 9 fields, 4 of them optional. A 9-arg constructor is unreadable (`new MembershipPlan(12, GOLD, true, 30, 0.10, false, 0, 2, true)` — what does any of that mean at the call site?). Telescoping constructors multiply combinatorially.
- **Why not a setter-based POJO?** Plans need to be **immutable** once issued — a member's plan price/duration shouldn't drift after signup. Setters break that. Builder enforces "build once, never mutate."
- **Why required validation in `build()`?** Some combinations are invalid (e.g., trial days > duration; freeze allowance on a basic tier). Centralising that check in `build()` means no caller can sneak around it.
- **Concrete payoff:** call-site reads like English —
  `new MembershipPlan.Builder("Gold Annual").durationMonths(12).tier(GOLD).autoRenew(true).freezeDays(30).build()`.
- **Speaker note:** Point at one call site in `Main.java` so the audience *sees* the readability win.

### 6. Why Observer? (the rationale slide — most important)
- **The pain Observer solves here:** A gym sends notifications, but *which channel* differs per member (email-only vs. SMS+push), and *which events fire* differs by member state (an expiring member gets a renewal reminder; an active member doesn't). Without Observer you end up with `if (member.hasEmail) ... if (member.hasSms) ...` branches scattered across every event-publishing method. That's the textbook tight-coupling smell.
- **Why a true Subject/Observer split and not a callback list?** We need observers to **subscribe and unsubscribe at runtime** as members change their preferences. We also want adding a new channel (push, in our case) to be **zero-edit** to `Gym`. Observer gives both for free.
- **Why an abstract `GymEvent` with concrete subclasses (`PaymentDueEvent` etc.)?** Each event carries different payload (PaymentDue has amount + due date; ClassCancelled has class name + slot). A flat `notify(String message)` would force string-formatting in `Gym` itself — leaking presentation into the domain. Typed events let each Observer decide how to render the message.
- **Concrete payoff:** `Gym.fireEvent(new RenewalReminderEvent(member))` reaches every subscribed notifier, each rendering the message in its own voice (Email gets HTML-ish, SMS gets terse, Push gets short).
- **Speaker note:** This is the slide to dwell on. Decision was *not* "use Observer because it's behavioral." It was "we have a 1-to-many fan-out across heterogeneous channels — Observer is the textbook fit."

### 7. Bonus — Lifecycle state machine
- `MembershipStatus` enum encodes the allowed transitions on the enum constants themselves.
- `Member.setStatus(...)` rejects illegal transitions with `IllegalArgumentException`.
- Why mention it: it's a third pattern (State) for free, costs no extra files, gives compile-time guarantees you cannot reach `EXPIRING` from `CANCELLED`.

### 8. SOLID quick pass (10 sec each)
- **S**RP — one job per class: `Gym` coordinates, notifiers render, events carry data.
- **O**CP — new notifier = new file, no edits to `Gym`. Demonstrated in slide 10.
- **L**SP — every notifier works through the `MemberNotifier` interface.
- **I**SP — `MemberNotifier` has one method; nothing fat.
- **D**IP — `Gym` holds `List<MemberNotifier>`, never a concrete class.

### 9. Live demo (≈3 min)
1. **Run scripted demo.** `java -cp bin Main`. Scroll past sections; pause on the Observer fan-out output (one event, three channels print).
2. **Open the GUI.** `java -jar GymManagerGUI.jar`. Load *Observer demo* scenario.
3. **Add/remove a notifier at runtime.** Untick "SMS" — fire another event — notice SMS stops printing. (This is the OCP/Observer payoff made tangible.)
4. **Trigger Builder validation.** Try to add a plan with `durationMonths(0)`; engine throws, dialog explains why.
5. **Trigger an invalid transition.** Try moving a `CANCELLED` member back to `ACTIVE`. Engine refuses.
- **Speaker note:** Validation lives in the engine, not the GUI. The GUI just surfaces the message.

### 10. Extension story — zero-edit demonstration
- Adding a `WhatsAppMemberNotifier`:
  - `WhatsAppMemberNotifier.java` (implements `MemberNotifier`).
  - One `gym.subscribe(new WhatsAppMemberNotifier())` call.
- Adding a new event type (e.g., `MembershipUpgradeEvent`):
  - One class extending `GymEvent`, one `fire` call.
- **Zero edits** to existing classes. That is the payoff.

### 11. What went well / what we'd do next
- Worked well:
  - The two patterns map cleanly to the domain (no contrived plumbing).
  - Builder validation centralises rules we'd otherwise scatter.
  - Observer + typed events keep `Gym` ignorant of channels.
- What's next:
  - Persistence (Repository pattern over JDBC).
  - Async notifier dispatch (current implementation is synchronous).
  - Bulk-event batching for the Promotion case (currently one fan-out per member).

### 12. Q&A
- Likely questions + where the answers live:
  - "Why not Lombok's @Builder?" — keep zero-dependency.
  - "Why an interface for Observer instead of abstract class?" — no shared state needed.
  - "Why immutable plans?" — see slide 5.
  - "Why typed events instead of `notify(String)`?" — see slide 6.
- Backup slide: `docs/design/study-guide.md`.

---

## Timing budget

| Slide | Time |
|---|---|
| 1 — Title | 0:30 |
| 2 — Domain | 1:00 |
| 3 — What it does | 1:00 |
| 4 — Architecture | 0:45 |
| 5 — Why Builder | 1:30 |
| 6 — Why Observer | 1:30 |
| 7 — Lifecycle | 0:45 |
| 8 — SOLID | 0:50 |
| 9 — Live demo | 3:00 |
| 10 — Extension | 0:45 |
| 11 — Recap | 0:30 |
| 12 — Q&A | 2–5:00 |
| **Total** | **~13 min + Q&A** |

If short on time: cut slide 8 (SOLID) and shorten slide 11.

---

## Pre-demo checklist

- [ ] `javac -d bin src/main/java/*.java src/main/java/gui/*.java` runs clean.
- [ ] `java -cp bin Main` shows all six sections finishing.
- [ ] `java -jar GymManagerGUI.jar` opens, header bar is teal, table renders.
- [ ] Have a known-bad input ready (e.g., duration 0) to trigger validation live.
- [ ] Backup screenshots in case the projector dies.
