package green.sailor.mc.huomautus.test

import green.sailor.mc.huomautus.annotations.registration.RegisterBlockEntity
import green.sailor.mc.testmod.generated.meta.TestModBlockEntities
import net.minecraft.block.entity.BlockEntity

@RegisterBlockEntity("testmod:test_be", ["testmod:test_block"])
class TestBlockEntity : BlockEntity(TestModBlockEntities.TEST_BE)
