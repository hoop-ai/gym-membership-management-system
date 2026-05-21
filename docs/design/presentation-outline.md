# Presentation Outline — Gym Membership Management System

**Course:** SEN3006 Software Architecture
**Project:** Gym Membership Management System (Elif)
**Patterns:** Builder (Creational) + Observer (Behavioral)
**Bonus pattern:** Lifecycle state machine encoded in `MembershipStatus`
**Presenters:** Elif + Teammate 2 (co-presented) — speaker tags `[E]` / `[T2]` per slide; rename `Teammate 2` once the partner is confirmed.

Aligned to `docs/design/design-spec.md` and `docs/report/report.md`.
Target: ~10 min talk + 3 min live demo + 2–5 min Q&A.

The professor already knows what each pattern *is*. The presentation
spends its time on **why we chose it here** and **what it bought us in
this codebase**.

---

## Suggested speaker split (negotiate with Teammate 2)

| Slide | Title | Suggested speaker | Rationale |
|---|---|---|---|
| 1 | Title | **Either** (open together) | Set the tone, hand off |
| 2 | Domain | **E** | Pitch — sets the "why-this-project" framing |
| 3 | What it does | **E** | Feature tour — natural follow-up from domain |
| 4 | Architecture | **T2** | First technical slide — T2 anchors the code side |
| 5 | Why Builder | **T2** | Whoever wrote the Builder code should defend it |
| 6 | Why Observer | **E** | Balances airtime; Observer rationale is conceptual + visual |
| 7 | Lifecycle | **T2** | Continues the code-walk arc from slide 5 |
| 8 | SOLID | **E** | 10 seconds per principle — fast, conversational |
| 9 | Live demo | **T2 drives · E narrates** | One hand on keyboard, one voice explaining |
| 10 | Extension story | **E** | "Imagine adding X" — pitch-style, E lands the OCP payoff |
| 11 | Recap | **Either** (E recommended) | Whoever opened should close |
| 12 | Q&A | **Both** | Field questions on the part each presented |

Adjust based on who feels stronger on which topic. The demo (slide 9)
is the highest-stakes — pair-program it so the keyboard-holder isn't
also doing the talking.

Rough airtime target: **E ≈ 5:30** · **T2 ≈ 5:15** · shared **3:30**.

---

## Table of contents

1. Title + one-line pitch
2. The domain — what problem are we solving
3. What the system does (feature summary)
4. Architecture in one diagram
5. **Why Builder?** (creational rationale)
6. **Why Observer?** (behavioral rationale)
7. Bonus — lifecycle state machine
8. SOLID quick pass
9. **Live demo** (the centerpiece)
10. Extension story — adding a notifier / event / plan field
11. What went well / what's next
12. Q&A

---

## Slide-by-slide talking points

### 1. Title  [Either]
- "Gym Membership Management System — Builder + Observer in pure Java."
- Course code, team names, deadline. Single-sentence pitch:
  > "Two design patterns made visible inside a working gym back-office."

### 2. The domain — why a gym  [E]
- A gym back-office has two recurring problems:
  - **Plans have many optional fields** — name, duration, monthly fee,
    access tier, included classes, guest passes, freeze days, personal
    trainer flag. Most plans only configure 3–4 of them. → Creational.
  - **Notifications fan out across channels and target subsets** —
    payment-due is per-member; renewal-reminders are per-member;
    class-cancelled is broadcastable; promotion is always broadcast.
    Each member subscribes to a different mix of email / SMS / push.
    → Behavioral.
- The patterns were chosen *because the domain forced them*, not the
  other way around.

### 3. What the system does  [E]
- Engine = a single `Gym` aggregator.
- 8-field plans built via `MembershipPlan.Builder`.
- 4 event types: `PaymentDueEvent`, `RenewalReminderEvent`,
  `ClassCancelledEvent`, `PromotionEvent`.
- 3 notifier channels: `EmailMemberNotifier`, `SmsMemberNotifier`,
  `PushMemberNotifier`. Notifiers attach **to a member**, not to the
  Gym — so each member carries their own subscription list.
- 6-state lifecycle: `PENDING → ACTIVE → EXPIRING → EXPIRED`, with
  `FROZEN ↔ ACTIVE` and `CANCELLED` branches (both `EXPIRED` and
  `CANCELLED` are terminal).
- Three entry points share the same engine:
  - `Main` — 6 scripted self-checking test sections.
  - `GymManagementApp` — interactive console menu.
  - `gui.GymManagerGUI` — Swing window with live notification log.

### 4. Architecture in one picture  [T2]
- Class diagram (Mermaid render from `docs/uml/`).
- Three layers:
  - **Domain** — `Member`, `MembershipPlan`/`Builder`, `Gym`, enums
    (`MembershipStatus`, `AccessTier`).
  - **Pattern** — `MemberNotifier` interface + 3 concrete observers;
    `GymEvent` abstract + 4 concrete events.
  - **Entry-points** — `Main`, `GymManagementApp`, `GymManagerGUI`.
- Arrows only ever point *toward* abstractions — DIP made visible.

### 5. Why Builder? (rationale — most important slide)  [T2]
- **The pain it solves here:** `MembershipPlan` has 8 fields and only
  `name` is truly mandatory. A flat constructor would be either
  `new MembershipPlan(name, 12, 49.99, GOLD, Set.of("yoga","spin"), 2, 30, true)`
  (8 anonymous positional args — impossible to read at the call site)
  or a telescoping ladder of constructors that explodes combinatorially.
- **Why not a setter-based POJO?** Plans must be **immutable** once
  registered with the gym — pricing and tier cannot drift after a
  member signs up. Setters break that. Builder gives a "build once,
  never mutate" contract: the class is `public final` with no setters.
- **Why centralise validation in `build()`?** Several rules are
  *combinations* of fields (duration >= 1; monthly fee >= 0; tier
  non-null; guest passes / freeze days >= 0). Putting them all in
  `build()` means no caller can sneak past them by hand-rolling a
  partial object.
- **The payoff — call site reads like English:**
  ```java
  new MembershipPlan.Builder("Gold Annual")
      .durationMonths(12)
      .monthlyFee(49.99)
      .accessTier(AccessTier.PREMIUM)
      .includesClass("yoga").includesClass("spin")
      .freezeDaysPerYear(30)
      .personalTrainerIncluded(true)
      .build();
  ```
- **Speaker note:** Open `Main.java` Test 1 on screen — show that
  every plan in the test suite is built in exactly this shape.

### 6. Why Observer? (rationale — most important slide)  [E]
- **The pain it solves here:** the gym needs to notify members about
  things, but *which channel* differs per member and *which event
  fires* differs by member state. Without Observer the publishing
  code ends up sprinkled with
  `if (member.email != null) emailSvc.send(...); if (...) smsSvc.send(...);`
  in every event-producing method. That is the textbook tight-coupling
  smell.
- **Why notifiers attach to the member, not the Gym?** Subscriptions
  belong to the person being notified — a member who only wants SMS
  shouldn't pollute another member's channel list. `Gym.publishEvent`
  walks the target member's notifiers for targeted events, and walks
  every member's notifiers for broadcasts.
- **Why typed `GymEvent` subclasses, not `notify(String)`?** Each
  event carries different payload (`PaymentDueEvent` has amount + due
  date; `ClassCancelledEvent` has class name + date and is
  broadcastable; `PromotionEvent` has discount percent and is *always*
  broadcast). A string-based API would force `Gym` to render messages
  itself — leaking presentation into the domain. Typed events let
  each notifier format the message in its own voice (Email gets a long
  body, SMS gets a one-liner, Push gets a 40-char headline).
- **The payoff — one line of publication, three rendered channels:**
  ```java
  gym.publishPaymentDue(memberId, dueDate, 49.99);
  // Email + SMS + Push notifiers for that member each render their own message.
  ```
- **Speaker note:** This is the slide to dwell on. The choice was
  *not* "use Observer because behavioral was required." It was
  "we have 1-to-many fan-out across heterogeneous channels with
  runtime subscription changes — Observer is the canonical fit."

### 7. Bonus — lifecycle as a state machine  [T2]
- `MembershipStatus` enum carries its own allowed-transition table on
  each constant.
- `Member.setStatus(...)` calls `canTransitionTo(...)` and throws
  `IllegalArgumentException("Cannot transition from X to Y")` on a
  bad move.
- It's a third pattern (State) for zero extra files — a single enum.
- Compile-time guarantee: cannot reach `EXPIRING` from `CANCELLED`.
- Diagram on screen showing the six states and their nine valid edges.

### 8. SOLID quick pass (10 seconds each)  [E]
- **S**RP — `Gym` coordinates only; the Builder never publishes events; notifiers never mutate the journal.
- **O**CP — new plan attribute = field + setter on the Builder. New event = new `GymEvent` subclass. New channel = new `MemberNotifier`. Test 5 in `Main.java` proves it by installing an anonymous notifier **at runtime**.
- **L**SP — every notifier and event subclass is fully substitutable.
- **I**SP — `MemberNotifier` exposes three minimal methods, all used.
- **D**IP — `Gym` references only abstractions; no `new EmailMemberNotifier(...)` lives inside `Gym`.

### 9. Live demo (≈3 min)  [T2 drives · E narrates]
1. **Run scripted demo.** `java -cp bin Main`. Scroll past sections 1–4 quickly. Pause on **Section 6 (Observer fan-out)** — one publish call, three printed messages (Email/SMS/Push), all formatted differently.
2. **Open the GUI.** `java -jar GymManagerGUI.jar`. Show the four-region layout (banner, table, form, action strip + log).
3. **Trigger Builder validation live.** Try to enrol a member into a plan you never built, or create a plan with `monthlyFee(-1)`. Engine throws; dialog explains why.
4. **Trigger a notification.** Click *Publish payment due* on a known member — watch the dark notification log fill with one entry per attached notifier.
5. **Trigger an illegal transition.** Pick a `CANCELLED` row, click *Set ACTIVE*. Engine refuses with the exact "Cannot transition from CANCELLED to ACTIVE" message.
- **Speaker note:** Validation lives in the engine, not the GUI. The GUI just surfaces the message.

### 10. Extension story — zero-edit demonstration  [E]
- Adding a new channel `DiscordMemberNotifier`:
  - One file implementing `MemberNotifier`.
  - One `member.attachNotifier(new DiscordMemberNotifier(member, ...))` call.
  - **Zero edits** to `Gym`, `GymEvent`, or any existing notifier.
- Adding a new event `MemberBirthdayEvent`:
  - One class extending `GymEvent`.
  - Optionally one new convenience publisher on `Gym`.
  - **Zero edits** to any existing event or notifier.
- Adding a plan attribute `lockerIncluded`:
  - One field + accessor on `MembershipPlan`.
  - One setter on `MembershipPlan.Builder`.
  - Optionally one validation line in `build()`.
- That is the OCP payoff in concrete numbers.

### 11. What went well / what's next  [Either]
- **Worked well**
  - Both patterns map cleanly to real-domain concerns — no contrived plumbing.
  - Builder centralises validation rules we would otherwise scatter across constructors.
  - Observer + typed events keep `Gym` ignorant of presentation.
  - The lifecycle enum proves correctness without an extra class.
- **What's next**
  - Persistence (Repository pattern over JDBC).
  - Async notifier dispatch — currently synchronous fan-out blocks the publisher.
  - Bulk-event batching for the Promotion broadcast case.

### 12. Q&A — likely questions  [Both]
- "Why not Lombok `@Builder`?" — the brief asks for hand-written Java; zero external dependencies.
- "Why an interface for `MemberNotifier` instead of an abstract class?" — there's no shared state to inherit; only behaviour.
- "Why immutable plans?" — pricing/tier must not drift after signup; immutable plans guarantee that.
- "Why typed events instead of `notify(String)`?" — keeps `Gym` ignorant of presentation; lets each notifier render in its own voice.
- Backup cheat-sheet: `docs/design/study-guide.md`.

---

## Timing budget

| Slide | Time | Speaker |
|---|---|---|
| 1 — Title | 0:30 | Either |
| 2 — Domain | 1:00 | E |
| 3 — What it does | 1:00 | E |
| 4 — Architecture | 0:45 | T2 |
| 5 — Why Builder | 1:30 | T2 |
| 6 — Why Observer | 1:30 | E |
| 7 — Lifecycle | 0:45 | T2 |
| 8 — SOLID | 0:50 | E |
| 9 — Live demo | 3:00 | T2 drives · E narrates |
| 10 — Extension | 0:45 | E |
| 11 — Recap | 0:30 | Either |
| 12 — Q&A | 2–5:00 | Both |
| **Total** | **~13 min + Q&A** | |

If short on time: cut slide 8 (SOLID) and shorten slide 11.

Rough airtime: **E ≈ 5:30** · **T2 ≈ 5:15** · shared **3:30**.

---

## Pre-demo checklist

- [ ] `javac -d bin src/main/java/*.java src/main/java/gui/*.java` runs clean.
- [ ] `java -cp bin Main` — all 6 sections finish with `[PASS]`.
- [ ] `java -jar GymManagerGUI.jar` opens; banner is teal; table renders; status bar shows "Ready".
- [ ] Have a known-bad input ready: `monthlyFee(-1)` for Builder failure; `CANCELLED → ACTIVE` for transition failure.
- [ ] Backup screenshots in case the projector dies.

---

## Rendered slide deck

The actual slide deck is generated from `PRESENTATION.md` at the
project root via Marp:

```bash
npx -y @marp-team/marp-cli PRESENTATION.md -o PRESENTATION.pptx
```

That produces `PRESENTATION.pptx` — editable in PowerPoint / Google
Slides / Keynote. Use this outline as the speaker-notes companion.
