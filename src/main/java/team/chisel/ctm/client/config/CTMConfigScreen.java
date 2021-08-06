package team.chisel.ctm.client.config;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class CTMConfigScreen extends Screen {
	private Screen parent;
	private ConfigManager configManager;

	public CTMConfigScreen(Screen parent, ConfigManager configManager) {
		super(new TranslatableText("screen.ctm.config.title"));
		this.parent = parent;
		this.configManager = configManager;
	}

	@Override
	protected void init() {
		addButton(new ButtonWidget(width / 2 - 90 - 75, height / 2 - 10, 150, 20, getBooleanOptionText("options.ctm.disable_ctm", configManager.getConfig().disableCTM),
				(button) -> {
					boolean value = !configManager.getConfig().disableCTM;
					button.setMessage(getBooleanOptionText("options.ctm.disable_ctm", value));
					configManager.getConfig().disableCTM = value;
				},
				(button, matrices, mouseX, mouseY) -> {
					renderWrappedTooltip(matrices, new TranslatableText("options.ctm.disable_ctm.tooltip"), mouseX, mouseY);
				}
		));

		addButton(new ButtonWidget(width / 2 + 90 - 75, height / 2 - 10, 150, 20, getBooleanOptionText("options.ctm.connect_inside_ctm", configManager.getConfig().connectInsideCTM),
				(button) -> {
					boolean value = !configManager.getConfig().connectInsideCTM;
					button.setMessage(getBooleanOptionText("options.ctm.connect_inside_ctm", value));
					configManager.getConfig().connectInsideCTM = value;
				},
				(button, matrices, mouseX, mouseY) -> {
					renderWrappedTooltip(matrices, new TranslatableText("options.ctm.connect_inside_ctm.tooltip"), mouseX, mouseY);
				}
		));

		addButton(new ButtonWidget(width / 2 - 100, (int) (height * 0.8F), 200, 20, ScreenTexts.DONE, (button) -> onClose()));
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		drawCenteredText(matrices, textRenderer, title, width / 2, (int) (height * 0.15F), 0xFFFFFF);
		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public void onClose() {
		client.openScreen(parent);
	}

	@Override
	public void removed() {
		configManager.onConfigChange();
	}

	private List<OrderedText> wrapText(StringVisitable text, int mouseX, int padding) {
		return textRenderer.wrapLines(text, width - mouseX - 12 - padding);
	}

	private void renderWrappedTooltip(MatrixStack matrices, StringVisitable text, int x, int y) {
		renderOrderedTooltip(matrices, wrapText(text, x, 8), x, y);
	}

	private static Text getBooleanOptionText(String key, boolean value) {
		return new TranslatableText(key, ScreenTexts.getToggleText(value));
	}
}
