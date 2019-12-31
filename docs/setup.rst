Setup
=====

Huomautus is a compile-time annotation processor, so it requires some setup in your
gradle buildscript. The code below is for Kotlin DSL buildscripts.

Maven Repo
----------

TODO: publish this somewhere


Adding kapt
-----------

Huomautus is powered by ``kapt``, the Kotlin annotation processor tool. Add the following to your
plugins block:

.. code-block:: kotlin

    plugins {
        kotlin("kapt")
    }


Next, you need to configure kapt:

.. code-block:: kotlin

    kapt {
        // Required line!
        annotationProcessor("green.sailor.mc.huomautus.Processor")
        arguments {
            // Set the package for generated code to be outputted here.
            arg("sailor.huomautus.package", "green.sailor.mc.testmod.generated")
            // Set the prefix for your Blocks, Items, etc objects to have.
            arg("sailor.huomautus.prefix", "TestMod")
        }
    }

Finally, add the generated sourceset:

.. code-block:: kotlin

    sourceSets {
        main {
            java {
                srcDir("${buildDir.absolutePath}/generated/source/kaptKotlin/")
            }
        }
    }

Adding Huomautus
----------------

As Huomautus is an annotation processor, you only want to use it at compile time. None of the
annotations provided are retained at runtime, so there's no point adding it as a runtime dependency.

.. code-block:: kotlin

    val huomautusVersion = "0.1.0"

    compileOnly(group = "green.sailor.mc", name = "huomautus", verison = huomautusVersion)
    kapt(group = "green.sailor.mc", name = "huomautus", verison = huomautusVersion)

