package ma.shaur.bettercoppergolem.config;

import net.minecraft.text.Text;

@SuppressWarnings("unused")
public class Config 
{
	private final String _shulkerAndBundleSorting = Text.translatable("option.bettercoppergolem.shulker_and_bundle_sorting.info").getString();
	public boolean shulkerAndBundleSorting = true;
	
	private final String _ignoreColor = Text.translatable("option.bettercoppergolem.ignore_color.info").getString();
	private final String _ignoreColorTrue = Text.translatable("option.bettercoppergolem.ignore_color.info.true").getString();
	private final String _ignoreColorFalse = Text.translatable("option.bettercoppergolem.ignore_color.info.false").getString();
	public boolean ignoreColor = false;

	private final String _allowIndividualItemsMatchContainerContents = Text.translatable("option.bettercoppergolem.allow_individual_items_match_container_contents.info").getString();
	private final String _allowIndividualItemsMatchContainerContentsTrue = Text.translatable("option.bettercoppergolem.allow_individual_items_match_container_contents.info.true").getString();
	private final String _allowIndividualItemsMatchContainerContentsFalse = Text.translatable("option.bettercoppergolem.allow_individual_items_match_container_contents.info.false").getString();
	public boolean allowIndividualItemsMatchContainerContents = false;

	private final String _allowInsertingItemsIntoContainers =  Text.translatable("option.bettercoppergolem.allow_inserting_items_into_containers.info").getString();
	private final String _allowInsertingItemsIntoContainersTrue = Text.translatable("option.bettercoppergolem.allow_inserting_items_into_containers.info.true").getString();
	private final String _allowInsertingItemsIntoContainersFalse = Text.translatable("option.bettercoppergolem.allow_inserting_items_into_containers.info.false").getString();
	public boolean allowInsertingItemsIntoContainers = false;

	public String _maxChestCheckCount = Text.translatable("option.bettercoppergolem.max_chest_check_count.info").getString();
	public int maxChestCheckCount = 10;

	public String _maxHeldItemStackSize = Text.translatable("option.bettercoppergolem.max_held_item_stack_size.info").getString();
	public int maxHeldItemStackSize = 16;
	
	Config() {}
}
