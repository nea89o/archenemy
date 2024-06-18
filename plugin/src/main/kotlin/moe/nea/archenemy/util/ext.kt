package moe.nea.archenemy.util

import moe.nea.archenemy.mojang.ArchenemySharedExtension
import org.gradle.api.Project

val Project.sharedExtension
    get() = project.extensions.getByType(ArchenemySharedExtension::class.java)