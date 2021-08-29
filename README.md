# honey-minestom

Honey world format implementation for [Minestom](https://github.com/Minestom/Minestom/)

## Usage

```java
class MyClass {

    public void example(byte[] honeyWorldData) {
        final InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        final InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        HoneyWorld world = HoneyReaders.detectAndRead(honeyWorldData);
        final HoneyChunkLoader honeyChunkLoader = new HoneyChunkLoader(world, (chunk, honeyChunk) -> {
            // This is called when Minestom saves a chunk
            // Since Minestom does not save chunks automatically, you should not save 
            // your world in this callback because Minestom will flood this callback with 
            // every single loaded chunk in a very short period of time. Instead, you 
            // should save the world after you've manually triggered the save.

            // Saving example:
            instanceContainer.saveChunksToStorage();
            try (final FileOutputStream outputStream
                         = new FileOutputStream(new File("world.honey"))) {
                outputStream.write(HoneyWriters.writeWithLatest(world));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            // Again, *don't* do this in here, that's not a good idea!
        });
        instanceContainer.setChunkLoader(honeyChunkLoader);
    }
}
```