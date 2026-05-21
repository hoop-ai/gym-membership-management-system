# Submission Guide

## What to submit

Per the assignment guide (Section 5.1), the deliverable is:

1. **Report** -- PDF format
2. **Java source code** -- a single ZIP file

## Step-by-step submission instructions

### Step 1: Convert the report to PDF

The report lives at `docs/report/report.md`.

**Option A -- GitHub (easiest):**

1. Open the report on GitHub (diagrams render automatically).
2. Print to PDF from your browser (Ctrl+P -> Save as PDF).

**Option B -- VS Code:**

1. Open the report in VS Code.
2. Install the "Markdown PDF" extension (`yzane.markdown-pdf`).
3. Right-click the file -> "Markdown PDF: Export (pdf)".

**Option C -- Online:**

1. Go to https://markdownlivepreview.com/ or https://dillinger.io/
2. Paste the report content.
3. Export as PDF.

### Step 2: Create the ZIP file

The ZIP should contain ONLY the 19 Java source files (14 core + 5 GUI):

```
SEN3006_Gym_Membership_Management_System.zip
├── AccessTier.java
├── ClassCancelledEvent.java
├── EmailMemberNotifier.java
├── Gym.java
├── GymEvent.java
├── GymManagementApp.java
├── Main.java
├── Member.java
├── MemberNotifier.java
├── MembershipPlan.java
├── MembershipStatus.java
├── PaymentDueEvent.java
├── PromotionEvent.java
├── PushMemberNotifier.java
├── RenewalReminderEvent.java
├── SmsMemberNotifier.java
└── gui/
    ├── DemoScenarios.java
    ├── GymManagerGUI.java
    ├── MemberFormPanel.java
    ├── MemberTableModel.java
    └── MemberTablePanel.java
```

**To create the ZIP:**

Windows (PowerShell):

```powershell
Compress-Archive -Path "src\main\java\*","src\main\java\gui" -DestinationPath "SEN3006_Gym_Membership_Management_System.zip"
```

Git Bash, macOS or Linux:

```bash
cd src/main/java && zip -r ../../../SEN3006_Gym_Membership_Management_System.zip *.java gui
```

### Step 3: Verify before submitting

Before you upload, walk through this checklist:

- [ ] Report is in PDF format.
- [ ] PDF has every UML diagram visible.
- [ ] PDF contains all 9 required sections (Introduction through References).
- [ ] ZIP contains 19 `.java` files (14 in the root + 5 inside `gui/`).
- [ ] After unzipping, the code compiles:
      `javac -d bin *.java gui/*.java`
- [ ] After compiling, the tests run:
      `java -cp bin Main` -> prints `ALL TESTS PASSED`.
- [ ] The runnable `GymManagerGUI.jar` opens the GUI without errors.

### Step 4: Submit

Upload both files to the submission portal before the assignment deadline.

## For the presentation

Review these files before your presentation:

- `docs/design/study-guide.md` -- every class explained + Q&A cheat-sheet.
- `docs/design/presentation-outline.md` -- slide-by-slide plan.
- `docs/design/test-documentation.md` -- what each test proves.

During the demo:

1. Open the GitHub repository (Mermaid diagrams render visually in the `.md`
   files, so the professor can scroll through the report without a PDF reader).
2. Run `java -cp bin Main` live and walk through the labelled `[PASS]` output.
3. Open `java -jar GymManagerGUI.jar`, load the *Observer demo* from the
   menu, and publish a payment-due event so the professor sees the event log
   light up.
4. Point to specific classes (`MembershipPlan.Builder`, `MemberNotifier`,
   `Gym.publishEvent`) when explaining the patterns.
