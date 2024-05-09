package com.blakebr0.cucumber.util;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class MultiblockPositions {
    private final List<BlockPos> positions;

    private MultiblockPositions(List<BlockPos> positions) {
        this.positions = positions;
    }

    public List<BlockPos> get(BlockPos pos) {
        return this.positions.stream().map(pos::offset).toList();
    }

    public static MultiblockPositions of(List<BlockPos> positions) {
        return new MultiblockPositions(positions);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<BlockPos> positions = new ArrayList<>();

        public Builder pos(int x, int y, int z) {
            this.positions.add(new BlockPos(x, y, z));
            return this;
        }

        public MultiblockPositions build() {
            return new MultiblockPositions(this.positions);
        }
    }
}
