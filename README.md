# Petclinic UI Automation Framework

An end-to-end (E2E) automated testing suite built with Java, Selenium WebDriver, and JUnit 5 for testing the Spring Petclinic Web Application. It features cross-browser support, parallel execution, ExtentReports with auto-captured screenshots on failure, and automated reporting via Microsoft Teams & GitHub Pages.

---

## 📋 What the Tests Cover

* **Navigation Verification**: Validates top header navbar navigation, responsive menu toggles, and page routing across the application.
* **Owner Management**: End-to-end flows for finding existing owners, registering new owners, updating owner details, and validating input forms.
* **Pet & Visit Registration**: UI validation for adding pets to existing owners and scheduling new veterinary visits.
* **Veterinarian Directory**: Verifies listing views, search functionality, and specialty rendering.

---
## 🏗️ Architecture & Tech Stack

* **Language**: Java 17
* **Build Tool**: Maven
* **Test Runner**: JUnit 5 (Jupiter Engine)
* **Design Pattern**: Page Object Model (POM) + Component Object Model
* **Web Automation**: Selenium WebDriver 4.x (with WebDriverManager)
* **Reporting**: ExtentReports 5 + Microsoft Teams Adaptive Cards + GitHub Pages
* **Parallel Execution**: ThreadLocal WebDriver for isolated thread safety

---
## 📁 Project Structure

petclinic-ui-tests/
├── .github/
│   └── workflows/
│       └── test.yml          # GitHub Actions CI/CD configuration
├── src/
│   ├── main/java/com/adesso/qa/
│   │   ├── driver/           # ThreadLocal Driver Manager setup
│   │   ├── listeners/        # Extent & JUnit TestWatcher Listener
│   │   └── pages/            # Page Object Model classes
│   └── test/java/com/adesso/qa/
│       └── tests/            # Automated test suite classes
├── target/test-report/       # Extent HTML report output directory
├── pom.xml                   # Maven dependencies and Surefire setup
└── README.md                 # Framework documentation

---

## 💻 Running Tests Locally

### Prerequisites
* **Java**: JDK 17+ installed and configured (`JAVA_HOME`).
* **Maven**: 3.8+ installed.
* **Browsers**: Google Chrome or Mozilla Firefox installed locally.

### Terminal Commands

* **Run default test suite (Chrome, Headful)**:
  ```bash
  mvn clean test
  ```

* **Run tests in Headless mode**:
  ```bash
  mvn clean test -Dheadless=true
  ```

* **Run tests on Firefox**:
  ```bash
  mvn clean test -Dbrowser=FIREFOX
  ```

* **Run a single test class**:
  ```bash
  mvn test -Dtest=NavigationTest
  ```

> **Note**: Local test runs host the HTML report locally at `http://localhost:8001/test-report.html` and send execution metrics to Teams via your local listener (if not running in CI).

---
## 📊 Reporting & Test Artifacts

### ExtentSparkReport (`target/test-report/test-report.html`)
The framework utilizes **ExtentReports** to generate an interactive HTML report after every test execution:

* **Automated Screenshots**: Automatically captures and embeds Base64 screenshots inside the report when a test step fails.
* **Execution Metrics**: Tracks total run duration, environment metadata (browser, headless mode), and individual test statuses (Passed, Failed, Skipped).
* **Cross-Environment Access**:
  * **Local Runs**: Served locally via an embedded HTTP server at `http://localhost:8001/test-report.html`.
  * **CI/CD Runs**: Published automatically to GitHub Pages at `https://svitlanashe.github.io/petclinic-ui-tests/test-report.html`.

---

## ⚙️ Environment Variables & System Properties

You can customize test execution at runtime by passing Maven flags (`-Dvariable=value`):

| Property | Default Value | Options | Description |
| :--- | :--- | :--- | :--- |
| `browser` | `CHROME` | `CHROME`, `FIREFOX` | Target browser engine to execute tests. |
| `headless` | `false` | `true`, `false` | Runs browser in headless background mode. |
| `reportUrl` | Local/ngrok URL | Any valid URL | Overrides report link inside generated Extent Report. |

---

## 🚀 CI/CD Pipeline Configuration (GitHub Actions)

### File Location
The pipeline file is located at:
```text
.github/workflows/test.yml
```

### What the Workflow Does
1. **Triggering**: Runs automatically on every `push` or `pull_request` targetting the `main` branch.
2. **Environment Setup**: Provisions an Ubuntu Linux runner, installs Java 17 (Temurin), and sets up Maven dependencies with local caching.
3. **Execution**: Runs tests headlessly (`-Dheadless=true`) in Chrome.
4. **Environment Awareness**: GitHub Actions automatically sets `CI=true` and `GITHUB_ACTIONS=true`. Your `TestListener` recognizes this and skips local server initialization and duplicate local webhooks.
5. **Report Deployment**: Generates the Extent HTML report and deploys it directly to **GitHub Pages** (`gh-pages` branch).
6. **Notification**: Posts a clean, interactive **Adaptive Card** to Microsoft Teams containing direct status updates and a clickable link to the live GitHub Pages report.

### Full Pipeline Workflow YAML (`.github/workflows/test.yml`)

```yaml
name: UI Automation Pipeline

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Run Headless UI Tests
        run: mvn clean test -Dheadless=true -Dbrowser=CHROME
        continue-on-error: true

      - name: Deploy Report to GitHub Pages
        if: always()
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./target/test-report
          destination_dir: .

      - name: Send Microsoft Teams Notification
        if: always()
        env:
          TEAMS_WEBHOOK_URL: ${{ secrets.TEAMS_WEBHOOK_URL }}
        run: |
          STATUS="${{ job.status }}"
          
          curl -H "Content-Type: application/json" -d "{
            "type": "message",
            "attachments": [
              {
                "contentType": "application/vnd.microsoft.card.adaptive",
                "contentUrl": null,
                "content": {
                  "\$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
                  "type": "AdaptiveCard",
                  "version": "1.4",
                  "body": [
                    {
                      "type": "TextBlock",
                      "text": "UI Automation Pipeline Finished",
                      "weight": "Bolder",
                      "size": "Medium"
                    },
                    {
                      "type": "FactSet",
                      "facts": [
                        { "title": "Status:", "value": "$STATUS" },
                        { "title": "Repository:", "value": "${{ github.repository }}" },
                        { "title": "Branch:", "value": "${{ github.ref_name }}" }
                      ]
                    },
                    {
                      "type": "TextBlock",
                      "text": "[👉 Click Here to Open HTML Test Report](https://svitlanashe.github.io/petclinic-ui-tests/test-report.html)",
                      "wrap": true
                    }
                  ]
                }
              }
            ]
          }" $TEAMS_WEBHOOK_URL
```
---

### 🚨 Troubleshooting Common Issues

Include solutions for frequent local or CI execution hurdles so developers can self-serve when issues arise:

## 🚨 Troubleshooting Common Issues

* **Element Not Interactable / Small Viewport (Headless Mode)**:
  * *Cause*: Linux headless runners defaulting to mobile dimensions (hiding header menu items under the hamburger icon).
  * *Fix*: Ensure Chrome CDP emulation or explicit window sizing (`--window-size=1920,1080`) is enabled in `DriverManager.java`.

* **Local Port 8001 Already in Use**:
  * *Cause*: A previous test run left the local report HTTP server running.
  * *Fix*: Terminate the process bound to port `8001` or wait 30 seconds for the listener timeout to release the socket.

* **Teams Webhook Fails on Local Runs**:
  * *Cause*: Missing or invalid `TEAMS_WEBHOOK_URL` environment variable.
  * *Fix*: Webhooks execute automatically in CI via GitHub Secrets. For local runs, pass `-DreportUrl=YOUR_URL` or verify system environment variables.
---

## 🔮 Future Improvements & Expansion Roadmap

### 1. Architectural Improvements
* **Page Object Model Refactoring**: Abstract reusable UI components (like header bars or search tables) into dedicated component classes rather than binding them directly inside page objects.
* **Data-Driven Testing**: Implement JUnit 5 `@ParameterizedTest` with `@CsvSource` or JSON data providers to test boundary values (e.g., edge-case owner names, invalid phone numbers, empty fields).
* **API Pre-conditions**: Integrate REST-Assured to set up test data (creating owners/pets via REST API) before running UI validation steps, accelerating execution speed.

### 2. Test Suite Expansion
* **Negative & Form Validation Scenarios**: Add dedicated tests for empty form submissions, special character injection, invalid phone/date formats, and field-level error message assertions.
* **Visual Regression Testing**: Integrate tools like Applitools or Percy to catch CSS/layout shifts across different screen resolutions automatically.
* **Cross-Browser Matrix Expansion**: Extend WebDriver setup to support Microsoft Edge, Safari (macOS), and cloud execution grids (SauceLabs, BrowserStack, or Selenium Grid).
* **Performance / Lighthouse Audits**: Add basic page load timing assertions or Google Lighthouse integration to monitor frontend performance during UI runs.
