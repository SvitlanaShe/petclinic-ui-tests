package com.adesso.qa.listeners;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import java.lang.reflect.InvocationTargetException;

public class RetryExtension implements TestExecutionExceptionHandler {

    private static final int MAX_RETRIES = 2; // Set your max retry limit here

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        // Unwrap InvocationTargetException to get the real cause
        Throwable cause = (throwable instanceof InvocationTargetException)
                ? throwable.getCause()
                : throwable;

        ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
        int count = store.getOrDefault("retryCount", Integer.class, 0);

        if (count < MAX_RETRIES) {
            store.put("retryCount", count + 1);
            System.out.println("[RETRYING TEST] Attempt " + (count + 1) + " for " + context.getRequiredTestMethod().getName());

            try {
                // Re-run the test method instance
                context.getRequiredTestMethod().invoke(context.getRequiredTestInstance());
            } catch (Throwable retryThrowable) {
                // Recursively handle if it fails during retry
                handleTestExecutionException(context, retryThrowable);
            }
        } else {
            // Rethrow the actual root cause on final failure
            throw cause;
        }
    }
}