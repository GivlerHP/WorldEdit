package com.sk89q.worldedit;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.world.NullWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.util.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NegativeHeightTest {
    static class MemoryWorld extends NullWorld {
        final int minY;
        final Map<Vector, BaseBlock> blocks = new HashMap<>();
        MemoryWorld(int minY) { this.minY = minY; }
        @Override public Vector getMinimumPoint() { return new Vector(-30000000,minY,-30000000); }
        @Override public BaseBlock getBlock(Vector pos) { return blocks.getOrDefault(pos.toBlockVector(),new BaseBlock(0)); }
        @Override public BaseBlock getLazyBlock(Vector pos) { return getBlock(pos); }
        @Override public boolean setBlock(Vector pos, BaseBlock block, boolean notify) {
            blocks.put(pos.toBlockVector(),new BaseBlock(block)); return true;
        }
    }
    static EditSession session(MemoryWorld world) {
        return new EditSession(new EventBus(),world,-1,null,new EditSessionEvent(world,null,-1,null));
    }
    @Test public void selectionSetReplaceUndoAndCopy() throws Exception {
        MemoryWorld world = new MemoryWorld(-64);
        CuboidRegion region = new CuboidRegion(world,new Vector(0,-64,0),new Vector(1,-1,1));
        assertEquals(-64,region.getMinimumPoint().getBlockY());
        assertEquals(-1,region.getMaximumPoint().getBlockY());
        EditSession edit = session(world);
        assertEquals(256,edit.setBlocks(region,new BaseBlock(1)));
        edit.flushQueue();
        assertEquals(1,world.getBlock(new Vector(0,-64,0)).getType());
        EditSession replacement = session(world);
        assertEquals(256,replacement.replaceBlocks(region,Collections.singleton(new BaseBlock(1)),new BaseBlock(3)));
        replacement.flushQueue();
        assertEquals(3,world.getBlock(new Vector(1,-1,1)).getType());
        replacement.undo(session(world));
        assertEquals(1,world.getBlock(new Vector(1,-1,1)).getType());
        replacement.redo(session(world));
        assertEquals(3,world.getBlock(new Vector(1,-1,1)).getType());
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        Operations.complete(new ForwardExtentCopy(world,region,clipboard,region.getMinimumPoint()));
        EditSession paste = session(world);
        Operations.complete(new ForwardExtentCopy(clipboard,region,region.getMinimumPoint(),paste,new Vector(4,-40,4)));
        paste.flushQueue();
        assertEquals(3,world.getBlock(new Vector(4,-40,4)).getType());
        EditSession invalid = session(world);
        assertFalse(invalid.setBlock(new Vector(0,-65,0),new BaseBlock(1)));
        assertFalse(invalid.setBlock(new Vector(0,256,0),new BaseBlock(1)));
    }
    @Test public void cylinderFillAndPolygonBelowZero() throws Exception {
        MemoryWorld world = new MemoryWorld(-64);
        EditSession edit = session(world);
        edit.makeCylinder(new Vector(0,-40,0),new com.sk89q.worldedit.patterns.SingleBlockPattern(new BaseBlock(1)),2,3,true);
        edit.flushQueue();
        assertEquals(1,world.getBlock(new Vector(0,-40,0)).getType());
        assertEquals(1,world.getBlock(new Vector(0,-38,0)).getType());
        assertEquals(0,world.getBlock(new Vector(0,0,0)).getType());
        EditSession fill = session(world);
        fill.fillXZ(new Vector(8,-20,8),new BaseBlock(3),3,3,false);
        fill.flushQueue();
        assertEquals(3,world.getBlock(new Vector(8,-22,8)).getType());
        com.sk89q.worldedit.regions.Polygonal2DRegion polygon = new com.sk89q.worldedit.regions.Polygonal2DRegion(
                world,java.util.Arrays.asList(new BlockVector2D(0,0),new BlockVector2D(3,0),new BlockVector2D(0,3)),-64,-1);
        assertEquals(-64,polygon.getMinimumPoint().getBlockY());
        assertEquals(-1,polygon.getMaximumPoint().getBlockY());
    }
    @Test public void vanillaAndClipboardBounds() {
        MemoryWorld world = new MemoryWorld(0);
        CuboidRegion vanilla = new CuboidRegion(world,new Vector(0,-64,0),new Vector(0,1,0));
        assertEquals(0,vanilla.getMinimumPoint().getBlockY());
        CuboidRegion detached = new CuboidRegion(new Vector(0,-64,0),new Vector(0,-1,0));
        assertEquals(-64,detached.getMinimumPoint().getBlockY());
    }
}

