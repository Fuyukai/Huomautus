package green.sailor.mc.huomautus.testmod.mixin;

import green.sailor.mc.huomautus.annotations.GenerateExtensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.WindowProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
@GenerateExtensions
public interface MinecraftClientAccessor {
    @Accessor
    int getFpsCounter();

    @Accessor
    int setFpsCounter(int value);
}
