package com.android.kmmshared.ext

import co.touchlab.stately.collections.IsoArrayDeque
import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.freeze

/**
 * 封装的线程安全可变列表
 *
 * @see [IsoArrayDeque]
 */
@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@OptIn(ExperimentalStdlibApi::class)
class SafeMutableList<T> : MutableList<T> {

    /**
     * 列表存储实现，使用 IsoArrayDeque，frozen 变量
     */
    private val _storage: IsoArrayDeque<T> = IsoArrayDeque<T>().freeze()

    /**
     * 当前列表的不可变实例原子引用，frozen 变量
     */
    private val _ref = AtomicReference<List<T>>(emptyList()).freeze()

    /**
     * 元素数量，返回不可变列表中的数量
     */
    override val size: Int
        get() = _ref.get().size

    /**
     * 是否包含元素，使用不可变列表判断
     *
     * @return Boolean
     */
    override fun contains(element: T): Boolean = _ref.get().contains(element)

    /**
     * 是否包含元素，使用不可变列表判断
     *
     * @param elements Collection<T>
     * @return Boolean
     */
    override fun containsAll(elements: Collection<T>): Boolean = _ref.get().containsAll(elements)

    /**
     * 获取元素，使用不可变列表
     *
     * @param index Int
     * @return T
     */
    override fun get(index: Int): T = _ref.get()[index]

    /**
     * 获取元素下标，使用不可变列表
     *
     * @param element T
     * @return Int
     */
    override fun indexOf(element: T): Int = _ref.get().indexOf(element)

    /**
     * 列表是否为空，使用不可变列表

     * @return Boolean
     */
    override fun isEmpty(): Boolean = _ref.get().isEmpty()

    /**
     * 可变列表迭代器
     *
     * @return MutableIterator<T>
     */
    override fun iterator(): MutableIterator<T> = _storage.iterator()

    /**
     * 可变列表迭代器
     *
     * @return MutableIterator<T>
     */
    override fun listIterator(): MutableListIterator<T> = _storage.listIterator()

    /**
     * 可变列表迭代器
     *
     * @param index 起始迭代位置
     * @return MutableIterator<T>
     */
    override fun listIterator(index: Int): MutableListIterator<T> = _storage.listIterator(index)

    /**
     * 获取元素最后一次出现的下标，使用不可变列表
     *
     * @param element T
     * @return Int
     */
    override fun lastIndexOf(element: T): Int = _ref.get().lastIndexOf(element)

    /**
     * 添加元素
     *
     * @param element T
     */
    override fun add(element: T): Boolean = _storage.add(element)

    /**
     * 添加元素
     *
     * @param index 下标
     * @param element T
     */
    override fun add(index: Int, element: T) = _storage.add(index, element)

    /**
     * 批量添加元素
     *
     * @param index 下标
     * @param elements Collection<T>
     * @return boolean
     */
    override fun addAll(index: Int, elements: Collection<T>): Boolean = _storage.addAll(index, elements)

    /**
     * 批量添加元素
     *
     * @param elements Collection<T>
     * @return boolean
     */
    override fun addAll(elements: Collection<T>): Boolean = _storage.addAll(elements)

    /**
     * 清空列表
     */
    override fun clear() = _storage.clear()

    /**
     * 删除元素
     *
     * @param element T
     * @return Boolean
     */
    override fun remove(element: T): Boolean = _storage.remove(element)

    /**
     * 批量删除元素，自动更新 ref
     *
     * @param elements Collection<T>
     * @return Boolean
     */
    override fun removeAll(elements: Collection<T>): Boolean = _storage.removeAll(elements.toSet())

    /**
     * 删除元素
     *
     * @param index 下标
     * @return T
     */
    override fun removeAt(index: Int): T = _storage.removeAt(index)

    /**
     * 删除指定集合中不存在的元素
     *
     * @param elements 指定的集合 Collection<T>
     * @return T
     */
    override fun retainAll(elements: Collection<T>): Boolean = _storage.retainAll(elements.toSet())

    /**
     * 设置元素
     *
     * @param index 下标
     * @param element T
     */
    override fun set(index: Int, element: T): T = _storage.set(index, element)

    /**
     * 截取子序列
     *
     * @param fromIndex 起始下标
     * @param toIndex 终止下标
     */
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = _storage.subList(fromIndex, toIndex)

    /**
     * 更新原子引用
     */
    fun updateRef() {
        _ref.set(_storage.toList())
    }

    /**
     * 获取不可变数组，避免直接使用 IsoArrayDeque 造成性能问题，数据回调给上层前，需要调用此方法
     *
     * @return List<T>
     */
    fun getImmutableList(): List<T> = _ref.get()

    /**
     * 移动元素
     *
     * @param fromPos 起始位置（需要把哪个位置的元素移动）
     * @param toPos 目标位置（到哪个位置）
     */
    fun moveToPosition(fromPos: Int, toPos: Int) {
        if ((fromPos !in 0 until size) || (toPos !in 0 until size)) {
            return
        }
        add(toPos, removeAt(fromPos))
    }
}