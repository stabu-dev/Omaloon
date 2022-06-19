//@file:JvmName("Utils")
@file:Suppress("UnusedImport")

package ol.type.bullets

import arc.func.*
import arc.math.*
import arc.math.geom.*
import arc.math.geom.Geometry.*
import arc.struct.*
import arc.util.*
import mindustry.*
import mindustry.content.*
import mindustry.core.*
import mindustry.ctype.*
import mindustry.entities.*
import mindustry.entities.units.*
import mindustry.game.*
import mindustry.gen.*
import mindustry.graphics.*
import mindustry.type.*
import mindustry.ui.fragments.*

//import kotlin.Unit as KotlinUnit
typealias Unit = mindustry.gen.Unit

//@JvmStatic
@JvmName("TODO")
@JvmOverloads
fun <T> TODO_(reason: String? = null): T = if (reason === null) TODO() else TODO(reason)

/**
 * Набор различных static утилит.
 */
@Suppress("MemberVisibilityCanBePrivate")
object Utils {
    @JvmStatic
    private val rv = Vec2()

    @JvmStatic
    private val tr = Vec2()

    @JvmStatic
    private val rect = Rect()

    @JvmStatic
    private val hitrect = Rect()

    @JvmStatic
    private val collidedBlocks = IntSet()

    @JvmName("getByIndex")
    @JvmStatic
    operator fun IntSet.get(index: Int): Int {
        var counter = 0
        val iterator = iterator()
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        while (iterator.hasNext) {
            val value = iterator.next()
            if (counter == index) {
                iterator.reset()
                return value
            }
            counter++;
        }
        throw IllegalArgumentException()
    }

    @JvmStatic
    fun findFireTarget(x: Float, y: Float, team: Team, range: Float, unitFilter: Boolf<Unit?>, buildingFilter: Boolf<Building?>?): Posc? {
        var target: Posc?
        target = team.data().units.min({ unit -> unitFilter[unit] && unit.isBurning() }) { un -> un.healthf() * un.healthf() * un.dst2(x, y) }
        // если не нашли юнитов, то ищем постройки
        if (target == null) {
            var minCost = Float.MAX_VALUE
            Vars.indexer.eachBlock(team, x, y, range, buildingFilter) { building: Building ->
                val fire = building.getFire() ?: return@eachBlock
                val cost = Mathf.sqr(building.healthf()) * Mathf.dst(x, y, building.x, building.y)
                if (cost < minCost) {
                    minCost = cost
                    target = fire
                }
            }
        }
        return target
    }

    /**
     * Определяет, горит ли юнит.
     */
    @JvmStatic
    fun Unit.isBurning(): Boolean {
        // Считаем, что эффект - горение, если он наносит урон и
        // среди его противоположностей есть вода. Такой подход позволит
        // тушить "пожары" из других модов.
        return Vars.content.getBy<Content?>(ContentType.status).find { content: Content? ->
            val s = content as StatusEffect?
            hasEffect(s) && s!!.damage > 0 && s.opposites.contains(StatusEffects.wet)
        } != null
    }

    /**
     * Возвращает один из огней на блоке или null, если блок не горит.
     */
    @JvmName("getBuildingFire")
    @JvmStatic
    fun Building.getFire(): Fire? {
        val offsetx = -(block.size - 1) / 2
        val offsety = -(block.size - 1) / 2
        for (dx in 0 until block.size) for (dy in 0 until block.size) {
            val x = tileX() + dx - offsetx
            val y = tileY() + dy - offsety
            if (Fires.has(x, y)) return Fires.get(x, y)
        }
        return null
    }

    /**
     * Принудительно устанавливает юнита в меню.
     */
    @JvmStatic
    fun setMenuUnit(type: UnitType) {
        if (Vars.headless) return
        try {
            val rendererF = MenuFragment::class.java.getDeclaredField("renderer")
            rendererF.isAccessible = true
            val renderer = rendererF[Vars.ui.menufrag] as MenuRenderer
            val flyerTypeF = MenuRenderer::class.java.getDeclaredField("flyerType")
            flyerTypeF.isAccessible = true
            flyerTypeF[renderer] = type
            val generateM = MenuRenderer::class.java.getDeclaredMethod("generate")
            generateM.isAccessible = true
            generateM.invoke(renderer)
            val cacheM = MenuRenderer::class.java.getDeclaredMethod("cache")
            cacheM.isAccessible = true
            cacheM.invoke(renderer)
        } catch (e: Throwable) {
            Log.err(e)
        }
    }

    /**
     * Ищет коллизию, игнорируя некоторые цели.
     */
    @JvmStatic
    fun linecast(hitter: Bullet, x: Float, y: Float, angle: Float, length: Float, predicate: Boolf<Healthc?>): Healthc? {
        val tmpBuilding = arrayOf<Building?>(null)
        val tmpUnit = arrayOf<Unit?>(null)
        tr.trns(angle, length)
        if (hitter.type.collidesGround) {
            World.raycastEachWorld(x, y, x + tr.x, y + tr.y) { cx: Int, cy: Int ->
                val tile = Vars.world.build(cx, cy)
                if (tile == null || tile.team === hitter.team || !predicate[tile]) return@raycastEachWorld false
                tmpBuilding[0] = tile
                true
            }
        }
        rect.setPosition(x, y).setSize(tr.x, tr.y)
        val x2 = tr.x + x
        val y2 = tr.y + y
        if (rect.width < 0) {
            rect.x += rect.width
            rect.width *= -1f
        }
        if (rect.height < 0) {
            rect.y += rect.height
            rect.height *= -1f
        }
        val expand = 3f
        rect.x -= expand
        rect.y -= expand
        rect.width += expand * 2
        rect.height += expand * 2
        Units.nearbyEnemies(hitter.team, rect) { e: Unit ->
            if (tmpUnit[0] != null && e.dst2(x, y) > tmpUnit[0]!!.dst2(x, y) || !e.checkTarget(hitter.type.collidesAir, hitter.type.collidesGround) || !predicate[e]) return@nearbyEnemies
            e.hitbox(hitrect)
            val other = hitrect
            other.x -= expand
            other.y -= expand
            other.width += expand * 2
            other.height += expand * 2
            if (raycastRect(x, y, x2, y2, other) != null) tmpUnit[0] = e
        }
        if (tmpBuilding[0] != null && tmpUnit[0] != null) {
            if (Mathf.dst2(x, y, tmpUnit[0]!!.getX(), tmpUnit[0]!!.getY()) <= Mathf.dst2(x, y, tmpBuilding[0]!!.getX(), tmpBuilding[0]!!.getY())) return tmpUnit[0]
        } else if (tmpBuilding[0] != null) return tmpBuilding[0]
        return tmpUnit[0]
    }

    /**
     * for EMP
     */
    @JvmStatic
    fun trueEachBlock(wx: Float, wy: Float, range: Float, cons: Cons<Building>) {
//        Units.nearbyBuildings(wx, wy, range, cons);
        collidedBlocks.clear()
        val tx = World.toTile(wx)
        val ty = World.toTile(wy)
        val tileRange = Mathf.floorPositive(range / Vars.tilesize)
        for (x in -tileRange + tx..tileRange + tx) {
            for (y in -tileRange + ty..tileRange + ty) {
                if (Mathf.within((x * Vars.tilesize).toFloat(), (y * Vars.tilesize).toFloat(), wx, wy, range)) {
                    val other = Vars.world.build(x, y)
                    if (other != null && !collidedBlocks.contains(other.pos())) {
                        cons[other]
                        collidedBlocks.add(other.pos())
                    }
                }
            }
        }
    }

    @JvmStatic
    @Deprecated("use allNearbyEnemies(team,x,y,radius,it->seq.add(it.<Teamc>as()))")
    fun allNearbyEnemiesOld(team: Team, x: Float, y: Float, radius: Float): Seq<Teamc> {
        val targets = Seq<Teamc>()
        Units.nearbyEnemies(team, x - radius, y - radius, radius * 2f, radius * 2f) { unit: Unit ->
            if (Mathf.within(x, y, unit.x, unit.y, radius) && !unit.dead) {
                targets.add(unit)
            }
        }
        trueEachBlock(x, y, radius) { build: Building ->
            if (build.team !== team && !build.dead && build.block != null) {
                targets.add(build)
            }
        }
        return targets
    }

    @JvmStatic
    fun allNearbyEnemies(team: Team, x: Float, y: Float, radius: Float, cons: Cons<Healthc>) {
        Units.nearbyEnemies(team, x - radius, y - radius, radius * 2f, radius * 2f) { unit: Unit ->
            if (unit.within(x, y, radius + unit.hitSize / 2f) && !unit.dead) {
                cons[unit]
            }
        }
        trueEachBlock(x, y, radius) { build: Building ->
            if (build.team !== team && !build.dead && build.block != null) {
                cons[build]
            }
        }
    }

    @JvmStatic
    fun checkForTargets(team: Team, x: Float, y: Float, radius: Float): Boolean {
        var check = false
        Units.nearbyEnemies(team, x - radius, y - radius, radius * 2f, radius * 2f) { unit: Unit ->
            if (unit.within(x, y, radius + unit.hitSize / 2f) && !unit.dead) {
                check = true
            }
        }
        trueEachBlock(x, y, radius) { build: Building ->
            if (build.team !== team && !build.dead && build.block != null) {
                check = true
            }
        }
        return check
    }

    /**
     * For reload bar.
     */
    @JvmStatic
    fun Float.stringsFixed(): String {
        return Strings.autoFixed(this, 2)
    }

    @JvmStatic
    fun mountX(unit: Unit, mount: WeaponMount): Float {
        val weapon = mount.weapon
        val rotation = unit.rotation - 90
        val weaponRotation = rotation + (if (weapon.rotate) mount.rotation else 0f)
        return unit.x + Angles.trnsx(rotation, weapon.x, weapon.y) + Angles.trnsx(weaponRotation, 0f, -mount.recoil)
    }

    @JvmStatic
    fun mountY(unit: Unit, mount: WeaponMount): Float {
        val weapon = mount.weapon
        val rotation = unit.rotation - 90
        val weaponRotation = rotation + (if (weapon.rotate) mount.rotation else 0f)
        return unit.y + Angles.trnsy(rotation, weapon.x, weapon.y) + Angles.trnsy(weaponRotation, 0f, -mount.recoil)
    }

    // Some powers below because Math.Pow is VERY slow
    @JvmStatic
    fun Float.pow3() = this * this * this

    interface Targeting {
        fun targetPos(): Vec2? {
            return null
        }
    }
}