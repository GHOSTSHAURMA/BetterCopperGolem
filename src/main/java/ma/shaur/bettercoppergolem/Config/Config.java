package ma.shaur.bettercoppergolem.Config;

@SuppressWarnings("unused")
public class Config 
{
	private final String _ignoreColor = "Completely ignore color of shulker boxes and bundles when sorting";
	private final String _ignoreColorTrue = "true - will not consider color of the shulkers when placing it in a chest";
	private final String _ignoreColorFalse = "flase - all dyed shulkers will be sorted by color only, the non-dyed ones will still be sorted by content";
	public boolean ignoreColor = false;

	private final String _allowIndividualItemsMatchContainerContents = "Allow matching items with inventories of containers within chests";
	private final String _allowIndividualItemsMatchContainerContentsTrue = "true - will consider container contents within chests when checking if the item belongs in a chest";
	private final String _allowIndividualItemsMatchContainerContentsFalse = "flase - will ignore container contents";
	public boolean allowIndividualItemsMatchContainerContents = false;

	private final String _allowInsertingItemsIntoContainers = "Allow inserting items into containers within chests ";
	private final String _allowInsertingItemsIntoContainersTrue = "true - will insert matched items direcrly into container";
	private final String _allowInsertingItemsIntoContainersFalse = "flase - will not insert items into containers";
	public boolean allowInsertingItemsIntoContainers = false;

	private String _maxChestCheckCount = "Maximum ammount of chests golem will check before waiting";
	public int maxChestCheckCount = 10;

	private String _maxHeldItemStackSize = "Maximum ammount of items golem can hold at one time";
	public int maxHeldItemStackSize = 16;
	
	Config() {}
}
