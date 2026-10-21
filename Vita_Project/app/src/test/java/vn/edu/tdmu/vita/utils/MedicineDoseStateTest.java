package vn.edu.tdmu.vita.utils;

import org.junit.Test;
import java.text.SimpleDateFormat;
import java.util.Locale;
import static org.junit.Assert.*;

public class MedicineDoseStateTest {
    @Test public void unconfirmedDoseBecomesDueWithoutBecomingSkipped() throws Exception {
        long now = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse("2026-10-19 08:00").getTime();
        assertEquals(MedicineDoseState.UNCONFIRMED, MedicineDoseState.classify("2026-10-19", "07:00", null, now));
        assertEquals(MedicineDoseState.UNCONFIRMED, MedicineDoseState.classify("2026-10-19", "08:00", null, now));
        assertEquals(MedicineDoseState.UPCOMING, MedicineDoseState.classify("2026-10-19", "09:00", null, now));
        assertEquals(MedicineDoseState.UPCOMING, MedicineDoseState.classify("2026-10-20", "07:00", null, now));
    }
    @Test public void explicitStatusTakesPriorityOverScheduledTime() throws Exception {
        long now = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse("2026-10-19 08:00").getTime();
        assertEquals(MedicineDoseState.TAKEN, MedicineDoseState.classify("2026-10-19", "09:00", "TAKEN", now));
        assertEquals(MedicineDoseState.SKIPPED, MedicineDoseState.classify("2026-10-19", "07:00", "SKIPPED", now));
    }
}
