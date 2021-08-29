package net.valiry.honey.minestom;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.world.biomes.Biome;
import net.valiry.honey.HoneyChunk;
import net.valiry.honey.HoneyWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HoneyChunkLoader implements IChunkLoader {

    private final HoneyWorld honeyWorld;
    private final BiConsumer<Chunk, HoneyChunk> modificationCallback;

    public HoneyChunkLoader(final HoneyWorld honeyWorld) {
        this(honeyWorld, null);
    }

    public HoneyChunkLoader(final HoneyWorld honeyWorld, final BiConsumer<Chunk, HoneyChunk> modificationCallback) {
        this.honeyWorld = honeyWorld;
        this.modificationCallback = modificationCallback;
    }

    @Override
    public @NotNull CompletableFuture<@Nullable Chunk> loadChunk(@NotNull final Instance instance, final int chunkX, final int chunkZ) {
        final HoneyChunk chunk = this.honeyWorld != null ? this.honeyWorld.getChunk(chunkX, chunkZ) : null;
        return CompletableFuture.completedFuture(chunk == null
                ? null
                : this.makeChunk(chunk, instance, chunkX, chunkZ));
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunk(@NotNull final Chunk chunk) {
        final HoneyChunk honeyChunk = new HoneyChunk(
                HoneyChunk.ChunkId.of(chunk.getChunkX(), chunk.getChunkZ()),
                chunk.getSections().entrySet().stream()
                        // Map minestom chunk section to honey chunk section
                        .map(entry -> new AbstractMap.SimpleEntry<>(
                                entry.getKey(),
                                new HoneyChunk.HoneyChunkSection(this.extractStates(entry.getValue()))
                        ))
                        // Collect into map
                        .collect(Collectors.toMap(
                                o -> ((Map.Entry<Integer, HoneyChunk.HoneyChunkSection>) o).getKey(),
                                o -> ((Map.Entry<Integer, HoneyChunk.HoneyChunkSection>) o).getValue()
                        )),
                null,
                null
        );
        this.honeyWorld.put(honeyChunk);

        // Call callback
        if (this.modificationCallback != null) {
            this.modificationCallback.accept(chunk, honeyChunk);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Creates a minestom chunk from a honey chunk
     */
    private Chunk makeChunk(final HoneyChunk chunk, final Instance instance, final int chunkX, final int chunkZ) {
        final DynamicChunk dynamicChunk = new DynamicChunk(instance, this.makeBiomes(), chunkX, chunkZ);
        chunk.getSectionMap().forEach((integer, section) -> {
            final Section chunkSection = dynamicChunk.getSection(integer);
            int n = 0;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        chunkSection.setBlockAt(x, y, z, section.getStates()[n++]);
                    }
                }
            }
        });
        return dynamicChunk;
    }

    /**
     * Extracts the block state ids from a chunk section
     */
    private short[] extractStates(final Section section) {
        final short[] arr = new short[4096];
        int n = 0;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    arr[n++] = section.getBlockAt(x, y, z);
                }
            }
        }
        return arr;
    }

    /**
     * Creates a dummy biome array
     */
    private Biome[] makeBiomes() {
        final Biome[] arr = new Biome[1024];
        Arrays.fill(arr, Biome.PLAINS);
        return arr;
    }

}
