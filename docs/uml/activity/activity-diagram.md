# Activity Diagram -- Member lifecycle

End-to-end workflow of a gym member, from sign-up to expiration or
cancellation. PlantUML source:
[membership-lifecycle-activity.puml](membership-lifecycle-activity.puml).

```mermaid
flowchart TD
    A[Build plan via MembershipPlan.Builder] --> B[Register plan in Gym catalogue]
    B --> C[Member signs up]
    C --> D[Gym.enrolMember -> Member in PENDING]
    D --> E[Member attaches notifiers]
    E --> F{First payment cleared?}

    F -- no  --> X[Transition to CANCELLED]
    X --> Z((End))

    F -- yes --> G[Transition to ACTIVE]
    G --> H{Member needs a break?}

    H -- yes --> I[Transition to FROZEN]
    I --> J[Wait for resume]
    J --> K[Transition back to ACTIVE]
    K --> H

    H -- no --> L{Within renewal grace window?}
    L -- no  --> M[Stay ACTIVE]
    M --> N[Gym may publish Payment/Promotion/ClassCancelled events]
    N --> H

    L -- yes --> O[Transition to EXPIRING]
    O --> P[Gym publishes RenewalReminderEvent]
    P --> Q{Member renews?}
    Q -- yes --> R[Transition back to ACTIVE]
    R --> H
    Q -- no  --> S[Transition to EXPIRED]
    S --> Z
```

## How the activity diagram maps to the code

| Activity step | Concrete method call |
|---------------|----------------------|
| Build a plan | `new MembershipPlan.Builder(name).durationMonths(...).monthlyFee(...).build()` |
| Register a plan | `gym.registerPlan(plan)` |
| Enrol a member | `gym.enrolMember(name, email, phone, planName)` |
| Attach notifier | `member.attachNotifier(new EmailMemberNotifier(member))` |
| Activate | `member.setStatus(MembershipStatus.ACTIVE)` |
| Freeze / Resume | `member.setStatus(MembershipStatus.FROZEN)` / `MembershipStatus.ACTIVE` |
| Enter grace window | `member.setStatus(MembershipStatus.EXPIRING)` |
| Renewal reminder | `gym.publishRenewalReminder(member.getId())` |
| Renew | `member.setStatus(MembershipStatus.ACTIVE)` (renewal date is auto-pushed) |
| Expire | `member.setStatus(MembershipStatus.EXPIRED)` |
| Cancel | `member.setStatus(MembershipStatus.CANCELLED)` |

Every step is validated by `Member.setStatus(...)`; an invalid call
(e.g. jumping from `PENDING` straight to `EXPIRED`) throws
`IllegalArgumentException`.
