package net.unethicalite.motherlodemine;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.plugins.Task;
import net.unethicalite.api.plugins.TaskPlugin;
import net.unethicalite.motherlodemine.data.Activity;
import net.unethicalite.motherlodemine.tasks.*;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Random;
import java.util.Set;


@Extension
@PluginDescriptor(
        name = "<html>[<font color=#8f6b32>\uD83D\uDC24</font>] Motherlode Mine",
        enabledByDefault = false,
        tags = {"mining", "motherlode", "mlm", "motherlode mine", "s1d"}
)
@Slf4j
public class S1dMotherlodeMinePlugin extends TaskPlugin
{
    private static final Set<Integer> MOTHERLODE_MAP_REGIONS =
            ImmutableSet.of(14679, 14680, 14681, 14935, 14936, 14937, 15191, 15192, 15193);
    private static final int SACK_LARGE_SIZE = 162;
    private static final int SACK_SIZE = 81;
    private static final int UPPER_FLOOR_HEIGHT = -490;
    private static final int MAX_INVENTORY_SIZE = 28;

    @Inject
    private Config config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private S1dMlmOverlay s1dMlmOverlay;
    public int curSackSize;
    public int maxSackSize;
    public double nonPayDirtItems;
    public TileObject oreVein;
    @Getter
    @Setter
    public boolean sackFull;
    @Inject
    @Getter
    private Client client;

    @Getter
    @Setter
    private int lastGemBagEmpty;


    private Activity currentActivity;
    private Activity previousActivity;
    @Setter
    protected int taskCooldown;
    private boolean startedScript;

    // Assisted mining
    @Getter
    @Setter
    private boolean assistedMining;

    @Getter
    @Setter
    private boolean upstairs;

    public String getActivity()
    {
        if (isCurrentActivity(Activity.AFK))
        {
            return Activity.AFK.getName() + " (" + taskCooldown + ")";
        }
        return currentActivity.getName();
    }

    public void setActivity(Activity activity)
    {
        if (activity != Activity.IDLE)
        {
            log.info("Switching to this activity: " + activity.getName());
        }
        if (activity == Activity.IDLE && currentActivity != Activity.IDLE)
        {
            previousActivity = currentActivity;
        }
        log.info("previous activity: " + previousActivity);
        currentActivity = activity;

    }

    public boolean upStairs()
    {
        return upstairs;
    }

    public void setOreVein(TileObject oreVein)
    {
        this.oreVein = oreVein;
    }

    public final boolean isCurrentActivity(Activity activity)
    {
        return currentActivity == activity;
    }

    public final boolean wasPreviousActivity(Activity activity)
    {
        return previousActivity == activity;
    }

    // getter setter for mining area
    @Getter
    @Setter
    private MiningArea miningArea;


    @Provides
    public Config getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(Config.class);
    }

    private final Task[] tasks =
            {
                new Deposit(this),
                new startTaskPlugin(this),
                new Mine(this),
                new FixWheel(this),
                new GoDown(this),
                new GoUp(this),
                new HandleBank(this),
                new WithdrawSack(this),
                new AssistedMine(this)
    };

    @Override
    public Task[] getTasks()
    {
        return tasks;
    }

    @Override
    protected void startUp()
    {
        this.overlayManager.add(s1dMlmOverlay);
        setAssistedMining(config.assistedMining());
        log.info("Upstairs: " + config.upstairs());
        if (config.upstairs())
        {
            setUpstairs(true);
            miningArea = MiningArea.UPSTAIRS;
            log.info("Mining area set to: " + miningArea);
        }
        else
        {
            miningArea = MiningArea.INSIDE;
            setUpstairs(false);
            log.info("Mining area set to: " + miningArea);
        }

        startedScript = false;
        refreshSackValues();

        setActivity(Activity.IDLE);
        log.info("S1d Motherlode Mine started");
        log.info("Sack size: " + curSackSize + "/" + maxSackSize);
        log.info("Active activity: " + currentActivity.getName());
        if (curSackSize >= maxSackSize)
        {
            sackFull = true;
            setLastGemBagEmpty(0);
        }
    }

    @Override
    protected void shutDown()
    {
        this.overlayManager.remove(s1dMlmOverlay);
    }
    // subscribe to config changed event
    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!event.getGroup().equals("s1dmlm"))
        {
            return;
        }
        setAssistedMining(config.assistedMining());
        if (config.upstairs())
        {
            miningArea = MiningArea.UPSTAIRS;
            setUpstairs(true);
            log.info("Mining area set to: " + miningArea);
        }
        else
        {
            miningArea = MiningArea.INSIDE;
            setUpstairs(false);
            log.info("Mining area set to: " + miningArea);
        }
    }
    @Subscribe
    public void onAnimationChanged(AnimationChanged event)
    {

        Actor actor = event.getActor();
        if (!isRunning() || actor == null || actor != Players.getLocal())
        {
            return;
        }
        switch (Players.getLocal().getAnimation())
        {
            case AnimationID.MINING_MOTHERLODE_BRONZE:
            case AnimationID.MINING_MOTHERLODE_IRON:
            case AnimationID.MINING_MOTHERLODE_STEEL:
            case AnimationID.MINING_MOTHERLODE_BLACK:
            case AnimationID.MINING_MOTHERLODE_MITHRIL:
            case AnimationID.MINING_MOTHERLODE_ADAMANT:
            case AnimationID.MINING_MOTHERLODE_RUNE:
            case AnimationID.MINING_MOTHERLODE_DRAGON:
            case AnimationID.MINING_MOTHERLODE_DRAGON_OR:
            case AnimationID.MINING_MOTHERLODE_DRAGON_UPGRADED:
            case AnimationID.MINING_MOTHERLODE_CRYSTAL:
            case AnimationID.MINING_MOTHERLODE_GILDED:
            case AnimationID.MINING_MOTHERLODE_INFERNAL:
            case AnimationID.MINING_MOTHERLODE_3A:
                if (!isCurrentActivity(Activity.MINING))
                {
                    setActivity(Activity.MINING);
                }
                break;
            default:
        }
    }


    @Subscribe
    public void onWallObjectSpawned(WallObjectSpawned event)
    {
        WallObject wallObject = event.getWallObject();
        if (isCurrentActivity(Activity.MINING) && wallObject.getName().equals("Depleted vein"))
        {
            log.info("depleted vein location: " + wallObject.getWorldLocation());
            if (wallObject.getWorldLocation().equals(oreVein.getWorldLocation()))
            {
                log.info("Vein i was mining turned into a depleted vein");

                setOreVein(null);
                setTaskCooldown();
            }
        }
    }
    @Subscribe
    public void onConfigButtonPressed(ConfigButtonClicked event)
    {
        if (!event.getGroup().contains("s1dmlm") || !event.getKey().toLowerCase().contains("start"))
        {
            return;
        }

        if (startedScript)
        {
            startedScript = false;
            log.info("Script stopped");
        }
        else
        {
            startedScript = true;
            log.info("Script started");
        }
    }
    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event)
    {

        if (isCurrentActivity(Activity.REPAIRING)
                && event.getGameObject().getName().equals("Broken strut"))
        {
            log.info("Strut despawned");
            setTaskCooldown();
        }
    }
    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!startedScript)
        {
            if (isCurrentActivity(Activity.AFK))
                return;
            setActivity(Activity.AFK);
            return;
        }
        if (isRunning() && inMotherlodeMine() && !isAssistedMining())
        {
            if (taskCooldown > 0)
            {
                if (!isCurrentActivity(Activity.AFK))
                {
                    setActivity(Activity.AFK);
                }
                log.info("Task cooldown: " + taskCooldown);
                taskCooldown--;
            }
            else if (isCurrentActivity(Activity.AFK))
            {
                setActivity(Activity.IDLE);
            }
        }
        // if the player has the assisted mining option enabled and is in the motherlode mine area
        if (isRunning() && inMotherlodeMine() && isAssistedMining())
        {
            // check if there is a taskcooldown
            if (taskCooldown > 0)
            {
                // if there is a taskcooldown, set the activity to afk
                if (!isCurrentActivity(Activity.AFK))
                {
                    setActivity(Activity.AFK);
                    previousActivity = Activity.ASSISTED_MINING;
                }
                log.info("Task cooldown: " + taskCooldown);
                taskCooldown--;
            }
            else if (isCurrentActivity(Activity.AFK) && wasPreviousActivity(Activity.ASSISTED_MINING))
            {
                setActivity(Activity.ASSISTED_MINING);
            }
            else if (isCurrentActivity(Activity.AFK))
            {
                setActivity(Activity.IDLE);
            }


        }
    }
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        log.info("Item container changed");
        if (isCurrentActivity(Activity.DEPOSITING))
        {
            if (!Inventory.contains(ItemID.PAYDIRT))
            {
                setActivity(Activity.IDLE);
            }
        }
        else if (isCurrentActivity(Activity.WITHDRAWING))
        {
            if (Inventory.contains(
                    ItemID.RUNITE_ORE,
                    ItemID.ADAMANTITE_ORE,
                    ItemID.MITHRIL_ORE,
                    ItemID.GOLD_ORE,
                    ItemID.COAL,
                    ItemID.UNCUT_SAPPHIRE,
                    ItemID.UNCUT_EMERALD,
                    ItemID.UNCUT_RUBY,
                    ItemID.UNCUT_DIAMOND,
                    ItemID.UNCUT_DRAGONSTONE))
            {
                setActivity(Activity.IDLE);
            }
        }
        else if (isCurrentActivity(Activity.BANKING))
        {
            if (!Inventory.contains(
                    ItemID.RUNITE_ORE,
                    ItemID.ADAMANTITE_ORE,
                    ItemID.MITHRIL_ORE,
                    ItemID.GOLD_ORE,
                    ItemID.COAL,
                    ItemID.UNCUT_SAPPHIRE,
                    ItemID.UNCUT_EMERALD,
                    ItemID.UNCUT_RUBY,
                    ItemID.UNCUT_DIAMOND,
                    ItemID.UNCUT_DRAGONSTONE))
            {
                setActivity(Activity.IDLE);
            }
        }
        else if (isCurrentActivity(Activity.MINING))
        {
            if (Inventory.isFull())
            {
                log.info("Inventory full");
                setActivity(Activity.IDLE);
                if (isAssistedMining())
                {
                    startedScript = false;
                }

            }
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        if (isRunning() && inMotherlodeMine())
        {
            log.info("remaining deposits: " + getRemainingDeposits());
            refreshSackValues();
            log.info("Sack size: " + curSackSize + "/" + maxSackSize);
            if (curSackSize >= maxSackSize)
            {

                sackFull = true;
            }
        }
    }

    //check if the object we are hovering over has the option to mine
    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if (isRunning() && inMotherlodeMine() && config.assistedMining())
        {
            if (event.getType() == MenuAction.GAME_OBJECT_FIRST_OPTION.getId())
            {
                if (event.getOption().equals("Mine"))
                {
                    //replace the default menu entry with the custom one and give it a custom color
                    event.getMenuEntry().setOption("<col=ff0000>Mineer</col>");

                    //event.getMenuEntry().setOption("Mineer");
                    //addMenuEntry(event, "Mineer");
                }
            }
        }
    }

    // subscribe to the menu entry click event
    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (isRunning() && inMotherlodeMine())
        {
            if (event.getMenuOption().contains("Mineer"))
            {
                setActivity(Activity.ASSISTED_MINING);
                //consume click event
                event.consume();
                startedScript = true;
            }
        }
    }

    private void addMenuEntry(MenuEntryAdded event, String option) { //TODO: Update to new menu entry
        client.createMenuEntry(-1).setOption(option)
                .setTarget(event.getTarget())
                .setIdentifier(0)
                .setParam1(0)
                .setParam1(0)
                .setType(MenuAction.RUNELITE);
    }

    // replace the default menu entry with the custom one



    // get random task cooldown
    public void setTaskCooldown()
    {
        taskCooldown = new Random().nextInt(config.taskCooldownMax() - config.taskCooldownMin() + 1)
                + config.taskCooldownMin();
    }
    public boolean isUpperFloor()
    {
        return Perspective.getTileHeight(client, client.getLocalPlayer().getLocalLocation(), 0) < UPPER_FLOOR_HEIGHT;
    }

    // mine rockfall
    public void mineRockfall(final int x, final int y)
    {
        final TileObject rockfall = TileObjects.getFirstAt(x, y, 0,
                ObjectID.ROCKFALL, ObjectID.ROCKFALL_26680, ObjectID.ROCKFALL_28786);

        if (rockfall != null)
        {
            rockfall.interact("Mine");
            Time.sleepTicksUntil(
                    () -> TileObjects.getFirstAt(x, y, 0,
                            ObjectID.ROCKFALL, ObjectID.ROCKFALL_26680, ObjectID.ROCKFALL_28786) == null, 50
            );
        }
    }

    // in motherlode mine
    public boolean inMotherlodeMine()
    {
        return MOTHERLODE_MAP_REGIONS.contains(client.getLocalPlayer().getWorldLocation().getRegionID());
    }


    // function to calculate remaining deposits
    public int getRemainingDeposits()
    {
        double remainingDeposits = 0.0;
        nonPayDirtItems = Inventory.getAll().stream().filter(x -> x.getId() != ItemID.PAYDIRT).mapToDouble(x -> x.getQuantity()).sum();
        if (curSackSize < maxSackSize)
        {
            remainingDeposits = (maxSackSize - curSackSize) / (28.0-nonPayDirtItems);
        }
        // round up to the nearest whole number
        return (int) Math.ceil(remainingDeposits);

    }
    public void refreshSackValues()
    {
        curSackSize = Vars.getBit(Varbits.SACK_NUMBER);
        boolean sackUpgraded = Vars.getBit(Varbits.SACK_UPGRADED) == 1;
        maxSackSize = sackUpgraded ? SACK_LARGE_SIZE : SACK_SIZE;
        if (curSackSize >= maxSackSize)
        {
            sackFull = true;
        }
        if (curSackSize == 0)
        {
            setLastGemBagEmpty(0);
            sackFull = false;
        }
    }

}
