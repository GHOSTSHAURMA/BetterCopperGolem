package ma.shaur.bettercoppergolem.config;

@SuppressWarnings("unused")
public class Config 
{
	public final String _shulkerAndBundleSorting = "Enable soring shulker boxes and bundles based on their content and color";
	public boolean shulkerAndBundleSorting = true;
	
	public final String _ignoreColor = "Completely ignore color of shulker boxes and bundles when sorting";
	public final String _ignoreColorTrue = "Enabled - will not consider color of the shulkers when placing them into a chest";
	public final String _ignoreColorFalse = "Disabled - all dyed shulkers will be sorted by color only, the non-dyed ones will still be sorted by content";
	public boolean ignoreColor = false;

	public final String _allowIndividualItemsMatchContainerContents = "Allow matching items with inventories of containers within chests";
	public final String _allowIndividualItemsMatchContainerContentsTrue = "Enabled - will consider container contents within chests when checking if the item belongs in a chest";
	public final String _allowIndividualItemsMatchContainerContentsFalse = "Disabled - will ignore container contents";
	public boolean allowIndividualItemsMatchContainerContents = false;

	public final String _allowInsertingItemsIntoContainers = "Allow inserting items into containers within chests ";
	public final String _allowInsertingItemsIntoContainersTrue = "Enabled - will insert matched items direcrly into container";
	public final String _allowInsertingItemsIntoContainersFalse = "Disabled - will not insert items into containers";
	public boolean allowInsertingItemsIntoContainers = false;

	public String _maxChestCheckCount = "Maximum ammount of chests golem will check before waiting";
	public int maxChestCheckCount = 10;

	public String _maxHeldItemStackSize = "Maximum ammount of items golem can hold at one time";
	public int maxHeldItemStackSize = 16;
	
	Config() {}
}
