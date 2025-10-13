package ma.shaur.bettercoppergolem.config.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.google.gson.JsonIOException;

import ma.shaur.bettercoppergolem.BetterCopperGolemClient;
import ma.shaur.bettercoppergolem.config.Config;
import ma.shaur.bettercoppergolem.config.ConfigHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.TextWidget.TextOverflow;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.option.SimpleOption.TooltipFactory;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class ConfigScreen extends GameOptionsScreen 
{
	private static final Predicate<String> ONLY_INTEGER_NUMBERS_PREDICATE = (s) ->
	{
		try
		{
			Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			return false;
		}
		
		return true;
	};

	@SuppressWarnings("resource")
	public ConfigScreen(Screen parent) 
	{
		super(parent, MinecraftClient.getInstance().options, Text.translatable("bettercoppergolem.options"));
	}

	@Override
	protected void addOptions() 
	{
		if(body == null) return;
		
		Config config = ConfigHandler.getConfig();
		body.addAll(SimpleOption.ofBoolean(translationKey("shulker_and_bundle_sorting"), tooltipFactory("shulker_and_bundle_sorting"), config.shulkerAndBundleSorting, b -> config.shulkerAndBundleSorting = b),
					SimpleOption.ofBoolean(translationKey("ignore_color"), tooltipFactory("ignore_color"), config.ignoreColor, b -> config.ignoreColor = b),
					SimpleOption.ofBoolean(translationKey("allow_individual_items_match_container_contents"), tooltipFactory("allow_individual_items_match_container_contents"), config.allowIndividualItemsMatchContainerContents, b -> config.allowIndividualItemsMatchContainerContents = b),
					SimpleOption.ofBoolean(translationKey("allow_inserting_items_into_containers"), tooltipFactory("allow_inserting_items_into_containers"), config.allowInsertingItemsIntoContainers, b -> config.allowInsertingItemsIntoContainers = b));

		body.addWidgetEntry(intContainer(Text.translatable(translationKey("max_chest_check_count")),Text.translatable(translationKey("max_chest_check_count.info")), config.maxChestCheckCount, i -> config.maxChestCheckCount = i),
							intContainer(Text.translatable(translationKey("max_held_item_stack_size")), Text.translatable(translationKey("max_held_item_stack_size.info")), config.maxHeldItemStackSize, i -> config.maxHeldItemStackSize = i));
		body.addWidgetEntry(intContainer(Text.translatable(translationKey("cooldown_time")),Text.translatable(translationKey("cooldown_time.info")), config.cooldownTime, i -> config.cooldownTime = i), null);
	}
	
	private Container intContainer(Text text, Text tooltip, int value, ChangeListener listener)
	{
		TextWidget textWidget = new TextWidget(100, 20, text, textRenderer);
		textWidget.setMaxWidth(0, TextOverflow.SCROLLING);
		
		TextFieldWidget field = new TextFieldWidget(textRenderer, 50, 20, ScreenTexts.EMPTY);
		field.setText(Integer.toString(value));
		field.setCentered(true);
		field.setTextPredicate(ONLY_INTEGER_NUMBERS_PREDICATE);
		field.setChangedListener(s -> 
		{
			try
			{
				listener.changed(Integer.parseInt(s));
			}
			catch (NumberFormatException e) {}
		});

		Container layout = new Container(150, 20, List.of(textWidget, field));
		layout.setTooltip(Tooltip.of(tooltip));
		
		return layout;
	}

	@Override
	public void removed() 
	{
		try
		{
			ConfigHandler.saveConfig();
		} 
		catch (JsonIOException | IOException e)
		{
			BetterCopperGolemClient.LOGGER.error("Error saving config from a config screen: ", e);
		}
	}
	
	private static TooltipFactory<Boolean> tooltipFactory(String id) 
	{
		id = translationKey(id);
		MutableText text = Text.translatableWithFallback(id + ".info", "");
		Text infoTrue = Text.translatableWithFallback(id + ".info.true", "");
		Text infoFalse = Text.translatableWithFallback(id + ".info.false", "");

		if(!infoTrue.getString().isEmpty()) text.append("\n").append(infoTrue);
		if(!infoFalse.getString().isEmpty()) text.append("\n").append(infoFalse);
		
		return (b) -> text.getString().isEmpty() ? null : Tooltip.of(text);
	}

	private static String translationKey(String name)
	{
		return "option.bettercoppergolem." + name;
	}

	private class Container extends ContainerWidget 
	{
		private List<ClickableWidget> children = new ArrayList<>();

	    public Container(int width, int height, List<ClickableWidget> children) 
		{
			super(0, 0, width, height, ScreenTexts.EMPTY);
			this.children = children;
		}
	    
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) 
		{
			context.enableScissor(getX(), getY(), getX() + width, getY() + height);

			int x = getX(), y = getY();
			for(ClickableWidget widget : children) 
			{
				widget.setPosition(x, y);
				widget.render(context, mouseX, mouseY, deltaTicks);
				x += widget.getWidth();
			}

			context.disableScissor();
		}

		@Override
		public List<? extends Element> children() 
		{
			return children;
		}

		@Override
		protected int getContentsHeightWithPadding() 
		{
			return getHeight();
		}

		@Override
		protected double getDeltaYPerScroll() 
		{
			return 0;
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) { }
		
		@Override
		public void setFocused(boolean focused) 
		{
			super.setFocused(focused);
			if(!focused) for(ClickableWidget widget : children) widget.setFocused(false);
		}
	}
	
	private interface ChangeListener
	{
		void changed(int i);
	}
}
