# Sequence Diagram

Two flows: building a plan via the Builder, then enrolling a member
and publishing a targeted event through the Observer fabric. PlantUML
source: [publish-event-sequence.puml](publish-event-sequence.puml).

```mermaid
sequenceDiagram
    actor Staff
    participant Builder as MembershipPlan.Builder
    participant Plan as MembershipPlan
    participant Gym
    participant Alice as Member alice
    participant Email as EmailMemberNotifier
    participant Push as PushMemberNotifier

    Note over Staff,Builder: Build a plan (Builder pattern)

    Staff->>Builder: new Builder("Premium Annual")
    Staff->>Builder: durationMonths(12)
    Staff->>Builder: monthlyFee(89.99)
    Staff->>Builder: accessTier(PREMIUM)
    Staff->>Builder: includesClass("Yoga")
    Staff->>Builder: build()
    Builder->>Plan: new MembershipPlan(this)
    Plan-->>Builder: plan
    Builder-->>Staff: MembershipPlan

    Note over Staff,Gym: Register the plan + enrol Alice

    Staff->>Gym: registerPlan(plan)
    Staff->>Gym: enrolMember("Alice", ...)
    Gym->>Alice: new Member(...)
    Alice-->>Gym: member
    Gym-->>Staff: Member

    Staff->>Alice: attachNotifier(EmailMemberNotifier)
    Staff->>Alice: attachNotifier(PushMemberNotifier)

    Note over Staff,Gym: Publish a targeted event (Observer pattern)

    Staff->>Gym: publishPaymentDue(alice.id, dueDate, 89.99)
    Gym->>Alice: getNotifiers()
    Alice-->>Gym: [email, push]
    Gym->>Email: onEvent(event)
    Email->>Email: append to sentLog
    Gym->>Push: onEvent(event)
    Push->>Push: append to sentLog
```

## What to look at

- **Builder in action.** The Staff sets each attribute via a chainable
  method, then calls `build()`. The Builder validates and constructs
  the immutable `MembershipPlan`. The Staff never calls the
  `MembershipPlan` constructor directly -- it is private.
- **Observer in action.** A single `publishPaymentDue(...)` call on
  the `Gym` causes both of Alice's attached notifiers to fire. The
  `Gym` never names `EmailMemberNotifier` or `PushMemberNotifier`
  directly -- it iterates `Alice.getNotifiers()` and calls
  `onEvent(event)` polymorphically through the `MemberNotifier`
  interface.
