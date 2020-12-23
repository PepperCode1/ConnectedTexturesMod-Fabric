package team.chisel.ctm.client.util;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.Profiler;

public class ProfileUtil {
	private static final ThreadLocal<Profiler> PROFILER = ThreadLocal.withInitial(() -> {
		if (Thread.currentThread().getId() == 1) {
			return MinecraftClient.getInstance().getProfiler();
		} else {
			return DummyProfiler.INSTANCE;
		}
	});

	public static void push(@NotNull String section) {
		PROFILER.get().push(section);
	}

	public static void pop() {
		PROFILER.get().pop();
	}

	public static void swap(@NotNull String section) {
		PROFILER.get().swap(section);
	}
}
