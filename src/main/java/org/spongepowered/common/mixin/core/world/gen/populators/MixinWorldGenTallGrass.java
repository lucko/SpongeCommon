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
package org.spongepowered.common.mixin.core.world.gen.populators;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Objects;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.spongepowered.api.data.type.ShrubType;
import org.spongepowered.api.data.type.ShrubTypes;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.gen.populator.Shrub;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.util.VecHelper;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(WorldGenTallGrass.class)
public abstract class MixinWorldGenTallGrass extends WorldGenerator implements Shrub {

    @Shadow private IBlockState tallGrassState;

    private WeightedTable<ShrubType> types;
    private Function<Location<Chunk>, ShrubType> override = null;
    private VariableAmount count;

    @Inject(method = "<init>(Lnet/minecraft/block/BlockTallGrass$EnumType;)V", at = @At("RETURN") )
    public void onConstructed(BlockTallGrass.EnumType type, CallbackInfo ci) {
        this.types = new WeightedTable<ShrubType>();
        this.types.add(new WeightedObject<ShrubType>((ShrubType) (Object) type, 1));
        this.count = VariableAmount.fixed(128);
    }

    @Override
    public void populate(Chunk chunk, Random random) {
        Vector3i min = chunk.getBlockMin();
        World world = (World) chunk.getWorld();
        BlockPos position = new BlockPos(min.getX(), min.getY(), min.getZ());
        ShrubType stype = ShrubTypes.TALL_GRASS;
        List<ShrubType> result;
        // The vanilla populator places down grass in batches of 128, which is a
        // decent enough amount in order to get nice 'patches' of grass so we
        // divide the total count into batches of 128.
        int n = (int) Math.ceil(this.count.getFlooredAmount(random) / 128f);
        for (int i = 0; i < n; i++) {
            BlockPos pos = position.add(random.nextInt(16) + 8, 0, random.nextInt(16) + 8);
            pos = world.getTopSolidOrLiquidBlock(pos).add(0, 1, 0);
            if (this.override != null) {
                Location<Chunk> pos2 = new Location<>(chunk, VecHelper.toVector(pos));
                stype = this.override.apply(pos2);
            } else {
                result = this.types.get(random);
                if (result.isEmpty()) {
                    continue;
                }
                stype = result.get(0);
            }
            BlockTallGrass.EnumType type = (BlockTallGrass.EnumType) (Object) stype;
            this.tallGrassState = Blocks.tallgrass.getDefaultState().withProperty(BlockTallGrass.TYPE, type);
            generate(world, random, pos);
        }
    }

    @Override
    public WeightedTable<ShrubType> getTypes() {
        return this.types;
    }

    @Override
    public VariableAmount getShrubsPerChunk() {
        return this.count;
    }

    @Override
    public void setShrubsPerChunk(VariableAmount count) {
        this.count = count;
    }

    @Override
    public Optional<Function<Location<Chunk>, ShrubType>> getSupplierOverride() {
        return Optional.ofNullable(this.override);
    }

    @Override
    public void setSupplierOverride(@Nullable Function<Location<Chunk>, ShrubType> override) {
        this.override = override;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("Type", "Shrub")
                .add("PerChunk", this.count)
                .toString();
    }

}