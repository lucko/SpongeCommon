/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.data.property.store.common;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.property.PropertyHolder;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public abstract class AbstractBlockPropertyStore<T extends Property<?, ?>> extends AbstractSpongePropertyStore<T> {

    private final boolean checksItemStack;

    protected AbstractBlockPropertyStore(boolean checksItemStack) {
        this.checksItemStack = checksItemStack;
    }

    /**
     * Gets the property for the block, if the block is actually containing a
     * property in the first place.
     *
     * @param block The block
     * @return The property, if available
     */
    protected abstract Optional<T> getForBlock(IBlockState block);

    /**
     * This is intended for properties that are intentionally for directional
     * orientations. Typically, this is always absent, only a few properties can
     * be retrieved for specific directions.
     *
     * @param world The world
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param facing The facing direction
     * @return The property, if available
     */
    protected Optional<T> getForDirection(net.minecraft.world.World world, int x, int y, int z, EnumFacing facing) {
        return Optional.empty();
    }

    @Override
    public Optional<T> getFor(PropertyHolder propertyHolder) {
        if (propertyHolder instanceof Location) {
            final IBlockState block = (IBlockState) ((Location<?>) propertyHolder).getBlock();
            return getForBlock(block);
        } else if (this.checksItemStack && propertyHolder instanceof ItemStack) {
            final Item item = ((ItemStack) propertyHolder).getItem();
            if (item instanceof ItemBlock) {
                final Block block = ((ItemBlock) item).getBlock();
                if (block != null) {
                    return getForBlock(block.getDefaultState());
                }
            }
        } else if (propertyHolder instanceof IBlockState) {
            return getForBlock(((IBlockState) propertyHolder));
        } else if (propertyHolder instanceof Block) {
            return getForBlock(((Block) propertyHolder).getDefaultState());
        }
        return Optional.empty();
    }

    @Override
    public Optional<T> getFor(Location<World> location) {
        return getForBlock((IBlockState) location.getBlock());
    }

    @Override
    public Optional<T> getFor(Location<World> location, Direction direction) {
        return getForDirection(((net.minecraft.world.World) location.getExtent()), location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                toEnumFacing(direction));
    }
}
