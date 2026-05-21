# Class Diagram

Gym Membership Management System -- 19 classes organised in three layers
(domain / pattern / coordination). The PlantUML source is in
[gym-management-class.puml](gym-management-class.puml).

```mermaid
classDiagram
    class MembershipPlan {
        -name String
        -durationMonths int
        -monthlyFee double
        -accessTier AccessTier
        -includedClasses Set~String~
        -guestPassesPerMonth int
        -freezeDaysPerYear int
        -personalTrainerIncluded boolean
        +getTotalCost() double
    }

    class MembershipPlanBuilder {
        +Builder(name)
        +durationMonths(int) Builder
        +monthlyFee(double) Builder
        +accessTier(AccessTier) Builder
        +includesClass(String) Builder
        +guestPassesPerMonth(int) Builder
        +freezeDaysPerYear(int) Builder
        +personalTrainerIncluded(bool) Builder
        +build() MembershipPlan
    }

    class Member {
        -id int
        -name String
        -email String
        -phone String
        -plan MembershipPlan
        -status MembershipStatus
        -renewalDate LocalDate
        -notifiers List~MemberNotifier~
        +setStatus(MembershipStatus)
        +attachNotifier(MemberNotifier)
        +detachNotifier(MemberNotifier)
    }

    class MembershipStatus {
        <<enum>>
        PENDING
        ACTIVE
        EXPIRING
        EXPIRED
        FROZEN
        CANCELLED
        +canTransitionTo(MembershipStatus) bool
    }

    class AccessTier {
        <<enum>>
        BASIC
        STANDARD
        PREMIUM
    }

    class GymEvent {
        <<abstract>>
        -timestamp LocalDateTime
        -targetMember Member
        -message String
        +isBroadcast() bool
        +getType() String
    }

    class PaymentDueEvent {
        -dueDate LocalDate
        -amount double
    }

    class RenewalReminderEvent {
        -renewalDate LocalDate
    }

    class ClassCancelledEvent {
        -className String
        -classDate LocalDate
    }

    class PromotionEvent {
        -discountPercent double
    }

    class MemberNotifier {
        <<interface>>
        +getMember() Member
        +getChannel() String
        +onEvent(GymEvent)
    }

    class EmailMemberNotifier
    class SmsMemberNotifier
    class PushMemberNotifier

    class Gym {
        -name String
        -members List~Member~
        -planCatalogue Map~String,MembershipPlan~
        -eventJournal List~GymEvent~
        +registerPlan(MembershipPlan)
        +enrolMember(name, email, phone, planName) Member
        +publishEvent(GymEvent)
    }

    MembershipPlan ..> AccessTier
    MembershipPlanBuilder ..> MembershipPlan : build()

    Member o--> MembershipPlan
    Member ..> MembershipStatus
    Member o--> MemberNotifier

    GymEvent <|-- PaymentDueEvent
    GymEvent <|-- RenewalReminderEvent
    GymEvent <|-- ClassCancelledEvent
    GymEvent <|-- PromotionEvent
    GymEvent ..> Member

    MemberNotifier <|.. EmailMemberNotifier
    MemberNotifier <|.. SmsMemberNotifier
    MemberNotifier <|.. PushMemberNotifier
    MemberNotifier ..> GymEvent

    Gym o--> Member
    Gym o--> MembershipPlan
    Gym ..> GymEvent
    Gym ..> MemberNotifier
```

## What to look at

- **Two pattern hierarchies sit side by side.** The Builder side is
  `MembershipPlan` and its nested `Builder` (drawn as
  `MembershipPlanBuilder` here because Mermaid does not render nested
  notation cleanly). The Observer side is `GymEvent` with four
  subclasses plus `MemberNotifier` with three implementations.
- **`Gym` only references abstractions.** Its fields are
  `List<Member>`, `Map<String, MembershipPlan>`, and `List<GymEvent>`
  -- never a concrete event or notifier.
- **`MembershipStatus` sits alongside `Member`** and provides the
  lightweight State pattern that enforces lifecycle transitions.
