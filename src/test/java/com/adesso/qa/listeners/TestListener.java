package com.adesso.qa.listeners;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.adesso.qa.driver.DriverManager;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class TestListener implements TestWatcher, BeforeAllCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback, ExtensionContext.Store.CloseableResource {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThreadLocal = new ThreadLocal<>();
    private static final AtomicInteger skippedCount = new AtomicInteger(0);
    private static Instant startTime;
    private static boolean registered = false;

    private static final String TEAMS_WEBHOOK_URL = "https://defaultce570dca850b4b9bb0554dff932712.20.environment.api.powerplatform.com:443/powerautomate/automations/direct/cu/29/workflows/a9ce6a3baffd4b059de3f7c5ebb53278/triggers/manual/paths/invoke?api-version=1&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=9hxGYL03LsdpQrzYCZ89TYvh21twh1vH3DTZo8AJ4ls";
    private static HttpServer server;
    private static final int PORT = 8001;

    // Maps to manage unique runs and final outcomes
    private static final Map<String, ExtentTest> testMap = new ConcurrentHashMap<>();
    private static final Map<String, Status> finalTestStatuses = new ConcurrentHashMap<>();
    private static final java.util.List<String> skippedTestReasons = new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * Helper to check if tests are running inside CI/CD (GitHub Actions, Jenkins, etc.)
     */
    private boolean isCiEnvironment() {
        return System.getenv("GITHUB_ACTIONS") != null || System.getenv("CI") != null;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!registered) {
            registered = true;
            startTime = Instant.now();
            context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put("suite_close_resource", this);

            ExtentSparkReporter spark = new ExtentSparkReporter("target/test-report/test-report.html");
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setReportName("Automated Frontend Test Report");
            spark.config().setDocumentTitle("Test Execution Report");

            extent = new ExtentReports();
            extent.attachReporter(spark);

            String browser = System.getProperty("browser", "CHROME").toUpperCase();
            String reportUrl = System.getProperty("reportUrl", "https://motto-reseal-pug.ngrok-free.dev/test-report.html");

            extent.setSystemInfo("Browser", browser);
            extent.setSystemInfo("Headless", System.getProperty("headless", "false"));
            extent.setSystemInfo("Report Link", "<a href='" + reportUrl + "' target='_blank'>" + reportUrl + "</a>");
        }
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        getOrCreateTest(context);
    }

    private ExtentTest getOrCreateTest(ExtensionContext context) {
        String testName = getTestName(context);

        if (testThreadLocal.get() == null) {
            String browser = System.getProperty("browser", "CHROME").toUpperCase();

            if (testMap.containsKey(testName)) {
                extent.removeTest(testMap.get(testName));
            }

            ExtentTest test = extent.createTest(testName);
            test.assignCategory(browser);
            test.info("Executing test on browser: <b>" + browser + "</b>");

            testMap.put(testName, test);
            testThreadLocal.set(test);
        }
        return testThreadLocal.get();
    }

    @Override
    public void testDisabled(ExtensionContext context, java.util.Optional<String> reason) {
        skippedCount.incrementAndGet();

        String disableReason = reason.orElse("No explicit @Disabled reason provided");
        String testName = getTestName(context);
        String browser = System.getProperty("browser", "CHROME").toUpperCase();

        if (testMap.containsKey(testName)) {
            extent.removeTest(testMap.get(testName));
            testMap.remove(testName);
        }

        ExtentTest test = extent.createTest(testName);
        test.assignCategory(browser);
        test.log(Status.SKIP, "Test Skipped: " + disableReason);

        skippedTestReasons.add(context.getRequiredTestMethod().getName() + ": " + disableReason);
        System.out.println("[SKIPPED TEST] " + testName + " | Reason: " + disableReason);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        String testName = getTestName(context);
        finalTestStatuses.put(testName, Status.PASS);

        ExtentTest test = getOrCreateTest(context);
        test.log(Status.PASS, "Test Passed Successfully");
        DriverManager.quitDriver();
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        String testName = getTestName(context);
        finalTestStatuses.put(testName, Status.FAIL);

        ExtentTest test = getOrCreateTest(context);
        test.log(Status.FAIL, "Test Failed: " + cause.getMessage());
        test.fail(cause);

        try {
            String base64Screenshot = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BASE64);
            test.fail("Failure Screenshot", MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
        } catch (Exception e) {
            test.log(Status.WARNING, "Failed to capture screenshot: " + e.getMessage());
        } finally {
            DriverManager.quitDriver();
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        testThreadLocal.remove();
    }

    @Override
    public void close() {
        if (extent != null) {
            extent.flush();

            // ONLY run local server and local Teams notification if NOT running on CI/CD
            if (!isCiEnvironment()) {
                startLocalReportServer();
                sendTeamsNotification();

                try {
                    System.out.println("[REPORT SERVER] Keeping local server alive for 0.5 minutes...");
                    Thread.sleep(30_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                System.out.println("[CI ENVIRONMENT DETECTED] Skipping local Teams notification and local HTTP server.");
            }
        }
    }

    private static void startLocalReportServer() {
        try {
            if (server != null) return;

            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/test-report.html", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) {
                    try {
                        File file = new File("target/test-report/test-report.html");
                        if (!file.exists()) {
                            String response = "Report file not found.";
                            exchange.sendResponseHeaders(404, response.length());
                            OutputStream os = exchange.getResponseBody();
                            os.write(response.getBytes());
                            os.close();
                            return;
                        }

                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                        exchange.getResponseHeaders().set("ngrok-skip-browser-warning", "true");

                        exchange.sendResponseHeaders(200, file.length());

                        OutputStream os = exchange.getResponseBody();
                        FileInputStream fs = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int count;
                        while ((count = fs.read(buffer)) >= 0) {
                            os.write(buffer, 0, count);
                        }
                        fs.close();
                        os.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            server.setExecutor(null);
            server.start();
            System.out.println("[REPORT SERVER] Hosted at http://localhost:" + PORT + "/test-report.html");
        } catch (Exception e) {
            System.err.println("[REPORT SERVER] Could not start server: " + e.getMessage());
        }
    }

    private void sendTeamsNotification() {
        if (TEAMS_WEBHOOK_URL == null || TEAMS_WEBHOOK_URL.isEmpty()) return;

        Instant endTime = Instant.now();
        long durationSeconds = Duration.between(startTime, endTime).getSeconds();
        long minutes = durationSeconds / 60;
        long seconds = durationSeconds % 60;
        String timeFormatted = String.format("%d:%02d minutes", minutes, seconds);

        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(endTime);

        long passed = finalTestStatuses.values().stream().filter(s -> s == Status.PASS).count();
        long failed = finalTestStatuses.values().stream().filter(s -> s == Status.FAIL).count();
        long skipped = skippedCount.get();

        boolean isSuccess = failed == 0;
        String iconUrl = isSuccess
                ? "https://img.icons8.com/emoji/48/check-mark-emoji.png"
                : "https://img.icons8.com/emoji/48/cross-mark-emoji.png";

        String title = "Frontend Test Run Report - " + timestamp;
        String reportUrl = System.getProperty("reportUrl", "https://motto-reseal-pug.ngrok-free.dev/test-report.html");

        String jsonPayload = """
                {
                   "type": "message",
                   "attachments": [
                     {
                       "contentType": "application/vnd.microsoft.card.adaptive",
                       "content": {
                         "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
                         "type": "AdaptiveCard",
                         "version": "1.4",
                         "body": [
                           {
                             "type": "ColumnSet",
                             "columns": [
                               {
                                 "type": "Column",
                                 "width": "auto",
                                 "items": [
                                   {
                                     "type": "Image",
                                     "url": "%s",
                                     "size": "Medium"
                                   }
                                 ]
                               },
                               {
                                 "type": "Column",
                                 "width": "stretch",
                                 "items": [
                                   {
                                     "type": "TextBlock",
                                     "text": "%s",
                                     "weight": "Bolder",
                                     "size": "Medium"
                                   },
                                   {
                                     "type": "TextBlock",
                                     "text": "executed on local in %s",
                                     "isSubtle": true,
                                     "spacing": "None"
                                   },
                                   {
                                     "type": "FactSet",
                                     "facts": [
                                       { "title": "Passed", "value": "%d" },
                                       { "title": "Failed", "value": "%d" },
                                       { "title": "Skipped", "value": "%d" }
                                     ]
                                   }
                                 ]
                               }
                             ]
                           }
                         ],
                         "actions": [
                           {
                             "type": "Action.OpenUrl",
                             "title": "Build Report",
                             "url": "%s"
                           }
                         ]
                       }
                     }
                   ]
                }
                """.formatted(
                iconUrl,
                title,
                timeFormatted,
                passed,
                failed,
                skipped,
                reportUrl
        );

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TEAMS_WEBHOOK_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[TEAMS NOTIFICATION] Status: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("Failed to send Teams notification: " + e.getMessage());
        }
    }

    private String getTestName(ExtensionContext context) {
        return context.getRequiredTestClass().getSimpleName() + " :: " + context.getRequiredTestMethod().getName() + "()";
    }
}