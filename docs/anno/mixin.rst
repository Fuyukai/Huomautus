Mixin Helpers
=============

Huomautus comes with some helpers for mixin generation.

Setting up mixins.json
----------------------

Your mixins.json should point to the package name you specified with ``mixins`` added, as this is
where the generated mixin objects will be.

.. code-block:: json

    "package": "green.sailor.mc.testmod.generated.mixin"

MixinImpl
---------

Kotlin cannot be used directly with Mixins, so a pattern emerges which consists of writing the
actual mixin class in Java and the implementation elsewhere in Kotlin. Huomautus can automate this,
by writing your Mixin entirely in Kotlin and auto-generating a Java bridge.

.. code-block:: kotlin

    import green.sailor.mc.huomautus.annotations.MixinImpl
    import net.minecraft.client.gui.screen.TitleScreen
    import org.spongepowered.asm.mixin.injection.At
    import org.spongepowered.asm.mixin.injection.Inject
    import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

    @MixinImpl(TitleScreen::class)
    object MixinMinecraftClient {
        @Inject(at = [At(value = "HEAD")], method = ["init()V"])
        fun injectInit(info: CallbackInfo) {
            println("Hello, world!")
        }
    }

This will generate a Java class that simply calls ``MixinMinecraftClient.injectInit`` automatically
with the same params as you specify in your function.

.. note::

    In ``@Inject`` and other similar annotations, you need to use the array literal format for some
    fields instead of just the literal, due to differences in annotation handling between Java
    and Kotlin.

.. warning::

    Only methods are supported with MixinImpl. Shadowed fields are unsupported.

GenerateExtensions
------------------

``@GenerateExtensions`` can be added to an accessor interface to automatically generate extension
properties for that class, avoiding the need to cast.

.. code-block:: java

    @Mixin(MinecraftClient.class)
    @GenerateExtensions
    public interface MinecraftClientAccessor {
        @Accessor
        int getFpsCounter();
    }

Then, in some other class:

.. code-block:: kotlin

    println("FPS: ${MinecraftClient.getInstance().fpsCounter")

If only a getter is provided, these extensions are ``val`` properties; if a setter is provided too,
then these properties will be ``var`` properties.
