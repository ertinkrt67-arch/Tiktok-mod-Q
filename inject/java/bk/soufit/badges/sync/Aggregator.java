package bk.soufit.badges.sync;

import android.content.Context;
import bk.soufit.badges.model.Badge;
import bk.soufit.badges.render.IconCache;
import bk.soufit.badges.source.CreatorBadgeProvider;
import bk.soufit.badges.source.SourceAdapter;
import bk.soufit.badges.source.SourceOneAdapter;
import bk.soufit.badges.source.SourceSnapshot;
import bk.soufit.badges.source.SourceThreeAdapter;
import bk.soufit.badges.source.SourceTwoAdapter;
import bk.soufit.badges.store.BadgeRepository;
import bk.soufit.badges.store.Merger;
import bk.soufit.core.Diary;
import bk.soufit.core.Net;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.json.JSONObject;

public final class Aggregator {
    private static final long BUDGET_MS = 12000L;
    private static final ExecutorService POOL = Executors.newFixedThreadPool(3, new ThreadFactory() {
        @Override public Thread newThread(Runnable work) {
            Thread thread = new Thread(work, "soufit-badge-sync");
            thread.setDaemon(true);
            return thread;
        }
    });

    private final Context context;
    private final List<SourceAdapter> adapters;
    private final Map<Badge.Origin, SourceSnapshot> lastGood = new ConcurrentHashMap<>();
    private final Map<Badge.Origin, String> tags = new ConcurrentHashMap<>();

    public Aggregator(Context context, List<SourceAdapter> adapters) {
        this.context = context.getApplicationContext();
        this.adapters = new ArrayList<>(adapters);
    }

    public static Aggregator standard(Context context) {
        return new Aggregator(context, Arrays.<SourceAdapter>asList(
                new SourceOneAdapter(), new SourceTwoAdapter(), new SourceThreeAdapter()));
    }

    public synchronized void warm() {
        for (SourceAdapter adapter : adapters) {
            String name = nameOf(adapter.origin());
            byte[] cached = BadgeRepository.readCache(context, name);
            if (cached == null) continue;
            try {
                lastGood.put(adapter.origin(), adapter.adapt(new JSONObject(new String(cached, "UTF-8"))));
            } catch (Throwable error) {
                Diary.fail("badges cache " + name, error);
            }
        }
        publish();
    }

    public synchronized boolean refresh() {
        Map<Badge.Origin, Future<Boolean>> pending = new LinkedHashMap<>();
        for (final SourceAdapter adapter : adapters) {
            if (adapter.endpoint() == null || adapter.endpoint().isEmpty()) continue;
            pending.put(adapter.origin(), POOL.submit(new Callable<Boolean>() {
                @Override public Boolean call() {
                    return fetch(adapter);
                }
            }));
        }

        boolean reached = false;
        long deadline = System.currentTimeMillis() + BUDGET_MS;
        for (Map.Entry<Badge.Origin, Future<Boolean>> entry : pending.entrySet()) {
            try {
                long left = Math.max(1L, deadline - System.currentTimeMillis());
                if (entry.getValue().get(left, TimeUnit.MILLISECONDS)) reached = true;
            } catch (TimeoutException slow) {
                entry.getValue().cancel(true);
                Diary.note("badges: " + nameOf(entry.getKey()) + " timed out, using its last good copy");
            } catch (Exception failed) {
                Diary.fail("badges " + nameOf(entry.getKey()), failed);
            }
        }
        publish();
        return reached;
    }

    private Boolean fetch(SourceAdapter adapter) {
        Badge.Origin origin = adapter.origin();
        String name = nameOf(origin);
        try {
            Net.Answer answer = Net.fetch(adapter.endpoint(), tags.get(origin));
            if (answer == null) return false;
            if (answer.unchanged || answer.body == null) return true;
            SourceSnapshot parsed = adapter.adapt(new JSONObject(new String(answer.body, "UTF-8")));
            lastGood.put(origin, parsed);
            if (answer.tag != null) tags.put(origin, answer.tag);
            BadgeRepository.writeCache(context, name, answer.body);
            return true;
        } catch (Throwable error) {
            Diary.fail("badges " + name, error);
            return false;
        }
    }

    private void publish() {
        List<SourceSnapshot> sources = new ArrayList<>();
        sources.add(CreatorBadgeProvider.snapshot());
        for (SourceAdapter adapter : adapters) {
            SourceSnapshot known = lastGood.get(adapter.origin());
            if (known != null) sources.add(known);
        }
        BadgeRepository.get().publish(Merger.merge(sources));
        IconCache.prefetch(context, BadgeRepository.get().all());
    }

    private static String nameOf(Badge.Origin origin) {
        switch (origin) {
            case SOURCE_ONE: return "one";
            case SOURCE_TWO: return "two";
            case SOURCE_THREE: return "three";
            default: return "creator";
        }
    }
}
