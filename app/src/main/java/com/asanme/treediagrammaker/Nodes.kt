package com.asanme.treediagrammaker

class Nodes(
    val name: String,
    val children: List<Nodes>
){
    override fun toString(): String {
        return "\nName:\n$name\nChildren:\n$children\n"
    }

    fun hasChildren(): Boolean {
        return !children.isNullOrEmpty()
    }
}
