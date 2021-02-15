package team.chisel.ctm.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import team.chisel.ctm.client.CTMClient;

public class ModMenuApiImpl implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (parent) -> new CTMConfigScreen(parent, CTMClient.getConfigManager());
	}
}
