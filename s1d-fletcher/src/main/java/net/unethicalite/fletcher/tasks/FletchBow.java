package net.unethicalite.fletcher.tasks;

import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.fletcher.S1dFletcherPlugin;
import net.unethicalite.fletcher.data.Activity;
import net.unethicalite.fletcher.data.Material;
import net.unethicalite.fletcher.data.Mode;

import java.util.Optional;

public class FletchBow extends FletcherTask {
    public FletchBow(S1dFletcherPlugin context) {
        super(context);
    }

    @Override
    public boolean validate() {
        return this.isCurrentActivity(Activity.IDLE) && this.hasMaterial() && (this.isMode(Mode.FLETCHING_LONGBOW) || this.isMode(Mode.FLETCHING_SHORTBOW) && this.hasKnife());
    }

    @Override
    public int execute() {
        Material material = this.getMaterial();
        Item knife = Inventory.getFirst(ItemID.KNIFE);
        Item logItem = Inventory.getFirst(material.getLogID());
        if (this.isMode(Mode.FLETCHING_LONGBOW)) {
            this.setActivity(Activity.FLETCHING_LONGBOW);

            knife.useOn(logItem);
            Time.sleepTicksUntil(this::isFletchingWidgetOpen, 30);
            Time.sleepTick();
            Time.sleep(this.calculateClickDelay());
            // select the longbow option in the fletching widget
            Widget fletchingWidget = this.getClient().getWidget(270, 16);
            fletchingWidget.interact("Make");

            // wait until hasMaterial returns false
            Time.sleepTicksUntil(() -> !this.hasMaterial(), 80);
            this.setTaskCooldown(Optional.of(Activity.IDLE));


        } else if (this.isMode(Mode.FLETCHING_SHORTBOW)) {
            this.setActivity(Activity.FLETCHING_SHORTBOW);

            knife.useOn(logItem);
            Time.sleepTicksUntil(this::isFletchingWidgetOpen, 30);
            Time.sleepTick();
            Time.sleep(this.calculateClickDelay());
            // select the shortbow option in the fletching widget
            Widget fletchingWidget = this.getClient().getWidget(270, 15);
            fletchingWidget.interact("Make");

            // wait until hasMaterial returns false
            Time.sleepTicksUntil(() -> !this.hasMaterial(), 80);
            this.setTaskCooldown(Optional.of(Activity.IDLE));
        }


        return 0;
    }
}
