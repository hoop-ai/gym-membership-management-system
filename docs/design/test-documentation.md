# Test Documentation -- Gym Membership Management System

How to run the automated tests, what each section proves, and how to
fix the most common failures.

---

## How to run

From the project root:

```sh
javac -d bin src/main/java/*.java src/main/java/gui/*.java
java -cp bin Main
```

A successful run ends with the banner:

```
==========================================================
  ALL TESTS PASSED
==========================================================
```

If the build is clean (no compile errors) and `Main` prints
`ALL TESTS PASSED`, the engine works as designed.

---

## The six test sections

| # | Section | What it proves |
|---|---------|---------------|
| 1 | Builder demo | Three distinct plans (basic, standard, premium) are built via fluent chains; the premium plan exercises every optional attribute; `getTotalCost()` returns the expected value. |
| 2 | Observer demo | Targeted events reach only the affected member's notifiers; broadcasts reach every attached notifier; channel-specific formatting is applied (email, SMS truncated to 110 chars, push title-plus-body). Three explicit assertions confirm message counts: Alice = 6, Bob = 3, Chloe = 6. |
| 3 | Lifecycle demo | Every allowed `MembershipStatus` transition succeeds. Renewals push the renewal date forward. The `FROZEN` branch resolves correctly back to `ACTIVE`. Invalid transitions (`PENDING -> EXPIRED`) and the terminal state (`EXPIRED -> anything`) throw `IllegalArgumentException` with clear messages. |
| 4 | Integration demo | A realistic mixed workflow: register plans (Builder), enrol three members, attach a different notifier per member, transition each to `ACTIVE`, publish a targeted payment-due and a broadcast promotion, print a summary. |
| 5 | SOLID demo | An anonymous `MemberNotifier` (defined inline) is attached to Alice; the next published event reaches it, proving Open/Closed and Dependency Inversion at runtime. The four other SOLID principles are also walked through with code references. |
| 6 | Edge cases | Six guarded failure modes: blank plan name, zero-duration plan, unknown plan name on enrolment, unknown member ID, notifier ignores events for other members, detached notifier no longer receives events. |

Every section prints `[PASS]` on success and `FAIL` (with a clear
reason) on failure. Test 6 also prints `Edge case score: 6/6`.

---

## Expected output (abridged)

```
##########################################################
#                                                        #
#    SEN3006 -- Gym Membership Management System Demo    #
#         Builder  +  Observer  (pure Java)              #
#                                                        #
##########################################################

==========================================================
  TEST 1: Builder Pattern Demo
==========================================================
  Plan[name='Basic Monthly', tier=BASIC, months=1, monthlyFee=29.99, ...]
  Plan[name='Standard Six-Month', tier=STANDARD, months=6, ...]
  Plan[name='Premium Annual', tier=PREMIUM, months=12, ...]
  Premium total cost over 12 months: 1079.88
[PASS] Builder produced three distinct, immutable plans.

==========================================================
  TEST 2: Observer Pattern Demo
==========================================================
  ---- Targeted payment-due event for Alice ----
[EMAIL -> alice@example.com] (PAYMENT_DUE) Payment of 89.99 is due on ...
[PUSH -> Alice Aydin] Payment reminder | Payment of 89.99 is due on ...

  ---- Broadcast: class cancellation ----
[EMAIL -> alice@example.com] (CLASS_CANCELLED) The Spinning class scheduled...
[PUSH -> Alice Aydin] Class update | The Spinning class scheduled...
[SMS -> +90-555-0002] (CLASS_CANCELLED) The Spinning class scheduled...
[EMAIL -> chloe@example.com] (CLASS_CANCELLED) The Spinning class scheduled...
[SMS -> +90-555-0003] (CLASS_CANCELLED) The Spinning class scheduled...
[PUSH -> Chloe Celikel] Class update | The Spinning class scheduled...

  Alice received 6 messages across her 2 channels.
  Bob received   3 messages across his 1 channel.
  Chloe received 6 messages across her 3 channels.

[PASS] Targeted and broadcast events delivered to the right notifiers.

...

==========================================================
  ALL TESTS PASSED
==========================================================
```

---

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `javac: command not found` | JDK not installed or not on `PATH`. | Install Eclipse Temurin or any OpenJDK, then reopen the terminal. |
| `Error: Could not find or load main class Main` | Wrong working directory, or `bin/` has not been compiled. | `cd` into the project root; run the `javac` step first. |
| `Unsupported class file major version 65` | Trying to run a Java 21-compiled JAR on a Java 8 runtime. | Either install a newer JDK or recompile from source against your installed JDK. |
| Garbled characters in the demo output | Console is not in UTF-8. | On Windows: `chcp 65001` before running. On Linux/macOS: this is usually fine by default. |
| One of the tests prints `FAIL` | Source files were edited and broke an invariant. | Compare the failing line with the expected behaviour in this document; fix the source. |
| GUI window looks blank or buttons are unreadable | Windows L&F is hiding custom colours. | Verify you are running the latest JAR (`build-jar.bat` rebuilds it). The current GUI uses custom renderers that bypass the issue. |

---

## Edge cases in Test 6 in detail

| Case | Input | Expected outcome |
|------|-------|------------------|
| Blank plan name | `new MembershipPlan.Builder("")` | `IllegalArgumentException("Plan name must not be null or blank.")` |
| Zero-duration plan | `new MembershipPlan.Builder("Bad").durationMonths(0).build()` | `IllegalArgumentException("Duration must be at least 1 month, ...")` |
| Unknown plan name | `gym.enrolMember(..., "DOES_NOT_EXIST")` | `IllegalArgumentException` listing available plans |
| Missing member ID | `gym.getMember(99999)` | `IllegalArgumentException("No member found with ID: 99999")` |
| Wrong-target event | Calling `onEvent(event)` directly on a notifier whose member is not the event's target | Notifier silently ignores; `sentLog` size unchanged |
| Detached notifier | `member.detachNotifier(n); gym.publishPaymentDue(member.getId(), ...)` | `n.sentLog` unchanged after the detach |

All six cases are exercised by `Main.java` and counted in the
`Edge case score` line.

---

## Manual verification (GUI)

To verify the same behaviour visually:

1. Launch the GUI: `java -jar GymManagerGUI.jar`.
2. Open **Demos -> Load Observer demo** -- the table fills with the
   same three members Test 2 uses (Alice, Bob, Chloe), and the dark
   log strip at the bottom shows the same notifications.
3. Select Alice in the table, click **Payment due...**, accept the
   default date and amount. The log strip prints two new lines (email
   + push) -- both her channels firing.
4. Click **Promotion...** and accept the defaults. Every notifier
   attached to any member fires; the log fills with six lines.
5. Try to click **Mark cooked** -- the button is disabled, mirroring
   the engine's refusal to make an invalid lifecycle transition.
6. Try to enrol a member without selecting a plan -- the engine
   throws and the GUI surfaces the message in a dialog.
