package team.chisel.ctm.client.util;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.Profiler;

public class ProfileUtil {
	private static ThreadLocal<Profiler> profiler = ThreadLocal.withInitial(() -> {
		if (Thread.currentThread().getId() == 1) {
			return MinecraftClient.getInstance().getProfiler();
		} else {
			return DummyProfiler.INSTANCE;
		}
	});

	public static void push(@NotNull String section) {
		profiler.get().push(section);
	}

	public static void pop() {
		profiler.get().pop();
	}

	public static void swap(@NotNull String section) {
		profiler.get().swap(section);
	}
}
