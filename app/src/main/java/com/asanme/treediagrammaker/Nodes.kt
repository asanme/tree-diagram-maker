package com.asanme.treediagrammaker

/**
 * @param name represents the data displayed within a diagram
 * @param children represents the list of nodes within a Diagram Tree
 */
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
