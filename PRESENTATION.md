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

*Two design patterns made visible inside a working gym back-office.*

---

## Why a gym?

A gym back-office has **two recurring problems** that map to the patterns:

- **Plans have many optional fields** — name, duration, monthly fee, access tier, included classes, guest passes, freeze days, personal trainer. Most plans configure only 3–4.
  → Constructors blow up. **Creational problem.**
- **Notifications fan out across channels and target subsets** — payment-due is per-member; class-cancelled is broadcastable; promotion is always broadcast. Each member subscribes to a different mix of email / SMS / push.
  → Tight coupling smell. **Behavioral problem.**

<div class="why">

The domain was picked because the patterns **fit**, not the other way around.

</div>

---

## What the system does

- **Engine:** one `Gym` aggregator.
- **Plans:** 8-field `MembershipPlan` built via nested `Builder`.
- **4 events:** `PaymentDueEvent`, `RenewalReminderEvent`, `ClassCancelledEvent`, `PromotionEvent`.
- **3 channels:** `EmailMemberNotifier`, `SmsMemberNotifier`, `PushMemberNotifier` — attached to **the member**, not to the Gym.
- **Lifecycle (6 states):** `PENDING → ACTIVE → EXPIRING → EXPIRED`, with `FROZEN ↔ ACTIVE` and `CANCELLED` (both terminal).
- **Three entry points** share one engine:
  - `Main` — 6 self-checking test sections
  - `GymManagementApp` — console menu
  - `gui.GymManagerGUI` — Swing window with live notification log

---

## Architecture in one picture

```
┌─────────────────── Entry-point layer ───────────────────┐
│  Main      GymManagementApp      GymManagerGUI (Swing)  │
└────────────────────────┬────────────────────────────────┘
                         │ uses
┌────────────────────── Gym (Subject) ────────────────────┐
│  registerPlan / enrolMember / publishEvent              │
│  publishPaymentDue · publishRenewalReminder             │
│  publishClassCancellation · publishPromotion            │
└─────────┬───────────────────────────────┬───────────────┘
          │ owns Members                  │ fires events
┌─────────▼─────────┐               ┌─────▼────────────────┐
│  Member           │               │  GymEvent (abstract) │
│  └ List<Notifier> │◀──────────────│  ├ PaymentDue        │
└─────────┬─────────┘  delivered to │  ├ RenewalReminder   │
          │ attached                │  ├ ClassCancelled    │
┌─────────▼─────────┐               │  └ Promotion         │
│  MemberNotifier   │               └──────────────────────┘
│  ├ Email          │
│  ├ SMS            │
│  └ Push           │
└───────────────────┘

┌─────────── MembershipPlan (immutable, final) ──────────┐
│   built via MembershipPlan.Builder · validated on build │
└─────────────────────────────────────────────────────────┘
```

---

## Why Builder?  (the rationale)

**The pain it solves here:**

`MembershipPlan` has **8 fields**. Only `name` is truly mandatory.

```java
// Without Builder — 8 positional args, unreadable:
new MembershipPlan("Gold", 12, 49.99, PREMIUM,
    Set.of("yoga","spin"), 2, 30, true);
```

The alternative — a telescoping ladder of overloaded constructors — explodes combinatorially. Every new field doubles the surface area.

<div class="why">

We didn't reach for Builder because "Creational was required." We reached for it because the constructor was already a mess.

</div>

---

## Why Builder?  (continued)

**Why not a setter-based POJO?**
Plans must be **immutable** once registered. Pricing and tier cannot drift after a member signs up. `MembershipPlan` is `public final`, no setters. Builder gives a "build once, never mutate" contract.

**Why centralise validation in `build()`?**
Rules are combinations of fields (duration >= 1; fee >= 0; tier non-null; guest passes / freeze days >= 0). Putting them all in `build()` means no caller can sneak past with a hand-rolled partial object.

**The payoff — call sites read like English:**

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

---

## Why Observer?  (the rationale)

**The pain it solves here:**

Each member subscribes to a **different subset of channels**, and event types fan out to different targets (one member vs. all members).

```java
// Without Observer — branch hell, scattered everywhere:
if (member.hasEmail()) emailSvc.send(...);
if (member.hasSms())   smsSvc.send(...);
if (member.hasPush())  pushSvc.send(...);
```

Repeat in *every* event-producing method. Add a 4th channel → edit every call site.

<div class="why">

1-to-many fan-out across heterogeneous channels **with runtime subscription changes** is the canonical Observer fit.

</div>

---

## Why Observer?  (continued)

**Why notifiers attach to the member, not the Gym?**
Subscriptions belong to the person being notified — a member who only wants SMS shouldn't pollute another member's channel list. `Gym.publishEvent` walks the *target* member's notifiers for targeted events, every member's notifiers for broadcasts.

**Why typed `GymEvent` subclasses, not `notify(String)`?**

- `PaymentDueEvent` → amount + due date
- `ClassCancelledEvent` → class name + date (broadcastable)
- `PromotionEvent` → discount percent (always broadcast)

A string API would force `Gym` to render messages itself — leaking presentation into the domain. Typed events let each notifier render in its own voice (Email = long body, SMS = one-liner, Push = 40-char headline).

```java
gym.publishPaymentDue(memberId, dueDate, 49.99);
// One call · 3 channels · 3 different messages · Gym never knows.
```

---

## Bonus pattern — lifecycle as a state machine

```
       PENDING ──► ACTIVE ──► EXPIRING ──► EXPIRED (terminal)
                    ▲ ▼
                   FROZEN

             ACTIVE ──► CANCELLED (terminal)
```

- `MembershipStatus` enum carries its own transition table per constant.
- `Member.setStatus(...)` rejects illegal moves with `IllegalArgumentException("Cannot transition from X to Y")`.
- Third pattern (State) for **zero extra files**.
- Compile-time guarantee: cannot reach `EXPIRING` from `CANCELLED`.

---

## SOLID — 10 seconds each

- **S**RP — `Gym` coordinates; Builder builds; notifiers render; events carry data. One job per class.
- **O**CP — new channel = new file. New event = new subclass. Test 5 installs an **anonymous notifier at runtime**.
- **L**SP — every notifier and event subclass is fully substitutable.
- **I**SP — `MemberNotifier` exposes 3 minimal methods, all used.
- **D**IP — `Gym` references only abstractions; no `new EmailMemberNotifier(...)` lives inside `Gym`.

---

## Live demo (≈ 3 minutes)

1. **Scripted demo** — `java -cp bin Main` · pause on Section 6: one publish call → 3 channels render different messages.
2. **Open GUI** — `java -jar GymManagerGUI.jar` · show banner / table / form / log / status-bar.
3. **Builder validation** — try `monthlyFee(-1)` · engine throws · dialog explains.
4. **Notification fan-out** — click *Publish payment due* on a known member · log fills with one row per attached notifier.
5. **Illegal transition** — `CANCELLED → ACTIVE` · engine refuses with exact reason.

> Validation lives in the engine, not the GUI. The GUI just surfaces the message.

---

## Extension story — zero-edit proof

**Add a Discord channel:**

```java
public class DiscordMemberNotifier implements MemberNotifier { ... }

member.attachNotifier(new DiscordMemberNotifier(member, webhookUrl));
```

**Add a `MemberBirthdayEvent`:**

```java
public class MemberBirthdayEvent extends GymEvent { ... }

gym.publishEvent(new MemberBirthdayEvent(member));
```

**Add a `lockerIncluded` plan attribute:**

- One field + accessor on `MembershipPlan`.
- One setter on `MembershipPlan.Builder`.

**Zero edits to existing classes.** That is the OCP payoff.

---

## What went well / what's next

**Worked well**

- Both patterns map cleanly to real-domain concerns — no contrived plumbing.
- Builder centralises rules we'd otherwise scatter across constructors.
- Observer + typed events keep `Gym` ignorant of presentation.
- Lifecycle enum proves correctness without an extra class.

**What's next**

- Persistence (Repository pattern over JDBC).
- Async notifier dispatch (currently synchronous).
- Bulk-event batching for the Promotion broadcast case.

---

## Q & A

Likely questions:

- "Why not Lombok `@Builder`?" → brief asks for hand-written Java, zero deps.
- "Why an interface for `MemberNotifier`?" → no shared state to inherit.
- "Why immutable plans?" → pricing/tier mustn't drift after signup.
- "Why typed events?" → keep `Gym` ignorant of presentation.

Backup answers: `docs/design/study-guide.md`.

---

# Thank you

### Questions?

`github.com/hoop-ai/gym-membership-management-system`
