package team.chisel.ctm.client.config;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

import team.chisel.ctm.client.CTMClient;

// Using old API for backwards compatibility
@SuppressWarnings("deprecation")
public class ModMenuApiImpl implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (parent) -> new CTMConfigScreen(parent, CTMClient.getConfigManager());
	}
}
