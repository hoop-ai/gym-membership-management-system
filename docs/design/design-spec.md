# Design Specification -- Gym Membership Management System

Technical design reference for the SEN3006 Gym Membership Management
System. Pair this document with the source files in `src/main/java/`
and the UML diagrams in `docs/uml/`.

---

## 1. System overview

A single-JVM, in-memory Java application with three interchangeable
presentation layers:

| Entry point | Class | Purpose |
|-------------|-------|---------|
| Scripted demo | `Main` | Runs 6 self-checking test sections. |
| Console menu | `GymManagementApp` | Interactive menu-driven CLI. |
| Swing GUI | `gui.GymManagerGUI` | Visual demo with live notification log. |

All three drive the same engine -- the `Gym` -- through its public API.

---

## 2. Class signatures (engine)

### 2.1 `MembershipPlan` + `MembershipPlan.Builder`

```java
public final class MembershipPlan {
    public String      getName();
    public int         getDurationMonths();
    public double      getMonthlyFee();
    public AccessTier  getAccessTier();
    public Set<String> getIncludedClasses();   // unmodifiable
    public int         getGuestPassesPerMonth();
    public int         getFreezeDaysPerYear();
    public boolean     isPersonalTrainerIncluded();
    public double      getTotalCost();          // monthlyFee * durationMonths

    public static final class Builder {
        public Builder(String name);            // required, non-blank
        public Builder durationMonths(int);     // default 1
        public Builder monthlyFee(double);      // default 0.0
        public Builder accessTier(AccessTier);  // default BASIC
        public Builder includesClass(String);   // append to set
        public Builder guestPassesPerMonth(int);// default 0
        public Builder freezeDaysPerYear(int);  // default 0
        public Builder personalTrainerIncluded(boolean);
        public MembershipPlan build();          // validates + returns immutable
    }
}
```

All validation runs in `Builder.build()`: duration positive, fee
non-negative, tier non-null, guest passes / freeze days non-negative.
The Builder's constructor enforces the only truly mandatory field, the
plan's display name.

### 2.2 `Member`

```java
public class Member {
    public Member(String name, String email, String phone, MembershipPlan plan);

    public int               getId();
    public String            getName();
    public String            getEmail();
    public String            getPhone();
    public LocalDateTime     getJoinedAt();
    public MembershipPlan    getPlan();
    public MembershipStatus  getStatus();
    public LocalDate         getRenewalDate();

    public void setStatus(MembershipStatus newStatus);  // validated transition
    public void changePlan(MembershipPlan newPlan);

    public void attachNotifier(MemberNotifier notifier);
    public void detachNotifier(MemberNotifier notifier);
    public List<MemberNotifier> getNotifiers();          // unmodifiable
}
```

### 2.3 `MembershipStatus` (enum)

Six constants -- `PENDING`, `ACTIVE`, `EXPIRING`, `EXPIRED`, `FROZEN`,
`CANCELLED` -- each overriding `allowedTransitions()`. The public
method `canTransitionTo(MembershipStatus)` consults that set.

| From       | Allowed to                       |
|------------|----------------------------------|
| `PENDING`  | `ACTIVE`, `CANCELLED`            |
| `ACTIVE`   | `EXPIRING`, `FROZEN`, `CANCELLED`|
| `EXPIRING` | `ACTIVE`, `EXPIRED`, `CANCELLED` |
| `EXPIRED`  | (terminal)                       |
| `FROZEN`   | `ACTIVE`, `CANCELLED`            |
| `CANCELLED`| (terminal)                       |

### 2.4 `AccessTier` (enum)

Three constants in increasing-privilege order: `BASIC`, `STANDARD`,
`PREMIUM`. The enum carries no per-constant code; it is a simple,
ordered label.

### 2.5 Event hierarchy

```java
public abstract class GymEvent {
    public LocalDateTime getTimestamp();
    public Member        getTargetMember();   // null = broadcast
    public String        getMessage();
    public boolean       isBroadcast();        // targetMember == null
    public abstract String getType();          // "PAYMENT_DUE", "RENEWAL_REMINDER", ...
}

public class PaymentDueEvent     extends GymEvent { /* dueDate, amount */ }
public class RenewalReminderEvent extends GymEvent { /* renewalDate */ }
public class ClassCancelledEvent  extends GymEvent { /* className, classDate; broadcastable */ }
public class PromotionEvent       extends GymEvent { /* discountPercent; always broadcast */ }
```

### 2.6 `MemberNotifier` (interface) + three concrete observers

```java
public interface MemberNotifier {
    Member  getMember();
    String  getChannel();                  // "EMAIL", "SMS", "PUSH", ...
    void    onEvent(GymEvent event);
}
```

Each concrete notifier (`EmailMemberNotifier`, `SmsMemberNotifier`,
`PushMemberNotifier`) holds an internal `sentLog` of every formatted
message it has emitted. Notifiers must ignore events whose target
member differs from their wrapped member, while delivering every
broadcast.

### 2.7 `Gym`

```java
public class Gym {
    public Gym(String name);

    // Plan catalogue (Builder consumer)
    public void                  registerPlan(MembershipPlan plan);
    public MembershipPlan        getPlan(String name);
    public List<MembershipPlan>  getAllPlans();

    // Member management
    public Member        enrolMember(String name, String email, String phone, String planName);
    public Member        getMember(int id);
    public List<Member>  getAllMembers();              // unmodifiable
    public void          removeMember(int id);
    public void          changeMemberStatus(int memberId, MembershipStatus newStatus);

    // Observer publication
    public void           publishEvent(GymEvent event);
    public List<GymEvent> getEventJournal();           // unmodifiable

    // Convenience publishers
    public void publishPaymentDue(int memberId, LocalDate dueDate, double amount);
    public void publishRenewalReminder(int memberId);
    public void publishClassCancellation(String className, LocalDate classDate);
    public void publishPromotion(double discountPercent, String message);

    public String getSummary();
}
```

`publishEvent` is the single dispatcher: for targeted events it walks
only the affected member's notifiers; for broadcasts it walks every
notifier on every member. The event journal is append-only and powers
the GUI's notification log.

---

## 3. SOLID mapping

| Principle | Evidence |
|-----------|----------|
| **S**RP | Each class has one job. The Gym never builds plans; the Builder never publishes events. |
| **O**CP | Adding a new plan attribute = new field + new builder setter. Adding a new event = new `GymEvent` subclass. Adding a new channel = new `MemberNotifier` implementation. Test 5 installs an anonymous notifier live. |
| **L**SP | Every concrete notifier and event subclass is fully substitutable for its abstraction. |
| **I**SP | `MemberNotifier` has three methods, all used by every implementation. `GymEvent` exposes only universal fields. |
| **D**IP | `Gym` references only abstractions (`MemberNotifier`, `GymEvent`, `MembershipPlan`, `Member`). No `new EmailMemberNotifier(...)` lives inside `Gym`. |

---

## 4. GUI architecture

The Swing GUI is one `JFrame` with four cooperating regions:

| Region | Role |
|--------|------|
| Banner (`NORTH`) | Title + subtitle on a deep-teal strip. |
| Table panel (`CENTER`) | Status filter + member table. Rows colour-coded by lifecycle state. |
| Form panel (`EAST`) | Vertical form for enrolling a member. |
| Action strip + log + status bar (`SOUTH`) | Lifecycle and notification buttons, dark notification log, live status bar. |

To guarantee readable colours on Windows two presentation hacks are in
place:

1. The table-header uses a custom `DefaultTableCellRenderer` rather
   than relying on `JTableHeader.setBackground(...)`, which the
   Windows L&F ignores.
2. Accent buttons call `setUI(new MetalButtonUI())` so the custom
   athletic-teal colours actually paint. This is the standard fix for
   Windows L&F overriding button backgrounds.

Both fixes are local to the GUI classes; the engine is untouched.

---

## 5. Validation rules

| Where | Rule | Exception |
|-------|------|-----------|
| `MembershipPlan.Builder` ctor | name non-blank | `IllegalArgumentException` |
| `MembershipPlan.Builder.build()` | duration >= 1 | `IllegalArgumentException` |
| `MembershipPlan.Builder.build()` | monthly fee >= 0 | `IllegalArgumentException` |
| `MembershipPlan.Builder.build()` | tier non-null | `IllegalArgumentException` |
| `MembershipPlan.Builder.build()` | guest passes / freeze days >= 0 | `IllegalArgumentException` |
| `Member` ctor | name / email non-blank, phone non-null, plan non-null | `IllegalArgumentException` |
| `Member.setStatus` | transition allowed by state machine | `IllegalArgumentException("Cannot transition from X to Y")` |
| `Gym.enrolMember` | plan name registered | `IllegalArgumentException` listing available plans |
| `Gym.getMember` | id exists | `IllegalArgumentException` |
| `Gym.publishEvent` | event non-null | `IllegalArgumentException` |
| Concrete event constructors | target member required for non-broadcasts; payload fields validated | `IllegalArgumentException` |

---

## 6. Extension points

| Want to add | Files to touch | Existing files modified |
|-------------|---------------|------------------------|
| A new plan attribute (e.g. `lockerIncluded`) | `MembershipPlan` (field + accessor), `MembershipPlan.Builder` (field + setter). Optionally validate in `build()`. | Two -- only the plan and its builder. |
| A new event kind (e.g. `MemberBirthdayEvent`) | One new class extending `GymEvent`. Optionally one new convenience publisher on `Gym`. | Zero engine classes; optionally one. |
| A new notification channel (e.g. `DiscordMemberNotifier`) | One new class implementing `MemberNotifier`. Attach with `member.attachNotifier(...)`. | Zero. Test 5 in `Main.java` demonstrates this live. |
| A new lifecycle state | One new constant in `MembershipStatus`, plus updates to neighbouring `allowedTransitions()` methods. | Only the affected enum constants. |
| A new entry point (REST API, mobile, ...) | New top-level class that drives `Gym`. | Zero. The engine has no presentation assumptions. |

These extension paths are what NFR3 (the Open/Closed requirement)
demands, and Test 5 in `Main.java` proves they hold.
