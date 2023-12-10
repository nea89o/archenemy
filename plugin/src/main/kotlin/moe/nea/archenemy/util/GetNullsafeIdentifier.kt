package moe.nea.archenemy.util

import net.minecraftforge.artifactural.api.artifact.ArtifactIdentifier

fun getNullsafeIdentifier(identifier: ArtifactIdentifier): ArtifactIdentifier {
    return object : ArtifactIdentifier by identifier {
        override fun getClassifier(): String {
            return if (identifier.classifier == null)
                ""
            else
                identifier.classifier
        }
    }
}