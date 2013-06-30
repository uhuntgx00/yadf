package userinterface.game.graphicobject;

import java.awt.Graphics;

import simulation.item.Item;
import simulation.map.MapArea;
import simulation.map.MapIndex;
import userinterface.misc.Sprite;
import userinterface.misc.SpriteManager;

/**
 * Graphic object to render an item.
 */
public class ItemGraphicObject implements IGraphicObject {

    /** The item. */
    private final Item item;

    /**
     * Constructor.
     * @param itemTmp the item
     */
    public ItemGraphicObject(final Item itemTmp) {
        item = itemTmp;
    }

    @Override
    public void render(final Graphics graphics, final MapArea viewArea) {
        MapIndex position = item.getPosition();
        if (viewArea.containesIndex(position)) {
            int x = (position.x - viewArea.pos.x) * SpriteManager.SPRITE_SIZE;
            int y = (position.y - viewArea.pos.y) * SpriteManager.SPRITE_SIZE;
            Sprite itemSprite = SpriteManager.getInstance().getItemSprite(item.getType().sprite);
            itemSprite.draw(graphics, x, y);
        }
    }
}