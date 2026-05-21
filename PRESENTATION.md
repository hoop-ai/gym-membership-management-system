---
marp: true
theme: default
paginate: true
size: 16:9
header: 'SEN3006 — Gym Membership Management System'
footer: 'Builder + Observer · Elif'
style: |
  section { font-size: 26px; }
  h1 { color: #0f766e; }
  h2 { color: #0f766e; }
  code { background: #f4f4f5; padding: 2px 6px; border-radius: 4px; }
  .small { font-size: 20px; }
  .why { background: #ecfeff; padding: 12px 16px; border-left: 4px solid #0e7490; border-radius: 4px; }
---

# Gym Membership Management System
### Builder + Observer in pure Java

**SEN3006 — Software Architecture**
Elif · June 2026

<br>

*Two design patterns made visible in a working application.*

---

## Why a gym?

A gym membership has **two recurring problems** that map to the patterns:

- **Plans have many optional fields** — duration, tier, freeze allowance, auto-renew, discount, trial days, group/PT credits.
  → Constructors explode. **Creational problem.**
- **Notifications fan out** — every event (payment due, renewal, class cancel, promo) reaches a per-member subset of channels (email/SMS/push).
  → Tight coupling smell. **Behavioral problem.**

<div class="why">

The domain was picked because the patterns **fit**, not the other way around.

</div>

---

## What the system does

- **Domain:** `Member`, `MembershipPlan`, `Gym`
- **4 events:** `PaymentDue`, `RenewalReminder`, `ClassCancelled`, `Promotion`
- **3 channels:** `Email`, `SMS`, `Push`
- **Lifecycle:** `PENDING → ACTIVE → EXPIRING → EXPIRED`, with `FROZEN ↔ ACTIVE`, `CANCELLED`
- **Three entry points** share one engine:
  - `Main` — scripted demo
  - `GymManagementApp` — console menu
  - `GymManagerGUI` — Swing window

---

## Architecture in one picture

```
┌─────────────────── Entry-point layer ───────────────────┐
│  Main      GymManagementApp      GymManagerGUI (Swing)  │
└────────────────────────┬────────────────────────────────┘
                         │ uses
┌────────────────────── Gym (Subject) ────────────────────┐
│  subscribe / unsubscribe / fireEvent                    │
└─────────┬────────────────────────────┬──────────────────┘
          │ has-a                      │ fires
┌─────────▼─────────┐         ┌────────▼─────────────┐
│  MemberNotifier   │         │  GymEvent (abstract) │
│  ├ Email          │         │  ├ PaymentDue        │
│  ├ SMS            │         │  ├ RenewalReminder   │
│  └ Push           │         │  ├ ClassCancelled    │
└───────────────────┘         │  └ Promotion         │
                              └──────────────────────┘

┌─────────── MembershipPlan (immutable) ──────────┐
│   built via MembershipPlan.Builder              │
└─────────────────────────────────────────────────┘
```

---

## Why Builder?  (the rationale)

**The pain it solves here:**

`MembershipPlan` has **9 fields, 4 optional**.

```java
// Without Builder — unreadable, error-prone:
new MembershipPlan(12, GOLD, true, 30, 0.10, false, 0, 2, true);
```

What does each arg mean at the call site? Nothing.

<div class="why">

We didn't reach for Builder because "Creational was required." We reached for it because the constructor was already a mess.

</div>

---

## Why Builder?  (continued)

**Why not setters?** Plans must be **immutable** once issued — pricing/duration cannot drift after signup. Setters break immutability.

**Why validation in `build()`?** Some field combinations are invalid (trial > duration, freeze on basic tier). Centralising rules in `build()` means no caller can sneak past them.

**The payoff — call site reads like English:**

```java
new MembershipPlan.Builder("Gold Annual")
    .durationMonths(12)
    .tier(GOLD)
    .autoRenew(true)
    .freezeDays(30)
    .build();
```

---

## Why Observer?  (the rationale)

**The pain it solves here:**

Each member subscribes to a **different subset of channels**, and each event needs to fan out to all of them.

```java
// Without Observer — branch hell, scattered everywhere:
if (member.hasEmail()) emailSvc.send(...);
if (member.hasSms())   smsSvc.send(...);
if (member.hasPush())  pushSvc.send(...);
```

Repeat this in *every* event method. Add a 4th channel → edit every call site.

<div class="why">

1-to-many fan-out across heterogeneous channels is the textbook Observer fit.

</div>

---

## Why Observer?  (continued)

**Why a true Subject/Observer split?**
- Observers subscribe/unsubscribe **at runtime** (member changes preferences).
- Adding a new channel = **zero edits** to `Gym`.

**Why typed `GymEvent` subclasses, not `notify(String)`?**
- Each event has different payload (`PaymentDue` → amount + due date; `ClassCancelled` → class name + slot).
- A string-based API would force `Gym` to format messages — leaking presentation into the domain.
- Typed events let each notifier render in its own voice.

```java
gym.fireEvent(new RenewalReminderEvent(member, plan));
// Email renders HTML-ish, SMS terse, Push short — Gym never knows.
```

---

## Bonus pattern — Lifecycle as a state machine

```
       PENDING ──► ACTIVE ──► EXPIRING ──► EXPIRED
                    ▲ ▼
                   FROZEN

             ACTIVE ──► CANCELLED (terminal)
```

- `MembershipStatus` enum carries its own transition table.
- `Member.setStatus(...)` rejects illegal moves with `IllegalArgumentException`.
- Third pattern (State) for **zero extra files**.
- Compile-time guarantee: cannot reach `EXPIRING` from `CANCELLED`.

---

## SOLID — 10 seconds each

- **S**RP — `Gym` coordinates, notifiers render, events carry data.
- **O**CP — new notifier = new file. **Zero edits** to `Gym`.
- **L**SP — every notifier works through `MemberNotifier`.
- **I**SP — `MemberNotifier` has *one* method.
- **D**IP — `Gym` holds `List<MemberNotifier>`, never a concrete class.

---

## Live demo (≈ 3 minutes)

1. **Scripted demo** — `java -cp bin Main` · pause on the Observer fan-out output (1 event → 3 channels)
2. **Open GUI** — `java -jar GymManagerGUI.jar` · load *Observer demo* scenario
3. **Toggle a channel at runtime** — untick "SMS" · fire another event · SMS stops printing
4. **Trigger Builder validation** — duration `0` · engine throws · dialog explains
5. **Trigger illegal transition** — `CANCELLED → ACTIVE` · engine refuses

> Validation lives in the engine, not the GUI. The GUI just surfaces the message.

---

## Extension story — zero-edit proof

**Add a WhatsApp channel:**

```java
// 1. New file:
public class WhatsAppMemberNotifier implements MemberNotifier { ... }

// 2. One subscription line:
gym.subscribe(new WhatsAppMemberNotifier());
```

**Add a `MembershipUpgradeEvent`:**

```java
// 1. One class extending GymEvent.
// 2. One fireEvent call from wherever upgrades happen.
```

**Zero edits to existing classes.** That is the payoff.

---

## What went well / what's next

**Worked well**
- Patterns map cleanly — no contrived plumbing.
- Builder centralises validation we'd otherwise scatter.
- Observer + typed events keep `Gym` ignorant of channels.

**What's next**
- Persistence (Repository over JDBC).
- Async notifier dispatch (currently synchronous).
- Bulk batching for the Promotion fan-out case.

---

## Q & A

Likely questions:

- "Why not Lombok `@Builder`?" → zero-dependency project.
- "Why an interface, not an abstract class, for `MemberNotifier`?" → no shared state to inherit.
- "Why immutable plans?" → pricing can't drift after signup.
- "Why typed events?" → keep `Gym` ignorant of presentation.

Backup answers: `docs/design/study-guide.md`.

---

# Thank you
### Questions?

`github.com/hoop-ai/gym-membership-management-system`
