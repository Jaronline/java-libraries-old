package dev.jaronline.gradle.build

import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.provider.Provider

class GeoVersionConstraint(private val constraint: Provider<VersionConstraint>):
	Provider<VersionConstraint> by constraint {
	val version: String get() {
		val constraint = get()
		val version = constraint.preferredVersion

		if (version.isEmpty())
			return constraint.requiredVersion

		return version
	}

	val range: String get() {
		val constraint = get()
		var range = constraint.requiredVersion

		if (range.isEmpty()) {
			range = constraint.strictVersion

			if (range.isEmpty())
				throw IllegalArgumentException("Version constraint '${constraint.displayName}' does not have a required or strict version declared!")
		}

		return range
	}

	override fun toString(): String {
		return version
	}
}
