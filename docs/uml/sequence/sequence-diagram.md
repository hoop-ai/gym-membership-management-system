# Sequence Diagram - Enrol Member In Class

This diagram walks through a single call from `Main` into the Gym:
`gym.enrolMemberInClass(1, "yoga flow")`. It traces every interaction between
the entry point, the `Gym` Subject, the `FitnessClass` being modified, and the
three concrete observers attached during setup (`ConsoleObserver`,
`AuditFileObserver`, `InMemoryJournalObserver`). The two internal lookups
(`getMember`, `getFitnessClass`) are shown as self-messages on `Gym` because
they are private collaborations inside the same object.

The headline of the diagram is **step 7** combined with the three messages
underneath it. `Gym` calls its own private `publish(...)` exactly once, with a
single `MemberEnrolledInClassEvent`. That one publish call fans out to every
registered observer in turn - the console prints, the audit file appends, and
the in-memory journal records. This is the entire point of the Observer
pattern: one event, many side effects, zero coupling between `Gym` and any
specific reaction.

```mermaid
sequenceDiagram
    autonumber
    participant Main
    participant Gym
    participant Yoga as FitnessClass<br/>(Yoga Flow)
    participant Console as ConsoleObserver
    participant Audit as AuditFileObserver
    participant Journal as InMemoryJournalObserver

    Main->>Gym: enrolMemberInClass(1, "yoga flow")
    activate Gym

    Gym->>Gym: getMember(1)
    Note right of Gym: returns Sarah Connor

    Gym->>Gym: getFitnessClass("yoga flow")
    Note right of Gym: case-insensitive lookup<br/>returns the "Yoga Flow" class

    Gym->>Yoga: hasMember(sarah)
    Yoga-->>Gym: false

    Gym->>Yoga: isFull()
    Yoga-->>Gym: false

    Gym->>Yoga: addMember(sarah)
    Yoga-->>Gym: ok

    Gym->>Gym: publish(new MemberEnrolledInClassEvent(sarah, yoga))
    Note over Gym,Journal: Single publish call fans out<br/>to every registered observer

    Gym->>Console: onEvent(event)
    Note right of Console: prints "[HH:MM:SS] [MEMBER_ENROLLED] ..." to stdout
    Console-->>Gym: 

    Gym->>Audit: onEvent(event)
    Note right of Audit: appends one line to audit.log
    Audit-->>Gym: 

    Gym->>Journal: onEvent(event)
    Note right of Journal: stores event in in-memory list
    Journal-->>Gym: 

    Gym-->>Main: return
    deactivate Gym
```

## Why this matters

- One method call on the Subject (`Gym.publish`) drives N observers without
  `Gym` knowing what they are. Add a fourth observer (e.g. a metrics emitter)
  and `Gym` does not change.
- All publication goes through the same private `publish(GymEvent)` method.
  `Member`, `FitnessClass`, and `Main` never call `onEvent` directly - so when
  you are debugging "why did this fire?", you only have to look in one place.
- The lookups in steps 2 and 3 are deliberately separate steps so the diagram
  reflects the two-stage validation that happens before any state changes
  (and before any event is published).

The PlantUML mirror of this diagram is in
[publish-event-sequence.puml](publish-event-sequence.puml).
