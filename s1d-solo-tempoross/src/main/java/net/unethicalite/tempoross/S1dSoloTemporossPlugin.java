package net.unethicalite.tempoross;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.unethicalite.tempoross.TemporossID.*;

@Extension
@PluginDescriptor(
		name = "<html>[<font color=#18a9d9>S-1D</font>] Solo Tempoross  <font color=#18a9d9 size=\"5\">  \uD83D\uDC0B</html>",
		enabledByDefault = false
)
@Slf4j
public class S1dSoloTemporossPlugin extends LoopedPlugin
{

	@Inject
	private Client client;

	@Inject
	private S1dSoloTemporossConfig config;

	@Inject
	private S1dUtils utils;

	@Inject
	private ConfigManager configManager;
	private int waves = 0;
	private TemporossWorkArea workArea = null;

	private int clickDelay = 0;

	private static final Pattern DIGIT_PATTERN = Pattern.compile("(\\d+)");

	@Override
	protected int loop()
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return 600;
		}
		clickDelay = utils.calculateClickDelay(config.clickDelayMin(), config.clickDelayMax(), config.targetDelay(), config.deviation());
		int animation = player.getAnimation();
		if (!client.isInInstancedRegion())
		{
			waves = 0;
			workArea = null;
			incomingWave = false;
			scriptState = State.INITIAL_CATCH;

			if (player.isMoving() || player.isAnimating())
			{
				return -5;
			}

			TileObject startLadder = TileObjects.getFirstAt(3135, 2840, 0, OBJECT_LOBBY_LADDER);
			if (startLadder == null)
			{
				return -1;
			}

			// If east of ladder, we're not in the room.
			if (player.getWorldLocation().getX() > startLadder.getWorldLocation().getX())
			{
				startLadder.interact("Quick-climb");
				return -6;
			}

			int emptyBuckets = Inventory.getCount(ITEM_EMPTY_BUCKET);
			TileObject waterPump = TileObjects.getFirstAt(3135, 2832, 0, OBJECT_LOBBY_PUMP);
			if (waterPump != null && emptyBuckets > 0)
			{
				waterPump.interact("Use");
				return -6;
			}

			return -1;
		}

		if (workArea == null)
		{
			NPC npc = NPCs.getNearest(x -> x.hasAction("Forfeit"));
			NPC ammoCrate = NPCs.getNearest(x -> x.hasAction("Fill") && x.hasAction("Check-ammo"));

			if (npc == null || ammoCrate == null)
			{
				return -1;
			}

			boolean isWest = npc.getWorldLocation().getX() < ammoCrate.getWorldLocation().getX();
			TemporossWorkArea area = new TemporossWorkArea(npc.getWorldLocation(), isWest);
			log.info("Found work area: {}", area);
			workArea = area;
			return -1;
		}

		NPC leave = NPCs.getNearest(x -> x.hasAction("Leave"));
		if (leave != null)
		{
			leave.interact("Leave");
			return -6;
		}

		if (getPhase() >= 2 )
		{
			return -2;
		}
		HarpoonType harpoonType = config.harpoonType();
		boolean hasHarpoon = Equipment.contains(harpoonType.getId()) || Inventory.contains(harpoonType.getId());
		if (!hasHarpoon)
		{
			//if we don't have a harpoon, set to the default harpoon that we can pick up
			harpoonType = HarpoonType.HARPOON;
			//set the config to the default harpoon
			configManager.setConfiguration("s1dsolotempoross", "harpoonType", HarpoonType.HARPOON);
			int harpoonCount = Inventory.getCount(harpoonType.getId());
			if (harpoonCount != 1)
			{
				if (player.isMoving() || animation == ANIMATION_INTERACTING)
				{
					return -2;
				}

				if (harpoonCount > 1)
				{
					Inventory.getFirst(harpoonType.getId()).interact("Drop");
					return -3;
				}

				workArea.getHarpoonCrate().interact("Take");
				return -2;
			}
		}

		int bucketCount = Inventory.getCount(ITEM_EMPTY_BUCKET, ITEM_WATER_BUCKET);
		int bucketGoal = config.buckets();
		if (bucketCount != bucketGoal)
		{
			if (player.isMoving() || animation == ANIMATION_INTERACTING)
			{
				return -2;
			}

			if (bucketCount > bucketGoal)
			{
				Inventory.getFirst(ITEM_EMPTY_BUCKET).interact("Drop");
				return -3;
			}

			workArea.getBucketCrate().interact("Take");
			return -2;
		}

		int ropeCount = Inventory.getCount(ITEM_ROPE);
		boolean shouldBringRope = config.rope();
		boolean hasSpiritAngler = config.hasSpiritAngler();
		if (ropeCount != 1 && shouldBringRope && !hasSpiritAngler)
		{
			if (player.isMoving() || animation == ANIMATION_INTERACTING)
			{
				return -2;
			}

			if (ropeCount > 1)
			{
				Inventory.getFirst(ITEM_ROPE).interact("Drop");
				return -3;
			}

			workArea.getRopeCrate().interact("Take");
			return -2;
		}

		int hammerCount = Inventory.getCount(ITEM_HAMMER);
		boolean shouldBringHammer = config.hammer();
		if (hammerCount != 1 && shouldBringHammer)
		{
			if (player.isMoving() || animation == ANIMATION_INTERACTING)
			{
				return -2;
			}

			if (hammerCount > 1)
			{
				Inventory.getFirst(ITEM_HAMMER).interact("Drop");
				return -3;
			}


			workArea.getHammerCrate().interact("Take");
			return -2;
		}

		/**
		 * Is in game
		 */
		Widget energyWidget = Widgets.get(437, 35);
		Widget essenceWidget = Widgets.get(437, 45);
		Widget intensityWidget = Widgets.get(437, 55);
		if (!Widgets.isVisible(energyWidget) || !Widgets.isVisible(essenceWidget) || !Widgets.isVisible(intensityWidget))
		{
			return 1000;
		}

		Matcher energyMatcher = DIGIT_PATTERN.matcher(energyWidget.getText());
		Matcher essenceMatcher = DIGIT_PATTERN.matcher(essenceWidget.getText());
		Matcher intensityMatcher = DIGIT_PATTERN.matcher(intensityWidget.getText());
		if (!energyMatcher.find() || !essenceMatcher.find() || !intensityMatcher.find())
		{
			return 1000;
		}

		ENERGY = Integer.parseInt(energyMatcher.group(0));
		ESSENCE = Integer.parseInt(essenceMatcher.group(0));
		INTENSITY = Integer.parseInt(intensityMatcher.group(0));

		configManager.setConfiguration("s1dsolotempoross", "energy", ENERGY);
		configManager.setConfiguration("s1dsolotempoross", "essence", ESSENCE);
		configManager.setConfiguration("s1dsolotempoross", "intensity", INTENSITY);
		/**
		 * Danger tasks
		 */

		List<NPC> fires = NPCs.getAll(x -> x.getId() == NPC_FIRE);
		NPC fire = fires.stream()
				.min(Comparator.comparing(x -> x.getWorldLocation().distanceTo(player.getWorldLocation())))
				.orElse(null);
		if (fire != null && fire.getWorldLocation().distanceToPath(client, player.getWorldLocation()) <= 10)
		{
			if (fire.equals(player.getInteracting()))
			{
				return clickDelay;
			}
			fire.interact("Douse");
			return clickDelay;
		}


		TileObject damagedMast = TileObjects.getFirstAt(Tiles.getAt(workArea.getMastPoint()), OBJECT_DAMAGED_MAST);
		if (damagedMast != null && damagedMast.getWorldLocation().distanceToPath(client, player.getWorldLocation()) < 15)
		{
			if (damagedMast.equals(player.getInteracting()))
			{
				return clickDelay;
			}
			damagedMast.interact("Repair");
			return clickDelay;
		}

		TileObject tether = workArea.getClosestTether();
		if (incomingWave)
		{
			if (!isTethered() && tether != null && !Dialog.isOpen())
			{
				if (tether == null)
				{
					log.warn("Can't find tether object");
					return -1;
				}
				if (tether.equals(player.getInteracting()))
				{
					return clickDelay;
				}
				tether.interact("Tether");
				return clickDelay;
			}

			return -2;
		}

		if (tether != null && Players.getLocal().getGraphic() == GRAPHIC_TETHERED)
		{
			tether.interact("Untether");
			return -2;
		}

		NPC exitNpc = NPCs.getNearest(NPC_EXIT);
		if (exitNpc != null)
		{
			exitNpc.interact("Leave");
			return 10000;
		}

		NPC doubleSpot = NPCs.getNearest(NPC_DOUBLE_FISH_SPOT);
		if (scriptState == State.INITIAL_COOK && doubleSpot != null)
		{
			scriptState = scriptState.next;
		}

		if (INTENSITY >= 94 && scriptState == State.THIRD_COOK)
		{
			forfeitMatch();
			return clickDelay;
		}

		if (scriptState == null)
		{
			scriptState = State.THIRD_CATCH;
		}

		if (scriptState.isComplete.getAsBoolean())
		{
			scriptState = scriptState.next;
			if (scriptState == null)
			{
				scriptState = State.THIRD_CATCH;
			}
		}

		NPC temporossPool = NPCs.getNearest(NPC_VULN_WHIRLPOOL);
		if (temporossPool != null && scriptState != State.SECOND_FILL && scriptState != State.ATTACK_TEMPOROSS)
		{
			scriptState = State.ATTACK_TEMPOROSS;
		}
		if (temporossPool != null && scriptState == State.ATTACK_TEMPOROSS && ENERGY >= 95)
		{
			scriptState = null;
		}

		int rawFishCount = Inventory.getCount(ITEM_RAW_FISH);

		// Filter out dangerous NPCs
		final Predicate<NPC> filterDangerousNPCs = (NPC npc) -> !inCloud(npc.getWorldLocation(), 1);
		log.info("State: " + scriptState);
		/**
		 * Gather tasks
		 */
		switch (scriptState)
		{
			case INITIAL_CATCH:
			case SECOND_CATCH:
			case THIRD_CATCH:
				if (inCloud(player.getWorldLocation(), 10))
				{
					Movement.walkNextTo(getClosestCloudOrFire(player.getWorldLocation(), 10));
					return clickDelay;
				}
				NPC fishSpot = NPCs.getNearest(it ->
						NPC_DOUBLE_FISH_SPOT == it.getId()
								&& it.getWorldLocation().distanceTo(workArea.getRangePoint()) <= 20
								&& filterDangerousNPCs.test(it));

				if (fishSpot == null)
				{
					fishSpot = NPCs.getNearest(it ->
							Set.of(NPC_SINGLE_FISH_SPOT, NPC_SINGLE_FISH_SPOT_SECOND).contains(it.getId())
									&& it.getWorldLocation().distanceTo(workArea.getRangePoint()) <= 20
									&& filterDangerousNPCs.test(it));
				}

				if (fishSpot != null)
				{
					if (fishSpot.equals(player.getInteracting()) && !Dialog.isOpen())
					{
						return clickDelay;
					}

					fishSpot.interact("Harpoon");
					return clickDelay;
				}
				else
				{
					// if fish are null walk to the totem pole since it's in the center of the fish spots.
					Movement.walkTo(workArea.getTotemPoint());
					return clickDelay;
				}

			case INITIAL_COOK:
			case SECOND_COOK:
			case THIRD_COOK:
				TileObject range = workArea.getRange();
				if (range != null && rawFishCount > 0)
				{
					if ((player.getAnimation() == ANIMATION_COOK || player.isMoving()) && !Dialog.isOpen() && !range.equals(player.getInteracting()))
					{
						return clickDelay;
					}

					range.interact("Cook-at");
					return clickDelay;
				}
				else if (range == null)
				{
					Movement.walkTo(workArea.getRangePoint());
					return clickDelay;
				}

			case EMERGENCY_FILL:
			case SECOND_FILL:
			case INITIAL_FILL:
				if (inCloud(player.getWorldLocation(), 1))
				{
					// If in cloud, select the ammo crate that is furthest from the player
					List<NPC> ammoCrates = NPCs.getAll(x -> x.hasAction("Fill")
							&& x.getWorldLocation().distanceTo(workArea.getSafePoint()) <= 10
							&& x.hasAction("Check-ammo"));
					NPC ammoCrate = ammoCrates.stream()
							.max(Comparator.comparing(x -> x.getWorldLocation().distanceTo(player.getWorldLocation())))
							.orElse(null);
					if (ammoCrate != null)
					{
						Movement.walk(ammoCrate.getWorldLocation());
						return clickDelay;
					}
				}

				NPC ammoCrate = NPCs.getNearest(x -> x.hasAction("Fill")
						&& x.getWorldLocation().distanceTo(workArea.getSafePoint()) <= 10
						&& x.hasAction("Check-ammo")
						&& filterDangerousNPCs.test(x));


				if (ammoCrate != null && (!ammoCrate.equals(player.getInteracting()) || Dialog.isOpen()))
				{
					ammoCrate.interact("Fill");
					return -2;
				}
				else if (ammoCrate == null)
				{
					log.warn("Can't find the ammo crate");

					walkToSafePoint();
				}
				break;

			case ATTACK_TEMPOROSS:
				if (temporossPool != null && (!temporossPool.equals(player.getInteracting()) || Dialog.isOpen()))
				{
					temporossPool.interact("Harpoon");
					return 5000;
				}
				else if (temporossPool == null)
				{

					if (ENERGY > 0)
					{
						scriptState = null;
						return -1;
					}

					walkToSafePoint();
				}

				break;
		}

		return -1;
	}

	enum State
	{
		ATTACK_TEMPOROSS(() -> ENERGY >= 95,  null),
		SECOND_FILL(() -> getCookedFish() == 0, ATTACK_TEMPOROSS),
		THIRD_COOK(() -> getCookedFish() == 19 || INTENSITY >= 92, SECOND_FILL),
		THIRD_CATCH(() -> getAllFish() >= 19, THIRD_COOK),
		EMERGENCY_FILL(() -> getAllFish() == 0, THIRD_CATCH),
		INITIAL_FILL(() -> getCookedFish() == 0, THIRD_CATCH),
		SECOND_COOK(() -> getCookedFish() == 17, INITIAL_FILL),
		SECOND_CATCH(() -> getAllFish() >= 17, SECOND_COOK),
		INITIAL_COOK(() -> getRawFish() == 0, SECOND_CATCH),
		INITIAL_CATCH(() -> getRawFish() >= 7 || getAllFish() >= 10, INITIAL_COOK);

		@Getter
		private final BooleanSupplier isComplete;

		@Getter
		private final State next;

		State(BooleanSupplier isComplete, State next)
		{
			this.isComplete = isComplete;
			this.next = next;
		}
	}

	private State scriptState = State.INITIAL_CATCH;

	@Override
	protected void startUp() throws Exception
	{
		super.startUp();
		scriptState = State.INITIAL_CATCH;
	}

	private boolean incomingWave = false;

	private static int ENERGY = 100;
	private static int ESSENCE = 100;
	private static int INTENSITY = 0;

	private void forfeitMatch()
	{
		NPC npc = NPCs.getNearest(x -> x.hasAction("Forfeit"));
		if (npc != null)
		{
			npc.interact("Forfeit");
		}
	}

	private void walkToSafePoint()
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		WorldPoint safePoint = workArea.getSafePoint();
		if (!player.isMoving())
		{
			Movement.walk(safePoint);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		ChatMessageType type = event.getType();
		String message = event.getMessage();

		if (type == ChatMessageType.GAMEMESSAGE)
		{
			if (message.equals("<col=d30b0b>A colossal wave closes in...</col>"))
			{
				waves++;
				incomingWave = true;
			}

			if (message.contains("the rope keeps you securely") || message.contains("the wave slams into you"))
			{
				incomingWave = false;
			}
		}
	}

	protected boolean inCloud(WorldPoint point, int radius)
	{
		return TileObjects.getFirstSurrounding(point, radius, OBJECT_CLOUD_SHADOW, OBJECT_FIRE) != null;
	}

	// get closest cloud or fire object
	protected TileObject getClosestCloudOrFire(WorldPoint point, int radius)
	{
		return TileObjects.getFirstSurrounding(point, radius, OBJECT_CLOUD_SHADOW, OBJECT_FIRE);
	}

	private static boolean isTethered()
	{
		int graphic = Players.getLocal().getGraphic();
		int anim = Players.getLocal().getAnimation();
		return anim != 832 && (graphic == GRAPHIC_TETHERED || graphic == GRAPHIC_TETHERING);
	}

	private static int getRawFish()
	{
		return Inventory.getCount(ITEM_RAW_FISH);
	}

	private static int getCookedFish()
	{
		return Inventory.getCount(ITEM_COOKED_FISH);
	}

	private static int getAllFish()
	{
		return getRawFish() + getCookedFish();
	}

	private int getPhase()
	{
		return 1 + (waves / 3); // every 3 waves, phase increases by 1
	}

	@Provides
	public S1dSoloTemporossConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(S1dSoloTemporossConfig.class);
	}
}