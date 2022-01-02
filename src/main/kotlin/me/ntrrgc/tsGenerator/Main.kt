package me.ntrrgc.tsGenerator

import com.google.gson.annotations.SerializedName
import com.redeagle.kotlin_bedrock.bedrock.entity.component.*
import com.redeagle.kotlin_bedrock.bedrock.entity.component.dummy.*
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmName

class SerializedNameTransformer : ClassTransformer {
    override fun transformPropertyName(propertyName: String, property: KProperty<*>, klass: KClass<*>): String {
        val propertyName =
            (property.javaField?.getAnnotationsByType(SerializedName::class.java)?.firstOrNull()?.value) ?: propertyName
        val postFix = if (property.returnType.isMarkedNullable) "?" else ""
        return propertyName + postFix
    }
}

class KotlinBedrockTransformer : ClassTransformer {
    val underScore = listOf(
        "Behavior",
        "Navigation",
        "Movement",
        "Jump"
    )

    override fun transformClassName(className: String?, klass: KClass<*>): String? {
        var className = klass.simpleName
        if (isInnerClass(klass)) {
            className = constructInnerClassName(klass)
        }

        underScore.forEach {
            className = className?.replace("MC$it", "MC${it}_")
        }

        return className
    }

    private fun isInnerClass(klass: KClass<*>) = klass.jvmName.contains("$")

    private fun constructInnerClassName(klass: KClass<*>) =
        klass.jvmName.replace(klass.java.`package`.name, "").replace("$", "").replace(".", "")
}

val kodokClass = setOf(
    "Component.kt",
    "RawComponent.kt"
)

val folder = "output/testing"

fun main() {
//    Uncomment this to print all class in kodok
//    printAllClass(File("C:\\Projects\\minecraft\\kotlin-bedrock\\core\\src\\main\\java\\com\\redeagle\\kotlin_bedrock\\bedrock\\entity\\component"), kodokClass)

    File(folder).deleteRecursively()
    File(folder).mkdirs()

    generateDummy()
    generateComponents()
}

private fun generateComponents() {
    val definitions = TypeScriptGenerator(
        rootClasses = setOf(
            MCAdmireItem::class,
            MCAttack::class,
            MCBalloonable::class,
            MCBehaviorBeg::class,
            MCBehaviorFloat::class,
            MCBehaviorFollowOwner::class,
            MCBehaviorHide::class,
            MCBehaviorHurtByTarget::class,
            MCBehaviorLookAtPlayer::class,
            MCBehaviorMeleeAttack::class,
            MCBehaviorMoveToPoi::class,
            MCBehaviorNearestAttackableTarget::class,
            MCBehaviorPickUpItems::class,
            MCBehaviorRandomFly::class,
            MCBehaviorRandomLookAround::class,
            MCBehaviorRandomStroll::class,
            MCBehaviorScared::class,
            MCBehaviorSneeze::class,
            MCBehaviorStayWhileSitting::class,
            MCBreathable::class,
            MCCanClimb::class,
            MCCanFly::class,
            MCCanPowerJump::class,
            MCCollisionBox::class,
            MCCustomHitTest::class,
            MCDamageSensor::class,
            MCDespawn::class,
            MCEconomyTradeTable::class,
            MCEquipment::class,
            MCExplode::class,
            MCFireImmune::class,
            MCFloatsInLiquid::class,
            MCFlyingSpeed::class,
            MCFootSize::class,
            MCFrictionModifier::class,
            MCHealable::class,
            MCHealth::class,
            MCHiddenWhenInvisible::class,
            MCInputGroundControlled::class,
            MCInstantDespawn::class,
            MCInteract::class,
            MCJumpStatic::class,
            MCKnockbackResistance::class,
            MCLeashable::class,
            MCLoot::class,
            MCMovement::class,
            MCMovementBasic::class,
            MCMovementFly::class,
            MCMovementGeneric::class,
            MCMovementHover::class,
            MCMovementJump::class,
            MCMovementSkip::class,
            MCMovementSway::class,
            MCNameable::class,
            MCNavigationFly::class,
            MCNavigationWalk::class,
            MCPersistent::class,
            MCPhysics::class,
            MCPushable::class,
            MCPushThrough::class,
            MCRideable::class,
            MCScaffoldingClimber::class,
            MCScale::class,
            MCScaleByAge::class,
            MCShaking::class,
            MCShareable::class,
            MCShooter::class,
            MCSittable::class,
            MCSpawnEntity::class,
            MCStackable::class,
            MCTameable::class,
            MCTameMount::class,
            MCTickWorld::class,
            MCTimer::class,
            MCTransformation::class,
            MCTypeFamily::class,
            MCWaterMovement::class,
        ),
        ignoreSuperclasses = setOf(
            Component::class
        ),
        classTransformers = listOf(
            SerializedNameTransformer(),
            KotlinBedrockTransformer()
        ),
    ).individualDefinitions

    definitions.forEach {
        val name = createFileName(it)
        File("$folder/$name.ts").writeText(it+ "\n")
    }
}

private fun createFileName(it: String): String? {
    val regex = ("interface [\\s\\S]*? \\{").toRegex()

    return regex.find(it)?.value?.replace("interface ", "")?.replace(" {", "")
}

private fun generateDummy() {
    val definitions = TypeScriptGenerator(
        rootClasses = setOf(
            MCBaby::class,
            MCCharged::class,
            MCChested::class,
            MCIgnited::class,
            MCIllagerCaptain::class,
            MCMarkVariant::class,
            MCSaddled::class,
            MCSheared::class,
            MCSkinId::class,
            MCStunned::class,
            MCTamed::class,
            MCVariant::class,
        ),
        ignoreSuperclasses = setOf(
            Component::class
        ),
        classTransformers = listOf(
            SerializedNameTransformer(),
            KotlinBedrockTransformer()
        ),
    ).individualDefinitions

    val dummyFolder = "$folder/dummies"
    File(dummyFolder).mkdirs()
    definitions.forEach {
        val name = createFileName(it)
        File("$dummyFolder/$name.ts").writeText(it + "\n")
    }
}

fun printAllClass(folder: File, exclude: Set<String>) {
    folder.walkTopDown().forEach {
        if (!exclude.contains(it.name) && it.name.contains(".kt")) {
            println(it.name.replace(".kt", "") + "::class,")
        }
    }
}

