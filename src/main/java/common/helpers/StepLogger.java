package common.helpers;

import io.qameta.allure.Allure;

public class StepLogger {

    @FunctionalInterface
    public interface ThrowableRunnable<T> {
        T run() throws Throwable;
    }

    @FunctionalInterface
    public interface ThrowableVoidRunnable<T> {
        void run() throws Throwable;
    }

    public static <T> T log(String title, ThrowableRunnable<T> runnable) {
        return Allure.step(title, () -> runnable.run());
    }

    public static void log(String title, ThrowableVoidRunnable runnable) {
        Allure.step(title, () -> {
            runnable.run();
            return null;
        });
    }
    public static <T> T logUi(final String title, final ThrowableRunnable<T> runnable) {
        return Allure.step(title, () -> {
            try {
                T result = runnable.run();
                AllureAttachments.attachScreenshot("Screenshot: " + title);
                return result;
            } catch (Throwable throwable) {
                AllureAttachments.attachScreenshot("Screenshot on failure: " + title);
                throw throwable;
            }
        });
    }

    public static void logUi(final String title, final ThrowableVoidRunnable runnable) {
        Allure.step(title, () -> {
            try {
                runnable.run();
                AllureAttachments.attachScreenshot("Screenshot: " + title);
            } catch (Throwable throwable) {
                AllureAttachments.attachScreenshot("Screenshot on failure: " + title);
                throw throwable;
            }
            return null;
        });
    }
}

