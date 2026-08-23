package com.adesso.qa.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adesso.qa.driver.DriverManager;
import com.adesso.qa.listeners.RetryExtension;
import com.adesso.qa.listeners.TestListener;

@ExtendWith({TestListener.class, RetryExtension.class})
public abstract class BaseTest {

    protected static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected WebDriver driver;

    @BeforeEach
    public void setUp() {
        log.info("Starting test execution on thread [{}]", Thread.currentThread().getName());
        driver = DriverManager.getDriver();
    }

}