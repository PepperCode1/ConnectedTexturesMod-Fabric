package team.chisel.ctm.test;

import net.fabricmc.api.ModInitializer;

import team.chisel.ctm.client.CTMClient;

public class CTMTest implements ModInitializer {
	public static final String MOD_ID = "ctm_testmod";

	@Override
	public void onInitialize() {
		CTMClient.LOGGER.info("Hello from CTM testmod! If this message is displayed, the testmod is working.");

		//Block block = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "test"), new Block(AbstractBlock.Settings.of(Material.STONE)));
		//Item item = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "test"), new BlockItem(block, new Item.Settings()));
	}
}
