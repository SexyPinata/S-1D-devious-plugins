package net.unethicalite.motherlodemine.data;
import lombok.Value;

@Value
public class Activity
{

    public static final Activity IDLE = new Activity("Idle");
    public static final Activity BANKING = new Activity("Banking");
    public static final Activity DEPOSITING = new Activity("Depositing");
    public static final Activity WITHDRAWING = new Activity("Withdrawing");
    public static final Activity ATTACKING = new Activity("Attacking");
    public static final Activity MINING = new Activity("Mining");
    public static final Activity REPAIRING = new Activity("Repairing");
    // AFK Activity
    public static final Activity AFK = new Activity("AFK");
    // Assisted Mining Activity
    public static final Activity ASSISTED_MINING = new Activity("Assisted Mining");

    String name;
}