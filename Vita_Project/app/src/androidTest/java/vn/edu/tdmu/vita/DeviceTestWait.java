package vn.edu.tdmu.vita;

import android.os.SystemClock;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

final class DeviceTestWait {
    private DeviceTestWait() {}

    static void until(String message, BooleanSupplier condition) {
        long deadline = SystemClock.uptimeMillis() + 15000;
        AtomicBoolean ready = new AtomicBoolean();
        do {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> ready.set(condition.getAsBoolean()));
            if (ready.get()) return;
            SystemClock.sleep(25);
        } while (SystemClock.uptimeMillis() < deadline);
        throw new AssertionError(message);
    }
}
