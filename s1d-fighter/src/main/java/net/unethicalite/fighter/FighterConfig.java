package net.unethicalite.fighter;

import net.runelite.client.config.*;

@ConfigGroup("hootfighter")
public interface FighterConfig extends Config
{
	@ConfigSection(
			name = "General",
			description = "General settings",
			position = 990,
			closedByDefault = true
	)
	String general = "General";

	@ConfigSection(
			name = "Health",
			description = "General settings",
			position = 991,
			closedByDefault = true
	)
	String health = "Health";

	//Att section for Banking
	@ConfigSection(
			name = "Banking",
			description = "Banking settings",
			position = 992,
			closedByDefault = true
	)
	String banking = "Banking";
	@ConfigSection(
			name = "Loot",
			description = "Loot settings",
			position = 993,
			closedByDefault = true
	)
	String loot = "Loot";

	@ConfigSection(
			name = "Prayers",
			description = "Prayers settings",
			position = 994,
			closedByDefault = true
	)
	String prayers = "Prayers";

	@ConfigSection(
			name = "Alching",
			description = "Alching settings",
			position = 995,
			closedByDefault = true
	)
	String alching = "Alching";

	@ConfigSection(
			name = "Antipoison",
			description = "Antipoison settings",
			position = 996,
			closedByDefault = true
	)
	String antipoison = "Antipoison";

	@ConfigSection(
			name = "Slayer",
			description = "Slayer settings",
			position = 997,
			closedByDefault = true
	)
	String slayer = "Slayer";

	@ConfigSection(
			name = "Antifire",
			description = "Automatically uses antifire",
			position = 998,
			closedByDefault = true
	)
	String antifire = "Antifire";

	@ConfigSection(
			name = "Debug",
			description = "Debugging settings",
			position = 999,
			closedByDefault = true
	)
	String debug = "Debug";

	// Add start/stop button
	@ConfigItem(
			keyName = "startButton",
			name = "Start/Stop",
			description = "Test button that changes variable value",
			position = 350
	)
	default Button startButton() {
		return new Button();
	}

	@ConfigItem(
			keyName = "monster",
			name = "Monster",
			description = "Monster(s) to kill",
			position = 0,
			section = general
	)
	default String monster()
	{
		return "Chicken";
	}

	//insert Menu
	@ConfigItem(
			keyName = "instertMenu",
			name = "Enable menu option",
			description = "Insert menu option to attack monster",
			position = 0,
			section = general
	)
	default boolean insertMenu()
	{
		return true;
	}

	@Range(max = 100)
	@ConfigItem(
			keyName = "attackRange",
			name = "Attack range",
			description = "Monster attack range",
			position = 1,
			section = general
	)
	default int attackRange()
	{
		return 10;
	}

	@ConfigItem(
			keyName = "centerTile",
			name = "Center tile",
			description = "",
			position = 2,
			section = general
	)
	default String centerTile()
	{
		return "0 0 0";
	}

	@ConfigItem(
			keyName = "bury",
			name = "Bury bones",
			description = "Bury bones",
			position = 3,
			section = general
	)
	default boolean buryBones()
	{
		return true;
	}

	@ConfigItem(
			keyName = "bank",
			name = "Bank",
			description = "If enabled, will bank items when inventory is full. If disabled, will just stop looting",
			position = 0,
			section = banking
	)
	default boolean bank()
	{
		return true;
	}

	//Minimum free inventory slots to bank
	@Range(max = 28)
	@ConfigItem(
			keyName = "minFreeSlots",
			name = "Min. free slots",
			description = "Minimum free inventory slots to bank",
			position = 1,
			section = banking
	)
	default int minFreeSlots()
	{
		return 5;
	}

	// checkbox to ignore stamina potions when banking
	@ConfigItem(
			keyName = "ignoreStamina",
			name = "Ignore stamina potions",
			description = "Ignore stamina potions when banking",
			position = 2,
			section = banking
	)
	default boolean ignoreStamina()
	{
		return true;
	}
	// checkbox to ignore food when banking
	@ConfigItem(
			keyName = "ignoreFood",
			name = "Ignore food",
			description = "Ignore food when banking",
			position = 3,
			section = banking
	)
	default boolean ignoreFood()
	{
		return true;
	}
	// checkbox to ignore restore potions when banking
	@ConfigItem(
			keyName = "ignoreRestore",
			name = "Ignore restore potions",
			description = "Ignore restore potions when banking",
			position = 4,
			section = banking
	)
	default boolean ignoreRestore()
	{
		return true;
	}
	// checkbox to ignore prayer potions when banking
	@ConfigItem(
			keyName = "ignorePrayer",
			name = "Ignore prayer potions",
			description = "Ignore prayer potions when banking",
			position = 5,
			section = banking
	)
	default boolean ignorePrayer()
	{
		return true;
	}
	// checkbox to ignore antipoison potions when banking
	@ConfigItem(
			keyName = "ignoreAntipoison",
			name = "Ignore antipoison potions",
			description = "Ignore antipoison potions when banking",
			position = 6,
			section = banking
	)
	default boolean ignoreAntipoison()
	{
		return true;
	}
	// checkbox to ignore antifire potions when banking
	@ConfigItem(
			keyName = "ignoreAntifire",
			name = "Ignore antifire potions",
			description = "Ignore antifire potions when banking",
			position = 7,
			section = banking
	)
	default boolean ignoreAntifire()
	{
		return true;
	}
	// checkbox to ignore combat potions when banking
	@ConfigItem(
			keyName = "ignoreCombat",
			name = "Ignore combat potions",
			description = "Ignore combat potions when banking",
			position = 8,
			section = banking
	)
	default boolean ignoreCombat()
	{
		return true;
	}
	// checkbox to ignore teleportation items when banking
	@ConfigItem(
			keyName = "ignoreTeleport",
			name = "Ignore teleportation items",
			description = "Ignore teleportation items when banking",
			position = 9,
			section = banking
	)
	default boolean ignoreTeleport()
	{
		return true;
	}
	// checkbox to ignore


	@ConfigItem(
			keyName = "lootOnlyMode",
			name = "Loot only mode",
			description = "Enable loot only mode",
			position = 0,
			section = loot
	)
	default boolean lootOnlyMode()
	{
		return false;
	}

	@ConfigItem(
			keyName = "loots",
			name = "Loot Items",
			description = "Items to loot separated by comma. ex: Lobster,Tuna",
			position = 0,
			section = loot
	)
	default String loots()
	{
		return "Bones";
	}

	@ConfigItem(
			keyName = "dontLoot",
			name = "Don't loot",
			description = "Items to not loot separated by comma. ex: Lobster,Tuna",
			position = 0,
			section = loot
	)
	default String dontLoot()
	{
		return "Bones";
	}

	@ConfigItem(
			keyName = "lootByValue",
			name = "Loot items by value",
			description = "",
			position = 1,
			section = loot
	)
	default boolean lootByValue()
	{
		return true;
	}

	@ConfigItem(
			keyName = "lootValue",
			name = "Loot GP value",
			description = "Min. value for item to loot",
			position = 1,
			section = loot,
			hidden = true,
			unhide = "lootByValue"
	)
	default int lootValue()
	{
		return 0;
	}

	@ConfigItem(
			keyName = "untradables",
			name = "Loot untradables",
			description = "Loot untradables",
			position = 2,
			section = loot
	)
	default boolean untradables()
	{
		return true;
	}

	@ConfigItem(
			keyName = "eat",
			name = "Eat food",
			description = "Eat food to heal",
			position = 0,
			section = health
	)
	default boolean eat()
	{
		return true;
	}

	@Range(max = 100)
	@ConfigItem(
			keyName = "eatHealthPercent",
			name = "Health %",
			description = "Health % to eat at",
			position = 1,
			section = health
	)
	default int healthPercent()
	{
		return 65;
	}

	@ConfigItem(
			keyName = "foods",
			name = "Food",
			description = "Food to eat, separated by comma. ex: Bones,Coins",
			position = 0,
			section = health
	)
	default String foods()
	{
		return "Any";
	}

	@ConfigItem(
			keyName = "quickPrayer",
			name = "Use Quick Prayers",
			description = "Use Quick Prayers",
			position = 0,
			section = prayers
	)
	default boolean quickPrayer()
	{
		return false;
	}

	@ConfigItem(
			keyName = "flick",
			name = "Flick",
			description = "One ticks quick prayers",
			position = 1,
			section = prayers
	)
	default boolean flick()
	{
		return false;
	}

	@ConfigItem(
			keyName = "restore",
			name = "Restore prayer",
			description = "Drinks pots to restore prayer points",
			position = 2,
			section = prayers
	)
	default boolean restore()
	{
		return false;
	}

	@ConfigItem(
			keyName = "alch",
			name = "Alch items",
			description = "Alchs items",
			position = 0,
			section = alching
	)
	default boolean alching()
	{
		return false;
	}

	@ConfigItem(
			keyName = "alchSpell",
			name = "Alch spell",
			description = "Alch spell",
			position = 1,
			section = alching
	)
	default AlchSpell alchSpell()
	{
		return AlchSpell.HIGH;
	}

	@ConfigItem(
			keyName = "alchItems",
			name = "Alch items",
			description = "Items to alch, separated by comma. ex: Maple shortbow,Rune scimitar",
			position = 2,
			section = alching
	)
	default String alchItems()
	{
		return "Weed";
	}

	@ConfigItem(
			keyName = "antipoison",
			name = "Use antipoison",
			description = "Automatically cure antipoison",
			position = 0,
			section = antipoison
	)
	default boolean antipoison()
	{
		return false;
	}

	@ConfigItem(
			keyName = "antipoisonType",
			name = "Antipoison type",
			description = "Type of antipoison potion to drink when poisoned",
			position = 1,
			section = antipoison
	)
	default AntipoisonType antipoisonType()
	{
		return AntipoisonType.ANTIPOISON;
	}

	@ConfigItem(
			keyName = "disableOnTaskCompletion",
			name = "Disable after task",
			description = "Disables plugin once slayer task is finished, so you don't continue attacking monster",
			position = 0,
			section = slayer
	)
	default boolean disableAfterSlayerTask()
	{
		return false;
	}
	
	@ConfigItem(
			keyName = "antifire",
			name = "Use antifire",
			description = "Automatically sips antifire",
			position = 0,
			section = antifire
	)
	default boolean antifire()
	{
		return false;
	}

	@ConfigItem(
			keyName = "antifireType",
			name = "Antifire type",
			description = "Type of antifire potion to drink",
			position = 1,
			section = antifire
	)
	default AntifireType antifireType()
	{
		return AntifireType.ANTIFIRE;
	}

	@ConfigItem(
			keyName = "drawRadius",
			name = "Draw attack area",
			description = "",
			position = 0,
			section = debug
	)
	default boolean drawRadius()
	{
		return false;
	}

	@ConfigItem(
			keyName = "drawCenter",
			name = "Draw center tile",
			description = "",
			position = 1,
			section = debug
	)
	default boolean drawCenter()
	{
		return false;
	}
}
